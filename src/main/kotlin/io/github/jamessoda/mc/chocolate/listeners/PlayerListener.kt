package io.github.jamessoda.mc.chocolate.listeners

import io.github.jamessoda.mc.chocolate.Chocolate.Companion.plugin
import io.github.jamessoda.mc.chocolate.magic.ChannelSpell
import io.github.jamessoda.mc.chocolate.utils.BossBarUtils
import io.github.jamessoda.mc.chocolate.utils.EffectUtils
import io.github.jamessoda.mc.chocolate.utils.ItemUtils
import io.github.jamessoda.mc.chocolate.utils.extensions.getCurrentTotalExp
import io.github.jamessoda.mc.chocolate.utils.extensions.getId
import io.github.jamessoda.mc.chocolate.utils.extensions.isChocolatePlayer
import io.github.jamessoda.mc.chocolate.utils.extensions.isPVP
import org.bukkit.*
import org.bukkit.block.Skull
import org.bukkit.block.data.Ageable
import org.bukkit.entity.EnderPearl
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.*
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerRespawnEvent
import org.bukkit.persistence.PersistentDataType
import kotlin.math.floor

class PlayerListener() : Listener {

    private val portalManager = plugin.portalManager

    private val giveExpOnBreakMaterials = mutableListOf(
            Material.WHEAT,
            Material.BEETROOTS,
            Material.CARROT,
            Material.POTATOES,
            Material.NETHER_WART
    )

    @EventHandler
    fun onEnderPearl(event: ProjectileLaunchEvent) {
        if (event.entity.type == EntityType.ENDER_PEARL) {
            val player = event.entity.shooter as? Player ?: return
            val pearl = event.entity as EnderPearl

            if (plugin.chocolateConfig.spawn.world == player.world) {
                event.isCancelled = true
            }
        }
    }

    @EventHandler
    fun onPlayerFatalDamage(event: EntityDamageEvent) {
        val player = event.entity as? Player ?: return

        if(event.finalDamage >= player.health) {
            BossBarUtils.removeBossBars(player)
        }
    }

    @EventHandler
    fun onPlayerDeath(event: PlayerDeathEvent) {
        val player = event.entity
        val exp = player.getCurrentTotalExp()

        BossBarUtils.removeBossBars(player)

        ChannelSpell.clear(player)
        BossBarUtils.removeBossBars(player)

        val coinPouch = plugin.database.getCoinPouch(player.uniqueId)
        plugin.database.setCoinPouch(player.uniqueId, coinPouch / 2)

        event.keepLevel = false
        event.newExp = floor(exp * .05).toInt()
        event.setShouldDropExperience(true)

        val killer = player.killer
        if (killer != null && player.isPVP(killer)) {
            event.keepInventory = false
            event.droppedExp = (exp * .1).toInt().coerceAtMost(216).coerceAtLeast(16)

        } else {
            event.keepInventory = true
            event.droppedExp = (exp * .1).toInt().coerceAtMost(216)
            event.drops.clear()
        }

        val droppedCoins = (coinPouch / 4)

        val coin = plugin.itemsConfig.getItem(5)

        if (coin != null) {
            val drop = coin.clone()
            if (drop.type != Material.AIR) {
                drop.amount = 1
                ItemUtils.setData(drop, "coin-amount", "$droppedCoins")
                event.drops.add(drop)
            }
        }
    }

    @EventHandler
    fun onPlayerRespawn(event: PlayerRespawnEvent) {
        val player = event.player

        if (!event.isBedSpawn && !event.isAnchorSpawn) {
            val spawnPortal = portalManager.getClosestPortal(player)

            val spawnLocation = spawnPortal?.location

            if(spawnLocation == null) {
                val guildId = plugin.database.getGuildIdOfPlayer(player.uniqueId)
                if(guildId != null) {
                    val guildSpawn = plugin.database.getGuildPortalLocation(guildId)
                    if(guildSpawn != null) {
                        event.respawnLocation = guildSpawn
                    } else {
                        event.respawnLocation = Bukkit.getWorlds()[0].spawnLocation
                    }
                }
            } else {
                event.respawnLocation = spawnLocation
            }
        }
    }

