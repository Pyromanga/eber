package villagecraft.skills

import villagecraft.entity.player.Player
import villagecraft.entity.player.PlayerAction
import villagecraft.world.TileMap
import villagecraft.world.TileType
import kotlin.random.Random

/**
 * Skill-Aktionen: Holzfällen, Mining, Angeln, Sammeln.
 *
 * Jede Aktion läuft tickbasiert (wie OSRS):
 * - Spieler klickt auf eine Ressource → Aktion startet
 * - Jeder Tick: Erfolgsroll (abhängig von Level)
 * - Bei Erfolg: Item ins Inventar, XP vergeben
 * - Ressource erschöpft → Tile verändert sich temporär
 */
object SkillActions {

    fun startWoodcutting(player: Player, tileMap: TileMap, tx: Int, ty: Int): SkillSession? {
        val tile = tileMap.get(tx, ty)
        if (tile != TileType.TREE) return null
        if (!player.skills.meetsRequirement(SkillType.WOODCUTTING, 1)) return null

        return SkillSession(
            player = player,
            skill = SkillType.WOODCUTTING,
            action = PlayerAction.CHOPPING,
            tickSeconds = 2.4f,
            itemId = "logs",
            xpPerItem = 25,
            successChance = calcSuccessChance(player.skills.getLevel(SkillType.WOODCUTTING), 1),
            onSuccess = {
                tileMap.set(tx, ty, TileType.GRASS) // Baum fällt
                // TODO: Respawn-Timer setzen
            }
        )
    }

    fun startMining(player: Player, tileMap: TileMap, tx: Int, ty: Int): SkillSession? {
        val tile = tileMap.get(tx, ty)
        val (itemId, levelReq, xp) = when (tile) {
            TileType.COPPER_ORE -> Triple("copper_ore", 1, 17)
            TileType.IRON_ORE   -> Triple("iron_ore", 15, 35)
            else -> return null
        }
        if (!player.skills.meetsRequirement(SkillType.MINING, levelReq)) return null

        return SkillSession(
            player = player,
            skill = SkillType.MINING,
            action = PlayerAction.MINING,
            tickSeconds = 3.0f,
            itemId = itemId,
            xpPerItem = xp,
            successChance = calcSuccessChance(player.skills.getLevel(SkillType.MINING), levelReq),
            onSuccess = {
                tileMap.set(tx, ty, TileType.ROCK) // Erz erschöpft
            }
        )
    }

    fun startFishing(player: Player, tx: Int, ty: Int, nearWater: Boolean): SkillSession? {
        if (!nearWater) return null
        if (!player.skills.meetsRequirement(SkillType.FISHING, 1)) return null

        return SkillSession(
            player = player,
            skill = SkillType.FISHING,
            action = PlayerAction.FISHING,
            tickSeconds = 4.0f,
            itemId = "fish_raw",
            xpPerItem = 40,
            successChance = calcSuccessChance(player.skills.getLevel(SkillType.FISHING), 1),
        )
    }

    fun startForaging(player: Player): SkillSession? {
        if (!player.skills.meetsRequirement(SkillType.FORAGING, 1)) return null

        val items = listOf("apple" to 8, "mushroom" to 6, "flower" to 5)
        val (itemId, xp) = items.random()

        return SkillSession(
            player = player,
            skill = SkillType.FORAGING,
            action = PlayerAction.IDLE,
            tickSeconds = 3.5f,
            itemId = itemId,
            xpPerItem = xp,
            successChance = 0.7f,
        )
    }

    /**
     * Berechnet Erfolgswahrscheinlichkeit basierend auf Level und Anforderung.
     * Niedriglevels = langsamer (wie OSRS).
     */
    private fun calcSuccessChance(level: Int, req: Int): Float {
        val effective = (level - req).coerceAtLeast(0)
        return (0.3f + effective * 0.007f).coerceIn(0.3f, 0.95f)
    }
}

/**
 * Eine aktive Skill-Session (läuft bis Inventar voll oder Aktion abgebrochen).
 */
class SkillSession(
    private val player: Player,
    val skill: SkillType,
    val action: PlayerAction,
    val tickSeconds: Float,
    val itemId: String,
    val xpPerItem: Int,
    private val successChance: Float,
    private val onSuccess: (() -> Unit)? = null,
    val isRepeating: Boolean = true,
) {
    var timer: Float = 0f
    var isActive: Boolean = true
    var totalItemsGathered: Int = 0

    /**
     * Wird jeden Frame aufgerufen.
     * Gibt true zurück wenn ein Item gewonnen wurde.
     */
    fun update(deltaSeconds: Float): TickResult {
        if (!isActive) return TickResult.INACTIVE

        // Inventar voll? Abbrechen.
        if (player.inventory.isFull) {
            isActive = false
            return TickResult.INVENTORY_FULL
        }

        timer += deltaSeconds
        val progress = (timer / tickSeconds).coerceIn(0f, 1f)
        player.actionProgress = progress

        if (timer >= tickSeconds) {
            timer = 0f

            // Erfolgsroll
            if (Random.nextFloat() < successChance) {
                val added = player.inventory.add(itemId)
                if (added) {
                    player.skills.addXp(skill, xpPerItem)
                    totalItemsGathered++
                    onSuccess?.invoke()
                    if (!isRepeating) {
                        isActive = false
                        return TickResult.SUCCESS_DONE
                    }
                    return TickResult.SUCCESS
                }
            }
        }

        return TickResult.IN_PROGRESS
    }

    fun cancel() {
        isActive = false
        player.actionProgress = 0f
        player.currentAction = PlayerAction.IDLE
    }
}

enum class TickResult {
    IN_PROGRESS,
    SUCCESS,
    SUCCESS_DONE,
    INVENTORY_FULL,
    INACTIVE,
}
