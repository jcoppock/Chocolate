package io.github.jamessoda.mc.chocolate.portals

import io.github.jamessoda.mc.chocolate.Chocolate
import io.github.jamessoda.mc.chocolate.magic.spells.utility.TeleportSpell
import io.github.jamessoda.mc.chocolate.utils.WorldUtils
import io.github.jamessoda.mc.chocolate.utils.extensions.format
import io.github.jamessoda.mc.chocolate.utils.extensions.setDescription
import io.github.jamessoda.mc.chocolate.utils.extensions.setDisplayName
import io.github.jamessoda.mc.chocolate.utils.menu.Menu
import io.github.jamessoda.mc.chocolate.utils.menu.MenuItem
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.logging.Level
import kotlin.math.sqrt

class TeleportPortalMenu : Menu(6) {

    override fun openInventory(player: Player) {

        clearItems()

        val guildSlot = 13
        val guildId = Chocolate.plugin.database.getGuildIdOfPlayer(player)
        if (guildId != null) {
            val location = Chocolate.plugin.database.getGuildPortalLocation(guildId)
            if (location != null) {
                val portal = Portal(location, Chocolate.plugin.database.getGuildName(guildId))
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
                        if (TeleportSpell.portalMenuConsumers.containsKey(player.uniqueId)) {

                            TeleportSpell.portalMenuConsumers[player.uniqueId]?.accept(portal)
                        }
                        player.closeInventory()
                    }
                })
            }
        }

        var slot = 27
        for (portal in Chocolate.plugin.portalManager.getPortals()) {
            if (slot >= (9 * 6)) {
                Bukkit.getLogger().log(Level.SEVERE, "There are too many Portals to be accommodated by the PortalMenu")
                break
            }

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

                    if (TeleportSpell.portalMenuConsumers.containsKey(player.uniqueId)) {

                        TeleportSpell.portalMenuConsumers[player.uniqueId]?.accept(portal)
                    }
                    player.closeInventory()
                }
            })

            slot += 2
        }

        super.openInventory(player)
    }

    override fun getTitle(player: Player): String {

        return "${ChatColor.DARK_PURPLE}Using Teleport Scroll..."
    }

    companion object {
        val instance: TeleportPortalMenu by lazy {
            TeleportPortalMenu()
        }
    }
}