package villagecraft

import villagecraft.core.GameEngine
import javax.swing.SwingUtilities

fun main() {
    SwingUtilities.invokeLater {
        GameEngine().start()
    }
}
