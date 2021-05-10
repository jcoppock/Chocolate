package io.github.jamessoda.mc.chocolate.economy

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.*
import co.aikar.commands.bukkit.contexts.OnlinePlayer
import io.github.jamessoda.mc.chocolate.Chocolate.Companion.plugin
import io.github.jamessoda.mc.chocolate.economy.bank.menu.BankMenu
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player

@CommandAlias("economy|eco")
class EconomyCommand : BaseCommand() {

    val database = plugin.database

    @Default
    @Subcommand("balance|bal")
    @CommandAlias("balance|bal")
    fun onBal(player: Player) {
        plugin.language.sendMessage(player, "economy.balance")
    }

    @Subcommand("pay|give")
    @CommandCompletion("@players")
    @CommandAlias("pay")
    fun onPay(player: Player, target: OnlinePlayer, amount: Int) {

        if (player == target.player) {
            plugin.language.sendMessage(player, "economy.pay_oneself")
            return
        }

        var coinPouch = database.getCoinPouch(player)

        if (coinPouch < amount) {
            plugin.language.sendMessage(player, "economy.insufficient_funds")
        } else {
            var targetCoinPouch = database.getCoinPouch(target.player)
            coinPouch -= amount
            targetCoinPouch += amount

            database.setCoinPouch(player, coinPouch)
            database.setCoinPouch(target.player, targetCoinPouch)

            plugin.language.sendMessage(player, "economy.payment_give", mapOf(
                    Pair("{player}", target.player.name), Pair("{coins}", "$amount")))

            plugin.language.sendMessage(target.player, "economy.payment_receive", mapOf(
                    Pair("{player}", player.name), Pair("{coins}", "$amount")))
        }
    }

    @Subcommand("leaderboard|top")
    @CommandAlias("baltop")
    fun onLeaderboard(player: Player) {

        database.getEconomyTop() { topUsers ->
            topUsers.sortedWith(compareByDescending { database.getTotalMoney(it) })

            Bukkit.getScheduler().runTask(plugin, Runnable {
                plugin.language.sendMessage(player, "economy.leaderboard_header")
                for (i in 1..topUsers.size) {
                    val uuid = topUsers[i - 1]

                    plugin.language.sendMessage(player, "economy.leaderboard_entry", mapOf(
                            Pair("{position}", "$i"), Pair("{player}", Bukkit.getOfflinePlayer(uuid).name ?: ""),
                            Pair("{total_money}", "${database.getTotalMoney(uuid)}")))
                }
            })
        }
    }

    @Subcommand("admin give")
    @CommandPermission("chocolate.admin")
    @CommandCompletion("@players @currency-type @nothing")
    fun onAdminGive(sender: CommandSender, target: OnlinePlayer, currency: String, amount: Int) {
        sender.sendMessage("${ChatColor.GREEN}Gave money")

        if (currency.toLowerCase() == "bank") {
            database.addBankBalance(target.player, amount)
        } else if (currency.toLowerCase() == "pouch") {
            database.addCoins(target.player, amount)
        }

        target.player.sendMessage("${ChatColor.GREEN}Gave [${amount}] to [${target.player.name}]'s $currency.")
    }

    @Subcommand("admin bank")
    @CommandPermission("chocolate.admin")
    fun onAdminBank(player: Player) {
        BankMenu.instance.openInventory(player)
    }
}