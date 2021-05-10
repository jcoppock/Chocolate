package io.github.jamessoda.mc.chocolate.utils.menu

import org.bukkit.entity.Player

abstract class OpenMenuItem(private val menu: Menu) : MenuItem {

    override fun onClick(player: Player) {
        menu.openInventory(player)
    }
}