package villagecraft.core

/**
 * Einfacher, typsicherer Event-Bus.
 * Systeme können Events publishen und subscriben ohne direkte Abhängigkeiten.
 *
 * Beispiel:
 *   EventBus.subscribe<SkillLevelUpEvent> { e -> showLevelUpAnimation(e.skill) }
 *   EventBus.publish(SkillLevelUpEvent(Skill.WOODCUTTING, newLevel = 10))
 */
object EventBus {

    private val listeners = mutableMapOf<Class<*>, MutableList<(Any) -> Unit>>()

    @Suppress("UNCHECKED_CAST")
    fun <T : GameEvent> subscribe(eventClass: Class<T>, listener: (T) -> Unit) {
        listeners.getOrPut(eventClass) { mutableListOf() }
            .add(listener as (Any) -> Unit)
    }

    inline fun <reified T : GameEvent> subscribe(noinline listener: (T) -> Unit) {
        subscribe(T::class.java, listener)
    }

    fun <T : GameEvent> publish(event: T) {
        listeners[event::class.java]?.forEach { it(event) }
    }

    fun clear() = listeners.clear()
}

/** Marker-Interface für alle Spiel-Events */
interface GameEvent

// ── Konkrete Events ──────────────────────────────────────────────────────────

data class SkillXpGainedEvent(val skill: villagecraft.skills.SkillType, val xp: Int) : GameEvent
data class SkillLevelUpEvent(val skill: villagecraft.skills.SkillType, val newLevel: Int) : GameEvent

data class PlayerDiedEvent(val killedBy: String) : GameEvent
data class PlayerHealedEvent(val amount: Int) : GameEvent

data class QuestStartedEvent(val questId: String) : GameEvent
data class QuestCompletedEvent(val questId: String) : GameEvent
data class QuestObjectiveUpdatedEvent(val questId: String, val objectiveIndex: Int) : GameEvent

data class DialogueStartedEvent(val npcName: String) : GameEvent
data class DialogueEndedEvent(val npcName: String) : GameEvent

data class ItemPickedUpEvent(val itemId: String, val amount: Int) : GameEvent
data class ItemDroppedEvent(val itemId: String, val amount: Int) : GameEvent

data class DaytimeChangedEvent(val hour: Int) : GameEvent
data class SeasonChangedEvent(val season: villagecraft.world.Season) : GameEvent

data class TilePlacedEvent(val x: Int, val y: Int, val tileType: villagecraft.world.TileType) : GameEvent
