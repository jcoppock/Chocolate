package io.github.jamessoda.mc.chocolate.economy.triple_e.menu.buy

import io.github.jamessoda.mc.chocolate.utils.ItemUtils
import io.github.jamessoda.mc.chocolate.utils.extensions.setDescription
import io.github.jamessoda.mc.chocolate.utils.extensions.setDisplayName
import io.github.jamessoda.mc.chocolate.utils.menu.Menu
import io.github.jamessoda.mc.chocolate.utils.menu.MenuItem
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

class ViewBuyOrdersMenu : Menu(1) {

    override fun getTitle(player: Player): String {
        return "View Buy Orders"
    }

    init {

        setItem(2, object: MenuItem {
            override fun getItemStack(player: Player): ItemStack {
                val item = ItemUtils.getSkull(player)

                item.setDisplayName("${ChatColor.GOLD}View Your Buy Orders")
                item.setDescription(listOf("${ChatColor.GRAY}Click to view or collect your placed Buy Orders."))
                return item
            }

            override fun onClick(player: Player) {
                ViewYourBuyOrdersMenu.menu.openInventory(player)
            }
        })

        setItem(6, object: MenuItem {
            override fun getItemStack(player: Player): ItemStack {
                val item = ItemStack(Material.BOOK)
                item.setDisplayName("${ChatColor.GOLD}View Other Buy Orders")
                item.setDescription(listOf("${ChatColor.GRAY}Click to view others' Buy Orders."))
                return item
            }

            override fun onClick(player: Player) {
                ViewOtherBuyOrdersMenu.menu.openInventory(player)
            }
        })

    }

    companion object {
        val menu = ViewBuyOrdersMenu()
    }

}