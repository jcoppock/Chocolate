package io.github.jamessoda.mc.chocolate.magic.spells.combat.fireball

import io.github.jamessoda.mc.chocolate.Chocolate.Companion.plugin
import io.github.jamessoda.mc.chocolate.magic.Spell
import io.github.jamessoda.mc.chocolate.utils.AbilityUtils
import io.github.jamessoda.mc.chocolate.utils.EffectUtils
import io.github.jamessoda.mc.chocolate.utils.ShapeUtils
import io.github.jamessoda.mc.chocolate.utils.extensions.isPVP
import org.bukkit.*
import org.bukkit.entity.Damageable
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import java.util.function.Consumer
import kotlin.random.Random

abstract class FireballSpell(itemId: Int, tierSuffix: String, uses: Int, stats: Map<String, Pair<Double, Double>>) :
        Spell(itemId, "Fireball_$tierSuffix", uses, stats) {

    // speed is in blocks per second
    private val speed : Double = 18.0
    // tickPeriod is in seconds
    private val tickPeriod : Double = .1

    private val timeLimit = 10.0

    final override fun use(player: Player, cooldownCallback: Consumer<Boolean>) {

        var loc = player.eyeLocation

        var iterations = 0

        EffectUtils.playSound(Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, loc, 1f,
                Random.nextDouble(.6, 1.6).toFloat())

        val runnable = object: BukkitRunnable() {
            override fun run() {
                loc.add(loc.direction.normalize().multiply(speed * tickPeriod))

                EffectUtils.displayParticle(Particle.FLAME, loc, 50, 0.0,
                        Vector(.25, .25, .25))


                if(iterations % 3 == 1) {
                    EffectUtils.playSound(Sound.BLOCK_FIRE_AMBIENT, loc, 2f, 1.4f)
                }

                val nearby = AbilityUtils.getNearbyEntities(loc, 1.0).filter { it != player }
                if(nearby.isNotEmpty() || (loc.block.type != Material.AIR &&
                        loc.block.type != Material.CAVE_AIR)) {

                    cancel()
                    explode(loc, player)

                } else {
                    iterations++

                    if(iterations >= timeLimit / tickPeriod) {
                        cancel()
                        explode(loc, player)
                    }
                }
            }
        }

        runnable.runTaskTimer(plugin, 0L, (tickPeriod * 20L).toLong())

        cooldownCallback.accept(true)
    }

    private fun explode(loc: Location, player: Player) {
        val radius = getStat("radius", player) ?: return
        val damage = getStat("damage", player) ?: return

        EffectUtils.playSound(Sound.ENTITY_GENERIC_EXPLODE, loc, 3f, 1.4f)

        val nearby = AbilityUtils.getNearbyEntities(loc, radius).filter { it != player }

        for(ent in nearby) {
            if(ent is LivingEntity) {

                if(ent is Player) {
                    if(!player.isPVP(ent)) {
                        continue
                    }
                }

                ent.fireTicks = 10
                AbilityUtils.damageEntity(player, ent, damage)

                for(vec in ShapeUtils.getSpiralOffsets(1, 8, .03, 2.0)) {
                    EffectUtils.displayParticle(Particle.FLAME, ent.location.clone().add(vec),
                            1, 0.0)
                }
            }
        }

        EffectUtils.displayParticle(Particle.FLAME, loc, 100, .25)

    }
}