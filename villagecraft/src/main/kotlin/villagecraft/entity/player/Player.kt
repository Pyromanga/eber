package villagecraft.entity.player

import villagecraft.core.EventBus
import villagecraft.core.PlayerDiedEvent
import villagecraft.core.PlayerHealedEvent
import villagecraft.skills.SkillSet
import villagecraft.skills.SkillType

/**
 * Der Spieler-Charakter.
 * Enthält Position, Stats, Inventar, Equipment und Skills.
 */
class Player {

    // ── Position ──────────────────────────────────────────────────────────────
    var tileX: Int = 22
    var tileY: Int = 22
    var name: String = "Abenteurer"

    // ── Ressourcen ────────────────────────────────────────────────────────────
    var currentHp: Int = 100
    val maxHp: Int get() = 10 + skills.getLevel(SkillType.HITPOINTS) * 5

    var currentMana: Int = 50
    val maxMana: Int get() = 10 + skills.getLevel(SkillType.MAGIC) * 3

    var isAlive: Boolean = true
        private set

    // ── Systeme ───────────────────────────────────────────────────────────────
    val skills = SkillSet()
    val inventory = Inventory()
    val equipment = Equipment()

    // ── Kampf-Stats (Base + Equipment) ───────────────────────────────────────
    val attackLevel: Int get() = skills.getLevel(SkillType.ATTACK)
    val defenceLevel: Int get() = skills.getLevel(SkillType.DEFENCE)
    val strengthLevel: Int get() = skills.getLevel(SkillType.STRENGTH)

    val effectiveAttack: Int get() = attackLevel + equipment.totalAttackBonus()
    val effectiveDefence: Int get() = defenceLevel + equipment.totalDefenceBonus()
    val effectiveStrength: Int get() = strengthLevel + equipment.totalStrengthBonus()

    // ── Zustand ───────────────────────────────────────────────────────────────
    var currentAction: PlayerAction = PlayerAction.IDLE
    var actionProgress: Float = 0f   // 0..1 für Fortschrittsbalken
    var facingDirection: Direction = Direction.SOUTH

    // ── Movement Queue (Point-and-Click wie OSRS) ─────────────────────────────
    private val movementQueue = ArrayDeque<Pair<Int, Int>>()
    private var moveTimer: Float = 0f
    private val moveDelay: Float = 0.15f  // Sekunden pro Tile-Bewegung

    init {
        // Starter-Inventar
        inventory.add("gold_coins", 100)
        inventory.add("logs", 5)
        inventory.add("fish_cooked", 3)
    }

    fun update(deltaSeconds: Float) {
        // Bewegung verarbeiten
        moveTimer -= deltaSeconds
        if (moveTimer <= 0f && movementQueue.isNotEmpty()) {
            val (nx, ny) = movementQueue.removeFirst()
            tileX = nx
            tileY = ny
            moveTimer = moveDelay
        }

        // HP-Regeneration (langsam, wie OSRS)
        if (isAlive && currentHp < maxHp) {
            // 1 HP alle ~6 Sekunden regenerieren
            // (vereinfacht: pro Frame ein kleiner Bruchteil)
            currentHp = (currentHp + deltaSeconds / 6f).toInt().coerceAtMost(maxHp)
        }
    }

    fun moveTo(targetX: Int, targetY: Int) {
        movementQueue.clear()
        movementQueue.add(Pair(targetX, targetY))
        updateFacing(targetX, targetY)
    }

    fun queueMove(x: Int, y: Int) {
        movementQueue.add(Pair(x, y))
    }

    fun takeDamage(amount: Int, source: String = "Monster") {
        currentHp = (currentHp - amount).coerceAtLeast(0)
        if (currentHp <= 0 && isAlive) {
            isAlive = false
            EventBus.publish(PlayerDiedEvent(source))
        }
    }

    fun heal(amount: Int) {
        if (!isAlive) return
        currentHp = (currentHp + amount).coerceAtMost(maxHp)
        EventBus.publish(PlayerHealedEvent(amount))
    }

    fun respawn() {
        isAlive = true
        currentHp = maxHp / 2
        tileX = 22
        tileY = 22
        movementQueue.clear()
    }

    private fun updateFacing(tx: Int, ty: Int) {
        facingDirection = when {
            tx > tileX -> Direction.EAST
            tx < tileX -> Direction.WEST
            ty > tileY -> Direction.SOUTH
            else        -> Direction.NORTH
        }
    }

    val isMoving: Boolean get() = movementQueue.isNotEmpty()
}

// ── Hilfsdatentypen ───────────────────────────────────────────────────────────

enum class Direction { NORTH, SOUTH, EAST, WEST }

enum class PlayerAction {
    IDLE,
    WALKING,
    CHOPPING,
    MINING,
    FISHING,
    FIGHTING,
    TALKING,
    CRAFTING,
    COOKING,
    FARMING,
}
