package io.github.jamessoda.mc.chocolate.economy.triple_e.menu.buy

import io.github.jamessoda.mc.chocolate.Chocolate.Companion.plugin
import io.github.jamessoda.mc.chocolate.utils.extensions.hasFullInventory
import io.github.jamessoda.mc.chocolate.utils.extensions.safeAddItem
import io.github.jamessoda.mc.chocolate.utils.extensions.setDescription
import io.github.jamessoda.mc.chocolate.utils.extensions.setDisplayName
import io.github.jamessoda.mc.chocolate.utils.menu.Menu
import io.github.jamessoda.mc.chocolate.utils.menu.MenuItem
import org.bson.types.ObjectId
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import kotlin.math.floor

class YourBuyOrderItem(private val menu: ViewYourBuyOrdersMenu, val slot: Int) : MenuItem {

    override fun getItemStack(player: Player): ItemStack {
        val orderIds = menu.orderIds[player] ?: mutableListOf()

        if (orderIds.size > slot) {

            val orderId = orderIds[slot]

            val itemName = plugin.database.getItemNameSync(orderId) ?: return ItemStack(Material.WRITABLE_BOOK)

            val icon = if (itemName.toIntOrNull() != null) {
                val chocItem = plugin.itemsConfig.getItem(itemName.toInt())
                chocItem?.clone() ?: ItemStack(Material.WRITABLE_BOOK)
            } else {
                ItemStack(Material.valueOf(itemName))
            }

            icon.amount = plugin.database.getItemAmountSync(orderId) ?: 1

            val description = mutableListOf(
                    ""
            )

            if (plugin.database.getSellerSync(orderId) == null) {
                description.add("${ChatColor.GRAY}Click to cancel your Order")
            } else {
                description.add("${ChatColor.GRAY}Click to collect your Order")
            }

            icon.setDescription(description)

            return icon

        } else {
            val icon = ItemStack(Material.WRITABLE_BOOK)
            icon.setDisplayName("${ChatColor.GOLD}Place a Buy Order")
            icon.setDescription(listOf("${ChatColor.GRAY}Click to place an order for an item."))
            return icon
        }
    }

    override fun onClick(player: Player) {
        val orderIds = menu.orderIds[player] ?: mutableListOf()

        if (orderIds.size > slot) {
            val orderId = orderIds[slot]
            val itemName = plugin.database.getItemNameSync(orderId) ?: return

            val item = if (itemName.toIntOrNull() != null) {
                plugin.itemsConfig.getItem(itemName.toInt())
            } else {
                ItemStack(Material.valueOf(itemName))
            }

            if (item != null) {
                item.amount = plugin.database.getItemAmountSync(orderId) ?: return

                if (plugin.database.getSellerSync(orderId) == null) {

                    val price = plugin.database.getItemPriceSync(orderId) ?: return

                    val fee = floor(price * .02).toInt()

                    plugin.database.addCoins(player, price + fee)
                    plugin.database.deleteOrder(orderId)
                    Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                        ViewYourBuyOrdersMenu.menu.openInventory(player)
                    }, 5L)

                } else {

                    if (player.hasFullInventory()) {
                        player.sendMessage("${ChatColor.RED}Your inventory is full!")
                    } else {
                        player.safeAddItem(item)
                        plugin.database.setCollectedSync(orderId)
                        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                            ViewYourBuyOrdersMenu.menu.openInventory(player)
                        }, 5L)
                    }
                }
                return
            }
        }

        PlaceBuyOrderMenu.menu.resetPlayerOrder(player)
        PlaceBuyOrderMenu.menu.openInventory(player)

    }
}

class YourBuyOrderIndicatorItem(private val menu: ViewYourBuyOrdersMenu, val slot: Int) : MenuItem {


    override fun getItemStack(player: Player): ItemStack {

        val orderIds = menu.orderIds[player] ?: mutableListOf()

        if (orderIds.size > slot) {

            val orderId = orderIds[slot]

            val icon = if (plugin.database.getSellerSync(orderId) == null) {
                ItemStack(Material.BARRIER)
            } else {
                ItemStack(Material.LIME_STAINED_GLASS_PANE)
            }

            icon.setDisplayName(" ")

            return icon
        } else {
            return ItemStack(Material.AIR)
        }
    }

    override fun onClick(player: Player) {}
}

class ViewYourBuyOrdersMenu : Menu(6) {

    override fun getTitle(player: Player): String {
        return "Your Buy Orders"
    }

    val orderIds = mutableMapOf<Player, List<ObjectId>>()

    override fun openInventory(player: Player) {
        plugin.database.getCurrentBuyOrdersAsync(player.uniqueId) {
            orderIds[player] = it
            Bukkit.getScheduler().runTask(plugin, Runnable {
                super.openInventory(player)
            })
        }
    }

    init {

        for (i in 0..2) {
            setItem(i, YourBuyOrderIndicatorItem(this, 0))
            setItem(i + 9, YourBuyOrderIndicatorItem(this, 0))
            setItem(i + 18, YourBuyOrderIndicatorItem(this, 0))
        }

        for (i in 3..5) {
            setItem(i, YourBuyOrderIndicatorItem(this, 1))
            setItem(i + 9, YourBuyOrderIndicatorItem(this, 1))
            setItem(i + 18, YourBuyOrderIndicatorItem(this, 1))
        }

        for (i in 6..8) {
            setItem(i, YourBuyOrderIndicatorItem(this, 2))
            setItem(i + 9, YourBuyOrderIndicatorItem(this, 2))
            setItem(i + 18, YourBuyOrderIndicatorItem(this, 2))
        }

        for (i in 27..29) {
            setItem(i, YourBuyOrderIndicatorItem(this, 3))
            setItem(i + 9, YourBuyOrderIndicatorItem(this, 3))
            setItem(i + 18, YourBuyOrderIndicatorItem(this, 3))
        }

        for (i in 30..32) {
            setItem(i, YourBuyOrderIndicatorItem(this, 4))
            setItem(i + 9, YourBuyOrderIndicatorItem(this, 4))
            setItem(i + 18, YourBuyOrderIndicatorItem(this, 4))
        }
        for (i in 33..35) {
            setItem(i, YourBuyOrderIndicatorItem(this, 5))
            setItem(i + 9, YourBuyOrderIndicatorItem(this, 5))
            setItem(i + 18, YourBuyOrderIndicatorItem(this, 5))
        }


        setItem(10, YourBuyOrderItem(this, 0))
        setItem(13, YourBuyOrderItem(this, 1))
        setItem(16, YourBuyOrderItem(this, 2))
        setItem(37, YourBuyOrderItem(this, 3))
        setItem(40, YourBuyOrderItem(this, 4))
        setItem(43, YourBuyOrderItem(this, 5))

    }

    companion object {
        val menu = ViewYourBuyOrdersMenu()
    }
}