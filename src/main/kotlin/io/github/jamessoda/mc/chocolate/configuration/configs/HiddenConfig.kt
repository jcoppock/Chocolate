package io.github.jamessoda.mc.chocolate.configuration.configs

import io.github.jamessoda.mc.chocolate.configuration.ConfigFile
import org.bukkit.Bukkit
import java.util.logging.Level

class HiddenConfig : ConfigFile("hidden-config.yml", hasDefaults = true) {

    val botEnabled = yaml.getBoolean("bot-enabled")

    val botToken = yaml.getString("bot-token")


    val requiredRolesPerServer = mutableMapOf<String, List<String>>()


    init {
        val serversSection = yaml.getConfigurationSection("servers")

        if(serversSection != null) {
            for (id in serversSection.getKeys(false)) {
                Bukkit.getLogger().log(Level.INFO, "Found Discord Server: $id")
                requiredRolesPerServer[id] = serversSection.getStringList(id)

                Bukkit.getLogger().log(Level.INFO, "Roles For Discord Server $id: ${requiredRolesPerServer[id]}")
            }
        }
    }

}