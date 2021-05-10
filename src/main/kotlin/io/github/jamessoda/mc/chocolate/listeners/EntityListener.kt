package io.github.jamessoda.mc.chocolate.listeners

import io.github.jamessoda.mc.chocolate.Chocolate.Companion.plugin
import io.github.jamessoda.mc.chocolate.utils.extensions.getId
import org.bukkit.Bukkit
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.event.entity.EntityDeathEvent
import org.bukkit.event.entity.EntitySpawnEvent
import org.bukkit.event.entity.ItemMergeEvent
import java.util.*

class EntityListener : Listener {

    private val spawnReasons = mutableMapOf<UUID, CreatureSpawnEvent.SpawnReason>()

    init {
        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, Runnable {
            val iterator = spawnReasons.iterator()

            while(iterator.hasNext()) {
                val pair = iterator.next()
                val uuid = pair.key

                val entity = Bukkit.getEntity(uuid)

                if(entity == null || entity.isDead) {
                    iterator.remove()
                }
            }
        }, 10 * 20L, 10 * 20L)
    }

    @EventHandler
    fun onItemMerge(event: ItemMergeEvent) {
        val item = event.entity.itemStack
        val target = event.target.itemStack

        if(item.getId() == 5 || target.getId() == 5) {
            event.isCancelled = true
        }
    }

    @EventHandler
    fun onMobSpawn(event: CreatureSpawnEvent) {
        if(plugin.itemsConfig.entityDrops.keys.contains(event.entityType)) {
            spawnReasons[event.entity.uniqueId] = event.spawnReason
        }
    }

    @EventHandler
    fun onMobDeath(event: EntityDeathEvent) {

        val entity = event.entity
        val entityType = event.entityType

        if(entity.killer != null) {

            val drops = plugin.itemsConfig.getDrops(entityType, spawnReasons[entity.uniqueId])

            if(spawnReasons[entity.uniqueId] == CreatureSpawnEvent.SpawnReason.SPAWNER) {
                event.droppedExp /= 2
            }

            event.drops.addAll(drops)
        }
    }
}