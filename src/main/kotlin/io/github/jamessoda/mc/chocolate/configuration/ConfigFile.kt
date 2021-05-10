package io.github.jamessoda.mc.chocolate.configuration

import io.github.jamessoda.mc.chocolate.Chocolate.Companion.plugin
import org.bukkit.Bukkit
import org.bukkit.configuration.file.YamlConfiguration
import java.io.File

abstract class ConfigFile(val fileName: String, val folder: String = "",
                          val hasDefaults : Boolean = false) {

    private val configFile = if(folder != "") {
        File(plugin.dataFolder.absoluteFile, folder + File.separator + fileName)
    } else {
        File(plugin.dataFolder.absoluteFile, fileName)
    }

    val yaml : YamlConfiguration by lazy {
        if (!configFile.exists()) {
            configFile.parentFile.mkdirs()
            if(hasDefaults) {
                plugin.saveResource(fileName, false)
            }
        }

        YamlConfiguration.loadConfiguration(configFile)
    }

    fun save() {
        yaml.save(configFile)
    }

}