package io.github.jamessoda.mc.chocolate.listeners

import io.github.jamessoda.mc.chocolate.Chocolate.Companion.plugin
import io.github.jamessoda.mc.chocolate.masteries.Mastery
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack

class CraftingListener : Listener {

    private val items = plugin.itemsConfig

    @EventHandler
    fun onCraftForm(event: PrepareItemCraftEvent) {

        val recipe = event.recipe ?: return
        val player = event.view.player as Player

        for(mastery in Mastery.masteries) {
            if(mastery.hasRecipe(recipe)) {

                if(!mastery.hasRecipeUnlocked(player, recipe)) {
                    event.inventory.result = ItemStack(Material.AIR)
                    player.sendMessage("${ChatColor.DARK_RED}You have not unlocked that Recipe!")
                    player.sendMessage("${ChatColor.DARK_RED}Type ${ChatColor.GOLD}/mastery ${mastery.name.toLowerCase()} " +
                            "${ChatColor.DARK_RED}to view ${mastery.name} Recipes")
                    return
                }
            }
        }

        for(spell in plugin.magicManager.getSpells()) {
            if(recipe.result.isSimilar(spell.getItemStack())) {
                if(!arcaneTableViews.containsValue(event.view)) {

                    event.inventory.result = ItemStack(Material.AIR)
                    player.sendMessage("${ChatColor.DARK_RED}You must craft that Recipe at an Arcane Table!")
                    return
                }
            }
        }
    }

    @EventHandler
    fun onCraftingClose(event: InventoryCloseEvent) {
        arcaneTableViews.remove(event.player as Player)
    }

    companion object {
        val arcaneTableViews = mutableMapOf<Player, InventoryView>()
    }

}