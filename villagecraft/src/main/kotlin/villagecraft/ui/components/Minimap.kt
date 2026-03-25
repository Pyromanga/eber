package villagecraft.ui.components

import villagecraft.world.GameWorld
import villagecraft.world.TileType
import java.awt.*
import java.awt.image.BufferedImage

/**
 * Minimap in der unteren rechten Ecke.
 * Rendert eine skalierte Version der TileMap und zeigt Spieler + NPC-Positionen.
 *
 * Die Minimap wird als BufferedImage gecacht und nur bei Änderungen neu generiert.
 */
class Minimap(private val world: GameWorld) {

    companion object {
        const val SIZE = 140          // Pixel Größe der Minimap
        const val PADDING = 10        // Abstand vom Bildschirmrand
        const val BORDER_RADIUS = 8
    }

    private var cachedMap: BufferedImage? = null
    private var lastPlayerX = -1
    private var lastPlayerY = -1

    fun render(g: Graphics2D, screenWidth: Int, screenHeight: Int) {
        val x = screenWidth - SIZE - PADDING
        val y = screenHeight - SIZE - PADDING - 30 // 30px über der Statusleiste

        // Hintergrund
        g.color = Color(0, 0, 0, 160)
        g.fillRoundRect(x - 4, y - 4, SIZE + 8, SIZE + 8, BORDER_RADIUS, BORDER_RADIUS)
        g.color = Color(80, 60, 160, 180)
        g.stroke = BasicStroke(1.5f)
        g.drawRoundRect(x - 4, y - 4, SIZE + 8, SIZE + 8, BORDER_RADIUS, BORDER_RADIUS)

        // Karte rendern
        val mapImage = getOrBuildMapImage()
        g.drawImage(mapImage, x, y, SIZE, SIZE, null)

        val map = world.tileMap
        val scaleX = SIZE.toFloat() / map.width
        val scaleY = SIZE.toFloat() / map.height

        // NPCs (kleine blaue Punkte)
        g.color = Color(100, 180, 255)
        world.npcManager.all().forEach { npc ->
            val nx = x + (npc.tileX * scaleX).toInt()
            val ny = y + (npc.tileY * scaleY).toInt()
            g.fillOval(nx - 2, ny - 2, 5, 5)
        }

        // Spieler (weißer Punkt mit Glühen)
        val px = x + (world.player.tileX * scaleX).toInt()
        val py = y + (world.player.tileY * scaleY).toInt()

        g.color = Color(255, 255, 255, 60)
        g.fillOval(px - 4, py - 4, 9, 9)
        g.color = Color.WHITE
        g.fillOval(px - 2, py - 2, 5, 5)

        // Kompass N
        g.font = Font("SansSerif", Font.BOLD, 9)
        g.color = Color(200, 200, 200)
        g.drawString("N", x + SIZE / 2 - 3, y + 10)
    }

    private fun getOrBuildMapImage(): BufferedImage {
        val cached = cachedMap
        if (cached != null) return cached

        val map = world.tileMap
        val img = BufferedImage(map.width, map.height, BufferedImage.TYPE_INT_RGB)

        for (ty in 0 until map.height) {
            for (tx in 0 until map.width) {
                val tile = map.get(tx, ty)
                val color = tile.toAwtColor()
                img.setRGB(tx, ty, color.rgb)
            }
        }

        cachedMap = img
        return img
    }

    /** Invalidiert den Cache (z.B. wenn Tiles sich ändern) */
    fun invalidate() {
        cachedMap = null
    }
}
