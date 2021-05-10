package io.github.jamessoda.mc.chocolate

import co.aikar.commands.PaperCommandManager
import com.tjplaysnow.discord.`object`.Bot
import com.tjplaysnow.discord.`object`.CommandSpigotManager
import com.tjplaysnow.discord.`object`.ProgramCommand
import com.tjplaysnow.discord.`object`.ThreadSpigot
import io.github.jamessoda.mc.chocolate.cmds.AdminCommand
import io.github.jamessoda.mc.chocolate.cmds.DiscordCommand
import io.github.jamessoda.mc.chocolate.configuration.configs.Config
import io.github.jamessoda.mc.chocolate.configuration.configs.HiddenConfig
import io.github.jamessoda.mc.chocolate.configuration.configs.Items
import io.github.jamessoda.mc.chocolate.configuration.configs.Language
import io.github.jamessoda.mc.chocolate.db.DatabaseHandler
import io.github.jamessoda.mc.chocolate.economy.EconomyCommand
import io.github.jamessoda.mc.chocolate.guild.GuildCommand
import io.github.jamessoda.mc.chocolate.guild.GuildListener
import io.github.jamessoda.mc.chocolate.guild.InviteManager
import io.github.jamessoda.mc.chocolate.listeners.*
import io.github.jamessoda.mc.chocolate.magic.MagicManager
import io.github.jamessoda.mc.chocolate.magic.SpellCastListener
import io.github.jamessoda.mc.chocolate.magic.spells.combat.*
import io.github.jamessoda.mc.chocolate.magic.spells.combat.fireball.FireballSpellT1
import io.github.jamessoda.mc.chocolate.magic.spells.combat.fireball.FireballSpellT2
import io.github.jamessoda.mc.chocolate.magic.spells.combat.fireball.FireballSpellT3
import io.github.jamessoda.mc.chocolate.magic.spells.combat.iceblast.IceBlastSpell
import io.github.jamessoda.mc.chocolate.magic.spells.combat.iceblast.IceBlastSpellT1
import io.github.jamessoda.mc.chocolate.magic.spells.combat.iceblast.IceBlastSpellT2
import io.github.jamessoda.mc.chocolate.magic.spells.combat.iceblast.IceBlastSpellT3
import io.github.jamessoda.mc.chocolate.magic.spells.combat.watersurge.WaterSurgeSpell
import io.github.jamessoda.mc.chocolate.magic.spells.combat.watersurge.WaterSurgeSpellT1
import io.github.jamessoda.mc.chocolate.magic.spells.combat.watersurge.WaterSurgeSpellT2
import io.github.jamessoda.mc.chocolate.magic.spells.combat.watersurge.WaterSurgeSpellT3
import io.github.jamessoda.mc.chocolate.magic.spells.utility.*
import io.github.jamessoda.mc.chocolate.masteries.MasteriesCommand
import io.github.jamessoda.mc.chocolate.masteries.Mastery
import io.github.jamessoda.mc.chocolate.npc.traits.BankerTrait
import io.github.jamessoda.mc.chocolate.npc.traits.ClerkTrait
import io.github.jamessoda.mc.chocolate.npc.traits.InfoTrait
import io.github.jamessoda.mc.chocolate.portals.Portal
import io.github.jamessoda.mc.chocolate.portals.PortalListener
import io.github.jamessoda.mc.chocolate.portals.PortalManager
import io.github.jamessoda.mc.chocolate.safehaven.SafeHavenListener
import io.github.jamessoda.mc.chocolate.safehaven.SafeHavenManager
import io.github.jamessoda.mc.chocolate.task.ChunkOutlineTask
import io.github.jamessoda.mc.chocolate.task.ScoreboardTask
import io.github.jamessoda.mc.chocolate.utils.menu.MenuListener
import net.citizensnpcs.api.CitizensAPI
import net.citizensnpcs.api.trait.TraitInfo
import net.dv8tion.jda.core.Permission
import net.dv8tion.jda.core.entities.Guild
import net.dv8tion.jda.core.entities.MessageChannel
import net.dv8tion.jda.core.entities.User
import net.md_5.bungee.api.chat.ClickEvent
import net.md_5.bungee.api.chat.HoverEvent
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.chat.hover.content.Text
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.entity.Player
import org.bukkit.inventory.MerchantRecipe
import org.bukkit.plugin.java.JavaPlugin
import java.util.logging.Level
import kotlin.properties.Delegates

