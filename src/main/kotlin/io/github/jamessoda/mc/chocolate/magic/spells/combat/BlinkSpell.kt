package io.github.jamessoda.mc.chocolate.magic.spells.combat

import io.github.jamessoda.mc.chocolate.utils.extensions.getTargetBlock
import io.github.jamessoda.mc.chocolate.magic.Spell
import io.github.jamessoda.mc.chocolate.utils.EffectUtils
import io.github.jamessoda.mc.chocolate.utils.ShapeUtils
import org.bukkit.*
import org.bukkit.entity.Player
import java.util.function.Consumer

class BlinkSpell : Spell(101, "Blink", 30, mapOf(
        Pair("cooldown", Pair(10.0, 8.0)),
        Pair("item-damage-chance", Pair(1.0, 0.5)),
        Pair("range", Pair(14.0, 14.0))
), true) {

    private val circleOffsets = ShapeUtils.getCircleOffsets(1, 6)

    override fun use(player: Player, cooldownCallback: Consumer<Boolean>) {

        val block = player.getTargetBlock(getStat("range", player) ?: 14.0, true)

        if(block == null) {
            cooldownCallback.accept(false)
            return
        }

        var loc = player.location.clone()
        EffectUtils.displayParticle(Particle.PORTAL, loc, 2)
        EffectUtils.displayParticle(Particle.PORTAL, loc, 1, 1.5)

        for (offset in circleOffsets) {
            val circLoc = loc.clone().add(offset)
            EffectUtils.displayDustParticle(circLoc, 1, Color.PURPLE, .8f)
        }

        EffectUtils.playSound(Sound.ENTITY_ENDERMAN_TELEPORT, player.location, 1f, 1f)

        val direction = player.location.direction

        val newLocation = block.location
        newLocation.direction = direction
        player.teleport(newLocation)

        loc = player.location.clone()
        EffectUtils.displayParticle(Particle.PORTAL, loc, 2)
        EffectUtils.displayParticle(Particle.PORTAL, loc, 1, 1.5)

        for (offset in circleOffsets) {
            val circLoc = loc.clone().add(offset)
            EffectUtils.displayDustParticle(circLoc, 1, Color.PURPLE, .8f)
        }
        EffectUtils.playSound(Sound.ENTITY_ENDERMAN_TELEPORT, player.location, 1f, 1f)

        cooldownCallback.accept(true)
    }
}