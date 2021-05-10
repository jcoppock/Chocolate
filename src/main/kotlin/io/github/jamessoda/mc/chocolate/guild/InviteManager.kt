package io.github.jamessoda.mc.chocolate.guild

import io.github.jamessoda.mc.chocolate.Chocolate.Companion.plugin
import org.bson.types.ObjectId
import org.bukkit.Bukkit
import org.bukkit.entity.Player

class InviteManager {

    val outlineToggled = mutableListOf<Player>()

    // A list of guild invites that a player has received
    val invites = mutableMapOf<Player, List<ObjectId>>()

    val database = plugin.database

    fun invite(sender: Player, receiver: Player) {

        val guildId = database.getGuildIdOfPlayer(sender)

        if(guildId == null) {
            plugin.language.sendMessage(sender, "guild.no_info")
            return
        }

        if (database.getGuildIdOfPlayer(receiver) != null) {
            plugin.language.sendMessage(sender, "guild.invite_already_in_guild", mapOf(Pair("{player}", receiver.name)))
            return
        }

        if (!database.isLeaderOrOwner(sender)) {
            plugin.language.sendMessage(sender, "guild.must_be_leader")
            return
        }

        val guildInvites = invites[receiver]?.toMutableList() ?: mutableListOf()

        if (guildInvites.contains(guildId)) {
            sender.sendMessage("That player has already been invited recently! Wait for them to accept or try again later.")
            return
        }

        guildInvites.add(guildId)
        invites[receiver] = guildInvites

        plugin.language.sendMessage(sender, "guild.invite_sent", mapOf(Pair("{player}", receiver.name)))
        plugin.language.sendMessage(receiver, "guild.invite_received", mapOf(Pair("{player}", sender.name),
                Pair("{guild_name}", database.getGuildName(guildId))))


        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, Runnable {
            val guildInvitesLater = invites[receiver]?.toMutableList() ?: mutableListOf()
            if (guildInvitesLater.contains(guildId)) {
                guildInvitesLater.remove(guildId)
            }
            invites[receiver] = guildInvitesLater
        }, 60 * 20L)

        return
    }

}