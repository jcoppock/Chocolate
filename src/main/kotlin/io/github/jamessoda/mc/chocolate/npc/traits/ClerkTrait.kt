package io.github.jamessoda.mc.chocolate.npc.traits

import io.github.jamessoda.mc.chocolate.economy.triple_e.menu.TripleEMainMenu
import net.citizensnpcs.api.event.NPCRightClickEvent
import net.citizensnpcs.api.trait.Trait
import org.bukkit.event.EventHandler

class ClerkTrait : Trait("clerk") {


    @EventHandler
    fun onClick(event: NPCRightClickEvent) {
        val player = event.clicker
        val npc = event.npc

        if (npc != this.npc) {
            return
        }


        TripleEMainMenu.menu.openInventory(player)

    }
}