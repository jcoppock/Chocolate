package io.github.jamessoda.mc.chocolate.npc.traits

import io.github.jamessoda.mc.chocolate.economy.bank.menu.BankMenu
import net.citizensnpcs.api.event.NPCClickEvent
import net.citizensnpcs.api.event.NPCRightClickEvent
import net.citizensnpcs.api.trait.Trait
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler

class BankerTrait : Trait("banker") {

    @EventHandler
    fun onClick(event: NPCRightClickEvent) {
        val player = event.clicker
        val npc = event.npc

        if (npc != this.npc) {
            return
        }

        BankMenu.instance.openInventory(player)
    }
}