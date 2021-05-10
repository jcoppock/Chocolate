package io.github.jamessoda.mc.chocolate.utils

import org.bukkit.Chunk
import org.bukkit.Location
import java.util.*


object WorldUtils {
    fun getChunkOutline(chunk: Chunk, yPos: Double) : List<Location> {
        val startingBlock = chunk.getBlock(0, 0, 0)

        val startingLoc = startingBlock.location

        val results = mutableListOf(startingLoc)

        for(x in 0..32) {
            val loc1 = startingLoc.clone()
            loc1.add(x / 2.0, 0.0, 0.0)
            results.add(loc1)

            val loc2 = startingLoc.clone()
            loc2.add(x / 2.0, 0.0, 16.0)
            results.add(loc2)
        }

        for(z in 0..32) {
            val loc1 = startingLoc.clone()
            loc1.add(0.0, 0.0, z / 2.0)
            results.add(loc1)

            val loc2 = startingLoc.clone()
            loc2.add(16.0, 0.0, z / 2.0)
            results.add(loc2)
        }

        results.forEach {
            it.y = yPos
        }

        return results
    }

    fun getChunksAround(origin: Chunk, radius: Int): Set<Chunk> {
        val world = origin.world
        val length = radius * 2 + 1
        val chunks: MutableSet<Chunk> = HashSet(length * length)
        val cX = origin.x
        val cZ = origin.z
        for (x in -radius..radius) {
            for (z in -radius..radius) {
                chunks.add(world.getChunkAt(cX + x, cZ + z))
            }
        }
        return chunks
    }

    fun squaredDistance(loc1: Location, loc2: Location) : Double {

        val xDiff = loc1.x - loc2.x
        val yDiff = loc1.y - loc2.y
        val zDiff = loc1.z - loc2.z

        return xDiff * xDiff + yDiff * yDiff + zDiff * zDiff
    }
}