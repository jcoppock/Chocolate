package io.github.jamessoda.mc.chocolate.portals

import io.github.jamessoda.mc.chocolate.Chocolate.Companion.plugin
import io.github.jamessoda.mc.chocolate.magic.spells.utility.TeleportSpell
import io.github.jamessoda.mc.chocolate.utils.WorldUtils
import io.github.jamessoda.mc.chocolate.utils.extensions.format
import io.github.jamessoda.mc.chocolate.utils.extensions.setDisplayName
import io.github.jamessoda.mc.chocolate.utils.extensions.setDescription
import io.github.jamessoda.mc.chocolate.utils.menu.Menu
import io.github.jamessoda.mc.chocolate.utils.menu.MenuItem
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryCloseEvent
import org.bukkit.inventory.ItemStack
import java.util.logging.Level
import kotlin.math.sqrt

class EnteredPortalMenu : Menu(6) {

    override fun openInventory(player: Player) {

        clearItems()

        TeleportSpell.teleportingPlayers.remove(player.uniqueId)
        TeleportSpell.portalMenuConsumers.remove(player.uniqueId)

        val closestPortal = plugin.portalManager.getClosestPortal(player) ?: return

        /*
        setItem(13, object : MenuItem {
            override fun getItemStack(player: Player): ItemStack {

                return ItemStack(Material.DIAMOND, 1)
                        .setDisplayName("${ChatColor.DARK_PURPLE}Current Location")
                        .setDescription(listOf("${ChatColor.DARK_PURPLE}Your current location is " +
                                "${ChatColor.GOLD}[${closestPortal.name}]"))
            }

            override fun onClick(player: Player) {}
        })

         */

        val guildSlot = 13
        val guildId = plugin.database.getGuildIdOfPlayer(player)
        if (guildId != null) {
            val location = plugin.database.getGuildPortalLocation(guildId)
            if (location != null) {
                val portal = Portal(location, plugin.database.getGuildName(guildId))
                setItem(guildSlot, object : MenuItem {
                    override fun getItemStack(player: Player): ItemStack {

                        val distance = sqrt(WorldUtils.squaredDistance(portal.location, player.location))

                        if (player.world == portal.location.world) {
                            return ItemStack(Material.ENDER_PEARL, 1)
                                    .setDisplayName("${ChatColor.DARK_PURPLE}Teleport to ${ChatColor.GOLD}[${portal.name}]")
                                    .setDescription(listOf(
                                            "${ChatColor.DARK_PURPLE}Click to teleport to your Guild's Spawn!",
                                            "${ChatColor.DARK_PURPLE}Distance: " +
                                                    "${ChatColor.GRAY}${distance.format(2)} blocks"
                                    ))
                        } else {
                            return ItemStack(Material.ENDER_PEARL, 1)
                                    .setDisplayName("${ChatColor.DARK_PURPLE}Teleport to ${ChatColor.GOLD}[${portal.name}]")
                                    .setDescription(listOf(
                                            "${ChatColor.DARK_PURPLE}Click to teleport to your Guild's Spawn!",
                                            "${ChatColor.DARK_PURPLE}Distance: " +
                                                    "${ChatColor.GREEN}${ChatColor.MAGIC}${distance.format(2)} blocks"
                                    ))
                        }
                    }

                    override fun onClick(player: Player) {
                        plugin.portalManager.teleport(player, portal)
                    }
                })
            }
        }

        var slot = 28
        if(!plugin.portalManager.getPortals().contains(closestPortal)) {
            slot = 27
        }
        for (portal in plugin.portalManager.getPortals()) {
            if (slot >= (9 * 6)) {
                Bukkit.getLogger().log(Level.SEVERE, "There are too many Portals to be accommodated by the PortalMenu")
                break
            }
            if (portal == closestPortal)
                continue

            setItem(slot, object : MenuItem {
                override fun getItemStack(player: Player): ItemStack {

                    val distance = sqrt(WorldUtils.squaredDistance(portal.location, player.location))

                    if (player.world == portal.location.world) {
                        return ItemStack(Material.ENDER_PEARL, 1)
                                .setDisplayName("${ChatColor.DARK_PURPLE}Teleport to ${ChatColor.GOLD}[${portal.name}]")
                                .setDescription(listOf(
                                        "${ChatColor.DARK_PURPLE}Click to teleport to this Location!",
                                        "${ChatColor.DARK_PURPLE}Distance: " +
                                                "${ChatColor.GRAY}${distance.format(2)} blocks"
                                ))
                    } else {
                        return ItemStack(Material.ENDER_PEARL, 1)
                                .setDisplayName("${ChatColor.DARK_PURPLE}Teleport to ${ChatColor.GOLD}[${portal.name}]")
                                .setDescription(listOf(
                                        "${ChatColor.DARK_PURPLE}Click to teleport to your this Location!",
                                        "${ChatColor.DARK_PURPLE}Distance: " +
                                                "${ChatColor.GREEN}${ChatColor.MAGIC}${distance.format(2)} blocks"
                                ))
                    }
                }

                override fun onClick(player: Player) {
                    plugin.portalManager.teleport(player, portal)
                }
            })

            slot += 2
        }



        super.openInventory(player)
    }

    override fun getTitle(player: Player): String {

        val closestPortal = plugin.portalManager.getClosestPortal(player)

        return "${closestPortal?.name}"
    }

    override fun onClose(player: Player, reason: InventoryCloseEvent.Reason) {
        val portal = plugin.portalManager.getClosestPortal(player) ?: return

        if (WorldUtils.squaredDistance(portal.location, player.location) <= 4.0) {
            plugin.portalManager.getClosestPortal(player)?.knockback(player)
            plugin.portalManager.addCooldown(player)
        }
    }

    companion object {
        val instance: EnteredPortalMenu by lazy {
            EnteredPortalMenu()
        }
    }
}