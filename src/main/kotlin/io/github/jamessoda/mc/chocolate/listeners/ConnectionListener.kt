package io.github.jamessoda.mc.chocolate.listeners

import io.github.jamessoda.mc.chocolate.Chocolate.Companion.plugin
import io.github.jamessoda.mc.chocolate.utils.BossBarUtils
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerPreLoginEvent
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import org.bukkit.inventory.ItemStack
import kotlin.random.Random

class ConnectionListener() : Listener {

    @EventHandler
    fun onPreLogin(event: AsyncPlayerPreLoginEvent) {
        val uuid = event.uniqueId

        if (event.loginResult != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            return
        }
        plugin.database.loadUser(uuid)

        //TODO: Check if banned.

        if (event.loginResult != AsyncPlayerPreLoginEvent.Result.ALLOWED) {
            return
        }
    }

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player

        plugin.database.isNewPlayerAsync(player.uniqueId) {
            if(!it) {
                return@isNewPlayerAsync
            }

            Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                player.teleport(plugin.chocolateConfig.spawn)
                for (p in Bukkit.getOnlinePlayers()) {
                    plugin.language.sendMessage(p, "misc.welcome", mapOf(Pair("{player}", player.name)))
                }
                plugin.language.sendMessage(player, "misc.welcome_player")

                // Update Ender Chest
                val starterItems = mutableListOf<ItemStack>()

                val teleportScroll = plugin.itemsConfig.getItem(201)
                if (teleportScroll != null) {
                    starterItems.add(teleportScroll)
                }

                val chest = player.enderChest
                chest.clear()
                if (starterItems.size < chest.size) {
                    val usedSlots = mutableListOf<Int>()
                    for (item in starterItems) {
                        var slot: Int
                        do {
                            slot = Random.nextInt(0, chest.size)
                        } while (usedSlots.contains(slot))

                        usedSlots.add(slot)
                        chest.setItem(slot, item)
                    }
                }
            }, 5L)
        }

        val guildId = plugin.database.getGuildIdOfPlayer(player.uniqueId)

        if(guildId != null) {
            plugin.database.loadGuild(guildId)
        }

        plugin.database.refreshUserInfo(player)

        // Update any custom items
        plugin.itemsConfig.refreshItems(player)

        plugin.database.hasUncollectedBuyOrdersAsync(player.uniqueId) {
            if(it) {
                Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                    player.sendMessage("${ChatColor.GREEN}Someone has filled one of your ${ChatColor.GOLD}Buy Orders!")
                    player.sendMessage("${ChatColor.GREEN}Go see a Clerk to collect your items!")
                }, 20L)
            }
        }

        plugin.database.hasUncollectedSellOrdersAsync(player.uniqueId) {
            if(it) {
                Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                    player.sendMessage("${ChatColor.GREEN}Someone has purchased one of your ${ChatColor.GOLD}Sell Orders!")
                    player.sendMessage("${ChatColor.GREEN}Go see a Clerk to collect your items!")
                }, 20L)
            }
        }
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        val player = event.player

        BossBarUtils.removeBossBars(player)

        plugin.database.refreshLastSeen(player)
        plugin.database.forceSaveUser(player.uniqueId)

    }
}