package io.github.jamessoda.mc.chocolate.guild

import io.github.jamessoda.mc.chocolate.Chocolate
import io.github.jamessoda.mc.chocolate.db.entities.UserEntity
import io.github.jamessoda.mc.chocolate.db.entities.guild.GuildEntity
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

class GuildManager(private val plugin: Chocolate) {

    private val database = plugin.database
    private val language = plugin.language

    val invites = mutableMapOf<Player, List<GuildEntity>>()

    fun createGuild(owner: Player, name: String, tag: String) {
        val user = database.getUserFromPlayer(owner)
        if(database.getGuild(user) != null) {
            owner.sendMessage("You are already in a guild")
            return
        }

        if(name.length < 3) {
            owner.sendMessage("Name is too small")
            return
        }

        if(tag.length != 3 && tag.length != 4) {
            owner.sendMessage("Tag is wrong size")
            return
        }

        val guild = GuildEntity()
        guild.name = name
        guild.tag = tag
        guild.owner = user.id
        val users = guild.users ?: listOf(user.id!!)
        guild.users = users
        guild.createdDate = Date(System.currentTimeMillis())
        database.saveGuild(guild)

        user.guild = guild.id
        database.saveUser(user)

        language.sendMessage(owner, "guild.create", mapOf(Pair("{guild_name}", guild.name!!)))

    }

    fun invite(sender: Player, receiver: Player) : Boolean {

        val senderUser = database.getUserFromPlayer(sender)

        val senderGuild = database.getGuild(senderUser) ?: return false

        if(database.getGuild(database.getUserFromPlayer(receiver)) != null) {
            sender.sendMessage("That player is already in a guild!")
            return false
        }

        val guildInvites = invites[receiver]?.toMutableList() ?: mutableListOf()

        if(guildInvites.contains(senderGuild)) {
            sender.sendMessage("That player has already been invited recently! Wait for them to accept or try again later.")
            return false
        }

        guildInvites.add(senderGuild)
        invites[receiver] = guildInvites

        language.sendMessage(sender, "guild.invite_sent", mapOf(Pair("{player}", receiver.name)))
        language.sendMessage(receiver, "guild.invite_received",
                mapOf(Pair("{player}", sender.name), Pair("{guild_name}", senderGuild.name!!)))


        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, Runnable {
            val guildInvitesLater = invites[receiver]?.toMutableList() ?: mutableListOf()
            if(guildInvitesLater.contains(senderGuild)) {
                guildInvitesLater.remove(senderGuild)



            }
            invites[receiver] = guildInvitesLater
        }, 60 * 20L)

        return true
    }

    fun joinGuild(user: UserEntity, guild: GuildEntity) : Boolean {

        if(database.getGuild(user) == null) {
            user.guild = guild.id
            addUser(guild, user)
            database.saveUser(user)
            return true
        }

        return false
    }

    fun addUser(guild: GuildEntity, user: UserEntity) {
        val users = guild.users?.toMutableList() ?: mutableListOf()

        if(!users.contains(user.id)) {
            users.add(user.id!!)
        }
    }

    fun removeUser(guild: GuildEntity, user: UserEntity) {
        if(guild.users != null && guild.users!!.contains(user.id)) {
            val newUsers = guild.users!!.toMutableList()
            newUsers.remove(user.id)
            guild.users = newUsers
        }
    }

    fun getUsers(guild: GuildEntity) : List<UserEntity> {

        val users = mutableListOf<UserEntity>()

        for(u in guild.users ?: listOf()) {
            users.add(database.getUserFromId(u))
        }

        return users
    }

}