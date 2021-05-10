package io.github.jamessoda.mc.chocolate.magic

import io.github.jamessoda.mc.chocolate.Chocolate.Companion.plugin
import io.github.jamessoda.mc.chocolate.utils.BossBarUtils
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.NamespacedKey
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import java.util.*
import java.util.function.Consumer

abstract class ChannelSpell(itemId: Int, name: String, uses: Int,
                            stats: Map<String, Pair<Double, Double>>,
                            inSafeHaven: Boolean = false) : Spell(itemId, name, uses, stats, inSafeHaven) {

    companion object {
        private val users = mutableSetOf<UUID>()
        fun isChanneling(player: Player): Boolean {
            return users.contains(player.uniqueId)
        }

        fun clear(player: Player) {
            users.remove(player.uniqueId)
        }
    }

    override fun use(player: Player, cooldownCallback: Consumer<Boolean>) {

        if(users.contains(player.uniqueId)) {
            return
        }

        users.add(player.uniqueId)

        var tick = 0

        object : BukkitRunnable() {
            override fun run() {
                if (users.contains(player.uniqueId) && !player.isDead) {
                    tickEffect(player, tick)
                    tick++
                } else {
                    users.remove(player.uniqueId)
                    cancel()
                }
            }
        }.runTaskTimer(plugin, 1L, 1L)

        val channelTime = getStat("channel-time", player) ?: 1.0
        val slowness = (getStat("channel-slow", player) ?: 0.0).toInt()

        if(slowness != 0) {
            player.addPotionEffect(PotionEffect(PotionEffectType.SLOW, (channelTime * 20).toInt(),
                    slowness, true, false, false))
        }

        BossBarUtils.addCountUpBossBar(player,
                NamespacedKey(plugin, "${player.uniqueId}_${name}_channel"),
                "${ChatColor.BLUE}$prettyName", channelTime)

        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            if (users.contains(player.uniqueId)) {
                finishChannel(player, cooldownCallback)
                users.remove(player.uniqueId)
            }
        }, (20L * channelTime).toLong())

    }

    /**
     * Runs every half second while channeling
     */
    abstract fun tickEffect(player: Player, tick: Int)

    abstract fun finishChannel(player: Player, cooldownCallback: Consumer<Boolean>)
}