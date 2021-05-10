package io.github.jamessoda.mc.chocolate.magic.spells.utility

import io.github.jamessoda.mc.chocolate.Chocolate.Companion.plugin
import io.github.jamessoda.mc.chocolate.magic.ChannelSpell
import io.github.jamessoda.mc.chocolate.portals.Portal
import io.github.jamessoda.mc.chocolate.portals.EnteredPortalMenu
import io.github.jamessoda.mc.chocolate.portals.TeleportPortalMenu
import io.github.jamessoda.mc.chocolate.utils.EffectUtils
import io.github.jamessoda.mc.chocolate.utils.ShapeUtils
import org.bukkit.Color
import org.bukkit.Sound
import org.bukkit.entity.Player
import java.util.*
import java.util.function.Consumer
import kotlin.random.Random

class TeleportSpell : ChannelSpell(201, "Teleport", 20, mapOf(
        Pair("cooldown", Pair(60.0, 30.0)),
        Pair("channel-time", Pair(6.0, 4.5)),
        Pair("channel-slow", Pair(3.0, 3.0)),
        Pair("item-damage-chance", Pair(1.0, .8))
), true) {


    override fun use(player: Player, cooldownCallback: Consumer<Boolean>) {
        TeleportPortalMenu.instance.openInventory(player)

        portalMenuConsumers[player.uniqueId] = Consumer {
            teleportingPlayers[player.uniqueId] = it
            super.use(player, cooldownCallback)
        }
    }

    override fun tickEffect(player: Player, tick: Int) {

        if(tick % 8 == 1) {
            val tickMax = (getStat("channel-time", player) ?: 0.0) * 20

            val loc = player.eyeLocation.add(player.eyeLocation.direction)

            EffectUtils.playSound(Sound.ENTITY_ENDERMAN_TELEPORT, loc, 2f, (2f - (tick / tickMax)).toFloat())
            EffectUtils.displayDustParticle(loc, tick / 2, Color.PURPLE, (tick / tickMax).toFloat() * 5f)
        }

        val offsets = ShapeUtils.getHollowSphereOffsets(1, 30)
            EffectUtils.displayDustParticle(player.eyeLocation.clone().add(offsets[Random.nextInt(offsets.size)]),
                    1, Color.PURPLE, 2f)
    }

    override fun finishChannel(player: Player, cooldownCallback: Consumer<Boolean>) {
        val portal = teleportingPlayers[player.uniqueId] ?: return

        plugin.portalManager.teleport(player, portal)

        portalMenuConsumers.remove(player.uniqueId)
        teleportingPlayers.remove(player.uniqueId)

        cooldownCallback.accept(true)
    }

    companion object {
        val portalMenuConsumers = mutableMapOf<UUID, Consumer<Portal>>()
        val teleportingPlayers = mutableMapOf<UUID, Portal>()
    }
}