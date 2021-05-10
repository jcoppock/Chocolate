package io.github.jamessoda.mc.chocolate.utils.menu

import io.github.jamessoda.mc.chocolate.Chocolate.Companion.plugin
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent

abstract class Menu constructor(val rows: Int) {

    private val items = mutableMapOf<Int, MenuItem>()

    val viewingPlayers = mutableSetOf<Player>()

    fun getItem(slot: Int) : MenuItem? {
        return items[slot]
    }

    fun setItem(slot: Int, item: MenuItem) {
        items[slot] = item
    }

    fun getItems() : Map<Int, MenuItem> {
        return items
    }

    fun clearItems() {
        items.clear()
    }

    fun refresh() {
        for(p in viewingPlayers) {
            openInventory(p)
        }
    }

    abstract fun getTitle(player: Player) : String

    open fun openInventory(player: Player) {
        val inventory = MenuHolder(plugin, player, this).inventory

        player.openInventory(inventory)
        viewingPlayers.add(player)
    }

    open fun onClose(player: Player, reason: InventoryCloseEvent.Reason) {}
}