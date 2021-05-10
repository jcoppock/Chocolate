package io.github.jamessoda.mc.chocolate.utils

import dev.dbassett.skullcreator.SkullCreator
import io.github.jamessoda.mc.chocolate.Chocolate
import org.bukkit.NamespacedKey
import org.bukkit.configuration.InvalidConfigurationException
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.ItemMeta
import org.bukkit.persistence.PersistentDataType
import java.nio.charset.StandardCharsets
import java.util.*
import javax.xml.bind.DatatypeConverter


object ItemUtils {

    fun setData(item: ItemStack, keyName: String, data: String) : ItemStack {

        val key = NamespacedKey(Chocolate.plugin, keyName)
        val itemMeta: ItemMeta = item.itemMeta ?: return item
        itemMeta.persistentDataContainer.set(key, PersistentDataType.STRING, data)
        item.itemMeta = itemMeta

        return item
    }

    fun getData(item: ItemStack, keyName: String) : String? {
        val key = NamespacedKey(Chocolate.plugin, keyName)
        val itemMeta: ItemMeta = item.itemMeta ?: return null
        val result = itemMeta.persistentDataContainer.get(key, PersistentDataType.STRING)

        return result
    }

    fun getSkull(player: Player) : ItemStack {
        return getSkull(player.uniqueId)
    }

    fun getSkull(uuid: UUID) : ItemStack {
        return SkullCreator.itemFromUuid(uuid)
    }

    fun getSkull(url: String): ItemStack? {
        return SkullCreator.itemFromUrl(url)
    }



}