package io.github.jamessoda.mc.chocolate

import co.aikar.commands.PaperCommandManager
import io.github.jamessoda.mc.chocolate.economy.EconomyCommand
import io.github.jamessoda.mc.chocolate.db.DatabaseHandler
import io.github.jamessoda.mc.chocolate.economy.EconomyManager
import io.github.jamessoda.mc.chocolate.guild.GuildCommand
import io.github.jamessoda.mc.chocolate.guild.GuildManager
import io.github.jamessoda.mc.chocolate.listeners.ConnectionListener
import org.bukkit.Bukkit
import org.bukkit.plugin.java.JavaPlugin
import org.bukkit.util.Vector
import java.io.BufferedReader
import java.io.File
import java.io.InputStream
import java.io.InputStreamReader
import java.util.*

class Chocolate : JavaPlugin() {

    val database by lazy { DatabaseHandler() }
    val economyManager by lazy { EconomyManager(this) }
    val guildManager by lazy { GuildManager(this) }

    val language by lazy { Language(this) }

    private val commandManager by lazy { PaperCommandManager(this) }

    override fun onEnable() {
        Bukkit.getPluginManager().registerEvents(ConnectionListener(this), this)

        registerCommands()
        updateCommandCompletions()
    }

    override fun onDisable() {

    }

    private fun registerCommands() {
        commandManager.registerCommand(EconomyCommand(this))
        commandManager.registerCommand(GuildCommand(this))
    }

    fun updateCommandCompletions() {

    }
}