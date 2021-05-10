package io.github.jamessoda.mc.chocolate.economy.triple_e.menu

import io.github.jamessoda.mc.chocolate.economy.triple_e.menu.buy.PlaceBuyOrderMenu
import io.github.jamessoda.mc.chocolate.economy.triple_e.menu.buy.ViewBuyOrdersMenu
import io.github.jamessoda.mc.chocolate.economy.triple_e.menu.buy.ViewOtherBuyOrdersMenu
import io.github.jamessoda.mc.chocolate.utils.extensions.setDescription
import io.github.jamessoda.mc.chocolate.utils.extensions.setDisplayName
import io.github.jamessoda.mc.chocolate.utils.menu.Menu
import io.github.jamessoda.mc.chocolate.utils.menu.MenuItem
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class TripleEMainMenu : Menu(1) {

    override fun getTitle(player: Player): String {
        return "Epic Economic Exchange"
    }


    init {

        setItem(2, object: MenuItem {
            override fun getItemStack(player: Player): ItemStack {
                val item = ItemStack(Material.BOOK)
                item.setDisplayName("${ChatColor.GOLD}View Buy Orders")
                item.setDescription(listOf("${ChatColor.GRAY}Click to view the items people would like to buy."))
                return item
            }

            override fun onClick(player: Player) {
                ViewBuyOrdersMenu.menu.openInventory(player)
                ViewOtherBuyOrdersMenu.menu.currentPages[player] = 0
            }
        })

        setItem(6, object: MenuItem {
            override fun getItemStack(player: Player): ItemStack {
                val item = ItemStack(Material.WRITABLE_BOOK)
                item.setDisplayName("${ChatColor.GOLD}Place a Buy Order")
                item.setDescription(listOf("${ChatColor.GRAY}Click to place an order for an item."))
                return item
            }

            override fun onClick(player: Player) {
                PlaceBuyOrderMenu.menu.resetPlayerOrder(player)
                PlaceBuyOrderMenu.menu.openInventory(player)
            }
        })

    }

    companion object {

        val menu = TripleEMainMenu()

    }

}