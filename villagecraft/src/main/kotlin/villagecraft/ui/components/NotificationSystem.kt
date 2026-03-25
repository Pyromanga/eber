package villagecraft.ui.components

import villagecraft.core.*
import villagecraft.skills.SkillType
import java.awt.*

/**
 * Benachrichtigungssystem: Level-Up-Banner, Quest-Started, Loot-Popups.
 * Animiert ein- und ausblendendes Overlay.
 *
 * Wird vom GameScreen aufgerufen und über dem HUD gerendert.
 */
class NotificationSystem {

    private val queue = ArrayDeque<Notification>()
    private var current: Notification? = null
    private var timer: Float = 0f

    init {
        EventBus.subscribe<SkillLevelUpEvent> { e ->
            push(Notification(
                type = NotificationType.LEVEL_UP,
                title = "LEVEL UP!",
                message = "${e.skill.emoji} ${e.skill.displayName} ist jetzt Level ${e.newLevel}!",
                color = Color(255, 200, 50),
                duration = 3.5f,
            ))
        }
        EventBus.subscribe<QuestStartedEvent> { e ->
            push(Notification(
                type = NotificationType.QUEST,
                title = "Quest gestartet",
                message = "📜 Neue Quest erhalten!",
                color = Color(100, 200, 120),
                duration = 2.5f,
            ))
        }
        EventBus.subscribe<QuestCompletedEvent> { e ->
            push(Notification(
                type = NotificationType.QUEST_DONE,
                title = "QUEST ABGESCHLOSSEN!",
                message = "🎉 Belohnung erhalten!",
                color = Color(255, 180, 50),
                duration = 4.0f,
            ))
        }
        EventBus.subscribe<SkillXpGainedEvent> { e ->
            // Kleine XP-Popups nur wenn signifikante Menge
            if (e.xp >= 50) {
                push(Notification(
                    type = NotificationType.XP,
                    title = "+${e.xp} XP",
                    message = "${e.skill.emoji} ${e.skill.displayName}",
                    color = Color(180, 220, 255),
                    duration = 1.5f,
                ))
            }
        }
    }

    fun update(deltaSeconds: Float) {
        val n = current
        if (n != null) {
            timer += deltaSeconds
            n.progress = (timer / n.duration).coerceIn(0f, 1f)
            if (timer >= n.duration) {
                current = null
                timer = 0f
            }
        } else if (queue.isNotEmpty()) {
            current = queue.removeFirst()
            timer = 0f
        }
    }

    fun render(g: Graphics2D, screenWidth: Int, screenHeight: Int) {
        val n = current ?: return

        val progress = n.progress
        // Einblenden (0..0.2) → stabil (0.2..0.8) → Ausblenden (0.8..1.0)
        val alpha = when {
            progress < 0.15f -> (progress / 0.15f)
            progress > 0.8f  -> 1f - ((progress - 0.8f) / 0.2f)
            else             -> 1f
        }

        val w = 380
        val h = 70
        val x = (screenWidth - w) / 2
        val y = 60  // unter dem HUD

        val composite = g.composite
        g.composite = AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha)

        // Hintergrund
        g.color = Color(20, 15, 40, 220)
        g.fillRoundRect(x, y, w, h, 12, 12)

        // Akzent-Linie links
        g.color = n.color
        g.fillRoundRect(x, y, 4, h, 2, 2)

        // Titel
        g.font = Font("SansSerif", Font.BOLD, 16)
        g.color = n.color
        g.drawString(n.title, x + 14, y + 24)

        // Message
        g.font = Font("SansSerif", Font.PLAIN, 13)
        g.color = Color(210, 210, 210)
        g.drawString(n.message, x + 14, y + 48)

        // Fortschrittsbalken unten
        g.color = Color(255, 255, 255, 40)
        g.fillRoundRect(x, y + h - 4, w, 4, 2, 2)
        g.color = n.color.darker()
        val barW = ((1f - progress) * w).toInt()
        if (barW > 0) g.fillRoundRect(x, y + h - 4, barW, 4, 2, 2)

        g.composite = composite
    }

    private fun push(n: Notification) {
        // Max 3 in der Queue
        if (queue.size < 3) queue.addLast(n)
    }
}

data class Notification(
    val type: NotificationType,
    val title: String,
    val message: String,
    val color: Color,
    val duration: Float,
    var progress: Float = 0f,
)

enum class NotificationType {
    LEVEL_UP, QUEST, QUEST_DONE, XP, INFO, WARNING
}
