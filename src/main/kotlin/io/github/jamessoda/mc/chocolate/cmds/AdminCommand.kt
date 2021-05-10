package io.github.jamessoda.mc.chocolate.cmds

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import io.github.jamessoda.mc.chocolate.Chocolate.Companion.plugin
import io.github.jamessoda.mc.chocolate.economy.triple_e.menu.TripleEMainMenu
import io.github.jamessoda.mc.chocolate.masteries.Mastery
import io.github.jamessoda.mc.chocolate.utils.extensions.safeAddItem
import org.bukkit.Bukkit
import org.bukkit.entity.Player


@CommandAlias("admin")
class AdminCommand : BaseCommand() {

    private val magicManager = plugin.magicManager

    @Subcommand("messages reload")
    @CommandPermission("chocolate.admin")
    fun onMessagesReload() {
        plugin.language.load()
    }

    @Subcommand("items reload")
    @CommandPermission("chocolate.admin")
    fun onItemsReload() {
        Bukkit.resetRecipes()
        plugin.itemsConfig.load()

        for(p in Bukkit.getOnlinePlayers()) {
            plugin.itemsConfig.refreshItems(p)
        }
    }

    @Subcommand("items get")
    @CommandPermission("chocolate.admin")
    fun onItemsGet(player: Player, id: Int) {
        val item = plugin.itemsConfig.getItem(id) ?: return

        player.safeAddItem(item)
    }

    @Subcommand("chunk add")
    @CommandPermission("chocolate.admin")
    fun onChunkAdd(player: Player, safeHaven: String) {

        val chunk = player.location.chunk

        if(plugin.safeHavenManager.getSafeHaven(chunk) != null) {
            return
        }

        plugin.safeHavenManager.addChunk(safeHaven, chunk)
        plugin.safeHavenManager.saveChunks()

    }

    @Subcommand("chunk remove")
    @CommandPermission("chocolate.admin")
    fun onChunkRemove(player: Player) {

        val chunk = player.location.chunk

        if(plugin.safeHavenManager.getSafeHaven(chunk) != null) {
            plugin.safeHavenManager.removeChunk(chunk)
            plugin.safeHavenManager.saveChunks()
        }

    }


    @Subcommand("spell get")
    @CommandPermission("chocolate.admin")
    fun onSpellGet(player: Player, spellName: String) {

        val spell = magicManager.getSpell(spellName) ?: return

        player.inventory.addItem(spell.getItemStack())

    }

    @Subcommand("test")
    @CommandPermission("chocolate.admin")
    fun onTest(player: Player) {

        TripleEMainMenu.menu.openInventory(player)

    }

    @Subcommand("magic menu")
    @CommandPermission("chocolate.admin")
    fun onMagicMenu(player: Player) {
        Mastery.MAGIC.masteryMenu.open(player)
    }

}