class Chocolate : JavaPlugin() {
    val linkingPlayers = mutableMapOf<Player, String>()

    val database by lazy { DatabaseHandler() }
    val inviteManager by lazy { InviteManager() }
    val portalManager by lazy {
        PortalManager(this)
    }

    val safeHavenManager by lazy {
        SafeHavenManager(this)
    }

    val chocolateConfig by lazy {
        Config()
    }

    val magicManager by lazy {
        MagicManager(this)
    }

    val itemsConfig by lazy {
        Items()
    }

    private val hiddenConfig by lazy {
        HiddenConfig()
    }

    val language by lazy { Language(this) }

    val commandManager by lazy { PaperCommandManager(this) }

    override fun onEnable() {
        Bukkit.resetRecipes()
        val bossBarIterator = Bukkit.getBossBars().iterator()

        while (bossBarIterator.hasNext()) {
            val bar = bossBarIterator.next()

            bar.removeAll()
            Bukkit.removeBossBar(bar.key)
        }

        if (server.pluginManager.getPlugin("Citizens") == null || !server.pluginManager.getPlugin("Citizens")!!.isEnabled) {
            logger.log(Level.SEVERE, "Citizens 2.0 not found or not enabled")
        } else {

            logger.log(Level.SEVERE, "Registering Traits")
            CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(BankerTrait::class.java)
                    .withName("banker"))

            CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(InfoTrait::class.java)
                    .withName("info"))

