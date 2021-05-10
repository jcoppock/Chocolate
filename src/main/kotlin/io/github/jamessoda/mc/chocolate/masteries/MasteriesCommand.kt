package io.github.jamessoda.mc.chocolate.masteries

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import io.github.jamessoda.mc.chocolate.Chocolate.Companion.plugin
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player

@CommandAlias("masteries|mastery")
class MasteriesCommand : BaseCommand() {

    @Default
    @HelpCommand
    fun onMasteries(player: Player) {

        plugin.language.sendMessage(player, "masteries.help")

    }

    @Subcommand("magic")
    @CommandAlias("magic")
    fun onMagic(player: Player) {
        Mastery.MAGIC.masteryMenu.open(player)
    }

    @Subcommand("leaderboard")
    fun onLeaderboard(player: Player) {

        player.sendMessage("${ChatColor.RED}Please provide a Mastery name. ${ChatColor.WHITE}/mastery leaderboard <name>")

    }


    @Subcommand("leaderboard")
    @CommandCompletion("@masteries")
    fun onLeaderboard(player: Player, masteryName: String) {

        val mastery = Mastery.getMastery(masteryName)

        if(mastery == null) {
            player.sendMessage("${ChatColor.RED}That is not a valid Mastery name!")
            return
        }

        plugin.database.getMasteryTop(mastery.name) {topUsers ->

            topUsers.sortedWith(compareByDescending { plugin.database.getMasteryLevel(it, masteryName) })

            Bukkit.getScheduler().runTask(plugin, Runnable {
                plugin.language.sendMessage(player, "masteries.leaderboard_header", mapOf(Pair("{name}", mastery.name)))
                for (i in 1..topUsers.size) {
                    val uuid = topUsers[i - 1]

                    plugin.language.sendMessage(player, "masteries.leaderboard_entry", mapOf(
                            Pair("{position}", "$i"), Pair("{player}", Bukkit.getOfflinePlayer(uuid).name ?: ""),
                            Pair("{level}", "${plugin.database.getMasteryLevel(uuid, mastery.name)}"),
                            Pair("{name}", mastery.name)))
                }
            })
        }
    }
}