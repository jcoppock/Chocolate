package io.github.jamessoda.mc.chocolate.magic.spells.utility

import io.github.jamessoda.mc.chocolate.magic.ChannelSpell
import io.github.jamessoda.mc.chocolate.utils.EffectUtils
import io.github.jamessoda.mc.chocolate.utils.ShapeUtils
import io.github.jamessoda.mc.chocolate.utils.extensions.safeAddItem
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.function.Consumer

/*
class SmeltOreSpell : ChannelSpell(200, "Smelt_Ore", 10, mapOf(
        Pair("cooldown", Pair(4.0, 1.0)),
        Pair("channel-time", Pair(2.0, 1.0)),
        Pair("item-damage-chance", Pair(1.0, .8))
), true) {

    override fun tickEffect(player: Player, tick: Int) {

        for (vec in ShapeUtils.getCircleOffsets(1, 5)) {
            EffectUtils.displayParticle(Particle.FLAME,
                    player.location.clone().add(vec), 1, 0.0, Vector(.1, 0.0, .1))
        }

        EffectUtils.playSound(Sound.BLOCK_BLASTFURNACE_FIRE_CRACKLE, player.location,
                1f, 1f)

    }

    val ores = listOf(Material.IRON_ORE, Material.GOLD_ORE)

    override fun finishChannel(player: Player, cooldownCallback: Consumer<Boolean>) {

        EffectUtils.playSound(Sound.BLOCK_FIRE_AMBIENT, player.location,
                1f, 1.4f)
        EffectUtils.playSound(Sound.BLOCK_FIRE_EXTINGUISH, player.location,
                1f, .8f)

        var remainingSmelts = 64

        for (i in player.inventory.contents.indices) {
            val slot = player.inventory.contents[i] ?: continue

            if (remainingSmelts <= 0) {
                break
            }

            if (ores.contains(slot.type)) {

                val amount = slot.amount

                val result: Material = when (slot.type) {
                    Material.IRON_ORE -> {
                        Material.IRON_INGOT
                    }
                    Material.GOLD_ORE -> {
                        Material.GOLD_INGOT
                    }
                    else -> {
                        Material.AIR
                    }
                }


                if(result == Material.AIR) {
                    continue
                }

                if (amount <= remainingSmelts) {
                    player.inventory.setItem(i, ItemStack(Material.AIR))
                    player.safeAddItem(ItemStack(result, amount))
                    remainingSmelts -= amount
                } else {
                    slot.amount = amount - remainingSmelts
                    player.safeAddItem(ItemStack(result, remainingSmelts))
                    remainingSmelts = 0
                }
                player.updateInventory()
            }
        }
        EffectUtils.playSound(Sound.BLOCK_ANVIL_USE, player.location, .25f, 1.4f)

        cooldownCallback.accept(true)
    }
}
*/