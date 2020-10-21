package io.github.jamessoda.mc.chocolate.listeners

import io.github.jamessoda.mc.chocolate.Chocolate
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerJoinEvent
import org.bukkit.event.player.PlayerQuitEvent
import java.util.*

class ConnectionListener(private val plugin: Chocolate) : Listener {

    @EventHandler
    fun onJoin(event: PlayerJoinEvent) {
        val player = event.player

        if(!plugin.database.playerHasUser(player)) {
            //TODO: First time join
        }

        val user = plugin.database.getUserFromPlayer(player)

        if(user.username != player.name) {
            user.username = player.name
        }

        val nameHistory = user.nameHistory?.toMutableList() ?: mutableListOf()
        if(!nameHistory.contains(player.name)) {
            nameHistory.add(player.name)
            user.nameHistory = nameHistory
        }

        val ipHistory = user.ipHistory?.toMutableList() ?: mutableListOf()
        if(!ipHistory.contains(player.address?.address.toString())) {
            ipHistory.add(player.address?.address.toString())
            user.ipHistory = ipHistory
        }

        user.lastSeenDate = Date(System.currentTimeMillis())
        plugin.database.saveUser(user)
    }

    @EventHandler
    fun onQuit(event: PlayerQuitEvent) {
        val player = event.player

        val user = plugin.database.getUserFromPlayer(player)
        user.lastSeenDate = Date(System.currentTimeMillis())
        plugin.database.saveUser(user)

    }
}