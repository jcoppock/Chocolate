package io.github.jamessoda.mc.chocolate.magic.spells.combat.watersurge

import io.github.jamessoda.mc.chocolate.Chocolate.Companion.plugin
import io.github.jamessoda.mc.chocolate.magic.Spell
import io.github.jamessoda.mc.chocolate.utils.AbilityUtils
import io.github.jamessoda.mc.chocolate.utils.EffectUtils
import io.github.jamessoda.mc.chocolate.utils.ShapeUtils
import io.github.jamessoda.mc.chocolate.utils.extensions.isPVP
import io.github.jamessoda.mc.chocolate.utils.extensions.isSameGuild
import io.github.jamessoda.mc.chocolate.utils.extensions.knockback
import org.bukkit.*
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.entity.ThrownPotion
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.event.entity.ProjectileHitEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.PotionMeta
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import java.util.function.Consumer
import kotlin.math.floor
import kotlin.random.Random

abstract class WaterSurgeSpell(itemId: Int, tierSuffix: String, uses: Int, stats: Map<String, Pair<Double, Double>>)
    : Spell(itemId, "Water_Surge_$tierSuffix", uses, stats) {

    override fun use(player: Player, cooldownCallback: Consumer<Boolean>) {

        EffectUtils.playSound(Sound.ITEM_BOTTLE_FILL, player.location, 1f,
                Random.nextDouble(1.0, 2.0).toFloat())

        val potion = player.world.spawn(player.eyeLocation.clone(), ThrownPotion::class.java)
        potion.setMetadata(name, FixedMetadataValue(plugin, true))
        potion.shooter = player

        val potionItem = ItemStack(Material.SPLASH_POTION)
        val potionMeta = potionItem.itemMeta as PotionMeta
        potionMeta.color = Color.BLUE
        potionItem.itemMeta = potionMeta

        potion.item = potionItem

        potion.velocity = player.location.direction.normalize().multiply(1.25)

        object : BukkitRunnable() {
            override fun run() {
                if (plugin.isEnabled && !potion.isDead) {
                    EffectUtils.displayParticle(Particle.DRIP_WATER, potion.location,
                            10, 0.0, Vector(0.25, 0.25, 0.25))
                } else {
                    cancel()
                }
            }
        }.runTaskTimer(plugin, 0L, 1L)

        cooldownCallback.accept(true)
    }

    @EventHandler
    fun projectileLand(event: ProjectileHitEvent) {

        val potion = event.entity

        if (potion.hasMetadata(name)) {

            val shooter = potion.shooter as? Player ?: return

            val radius = getStat("radius", shooter) ?: return
            val damage = getStat("damage", shooter) ?: return
            val strength = getStat("strength", shooter) ?: return
            val fallDistance = getStat("fall-distance", shooter)?.toFloat() ?: return

            EffectUtils.playSound(Sound.ENTITY_GENERIC_SPLASH, potion.location,
                    2f, Random.nextDouble(1.0, 2.0).toFloat())
            EffectUtils.playSound(Sound.BLOCK_WATER_AMBIENT, potion.location,
                    2f, Random.nextDouble(1.0, 2.0).toFloat())

            for (vec in ShapeUtils.getSphereOffsets(floor(radius).toInt(), 9)) {
                EffectUtils.displayParticle(Particle.DRIP_WATER, potion.location.clone().add(vec),
                        1, 0.0, Vector(0.25, 0.5, 0.25))
                EffectUtils.displayParticle(Particle.CRIT_MAGIC, potion.location.clone().add(vec),
                        1, 0.25, Vector(0.25, 0.5, 0.25))
            }

            for (ent in AbilityUtils.getNearbyEntities(potion.location, radius)) {
                ent.knockback(potion.location.clone().subtract(0.0, -1.0, 0.0), strength)
                Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                    if (ent is Player && shooter.isSameGuild(ent)) {
                        ent.fallDistance = -fallDistance
                    } else {
                        ent.fallDistance = fallDistance
                    }

                    EffectUtils.displayParticle(Particle.WATER_DROP, potion.location, 100, .25)
                    EffectUtils.displayParticle(Particle.CRIT_MAGIC, ent.location, 100, .5)

                    if (ent is LivingEntity) {

                        val shouldDamage: Boolean
                        if (ent is Player) {

                            shouldDamage = shooter.isPVP(ent)

                        } else {
                            shouldDamage = true
                        }

                        if (shouldDamage) {
                            AbilityUtils.damageEntity(shooter, ent, damage)
                        }
                    }
                }, 1L)
            }
        }
    }

}