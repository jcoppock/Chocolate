package io.github.jamessoda.mc.chocolate.magic.spells.combat

import io.github.jamessoda.mc.chocolate.magic.ChannelSpell
import io.github.jamessoda.mc.chocolate.utils.AbilityUtils
import io.github.jamessoda.mc.chocolate.utils.EffectUtils
import io.github.jamessoda.mc.chocolate.utils.ShapeUtils
import org.bukkit.Color
import org.bukkit.Sound
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import java.util.function.Consumer
import kotlin.math.roundToInt
import kotlin.random.Random

/*
class RadianceSpell : ChannelSpell(103, "Radiance", 50, mapOf(
        Pair("cooldown", Pair(14.0, 13.0)),
        Pair("channel-time", Pair(4.0, 2.0)),
        Pair("item-damage-chance", Pair(1.0, .8)),
        Pair("radius", Pair(6.0, 7.0)),
        Pair("duration", Pair(6.0, 6.5)),
        Pair("strength", Pair(1.0, 2.0))
)) {

    override fun tickEffect(player: Player, tick: Int) {

        if(tick % 4 == 1) {

            val offsets = ShapeUtils.getHollowSphereOffsets(1, 20)
            for (i in 0 until 10) {
                EffectUtils.displayDustParticle(player.location.clone().add(offsets[Random.nextInt(offsets.size)]),
                        1, Color.fromRGB(161, 68, 133), 2f)
            }

            EffectUtils.playSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, player.location,
                    1f, 1.6f)
        }
    }

    override fun finishChannel(player: Player, cooldownCallback: Consumer<Boolean>) {

        val radius = getStat("radius", player) ?: 0.0
        val duration = getStat("duration", player) ?: 0.0
        val strength = getStat("strength", player)?.roundToInt() ?: 0

        for(ent in AbilityUtils.getNearbyEntities(player.location, radius)) {
            if(ent.type == EntityType.PLAYER) {

                if(player.isSameGuild(ent)) {
                EffectUtils.playSound(Sound.ENTITY_EXPERIENCE_ORB_PICKUP, ent.location,
                        1f, 1.6f)
                (ent as Player).addPotionEffect(PotionEffect(PotionEffectType.REGENERATION,
                        (duration * 20).toInt(), strength, false, true, true))
                }
            }
        }

        for(vec in ShapeUtils.getSpiralOffsets(radius.roundToInt(), 2, .0025, radius)) {
            EffectUtils.displayDustParticle(player.location.clone().add(vec), 1,
                    Color.fromRGB(161, 68, 133), 1f)
        }

        cooldownCallback.accept(true)
    }
}

 */