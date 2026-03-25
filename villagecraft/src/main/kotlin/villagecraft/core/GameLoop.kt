package villagecraft.core

import java.util.concurrent.Executors
import java.util.concurrent.ScheduledFuture
import java.util.concurrent.TimeUnit

/**
 * Fester Game-Loop mit konfigurierbarer FPS-Rate.
 * Ruft den [tick]-Callback mit deltaSeconds auf.
 *
 * Verwendet einen ScheduledExecutorService damit der Loop
 * unabhängig vom Swing EDT läuft und trotzdem präzise ist.
 */
class GameLoop(private val targetFps: Int = 60) {

    private val executor = Executors.newSingleThreadScheduledExecutor { r ->
        Thread(r, "game-loop").also { it.isDaemon = true }
    }
    private var future: ScheduledFuture<*>? = null
    private var lastNano = System.nanoTime()

    fun start(tick: (deltaSeconds: Float) -> Unit) {
        val intervalMs = 1000L / targetFps
        lastNano = System.nanoTime()

        future = executor.scheduleAtFixedRate({
            val now = System.nanoTime()
            val delta = ((now - lastNano) / 1_000_000_000.0).toFloat()
            lastNano = now
            tick(delta.coerceAtMost(0.1f)) // max 100ms damit Freeze-Spikes nicht alles zerstören
        }, 0, intervalMs, TimeUnit.MILLISECONDS)
    }

    fun stop() {
        future?.cancel(false)
        executor.shutdown()
    }
}
