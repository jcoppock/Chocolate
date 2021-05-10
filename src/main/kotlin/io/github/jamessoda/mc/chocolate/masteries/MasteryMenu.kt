package io.github.jamessoda.mc.chocolate.masteries

import io.github.jamessoda.mc.chocolate.Chocolate.Companion.plugin
import io.github.jamessoda.mc.chocolate.utils.extensions.setCustomModelData
import io.github.jamessoda.mc.chocolate.utils.extensions.setDisplayName
import io.github.jamessoda.mc.chocolate.utils.menu.Menu
import io.github.jamessoda.mc.chocolate.utils.menu.MenuItem
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack

class MasteryMenu(val mastery: Mastery)  {

    val pages = mapOf(
            Pair(0, MasteryMenuPage(this, 0)),
            Pair(1, MasteryMenuPage(this, 1)),
            Pair(2, MasteryMenuPage(this, 2)))

    fun open(player: Player) {
        pages[0]?.openInventory(player)
    }
}

class MasteryMenuPage(private val masteryMenu: MasteryMenu, private val page: Int) : Menu(6) {

    override fun getTitle(player: Player): String {
        return masteryMenu.mastery.name + " (Levels ${(36 * page) + 1}-${((36 * page) + 36).coerceAtMost(100)})"
    }

    init {
        var slot = 0
        for (i in ((36 * page) + 1) .. ((36*page) + 36).coerceAtMost(100)) {
            if(i == 100) {
                slot = 31
            }
            setItem(slot, object: MenuItem {
                override fun getItemStack(player: Player): ItemStack {

                    return masteryMenu.mastery.getLevelIcon(player, i)
                }

                override fun onClick(player: Player) {
                    val playerLevel = plugin.database.getMasteryLevel(player.uniqueId, masteryMenu.mastery.name)

                    if(i <= playerLevel) {
                        player.sendMessage("${ChatColor.DARK_RED}You have already achieved this Mastery Level!")
                    } else if(i == playerLevel + 1 ) {

                        masteryMenu.mastery.levelUpMenu.openInventory(player)

                    } else {
                        player.sendMessage("${ChatColor.DARK_RED}You must achieve all of the previous Mastery Levels!")
                    }
                }
            })
            slot++
        }

        setItem(46, object: MenuItem {
            override fun getItemStack(player: Player): ItemStack {
                val icon = ItemStack(Material.ARROW)
                icon.setCustomModelData(2)
                icon.setDisplayName("${ChatColor.GOLD}View Previous Page")
                return icon
            }

            override fun onClick(player: Player) {

                var newPage = page - 1

                if(newPage < 0) {
                    newPage = masteryMenu.pages.size - 1
                }

                masteryMenu.pages[newPage]?.openInventory(player)
            }
        })

        setItem(52, object: MenuItem {
            override fun getItemStack(player: Player): ItemStack {
                val icon = ItemStack(Material.ARROW)
                icon.setCustomModelData(1)
                icon.setDisplayName("${ChatColor.GOLD}View Next Page")
                return icon
            }

            override fun onClick(player: Player) {

                var newPage = page + 1

                if(newPage >= masteryMenu.pages.size) {
                    newPage = 0
                }

                masteryMenu.pages[newPage]?.openInventory(player)
            }
        })

    }

}