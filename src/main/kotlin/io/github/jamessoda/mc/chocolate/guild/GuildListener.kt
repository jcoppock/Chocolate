package io.github.jamessoda.mc.chocolate.guild

import io.github.jamessoda.mc.chocolate.Chocolate
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityExplodeEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent

class GuildListener(plugin: Chocolate) : Listener {

    private val language = plugin.language
    private val database = plugin.database


    @EventHandler
    fun onGuildEnter(event: PlayerMoveEvent) {

        val player = event.player

        val chunkFrom = event.from.chunk
        val chunkTo = event.to.chunk

        if(chunkFrom != chunkTo) {

            val fromOwner = database.getChunkOwner(chunkFrom)
            val toOwner = database.getChunkOwner(chunkTo)

            if(fromOwner != toOwner) {


                if(fromOwner != null) {
                    val fromName = database.getGuildName(fromOwner)
                    val fromTag = database.getGuildTag(fromOwner)
                    language.sendMessage(player, "guild.leave", mapOf(
                            Pair("{guild_name", fromName), Pair("{guild_tag}", fromTag)), true)
                }

                if(toOwner != null) {
                    val toName = database.getGuildName(toOwner)
                    val toTag = database.getGuildTag(toOwner)
                    language.sendMessage(player, "guild.enter", mapOf(
                            Pair("{guild_name", toName), Pair("{guild_tag}", toTag)), true)
                }
            }
        }
    }

    @EventHandler
    fun onGuildBlockBreak(event: BlockBreakEvent) {
        val block = event.block
        val chunk = block.location.chunk
        val chunkOwner = database.getChunkOwner(chunk) ?: return

        val player = event.player

        val guild = database.getGuildIdOfPlayer(player.uniqueId)

        if(guild == null || chunkOwner != guild) {
            event.isCancelled = true
            language.sendMessage(player, "guild.cancelled_action", mapOf(
                    Pair("{guild_name}", database.getGuildName(chunkOwner))
            ))
        }
    }

    @EventHandler
    fun onGuildBlockPlace(event: BlockPlaceEvent) {

        val block = event.block
        val chunk = block.location.chunk
        val chunkOwner = database.getChunkOwner(chunk) ?: return

        val player = event.player

        val guild = database.getGuildIdOfPlayer(player.uniqueId)

        if(guild == null || chunkOwner != guild) {
            event.isCancelled = true
            language.sendMessage(player, "guild.cancelled_action", mapOf(
                    Pair("{guild_name}", database.getGuildName(chunkOwner))
            ))
        }
    }

    @EventHandler
    fun onGuildBlockInteract(event: PlayerInteractEvent) {

        val block = event.clickedBlock ?: return
        val chunk = block.location.chunk
        val chunkOwner = database.getChunkOwner(chunk) ?: return

        val player = event.player

        val guild = database.getGuildIdOfPlayer(player.uniqueId)

        if(guild == null || chunkOwner != guild) {
            event.isCancelled = true
            language.sendMessage(player, "guild.cancelled_action", mapOf(
                    Pair("{guild_name}", database.getGuildName(chunkOwner))
            ))
        }
    }

    @EventHandler
    fun onEntityExplode(event: EntityExplodeEvent) {
        val entity = event.entity
        val uuid = entity.uniqueId

        val currentLocation = entity.location

        val currentLocationGuild = database.getChunkOwner(currentLocation.chunk)

        if(currentLocationGuild != null) {
            event.blockList().clear()
        }
    }
}