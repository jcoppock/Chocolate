package io.github.jamessoda.mc.chocolate.portals

import io.github.jamessoda.mc.chocolate.Chocolate
import io.github.jamessoda.mc.chocolate.task.PortalParticlesTask
import io.github.jamessoda.mc.chocolate.utils.EffectUtils
import io.github.jamessoda.mc.chocolate.utils.WorldUtils
import org.bson.types.ObjectId
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Sound
import org.bukkit.entity.Player

class PortalManager constructor(val plugin: Chocolate){

    private val portals = mutableListOf<Portal>()
    private val guildPortals = mutableMapOf<ObjectId, Portal>()

    private val portalCooldowns = mutableSetOf<Player>()

    private val tasks = mutableMapOf<Portal, Int>()

    fun getPortalCooldowns() : Set<Player> {
        return portalCooldowns
    }

    fun getPortals() : List<Portal> {
        return portals
    }

    fun addPortal(portal: Portal) {
        portals.add(portal)

        tasks[portal] = PortalParticlesTask(portal, portal.location).runTaskTimer(plugin, 20L, 20L).taskId
    }

    fun getGuildPortals() : Map<ObjectId, Portal> {
        return guildPortals
    }

    fun addGuildPortal(guildId: ObjectId, portal: Portal) {
        guildPortals[guildId] = portal

        PortalParticlesTask(portal, portal.location).runTaskTimer(plugin, 20L, 20L)
    }

    fun removeGuildPortal(guildId: ObjectId) {

        val taskId = tasks[guildPortals[guildId]]

        if(taskId != null) {
            Bukkit.getScheduler().cancelTask(taskId)
            tasks.remove(guildPortals[guildId])
        }

        guildPortals.remove(guildId)
    }

    fun addCooldown(player: Player, delay: Long = 60L) {
        portalCooldowns.add(player)

        Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, Runnable {
            portalCooldowns.remove(player)
        }, delay)
    }

    fun teleport(player: Player, portal: Portal) {

        EffectUtils.playSound(Sound.ENTITY_ENDERMAN_TELEPORT, player.location, 1f, 1f)
        player.teleport(portal.location)
        EffectUtils.playSound(Sound.ENTITY_ENDERMAN_TELEPORT, player.location, 1f, 1f)
    }

    fun getClosestPortal(location: Location) : Portal? {
        var portal : Portal? = null
        var distance = Double.MAX_VALUE

        val portals = mutableListOf<Portal>()
        portals.addAll(this.portals)

        for(p in portals) {
            if(p.location.world == null || p.location.world != location.world) {
                continue
            }

            val dist = WorldUtils.squaredDistance(p.location, location)
            if(dist < distance) {
                distance = dist
                portal = p
            }
        }

        return portal
    }

    fun getClosestPortal(player: Player) : Portal? {
        return getClosestPortal(player.location)
    }
}