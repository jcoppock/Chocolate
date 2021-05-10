package io.github.jamessoda.mc.chocolate.utils

import net.minecraft.server.v1_16_R3.DamageSource
import org.bukkit.Location
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftLivingEntity
import org.bukkit.craftbukkit.v1_16_R3.entity.CraftPlayer
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityDamageEvent
import sun.audio.AudioPlayer.player


object AbilityUtils {

    fun getNearbyEntities(location: Location, radius: Double,
                          vararg ignored: EntityType =
                                  arrayOf(EntityType.ARMOR_STAND, EntityType.ITEM_FRAME)) : List<Entity> {

        val entities = location.getNearbyEntities(radius + 1, radius + 1, radius + 1)

        return entities.filter { !ignored.contains(it.type) }
                .filter { it.location.distance(location) <= radius
                        || it.location.add(0.0, 1.0, 0.0).distance(location) <= radius }
    }

    fun damageEntity(damager: LivingEntity, entity: LivingEntity, damage: Double) {
        (entity as CraftLivingEntity).handle.damageEntity(DamageSource.MAGIC, damage.toFloat())
        entity.lastDamageCause = EntityDamageByEntityEvent(damager, entity, EntityDamageEvent.DamageCause.MAGIC, damage)

    }


}