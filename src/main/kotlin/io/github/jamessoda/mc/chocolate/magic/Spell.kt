package io.github.jamessoda.mc.chocolate.magic

import io.github.jamessoda.mc.chocolate.Chocolate.Companion.plugin
import io.github.jamessoda.mc.chocolate.masteries.Mastery
import io.github.jamessoda.mc.chocolate.utils.MathUtils
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.Listener
import org.bukkit.inventory.ItemStack
import java.util.function.Consumer


abstract class Spell(val itemId: Int, val name: String, val uses: Int,
                     private val stats: Map<String, Pair<Double, Double>>,
                     val inSafeHaven: Boolean = false) : Listener {

    val prettyName = name.replace("_", " ")

    abstract fun use(player: Player, cooldownCallback: Consumer<Boolean>)

    fun getItemStack() : ItemStack {
        return plugin.itemsConfig.getItem(itemId)?.clone()!!
    }

    fun getStat(name: String, player: Player) : Double? {

        val stat : Pair<Double, Double> = stats[name] ?: return null
        val min = stat.first
        val max = stat.second


        return MathUtils.lerp(min, max, Mastery.getEffectiveness(plugin.database.getMasteryLevel(player.uniqueId, "magic")))
    }

    companion object {
        val spellTravelThroughBlocks = mutableListOf(Material.GRASS, Material.TALL_GRASS, Material.FERN, Material.SUGAR_CANE)
    }
}