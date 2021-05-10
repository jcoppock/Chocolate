package io.github.jamessoda.mc.chocolate.economy.bank.menu

import io.github.jamessoda.mc.chocolate.Chocolate
import io.github.jamessoda.mc.chocolate.utils.extensions.setCustomModelData
import io.github.jamessoda.mc.chocolate.utils.extensions.setDescription
import io.github.jamessoda.mc.chocolate.utils.extensions.setDisplayName
import io.github.jamessoda.mc.chocolate.utils.menu.Menu
import io.github.jamessoda.mc.chocolate.utils.menu.OpenMenuItem
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

class BankMenu : Menu(1) {

    private val depositMenu = DepositBankMenu()
    private val withdrawMenu = WithdrawBankMenu()

    override fun getTitle(player: Player): String {
        return "&2Bank &f- Deposit or Withdraw?"
    }

    override fun openInventory(player: Player) {
        depositMenu.amounts[player.uniqueId] = 0
        withdrawMenu.amounts[player.uniqueId] = 0
        super.openInventory(player)
    }

    init {

        setItem(2, object: OpenMenuItem(depositMenu) {
            override fun getItemStack(player: Player): ItemStack {

                val item = ItemStack(Material.LIME_DYE)
                item.setCustomModelData(2)

                item.setDisplayName("&6Click to Deposit ^coins^Coins")

                item.setDescription(player, "bank.deposit_menu_lore")

                return item
            }
        })

        setItem(6, object: OpenMenuItem(withdrawMenu) {
            override fun getItemStack(player: Player): ItemStack {

                val item = ItemStack(Material.RED_DYE)
                item.setCustomModelData(2)

                item.setDisplayName("&6Click to Withdraw ^coins^Coins")

                item.setDescription(player, "bank.withdraw_menu_lore")

                return item
            }
        })
    }

    companion object {
        val instance by lazy {
            BankMenu()
        }
    }

}