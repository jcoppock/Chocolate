package io.github.jamessoda.mc.chocolate.portals

import io.github.jamessoda.mc.chocolate.Chocolate.Companion.plugin
import io.github.jamessoda.mc.chocolate.magic.spells.utility.TeleportSpell
import io.github.jamessoda.mc.chocolate.utils.menu.MenuHolder
import io.github.jamessoda.mc.chocolate.utils.WorldUtils
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerMoveEvent

class PortalListener : Listener {

    private val portalManager = plugin.portalManager

    @EventHandler
    fun onPlayerMove(event: PlayerMoveEvent) {

        val player = event.player

        val to = event.to

        val portals = mutableListOf<Portal>()

        portals.addAll(portalManager.getPortals())
        portals.addAll(portalManager.getGuildPortals().values)

        for (portal in portals) {
            if(to.world != portal.location.world) {
                continue
            }
            val dist = WorldUtils.squaredDistance(portal.location, to)
            if(dist <= 1.5 && WorldUtils.squaredDistance(portal.location, event.from) > dist) {

                if(portalManager.getPortalCooldowns().contains(event.player)) {
                    portal.knockback(player)
                    player.closeInventory()
                    return
                } else {

                    val holder = player.openInventory.topInventory.holder
                    if(holder is MenuHolder && holder.menu is EnteredPortalMenu) {
                        return
                    }

                    TeleportSpell.portalMenuConsumers.remove(player.uniqueId)
                    TeleportSpell.teleportingPlayers.remove(player.uniqueId)
                    EnteredPortalMenu.instance.openInventory(event.player)
                    return
                }
            }
        }
    }
}