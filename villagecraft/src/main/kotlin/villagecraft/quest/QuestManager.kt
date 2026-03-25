package villagecraft.quest

import villagecraft.core.EventBus
import villagecraft.core.QuestCompletedEvent
import villagecraft.core.QuestStartedEvent
import villagecraft.world.GameWorld

// ── Quest-Datenmodell ─────────────────────────────────────────────────────────

enum class QuestStatus { NOT_STARTED, IN_PROGRESS, COMPLETED }

data class QuestObjective(
    val id: String,
    val description: String,
    val isComplete: (GameWorld) -> Boolean,
)

data class QuestReward(
    val goldCoins: Int = 0,
    val xpRewards: Map<villagecraft.skills.SkillType, Int> = emptyMap(),
    val items: List<Pair<String, Int>> = emptyList(),  // itemId to amount
)

data class QuestDefinition(
    val id: String,
    val title: String,
    val description: String,
    val emoji: String,
    val difficulty: QuestDifficulty,
    val objectives: List<QuestObjective>,
    val reward: QuestReward,
    val prerequisiteQuestIds: List<String> = emptyList(),
    val completionText: String = "Quest abgeschlossen!",
)

enum class QuestDifficulty(val displayName: String, val colorHex: String) {
    BEGINNER  ("Anfänger",    "#55aa55"),
    EASY      ("Leicht",      "#aaaaff"),
    MEDIUM    ("Mittel",      "#ffaa00"),
    HARD      ("Schwer",      "#ff5555"),
    LEGENDARY ("Legendär",    "#ffaa00"),
}

// ── Quest-Register ─────────────────────────────────────────────────────────────

object QuestRegistry {

    val ALL_QUESTS: List<QuestDefinition> = listOf(

        QuestDefinition(
            id = "barts_first_quest",
            title = "Bart braucht Holz",
            description = "Händler Bart hat sein Lager geleert. Er braucht 10 Holzstämme.",
            emoji = "🪵",
            difficulty = QuestDifficulty.BEGINNER,
            objectives = listOf(
                QuestObjective("collect_logs", "Sammle 10 Holzstämme") { w ->
                    w.player.inventory.has("logs", 10)
                }
            ),
            reward = QuestReward(
                goldCoins = 50,
                xpRewards = mapOf(villagecraft.skills.SkillType.WOODCUTTING to 200)
            ),
            completionText = "Bart strahlt dich an. 'Wunderbar - ein Schnäppchen für uns beide!'"
        ),

        QuestDefinition(
            id = "brunos_farm_quest",
            title = "Brunos Ernte",
            description = "Bauer Bruno ist zu schläfrig zum Ernten. Hilf ihm mit der Apfelernte.",
            emoji = "🍎",
            difficulty = QuestDifficulty.BEGINNER,
            objectives = listOf(
                QuestObjective("collect_apples", "Sammle 5 Äpfel") { w ->
                    w.player.inventory.has("apple", 5)
                },
                QuestObjective("give_apples", "Bringe Bruno die Äpfel") { w ->
                    w.questManager.getObjectiveFlag("brunos_farm_quest", "gave_apples")
                }
            ),
            reward = QuestReward(
                goldCoins = 30,
                xpRewards = mapOf(villagecraft.skills.SkillType.FARMING to 150)
            ),
            completionText = "Bruno gähnt zufrieden. 'Danke... zzz... du bist ein guter Mensch...'"
        ),

        QuestDefinition(
            id = "minervas_knowledge",
            title = "Minervas Wissen",
            description = "Die weise Minerva möchte wissen ob du wirklich abenteuerlich bist. Besieg 5 Goblins!",
            emoji = "🦉",
            difficulty = QuestDifficulty.EASY,
            prerequisiteQuestIds = listOf("barts_first_quest"),
            objectives = listOf(
                QuestObjective("kill_goblins", "Besiege 5 Goblins") { w ->
                    (w.questManager.getCounter("minervas_knowledge", "goblin_kills") ?: 0) >= 5
                }
            ),
            reward = QuestReward(
                goldCoins = 100,
                xpRewards = mapOf(
                    villagecraft.skills.SkillType.ATTACK to 500,
                    villagecraft.skills.SkillType.STRENGTH to 500,
                )
            ),
            completionText = "'Beeindruckend... hoo hoo. Du hast das Potential, die alten Geheimnisse zu lüften.'"
        ),

        QuestDefinition(
            id = "the_ancient_seed",
            title = "Der uralte Samen",
            description = "Du hast einen mysteriösen Samen gefunden. Minerva weiß mehr...",
            emoji = "🌰",
            difficulty = QuestDifficulty.MEDIUM,
            prerequisiteQuestIds = listOf("minervas_knowledge"),
            objectives = listOf(
                QuestObjective("find_seed", "Finde den mysteriösen Samen") { w ->
                    w.player.inventory.has("strange_seed")
                },
                QuestObjective("talk_minerva", "Sprich mit der weisen Minerva über den Samen") { w ->
                    w.questManager.getObjectiveFlag("the_ancient_seed", "talked_minerva")
                },
                QuestObjective("plant_seed", "Pflanze den Samen bei Vollmond") { w ->
                    w.questManager.getObjectiveFlag("the_ancient_seed", "seed_planted")
                }
            ),
            reward = QuestReward(
                goldCoins = 250,
                xpRewards = mapOf(
                    villagecraft.skills.SkillType.FARMING to 1000,
                    villagecraft.skills.SkillType.MAGIC to 500,
                )
            ),
            completionText = "Aus dem Boden wächst etwas Strahlendes. Die Natur hält Geheimnisse bereit..."
        ),
    )

