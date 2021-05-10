package io.github.jamessoda.mc.chocolate.utils

import io.github.jamessoda.mc.chocolate.Chocolate.Companion.plugin
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.util.Vector


object EffectUtils {

    fun playSound(sound: Sound, loc: Location, volume: Float, pitch: Float) {
        val world: World = loc.world ?: return
        world.playSound(loc, sound, volume, pitch)
    }

    fun playSound(sound: Sound, player: Player, volume: Float, pitch: Float) {
        player.playSound(player.location, sound, volume, pitch)
    }

    fun displayParticle(particle: Particle, loc: Location, count: Int, speed: Double = 1.0, offset: Vector = Vector(0.0, 0.0, 0.0)) {
        val world = loc.world ?: return
        world.spawnParticle(particle, loc, count, offset.x, offset.y, offset.z, speed)
    }

    fun displayDustParticle(loc: Location, count: Int, color: Color, particleSize: Float) {
        val world = loc.world ?: return
        val dustOptions = Particle.DustOptions(color, particleSize)
        world.spawnParticle(Particle.REDSTONE, loc, count, dustOptions)
    }

    fun displayDustParticle(player: Player, loc: Location, count: Int, color: Color, particleSize: Float) {
        val dustOptions = Particle.DustOptions(color, particleSize)
        player.spawnParticle(Particle.REDSTONE, loc, count, dustOptions)
    }

    fun createAnimation(color: Color, particleSize: Float, loc: Location, shape: List<Vector>, animDelay: Long) : Int {

        var count = 0

        val runnable = object: BukkitRunnable() {
            override fun run() {
                if(count >= shape.size) {
                    this.cancel()
                    return
                }

                val offset = shape[count]
                displayDustParticle(loc.clone().add(offset), 1, color, particleSize)

                count++
            }
        }

        return runnable.runTaskTimer(plugin, 0L, animDelay).taskId
    }

}