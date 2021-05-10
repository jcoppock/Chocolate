package io.github.jamessoda.mc.chocolate.economy.triple_e.menu.buy

import io.github.jamessoda.mc.chocolate.Chocolate.Companion.plugin
import io.github.jamessoda.mc.chocolate.utils.extensions.*
import io.github.jamessoda.mc.chocolate.utils.menu.Menu
import io.github.jamessoda.mc.chocolate.utils.menu.MenuItem
import org.bson.types.ObjectId
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable
import java.text.SimpleDateFormat

class BuyOrderItem(private val menu: ViewOtherBuyOrdersMenu, private val slot: Int) : MenuItem {

    private val dateFormat = SimpleDateFormat("yy/MM/dd hh:mmZ")

    override fun getItemStack(player: Player): ItemStack {
        val orderIds = menu.orderIds[player] ?: mutableListOf()

        if (orderIds.size > slot) {

            val orderId = orderIds[slot]

            val itemName = plugin.database.getItemNameSync(orderId) ?: return ItemStack(Material.AIR)
            val itemAmount = plugin.database.getItemAmountSync(orderId) ?: return ItemStack(Material.AIR)
            val itemPrice = plugin.database.getItemPriceSync(orderId) ?: return ItemStack(Material.AIR)
            val date = plugin.database.getDatePlacedSync(orderId) ?: return ItemStack(Material.AIR)

            val buyerUUID = plugin.database.getBuyerSync(orderId) ?: return ItemStack(Material.AIR)
            val buyerUsername = Bukkit.getServer().getOfflinePlayer(buyerUUID).name ?: return ItemStack(Material.AIR)

            val icon = if (itemName.toIntOrNull() != null) {
                val chocItem = plugin.itemsConfig.getItem(itemName.toInt())
                chocItem?.clone() ?: ItemStack(Material.WRITABLE_BOOK)
            } else {
                ItemStack(Material.valueOf(itemName))
            }

            icon.amount = itemAmount

            icon.setDescription(listOf(
                    "${ChatColor.GRAY}Click to fill this Order and sell your items",
                    plugin.language.replacements("${ChatColor.GOLD}Price: %coins%", mapOf(Pair("{coins}", "$itemPrice"))),
                    "${ChatColor.GOLD}Buyer: $buyerUsername",
                    "${ChatColor.GOLD}Date Placed: ${dateFormat.format(date)}"
            ))

            return icon

        } else {
            return ItemStack(Material.AIR)
        }
    }

    override fun onClick(player: Player) {

        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable Async@{

            val orderIds = menu.orderIds[player] ?: mutableListOf()

            if (orderIds.size > slot) {
                val orderId = orderIds[slot]

                val itemName = plugin.database.getItemNameSync(orderId) ?: return@Async
                val itemAmount = plugin.database.getItemAmountSync(orderId) ?: return@Async
                val itemPrice = plugin.database.getItemPriceSync(orderId) ?: return@Async
                val buyerUUID = plugin.database.getBuyerSync(orderId) ?: return@Async

                if (plugin.database.getSellerSync(orderId) != null) {
                    return@Async
                }

                Bukkit.getScheduler().runTask(plugin, Runnable Sync@{
                    val item = if (itemName.toIntOrNull() != null) {
                        val chocItem = plugin.itemsConfig.getItem(itemName.toInt())
                        chocItem?.clone() ?: return@Sync
                    } else {
                        ItemStack(Material.valueOf(itemName))
                    }

                    item.amount = itemAmount

                    var amountRemaining = item.amount

                    val slotsToRemove = mutableListOf<Int>()

                    var sold = false

                    for (i in player.inventory.contents.indices) {
                        val pItem = player.inventory.getItem(i) ?: continue

                        var match = false

                        if (item.getId() != null && pItem.getId() != null && item.compare(pItem)) {
                            match = true
                        } else {
                            if (pItem.type == item.type) {
                                match = true
                            }
                        }

                        if (pItem.itemMeta is Damageable) {
                            if ((pItem.itemMeta as Damageable).hasDamage()) {
                                match = false
                            }
                        }

                        if (!match) {
                            continue
                        }

                        val pItemAmount = pItem.amount
                        if (amountRemaining > pItemAmount) {
                            slotsToRemove.add(i)
                            amountRemaining -= pItemAmount
                        } else {

                            sold = true
                            for (slot in slotsToRemove) {
                                player.inventory.clear(slot)
                            }
                            pItem.amount = pItemAmount - amountRemaining
                            Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
                                plugin.database.setSellerSync(orderId, player.uniqueId)
                            })
                            plugin.database.addCoins(player, itemPrice)
                            break
                        }
                    }

                    if (sold) {
                        player.sendMessage("${ChatColor.GREEN}Sale complete!")
                        player.closeInventory()
                        Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                            ViewOtherBuyOrdersMenu.menu.refresh()
                            ViewOtherBuyOrdersMenu.menu.openInventory(player)
                        }, 5L)
                        val buyer = Bukkit.getServer().getPlayer(buyerUUID)
                        if (buyer != null) {
                            buyer.sendMessage("${ChatColor.GREEN}${player.name} has filled one of your ${ChatColor.GOLD}Buy Orders!")
                            buyer.sendMessage("${ChatColor.GREEN}Go see a Clerk to collect your items!")
                        }
                    } else {
                        player.sendMessage("${ChatColor.RED}You do not have the items necessary to fill that Order!")
                    }
                })
            }
        })
    }
}

class ViewOtherBuyOrdersMenu : Menu(6) {

    val currentPages = mutableMapOf<Player, Int>()

    val orderIds = mutableMapOf<Player, List<ObjectId>>()

    override fun getTitle(player: Player): String {
        val page = currentPages[player] ?: 0
        return "Buy Orders : Page #${page + 1}"
    }

    override fun openInventory(player: Player) {
        val page = currentPages[player] ?: 0

        plugin.database.getUnfilledBuyOrdersOfOthersAsync(player.uniqueId, 45, page, null) {
            orderIds[player] = it
            Bukkit.getScheduler().runTask(plugin, Runnable {
                super.openInventory(player)
            })
        }
    }

    init {

        for (i in 0..44) {
            setItem(i, BuyOrderItem(this, i))
        }

        for (i in 45..53) {
            setItem(i, object : MenuItem {
                override fun getItemStack(player: Player): ItemStack {
                    val icon = ItemStack(Material.GRAY_STAINED_GLASS_PANE)
                    icon.setDisplayName(" ")
                    return icon
                }

                override fun onClick(player: Player) {}
            })
        }

        setItem(46, object : MenuItem {
            override fun getItemStack(player: Player): ItemStack {
                val icon = ItemStack(Material.ARROW)
                icon.setCustomModelData(2)
                icon.setDisplayName("${ChatColor.GOLD}View Previous Page")
                return icon
            }

            override fun onClick(player: Player) {
                val page = ((currentPages[player] ?: 1) - 1).coerceAtLeast(0)
                currentPages[player] = page
                openInventory(player)
            }
        })


        setItem(52, object : MenuItem {
            override fun getItemStack(player: Player): ItemStack {
                val icon = ItemStack(Material.ARROW)
                icon.setCustomModelData(1)
                icon.setDisplayName("${ChatColor.GOLD}View Next Page")
                return icon
            }

            override fun onClick(player: Player) {
                val page = (currentPages[player] ?: 0) + 1
                currentPages[player] = page
                openInventory(player)
            }
        })

    }

    companion object {
        val menu = ViewOtherBuyOrdersMenu()
    }

}