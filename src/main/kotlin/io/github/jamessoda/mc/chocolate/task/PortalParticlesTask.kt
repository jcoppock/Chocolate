package io.github.jamessoda.mc.chocolate.task

import io.github.jamessoda.mc.chocolate.Chocolate.Companion.plugin
import io.github.jamessoda.mc.chocolate.portals.Portal
import io.github.jamessoda.mc.chocolate.utils.EffectUtils
import io.github.jamessoda.mc.chocolate.utils.ShapeUtils
import org.bukkit.ChatColor
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.EntityType
import org.bukkit.metadata.FixedMetadataValue
import org.bukkit.scheduler.BukkitRunnable

class PortalParticlesTask(private val portal: Portal, private val location: Location) : BukkitRunnable() {

    private val height = 3.5
    private val heightChange = .025

    private val portalManager = plugin.portalManager

    private var marker : ArmorStand? = null

    var shouldCheck = false

    override fun run() {

        //Limit the check should cancel / marker updates to every other run
        if(shouldCheck) {
            if (!portalManager.getPortals().contains(portal) && !portalManager.getGuildPortals().values.contains(portal)) {
                marker?.remove()
                marker = null
                cancel()
                return
            } else {
                if (marker == null || marker!!.isDead) {
                    marker = location.world.spawnEntity(location.clone().add(0.0, 1.75, 0.0), EntityType.ARMOR_STAND) as ArmorStand

                    marker?.isMarker = true
                    marker?.isVisible = false
                    marker?.isCustomNameVisible = true
                    marker?.customName = "${ChatColor.DARK_PURPLE}${portal.name}"
                    marker?.setMetadata("portal-marker", FixedMetadataValue(plugin, portal.name))
                }
            }
        }

        if(location.getNearbyPlayers(32.0).isNotEmpty()) {

            EffectUtils.displayParticle(Particle.PORTAL, location, 1)
            EffectUtils.displayParticle(Particle.PORTAL, location, 1, 1.5)
            val top = location.clone().add(0.0, height, 0.0)
            EffectUtils.displayParticle(Particle.PORTAL, top, 1)
            EffectUtils.displayParticle(Particle.PORTAL, top, 1, 1.5)

            EffectUtils.createAnimation(Color.PURPLE, .8f, location,
                    ShapeUtils.getSpiralOffsets(1, 6, heightChange, height), 1L)
        }

        shouldCheck = !shouldCheck
    }

    override fun cancel() {
        marker?.remove()
        super.cancel()
    }
}