package io.github.jamessoda.mc.chocolate.economy.bank.menu

import io.github.jamessoda.mc.chocolate.Chocolate
import io.github.jamessoda.mc.chocolate.Chocolate.Companion.plugin
import io.github.jamessoda.mc.chocolate.utils.extensions.setCustomModelData
import io.github.jamessoda.mc.chocolate.utils.extensions.setDisplayName
import io.github.jamessoda.mc.chocolate.utils.extensions.setDescription
import io.github.jamessoda.mc.chocolate.utils.menu.MenuItem
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

class DepositBankMenu() : BankDepositWithdrawMenu() {

    val database = plugin.database

    override fun getTitle(player: Player): String {
        val amount = amounts[player.uniqueId] ?: 0

        return if(amount == 0) {
            "Deposit your ^coins^Coins"
        } else {
            "Deposit ^coins^$amount Coins"
        }
    }

    init {
            Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, Runnable {
                val iterator = amounts.iterator()

                while(iterator.hasNext()) {
                    val pair = iterator.next()
                    val uuid = pair.key

                    if(Bukkit.getPlayer(uuid) == null) {
                        iterator.remove()
                    }
                }
            }, 0L, 2 * 60 * 20L)

        setItem(4, object: MenuItem {
            override fun getItemStack(player: Player): ItemStack {

                val item = ItemStack(Material.GOLD_NUGGET)

                item.setCustomModelData(1)

                item.setDisplayName(player, "&fYour ^coins^Coins&f:")

                item.setDescription(player, "bank.balance")

                return item
            }

            override fun onClick(player: Player) {}
        })

        setItem(9, BankChangeAmountItem(this, false, 1, true))
        setItem(11, BankChangeAmountItem(this, false, 5, true))
        setItem(13, BankChangeAmountItem(this, false, 10, true))
        setItem(15, BankChangeAmountItem(this, false, 20, true))
        setItem(17, BankChangeAmountItem(this, false, 50, true))

        setItem(18, BankChangeAmountItem(this, false, -1, true))
        setItem(20, BankChangeAmountItem(this, false, -5, true))
        setItem(22, BankChangeAmountItem(this, false, -10, true))
        setItem(24, BankChangeAmountItem(this, false, -20, true))
        setItem(26, BankChangeAmountItem(this, false, -50, true))

        setItem(28, BankChangeAmountItem(this, false, 0, false))
        setItem(31, BankChangeAmountItem(this, false, .5))
        setItem(34, BankChangeAmountItem(this, false, 1.0))


        setItem(51, object: MenuItem{
            override fun getItemStack(player: Player): ItemStack {
                val amount = amounts[player.uniqueId] ?: 0

                if(amount == 0) {
                    return ItemStack(Material.AIR)
                }

                val item = ItemStack(Material.LIME_DYE)
                item.setCustomModelData(1)

                item.setDisplayName(player, "bank.confirm_deposit_name", mapOf(Pair("{coins}", "$amount")))

                item.setDescription(player, "bank.confirm_deposit_lore", mapOf(Pair("{coins}", "$amount")))

                return item

            }

            override fun onClick(player: Player) {
                val amount = amounts[player.uniqueId] ?: 0

                if(amount == 0) {
                    return
                }

                player.closeInventory()
                database.addBankBalance(player, amount)
                database.addCoins(player, -amount)

                player.closeInventory()
                plugin.language.sendMessage(player, "bank.deposit", mapOf(Pair("{coins}", "$amount")))
                amounts[player.uniqueId] = 0

            }
        })
    }
}