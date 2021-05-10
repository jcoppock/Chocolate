package io.github.jamessoda.mc.chocolate.utils.extensions

import org.bukkit.Location
import org.bukkit.entity.Entity
import org.bukkit.util.Vector

fun Entity.knockback(location: Location, strength: Double = 1.25) {
    val direction: Vector = this.location.clone().add(0.0, 0.5, 0.0).toVector()
            .subtract(location.toVector()).normalize()

    this.velocity = direction.multiply(strength)
}