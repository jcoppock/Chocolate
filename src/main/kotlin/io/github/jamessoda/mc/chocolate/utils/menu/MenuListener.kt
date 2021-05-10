package io.github.jamessoda.mc.chocolate.utils.menu

import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryAction
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryCloseEvent

class MenuListener : Listener {

    @EventHandler
    fun onClick(event: InventoryClickEvent) {
        val player = event.view.player as Player

        val inventory = event.clickedInventory ?: return

        val menuHolder = inventory.holder

        if (menuHolder !is MenuHolder) {
            return
        }
        event.isCancelled = true

        val menu = menuHolder.menu
        val slot = event.rawSlot

        val menuItem = menu.getItem(slot) ?: return

        menuItem.onClick(player)
    }

    @EventHandler
    fun onInventoryClose(event: InventoryCloseEvent) {
        val player = event.player as Player

        val inventory = event.inventory

        val menuHolder = inventory.holder

        if (menuHolder !is MenuHolder) {
            return
        }

        val menu = menuHolder.menu

        menu.viewingPlayers.remove(player)
        menu.onClose(player, event.reason)
    }


}