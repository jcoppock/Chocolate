package io.github.jamessoda.mc.chocolate.economy.triple_e.menu.buy

import io.github.jamessoda.mc.chocolate.Chocolate.Companion.plugin
import io.github.jamessoda.mc.chocolate.utils.extensions.getDisplayName
import io.github.jamessoda.mc.chocolate.utils.extensions.setDescription
import io.github.jamessoda.mc.chocolate.utils.extensions.setDisplayName
import io.github.jamessoda.mc.chocolate.utils.menu.Menu
import io.github.jamessoda.mc.chocolate.utils.menu.MenuItem
import net.wesjd.anvilgui.AnvilGUI
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.function.Consumer

class BuyItemSelectionMenu : Menu(6) {

    override fun getTitle(player: Player): String {
        return "Please Select An Item"
    }

    val playerSearchResults = mutableMapOf<Player, List<ItemStack>>()
    val playerCallbacks = mutableMapOf<Player, Consumer<ItemStack>>()

    init {

        for (i in 0..44) {
            setItem(i, object : MenuItem {
                override fun getItemStack(player: Player): ItemStack {
                    val searchResults = playerSearchResults[player] ?: return ItemStack(Material.AIR)

                    return if (searchResults.size > i) {
                        searchResults[i]
                    } else {
                        ItemStack(Material.AIR)
                    }
                }

                override fun onClick(player: Player) {
                    val callback = playerCallbacks[player] ?: return
                    val item = getItemStack(player)

                    if(item.type == Material.AIR || item.type == Material.CAVE_AIR) {
                        return
                    }
                    player.closeInventory()
                    callback.accept(getItemStack(player))
                }
            })
        }


        val filler = object : MenuItem {
            override fun getItemStack(player: Player): ItemStack {
                val item = ItemStack(Material.GRAY_STAINED_GLASS_PANE)
                item.setDisplayName("")
                return item
            }

            override fun onClick(player: Player) {}
        }

        for (i in 45..53) {
            setItem(i, filler)
        }

        setItem(49, object : MenuItem {
            override fun getItemStack(player: Player): ItemStack {
                val item = ItemStack(Material.NAME_TAG)
                item.setDisplayName("${ChatColor.GOLD}Click to Search")
                item.setDescription(listOf("${ChatColor.GRAY}Click to Search for a new Item"))
                return item
            }

            override fun onClick(player: Player) {
                val callback = playerCallbacks[player] ?: return
                openSearch(player, callback)
            }
        })

    }

    fun open(player: Player, search: String = "", callback: Consumer<ItemStack>) {
        var searchResults = mutableListOf<ItemStack>()

        for (mat in Material.values()) {
            var blacklisted = false
            for(black in plugin.chocolateConfig.buyOrderBlacklist) {
                if(black == mat.name || (black.contains("*") && mat.name.contains(black.replace("*", "")))) {
                    blacklisted = true
                    break
                }
            }
            if(blacklisted) {
                continue
            }
            if (search == "" || mat.name.toLowerCase().replace("_", " ").contains(search.toLowerCase())) {
                searchResults.add(ItemStack(mat))
            }
        }

        for (id in plugin.itemsConfig.getItemIds()) {
            if (plugin.chocolateConfig.buyOrderBlacklist.contains("$id")) {
                continue
            }
            val item = plugin.itemsConfig.getItem(id) ?: continue
            val itemName = ChatColor.stripColor(item.getDisplayName()) ?: continue
            if (search == "" || itemName.toLowerCase().replace("_", " ").contains(search.toLowerCase())) {
                searchResults.add(item)
            }
        }

        searchResults.sortWith { o1, o2 -> o1.getDisplayName().compareTo(o2.getDisplayName()) }

        playerCallbacks[player] = callback
        playerSearchResults[player] = searchResults
        openInventory(player)
    }

    companion object {

        val menu = BuyItemSelectionMenu()

        fun openSearch(player: Player, callback: Consumer<ItemStack>) {
            AnvilGUI.Builder()
                    .plugin(plugin)
                    .title("Search for an Item")
                    .text("")
                    .onComplete { p, input ->
                        menu.open(p, input, callback)
                        return@onComplete AnvilGUI.Response.close()
                    }
                    .open(player)
        }
    }
}