package io.github.jamessoda.mc.chocolate.economy.bank.menu

import io.github.jamessoda.mc.chocolate.Chocolate.Companion.plugin
import io.github.jamessoda.mc.chocolate.utils.extensions.setCustomModelData
import io.github.jamessoda.mc.chocolate.utils.extensions.setDisplayName
import io.github.jamessoda.mc.chocolate.utils.menu.MenuItem
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.math.floor

class BankChangeAmountItem : MenuItem {

    private val menu: BankDepositWithdrawMenu

    private val withdraw: Boolean
    private val add: Boolean
    private val intAmount: Int?
    private val proportionAmount: Double?

    constructor(menu: BankDepositWithdrawMenu, withdraw: Boolean, amount: Int, add: Boolean = false) {
        this.menu = menu
        intAmount = amount
        proportionAmount = null
        this.add = add
        this.withdraw = withdraw
    }

    constructor(menu: BankDepositWithdrawMenu, withdraw: Boolean, proportion: Double) {
        this.menu = menu
        proportionAmount = proportion
        intAmount = null
        this.add = false
        this.withdraw = withdraw
    }

    override fun getItemStack(player: Player): ItemStack {
        val item = ItemStack(Material.GOLD_NUGGET)

        var displayName = ""

        var type = if(withdraw) {
            "Withdraw"
        } else {
            "Deposit"
        }

        if(intAmount != null) {

            if(add) {

                when(intAmount) {
                    1 -> item.setCustomModelData(2)
                    5 -> item.setCustomModelData(3)
                    10 -> item.setCustomModelData(4)
                    20 -> item.setCustomModelData(5)
                    50 -> item.setCustomModelData(6)
                    -1 -> item.setCustomModelData(7)
                    -5 -> item.setCustomModelData(8)
                    -10 -> item.setCustomModelData(9)
                    -20 -> item.setCustomModelData(10)
                    -50 -> item.setCustomModelData(11)
                }


                if(intAmount > 0) {
                    displayName = "&fAdd $intAmount Coins to the $type Amount"
                } else {
                    displayName = "&fRemove ${intAmount * -1} Coins from the $type Amount"
                }
            } else {
                displayName = "&fSet the $type Amount to $intAmount Coins"

                if(intAmount == 0) {
                    item.setCustomModelData(12)
                }

            }
        } else if(proportionAmount != null) {

            when(proportionAmount) {
                .5 -> item.setCustomModelData(13)
                1.0 -> item.setCustomModelData(14)
            }

            displayName = "&fSet the $type Amount to %${proportionAmount * 100} of your Coins"

        } else {
            return item
        }

        item.setDisplayName(displayName)

        return item
    }

    override fun onClick(player: Player) {
        if(intAmount != null) {
            if(add) {
                val current = menu.amounts[player.uniqueId] ?: 0

                if(current + intAmount < 0) {
                    menu.amounts[player.uniqueId] = 0
                } else {

                    var finAmount = current + intAmount

                    if(withdraw) {
                        val bankBalance = plugin.database.getBankBalance(player)

                        if(finAmount > bankBalance) {
                            finAmount = bankBalance
                        }

                    } else {
                        val coinPouch = plugin.database.getCoinPouch(player)

                        if(finAmount > coinPouch) {
                            finAmount = coinPouch
                        }
                    }

                    menu.amounts[player.uniqueId] = finAmount
                }
            } else {
                menu.amounts[player.uniqueId] = intAmount
            }

        } else if(proportionAmount != null) {

            val amount = if(withdraw) {
                plugin.database.getBankBalance(player)
            } else {
                plugin.database.getCoinPouch(player)
            }

            menu.amounts[player.uniqueId] = floor(amount * proportionAmount).toInt()
        }

        menu.refresh(player)
    }
}