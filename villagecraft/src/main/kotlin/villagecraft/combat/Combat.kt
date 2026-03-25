package villagecraft.combat

import villagecraft.entity.player.Player
import villagecraft.skills.SkillType
import kotlin.math.floor
import kotlin.random.Random

/**
 * OSRS-inspiriertes Kampfsystem.
 *
 * Kernmechaniken:
 * - Ticks: Kampf läuft in Ticks (0.6s pro Tick wie in OSRS)
 * - Max Hit: Basiert auf Strength-Level + Equipment-Boni
 * - Accuracy: Angriff vs. Verteidigung (Roll-System)
 * - XP: Attack + Strength + HP XP pro Schlag
 */
class CombatEngine {

    companion object {
        const val TICK_SECONDS = 0.6f
        const val XP_PER_DAMAGE = 4   // 4 XP pro Schadenspunkt (wie OSRS)
        const val HP_XP_PER_DAMAGE = 1 // +1.33 HP-XP gerundet auf 1
    }

    /**
     * Berechnet den maximalen Trefferschaden eines Spielers.
     * Formel angelehnt an OSRS: floor(0.5 + effStrength * (strengthBonus + 64) / 640)
     */
    fun calcMaxHit(player: Player): Int {
        val effectiveStrength = player.effectiveStrength + 8  // +8 für Gebet-Bonus (vereinfacht)
        return floor(0.5 + effectiveStrength * (player.equipment.totalStrengthBonus() + 64) / 640.0).toInt()
            .coerceAtLeast(1)
    }

    /**
     * Würfelt ob ein Angriff trifft.
     * Basiert auf effektivem Angriff vs. Monster-Verteidigung.
     */
    fun rollHit(attackerRoll: Int, defenderRoll: Int): Boolean {
        return if (attackerRoll > defenderRoll) {
            val hitChance = 1.0 - (defenderRoll + 2.0) / (2.0 * (attackerRoll + 1))
            Random.nextDouble() < hitChance
        } else {
            val hitChance = attackerRoll / (2.0 * (defenderRoll + 1))
            Random.nextDouble() < hitChance
        }
    }

    /**
     * Führt einen Angriff des Spielers auf ein Monster aus.
     * Gibt den Schaden zurück (0 = miss).
     */
    fun playerAttack(player: Player, monster: Monster): Int {
        val maxAttackRoll = player.effectiveAttack * 4 + 8
        val monsterDefRoll = monster.defenceLevel * 4 + 8

        val hit = rollHit(maxAttackRoll, monsterDefRoll)
        val damage = if (hit) Random.nextInt(0, calcMaxHit(player) + 1) else 0

        if (damage > 0) {
            monster.currentHp -= damage
            // XP vergeben
            player.skills.addXp(SkillType.ATTACK, damage * XP_PER_DAMAGE)
            player.skills.addXp(SkillType.HITPOINTS, damage * HP_XP_PER_DAMAGE)
        }

        return damage
    }

    /**
     * Monster greift den Spieler an.
     * Gibt den Schaden zurück.
     */
    fun monsterAttack(monster: Monster, player: Player): Int {
        val monAtkRoll = monster.attackLevel * 4 + 8
        val playerDefRoll = player.effectiveDefence * 4 + 8

        val hit = rollHit(monAtkRoll, playerDefRoll)
        val damage = if (hit) Random.nextInt(0, monster.maxHit + 1) else 0

        if (damage > 0) {
            player.takeDamage(damage, monster.name)
            // Defence XP für abwehren
            player.skills.addXp(SkillType.DEFENCE, (damage * 1.3).toInt())
        }

        return damage
    }
}

// ── Monster ───────────────────────────────────────────────────────────────────

data class LootEntry(val itemId: String, val minAmount: Int = 1, val maxAmount: Int = 1, val weight: Int = 100)

data class Monster(
    val id: String,
    val name: String,
    val emoji: String,
    val level: Int,
    val maxHp: Int,
    val attackLevel: Int,
    val defenceLevel: Int,
    val maxHit: Int,
    val xpOnKill: Int,
    val lootTable: List<LootEntry> = emptyList(),
    val description: String = "",
) {
    var currentHp: Int = maxHp
    val isAlive: Boolean get() = currentHp > 0

    /** Generiert Loot beim Tod des Monsters */
    fun rollLoot(): List<Pair<String, Int>> {
        val loot = mutableListOf<Pair<String, Int>>()
        val totalWeight = lootTable.sumOf { it.weight }

        lootTable.forEach { entry ->
            val roll = Random.nextInt(totalWeight)
            if (roll < entry.weight) {
                val amount = Random.nextInt(entry.minAmount, entry.maxAmount + 1)
                loot.add(entry.itemId to amount)
            }
        }

        return loot
    }
}

