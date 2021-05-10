package io.github.jamessoda.mc.chocolate.economy.bank.menu

import io.github.jamessoda.mc.chocolate.Chocolate
import io.github.jamessoda.mc.chocolate.Chocolate.Companion.plugin
import io.github.jamessoda.mc.chocolate.utils.menu.Menu
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import java.util.*

abstract class BankDepositWithdrawMenu() : Menu(6) {


    val amounts = mutableMapOf<UUID, Int>()


    fun refresh(player: Player) {

        openInventory(player)

    }

}