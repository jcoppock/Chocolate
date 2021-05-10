package io.github.jamessoda.mc.chocolate.magic.spells.combat.iceblast

import io.github.jamessoda.mc.chocolate.Chocolate
import io.github.jamessoda.mc.chocolate.magic.ChannelSpell
import io.github.jamessoda.mc.chocolate.magic.Spell
import io.github.jamessoda.mc.chocolate.utils.AbilityUtils
import io.github.jamessoda.mc.chocolate.utils.EffectUtils
import io.github.jamessoda.mc.chocolate.utils.ShapeUtils
import io.github.jamessoda.mc.chocolate.utils.extensions.isPVP
import org.bukkit.*
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.entity.EntityDamageEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector
import java.util.function.Consumer
import kotlin.math.roundToInt
import kotlin.random.Random

abstract class IceBlastSpell(itemId: Int, tierSuffix: String, uses: Int, stats: Map<String, Pair<Double, Double>>) :
        ChannelSpell(itemId, "Ice_Blast_$tierSuffix", uses, stats) {

    override fun tickEffect(player: Player, tick: Int) {

        val tickMax = (getStat("channel-time", player) ?: 0.0) * 20

        val loc = player.eyeLocation.add(player.eyeLocation.direction.multiply(1.5))

        EffectUtils.playSound(Sound.BLOCK_SNOW_STEP, loc, 2f, (2f - (tick / tickMax)).toFloat())
        EffectUtils.displayParticle(Particle.SNOWBALL, loc, (tick / tickMax).toInt() * 10, 0.0, Vector(0.1, 0.1, 0.1))

    }

    // speed is in blocks per second
    private val speed : Double = 20.0
    // tickPeriod is in seconds
    private val tickPeriod : Float = 0.05f

    private val timeLimit = 10.0

    override fun finishChannel(player: Player, cooldownCallback: Consumer<Boolean>) {

        var loc = player.eyeLocation.add(player.eyeLocation.direction.multiply(1.5))

        var iterations = 0

        EffectUtils.playSound(Sound.ENTITY_FIREWORK_ROCKET_LAUNCH, loc, 1f,
                Random.nextDouble(.6, 1.6).toFloat())

        EffectUtils.playSound(Sound.BLOCK_GLASS_BREAK, loc, 1f,
                Random.nextDouble(.6, 1.6).toFloat())

        val runnable = object: BukkitRunnable() {
            override fun run() {
                loc.add(loc.direction.normalize().multiply(speed * tickPeriod))

                EffectUtils.displayParticle(Particle.SNOWBALL, loc, 50, 0.0,
                        Vector(.25, .25, .25))


                if(iterations % 3 == 1) {
                    EffectUtils.playSound(Sound.BLOCK_SNOW_STEP, loc, 2f, 1f)
                }

                val nearby = AbilityUtils.getNearbyEntities(loc, 1.0).filter { it != player }
                if(nearby.isNotEmpty()) {

                    cancel()

                    val damage = getStat("damage", player) ?: return
                    val duration = getStat("duration", player) ?: return
                    val strength =  getStat("strength", player) ?: return

                    EffectUtils.playSound(Sound.BLOCK_GLASS_BREAK, loc, 1f,
                            Random.nextDouble(.6, 1.6).toFloat())
                    EffectUtils.playSound(Sound.ENTITY_GENERIC_EXPLODE, loc, 3f, 2.0f)

                    for(ent in nearby) {
                        if(ent is LivingEntity) {

                            if(ent is Player) {
                                if(!player.isPVP(ent)) {
                                    continue
                                }
                            }

                            AbilityUtils.damageEntity(player, ent, damage)

                            for(vec in ShapeUtils.getSpiralOffsets(1, 8, .03, 2.0)) {
                                EffectUtils.displayParticle(Particle.SNOWBALL, ent.location.clone().add(vec),
                                        1, 0.0)
                            }
                            ent.addPotionEffect(PotionEffect(PotionEffectType.SLOW, (duration * 20).toInt(),
                                    strength.roundToInt(), false, true, true))
                        }
                    }

                    EffectUtils.displayDustParticle(loc, 5, Color.WHITE, 1f)


                } else if((loc.block.type != Material.AIR &&
                                loc.block.type != Material.CAVE_AIR && Spell.spellTravelThroughBlocks.contains(loc.block.type))) {

                    cancel()

                } else {
                    iterations++

                    if(iterations >= timeLimit / tickPeriod) {
                        cancel()
                    }
                }
            }
        }

        runnable.runTaskTimer(Chocolate.plugin, 0L, (tickPeriod * 20L).toLong())

        cooldownCallback.accept(true)
    }
}