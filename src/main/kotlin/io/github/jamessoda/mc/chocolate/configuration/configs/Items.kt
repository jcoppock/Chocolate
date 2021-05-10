package io.github.jamessoda.mc.chocolate.configuration.configs

import io.github.jamessoda.mc.chocolate.Chocolate
import io.github.jamessoda.mc.chocolate.Chocolate.Companion.plugin
import io.github.jamessoda.mc.chocolate.configuration.ConfigFile
import io.github.jamessoda.mc.chocolate.utils.ItemUtils
import io.github.jamessoda.mc.chocolate.utils.MathUtils
import io.github.jamessoda.mc.chocolate.utils.extensions.setCustomModelData
import io.github.jamessoda.mc.chocolate.utils.extensions.setDescription
import io.github.jamessoda.mc.chocolate.utils.extensions.setDisplayName
import io.github.jamessoda.mc.chocolate.utils.extensions.setSkullValue
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.enchantments.Enchantment
import org.bukkit.entity.EntityType
import org.bukkit.entity.Player
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.inventory.*
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta
import java.util.logging.Level

class Drop(val item: ItemStack, val probability: Double)

class Items : ConfigFile("items.yml", hasDefaults = true) {


    private val items = mutableMapOf<Int, ItemStack>()

    // first - result id
    // second - recipe id
    private val recipes = mutableMapOf<Pair<Int, Int>, NamespacedKey>()

    val entityDrops = mutableMapOf<EntityType, List<Drop>>()

    init {
        load()
    }

    fun load() {
        val itemsSection = yaml.getConfigurationSection("items")

        itemsSection?.getKeys(false)?.forEach { key ->
            val itemId = key.toIntOrNull() ?: return@forEach
            val name = itemsSection.getString("$key.name") ?: return@forEach
            val materialName = itemsSection.getString("$key.material") ?: return@forEach

            val flags = itemsSection.getStringList("$key.flags")

            val material = Material.getMaterial(materialName) ?: return@forEach

            val model = if (itemsSection.get("$key.model") != null) {
                itemsSection.getInt("$key.model")
            } else {
                null
            }

            val skinValue = if (itemsSection.get("$key.skin") != null) {
                itemsSection.getString("$key.skin")
            } else {
                null
            }

            val description = itemsSection.getStringList("$key.description")

            addItem(itemId, name, material, model, skinValue, flags, description)
        }

        val recipesSection = yaml.getConfigurationSection("recipes")

        recipesSection?.getKeys(false)?.forEach { key ->
            val resultItemId = key.toIntOrNull() ?: return@forEach

            val resultItem = getItem(resultItemId) ?: return@forEach

            val recipeSection = recipesSection.getConfigurationSection(key)

            recipeSection?.getKeys(false)?.forEach { recipeId ->
                val shape = recipeSection.getStringList("$recipeId.shape")
                val items = mutableMapOf<Char, ItemStack>()

                val recipeItemsSection = recipeSection.getConfigurationSection("$recipeId.items")

                recipeItemsSection?.getKeys(false)?.forEach { itemChar ->
                    val itemName = recipeItemsSection.getString(itemChar)

                    if (itemName?.toIntOrNull() != null) {

                        val item = getItem(itemName.toInt())
                        if (item != null) {
                            items.putIfAbsent(itemChar[0], item)
                        }
                    } else if (itemName != null) {
                        val material = Material.getMaterial(itemName)

                        if (material != null) {
                            items.putIfAbsent(itemChar[0], ItemStack(material))
                        }
                    }
                }

                registerRecipe(resultItem, resultItemId, recipeId.toInt(), shape, items)
            }
        }

        val dropsSection = yaml.getConfigurationSection("drops")

        dropsSection?.getKeys(false)?.forEach { key ->
            val entityType = EntityType.valueOf(key)

            val dropList = dropsSection.getStringList(key)

            val drops = mutableListOf<Drop>()

            for (drop in dropList) {

                val arr = drop.split(":")

                if (arr.size == 3) {
                    val itemId = arr[0].toIntOrNull() ?: continue
                    val amount = arr[1].toIntOrNull() ?: continue
                    val probability = arr[2].toDoubleOrNull() ?: continue

                    val item = getItem(itemId) ?: continue
                    item.amount = amount

                    drops.add(Drop(item, probability))
                }
            }

            entityDrops[entityType] = drops
        }

    }

