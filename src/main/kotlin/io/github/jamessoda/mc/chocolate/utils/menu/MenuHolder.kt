package io.github.jamessoda.mc.chocolate.utils.menu

import io.github.jamessoda.mc.chocolate.Chocolate
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

class MenuHolder (val plugin: Chocolate, val player: Player, val menu: Menu) : InventoryHolder {

    override fun getInventory(): Inventory {

        var title = menu.getTitle(player)
        title = plugin.language.replaceColors(title)
        title = plugin.language.replacePlaceholders(player, title)

        val inv = Bukkit.createInventory(this, 9 * menu.rows, title)

        for(pair in menu.getItems()) {
            val slot = pair.key
            val item = pair.value

            inv.setItem(slot, item.getItemStack(player))
        }

        return inv
    }
}