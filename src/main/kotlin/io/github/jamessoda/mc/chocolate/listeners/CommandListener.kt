package io.github.jamessoda.mc.chocolate.listeners

import io.github.jamessoda.mc.chocolate.Chocolate.Companion.plugin
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerCommandPreprocessEvent
import org.bukkit.event.player.PlayerCommandSendEvent

class CommandListener : Listener {

    private val allowedCommands = mutableListOf("/rules")

    private val chocolateCommands : List<String> by lazy {
        plugin.commandManager.registeredRootCommands.map { it.commandName }
    }

    @EventHandler
    fun onCommandsSendToClient(event: PlayerCommandSendEvent) {

        val iterator = event.commands.iterator()
        while(iterator.hasNext()) {
            val command = iterator.next()
            if(!chocolateCommands.contains(command) && !allowedCommands.contains(command)) {
                if(!event.player.hasPermission("chocolate.tab.bypass")) {
                    iterator.remove()
                }
            }
        }
    }

    @EventHandler
    fun onCommand(event: PlayerCommandPreprocessEvent) {

        var isChocolateCommand = false

        val iterator = chocolateCommands.iterator()
        while(iterator.hasNext()) {
            val command = iterator.next()
            if(event.message.startsWith("/$command")) {
                isChocolateCommand = true
            }
        }

        if(allowedCommands.contains(event.message.split(" ")[0])) {
            return
        }

        if(!isChocolateCommand && !event.player.hasPermission("chocolate.command.bypass")) {
            event.isCancelled = true
            plugin.language.sendMessage(event.player, "misc.no_command")
        }

    }
}