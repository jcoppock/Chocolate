package io.github.jamessoda.mc.chocolate.magic

import io.github.jamessoda.mc.chocolate.Chocolate
import io.github.jamessoda.mc.chocolate.Chocolate.Companion.plugin
import io.github.jamessoda.mc.chocolate.utils.EffectUtils
import io.github.jamessoda.mc.chocolate.utils.ItemUtils
import io.github.jamessoda.mc.chocolate.utils.MathUtils
import io.github.jamessoda.mc.chocolate.utils.extensions.isChocolatePlayer
import org.bukkit.Sound
import org.bukkit.entity.HumanEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.meta.Damageable
import org.bukkit.inventory.meta.ItemMeta

class SpellCastListener() : Listener {

    private val magicManager = plugin.magicManager

    @EventHandler
    fun onPlayerCastSpell(event: PlayerInteractEvent) {

        if (!event.hasItem()) {
            return
        }

        if (event.action == Action.RIGHT_CLICK_AIR || event.action == Action.RIGHT_CLICK_BLOCK ||
                event.action == Action.LEFT_CLICK_AIR || event.action == Action.LEFT_CLICK_BLOCK) {
            val item = event.item ?: return
            val spell = magicManager.getSpell(item) ?: return
            event.isCancelled = true

            if(!event.player.isChocolatePlayer()) {
                plugin.language.sendMessage(event.player, "misc.not_verified")
                return
            }

            if (magicManager.isOnCooldown(event.player, spell)) {
                return
            }

            if(spell is ChannelSpell && ChannelSpell.isChanneling(event.player)) {
                return
            }

            if(plugin.safeHavenManager.isInSafeHaven(event.player) && !spell.inSafeHaven) {
                plugin.language.sendMessage(event.player, "magic.not_here")
                return
            }

            spell.use(event.player) {
                if (it) {
                    magicManager.addCooldown(event.player, spell)

                    (event.player as HumanEntity).setCooldown(item.type,
                            (magicManager.universalCooldown * 20).toInt())

                    val itemDamageChance = spell.getStat("item-damage-chance", event.player) ?: 1.0

                    if (MathUtils.probabilityCheck(itemDamageChance)) {
                        val maxUses = spell.uses
                        var uses = (ItemUtils.getData(item, "spell-uses") ?: "").toIntOrNull() ?: 0
                        uses += 1

                        if (uses > maxUses) {
                            event.player.inventory.remove(item)
                            EffectUtils.playSound(Sound.BLOCK_RESPAWN_ANCHOR_DEPLETE, event.player.location,
                                    1f, 2f)
                        } else {
                            if (item.itemMeta is Damageable) {
                                val itemMeta = item.itemMeta as Damageable
                                val maxDurability = item.type.maxDurability

                                val damage = maxDurability * (uses.toDouble() / maxUses.toDouble())
                                itemMeta.damage = damage.toInt()
                                item.itemMeta = itemMeta as ItemMeta
                            }
                            ItemUtils.setData(item, "spell-uses", uses.toString())
                        }
                    }
                }
            }
        }
    }
}