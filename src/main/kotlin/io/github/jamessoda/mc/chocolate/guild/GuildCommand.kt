package io.github.jamessoda.mc.chocolate.guild

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import io.github.jamessoda.mc.chocolate.Chocolate.Companion.plugin
import io.github.jamessoda.mc.chocolate.db.LocationInfo
import io.github.jamessoda.mc.chocolate.portals.Portal
import org.bson.types.ObjectId
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.World
import org.bukkit.block.Biome
import org.bukkit.entity.Player

@CommandAlias("guild")
class GuildCommand : BaseCommand() {

    private val database = plugin.database
    private val guildManager = plugin.inviteManager
    private val chocolateConfig = plugin.chocolateConfig
    private val language = plugin.language

    @Default
    fun onDefault(player: Player) {
        val guild = database.getGuildIdOfPlayer(player)


        if (guild != null) {
            Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
                val guildMoney = database.getGuildMoneySync(guild)
                language.sendMessage(player, "guild.info", mapOf(Pair("{coins}", "$guildMoney")))
            })
        } else {
            language.sendMessage(player, "guild.no_info")
        }
    }

    @Subcommand("members")
    fun onMembers(player: Player) {

        val guildId = database.getGuildIdOfPlayer(player)

        if (guildId != null) {

            language.sendMessage(player, "guild.member_header");

            for (uuid in database.getUUIDsInGuild(guildId)) {
                val p = Bukkit.getOfflinePlayer(uuid)

                val name = p.name ?: continue

                language.sendMessage(player, "guild.member_entry", mapOf(Pair("{player}", name)))
            }

        } else {
            language.sendMessage(player, "guild.no_info")
        }

    }

    @Subcommand("leaderboard|leader|list")
    fun onLeaderboard(player: Player) {

        language.sendMessage(player, "guild.list_header");

        var guildMoney = mutableMapOf<ObjectId, Int>()

        database.getGuildIdsAsync { guildIds ->

            for (guildId in guildIds) {
                guildMoney[guildId] = database.getGuildMoneySync(guildId)
            }

            guildMoney = guildMoney.toList()
                    .sortedByDescending { (id, _) -> database.getUUIDsInGuild(id).size }
                    .sortedByDescending { (_, money) -> money }
                    .sortedByDescending { (id, _) -> database.getChunks(id).size }.toMap().toMutableMap()

            var i = 1
            for (entry in guildMoney) {
                if (i > 10) {
                    break
                }
                val guildId = entry.key
                val users = database.getUUIDsInGuild(guildId)

                val placeholders = mutableMapOf<String, String>()

                placeholders["{position}"] = "$i"
                placeholders["{guild_name}"] = database.getGuildName(guildId)
                placeholders["{guild_tag}"] = database.getGuildTag(guildId)
                placeholders["{coins}"] = "${entry.value}"
                placeholders["{guild_users}"] = "${users.size}"
                placeholders["{guild_chunks}"] = "${database.getChunks(guildId).size}"
                Bukkit.getScheduler().runTask(plugin, Runnable {
                    language.sendMessage(player, "guild.list_entry", placeholders)
                })
                i++
            }

        }
    }

    @Subcommand("create")
    @Syntax("[tag] [name]")
    fun onCreate(player: Player, tag: String, nameArr: Array<String>) {

        var name = ""
        nameArr.forEach {
            name += "$it "
        }

        name = name.substring(0, name.length - 1)

        if (name.length < 3) {
            player.sendMessage("${ChatColor.RED}That name is too small!")
            return
        }

        if (name.length > 16) {
            player.sendMessage("${ChatColor.RED}That name is too large!")
            return
        }

        if (tag.length != 3 && tag.length != 4) {
            player.sendMessage("${ChatColor.RED}Guild Tag must be 3 or 4 characters!")
            return
        }

        database.getGuildIdsAsync() {
            for(guild in it) {
                val n = database.getGuildName(guild)
                val t = database.getGuildTag(guild)

                if (n == name || t == tag) {
                    player.sendMessage("${ChatColor.RED}A Guild with that name or tag already exists!")
                    return@getGuildIdsAsync
                }
            }
            database.createGuild(player, name, tag)

            Bukkit.broadcastMessage("${ChatColor.GRAY}${player.name} has created a new Guild!")
            Bukkit.broadcastMessage("${ChatColor.GRAY}    Name: $name")
            Bukkit.broadcastMessage("${ChatColor.GRAY}    Tag:  $tag")
            plugin.refreshGuildNamesCommandCompletions()

        }
    }

    @Subcommand("leave")
    fun onLeave(player: Player) {
        val guildId = database.getGuildIdOfPlayer(player)

        if (guildId != null) {

            if (database.getGuildOwner(guildId) == player.uniqueId && database.getUUIDsInGuild(guildId).size > 1) {
                player.sendMessage("${ChatColor.RED}You cannot leave the Guild since you are the owner!")
                player.sendMessage("${ChatColor.RED}Use ${ChatColor.GRAY}/guild transfer <Player> " +
                        "${ChatColor.RED}to make someone else the Guild's Owner")
                return
            }

            database.removePlayerFromGuild(player)

            if (database.getLeaders(guildId).contains(player.uniqueId)) {
                database.removeLeader(guildId, player.uniqueId)
            }

            language.sendMessage(player, "guild.left", mapOf(Pair("{player}", player.name)))

            Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                val uuids = database.getUUIDsInGuild(guildId)
                if (uuids.isNotEmpty()) {
                    for (u in uuids) {
                        val offlineP = Bukkit.getOfflinePlayer(u)

                        if (offlineP.isOnline) {
                            val p = offlineP.player!!
                            language.sendMessage(p, "guild.left_other", mapOf(Pair("{player}", player.name)))
                        }
                    }
                } else {
                    for (p in Bukkit.getOnlinePlayers()) {
                        language.sendMessage(p, "guild.disbanded", mapOf(Pair("{guild_name}",
                                database.getGuildName(guildId))))
                    }

                    plugin.portalManager.removeGuildPortal(guildId)
                    database.setGuildPortalLocation(guildId, null)


                    val chunks = database.getChunks(guildId)
                    for (chunk in chunks) {
                        database.unclaimChunk(guildId, chunk)
                    }
                }
            }, 5L)


        } else {
            language.sendMessage(player, "guild.no_info")
        }
    }

    @Subcommand("kick")
    @CommandCompletion("@players")
    fun onKick(player: Player, target: OnlinePlayer) {
        val guildId = database.getGuildIdOfPlayer(player)

        if (guildId == null) {
            language.sendMessage(player, "guild.no_info")
            return
        }

        if (player == target.player) {
            language.sendMessage(player, "guild.cannot_kick", mapOf(Pair("{player}", target.player.name)))
            return
        }

        if (target.player.uniqueId == database.getGuildOwner(guildId)) {
            language.sendMessage(player, "guild.cannot_kick", mapOf(Pair("{player}", target.player.name)))
            return
        }

        if (database.getGuildOwner(guildId) == player.uniqueId) {

            if (!database.getUUIDsInGuild(guildId).contains(target.getPlayer().uniqueId)) {
                player.sendMessage("${ChatColor.RED}That player is not in your guild!")
                return
            }

            language.sendMessage(target.getPlayer(), "guild.kick_self")
            database.removePlayerFromGuild(target.getPlayer())

            if (database.getLeaders(guildId).contains(target.player.uniqueId)) {
                database.removeLeader(guildId, target.player.uniqueId)
            }

            language.sendMessage(guildId, "guild.kick_other", mapOf(Pair("{player}", target.getPlayer().name)))

        } else {
            player.sendMessage("${ChatColor.RED}You can only kick members if your the Guild Owner!")
            return
        }
    }

    @Subcommand("invite")
    @CommandCompletion("@players")
    fun onInvite(player: Player, target: OnlinePlayer) {
        guildManager.invite(player, target.player)
    }

    @Subcommand("accept")
    @CommandCompletion("@guilds")
    fun onAccept(player: Player, name: String) {

        if (database.getGuildIdOfPlayer(player) != null) {
            player.sendMessage("${ChatColor.RED}You are already in a guild!")
            return
        }

        val invites = guildManager.invites[player] ?: listOf()

        var guild: ObjectId? = null
        for (g in invites) {
            if (database.getGuildName(g) == name || database.getGuildTag(g) == name) {
                guild = g
            }
        }

        if (guild != null) {
            database.addPlayerToGuild(player, guild)
            for (u in database.getUUIDsInGuild(guild)) {
                val offlineP = Bukkit.getOfflinePlayer(u)

                if (offlineP.isOnline) {
                    val p = offlineP.player!!
                    language.sendMessage(p, "guild.invite_accepted", mapOf(Pair("{player}", player.name)))
                }
            }
        }
    }

    @Subcommand("chunk")
    fun onChunk(player: Player) {

        val guildId = database.getGuildIdOfPlayer(player)

        val owner = database.getChunkOwner(player.location.chunk)
        if (owner != null) {
            language.sendMessage(player, "guild.chunk_info")
        } else {
            if (guildId != null) {
                val chunkCost = chocolateConfig.chunkInitialCost +
                        (database.getChunks(guildId).size * chocolateConfig.chunkCostIncrease)

                language.sendMessage(player, "guild.chunk_no_info_with_cost", mapOf(Pair("{coins}", "$chunkCost")))

            } else {
                language.sendMessage(player, "guild.chunk_no_info")
            }
        }
    }

    @Subcommand("chunk claim")
    fun onClaim(player: Player) {

        val guildId = database.getGuildIdOfPlayer(player)

        if (player.world.environment == World.Environment.THE_END) {
            if (player.location.block.biome == Biome.THE_END) {
                player.sendMessage("${ChatColor.RED}You cannot claim chunks on the main End Island!")
                return
            }
        }

        if (guildId == null) {
            language.sendMessage(player, "guild.no_info")
            return
        }

        if (database.getGuildOwner(guildId) != player.uniqueId) {
            language.sendMessage(player, "guild.must_be_leader")
            return
        }

        if (database.getChunkOwner(player.location.chunk) != null) {
            player.sendMessage("${ChatColor.RED}That chunk is already claimed!")
            return
        }

        val coinPouch = database.getCoinPouch(player.uniqueId)

        val chunkCost = chocolateConfig.chunkInitialCost +
                (database.getChunks(guildId).size * chocolateConfig.chunkCostIncrease)

        if (coinPouch < chunkCost) {
            player.sendMessage("${ChatColor.RED}You cannot afford to purchase a Chunk for your Guild!")
            player.sendMessage("${ChatColor.RED}The current Chunk price for your Guild is $chunkCost!")
            return
        }

        database.addCoins(player.uniqueId, -chunkCost)

        database.claimChunk(guildId, player.location.chunk)
        player.sendMessage("${ChatColor.GREEN}You have claimed this Chunk for your Guild!")
    }

    @Subcommand("chunk unclaim")
    fun onUnclaim(player: Player) {

        val guildId = database.getGuildIdOfPlayer(player)

        if (guildId == null) {
            language.sendMessage(player, "guild.no_info")
            return
        }

        if (database.getGuildOwner(guildId) != player.uniqueId) {
            language.sendMessage(player, "guild.must_be_leader")
            return
        }

        val chunk = player.location.chunk

        val chunkOwner = database.getChunkOwner(chunk)

        if (chunkOwner == null) {
            player.sendMessage("${ChatColor.RED}That chunk is not yet claimed!")
            return
        }

        if (chunkOwner != guildId) {
            player.sendMessage("${ChatColor.RED}Your guild does not own that Chunk!")
            return
        }

        val spawn = database.getGuildPortalLocation(guildId)
        if (spawn != null && spawn.chunk == chunk) {
            player.sendMessage("${ChatColor.RED}That chunk contains your Guild's Spawn!")
            player.sendMessage("${ChatColor.RED}Please move or delete your Guild's Spawn first!")
            return
        }

        database.unclaimChunk(guildId, chunk)
        player.sendMessage("${ChatColor.GREEN}You have unclaimed this Chunk!")
    }

    @Subcommand("spawn set")
    fun onSetSpawn(player: Player) {
        val guildId = database.getGuildIdOfPlayer(player)

        if (guildId == null) {
            language.sendMessage(player, "guild.no_info")
            return
        }

        if (!database.isLeaderOrOwner(player)) {
            language.sendMessage(player, "guild.must_be_leader")
            return
        }

        val spawn = database.getGuildPortalLocation(guildId)
        if (spawn != null) {
            plugin.portalManager.removeGuildPortal(guildId)
        }

        val chunk = player.location.chunk

        if (guildId != database.getChunkOwner(chunk)) {
            player.sendMessage("${ChatColor.RED}You can only do that in your territory")
            return
        }

        val newSpawnInfo = LocationInfo.createNew(player.location)
        database.setGuildPortalLocation(guildId, newSpawnInfo)

        player.sendMessage("${ChatColor.GREEN}Guild Spawn set!")

        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
            plugin.portalManager.addGuildPortal(guildId, Portal(player.location, database.getGuildName(guildId)))
        }, 5L)
    }

    @Subcommand("spawn delete")
    fun onDeleteSpawn(player: Player) {
        val guildId = database.getGuildIdOfPlayer(player)

        if (guildId == null) {
            language.sendMessage(player, "guild.no_info")
            return
        }

        if (!database.isLeaderOrOwner(player)) {
            language.sendMessage(player, "guild.must_be_leader")
            return
        }

        if (database.getGuildPortalLocation(guildId) == null) {
            player.sendMessage("${ChatColor.RED}Your guild does not have a spawn!")
            return
        }

        database.setGuildPortalLocation(guildId, null)
        player.sendMessage("${ChatColor.GREEN}Guild Spawn deleted!")

        plugin.portalManager.removeGuildPortal(guildId)
    }

    @Subcommand("chunk outlines")
    fun onOutlines(player: Player) {
        if (guildManager.outlineToggled.contains(player)) {
            guildManager.outlineToggled.remove(player)
        } else {
            guildManager.outlineToggled.add(player)
        }
    }

    @Subcommand("transfer")
    fun onTransfer(player: Player, target: OnlinePlayer) {
        val guildId = database.getGuildIdOfPlayer(player)

        if (guildId == null) {
            language.sendMessage(player, "guild.no_info")
            return
        }

        if (database.getGuildOwner(guildId) != player.uniqueId) {
            language.sendMessage(player, "guild.must_be_owner")
            return
        }

        if (database.getGuildIdOfPlayer(target.player) != guildId) {
            language.sendMessage(player, "guild.not_in_your_guild", mapOf(Pair("{player}", target.player.name)))
            return
        }

        database.setGuildOwner(guildId, target.player.uniqueId)
        language.sendMessage(guildId, "guild.ownership_transfered", mapOf(Pair("{player}", target.player.name)))

        if (database.getLeaders(guildId).contains(target.player.uniqueId)) {
            database.removeLeader(guildId, target.player.uniqueId)
        }

        database.addLeader(guildId, player.uniqueId)
        language.sendMessage(guildId, "guild.leader_add", mapOf(Pair("{player}", player.name)))
    }

    @Subcommand("leader add")
    fun onAddLeader(player: Player, target: OnlinePlayer) {
        val guildId = database.getGuildIdOfPlayer(player)

        if (guildId == null) {
            language.sendMessage(player, "guild.no_info")
            return
        }

        if (database.getGuildOwner(guildId) != player.uniqueId) {
            language.sendMessage(player, "guild.must_be_owner")
            return
        }

        if (database.getGuildIdOfPlayer(target.player) != guildId) {
            language.sendMessage(player, "guild.not_in_your_guild", mapOf(Pair("{player}", target.player.name)))
            return
        }

        if (database.getLeaders(guildId).contains(target.player.uniqueId)) {
            language.sendMessage(player, "guild.leader_already", mapOf(Pair("{player}", target.player.name)))
            return
        }

        database.addLeader(guildId, target.player.uniqueId)
        language.sendMessage(guildId, "guild.leader_add", mapOf(Pair("{player}", target.player.name)))
    }

    @Subcommand("leader remove")
    fun onRemoveLeader(player: Player, target: OnlinePlayer) {
        val guildId = database.getGuildIdOfPlayer(player)

        if (guildId == null) {
            language.sendMessage(player, "guild.no_info")
            return
        }

        if (database.getGuildOwner(guildId) != player.uniqueId) {
            language.sendMessage(player, "guild.must_be_owner")
            return
        }

        if (database.getGuildIdOfPlayer(target.player) != guildId) {
            language.sendMessage(player, "guild.not_in_your_guild", mapOf(Pair("{player}", target.player.name)))
            return
        }

        if (!database.getLeaders(guildId).contains(target.player.uniqueId)) {
            language.sendMessage(player, "guild.leader_already", mapOf(Pair("{player}", target.player.name)))
            return
        }

        database.removeLeader(guildId, target.player.uniqueId)
        language.sendMessage(guildId, "guild.leader_remove", mapOf(Pair("{player}", target.player.name)))
    }

}