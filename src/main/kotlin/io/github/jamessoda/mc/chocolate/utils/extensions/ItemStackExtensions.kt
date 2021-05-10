package io.github.jamessoda.mc.chocolate.utils.extensions

import com.mojang.authlib.GameProfile
import com.mojang.authlib.properties.Property
import io.github.jamessoda.mc.chocolate.Chocolate.Companion.plugin
import io.github.jamessoda.mc.chocolate.utils.ItemUtils
import net.minecraft.server.v1_16_R3.im
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.Skull
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta
import java.lang.reflect.Field
import java.util.*


fun ItemStack.compare(itemStack: ItemStack): Boolean {
    return ItemUtils.getData(this, "chocolate-id") ==
            ItemUtils.getData(itemStack, "chocolate-id")
}

fun ItemStack.setDisplayName(string: String): ItemStack {
    val meta = this.itemMeta

    val name: String = plugin.language.replaceColors(string)

    meta.setDisplayName(name)
    this.itemMeta = meta

    return this
}

fun ItemStack.setDisplayName(player: Player, keyOrName: String): ItemStack {
    val meta = this.itemMeta

    var name = plugin.language.getMenuText(player, keyOrName)

    if (name == "") {
        name = keyOrName
        name = plugin.language.replaceColors(name)
        name = plugin.language.replacePlaceholders(player, name)
    }

    meta.setDisplayName(name)
    this.itemMeta = meta

    return this
}

fun ItemStack.setDisplayName(player: Player, key: String, placeholders: Map<String, String>): ItemStack {
    val meta = this.itemMeta

    meta.setDisplayName(plugin.language.getMenuText(player, key, placeholders))
    this.itemMeta = meta

    return this
}

fun ItemStack.getDisplayName(): String {
    val meta = this.itemMeta

    if (meta.hasDisplayName()) {
        return meta.displayName
    } else if (meta.hasLocalizedName()) {
        return meta.localizedName
    } else {
        return this.type.name.makePretty()
    }
}

fun ItemStack.setDescription(key: String): ItemStack {
    val meta = this.itemMeta ?: return this

    val lore = plugin.language.getMenuText(key).split("\n")

    meta.lore = lore.toList()
    this.itemMeta = meta

    return this
}

fun ItemStack.setDescription(player: Player, key: String): ItemStack {
    val meta = this.itemMeta ?: return this

    val lore = plugin.language.getMenuText(player, key).split("\n")

    meta.lore = lore.toList()
    this.itemMeta = meta

    return this
}

fun ItemStack.setDescription(player: Player, key: String, placeholders: Map<String, String>): ItemStack {
    val meta = this.itemMeta ?: return this

    val lore = plugin.language.getMenuText(player, key, placeholders).split("\n")

    meta.lore = lore.toList()
    this.itemMeta = meta

    return this
}

fun ItemStack.setDescription(lore: List<String>): ItemStack {
    val meta = this.itemMeta

    val lines = arrayListOf<String>()

    lore.forEach { s ->
        lines.add(plugin.language.replaceColors(s))
    }

    meta.lore = lines
    this.itemMeta = meta

    return this
}

fun ItemStack.setCustomModelData(model: Int): ItemStack {
    val meta = this.itemMeta

    meta.setCustomModelData(model)

    this.itemMeta = meta

    return this
}

fun ItemStack.setSkullValue(value: String): ItemStack {
    if (this.type == Material.PLAYER_HEAD) {
        val skullMeta = itemMeta as SkullMeta

        val url = "http://textures.minecraft.net/texture/$value"

        val profile = GameProfile(UUID.randomUUID(), null)
        val encodedData: ByteArray = Base64.getEncoder().encode(
                java.lang.String.format("{textures:{SKIN:{url:\"%s\"}}}", url).toByteArray())

        profile.properties.put("textures", Property("textures", String(encodedData)))
        var profileField: Field? = null
        try {
            profileField = skullMeta.javaClass.getDeclaredField("profile")
            profileField.isAccessible = true
            profileField.set(skullMeta, profile)
        } catch (e: NoSuchFieldException) {
            e.printStackTrace()
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
        } catch (e: IllegalAccessException) {
            e.printStackTrace()
        }

        this.itemMeta = skullMeta
    }
    return this
}

fun ItemStack.getId(): Int? {
    return ItemUtils.getData(this, "chocolate-id")?.toIntOrNull()
}
