package villagecraft.world

import villagecraft.core.DaytimeChangedEvent
import villagecraft.core.EventBus
import villagecraft.core.SeasonChangedEvent
import villagecraft.entity.npc.NpcManager
import villagecraft.entity.player.Player
import villagecraft.quest.QuestManager

/**
 * Hält den gesamten Zustand der Spielwelt:
 * - Karte (TileMap)
 * - Spieler
 * - NPCs
 * - Uhrzeit & Jahreszeit
 * - Quests
 */
class GameWorld {

    val tileMap = TileMap(width = 64, height = 64)
    val player = Player()
    val npcManager = NpcManager()
    val questManager = QuestManager()
    val worldClock = WorldClock()

    init {
        tileMap.generateDefault()
        npcManager.spawnDefaultNpcs(tileMap)
    }

    /** Wird jeden Frame vom GameLoop aufgerufen. deltaSeconds = vergangene Zeit seit letztem Frame */
    fun update(deltaSeconds: Float) {
        worldClock.advance(deltaSeconds)
        player.update(deltaSeconds)
        npcManager.update(deltaSeconds, this)
        questManager.checkAutoComplete(this)
    }
}

// ── Uhrzeit ──────────────────────────────────────────────────────────────────

/**
 * In-Game Uhrzeit.
 * 1 reale Sekunde = [TIME_SCALE] Spielminuten.
 * Ein kompletter Tag = 24 * 60 = 1440 Spielminuten.
 */
class WorldClock {

    /** Vergangene Spielminuten seit Spielstart */
    var totalMinutes: Float = 480f // Start um 08:00
        private set

    private var lastHour: Int = 8

    val hour: Int get() = (totalMinutes / 60).toInt() % 24
    val minute: Int get() = (totalMinutes % 60).toInt()
    val dayNumber: Int get() = (totalMinutes / 1440).toInt() + 1

    val season: Season get() = Season.fromDay(dayNumber)

    val isDay: Boolean get() = hour in 6..20
    val isNight: Boolean get() = !isDay

    fun advance(deltaSeconds: Float) {
        totalMinutes += deltaSeconds * TIME_SCALE
        val currentHour = hour
        if (currentHour != lastHour) {
            lastHour = currentHour
            EventBus.publish(DaytimeChangedEvent(currentHour))
        }
    }

    fun formattedTime(): String = "%02d:%02d".format(hour, minute)

    companion object {
        const val TIME_SCALE = 3f // 3 Spielminuten pro realer Sekunde
    }
}

// ── Jahreszeiten ─────────────────────────────────────────────────────────────

enum class Season(val displayName: String, val emoji: String) {
    SPRING("Frühling", "🌸"),
    SUMMER("Sommer", "☀️"),
    AUTUMN("Herbst", "🍂"),
    WINTER("Winter", "❄️");

    companion object {
        /** Ein Jahr = 28 Tage (7 Tage pro Jahreszeit) */
        fun fromDay(day: Int): Season {
            val dayInYear = ((day - 1) % 28)
            return when (dayInYear / 7) {
                0 -> SPRING
                1 -> SUMMER
                2 -> AUTUMN
                else -> WINTER
            }
        }
    }
}
