package io.github.jamessoda.mc.chocolate.utils.menu

import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

interface MenuItem {

    fun getItemStack(player: Player) : ItemStack

    fun onClick(player: Player)


}