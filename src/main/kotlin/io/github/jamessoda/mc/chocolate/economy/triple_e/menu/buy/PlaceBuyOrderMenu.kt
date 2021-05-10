package io.github.jamessoda.mc.chocolate.economy.triple_e.menu.buy

import io.github.jamessoda.mc.chocolate.Chocolate.Companion.plugin
import io.github.jamessoda.mc.chocolate.economy.triple_e.menu.PriceInputMenu
import io.github.jamessoda.mc.chocolate.economy.triple_e.menu.TripleEMainMenu
import io.github.jamessoda.mc.chocolate.utils.extensions.getDisplayName
import io.github.jamessoda.mc.chocolate.utils.extensions.setCustomModelData
import io.github.jamessoda.mc.chocolate.utils.extensions.setDescription
import io.github.jamessoda.mc.chocolate.utils.extensions.setDisplayName
import io.github.jamessoda.mc.chocolate.utils.menu.Menu
import io.github.jamessoda.mc.chocolate.utils.menu.MenuItem
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import kotlin.math.floor

class PlaceBuyOrderMenu : Menu(6) {

    override fun getTitle(player: Player): String {
        return "Place a Buy Order"
    }

    private val selectedItems = mutableMapOf<Player, ItemStack>()
    private val selectedAmounts = mutableMapOf<Player, Int>()
    private val inputPrices = mutableMapOf<Player, Int>()

    fun resetPlayerOrder(player: Player) {
        selectedItems.remove(player)
        selectedAmounts.remove(player)
        inputPrices.remove(player)
    }

    init {

        setItem(10, object : MenuItem {
            override fun getItemStack(player: Player): ItemStack {
                val item = selectedItems[player]?.clone() ?: ItemStack(Material.BARRIER)

                item.setDisplayName("${ChatColor.GOLD}Selected Item")
                item.setDescription(listOf("${ChatColor.GRAY}Click to select the item you wish to purchase."))

                return item
            }

            override fun onClick(player: Player) {
                BuyItemSelectionMenu.openSearch(player) {
                    selectedItems[player] = it
                    openInventory(player)
                }
            }
        })

        setItem(13, object : MenuItem {
            override fun getItemStack(player: Player): ItemStack {
                val item = selectedItems[player]?.clone() ?: return ItemStack(Material.AIR)

                val icon = selectedItems[player]?.clone() ?: ItemStack(Material.BARRIER)
                icon.setDisplayName("${ChatColor.GOLD}Amount")
                icon.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)

                val amount = selectedAmounts[player] ?: 1
                val maxStackSize = icon.maxStackSize

                if (maxStackSize == 1) {
                    return ItemStack(Material.AIR)
                }

                icon.amount = amount

                val description = mutableListOf("${ChatColor.GRAY}Click to select an amount you wish to purchase.", "")

                if (amount == 1) {
                    description.add("${ChatColor.DARK_AQUA}Amount = 1")
                } else {
                    description.add("${ChatColor.GRAY}1")
                }

                if (amount == (maxStackSize * .25).toInt()) {
                    description.add("${ChatColor.DARK_AQUA}Amount = ${(maxStackSize * .25).toInt()}")
                } else {
                    description.add("${ChatColor.GRAY}${(maxStackSize * .25).toInt()}")
                }

                if (amount == (maxStackSize * .5).toInt()) {
                    description.add("${ChatColor.DARK_AQUA}Amount = ${(maxStackSize * .5).toInt()}")
                } else {
                    description.add("${ChatColor.GRAY}${(maxStackSize * .5).toInt()}")
                }

                if (amount == (maxStackSize * .75).toInt()) {
                    description.add("${ChatColor.DARK_AQUA}Amount = ${(maxStackSize * .75).toInt()}")
                } else {
                    description.add("${ChatColor.GRAY}${(maxStackSize * .75).toInt()}")
                }

                if (amount == maxStackSize) {
                    description.add("${ChatColor.DARK_AQUA}Amount = $maxStackSize")
                } else {
                    description.add("${ChatColor.GRAY}$maxStackSize")
                }


                icon.setDescription(description)

                return icon
            }

            override fun onClick(player: Player) {
                val item = selectedItems[player]?.clone() ?: ItemStack(Material.BARRIER)

                val maxStackSize = item.maxStackSize

                if (maxStackSize == 1) {
                    return
                }
                var amount = selectedAmounts[player] ?: 1

                if (amount == 1) {
                    amount = (maxStackSize * .25).toInt()
                } else if (amount >= maxStackSize) {
                    amount = 1
                } else {
                    amount += (maxStackSize * .25).toInt()
                }



                selectedAmounts[player] = amount

                openInventory(player)
            }
        })

