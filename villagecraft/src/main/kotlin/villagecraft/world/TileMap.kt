package villagecraft.world

/**
 * Die 2D-Tile-Karte der Spielwelt.
 * Jede Zelle enthält einen [TileType].
 * Koordinaten: (0,0) = oben links.
 */
class TileMap(val width: Int, val height: Int) {

    private val tiles = Array(height) { Array(width) { TileType.GRASS } }

    fun get(x: Int, y: Int): TileType =
        if (inBounds(x, y)) tiles[y][x] else TileType.VOID

    fun set(x: Int, y: Int, type: TileType) {
        if (inBounds(x, y)) tiles[y][x] = type
    }

    fun inBounds(x: Int, y: Int) = x in 0 until width && y in 0 until height

    fun isWalkable(x: Int, y: Int) = get(x, y).walkable

    /** Generiert eine einfache Standardkarte mit Dorf, Fluss und Wald */
    fun generateDefault() {
        // Grundfläche: Gras
        fill(0, 0, width, height, TileType.GRASS)

        // Fluss (vertikal, links)
        fillColumn(8, TileType.WATER)
        fillColumn(9, TileType.WATER)

        // Wald-Rand (oben)
        for (x in 0 until width) {
            if (x !in 7..10) {
                set(x, 0, TileType.TREE)
                set(x, 1, TileType.TREE)
                set(x, 2, TileType.TREE)
            }
        }

        // Einige Bäume verstreut
        for (y in 5..30) for (x in 15..30) {
            if ((x + y) % 5 == 0) set(x, y, TileType.TREE)
        }

        // Dorfplatz (Pflasterstein)
        fill(20, 20, 10, 10, TileType.COBBLESTONE)

        // Wege
        for (x in 0 until width) set(x, 22, TileType.PATH)  // horizontaler Weg
        for (y in 0 until height) set(22, y, TileType.PATH)  // vertikaler Weg

        // Sand am Fluss
        fillColumn(7, TileType.SAND)
        fillColumn(10, TileType.SAND)

        // Erzadern in Felswand (unten)
        for (y in 55 until height) for (x in 0 until width) set(x, y, TileType.ROCK)
        set(18, 56, TileType.COPPER_ORE)
        set(22, 57, TileType.IRON_ORE)
        set(30, 58, TileType.COPPER_ORE)
        set(10, 59, TileType.IRON_ORE)
    }

    private fun fill(startX: Int, startY: Int, w: Int, h: Int, type: TileType) {
        for (y in startY until (startY + h)) for (x in startX until (startX + w)) set(x, y, type)
    }

    private fun fillColumn(x: Int, type: TileType) {
        for (y in 0 until height) set(x, y, type)
    }
}

/**
 * Alle Tile-Typen mit ihren Eigenschaften.
 */
enum class TileType(
    val displayName: String,
    val symbol: Char,          // ASCII-Fallback für Debug
    val walkable: Boolean,
    val colorHex: String,      // Hex-Farbe für Swing-Renderer
    val harvestable: Boolean = false,
    val harvestSkill: String? = null,
    val harvestItem: String? = null,
) {
    GRASS       ("Gras",       '.', true,  "#5a8a3c"),
    COBBLESTONE ("Pflaster",   '#', true,  "#888888"),
    PATH        ("Weg",        '/', true,  "#c8a96e"),
    SAND        ("Sand",       's', true,  "#e8d89a"),
    WATER       ("Wasser",     '~', false, "#3a7bd5"),
    VOID        ("Leere",      ' ', false, "#111111"),
    ROCK        ("Fels",       '^', false, "#666666"),
    TREE        ("Baum",       'T', false, "#2d6e2d",
        harvestable = true, harvestSkill = "WOODCUTTING", harvestItem = "logs"),
    COPPER_ORE  ("Kupfererz",  'C', false, "#b87333",
        harvestable = true, harvestSkill = "MINING", harvestItem = "copper_ore"),
    IRON_ORE    ("Eisenerz",   'I', false, "#8a8a8a",
        harvestable = true, harvestSkill = "MINING", harvestItem = "iron_ore"),
    ;

    /** Konvertiert den Hex-String in java.awt.Color */
    fun toAwtColor(): java.awt.Color {
        val hex = colorHex.removePrefix("#")
        return java.awt.Color(
            hex.substring(0, 2).toInt(16),
            hex.substring(2, 4).toInt(16),
            hex.substring(4, 6).toInt(16)
        )
    }
}
