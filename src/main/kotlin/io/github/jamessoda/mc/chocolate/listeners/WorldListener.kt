package io.github.jamessoda.mc.chocolate.listeners

import io.github.jamessoda.mc.chocolate.Chocolate.Companion.plugin
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.ChunkLoadEvent

class WorldListener : Listener {

    @EventHandler
    fun onChunkLoad(event: ChunkLoadEvent) {
        plugin.database.loadChunk(event.chunk)
        plugin.database.loadGuild(event.chunk)
    }

}