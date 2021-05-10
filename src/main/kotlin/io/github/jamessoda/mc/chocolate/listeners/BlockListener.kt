package io.github.jamessoda.mc.chocolate.listeners

import io.github.jamessoda.mc.chocolate.utils.extensions.getId
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryPickupItemEvent
import org.bukkit.event.inventory.InventoryType

class BlockListener : Listener {

    @EventHandler
    fun onHopperPickupGold(event: InventoryPickupItemEvent) {

        if(event.inventory.type == InventoryType.PLAYER) {
            return
        }

        val item = event.item.itemStack

        // Check if it's a gold coin
        if(item.getId() == 5) {
            event.isCancelled = true
        }
    }

}