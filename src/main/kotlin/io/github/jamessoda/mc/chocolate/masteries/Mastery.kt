package io.github.jamessoda.mc.chocolate.masteries

import io.github.jamessoda.mc.chocolate.Chocolate.Companion.plugin
import io.github.jamessoda.mc.chocolate.utils.extensions.*
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.Recipe
import kotlin.math.ceil
import kotlin.math.pow

class Mastery private constructor(val name: String) {

    private val unlocks = mutableMapOf<Int, List<Recipe>>()

    private val items = plugin.itemsConfig

    val masteryMenu = MasteryMenu(this)
    val levelUpMenu = MasteryIncreaseMenu(this)

    init {
        masteries.add(this)
    }

    fun addUnlock(mastery: Int, resultId: Int, recipeId: Int) {

        val recipe = items.getRecipe(resultId, recipeId) ?: return

        for (recipeList in unlocks.values) {
            if (recipeList.contains(recipe)) {
                return
            }
        }

        val list = unlocks[mastery]?.toMutableList() ?: mutableListOf()
        if (list.contains(recipe)) {
            return
        }

        list.add(recipe)

        unlocks[mastery] = list
    }

    fun getLevelIcon(player: Player, mastery: Int): ItemStack {

        val playerMastery = plugin.database.getMasteryLevel(player.uniqueId, name)

        val item: ItemStack

        val description = mutableListOf<String>()

        description.add("${ChatColor.DARK_GRAY}- ${ChatColor.GRAY}Increased $name Effectiveness")

        description.add("      ${ChatColor.GRAY}from ${ChatColor.WHITE}%${(getEffectiveness(mastery - 1) * 100.0).format(3)} " +
                "${ChatColor.GRAY}to ${ChatColor.WHITE}%${(getEffectiveness(mastery) * 100.0).format(3)}")

        for (recipe in unlocks.getOrDefault(mastery, listOf())) {
            description.add("${ChatColor.DARK_GRAY}- ${ChatColor.GRAY}${recipe.result.getDisplayName()} Recipe")
        }

        description.add("")

        val color: ChatColor
        val unlockDescription: String

        if (playerMastery >= mastery) {
            item = ItemStack(Material.GREEN_STAINED_GLASS_PANE)
            color = ChatColor.GREEN
            unlockDescription = "${ChatColor.GRAY}You've already achieved this Mastery"

        } else if (unlocks.containsKey(mastery)) {
            item = ItemStack(Material.RED_STAINED_GLASS_PANE)
            color = ChatColor.RED
            if (mastery == playerMastery + 1) {
                unlockDescription = "${ChatColor.GRAY}Click to increase your $name Mastery"
                description.add("${ChatColor.GRAY}(Costs ${getLevelCost(mastery)} Levels)")
            } else {
                unlockDescription = "${ChatColor.GRAY}You must achieve all of the previous Masteries"
            }
        } else if(mastery == playerMastery + 1){
            item = ItemStack(Material.GRAY_STAINED_GLASS_PANE)
            color = ChatColor.RED
            unlockDescription = "${ChatColor.GRAY}Click to increase your $name Mastery"
            description.add("${ChatColor.GRAY}(Costs ${getLevelCost(mastery)} Levels)")
        } else {
            item = ItemStack(Material.GRAY_STAINED_GLASS_PANE)
            color = ChatColor.RED
            unlockDescription = "${ChatColor.GRAY}You must achieve all of the previous Masteries"
        }

        item.setDisplayName("${color}Mastery $mastery Unlocks:")

        description.add(unlockDescription)
        item.setDescription(description)

        return item
    }

    fun hasRecipe(recipe: Recipe): Boolean {
        for (unlock in unlocks.entries) {
            val recipes = unlock.value

            for (r in recipes) {
                if (r.result.getId() == recipe.result.getId()) {
                    return true
                }
            }
        }
        return false
    }

    fun hasRecipeUnlocked(player: Player, recipe: Recipe): Boolean {
        val playerLevel = plugin.database.getMasteryLevel(player.uniqueId, name)

        for (unlock in unlocks.entries) {
            val mastery = unlock.key
            val recipes = unlock.value

            for (r in recipes) {
                if (r.result.getId() == recipe.result.getId() && playerLevel >= mastery) {
                    return true
                }
            }
        }

        return false
    }

    companion object {

        const val maxMastery = 100

        val masteries = mutableListOf<Mastery>()

        val MAGIC: Mastery = Mastery("Magic")

        init {
            //TIER 1 SCROLLS
            MAGIC.addUnlock(1, 1, 1)
            MAGIC.addUnlock(1, 201, 1)
            MAGIC.addUnlock(1, 100, 1)

            MAGIC.addUnlock(5, 101, 1)
            MAGIC.addUnlock(10, 102, 1)
            MAGIC.addUnlock(15, 104, 1)


            //TIER 2 SCROLLS
            MAGIC.addUnlock(30, 2, 1)
            MAGIC.addUnlock(30, 110, 1)
            MAGIC.addUnlock(40, 112, 1)
            MAGIC.addUnlock(45, 114, 1)

            MAGIC.addUnlock(50, 400, 1)

            //TIER 3 SCROLLS
            MAGIC.addUnlock(60, 3, 1)
            MAGIC.addUnlock(60, 120, 1)
            MAGIC.addUnlock(70, 122, 1)
            MAGIC.addUnlock(75, 124, 1)


        }

        fun getEffectiveness(mastery: Int): Double {
            val steepness = 10
            return ((steepness.toDouble().pow(mastery.toDouble() / maxMastery.toDouble()) - 1) / (steepness.toDouble() - 1))
        }

        fun getLevelCost(mastery: Int): Int {
            return ceil(mastery / 1.666).toInt()
        }

        fun getMastery(name: String) : Mastery? {
            for(mastery in masteries) {
                if(mastery.name.toLowerCase() == name.toLowerCase()) {
                    return mastery
                }
            }

            return null
        }
    }


}