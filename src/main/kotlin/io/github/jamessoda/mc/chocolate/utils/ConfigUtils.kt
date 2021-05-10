package io.github.jamessoda.mc.chocolate.utils

import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Location
import java.util.logging.Level

object ConfigUtils {

    fun deserializeLocation(string: String) : Location? {
        Bukkit.getLogger().log(Level.INFO, "Loading Location at $string")

        val array = string.split(":")

        if(array.size != 4 && array.size != 6) {
            return null
        }

        val worldName = array[0]
        val x = array[1].toDoubleOrNull() ?: return null
        val y = array[2].toDoubleOrNull() ?: return null
        val z = array[3].toDoubleOrNull() ?: return null

        val world = Bukkit.getWorld(worldName) ?: return null

        if(array.size == 4) {
            return Location(world, x, y, z)
        } else if(array.size == 6){
            val pitch = array[4].toDoubleOrNull()
            val yaw = array[5].toDoubleOrNull()
            if(pitch == null || yaw == null) {
                return Location(world, x, y, z)
            }
            return Location(world, x, y, z, pitch.toFloat(), yaw.toFloat())
        } else {
            return null
        }
    }

    fun serializeLocation(location: Location, includeDirection: Boolean = false) : String? {

        val world = location.world ?: return null
        val x = location.x
        val y = location.y
        val z = location.z
        val pitch = location.pitch
        val yaw = location.yaw

        if(!includeDirection) {
            return "${world.name}:$x:$y:$z"
        } else {
            return "${world.name}:$x:$y:$z:$pitch:$yaw"
        }
    }

    fun serializeChunk(chunk: Chunk) : String {

        val world = chunk.world
        val x =  chunk.x
        val z = chunk.z

        return "${world.name}:$x:$z"
    }

    fun deserializeChunk(string: String) : Chunk? {
        val array = string.split(":")

        if(array.size != 3) {
            return null
        }

        val worldName = array[0]
        val x = array[1].toIntOrNull() ?: return null
        val z = array[2].toIntOrNull() ?: return null

        val world = Bukkit.getWorld(worldName) ?: return null

        return world.getChunkAt(x, z)
    }

}