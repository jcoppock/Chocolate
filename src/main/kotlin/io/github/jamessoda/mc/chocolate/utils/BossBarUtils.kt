package io.github.jamessoda.mc.chocolate.utils

import io.github.jamessoda.mc.chocolate.Chocolate.Companion.plugin
import io.github.jamessoda.mc.chocolate.utils.extensions.format
import org.bukkit.Bukkit
import org.bukkit.NamespacedKey
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.KeyedBossBar
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable

object BossBarUtils {

    private val bars = mutableListOf<KeyedBossBar>()

    private val startTimes = mutableMapOf<NamespacedKey, Long>()

    init {
        Bukkit.getScheduler().runTaskTimer(plugin, Runnable {

            val iterator = bars.iterator()

            while(iterator.hasNext()) {
                val bar = iterator.next()
                var remove = false

                for(p in bar.players) {
                    if(p.isOnline) {
                        remove = true
                    }
                }

                if(remove) {
                    if(startTimes.containsKey(bar.key)) {
                        startTimes.remove(bar.key)
                    }

                    bar.removeAll()
                    Bukkit.removeBossBar(bar.key)
                    iterator.remove()
                }
            }

        }, 2 * 20 * 60L, 2 * 20 * 60L)
    }

    private fun getBar(key: NamespacedKey): KeyedBossBar? {
        for (bar in bars) {
            if (bar.key == key) {
                return bar
            }
        }
        return null
    }

    private fun isBarForPlayer(bar: KeyedBossBar, player: Player): Boolean {
        return bar.key.key.contains(player.uniqueId.toString()) || bar.players.contains(player)
    }

    fun addBossBar(player: Player, key: NamespacedKey, title: String, color: BarColor,
                   style: BarStyle) : KeyedBossBar {

        val bar = Bukkit.createBossBar(key, title, color, style)

        bar.addPlayer(player)

        bars.add(bar)

        return bar
    }

    fun addCountDownBossBar(player: Player, key: NamespacedKey, title: String, time: Double) {

        val bar = addBossBar(player, key, "$title : ${time.format(1)}s", BarColor.RED, BarStyle.SOLID)

        startTimes[key] = System.currentTimeMillis()

        object : BukkitRunnable() {
            override fun run() {
                val timePassed = (System.currentTimeMillis() - (startTimes[key] ?: 0L)) / 1000.0
                if(timePassed >= time) {
                    cancel()
                    removeBossBar(bar.key)
                }

                val newColor = if(timePassed <= time * (1.0 / 3.0)) {
                    BarColor.RED
                } else if(timePassed <= time * (2.0 / 3.0)) {
                    BarColor.YELLOW
                } else {
                    BarColor.GREEN
                }

                val newTitle = "$title : ${(time - timePassed).format(1)}s"
                updateBossBar(bar.key, newTitle, 1.0 - (timePassed / time), newColor)
            }
        }.runTaskTimer(plugin, 1L, 1L)
    }

    fun addCountUpBossBar(player: Player, key: NamespacedKey, title: String, time: Double) {

        val bar = addBossBar(player, key, "$title : ${time.format(1)}", BarColor.RED, BarStyle.SOLID)

        startTimes[key] = System.currentTimeMillis()

        object : BukkitRunnable() {
            override fun run() {
                val timePassed = (System.currentTimeMillis() - (startTimes[key] ?: 0L)) / 1000.0
                if(timePassed >= time) {
                    cancel()
                    removeBossBar(bar.key)
                }

                val newColor = if(timePassed <= time * (1.0 / 3.0)) {
                    BarColor.RED
                } else if(timePassed <= time * (2.0 / 3.0)) {
                    BarColor.YELLOW
                } else {
                    BarColor.GREEN
                }

                val newTitle = "$title ${(time - timePassed).format(1)}s"
                updateBossBar(bar.key, newTitle, timePassed / time, newColor)
            }
        }.runTaskTimer(plugin, 1L, 1L)
    }

    fun updateBossBar(key: NamespacedKey, newTitle: String?, newProgress: Double?, newColor: BarColor?) {
        val bar = getBar(key) ?: return

        if (newTitle != null) {
            bar.setTitle(newTitle)
        }

        if (newProgress != null) {
            bar.progress = newProgress.coerceIn(0.0, 1.0)
        }

        if (newColor != null) {
            bar.color = newColor
        }
    }

    fun removeBossBars(player: Player) {
        val iterator = bars.iterator()

        while (iterator.hasNext()) {
            val bar = iterator.next()

            if (isBarForPlayer(bar, player)) {
                if(startTimes.containsKey(bar.key)) {
                    startTimes.remove(bar.key)
                }
                Bukkit.removeBossBar(bar.key)
                iterator.remove()
            }
        }
    }

    fun removeBossBar(key: NamespacedKey) {
        val bar = getBar(key) ?: return

        if(startTimes.containsKey(bar.key)) {
            startTimes.remove(bar.key)
        }

        bar.removeAll()
        Bukkit.removeBossBar(bar.key)

        bars.remove(bar)
    }
}