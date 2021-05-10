package io.github.jamessoda.mc.chocolate.listeners

import io.github.jamessoda.mc.chocolate.Chocolate.Companion.plugin
import io.github.jamessoda.mc.chocolate.utils.extensions.isChocolatePlayer
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.AsyncPlayerChatEvent

class ChatListener : Listener {

    @EventHandler
    fun onChat(event: AsyncPlayerChatEvent) {
        val player = event.player

        if(!player.isChocolatePlayer()) {

            plugin.language.sendMessage(player, "misc.not_verified")
            event.isCancelled = true
            return
        }

        event.message = plugin.language.coinsReplace(event.message)
        val message = event.message
        val guildId = plugin.database.getGuildIdOfPlayer(player)

        if(message.startsWith("@")) {

            event.isCancelled = true


            if(guildId == null) {
                plugin.language.sendMessage(player, "guild.not_in_guild")
            } else {

                for(user in plugin.database.getUUIDsInGuild(guildId)) {
                    val p = Bukkit.getOfflinePlayer(user)

                    if(p.isOnline) {
                        p.player?.sendMessage("${ChatColor.AQUA}[${plugin.database.getGuildTag(guildId)}] " +
                                "${ChatColor.GREEN}${player.name} >> " +
                                message.substring(1, message.length))
                    }
                }

            }
        } else {

            if(guildId != null) {
                event.format = "${ChatColor.DARK_GRAY}[${ChatColor.GRAY}${plugin.database.getGuildTag(guildId)}${ChatColor.DARK_GRAY}]${ChatColor.RESET}${event.format}"
            }

            /*
            var finalMessage = event.format

            finalMessage = finalMessage.replace("%1\$s", player.displayName)
            finalMessage = finalMessage.replace("%2\$s", event.message)

            val component = TextComponent(finalMessage)

            var hover = if(guildId != null) {
                "${ChatColor.GRAY}Guild:   {guild_name} : [{guild_tag}]"
            } else {
                "${ChatColor.GRAY}Guild: Not in a guild."
            }


            hover = plugin.language.replacePlaceholders(player, hover)

            component.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text(hover))

            event.isCancelled = true

            for(recipient in event.recipients) {
                recipient.spigot().sendMessage(component)
            }

             */

        }
    }


}