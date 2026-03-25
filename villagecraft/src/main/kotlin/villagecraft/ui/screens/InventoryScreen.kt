package villagecraft.ui.screens

import villagecraft.entity.player.EquipSlot
import villagecraft.entity.player.Item
import villagecraft.entity.player.ItemRegistry
import villagecraft.entity.player.Player
import java.awt.*
import java.awt.event.MouseAdapter
import java.awt.event.MouseEvent
import javax.swing.JPanel

/**
 * Inventar-Overlay (28 Slots, Equipment-Slots, Item-Infos).
 * Wird als JPanel über dem GameScreen eingeblendet (CardLayout oder Overlay).
 *
 * Features:
 * - 28 Slot-Grid (4×7 wie OSRS)
 * - Equipment-Slots: Kopf, Körper, Beine, Waffe, Schild, Ring, Amulett
 * - Hover: Item-Tooltip mit Name, Beschreibung, Wert
 * - Klick auf ausrüstbares Item → equip/unequip
 * - Klick auf Essen → heilt
 */
class InventoryScreen(private val player: Player) : JPanel() {

    companion object {
        const val SLOT_SIZE = 50
        const val SLOT_GAP = 6
        const val COLS = 4
        const val ROWS = 7
        val PANEL_WIDTH = COLS * (SLOT_SIZE + SLOT_GAP) + SLOT_GAP + 160 + 20
        val PANEL_HEIGHT = ROWS * (SLOT_SIZE + SLOT_GAP) + SLOT_GAP + 60
    }

    private var hoveredSlot: Int = -1
    private var statusMessage: String = ""
    private var statusTimer: Float = 0f

    init {
        preferredSize = Dimension(PANEL_WIDTH, PANEL_HEIGHT)
        background = Color(0, 0, 0, 0)
        isOpaque = false

        addMouseListener(object : MouseAdapter() {
            override fun mouseClicked(e: MouseEvent) = handleClick(e)
        })
        addMouseMotionListener(object : MouseAdapter() {
            override fun mouseMoved(e: MouseEvent) {
                hoveredSlot = slotAt(e.x, e.y)
                repaint()
            }
        })
    }

    override fun paintComponent(g: Graphics) {
        val g2 = g as Graphics2D
        g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON)

        val panelX = 10
        val panelY = 10

        // Panel-Hintergrund
        g2.color = Color(20, 15, 40, 230)
        g2.fillRoundRect(panelX, panelY, PANEL_WIDTH - 20, PANEL_HEIGHT - 20, 16, 16)
        g2.color = Color(100, 80, 200)
        g2.stroke = BasicStroke(1.5f)
        g2.drawRoundRect(panelX, panelY, PANEL_WIDTH - 20, PANEL_HEIGHT - 20, 16, 16)

        // Titel
        g2.font = Font("SansSerif", Font.BOLD, 15)
        g2.color = Color(255, 220, 100)
        g2.drawString("🎒 Inventar  (${player.inventory.size}/${player.inventory.maxSlots})", panelX + 14, panelY + 26)

        // Gold
        g2.font = Font("SansSerif", Font.PLAIN, 12)
        g2.color = Color(255, 220, 100)
        g2.drawString("🪙 ${player.inventory.countGold()} Münzen", panelX + 14, panelY + 44)

        // Inventar-Slots (4×7)
        val slotStartX = panelX + SLOT_GAP
        val slotStartY = panelY + 54
        val slots = player.inventory.getSlots()

        for (row in 0 until ROWS) {
            for (col in 0 until COLS) {
                val idx = row * COLS + col
                val sx = slotStartX + col * (SLOT_SIZE + SLOT_GAP)
                val sy = slotStartY + row * (SLOT_SIZE + SLOT_GAP)
                val isHovered = idx == hoveredSlot
                val stack = slots.getOrNull(idx)

                // Slot-Hintergrund
                g2.color = if (isHovered) Color(60, 50, 100) else Color(35, 30, 60)
                g2.fillRoundRect(sx, sy, SLOT_SIZE, SLOT_SIZE, 8, 8)
                g2.color = if (isHovered) Color(120, 100, 200) else Color(70, 60, 110)
                g2.stroke = BasicStroke(1f)
                g2.drawRoundRect(sx, sy, SLOT_SIZE, SLOT_SIZE, 8, 8)

                if (stack != null) {
                    // Item-Emoji
                    g2.font = Font("Segoe UI Emoji", Font.PLAIN, 22)
                    g2.color = Color.WHITE
                    try { g2.drawString(stack.item.emoji, sx + 10, sy + 32) } catch (_: Exception) {}

                    // Anzahl
                    if (stack.item.stackable && stack.amount > 1) {
                        g2.font = Font("SansSerif", Font.BOLD, 10)
                        val amtStr = if (stack.amount >= 1000) "${stack.amount / 1000}K" else stack.amount.toString()
                        g2.color = Color(255, 220, 50)
                        g2.drawString(amtStr, sx + 3, sy + SLOT_SIZE - 3)
                    }
                }
            }
        }

        // Equipment-Panel rechts
        drawEquipmentPanel(g2, panelX + COLS * (SLOT_SIZE + SLOT_GAP) + SLOT_GAP + 10, panelY + 54)