    private fun addItem(id: Int, name: String, material: Material, model: Int?, skinValue: String?,
                        flags: List<String>, description: List<String>) {

        if (items[id] != null) {
            Bukkit.getLogger().log(Level.SEVERE, "Attempted to add Item with duplicate ID!")
            return
        }

        val item = if (skinValue != null) {
            ItemUtils.getSkull("http://textures.minecraft.net/texture/$skinValue")
                    ?: ItemStack(Material.PLAYER_HEAD)
        } else {
            ItemStack(material)
        }

        item.setDisplayName("&r${plugin.language.replacements(name)}")

        if (model != null) {
            item.setCustomModelData(model)
        }

        val newDescription = mutableListOf<String>()

        for(line in description) {
            newDescription.add(plugin.language.replacements(line))
        }

        if (newDescription.isNotEmpty()) {
            item.setDescription(newDescription)
        }

        if (flags.contains("enchanted")) {
            item.addUnsafeEnchantment(Enchantment.ARROW_INFINITE, 0)
            item.addItemFlags(ItemFlag.HIDE_ENCHANTS)
        }

        ItemUtils.setData(item, "chocolate-id", id.toString())

        items[id] = item
    }

    fun getItem(id: Int): ItemStack? {
        return items[id]?.clone()
    }

    fun getItemIds() : List<Int> {
        return items.keys.toList()
    }

    private fun registerRecipe(result: ItemStack, resultId: Int, recipeId: Int, shape: MutableList<String>,
                               itemMap: Map<Char, ItemStack>) {

        if (shape.size != 3) {
            Bukkit.getLogger().log(Level.SEVERE, "Attempted to register Recipe with invalid shape!")
            return
        }

        var shapeString = ""
        for (r in shape) {
            shapeString += r
        }

        // This includes the empty space, if present
        var itemCount = shapeString.chars().distinct().count()

        if (shapeString.contains(" ")) {
            itemCount -= 1
        }

        if (itemCount > 0 && itemCount.toInt() == itemMap.size) {
            val namespacedKey = NamespacedKey(Chocolate.plugin, "$resultId-$recipeId")
            val recipe = ShapedRecipe(namespacedKey, result)

            val row1 = shape[0]
            val row2 = shape[1]
            val row3 = shape[2]

            if (row1.length != 3 || row2.length != 3 || row3.length != 3) {
                Bukkit.getLogger().log(Level.SEVERE, "Attempted to register Recipe with invalid shape!")
                return
            }

            recipe.shape(row1, row2, row3)

            for (entry in itemMap) {
                recipe.setIngredient(entry.key, entry.value)
            }

            Bukkit.addRecipe(recipe)
            recipes[Pair(resultId, recipeId)] = namespacedKey
        }
    }

    fun getRecipe(resultId: Int, recipeId: Int): Recipe? {
        val key = recipes[Pair(resultId, recipeId)]
        if (key != null) {
            return Bukkit.getRecipe(key)
        }

        return null
    }

    fun getRecipe(result: ItemStack, recipeId: Int = 1): Recipe? {
        for (entry in recipes.entries) {
            val id = entry.key
            if (id.second == recipeId) {
                val key = entry.value
                val recipe = Bukkit.getRecipe(key) ?: return null

                if (recipe.result.isSimilar(result)) {
                    return recipe
                }
            }
        }
        return null
    }

    fun getDrops(entityType: EntityType, spawnReason: CreatureSpawnEvent.SpawnReason? = null) : List<ItemStack> {
        val drops = entityDrops[entityType] ?: listOf()

        val result = mutableListOf<ItemStack>()

        for(drop in drops) {
            var probability = drop.probability

            if(spawnReason == CreatureSpawnEvent.SpawnReason.SPAWNER) {
                probability /= 2
            }

            if(MathUtils.probabilityCheck(drop.probability)) {
                result.add(drop.item)
            }
        }

        return result
    }

    fun refreshItems(player: Player) {

        player.discoverRecipes(recipes.values)

        refreshItems(player.inventory)
        refreshItems(player.enderChest)

    }

    private fun refreshItems(inventory: Inventory) {
        for (i in 0 until inventory.size) {
            val item = inventory.getItem(i)
            if (item == null || item.type == Material.AIR) {
                continue
            }
            val idString = ItemUtils.getData(item, "chocolate-id")
            if (idString != null) {
                val id = idString.toInt()

                val itemFromId = getItem(id) ?: continue

                if (!item.isSimilar(itemFromId)) {
                    val uses = ItemUtils.getData(item, "spell-uses")

                    if (uses != null) {
                        ItemUtils.setData(itemFromId, "spell-uses", uses)

                        val spell = plugin.magicManager.getSpell(itemFromId)

                        if (spell != null && itemFromId.itemMeta is Damageable) {
                            val itemMeta = itemFromId.itemMeta as Damageable
                            val maxDurability = itemFromId.type.maxDurability

                            val damage = maxDurability * (uses.toDouble() / spell.uses.toDouble())
                            itemMeta.damage = damage.toInt()
                            itemFromId.itemMeta = itemMeta as ItemMeta
                        }
                    }

                    itemFromId.amount = item.amount

                    inventory.setItem(i, itemFromId)
                }
            }
        }
    }

}