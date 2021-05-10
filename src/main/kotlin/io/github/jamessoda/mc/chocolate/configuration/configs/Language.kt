package io.github.jamessoda.mc.chocolate.configuration.configs

import io.github.jamessoda.mc.chocolate.Chocolate
import io.github.jamessoda.mc.chocolate.configuration.ConfigFile
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.ChatMessageType
import net.md_5.bungee.api.chat.TextComponent
import org.bson.types.ObjectId
import org.bukkit.Bukkit
import org.bukkit.ChatColor.getLastColors
import org.bukkit.configuration.ConfigurationSection
import org.bukkit.entity.Player
import java.awt.Color
import java.lang.NumberFormatException
import java.util.logging.Level
import java.util.regex.Pattern

class Language(private val plugin: Chocolate) : ConfigFile("lang.yml", hasDefaults = true) {

    private val database by lazy {
        plugin.database
    }
    private val guildManager by lazy {
        plugin.inviteManager
    }

    private val customColors = HashMap<String, Color>()

    private val templates = HashMap<String, String>()

    private val messages = HashMap<String, String>()

    private val menuText = HashMap<String, String>()

    private lateinit var scoreboardTitle: String
    private val scoreboardLines = mutableListOf<String>()

    init {
        load()
    }

    fun load() {
        customColors.clear()
        templates.clear()
        messages.clear()

        val colorsSection = yaml.getConfigurationSection("colors")

        colorsSection?.getKeys(false)?.forEach { key ->
            val potentialColorString = colorsSection.getString(key)
            if (potentialColorString != null) {
                val color = parseCustomColors(potentialColorString)
                if (color != null) {
                    customColors["^$key^"] = color
                } else {
                    Bukkit.getLogger().log(Level.SEVERE, "Color String is not formatted properly for [$key]")
                }
            }
        }

        val templatesSection = yaml.getConfigurationSection("templates")

        templatesSection?.getKeys(false)?.forEach { key ->
            val potentialTemplateString = templatesSection.getString(key)
            if (potentialTemplateString != null) {
                templates["%$key%"] = potentialTemplateString
            }
        }


        val messagesSection = yaml.getConfigurationSection("messages")


        if (messagesSection != null) {
            messages.putAll(getMessagesInSection(messagesSection, "messages"))
        }

        val menuTextSection = yaml.getConfigurationSection("menu")
        if (menuTextSection != null) {
            menuText.putAll(getMessagesInSection(menuTextSection, "menu"))
        }

        val scoreboardSection = yaml.getConfigurationSection("scoreboard")

        if (scoreboardSection != null) {
            scoreboardLines.addAll(scoreboardSection.getStringList("lines"))
            scoreboardTitle = scoreboardSection.getString("title").toString()
        }
    }

    private fun getMessagesInSection(section: ConfigurationSection, sectionName: String): HashMap<String, String> {

        val messagesInSection = HashMap<String, String>()

        section.getKeys(false).forEach { key ->
            val potentialSection = section.getConfigurationSection(key)
            if (potentialSection != null) {
                messagesInSection.putAll(getMessagesInSection(potentialSection, sectionName))
            } else {

                var potentialMessage: String?

                val potentialListMessage = section.getStringList(key)
                if (potentialListMessage.isNotEmpty()) {

                    potentialMessage = ""

                    potentialListMessage.forEach {
                        if (potentialListMessage.last() != it) {
                            potentialMessage += it + "\n"
                        } else {
                            potentialMessage += it
                        }
                    }

                } else {
                    potentialMessage = section.getString(key)
                }

                if (potentialMessage != null) {

                    messagesInSection["${
                        section.currentPath?.replace(
                                "$sectionName.", "")
                    }.$key"] = replacements(potentialMessage)
                }
            }
        }

        return messagesInSection
    }

    fun coinsReplace(string: String) : String {

        val pattern = Pattern.compile("(!)([0-9]+)(c)", Pattern.CASE_INSENSITIVE)

        val matcher = pattern.matcher(string)

        var result = string

        while(matcher.find()) {
            val lastColor = getLastColors(matcher.group(0))
            val coins = matcher.group(2)
            val replacement = replacements("%coins%", mapOf(Pair("{coins}", coins)))
            plugin.logger.log(Level.SEVERE, (replacement))
            result = result.replace(matcher.group(0), replacement + lastColor)
        }

        return result
    }

    fun replacements(string: String, placeholders: Map<String, String> = mutableMapOf()): String {

        var result = string

        for (pair in templates.entries) {
            result = result.replace(pair.key, pair.value)
        }
        for (pair in customColors.entries) {
            result = result.replace(pair.key, ChatColor.of(pair.value).toString())
        }
        for (pair in placeholders) {
            result = result.replace(pair.key, pair.value)
        }

        result = ChatColor.translateAlternateColorCodes('&', result)

        return result
    }

    private fun parseCustomColors(color: String): Color? {
        return try {
            Color.decode(color)
        } catch (e: NumberFormatException) {
            e.printStackTrace()
            null
        }
    }

