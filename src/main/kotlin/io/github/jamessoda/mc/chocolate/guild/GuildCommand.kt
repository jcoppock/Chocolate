package io.github.jamessoda.mc.chocolate.guild

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import com.mongodb.client.model.Filters.eq
import io.github.jamessoda.mc.chocolate.Chocolate
import io.github.jamessoda.mc.chocolate.db.entities.guild.GuildEntity
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player

@CommandAlias("guild")
class GuildCommand(private val plugin: Chocolate) : BaseCommand() {

    private val database = plugin.database
    private val guildManager = plugin.guildManager
    private val language = plugin.language

    @Default
    fun onDefault(player: Player) {

        val guild = database.getGuild(player)

        if(guild != null) {
            language.sendMessage(player, "guild.info", mapOf(Pair("{guild_name}", guild.name!!)))
        } else {
            language.sendMessage(player, "guild.no_info")
        }
    }

    @Subcommand("create")
    fun onCreate(player: Player, name: String, tag: String) {

        guildManager.createGuild(player, name, tag)

    }

    @Subcommand("invite")
    fun onInvite(player: Player, target: OnlinePlayer) {
        guildManager.invite(player, target.player)
    }

    @Subcommand("accept")
    fun onAccept(player: Player, name: String) {

        val user = database.getUserFromPlayer(player)

        if(user.guild != null) {
            player.sendMessage("You are already in a guild!")
            return
        }

        val invites = guildManager.invites[player] ?: listOf()

        var guild : GuildEntity? = null
        for(g in invites) {
            if(g.name!! == name) {
                guild = g
            }
        }

        if(guild != null) {
            if(guildManager.joinGuild(user, guild)) {
                language.sendMessage(player, "guild.invite_accepted", mapOf(Pair("{player}", player.name)))

                for(u in guildManager.getUsers(guild)) {
                    val offlineP = database.getOfflinePlayerFromUser(u)

                    if(offlineP.isOnline) {
                        val p = offlineP.player!!
                        language.sendMessage(p, "guild.invite_accepted", mapOf(Pair("{player}", player.name)))
                    }
                }

            } else {
                player.sendMessage("${ChatColor.RED}Could not join guild.")
            }
        }
    }
}