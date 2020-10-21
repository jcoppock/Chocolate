package io.github.jamessoda.mc.chocolate

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand


@CommandAlias("admin")
class AdminCommand(private val plugin: Chocolate) : BaseCommand() {

    @Subcommand("messages reload")
    @CommandPermission("chocolate.admin")
    fun onMessagesReload() {
        plugin.language.load()
    }

}