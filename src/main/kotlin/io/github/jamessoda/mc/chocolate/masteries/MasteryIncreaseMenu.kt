package io.github.jamessoda.mc.chocolate.masteries

import io.github.jamessoda.mc.chocolate.Chocolate.Companion.plugin
import io.github.jamessoda.mc.chocolate.utils.EffectUtils
import io.github.jamessoda.mc.chocolate.utils.extensions.setCustomModelData
import io.github.jamessoda.mc.chocolate.utils.extensions.setDescription
import io.github.jamessoda.mc.chocolate.utils.extensions.setDisplayName
import io.github.jamessoda.mc.chocolate.utils.menu.Menu
import io.github.jamessoda.mc.chocolate.utils.menu.MenuItem
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class MasteryIncreaseMenu (private val mastery: Mastery): Menu(3) {

    override fun getTitle(player: Player): String {
        return "${mastery.name} Mastery"
    }

    init {

        setItem(15, object: MenuItem {
            override fun getItemStack(player: Player): ItemStack {
                return mastery.getLevelIcon(player, plugin.database.getMasteryLevel(player.uniqueId, mastery.name) + 1)
            }

            override fun onClick(player: Player) {
                val masteryLevel = plugin.database.getMasteryLevel(player.uniqueId, mastery.name)

                val playerLevel = player.level
                val cost = Mastery.getLevelCost( masteryLevel + 1)

                if(playerLevel >= cost) {

                    player.level -= cost
                    plugin.database.setMasteryLevel(player.uniqueId, mastery.name, masteryLevel + 1)

                    if(masteryLevel + 1 % 10 == 0) {
                        for(p in Bukkit.getOnlinePlayers()) {
                            plugin.language.sendMessage(p, "masteries.increase_broadcast",
                                    mapOf(Pair("{player}", player.name), Pair("{name}", mastery.name),
                                            Pair("{level}", "${masteryLevel + 1}")))
                        }
                    }

                    EffectUtils.playSound(Sound.ENTITY_PLAYER_LEVELUP, player, 1f, .5f)
                    Mastery.MAGIC.masteryMenu.open(player)

                } else {
                    player.sendMessage("${ChatColor.DARK_RED}You do not have enough Levels!")
                    player.closeInventory()
                }
            }
        })

        setItem(11, object: MenuItem {
            override fun getItemStack(player: Player): ItemStack {
                val icon = ItemStack(Material.ARROW)
                icon.setCustomModelData(2)
                icon.setDisplayName("${ChatColor.GOLD}Go Back")
                return icon
            }

            override fun onClick(player: Player) {
                mastery.masteryMenu.open(player)
            }
        })
    }
}