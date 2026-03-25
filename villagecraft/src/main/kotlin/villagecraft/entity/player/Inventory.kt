package villagecraft.entity.player

import villagecraft.core.EventBus
import villagecraft.core.ItemDroppedEvent
import villagecraft.core.ItemPickedUpEvent

// ── Item-Definitionen ─────────────────────────────────────────────────────────

enum class ItemRarity(val displayName: String, val colorHex: String) {
    COMMON   ("Gewöhnlich", "#aaaaaa"),
    UNCOMMON ("Ungewöhnlich", "#55aa55"),
    RARE     ("Selten", "#5555ff"),
    EPIC     ("Episch", "#aa00aa"),
    LEGENDARY("Legendär", "#ffaa00"),
}

data class Item(
    val id: String,
    val name: String,
    val emoji: String,
    val description: String,
    val rarity: ItemRarity = ItemRarity.COMMON,
    val stackable: Boolean = true,
    val value: Int = 1,           // Goldwert beim Verkauf
    val equipSlot: EquipSlot? = null,
    val combatStats: CombatStats? = null,
)

enum class EquipSlot {
    HEAD, BODY, LEGS, FEET, WEAPON, SHIELD, RING, AMULET
}

data class CombatStats(
    val attackBonus: Int = 0,
    val defenceBonus: Int = 0,
    val strengthBonus: Int = 0,
)

/**
 * Zentrales Item-Register.
 * Alle Items werden hier definiert und per ID abgerufen.
 */
object ItemRegistry {
    private val items = mutableMapOf<String, Item>()

    init {
        register(
            // Materialien
            Item("logs",        "Holzstämme",    "🪵", "Frisch gefälltes Holz.", value = 5),
            Item("oak_logs",    "Eichenstämme",  "🪵", "Schwere Eichenstämme.", rarity = ItemRarity.UNCOMMON, value = 15),
            Item("copper_ore",  "Kupfererz",     "🟤", "Rohes Kupfererz.", value = 8),
            Item("iron_ore",    "Eisenerz",      "⚫", "Rohes Eisenerz.", rarity = ItemRarity.UNCOMMON, value = 20),
            Item("copper_bar",  "Kupferbarren",  "🟫", "Geschmolzenes Kupfer.", value = 25),
            Item("iron_bar",    "Eisenbarren",   "🔩", "Geschmolzenes Eisen.", rarity = ItemRarity.UNCOMMON, value = 50),
            Item("fish_raw",    "Roher Fisch",   "🐟", "Frisch gefangener Fisch.", value = 5),
            Item("fish_cooked", "Gekochter Fisch","🍖", "Lecker! Heilt 6 HP.", value = 12),
            Item("apple",       "Apfel",         "🍎", "Knackiger Apfel. Heilt 2 HP.", value = 3),
            Item("mushroom",    "Pilz",          "🍄", "Sieht essbar aus... hoffentlich.", value = 4),

            // Ausrüstung
            Item("bronze_sword","Bronzeschwert", "⚔️", "Ein einfaches Bronzeschwert.",
                stackable = false, value = 80, equipSlot = EquipSlot.WEAPON,
                combatStats = CombatStats(attackBonus = 4, strengthBonus = 3)),
            Item("iron_sword",  "Eisenschwert",  "🗡️", "Solide Klinge.",
                stackable = false, rarity = ItemRarity.UNCOMMON, value = 200, equipSlot = EquipSlot.WEAPON,
                combatStats = CombatStats(attackBonus = 8, strengthBonus = 6)),
            Item("leather_body","Lederweste",    "🥋", "Leichter Schutz.",
                stackable = false, value = 120, equipSlot = EquipSlot.BODY,
                combatStats = CombatStats(defenceBonus = 5)),
            Item("wooden_shield","Holzschild",   "🛡️", "Billig aber funktional.",
                stackable = false, value = 60, equipSlot = EquipSlot.SHIELD,
                combatStats = CombatStats(defenceBonus = 3)),

            // Quests & Sonstiges
            Item("gold_coins",  "Goldmünzen",    "🪙", "Das universelle Zahlungsmittel.", value = 1),
            Item("letter",      "Brief",         "📜", "Ein versiegelter Brief.", stackable = false, value = 0),
            Item("flower",      "Blume",         "🌸", "Eine hübsche Wildblume.", value = 2),
            Item("strange_seed","Mysteriöser Samen","🌰", "Was wohl draus wird?",
                rarity = ItemRarity.RARE, stackable = false, value = 50),
        )
    }

    private fun register(vararg itemList: Item) = itemList.forEach { items[it.id] = it }

    fun get(id: String): Item = items[id] ?: error("Unbekanntes Item: $id")
    fun getOrNull(id: String): Item? = items[id]
    fun all(): Collection<Item> = items.values
}

// ── Inventar ──────────────────────────────────────────────────────────────────

data class ItemStack(val item: Item, var amount: Int)

/**
 * Spieler-Inventar mit fester Slot-Anzahl (wie OSRS: 28 Slots).
 */
class Inventory(val maxSlots: Int = 28) {

    private val slots = mutableListOf<ItemStack>()

    val size: Int get() = slots.size
    val isFull: Boolean get() = slots.count { !it.item.stackable || it.amount == 0 } >= maxSlots

    fun getSlots(): List<ItemStack> = slots.toList()

    fun add(itemId: String, amount: Int = 1): Boolean {
        val item = ItemRegistry.get(itemId)
        if (item.stackable) {
            val existing = slots.find { it.item.id == itemId }
            if (existing != null) {
                existing.amount += amount
                EventBus.publish(ItemPickedUpEvent(itemId, amount))
                return true
            }
        }
        if (slots.size >= maxSlots) return false
        slots.add(ItemStack(item, amount))
        EventBus.publish(ItemPickedUpEvent(itemId, amount))
        return true
    }

    fun remove(itemId: String, amount: Int = 1): Boolean {
        val stack = slots.find { it.item.id == itemId } ?: return false
        if (stack.amount < amount) return false
        stack.amount -= amount
        if (stack.amount == 0) slots.remove(stack)
        EventBus.publish(ItemDroppedEvent(itemId, amount))
        return true
    }

    fun has(itemId: String, amount: Int = 1): Boolean =
        slots.find { it.item.id == itemId }?.amount?.let { it >= amount } ?: false

    fun count(itemId: String): Int = slots.find { it.item.id == itemId }?.amount ?: 0

    fun countGold(): Int = count("gold_coins")
}

// ── Equipment ─────────────────────────────────────────────────────────────────

class Equipment {
    private val equipped = mutableMapOf<EquipSlot, Item>()

    fun equip(item: Item): Item? {
        val slot = item.equipSlot ?: return null
        val old = equipped[slot]
        equipped[slot] = item
        return old
    }

    fun unequip(slot: EquipSlot): Item? = equipped.remove(slot)
    fun get(slot: EquipSlot): Item? = equipped[slot]
    fun all(): Map<EquipSlot, Item> = equipped.toMap()

    fun totalAttackBonus(): Int = equipped.values.sumOf { it.combatStats?.attackBonus ?: 0 }
    fun totalDefenceBonus(): Int = equipped.values.sumOf { it.combatStats?.defenceBonus ?: 0 }
    fun totalStrengthBonus(): Int = equipped.values.sumOf { it.combatStats?.strengthBonus ?: 0 }
}
