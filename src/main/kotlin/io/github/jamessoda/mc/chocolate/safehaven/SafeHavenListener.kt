package io.github.jamessoda.mc.chocolate.safehaven

import io.github.jamessoda.mc.chocolate.Chocolate
import io.github.jamessoda.mc.chocolate.utils.extensions.isChocolatePlayer
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Monster
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.*
import org.bukkit.event.entity.*
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerMoveEvent
import org.bukkit.event.world.PortalCreateEvent

class SafeHavenListener(plugin: Chocolate) : Listener {

    private val safeHavenChunkManager = plugin.safeHavenManager

    @EventHandler(priority = EventPriority.LOWEST)
    fun onBlockBreak(event: LeavesDecayEvent) {
        if (safeHavenChunkManager.getSafeHaven(event.block.chunk) != null) {
            event.isCancelled = true
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onBlockBreak(event: BlockBreakEvent) {
        if (safeHavenChunkManager.getSafeHaven(event.block.chunk) != null) {
            if (event.player.gameMode != GameMode.CREATIVE) {
                event.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onBlockPhysics(event: EntityChangeBlockEvent) {
        if (safeHavenChunkManager.getSafeHaven(event.block.chunk) != null) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onPortalCreate(event: PortalCreateEvent) {
        for (block in event.blocks) {
            if (safeHavenChunkManager.getSafeHaven(block.block.chunk) != null) {
                event.isCancelled = true
                return
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onBlockInteract(event: PlayerInteractEvent) {

        if (safeHavenChunkManager.isInSafeHaven(event.player) ||
                (event.hasBlock() && safeHavenChunkManager.getSafeHaven(event.clickedBlock!!.chunk) != null)) {
            if (event.player.gameMode != GameMode.CREATIVE) {
                event.isCancelled = true

                if (event.action == Action.RIGHT_CLICK_AIR && event.hasItem() && event.item!!.type.isEdible) {
                    event.isCancelled = false
                }

                if (event.hasBlock() && event.clickedBlock?.type == Material.ENDER_CHEST) {
                    if (!event.player.isChocolatePlayer()) {

                        Chocolate.plugin.language.sendMessage(event.player, "misc.not_verified")
                        return
                    }

                    event.isCancelled = false
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.LOWEST)
    fun onNotEnemyTakeDamage(event: EntityDamageEvent) {
        val entity = event.entity

        if (entity is Monster) {
            return
        }

        if (safeHavenChunkManager.isInSafeHaven(entity)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onMobSpawn(event: EntitySpawnEvent) {
        if (event.entity is LivingEntity) {
            if (safeHavenChunkManager.isInSafeHaven(event.entity)) {
                event.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onExplosionPrime(event: ExplosionPrimeEvent) {
        if (safeHavenChunkManager.isInSafeHaven(event.entity)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onExplode(event: EntityExplodeEvent) {
        if (safeHavenChunkManager.isInSafeHaven(event.entity)) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onIgnite(event: BlockIgniteEvent) {
        if (safeHavenChunkManager.getSafeHaven(event.block.chunk) != null) {
            event.isCancelled = true
        }
    }
}