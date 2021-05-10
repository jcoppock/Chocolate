package io.github.jamessoda.mc.chocolate.economy.triple_e.menu

import io.github.jamessoda.mc.chocolate.Chocolate
import net.wesjd.anvilgui.AnvilGUI
import org.bukkit.entity.Player
import java.util.function.Consumer

object PriceInputMenu {

    fun open(player: Player, callback: Consumer<Int>) {
        AnvilGUI.Builder()
                .plugin(Chocolate.plugin)
                .title("Input a Price")
                .text("")
                .onComplete { p, input ->

                    val price = input.toIntOrNull()

                    if(price == null) {
                        AnvilGUI.Response.text("That is not a valid price!")
                    } else {
                        callback.accept(price)
                    }

                    return@onComplete AnvilGUI.Response.close()
                }
                .open(player)
    }


}