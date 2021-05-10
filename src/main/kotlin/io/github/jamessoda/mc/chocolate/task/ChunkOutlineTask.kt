package io.github.jamessoda.mc.chocolate.task

import io.github.jamessoda.mc.chocolate.Chocolate
import io.github.jamessoda.mc.chocolate.utils.EffectUtils
import io.github.jamessoda.mc.chocolate.utils.WorldUtils
import org.bukkit.Color
import org.bukkit.scheduler.BukkitRunnable

class ChunkOutlineTask(val plugin: Chocolate) : BukkitRunnable() {

    private val database = plugin.database
    private val guildManager = plugin.inviteManager

    private val radius = 1

    override fun run() {
        for (p in guildManager.outlineToggled) {
            val guildId = database.getGuildIdOfPlayer(p.uniqueId)

            val centerChunk = p.location.chunk

            val chunks = WorldUtils.getChunksAround(centerChunk, radius)

            for (chunk in chunks) {
                val chunkOwner = database.getChunkOwner(chunk) ?: continue

                for (loc in WorldUtils.getChunkOutline(chunk, p.location.y)) {
                    if(chunkOwner == guildId) {
                        EffectUtils.displayDustParticle(p, loc, 1, Color.YELLOW, 1f)
                    } else {
                        EffectUtils.displayDustParticle(p, loc, 1, Color.RED, 1f)
                    }
                }
            }
        }
    }
}