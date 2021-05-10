package io.github.jamessoda.mc.chocolate.magic

import io.github.jamessoda.mc.chocolate.Chocolate
import io.github.jamessoda.mc.chocolate.utils.BossBarUtils
import io.github.jamessoda.mc.chocolate.utils.extensions.compare
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.NamespacedKey
import org.bukkit.boss.BarColor
import org.bukkit.boss.BarStyle
import org.bukkit.boss.KeyedBossBar
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import java.util.*

class MagicManager(private val plugin: Chocolate) {

    private val universalCooldowns = mutableMapOf<UUID, Long>()
    val universalCooldown = 1.0

    private val spellCooldowns = mutableMapOf<UUID, MutableMap<Spell, Long>>()

    private val spells = mutableMapOf<String, Spell>()

    init {
        Bukkit.getScheduler().runTaskTimer(plugin, Runnable {
            val spellCooldownsIterator = spellCooldowns.iterator()

            while(spellCooldownsIterator.hasNext()) {
                val entry = spellCooldownsIterator.next()
                val uuid = entry.key
                val player = Bukkit.getPlayer(uuid)

                if(player == null || !player.isOnline) {
                    spellCooldownsIterator.remove()
                }
            }

            val universalCooldownsIterator = universalCooldowns.iterator()

            while(universalCooldownsIterator.hasNext()) {
                val entry = universalCooldownsIterator.next()
                val uuid = entry.key
                val player = Bukkit.getPlayer(uuid)

                if(player == null || !player.isOnline) {
                    universalCooldownsIterator.remove()
                }
            }
        }, 0L, 5 * 60 * 20L)
    }

    fun registerSpell(spell: Spell) {
        spells[spell.name.toLowerCase()] = spell

        Bukkit.getPluginManager().registerEvents(spell, plugin)
    }

    fun addCooldown(player: Player, spell: Spell) {
        universalCooldowns[player.uniqueId] = System.currentTimeMillis()

        val playerSpellCooldowns = spellCooldowns[player.uniqueId] ?: mutableMapOf()

        playerSpellCooldowns[spell] = System.currentTimeMillis()

        spellCooldowns[player.uniqueId] = playerSpellCooldowns

        BossBarUtils.addCountDownBossBar(player,
                NamespacedKey(plugin, "${player.uniqueId}_${spell.name.replace(" ", "_")}_cooldown"),
                "${ChatColor.BLUE}${spell.prettyName}",
                spell.getStat("cooldown", player) ?: 2.0)
    }

    fun getSpells() : List<Spell> {
        return spells.values.toList()
    }

    private fun getUniversalTimePassed(player: Player): Long {
        val uuid = player.uniqueId

        val currentTime = System.currentTimeMillis()

        val universalSpellTime = universalCooldowns[uuid] ?: 0

        return currentTime - universalSpellTime
    }

    private fun getSpellTimePassed(player: Player, spell: Spell): Long {
        val uuid = player.uniqueId

        val currentTime = System.currentTimeMillis()

        val spellTime = spellCooldowns[uuid]?.get(spell) ?: 0

        return currentTime - spellTime
    }

    fun isOnCooldown(player: Player, spell: Spell): Boolean {

        if (getUniversalTimePassed(player) < (universalCooldown * 1000)) {
            return true
        }
        val cooldown = spell.getStat("cooldown", player) ?: 4.0
        if (getSpellTimePassed(player, spell) < (cooldown * 1000)) {
            return true
        }

        return false
    }

    fun getSpell(name: String): Spell? {
        return spells[name.toLowerCase()]
    }

    fun getSpell(item: ItemStack): Spell? {
        for (entry in spells) {
            val spell = entry.value
            if (item.compare(spell.getItemStack())) {
                return spell
            }
        }

        return null
    }

    fun getSpell(itemId: Int): Spell? {
        for (entry in spells) {
            val spell = entry.value
            if (spell.itemId == itemId) {
                return spell
            }
        }

        return null
    }


}