    fun replaceColors(string: String): String {
        var result = string
        for (pair in customColors) {
            result = result.replace(pair.key, ChatColor.of(pair.value).toString())
        }

        return ChatColor.translateAlternateColorCodes('&', result)
    }

    fun replacePlaceholders(player: Player, string: String): String {
        var result = string
        for (pair in defaultPlaceholders(player)) {
            result = result.replace(pair.key, pair.value)
        }
        return result
    }

    fun replacePlaceholders(player: Player, string: String, placeholders: Map<String, String>): String {
        val finPlaceholders = defaultPlaceholders(player).toMutableMap()
        finPlaceholders.putAll(placeholders)
        var result = string
        for (pair in finPlaceholders) {
            result = result.replace(pair.key, pair.value)
        }
        return result
    }

    fun getScoreboardTitle(): String {
        return replacements(scoreboardTitle)
    }

    fun getScoreboardLines(player: Player, placeholders: Map<String, String> = mutableMapOf()): List<String> {

        val result = mutableListOf<String>()

        val finPlaceholders = defaultPlaceholders(player).toMutableMap()
        finPlaceholders.putAll(placeholders)

        for (line in scoreboardLines) {
            result.add(replacements(line, finPlaceholders))
        }

        return result
    }

    fun sendMessage(player: Player, key: String, actionBar: Boolean = false) {

        sendMessage(player, key, defaultPlaceholders(player), actionBar)
    }

    fun sendMessage(player: Player, key: String, placeholders: Map<String, String>, actionBar: Boolean = false) {

        val finPlaceholders = defaultPlaceholders(player).toMutableMap()

        // Replace default placeholders with any overridden placeholders
        for (pair in placeholders) {
            finPlaceholders[pair.key] = pair.value
        }

        var plainText = messages[key] ?: return
        for (pair in finPlaceholders) {
            plainText = plainText.replace(pair.key, pair.value)
        }

        for (pair in customColors) {
            plainText = plainText.replace(pair.key, ChatColor.of(pair.value).toString())
        }

        for (line in plainText.split("\n")) {

            if (actionBar) {
                player.spigot().sendMessage(ChatMessageType.ACTION_BAR, TextComponent(line))
            } else {
                player.sendMessage(line)
            }
        }
    }

    private fun defaultPlaceholders(player: Player): Map<String, String> {
        val placeholders = mutableMapOf<String, String>()
        placeholders["{name}"] = player.name
        placeholders["{coins}"] = "${database.getCoinPouch(player.uniqueId)}"
        placeholders["{bank_balance}"] = "${database.getBankBalance(player.uniqueId)}"
        placeholders["{total_money}"] = "${database.getTotalMoney(player.uniqueId)}"

        val guildId = database.getGuildIdOfPlayer(player.uniqueId)

        if (guildId != null) {
            placeholders["{guild_name}"] = database.getGuildName(guildId)
            placeholders["{guild_tag}"] = database.getGuildTag(guildId)
            placeholders["{guild_chunks}"] = "${database.getChunks(guildId).size}"
            placeholders["{guild_users}"] = "${database.getUUIDsInGuild(guildId).size}"
            placeholders["{guild_created_date}"] = "${database.getGuildCreationDate(guildId)}"
        }

        return placeholders
    }

    fun getMenuText(key: String): String {
        var plainText = menuText[key] ?: return ""

        plainText = replaceColors(plainText)

        return plainText
    }

    fun getMenuText(player: Player, key: String): String {
        return getMenuText(player, key, defaultPlaceholders(player))
    }

    fun getMenuText(player: Player, key: String, placeholders: Map<String, String>): String {
        val finPlaceholders = defaultPlaceholders(player).toMutableMap()

        // Replace default placeholders with any overridden placeholders
        for (pair in placeholders) {
            finPlaceholders[pair.key] = pair.value
        }

        var plainText = menuText[key] ?: return ""
        for (pair in finPlaceholders) {
            plainText = plainText.replace(pair.key, pair.value)
        }

        for (pair in customColors) {
            plainText = plainText.replace(pair.key, ChatColor.of(pair.value).toString())
        }

        return plainText
    }

    fun getGuildNameAtPosition(player: Player): String {
        var name = "${ChatColor.DARK_RED}Wilderness"
        val chunk = player.location.chunk

        val chunkOwner = database.getChunkOwner(chunk) ?: return name

        name = database.getGuildName(chunkOwner)

        if (chunkOwner == database.getGuildIdOfPlayer(player.uniqueId)) {
            name = "${ChatColor.GOLD}$name"
        } else {
            name = "${ChatColor.RED}$name"
        }

        return name
    }

    fun sendMessage(guildId: ObjectId, key: String, placeholders: Map<String, String>) {
        for(uuid in database.getUUIDsInGuild(guildId)) {
            val p = Bukkit.getOfflinePlayer(uuid)

            if(p.player != null && p.isOnline) {
                sendMessage(p.player!!, key, placeholders)
            }
        }
    }
}