            CitizensAPI.getTraitFactory().registerTrait(TraitInfo.create(ClerkTrait::class.java)
                    .withName("clerk"))
        }

        Bukkit.getPluginManager().registerEvents(ConnectionListener(), this)
        Bukkit.getPluginManager().registerEvents(PlayerListener(), this)

        Bukkit.getPluginManager().registerEvents(SafeHavenListener(this), this)
        Bukkit.getPluginManager().registerEvents(GuildListener(this), this)

        Bukkit.getPluginManager().registerEvents(MenuListener(), this)
        Bukkit.getPluginManager().registerEvents(PortalListener(), this)
        Bukkit.getPluginManager().registerEvents(SpellCastListener(), this)
        Bukkit.getPluginManager().registerEvents(CraftingListener(), this)
        Bukkit.getPluginManager().registerEvents(CommandListener(), this)
        Bukkit.getPluginManager().registerEvents(EntityListener(), this)
        Bukkit.getPluginManager().registerEvents(BlockListener(), this)

        Bukkit.getPluginManager().registerEvents(ChatListener(), this)

        registerCommands()
        registerCommandCompletions()

        ChunkOutlineTask(this).runTaskTimer(this, 20L, 20L)
        ScoreboardTask(this).runTaskTimer(this, 40L, 40L)

        registerSpells()

        if(hiddenConfig.botEnabled) {
            registerBot()
        }

        database.getGuildIdsAsync() {
            for (guildId in it) {
                database.getGuildPortalLocationAsync(guildId) {portalLoc ->
                    if(portalLoc != null) {
                        Bukkit.getScheduler().runTask(this, Runnable {
                            portalManager.addGuildPortal(guildId, Portal(portalLoc, database.getGuildNameAsync(guildId) ?: ""))
                        })
                    }
                }
            }
        }

    }

    override fun onDisable() {
        commandManager.unregisterCommands()

    }

    private fun registerSpells() {

        magicManager.registerSpell(FireballSpellT1())
        magicManager.registerSpell(FireballSpellT2())
        magicManager.registerSpell(FireballSpellT3())
        magicManager.registerSpell(BlinkSpell())
        magicManager.registerSpell(WaterSurgeSpellT1())
        magicManager.registerSpell(WaterSurgeSpellT2())
        magicManager.registerSpell(WaterSurgeSpellT3())
        //magicManager.registerSpell(RadianceSpell::class.java)
        magicManager.registerSpell(IceBlastSpellT1())
        magicManager.registerSpell(IceBlastSpellT2())
        magicManager.registerSpell(IceBlastSpellT3())

        //magicManager.registerSpell(SmeltOreSpell::class.java)
        magicManager.registerSpell(TeleportSpell())

    }

    private fun registerCommands() {
        commandManager.registerCommand(EconomyCommand())
        commandManager.registerCommand(GuildCommand())
        commandManager.registerCommand(AdminCommand())
        commandManager.registerCommand(DiscordCommand())
        commandManager.registerCommand(MasteriesCommand())
    }

    private fun registerCommandCompletions() {
        commandManager.commandCompletions.registerAsyncCompletion("currency-type") { listOf("bank", "pouch") }
        commandManager.commandCompletions.registerAsyncCompletion("masteries") { Mastery.masteries.map { it.name } }

        refreshGuildNamesCommandCompletions()
    }

    fun refreshGuildNamesCommandCompletions() {

        val names = mutableListOf<String>()

        database.getGuildIdsAsync() {
            for (guildId in it) {
                names.add(database.getGuildNameAsync(guildId) ?: "")
            }
            commandManager.commandCompletions.registerAsyncCompletion("guilds") { names }
        }
    }


    private fun registerBot() {
        val botPrefix = "^"


        val bot = Bot(hiddenConfig.botToken ?: "", botPrefix)

        bot.botThread = ThreadSpigot(this)
        bot.setConsoleCommandManager(CommandSpigotManager())

        bot.setActivity("Playing", "Minecraft")

        bot.addCommand(object : ProgramCommand() {
            override fun getLabel(): String {
                return "link"
            }

            override fun run(user: User, channel: MessageChannel, guild: Guild, label: String,
                             args: MutableList<String>): Boolean {

                val roles = hiddenConfig.requiredRolesPerServer[guild.id]


                if (roles == null) {
                    channel.sendMessage("${user.asMention} : ERROR\n\n" +
                            "Something went wrong! Fuss at Zaxarner!\n" +
                            "***================***").queue()
                    return false
                }

                var canVerify = false
                for (userRole in guild.getMember(user).roles) {
                    if (roles.contains(userRole.id)) {
                        canVerify = true
                        break
                    }
                }

                if (canVerify) {
                    if (args.size != 1) {
                        channel.sendMessage("\n${user.asMention} : ERROR\n\n" +
                                "Please type ``${botPrefix}link <MC username>``\n" +
                                "***================***").queue()
                        return false
                    }

                    val username = args[0]

                    val player = Bukkit.getPlayer(username)

                    if (player == null) {
                        channel.sendMessage("\n${user.asMention} : ERROR\n\n" +
                                "That is not an online Player!\n" +
                                "***================***").queue()
                        return false
                    }

                    if (database.getDiscordId(player.uniqueId) != null) {
                        channel.sendMessage("\n${user.asMention} : ERROR\n\n" +
                                "That Minecraft Account is already linked with a Discord account!\n" +
                                "***================***").queue()
                        return false
                    }


                    channel.sendMessage("\n${user.asMention} : LINK VERIFICATION SENT\n\n" +
                            "Respond to the prompt in Minecraft to link your accounts!\n" +
                            "You have 60 seconds!\n" +
                            "***================***").queue()

                    linkingPlayers[player] = user.id
                    Bukkit.getScheduler().runTaskLater(plugin, Runnable {
                        linkingPlayers.remove(player)
                    }, 60 * 20L)

                    val message = TextComponent("Click me!")
                    message.isUnderlined = true
                    message.clickEvent = ClickEvent(ClickEvent.Action.RUN_COMMAND, "/discord link")
                    message.hoverEvent = HoverEvent(HoverEvent.Action.SHOW_TEXT, Text("Verify your Discord!"))

                    player.sendMessage("")
                    player.sendMessage("${ChatColor.GREEN}Discord Verification!")
                    player.sendMessage("${ChatColor.GRAY}Click the underlined link to verify that your Discord account " +
                            "is ${ChatColor.RED}${user.name}${ChatColor.GRAY}!")
                    player.spigot().sendMessage(message)
                    player.sendMessage("")
                    player.sendMessage("${ChatColor.GRAY}If that is not your Discord account," +
                            " you can ignore this message!")
                    player.sendMessage("")

                } else {
                    channel.sendMessage("\n${user.asMention} : ERROR\n\n" +
                            "You do not have the required role on Discord to link with Minecraft!\n" +
                            "***================***").queue()
                }

                return false
            }

            override fun getDescription(): String {
                return "Used to link your Discord Account with your Minecraft Account."
            }


            override fun getPermissionNeeded(): Permission {
                return Permission.MESSAGE_WRITE
            }
        })

    }

    companion object {
        val plugin: Chocolate by lazy {
            Bukkit.getServer().pluginManager.getPlugin("Chocolate") as Chocolate
        }
    }


}