    fun byId(id: String) = ALL_QUESTS.find { it.id == id }
}

// ── Quest-Manager ─────────────────────────────────────────────────────────────

class QuestManager {

    private val statusMap = mutableMapOf<String, QuestStatus>()
    private val objectiveFlags = mutableMapOf<String, MutableMap<String, Boolean>>()
    private val counters = mutableMapOf<String, MutableMap<String, Int>>()

    fun start(questId: String) {
        if (statusMap[questId] == QuestStatus.IN_PROGRESS) return
        statusMap[questId] = QuestStatus.IN_PROGRESS
        EventBus.publish(QuestStartedEvent(questId))
    }

    fun complete(questId: String) {
        statusMap[questId] = QuestStatus.COMPLETED
        val def = QuestRegistry.byId(questId) ?: return
        EventBus.publish(QuestCompletedEvent(questId))
    }

    fun isActive(questId: String) = statusMap[questId] == QuestStatus.IN_PROGRESS
    fun isCompleted(questId: String) = statusMap[questId] == QuestStatus.COMPLETED
    fun getStatus(questId: String) = statusMap[questId] ?: QuestStatus.NOT_STARTED

    fun setObjectiveFlag(questId: String, flag: String, value: Boolean = true) {
        objectiveFlags.getOrPut(questId) { mutableMapOf() }[flag] = value
    }

    fun getObjectiveFlag(questId: String, flag: String): Boolean =
        objectiveFlags[questId]?.get(flag) ?: false

    fun incrementCounter(questId: String, counter: String, amount: Int = 1) {
        val map = counters.getOrPut(questId) { mutableMapOf() }
        map[counter] = (map[counter] ?: 0) + amount
    }

    fun getCounter(questId: String, counter: String): Int? =
        counters[questId]?.get(counter)

    /** Prüft alle aktiven Quests ob sie automatisch abschließbar sind */
    fun checkAutoComplete(world: GameWorld) {
        QuestRegistry.ALL_QUESTS
            .filter { isActive(it.id) }
            .forEach { quest ->
                val allComplete = quest.objectives.all { obj -> obj.isComplete(world) }
                if (allComplete) {
                    // Quest bereit zum Abschließen (muss noch beim NPC abgegeben werden)
                    // Oder auto-complete wenn kein turn-in NPC definiert
                }
            }
    }

    fun getActiveQuests(): List<QuestDefinition> =
        QuestRegistry.ALL_QUESTS.filter { isActive(it.id) }

    fun getCompletedQuests(): List<QuestDefinition> =
        QuestRegistry.ALL_QUESTS.filter { isCompleted(it.id) }

    fun getAvailableQuests(world: GameWorld): List<QuestDefinition> =
        QuestRegistry.ALL_QUESTS.filter { quest ->
            getStatus(quest.id) == QuestStatus.NOT_STARTED &&
            quest.prerequisiteQuestIds.all { isCompleted(it) }
        }
}
