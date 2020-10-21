package io.github.jamessoda.mc.chocolate.economy

import io.github.jamessoda.mc.chocolate.Chocolate
import io.github.jamessoda.mc.chocolate.db.DatabaseHandler
import org.bukkit.entity.Player

class EconomyManager(plugin: Chocolate) {

    private val database: DatabaseHandler = plugin.database

    fun getBalance(player: Player) : Int {
        val user = database.getUserFromPlayer(player)
        return user.balance ?: 0
    }

    fun setBalance(player: Player, balance: Int) {
        val user = database.getUserFromPlayer(player)
        user.balance = balance
        database.saveUser(user)
    }

}