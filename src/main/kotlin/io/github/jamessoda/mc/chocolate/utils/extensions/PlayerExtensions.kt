package io.github.jamessoda.mc.chocolate.utils.extensions

import io.github.jamessoda.mc.chocolate.Chocolate.Companion.plugin
import io.github.jamessoda.mc.chocolate.utils.PlayerUtils
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.BlockIterator
import kotlin.math.roundToInt

fun Player.isChocolatePlayer() : Boolean {
    return this.hasPermission("chocolate.player")
}

fun Player.isPVP(other: Player) : Boolean {

    val thisGuild = plugin.database.getGuildIdOfPlayer(this)
    val otherGuild = plugin.database.getGuildIdOfPlayer(other)

    //TODO: Check if in PVP world/area
    return this.world.pvp &&
            (thisGuild != otherGuild ||
            (thisGuild == null || otherGuild == null))

}

fun Player.isSameGuild(other: Player) : Boolean {
    val playerGuild = plugin.database.getGuildIdOfPlayer(this) ?: return false

    return playerGuild == plugin.database.getGuildIdOfPlayer(other)
}

fun Player.hasFullInventory() : Boolean {
    var full = true

    for(i in this.inventory.contents) {
        if(i == null || i.type == Material.AIR) {
            full = false
        }
    }

    return full
}

fun Player.safeAddItem(item: ItemStack) {
    if(!this.hasFullInventory()) {
        this.inventory.addItem(item)
    } else {
        this.world.dropItemNaturally(this.location, item)
    }
}

fun Player.getTargetBlock(range: Double, includeAir: Boolean): Block? {
    if (this.location.y < 0)
        return this.location.block

    val blockIterator = BlockIterator(this.location, 1.75, range.toInt())
    var firstBlock: Block = blockIterator.next()

    var lastAirBlock: Block = this.location.block

    while (blockIterator.hasNext()) {
        firstBlock = blockIterator.next()
        if (firstBlock.type == Material.AIR) {
            lastAirBlock = firstBlock
            continue
        }
        break
    }

    if (firstBlock.type == Material.AIR) {
        if (includeAir) {
            return lastAirBlock
        } else {
            return null
        }
    } else {
        return firstBlock
    }
}

// Calculate player's current EXP amount
fun Player.getCurrentTotalExp(): Int {
    var exp = 0
    val level = this.level

    // Get the amount of XP in past levels
    exp += PlayerUtils.getExpAtLevel(level)

    // Get amount of XP towards next level
    exp += (PlayerUtils.getExpToLevelUp(level) * this.exp).roundToInt()
    return exp
}

// Give or take EXP
fun Player.setCurrentTotalExp(exp: Int): Int {
    // Get player's current exp
    val currentExp = this.getCurrentTotalExp()

    // Reset player's current exp to 0
    this.exp = 0f
    this.level = 0

    // Give the player their exp back, with the difference
    val newExp = currentExp + exp
    this.giveExp(newExp)

    // Return the player's new exp amount
    return newExp
}
