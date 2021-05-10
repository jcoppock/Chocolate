package io.github.jamessoda.mc.chocolate.configuration.configs

import io.github.jamessoda.mc.chocolate.Chocolate.Companion.plugin
import io.github.jamessoda.mc.chocolate.configuration.ConfigFile
import io.github.jamessoda.mc.chocolate.portals.Portal
import io.github.jamessoda.mc.chocolate.utils.ConfigUtils
import org.bukkit.Bukkit
import org.bukkit.Chunk
import org.bukkit.Location
import org.bukkit.Material
import java.util.logging.Level

class Config : ConfigFile("config.yml", hasDefaults = true) {

    val chunkInitialCost = yaml.getInt("chunk.initial_cost")
    val chunkCostIncrease = yaml.getInt("chunk.cost_increase")

    val spawn: Location

    /***
     * Returns a list of Material Names/Chocolate Item Ids as Strings
     */
    val buyOrderBlacklist: List<String> by lazy {
        yaml.getStringList("economy.blacklist")
    }

    val databaseName = yaml.getString("database")

    init {

        val spawnString = yaml.getString("spawn")
        if(spawnString != null) {
            val tempSpawn = ConfigUtils.deserializeLocation(spawnString)
            if(tempSpawn == null) {
                Bukkit.getLogger().log(Level.SEVERE, "DESERIALIZE SPAWN STRING IS NULL")
                spawn = Bukkit.getWorld("flatworld")?.spawnLocation ?: Bukkit.getWorlds()[0].spawnLocation
            } else {
                spawn = tempSpawn
            }
        } else {
            spawn = Bukkit.getWorld("flatworld")?.spawnLocation ?: Bukkit.getWorlds()[0].spawnLocation
            Bukkit.getLogger().log(Level.SEVERE, "CONFIG SPAWN STRING IS NULL")
        }


        val portalSection = yaml.getConfigurationSection("portals")

        if (portalSection != null) {
            for (key in portalSection.getKeys(false)) {
                val name = key.replace("_", " ")
                val locationString = portalSection.getString(key) ?: ""

                val location = ConfigUtils.deserializeLocation(locationString) ?: continue

                plugin.portalManager.addPortal(Portal(location, name))
            }
        }

        val safeHavenSection = yaml.getConfigurationSection("safe-havens")

        if (safeHavenSection != null) {
            for (key in safeHavenSection.getKeys(false)) {

                val chunkStringList = safeHavenSection.getStringList(key)

                val chunkList = mutableSetOf<Chunk>()

                chunkStringList.forEach {
                    val chunk = ConfigUtils.deserializeChunk(it)
                    if (chunk != null) {
                        chunkList.add(chunk)
                    }
                }

                if (chunkList.isNotEmpty()) {
                    plugin.safeHavenManager.addSafeHaven(key, chunkList)
                }
            }
        }
    }
}