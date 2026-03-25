package villagecraft.skills

import villagecraft.core.EventBus
import villagecraft.core.SkillLevelUpEvent
import villagecraft.core.SkillXpGainedEvent

/**
 * Alle Skills die der Spieler leveln kann.
 * Inspiriert von OSRS, angereichert mit AC-Flavor.
 */
enum class SkillType(val displayName: String, val emoji: String, val category: SkillCategory) {
    // Gathering
    WOODCUTTING ("Holzfällen",    "🪓", SkillCategory.GATHERING),
    MINING      ("Bergbau",       "⛏️", SkillCategory.GATHERING),
    FISHING     ("Angeln",        "🎣", SkillCategory.GATHERING),
    FORAGING    ("Sammeln",       "🌿", SkillCategory.GATHERING),
    FARMING     ("Gärtnern",      "🌱", SkillCategory.GATHERING),

    // Crafting
    CRAFTING    ("Handwerk",      "🔨", SkillCategory.CRAFTING),
    COOKING     ("Kochen",        "🍳", SkillCategory.CRAFTING),
    SMITHING    ("Schmieden",     "⚒️", SkillCategory.CRAFTING),

    // Combat
    ATTACK      ("Angriff",       "⚔️", SkillCategory.COMBAT),
    DEFENCE     ("Verteidigung",  "🛡️", SkillCategory.COMBAT),
    STRENGTH    ("Stärke",        "💪", SkillCategory.COMBAT),
    HITPOINTS   ("Lebenspunkte",  "❤️", SkillCategory.COMBAT),
    MAGIC       ("Magie",         "✨", SkillCategory.COMBAT),

    // Social
    CHARM       ("Charme",        "💬", SkillCategory.SOCIAL),
    TRADING     ("Handel",        "💰", SkillCategory.SOCIAL),
}

enum class SkillCategory(val displayName: String) {
    GATHERING("Sammeln"),
    CRAFTING("Handwerk"),
    COMBAT("Kampf"),
    SOCIAL("Soziales"),
}

/**
 * XP-Tabelle kompatibel mit OSRS-Formel:
 * XP für Level n ≈ (1/8) * Σ(floor(l + 300 * 2^(l/7)))
 */
object XpTable {
    private val table: IntArray = IntArray(100)

    init {
        var points = 0.0
        for (level in 1..99) {
            points += Math.floor(level + 300.0 * Math.pow(2.0, level / 7.0))
            table[level] = (points / 4).toInt()
        }
    }

    /** XP die benötigt wird um Level [level] zu erreichen */
    fun xpForLevel(level: Int): Int = if (level <= 1) 0 else table[(level - 1).coerceIn(1, 99)]

    /** Berechnet das Level basierend auf gesammelter XP */
    fun levelForXp(xp: Int): Int {
        for (level in 98 downTo 1) {
            if (xp >= table[level]) return level + 1
        }
        return 1
    }

    /** XP bis zum nächsten Level */
    fun xpToNextLevel(currentXp: Int): Int {
        val currentLevel = levelForXp(currentXp)
        if (currentLevel >= 99) return 0
        return xpForLevel(currentLevel + 1) - currentXp
    }
}

/**
 * Der gesamte Skill-Stand eines Spielers.
 */
class SkillSet {

    private val xpMap = mutableMapOf<SkillType, Int>()

    init {
        // Alle Skills bei 0 XP (Level 1) initialisieren
        SkillType.values().forEach { xpMap[it] = 0 }
        // HP startet bei Level 10 (wie OSRS)
        xpMap[SkillType.HITPOINTS] = XpTable.xpForLevel(10)
    }

    fun getXp(skill: SkillType): Int = xpMap[skill] ?: 0

    fun getLevel(skill: SkillType): Int = XpTable.levelForXp(getXp(skill))

    fun getTotalLevel(): Int = SkillType.values().sumOf { getLevel(it) }

    /**
     * Fügt XP zu einem Skill hinzu.
     * Feuert [SkillXpGainedEvent] und bei Level-Up [SkillLevelUpEvent].
     */
    fun addXp(skill: SkillType, amount: Int) {
        val oldLevel = getLevel(skill)
        xpMap[skill] = (getXp(skill) + amount)
        val newLevel = getLevel(skill)

        EventBus.publish(SkillXpGainedEvent(skill, amount))

        if (newLevel > oldLevel) {
            EventBus.publish(SkillLevelUpEvent(skill, newLevel))
        }
    }

    /** Prüft ob der Spieler das Mindestlevel für eine Aktion hat */
    fun meetsRequirement(skill: SkillType, requiredLevel: Int): Boolean =
        getLevel(skill) >= requiredLevel

    fun toDisplayString(): String = buildString {
        SkillCategory.values().forEach { cat ->
            appendLine("── ${cat.displayName} ──")
            SkillType.values().filter { it.category == cat }.forEach { skill ->
                val level = getLevel(skill)
                val xp = getXp(skill)
                appendLine("  ${skill.emoji} ${skill.displayName.padEnd(16)} Lvl $level  ($xp XP)")
            }
        }
    }
}