    private val skullItemIdKey = NamespacedKey(plugin, "skull-item-id")

    @EventHandler
    fun onPlayerBreakBlock(event: BlockBreakEvent) {

        if (event.player.gameMode == GameMode.CREATIVE) {
            return
        }

        if (!event.player.isChocolatePlayer()) {
            event.isCancelled = true
            return
        }

        val block = event.block

        if (block.state is Skull && !event.isCancelled) {


            val state = block.state as Skull
            val itemId = state.persistentDataContainer.get(skullItemIdKey,
                    PersistentDataType.INTEGER) ?: return

            val item = plugin.itemsConfig.getItem(itemId) ?: return

            block.drops.clear()
            event.isDropItems = false

            block.world.dropItemNaturally(block.location, item)
        }

        if(giveExpOnBreakMaterials.contains(block.type)) {
            val state = block.state
            if(state is Ageable) {

                if(state.age == state.maximumAge) {
                    event.expToDrop += 1
                }
            }
        }
    }

    @EventHandler
    fun onClickArcaneTable(event: PlayerInteractEvent) {

        if (event.action == Action.RIGHT_CLICK_BLOCK) {
            val block = event.clickedBlock ?: return

            if (block.state is Skull) {

                val state = block.state as Skull
                val itemId = state.persistentDataContainer.get(skullItemIdKey,
                        PersistentDataType.INTEGER) ?: return

                if (itemId == 400) {

                    if (!event.player.isChocolatePlayer()) {
                        event.isCancelled = true
                        plugin.language.sendMessage(event.player, "misc.not_verified")
                        return
                    }

                    val view = event.player.openWorkbench(null, true) ?: return
                    CraftingListener.arcaneTableViews[event.player] = view
                }
            }
        }
    }

    @EventHandler
    fun onPlayerPlaceBlock(event: BlockPlaceEvent) {
        if (!event.player.isChocolatePlayer()) {
            event.isCancelled = true
            return
        }

        val item = event.itemInHand

        if (item.type == Material.PLAYER_HEAD && !event.isCancelled) {
            val itemId = item.getId() ?: return

            val block = event.block

            if (block.state is Skull) {
                val state = block.state as Skull
                state.persistentDataContainer.set(skullItemIdKey,
                        PersistentDataType.INTEGER, itemId)
                state.update()
            }
        }
    }

    @EventHandler
    fun onPlayerPickupItem(event: EntityPickupItemEvent) {
        val player = event.entity as? Player ?: return

        if (!player.isChocolatePlayer()) {
            event.isCancelled = true
            return
        }

        val item = event.item.itemStack
        val itemId = item.getId() ?: return

        if (itemId == 5) {
            event.isCancelled = true
            var amount = item.amount

            val hiddenCoinAmount = ItemUtils.getData(item, "coin-amount")
            if (hiddenCoinAmount != null) {
                amount = hiddenCoinAmount.toInt()
            }

            event.item.remove()
            plugin.database.addCoins(player.uniqueId, amount)
            EffectUtils.playSound(Sound.BLOCK_CHAIN_PLACE, player, 1f, 2f)
        }

    }

    @EventHandler
    fun onPlayerDropItem(event: EntityDropItemEvent) {
        val player = event.entity as? Player ?: return

        if (player.gameMode == GameMode.CREATIVE) {
            return
        }

        if (plugin.chocolateConfig.spawn.world == player.world) {
            event.isCancelled = true
        }

        if (!player.isChocolatePlayer()) {
            event.isCancelled = true
            return
        }
    }

    @EventHandler
    fun onFoodChange(event: FoodLevelChangeEvent) {
        val player = event.entity as Player

        if (player.world == plugin.chocolateConfig.spawn.world) {
            if (player.foodLevel > event.foodLevel) {
                event.isCancelled = true
            }
        }
    }


}