        setItem(16, object : MenuItem {
            override fun getItemStack(player: Player): ItemStack {
                val item = selectedItems[player]?.clone() ?: return ItemStack(Material.AIR)

                val icon = ItemStack(Material.GOLD_NUGGET)

                icon.setCustomModelData(1)
                icon.setDisplayName("${ChatColor.GOLD}Set the Price")

                val description = mutableListOf("${ChatColor.GRAY}Click to set the price of the purchase.")

                val price = inputPrices[player]

                if (price != null) {
                    description.add(plugin.language.replacements("${ChatColor.GRAY}Price: %coins%",
                            mapOf(Pair("{coins}", "$price"))))
                }

                icon.setDescription(description)

                return icon
            }

            override fun onClick(player: Player) {

                PriceInputMenu.open(player) {
                    inputPrices[player] = it
                    openInventory(player)
                }

            }
        })

        setItem(38, object : MenuItem {
            override fun getItemStack(player: Player): ItemStack {
                val item = selectedItems[player]?.clone() ?: return ItemStack(Material.AIR)

                val icon = ItemStack(Material.RED_DYE)
                icon.setCustomModelData(1)

                icon.setDisplayName("${ChatColor.GOLD}Cancel your Order")

                val description = mutableListOf("${ChatColor.GRAY}Click to cancel your Order")

                icon.setDescription(description)

                return icon
            }

            override fun onClick(player: Player) {

                inputPrices.remove(player)
                selectedItems.remove(player)
                selectedAmounts.remove(player)

                TripleEMainMenu.menu.openInventory(player)

            }
        })

        setItem(42, object : MenuItem {
            override fun getItemStack(player: Player): ItemStack {
                val price = inputPrices[player] ?: return ItemStack(Material.AIR)
                val item = selectedItems[player]?.clone() ?: return ItemStack(Material.AIR)
                val amount = selectedAmounts[player] ?: 1

                val icon = ItemStack(Material.LIME_DYE)
                icon.setCustomModelData(1)

                icon.setDisplayName("${ChatColor.GOLD}Place your Order")

                val description = mutableListOf("${ChatColor.GRAY}Click to place your Order")

                if (amount > 1) {
                    description.add(plugin.language.replacements("${ChatColor.GOLD}${item.getDisplayName()} " +
                            "${ChatColor.GRAY}x$amount for %coins%", mapOf(Pair("{coins}", "$price"))))
                } else {
                    description.add(plugin.language.replacements("${ChatColor.GOLD}${item.getDisplayName()} " +
                            "${ChatColor.GRAY}for %coins%", mapOf(Pair("{coins}", "$price"))))
                }
                val fee = floor(price * .02).toInt()

                description.add(plugin.language.replacements("${ChatColor.GRAY}Clerk Fee: %coins%", mapOf(Pair("{coins}", "$fee"))))

                icon.setDescription(description)

                return icon
            }

            override fun onClick(player: Player) {
                Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                    val price = inputPrices[player] ?: return@Runnable
                    val item = selectedItems[player] ?: return@Runnable
                    val amount = selectedAmounts[player]
                    val fee = floor(price * .02).toInt()

                    val coinPouch = plugin.database.getCoinPouch(player)

                    if (coinPouch >= (price + fee)) {
                        plugin.database.addCoins(player, -(price + fee))
                        plugin.database.createBuyOrder(player.uniqueId, item, amount ?: 1, price)
                        resetPlayerOrder(player)

                        ViewYourBuyOrdersMenu.menu.openInventory(player)
                    } else {
                        player.closeInventory()
                        player.sendMessage("${ChatColor.GOLD}You cannot afford to pay that much!")
                    }
                }, 5L)
            }
        })
    }

    companion object {
        val menu = PlaceBuyOrderMenu()
    }
}