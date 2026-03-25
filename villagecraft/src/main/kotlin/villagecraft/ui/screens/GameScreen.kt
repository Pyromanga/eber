package villagecraft.ui.screens

import villagecraft.core.GameEngine
import villagecraft.world.GameWorld
import villagecraft.world.TileType
import java.awt.*
import java.awt.event.*
import javax.swing.JPanel
import javax.swing.SwingUtilities

/**
 * Haupt-Rendering-Panel.
 * Zeichnet Karte, Spieler, NPCs und HUD.
 * Verarbeitet Maus- und Tastatureingaben.
 */
class GameScreen(private val world: GameWorld) : JPanel() {

    // Kamera-Offset (in Pixeln)
    private var camX = 0
    private var camY = 0

    // Aktiver Dialog-Zustand
    var activeDialogue: DialogueState? = null

    private val tileSize = GameEngine.TILE_SIZE
    private val colors = UiColors

    init {
        background = Color.BLACK
        isFocusable = true

        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) {
                handleClick(e)
            }
        })

        addKeyListener(object : KeyAdapter() {
            override fun keyPressed(e: KeyEvent) {
                handleKey(e)
            }
        })
    }

    override fun paintComponent(g: Graphics) {
        super.paintComponent(g)
        val g2 = g as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        // Kamera zentriert auf Spieler
        camX = world.player.tileX * tileSize - width / 2
        camY = world.player.tileY * tileSize - height / 2

        drawTiles(g2)
        drawNpcs(g2)
        drawPlayer(g2)
        drawHud(g2)

        if (activeDialogue != null) drawDialogue(g2)
    }

    // ── Rendering ─────────────────────────────────────────────────────────────

    private fun drawTiles(g: Graphics2D) {
        val startTX = (camX / tileSize).coerceAtLeast(0)
        val startTY = (camY / tileSize).coerceAtLeast(0)
        val endTX = ((camX + width) / tileSize + 1).coerceAtMost(world.tileMap.width)
        val endTY = ((camY + height) / tileSize + 1).coerceAtMost(world.tileMap.height)

        for (ty in startTY until endTY) {
            for (tx in startTX until endTX) {
                val tile = world.tileMap.get(tx, ty)
                val px = tx * tileSize - camX
                val py = ty * tileSize - camY

                g.color = tile.toAwtColor()
                g.fillRect(px, py, tileSize, tileSize)

                // Gitter
                g.color = Color(0, 0, 0, 40)
                g.drawRect(px, py, tileSize, tileSize)

                // Emoji-Symbol für harvestable Tiles
                if (tile.harvestable || tile == TileType.WATER) {
                    g.font = Font("Segoe UI Emoji", Font.PLAIN, 18)
                    g.color = Color.WHITE
                    val sym = when (tile) {
                        TileType.TREE       -> "🌲"
                        TileType.COPPER_ORE -> "🟤"
                        TileType.IRON_ORE   -> "⚫"
                        TileType.WATER      -> "💧"
                        else -> tile.symbol.toString()
                    }
                    drawEmoji(g, sym, px + 7, py + 22)
                }
            }
        }
    }

    private fun drawNpcs(g: Graphics2D) {
        world.npcManager.all().forEach { npc ->
            val px = npc.tileX * tileSize - camX
            val py = npc.tileY * tileSize - camY
            if (px < -tileSize || px > width || py < -tileSize || py > height) return@forEach

            // NPC-Körper
            g.color = Color(80, 60, 160)
            g.fillOval(px + 4, py + 8, tileSize - 8, tileSize - 8)
            g.color = Color(120, 100, 200)
            g.fillOval(px + 6, py + 4, tileSize - 12, 14)

            // NPC-Emoji
            g.font = Font("Segoe UI Emoji", Font.PLAIN, 16)
            drawEmoji(g, npc.emoji, px + 8, py + 20)

            // Name
            g.font = Font("SansSerif", Font.BOLD, 10)
            g.color = Color(255, 220, 100)
            val nameW = g.fontMetrics.stringWidth(npc.name)
            g.drawString(npc.name, px + (tileSize - nameW) / 2, py - 3)

            // Interaktions-Indikator
            g.color = Color(255, 220, 100, 180)
            g.font = Font("Segoe UI Emoji", Font.PLAIN, 12)
            drawEmoji(g, "💬", px + tileSize - 12, py)
        }
    }

    private fun drawPlayer(g: Graphics2D) {
        val px = world.player.tileX * tileSize - camX
        val py = world.player.tileY * tileSize - camY

        // Schatten
        g.color = Color(0, 0, 0, 80)
        g.fillOval(px + 6, py + tileSize - 8, tileSize - 12, 8)

        // Körper
        g.color = Color(50, 120, 200)
        g.fillOval(px + 4, py + 10, tileSize - 8, tileSize - 10)

        // Kopf
        g.color = Color(255, 200, 150)
        g.fillOval(px + 7, py + 2, tileSize - 14, 16)

        // Augen
        g.color = Color.BLACK
        g.fillOval(px + 10, py + 7, 3, 3)
        g.fillOval(px + 17, py + 7, 3, 3)
    }

    private fun drawHud(g: Graphics2D) {
        // Hintergrund oben
        g.color = Color(0, 0, 0, 180)
        g.fillRect(0, 0, width, 50)

        // HP Bar
        drawBar(g, 10, 10, 150, 16, world.player.currentHp, world.player.maxHp,
            Color(180, 40, 40), Color(80, 0, 0), "❤ ${world.player.currentHp}/${world.player.maxHp}")

        // Mana Bar
        drawBar(g, 10, 30, 150, 16, world.player.currentMana, world.player.maxMana,
            Color(40, 80, 180), Color(0, 20, 80), "✨ ${world.player.currentMana}/${world.player.maxMana}")

        // Uhrzeit
        g.font = Font("Monospaced", Font.BOLD, 13)
        g.color = Color(255, 220, 100)
        val timeStr = "${world.worldClock.formattedTime()} ${if (world.worldClock.isDay) "☀️" else "🌙"}"
        g.drawString(timeStr, width / 2 - 40, 20)

        // Jahreszeit
        g.font = Font("SansSerif", Font.PLAIN, 11)
        g.color = Color(180, 255, 180)
        val season = world.worldClock.season
        g.drawString("${season.emoji} ${season.displayName} - Tag ${world.worldClock.dayNumber}", width / 2 - 50, 38)

        // Skills (rechts oben) - Top 3 Skills
        g.color = Color(200, 200, 200)
        g.font = Font("SansSerif", Font.PLAIN, 11)
        val skills = listOf(
            villagecraft.skills.SkillType.WOODCUTTING,
            villagecraft.skills.SkillType.MINING,
            villagecraft.skills.SkillType.ATTACK,
        )
        skills.forEachIndexed { i, skill ->
            val level = world.player.skills.getLevel(skill)
            g.drawString("${skill.emoji} ${skill.displayName}: $level", width - 180, 15 + i * 14)
        }

        // Steuerung-Hint (unten)
        g.color = Color(140, 140, 140)
        g.font = Font("SansSerif", Font.PLAIN, 10)
        g.drawString("Klick: Bewegen/Interagieren  |  E: Inventar  |  Q: Quests  |  S: Skills", 10, height - 8)
    }

    private fun drawBar(g: Graphics2D, x: Int, y: Int, w: Int, h: Int,
                        current: Int, max: Int, fillColor: Color, bgColor: Color, label: String) {
        g.color = bgColor
        g.fillRoundRect(x, y, w, h, 6, 6)
        g.color = fillColor
        val filled = (w * current.toFloat() / max).toInt()
        if (filled > 0) g.fillRoundRect(x, y, filled, h, 6, 6)
        g.color = Color.WHITE
        g.font = Font("SansSerif", Font.BOLD, 10)
        g.drawString(label, x + 4, y + h - 3)
    }

    private fun drawDialogue(g: Graphics2D) {
        val dlg = activeDialogue ?: return
        val boxH = 180
        val boxY = height - boxH - 10
        val boxX = 10
        val boxW = width - 20

        // Box
        g.color = Color(20, 20, 40, 230)
        g.fillRoundRect(boxX, boxY, boxW, boxH, 16, 16)
        g.color = Color(100, 80, 200)
        g.stroke = BasicStroke(2f)
        g.drawRoundRect(boxX, boxY, boxW, boxH, 16, 16)

        // NPC-Name
        g.font = Font("SansSerif", Font.BOLD, 14)
        g.color = Color(255, 220, 100)
        g.drawString("${dlg.npcEmoji} ${dlg.npcName}", boxX + 16, boxY + 22)

        // Text
        g.font = Font("SansSerif", Font.PLAIN, 13)
        g.color = Color(220, 220, 220)
        drawWrappedText(g, dlg.text, boxX + 16, boxY + 44, boxW - 32, 16)

        // Optionen
        dlg.options.forEachIndexed { i, option ->
            val optY = boxY + 100 + i * 22
            val isHovered = i == dlg.hoveredOption
            g.color = if (isHovered) Color(255, 220, 100) else Color(180, 180, 180)
            g.font = Font("SansSerif", Font.PLAIN, 12)
            g.drawString("[${i + 1}] ${option.text}", boxX + 24, optY)
        }
    }

    private fun drawWrappedText(g: Graphics2D, text: String, x: Int, y: Int, maxWidth: Int, lineHeight: Int) {
        val words = text.split(" ")
        var line = ""
        var currentY = y
        val fm = g.fontMetrics

        words.forEach { word ->
            val testLine = if (line.isEmpty()) word else "$line $word"
            if (fm.stringWidth(testLine) > maxWidth) {
                g.drawString(line, x, currentY)
                line = word
                currentY += lineHeight
            } else {
                line = testLine
            }
        }
        if (line.isNotEmpty()) g.drawString(line, x, currentY)
    }

    private fun drawEmoji(g: Graphics2D, emoji: String, x: Int, y: Int) {
        try { g.drawString(emoji, x, y) } catch (_: Exception) {}
    }

    // ── Input ──────────────────────────────────────────────────────────────────

    private fun handleClick(e: MouseEvent) {
        // Dialog-Option geklickt?
        val dlg = activeDialogue
        if (dlg != null) {
            val boxH = 180
            val boxY = height - boxH - 10
            dlg.options.forEachIndexed { i, option ->
                val optY = boxY + 100 + i * 22
                if (e.y in (optY - 14)..(optY + 4)) {
                    option.onSelect()
                    return
                }
            }
            return
        }

        // Welt-Klick: Tile-Koordinate berechnen
        val tx = (e.x + camX) / tileSize
        val ty = (e.y + camY) / tileSize

        // NPC angeklickt?
        val npc = world.npcManager.getNpcAt(tx, ty)
        if (npc != null) {
            startDialogue(npc)
            return
        }

        // Bewegung
        if (world.tileMap.isWalkable(tx, ty)) {
            world.player.moveTo(tx, ty)
        }
    }

    private fun handleKey(e: KeyEvent) {
        when (e.keyCode) {
            KeyEvent.VK_ESCAPE -> activeDialogue = null
            KeyEvent.VK_1 -> activeDialogue?.options?.getOrNull(0)?.onSelect()
            KeyEvent.VK_2 -> activeDialogue?.options?.getOrNull(1)?.onSelect()
            KeyEvent.VK_3 -> activeDialogue?.options?.getOrNull(2)?.onSelect()
        }
    }

    private fun startDialogue(npc: villagecraft.entity.npc.Npc) {
        val tree = npc.getDialogue(world)
        val node = tree.startNode()

        activeDialogue = DialogueState(
            npcName = npc.name,
            npcEmoji = npc.emoji,
            text = node.text,
            options = node.options.filter { it.condition?.invoke(world) != false }
                .map { opt ->
                    DialogueOption(
                        text = opt.text,
                        onSelect = {
                            opt.onSelect?.invoke(world)
                            val nextId = opt.nextNodeId
                            if (nextId == null) {
                                activeDialogue = null
                            } else {
                                val nextNode = tree.getNode(nextId)
                                activeDialogue = activeDialogue?.copy(
                                    text = nextNode.text,
                                    options = nextNode.options
                                        .filter { it.condition?.invoke(world) != false }
                                        .map { o ->
                                            DialogueOption(o.text) {
                                                o.onSelect?.invoke(world)
                                                val nn = o.nextNodeId
                                                if (nn == null) activeDialogue = null
                                                else {
                                                    val nn2 = tree.getNode(nn)
                                                    activeDialogue = activeDialogue?.copy(
                                                        text = nn2.text,
                                                        options = emptyList()
                                                    )
                                                }
                                            }
                                        }
                                )
                            }
                        }
                    )
                }
        )
    }
}

// ── UI-Datentypen ─────────────────────────────────────────────────────────────

data class DialogueState(
    val npcName: String,
    val npcEmoji: String,
    val text: String,
    val options: List<DialogueOption>,
    val hoveredOption: Int = -1,
)

data class DialogueOption(
    val text: String,
    val onSelect: () -> Unit,
)

object UiColors {
    val BG = Color(15, 12, 25)
    val PANEL = Color(30, 25, 50, 220)
    val ACCENT = Color(120, 80, 220)
    val TEXT = Color(220, 215, 230)
    val GOLD = Color(255, 200, 60)
    val HP_RED = Color(180, 40, 40)
    val MANA_BLUE = Color(40, 80, 200)
}
