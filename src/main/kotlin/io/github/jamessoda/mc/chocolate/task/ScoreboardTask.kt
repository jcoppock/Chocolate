package io.github.jamessoda.mc.chocolate.task

import fr.mrmicky.fastboard.FastBoard
import io.github.jamessoda.mc.chocolate.Chocolate
import io.github.jamessoda.mc.chocolate.Chocolate.Companion.plugin
import net.md_5.bungee.api.ChatColor
import org.bukkit.Bukkit
import org.bukkit.scheduler.BukkitRunnable
import java.util.*

class ScoreboardTask(plugin: Chocolate) : BukkitRunnable() {

    val language = plugin.language

    private val safeHavenChunkManager = plugin.safeHavenManager

    private val scoreboards = mutableMapOf<UUID, FastBoard>()

    override fun run() {

        val iterator = scoreboards.iterator()

        while (iterator.hasNext()) {
            val pair = iterator.next()
            val player = Bukkit.getOfflinePlayer(pair.key)
            if (!player.isOnline) {
                pair.value.delete()
                iterator.remove()
            }
        }

        for (player in Bukkit.getOnlinePlayers()) {
            val board = scoreboards[player.uniqueId] ?: FastBoard(player)

            board.updateTitle(language.getScoreboardTitle())


            val chunk = player.chunk

            var locationName = safeHavenChunkManager.getSafeHaven(chunk)


            if (locationName == null) {
                locationName = language.getGuildNameAtPosition(player)
            } else {
                locationName = "${ChatColor.BLUE}${locationName.replace("_", " ")}"
            }

            val lines = language.getScoreboardLines(player, mapOf(Pair("{guild_name}", locationName)))

            val updatedLines = mutableListOf<String>()

            lines.forEach {
                updatedLines.add(String.format("%-24s", it))
            }

            board.updateLines(updatedLines)

            if (!scoreboards.containsKey(player.uniqueId)) {
                scoreboards[player.uniqueId] = board
            }
        }
    }
}