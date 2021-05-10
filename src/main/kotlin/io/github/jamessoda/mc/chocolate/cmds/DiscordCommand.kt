package io.github.jamessoda.mc.chocolate.cmds

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Private
import co.aikar.commands.annotation.Subcommand
import io.github.jamessoda.mc.chocolate.Chocolate.Companion.plugin
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("discord")
class DiscordCommand : BaseCommand() {

    val database = plugin.database

    @Default
    fun onDefault(player: Player) {
        plugin.language.sendMessage(player, "misc.discord")
    }


    @Subcommand("link")
    fun onLink(player: Player) {
        val discordId = plugin.linkingPlayers[player]
        if(discordId != null) {

            if(database.getDiscordId(player.uniqueId) != null) {
                player.sendMessage("${ChatColor.RED}You are already linked with a Discord " +
                        "account! Please ask a Staff member for assistance if you are trying to unlink.")
                return
            }

            database.setDiscordId(player.uniqueId, discordId)
            player.sendMessage("${ChatColor.GREEN}You've successfully linked with Discord!")
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "lp user ${player.name}" +
                    " parent settrack player player")

        } else {
            player.sendMessage("${ChatColor.RED}You either waited too long or did not " +
                    "run the \"^link\" command on Discord first!")
        }
    }

}