        // Tooltip für gehovertes Item
        val hovered = if (hoveredSlot >= 0) slots.getOrNull(hoveredSlot) else null
        if (hovered != null) {
            drawTooltip(g2, hovered.item, hovered.amount)
        }

        // Statusmeldung
        if (statusMessage.isNotEmpty()) {
            g2.font = Font("SansSerif", Font.PLAIN, 12)
            g2.color = Color(150, 255, 150)
            g2.drawString(statusMessage, panelX + 14, panelY + PANEL_HEIGHT - 30)
        }
    }

    private fun drawEquipmentPanel(g: Graphics2D, x: Int, y: Int) {
        g.font = Font("SansSerif", Font.BOLD, 12)
        g.color = Color(180, 160, 220)
        g.drawString("Ausrüstung", x, y - 6)

        val slots = listOf(
            EquipSlot.HEAD to Pair(x + 55, y),
            EquipSlot.AMULET to Pair(x + 100, y),
            EquipSlot.WEAPON to Pair(x, y + 56),
            EquipSlot.BODY to Pair(x + 55, y + 56),
            EquipSlot.SHIELD to Pair(x + 110, y + 56),
            EquipSlot.RING to Pair(x, y + 112),
            EquipSlot.LEGS to Pair(x + 55, y + 112),
            EquipSlot.FEET to Pair(x + 55, y + 168),
        )

        slots.forEach { (slot, pos) ->
            val (sx, sy) = pos
            val equipped = player.equipment.get(slot)
            val slotW = 42
            val slotH = 42

            g.color = Color(35, 30, 60)
            g.fillRoundRect(sx, sy, slotW, slotH, 8, 8)
            g.color = Color(70, 60, 110)
            g.stroke = BasicStroke(1f)
            g.drawRoundRect(sx, sy, slotW, slotH, 8, 8)

            if (equipped != null) {
                g.font = Font("Segoe UI Emoji", Font.PLAIN, 18)
                try { g.drawString(equipped.emoji, sx + 11, sy + 28) } catch (_: Exception) {}
            } else {
                // Slot-Label
                g.font = Font("SansSerif", Font.PLAIN, 9)
                g.color = Color(100, 90, 140)
                g.drawString(slot.name.take(3), sx + 4, sy + 24)
            }
        }
    }

    private fun drawTooltip(g: Graphics2D, item: Item, amount: Int) {
        val lines = mutableListOf(
            "${item.emoji} ${item.name}" to Font("SansSerif", Font.BOLD, 13),
            item.description to Font("SansSerif", Font.ITALIC, 11),
            "Wert: ${item.value} 🪙  ×$amount" to Font("SansSerif", Font.PLAIN, 11),
        )
        item.combatStats?.let { cs ->
            if (cs.attackBonus != 0) lines.add("+${cs.attackBonus} Angriff" to Font("SansSerif", Font.PLAIN, 11))
            if (cs.defenceBonus != 0) lines.add("+${cs.defenceBonus} Verteidigung" to Font("SansSerif", Font.PLAIN, 11))
        }

        val tw = 200
        val th = lines.size * 18 + 12
        val tx = width - tw - 20
        val ty = 20

        g.color = Color(10, 8, 20, 230)
        g.fillRoundRect(tx, ty, tw, th, 10, 10)
        g.color = Color(120, 100, 200)
        g.stroke = BasicStroke(1f)
        g.drawRoundRect(tx, ty, tw, th, 10, 10)

        lines.forEachIndexed { i, (text, font) ->
            g.font = font
            g.color = if (i == 0) Color(255, 220, 100) else Color(200, 195, 220)
            g.drawString(text, tx + 8, ty + 18 + i * 18)
        }
    }

    private fun slotAt(mx: Int, my: Int): Int {
        val panelX = 10
        val slotStartX = panelX + SLOT_GAP
        val slotStartY = panelX + 54 + 10
        for (row in 0 until ROWS) for (col in 0 until COLS) {
            val sx = slotStartX + col * (SLOT_SIZE + SLOT_GAP)
            val sy = slotStartY + row * (SLOT_SIZE + SLOT_GAP)
            if (mx in sx..(sx + SLOT_SIZE) && my in sy..(sy + SLOT_SIZE)) return row * COLS + col
        }
        return -1
    }

    private fun handleClick(e: MouseEvent) {
        val idx = slotAt(e.x, e.y)
        if (idx < 0) return
        val stack = player.inventory.getSlots().getOrNull(idx) ?: return
        val item = stack.item

        when {
            item.equipSlot != null -> {
                val old = player.equipment.equip(item)
                player.inventory.remove(item.id)
                old?.let { player.inventory.add(it.id) }
                showStatus("${item.emoji} ${item.name} ausgerüstet!")
            }
            item.id == "fish_cooked" -> {
                player.heal(6)
                player.inventory.remove(item.id)
                showStatus("🍖 Mmmh! +6 HP")
            }
            item.id == "apple" -> {
                player.heal(2)
                player.inventory.remove(item.id)
                showStatus("🍎 Knackig! +2 HP")
            }
        }
        repaint()
    }

    private fun showStatus(msg: String) {
        statusMessage = msg
        statusTimer = 3f
    }

    fun update(deltaSeconds: Float) {
        if (statusTimer > 0) {
            statusTimer -= deltaSeconds
            if (statusTimer <= 0) statusMessage = ""
        }
    }
}
