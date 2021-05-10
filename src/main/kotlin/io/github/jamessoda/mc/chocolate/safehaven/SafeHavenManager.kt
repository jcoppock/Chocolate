package io.github.jamessoda.mc.chocolate.safehaven

import io.github.jamessoda.mc.chocolate.Chocolate
import io.github.jamessoda.mc.chocolate.utils.ConfigUtils
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.entity.Entity
import org.bukkit.entity.Player

class SafeHavenManager(private val plugin: Chocolate) {

    private val chunks = mutableMapOf<String, Set<Chunk>>()

    init {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, Runnable {
            saveChunks()
        }, 0L, 2 * 20 * 60L);
    }

    fun addSafeHaven(name: String, chunks: Set<Chunk>) {
        this.chunks[name] = chunks
    }

    fun addChunk(name: String, chunk: Chunk) {
        val chunks = this.chunks[name]?.toMutableSet() ?: mutableSetOf()
        chunks.add(chunk)
        this.chunks[name] = chunks
    }

    fun removeChunk(chunk: Chunk) {
        for(entry in chunks.entries) {
            if(entry.value.contains(chunk)) {
                val newChunks = entry.value.toMutableSet()
                newChunks.remove(chunk)
                chunks[entry.key] = newChunks
            }
        }
    }

    fun getChunks(name: String) : Set<Chunk> {
        return chunks[name] ?: mutableSetOf()
    }

    fun getSafeHaven(chunk : Chunk) : String? {

        for(entry in this.chunks.entries) {
            val name = entry.key
            val cs = entry.value
            for(c in cs) {
                if (chunk.x == c.x && chunk.z == c.z && chunk.world == c.world) {
                    return name
                }
            }
        }

        return null
    }

    fun saveChunks() {
        val config = plugin.chocolateConfig

        val safeHavenSection = config.yaml.getConfigurationSection("safe-havens")

        if(safeHavenSection != null) {
            chunks.forEach { (name, chunks) ->
                val chunkStringList = mutableListOf<String>()

                chunks.forEach {
                    val serialized = ConfigUtils.serializeChunk(it)
                    chunkStringList.add(serialized)
                }

                safeHavenSection.set(name, chunkStringList)
            }
        }

        config.save()
    }

    fun isInSafeHaven(entity: Entity) : Boolean {
        return getSafeHaven(entity.chunk) != null
    }

}