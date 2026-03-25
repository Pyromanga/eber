package villagecraft.core

import villagecraft.ui.screens.GameScreen
import villagecraft.world.GameWorld
import java.awt.Dimension
import javax.swing.JFrame
import javax.swing.WindowConstants

/**
 * Herzstück des Spiels.
 * Erzeugt das Fenster, hält die GameWorld und steuert den Game-Loop.
 */
class GameEngine {

    val world = GameWorld()
    private lateinit var frame: JFrame
    private lateinit var gameScreen: GameScreen
    private val gameLoop = GameLoop(targetFps = 60)

    fun start() {
        frame = JFrame("VillageCraft").apply {
            defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE
            isResizable = false
        }

        gameScreen = GameScreen(world)
        gameScreen.preferredSize = Dimension(WINDOW_WIDTH, WINDOW_HEIGHT)
        frame.add(gameScreen)

        frame.pack()
        frame.setLocationRelativeTo(null)
        frame.isVisible = true

        gameLoop.start { deltaSeconds ->
            world.update(deltaSeconds)
            gameScreen.repaint()
        }
    }

    fun stop() {
        gameLoop.stop()
        frame.dispose()
    }

    companion object {
        const val WINDOW_WIDTH = 1280
        const val WINDOW_HEIGHT = 800
        const val TILE_SIZE = 32
    }
}