/** Monster-Bibliothek mit typischen OSRS/Fantasy-Gegnern */
object MonsterRegistry {

    val GOBLIN = Monster(
        id = "goblin", name = "Goblin", emoji = "👺",
        level = 2, maxHp = 5, attackLevel = 1, defenceLevel = 1, maxHit = 2,
        xpOnKill = 13,
        lootTable = listOf(
            LootEntry("gold_coins", 1, 5, 80),
            LootEntry("logs", 1, 1, 30),
        ),
        description = "Ein kleiner, fieser Goblin. Er riecht nach altem Käse."
    )

    val GIANT_RAT = Monster(
        id = "giant_rat", name = "Riesenratte", emoji = "🐀",
        level = 1, maxHp = 3, attackLevel = 1, defenceLevel = 1, maxHit = 1,
        xpOnKill = 6,
        lootTable = listOf(LootEntry("gold_coins", 1, 2, 50)),
        description = "So groß wie ein Hund. Stärker als sie aussieht."
    )

    val SKELETON = Monster(
        id = "skeleton", name = "Skelett", emoji = "💀",
        level = 15, maxHp = 15, attackLevel = 12, defenceLevel = 8, maxHit = 6,
        xpOnKill = 58,
        lootTable = listOf(
            LootEntry("gold_coins", 5, 25, 90),
            LootEntry("iron_ore", 1, 2, 30),
            LootEntry("bronze_sword", 1, 1, 5),
        ),
        description = "Klapper klapper. Mag keine Lebenden."
    )

    val DARK_WIZARD = Monster(
        id = "dark_wizard", name = "Dunkler Zauberer", emoji = "🧙",
        level = 20, maxHp = 20, attackLevel = 18, defenceLevel = 5, maxHit = 8,
        xpOnKill = 76,
        lootTable = listOf(
            LootEntry("gold_coins", 20, 60, 100),
            LootEntry("strange_seed", 1, 1, 3),
        ),
        description = "Murmelt unverständliche Beschwörungen. Glaub ihm kein Wort."
    )

    fun all() = listOf(GOBLIN, GIANT_RAT, SKELETON, DARK_WIZARD)
    fun byId(id: String) = all().find { it.id == id }
}

// ── Aktiver Kampf ─────────────────────────────────────────────────────────────

/**
 * Repräsentiert einen laufenden Kampf zwischen Spieler und Monster.
 * Wird vom GameWorld verwaltet und pro Tick verarbeitet.
 */
class ActiveCombat(
    val player: Player,
    val monster: Monster,
    private val engine: CombatEngine = CombatEngine(),
) {
    val log = mutableListOf<CombatLogEntry>()
    private var tickTimer: Float = 0f
    var isOver: Boolean = false
    var playerWon: Boolean = false

    fun update(deltaSeconds: Float): List<CombatLogEntry> {
        if (isOver) return emptyList()

        tickTimer += deltaSeconds
        if (tickTimer < CombatEngine.TICK_SECONDS) return emptyList()
        tickTimer -= CombatEngine.TICK_SECONDS

        val newEntries = mutableListOf<CombatLogEntry>()

        // Spieler greift an
        val playerDmg = engine.playerAttack(player, monster)
        newEntries.add(CombatLogEntry(
            actor = player.name,
            target = monster.name,
            damage = playerDmg,
            hit = playerDmg > 0
        ))

        if (!monster.isAlive) {
            isOver = true
            playerWon = true
            // Kill XP
            player.skills.addXp(SkillType.STRENGTH, monster.xpOnKill)
            // Loot
            val loot = monster.rollLoot()
            loot.forEach { (itemId, amount) -> player.inventory.add(itemId, amount) }
            newEntries.add(CombatLogEntry(actor = monster.name, target = "", damage = 0, hit = false,
                message = "⚰️ ${monster.name} besiegt! ${loot.joinToString { "${it.second}x ${it.first}" }}"))
            return newEntries
        }

        // Monster greift an
        val monsterDmg = engine.monsterAttack(monster, player)
        newEntries.add(CombatLogEntry(
            actor = monster.name,
            target = player.name,
            damage = monsterDmg,
            hit = monsterDmg > 0
        ))

        if (!player.isAlive) {
            isOver = true
            playerWon = false
        }

        log.addAll(newEntries)
        return newEntries
    }
}

data class CombatLogEntry(
    val actor: String,
    val target: String,
    val damage: Int,
    val hit: Boolean,
    val message: String? = null,
)
