package io.github.jamessoda.mc.chocolate

import net.kyori.adventure.platform.bukkit.BukkitAudiences
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.configuration.InvalidConfigurationException
import org.bukkit.configuration.file.YamlConfiguration
import org.bukkit.entity.Player
import java.io.File
import java.io.IOException


class Language(private val plugin: Chocolate) {

    private val customColors = HashMap<String, String>()

    private val templates = HashMap<String, String>()

    private val messages = HashMap<String, String>()

    private val audience = BukkitAudiences.create(plugin)


    init {
        load()
    }

    fun load() {
        customColors.clear()
        templates.clear()
        messages.clear()

        val messagesFile = File(plugin.dataFolder.absolutePath +
                "${File.separator}lang.yml")

        if (!messagesFile.exists()) {
            messagesFile.parentFile.mkdirs()
            plugin.saveResource("lang.yml", false)
        }


        val messagesConfig = YamlConfiguration()
        var loaded = true
        try {
            messagesConfig.load(messagesFile)
        } catch (e: IOException) {
            e.printStackTrace()
            loaded = false
        } catch (e: InvalidConfigurationException) {
            e.printStackTrace()
            loaded = false
        }

        if (loaded) {
            val colorsSection = messagesConfig.getConfigurationSection("colors")

            colorsSection?.getKeys(false)?.forEach { key ->
                val potentialColorString = colorsSection.getString(key)
                if(potentialColorString != null) {
                    customColors["{$key}"] =
                            parseCustomColors(potentialColorString)
                }
            }

            val templatesSection = messagesConfig.getConfigurationSection("templates")

            templatesSection?.getKeys(false)?.forEach { key ->
                val potentialTemplateString = templatesSection.getString(key)
                if(potentialTemplateString != null) {
                    templates["%$key%"] = potentialTemplateString
                }
            }


            val messagesSection = messagesConfig.getConfigurationSection("messages")


            if (messagesSection != null) {
                messages.putAll(getMessagesInSection(messagesSection))
            }
        }
    }

    private fun getMessagesInSection(section: ConfigurationSection): HashMap<String, String> {

        val messagesInSection = HashMap<String, String>()

        section.getKeys(false).forEach { key ->
            val potentialSection = section.getConfigurationSection(key)
            if (potentialSection != null) {
                messagesInSection.putAll(getMessagesInSection(potentialSection))
            } else {
                var potentialMessage = section.getString(key)
                if (potentialMessage != null) {
                    for(pair in templates.entries) {
                        potentialMessage = potentialMessage?.replace(pair.key, pair.value)
                    }
                    for(pair in customColors.entries) {
                        potentialMessage = potentialMessage?.replace(pair.key, pair.value)
                    }
                    messagesInSection["${section.currentPath?.replace(
                            "messages.", "")}.$key"] = potentialMessage!!
                }
            }
        }

        return messagesInSection
    }

    private fun parseCustomColors(message: String) : String {
        for (pair in customColors.entries) {
            message.replace(pair.key, pair.value)
        }
        return message
    }

    fun sendMessage(player: Player, key: String) {
        val placeholders = mutableMapOf<String, String>()
        placeholders["{name}"] = player.name
        placeholders["{balance}"] = "${plugin.economyManager.getBalance(player)}"

        sendMessage(player, key, placeholders)
    }

    fun sendMessage(player: Player, key: String, placeholders: Map<String, String>) {

        var plainText = messages[key] ?: return
        for(pair in placeholders) {
            plainText = plainText.replace(pair.key, pair.value)
        }
        val component = MiniMessage.get().parse(plainText)

        audience.player(player).sendMessage(component)

    }


}