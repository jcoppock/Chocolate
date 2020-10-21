package io.github.jamessoda.mc.chocolate.economy

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import io.github.jamessoda.mc.chocolate.Chocolate
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.mongodb.morphia.query.FindOptions

@CommandAlias("economy|eco")
class EconomyCommand(private val plugin: Chocolate) : BaseCommand() {

    private val economy = plugin.economyManager

    @Default
    @Subcommand("balance|bal")
    @CommandAlias("balance|bal")
    fun onBal(player: Player) {
        plugin.language.sendMessage(player, "economy.balance")
    }

    @Subcommand("pay|give")
    @CommandCompletion("@players @nothing")
    fun onPay(player: Player, target: OnlinePlayer, amount: Int) {

        if(player == target.player) {
            plugin.language.sendMessage(player, "economy.pay_oneself")
            return
        }

        var balance = economy.getBalance(player)

        if(balance < amount) {
            plugin.language.sendMessage(player, "economy.insufficient_funds")
        } else {
            var targetBalance = economy.getBalance(target.player)
            balance -= amount
            targetBalance += amount

            economy.setBalance(player, balance)
            economy.setBalance(target.player, targetBalance)

            plugin.language.sendMessage(player, "economy.payment_give", mapOf(
                    Pair("{player}", target.player.name), Pair("{amount}", "$amount")))

            plugin.language.sendMessage(target.player, "economy.payment_receive", mapOf(
                    Pair("{player}", player.name), Pair("{amount}", "$amount")))
        }
    }

    @Subcommand("leaderboard|top")
    fun onLeaderboard(player: Player) {
        val query = plugin.database.createUserQuery()

        val topUsers = query.field("balance").greaterThan(0).order("-balance").asList(FindOptions().limit(10))

        plugin.language.sendMessage(player, "economy.leaderboard_header")
        for(i in 1..topUsers.size) {
            val user = topUsers[i - 1]
            plugin.language.sendMessage(player, "economy.leaderboard_entry", mapOf(
                    Pair("{position}", "$i"), Pair("{player}", user.username!!),
                    Pair("{amount}", "${user.balance}")))
        }
    }

    @Subcommand("admin give")
    @CommandPermission("chocolate.admin")
    @CommandCompletion("@players @nothing")
    fun onAdminGive(sender: CommandSender, target: OnlinePlayer, amount: Int) {
        sender.sendMessage("${ChatColor.GREEN}Gave money")

        var balance = economy.getBalance(target.player)
        balance += amount

        economy.setBalance(target.player, balance)

        target.player.sendMessage("${ChatColor.GREEN}Given money [${amount}]")
    }
}