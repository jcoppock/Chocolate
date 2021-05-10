package io.github.jamessoda.mc.chocolate.portals

import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.util.Vector


class Portal constructor(val location: Location, val name: String) {

    fun knockback(player: Player) {
        val direction: Vector = player.location.toVector().subtract(location.toVector()).normalize()

        player.velocity = direction.multiply(1.25)
    }

}