package io.github.jamessoda.mc.chocolate.npc.traits

import io.github.jamessoda.mc.chocolate.Chocolate.Companion.plugin
import net.citizensnpcs.api.event.NPCClickEvent
import net.citizensnpcs.api.event.NPCRightClickEvent
import net.citizensnpcs.api.trait.Trait
import org.bukkit.event.EventHandler

class InfoTrait : Trait("info") {


    @EventHandler
    fun onClick(event: NPCRightClickEvent) {
        val player = event.clicker
        val npc = event.npc

        if (npc != this.npc) {
            return
        }

        val npcName = npc.name.toLowerCase()

        if(player.world == plugin.chocolateConfig.spawn.world) {

            if (npcName.contains("town crier")) {
                plugin.language.sendMessage(player, "spawn_info.general")

            } else if (npcName.contains("guild master")) {
                plugin.language.sendMessage(player, "spawn_info.guilds")

            } else if (npcName.contains("mastery")) {
                plugin.language.sendMessage(player, "spawn_info.masteries")

            } else if (npcName.contains("wizard")) {
                plugin.language.sendMessage(player, "spawn_info.magic")
            }
        }
    }
}