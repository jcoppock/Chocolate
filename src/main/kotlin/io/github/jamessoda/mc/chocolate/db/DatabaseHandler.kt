package io.github.jamessoda.mc.chocolate.db

import com.mongodb.MongoClient
import com.mongodb.WriteConcern
import io.github.jamessoda.mc.chocolate.Chocolate.Companion.plugin
import io.github.jamessoda.mc.chocolate.utils.extensions.getDisplayName
import io.github.jamessoda.mc.chocolate.utils.extensions.getId
import org.bson.types.ObjectId
import org.bukkit.*
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.mongodb.morphia.Datastore
import org.mongodb.morphia.Morphia
import org.mongodb.morphia.annotations.*
import org.mongodb.morphia.dao.BasicDAO
import org.mongodb.morphia.query.FindOptions
import org.mongodb.morphia.query.Query
import java.util.*
import java.util.function.Consumer
import java.util.logging.Level


class DatabaseHandler {

    private val mongoClient: MongoClient = MongoClient()
    private val morphia: Morphia = Morphia()

    private val datastore: Datastore
    private val userDAO: UserDAO
    private val guildDAO: GuildDAO
    private val chunkDAO: ChunkDAO
    private val orderDAO: OrderDAO

    private val userCache = mutableMapOf<UUID, UserEntity>()
    private val guildCache = mutableMapOf<ObjectId, GuildEntity>()
    private val chunkCache = mutableMapOf<Chunk, ChunkEntity>()

    private val dirtyUsers = mutableSetOf<UUID>()
    private val dirtyGuilds = mutableSetOf<ObjectId>()
    private val dirtyChunks = mutableSetOf<Chunk>()

    init {
        morphia.map(UserEntity::class.java)
        morphia.map(GuildEntity::class.java)
        morphia.map(ChunkEntity::class.java)
        morphia.map(OrderEntity::class.java)
        datastore = morphia.createDatastore(mongoClient, plugin.chocolateConfig.databaseName)
        userDAO = UserDAO(datastore)
        guildDAO = GuildDAO(datastore)
        chunkDAO = ChunkDAO(datastore)
        orderDAO = OrderDAO(datastore)
        datastore.ensureIndexes()

        Bukkit.getScheduler().runTaskTimerAsynchronously(plugin, Runnable {

            if (dirtyUsers.isNotEmpty() || dirtyGuilds.isNotEmpty() || dirtyChunks.isNotEmpty()) {
                Bukkit.getLogger().log(Level.INFO, "Saving Dirty Database Objects...")

                val userIterator = dirtyUsers.iterator()
                while (userIterator.hasNext()) {
                    val uuid = userIterator.next()
                    userDAO.save(getUser(uuid))

                    userIterator.remove()
                }

                val guildIterator = dirtyGuilds.iterator()
                while (guildIterator.hasNext()) {
                    val guildId = guildIterator.next()
                    val guildEntity = getGuild(guildId)
                    if (guildEntity != null) {
                        guildDAO.save(getGuild(guildId))
                    } else {
                        Bukkit.getLogger().log(Level.SEVERE, "DatabaseHandler Task tried to save a null Guild!")
                    }

                    guildIterator.remove()
                }

                val chunkIterator = dirtyChunks.iterator()
                while (chunkIterator.hasNext()) {
                    val chunk = chunkIterator.next()
                    val chunkEntity = getChunk(chunk)
                    if (chunkEntity != null) {
                        chunkDAO.save(getChunk(chunk))
                    } else {
                        Bukkit.getLogger().log(Level.SEVERE, "DatabaseHandler Task tried to save a null Chunk!")
                    }

                    chunkIterator.remove()
                }
            }

        }, 5 * 60 * 20L, 5 * 60 * 20L)
    }

    //region USER/PLAYER

    private fun getUser(uuid: UUID): UserEntity? {
        val userEntity = userCache[uuid]
        return if (userEntity != null) {
            userEntity
        } else {
            plugin.logger.log(Level.SEVERE, "Attempted to getUser() from cache without caching user first!")
            plugin.logger.log(Level.SEVERE, Exception().message)
            null
        }
    }

    private fun getUserAsync(uuid: UUID, callback: Consumer<UserEntity?>) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            val userEntity = userDAO.findOne("uuid", uuid)

            callback.accept(userEntity)
        })
    }

    private fun getUserSync(uuid: UUID): UserEntity? {
        return userDAO.findOne("uuid", uuid)
    }

    fun forceSaveUser(uuid: UUID) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            userDAO.save(getUser(uuid))
            dirtyUsers.remove(uuid)
        })
    }

    fun loadUser(uuid: UUID) {
        if (userCache.containsKey(uuid)) {
            return
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            var userEntity = userDAO.findOne("uuid", uuid)

            if (userEntity == null) {
                userEntity = UserEntity()
                userEntity.uuid = uuid
                userEntity.username = Bukkit.getOfflinePlayer(uuid).name

                userEntity.registerDate = Date(System.currentTimeMillis())

                userEntity.economyInfo = UserEconomyInfo.createNew()
                userEntity.masteriesInfo = UserMasteriesInfo.createNew()
                userDAO.save(userEntity)
            }

            userCache[uuid] = userEntity
        })
    }

    fun refreshUserInfo(player: Player) {
        val userEntity = getUser(player.uniqueId)

        if (userEntity == null) {
            Bukkit.getLogger().log(Level.SEVERE, "Tried to refreshUserInfo() when no UserEntity exists!")
            return
        }

        userEntity.lastSeenDate = Date(System.currentTimeMillis())

        if (userEntity.username != player.name) {
            userEntity.username = player.name
        }

        val nameHistory = userEntity.nameHistory?.toMutableList() ?: mutableListOf()
        if (!nameHistory.contains(player.name)) {
            nameHistory.add(player.name)
            userEntity.nameHistory = nameHistory
        }

        val ipHistory = userEntity.ipHistory?.toMutableList() ?: mutableListOf()
        if (!ipHistory.contains(player.address?.address.toString())) {
            ipHistory.add(player.address?.address.toString())
            userEntity.ipHistory = ipHistory
        }

        queueUserSave(player.uniqueId)
    }

    fun refreshLastSeen(player: Player) {
        val userEntity = getUser(player.uniqueId) ?: return
        userEntity.lastSeenDate = Date(System.currentTimeMillis())
        queueUserSave(player.uniqueId)
    }

    fun queueUserSave(uuid: UUID) {
        dirtyUsers.add(uuid)
    }

    private fun queueUserSave(user: UserEntity) {
        val uuid = user.uuid ?: return
        dirtyUsers.add(uuid)
    }

    fun getGuildIdOfPlayer(uuid: UUID): ObjectId? {
        return getUser(uuid)?.guild
    }

    fun getGuildIdOfPlayer(player: Player): ObjectId? {
        return getGuildIdOfPlayer(player.uniqueId)
    }

    fun isNewPlayerAsync(uuid: UUID, callback: Consumer<Boolean>) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            val userEntity = userDAO.findOne("uuid", uuid)

            callback.accept(userEntity?.lastSeenDate == null)
        })
    }

    fun getCoinPouch(uuid: UUID): Int {
        return getUser(uuid)?.economyInfo?.coinPouch ?: 0
    }

    fun getCoinPouch(player: Player): Int {
        return getCoinPouch(player.uniqueId)
    }

    fun setCoinPouch(uuid: UUID, amount: Int) {
        val user = getUser(uuid) ?: return

        val ecoInfo = user.economyInfo ?: UserEconomyInfo.createNew()

        if (amount < 0) {
            ecoInfo.coinPouch = 0
        } else {
            ecoInfo.coinPouch = amount
        }

        ecoInfo.totalMoney = (ecoInfo.bankBalance ?: 0) + (ecoInfo.coinPouch ?: 0)
        user.economyInfo = ecoInfo
        queueUserSave(user)
    }

    fun setCoinPouch(player: Player, amount: Int) {
        setCoinPouch(player.uniqueId, amount)
    }

    fun addCoins(uuid: UUID, amount: Int) {
        setCoinPouch(uuid, getCoinPouch(uuid) + amount)
    }

    fun addCoins(player: Player, amount: Int) {
        addCoins(player.uniqueId, amount)
    }

    fun getBankBalance(uuid: UUID): Int {
        return getUser(uuid)?.economyInfo?.bankBalance ?: 0
    }

    fun getBankBalance(player: Player): Int {
        return getBankBalance(player.uniqueId)
    }

    fun setBankBalance(uuid: UUID, amount: Int) {
        val user = getUser(uuid) ?: return

        val ecoInfo = user.economyInfo ?: UserEconomyInfo.createNew()

        if (amount < 0) {
            ecoInfo.bankBalance = 0
        } else {
            ecoInfo.bankBalance = amount
        }

        ecoInfo.totalMoney = (ecoInfo.bankBalance ?: 0) + (ecoInfo.coinPouch ?: 0)
        user.economyInfo = ecoInfo
        queueUserSave(user)
    }

    fun setBankBalance(player: Player, amount: Int) {
        setBankBalance(player.uniqueId, amount)
    }

    fun addBankBalance(uuid: UUID, amount: Int) {
        setBankBalance(uuid, getBankBalance(uuid) + amount)
    }

    fun addBankBalance(player: Player, amount: Int) {
        addBankBalance(player.uniqueId, amount)
    }

    fun getTotalMoney(uuid: UUID): Int {
        val user = getUser(uuid) ?: return 0

        val ecoInfo = user.economyInfo ?: return 0

        return ecoInfo.totalMoney ?: (getBankBalance(uuid) + getCoinPouch(uuid))
    }

    fun getTotalMoney(player: Player): Int {
        return getTotalMoney(player.uniqueId)
    }

    fun setDiscordId(uuid: UUID, discordId: String?) {
        val user = getUser(uuid) ?: return
        user.discordId = discordId
        queueUserSave(user)
    }

    fun getDiscordId(uuid: UUID): String? {
        return getUser(uuid)?.discordId
    }

    fun getMasteryLevel(uuid: UUID, masteryName: String): Int {
        val user = getUser(uuid)

        when (masteryName.toLowerCase().trim()) {
            "magic" -> return user?.masteriesInfo?.magicMastery ?: 0
        }

        return 0
    }

    fun setMasteryLevel(uuid: UUID, masteryName: String, mastery: Int) {
        val user = getUser(uuid) ?: return

        when (masteryName.toLowerCase().trim()) {
            "magic" -> {
                user.masteriesInfo?.magicMastery = mastery
                queueUserSave(user)
            }
        }
    }

    //endregion

    //region GUILDS/CHUNKS

    fun createGuild(owner: Player, name: String, tag: String) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            val guild = GuildEntity()

            guild.owner = owner.uniqueId
            guild.name = name
            guild.tag = tag
            guild.createdDate = Date(System.currentTimeMillis())
            val users = listOf(owner.uniqueId)
            guild.users = users
            guildDAO.save(guild)
            guildCache[guild.id!!] = guild

            queueGuildSave(guild.id!!)

            val user = getUser(owner.uniqueId) ?: return@Runnable
            user.guild = guild.id!!
            queueUserSave(user)
        })
    }

    private fun getGuild(guildId: ObjectId): GuildEntity? {
        val guildEntity = guildCache[guildId]
        return if (guildEntity != null) {
            guildEntity
        } else {
            Bukkit.getLogger().log(Level.SEVERE, "Attempted to getGuild() from cache without caching guild first!")
            plugin.logger.log(Level.SEVERE, Exception().message)
            null
        }
    }

    private fun getGuildAsync(guildId: ObjectId, callback: Consumer<GuildEntity?>) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            val guildEntity = guildDAO.findOne("id", guildId)

            callback.accept(guildEntity)
        })
    }

    private fun getGuildSync(guildId: ObjectId): GuildEntity? {
        return guildDAO.findOne("id", guildId)
    }

    fun loadGuild(chunk: Chunk) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            val chunkEntity = chunkDAO.findOne(chunkDAO.createQuery()
                    .field("world").equal(chunk.world.name)
                    .field("x").equal(chunk.x.toString())
                    .field("z").equal(chunk.z.toString())) ?: return@Runnable

            val chunkOwnerId = chunkEntity.guild

            if (chunkOwnerId != null) {
                loadGuild(chunkOwnerId)
            }
        })
    }

    fun loadGuild(guildId: ObjectId) {
        if (guildCache.containsKey(guildId)) {
            return
        }
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            val guildEntity = guildDAO.findOne("id", guildId)

            if (guildEntity != null) {
                guildCache[guildId] = guildEntity
            }
        })
    }

    fun queueGuildSave(guildId: ObjectId) {
        dirtyGuilds.add(guildId)
    }

    private fun getChunk(chunk: Chunk): ChunkEntity? {
        val chunkEntity = chunkCache[chunk]
        return chunkEntity
    }

    private fun getChunk(chunkId: ObjectId): ChunkEntity? {
        for (c in chunkCache.values) {
            if (c.id == chunkId) {
                return c
            }
        }
        return null
    }

    fun loadChunk(chunk: Chunk) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            val chunkEntity = chunkDAO.findOne(chunkDAO.createQuery()
                    .field("world").equal(chunk.world.name)
                    .field("x").equal(chunk.x.toString())
                    .field("z").equal(chunk.z.toString())) ?: return@Runnable


            val chunkId = chunkEntity.id

            if (chunkEntity.guild == null || chunkId == null) {
                chunkDAO.delete(chunkEntity)
            } else {
                chunkCache[chunk] = chunkEntity
            }
        })
    }

    fun queueChunkSave(chunk: Chunk) {
        dirtyChunks.add(chunk)
    }

    fun getChunkOwner(chunk: Chunk): ObjectId? {
        return getChunk(chunk)?.guild
    }

    fun getGuildName(guildId: ObjectId): String {
        return getGuild(guildId)?.name ?: ""
    }

    fun getGuildNameAsync(guildId: ObjectId): String? {
        return guildDAO.findOne("id", guildId)?.name
    }

    fun getGuildTag(guildId: ObjectId): String {
        return getGuild(guildId)?.tag ?: ""
    }

    fun getGuildPortalLocation(guildId: ObjectId): Location? {
        return getGuild(guildId)?.spawn?.getLocation()
    }

    fun getGuildPortalLocationAsync(guildId: ObjectId, callback: Consumer<Location?>) {
        getGuildAsync(guildId) {
            callback.accept(it?.spawn?.getLocation())
        }
    }

    fun setGuildPortalLocation(guildId: ObjectId, location: LocationInfo?) {
        val guild = getGuild(guildId) ?: return

        guild.spawn = location
        queueGuildSave(guildId)
    }

    fun getChunks(guildId: ObjectId): List<Chunk> {
        val guild = getGuild(guildId) ?: return listOf()

        val result = mutableListOf<Chunk>()

        for (chunkId in guild.chunks ?: listOf()) {
            val chunk = getChunk(chunkId)?.getChunk() ?: continue
            result.add(chunk)
        }

        return result
    }

    fun getUUIDsInGuild(guildId: ObjectId): List<UUID> {
        return getGuild(guildId)?.users ?: listOf()
    }

    fun getGuildCreationDate(guildId: ObjectId): Date? {
        return getGuild(guildId)?.createdDate
    }

    fun getGuildIdsAsync(callback: Consumer<List<ObjectId>>) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            val query: Query<GuildEntity> = datastore.find(GuildEntity::class.java)

            val list = mutableListOf<ObjectId>()

            query.fetch().forEach {
                it.id?.let { it1 -> list.add(it1) }
            }

            callback.accept(list)
        })
    }

    fun getGuildMoneySync(guildId: ObjectId): Int {
        val guildEntity = getGuildSync(guildId)
        if (guildEntity == null) {
            return 0
        }

        var money = 0
        for (u in guildEntity.users ?: listOf()) {
            money += (getUserSync(u)?.economyInfo?.totalMoney ?: 0)
        }

        return money
    }

    fun getGuildOwner(guildId: ObjectId): UUID? {
        return getGuild(guildId)?.owner
    }

    fun setGuildOwner(guildId: ObjectId, uuid: UUID) {
        val guild = getGuild(guildId) ?: return

        guild.owner = uuid
        queueGuildSave(guildId)
    }

    fun addPlayerToGuild(player: Player, guildId: ObjectId) {
        val guild = getGuild(guildId)
        if (guild != null) {
            val users = guild.users?.toMutableList() ?: mutableListOf()
            users.add(player.uniqueId)

            guild.users = users
            queueGuildSave(guildId)

            val user = getUser(player.uniqueId)
            user?.guild = guildId
            queueUserSave(player.uniqueId)
        }
    }

    fun removePlayerFromGuild(player: Player) {
        val guildId = getGuildIdOfPlayer(player) ?: return
        val guildEntity = getGuild(guildId) ?: return


        val users = guildEntity.users?.toMutableList() ?: mutableListOf()
        if (users.contains(player.uniqueId)) {
            users.remove(player.uniqueId)

            guildEntity.users = users
            queueGuildSave(guildId)

            val user = getUser(player.uniqueId) ?: return
            user.guild = null
            queueUserSave(user)
        }
    }

    fun addLeader(guildId: ObjectId, uuid: UUID) {
        val guild = getGuild(guildId) ?: return

        val leaders = guild.leaders?.toMutableList() ?: mutableListOf()

        leaders.add(uuid)
        guild.leaders = leaders
        queueGuildSave(guildId)
    }

    fun removeLeader(guildId: ObjectId, uuid: UUID) {
        val guild = getGuild(guildId) ?: return

        val leaders = guild.leaders?.toMutableList() ?: mutableListOf()

        leaders.remove(uuid)
        guild.leaders = leaders
        queueGuildSave(guildId)
    }

    fun getLeaders(guildId: ObjectId): List<UUID> {
        return getGuild(guildId)?.leaders ?: listOf()
    }

    fun isLeaderOrOwner(player: Player): Boolean {
        val guildId = getGuildIdOfPlayer(player) ?: return false

        return getGuildOwner(guildId) == player.uniqueId || getLeaders(guildId).contains(player.uniqueId)
    }

    fun claimChunk(guildId: ObjectId, chunk: Chunk) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            var chunkEntity = getChunk(chunk)
            val guild = getGuild(guildId) ?: return@Runnable

            if (chunkEntity == null) {
                chunkEntity = ChunkEntity()
                chunkEntity.world = chunk.world.name
                chunkEntity.x = chunk.x
                chunkEntity.z = chunk.z
                chunkDAO.save(chunkEntity)
            }
            if (chunkEntity.guild == null) {
                chunkEntity.guild = guildId
                chunkDAO.save(chunkEntity)

                val chunks = guild.chunks?.toMutableList() ?: mutableListOf()
                chunks.add(chunkEntity.id!!)

                guild.chunks = chunks
                guildDAO.save(guild)

            }
        })
    }

    fun unclaimChunk(guildId: ObjectId, chunk: Chunk) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            var chunkEntity = getChunk(chunk) ?: return@Runnable
            val guild = getGuild(guildId) ?: return@Runnable

            if (chunkEntity.guild != guildId) {
                return@Runnable
            }

            chunkEntity.guild = null
            queueChunkSave(chunk)

            val chunks = guild.chunks?.toMutableList() ?: return@Runnable

            if (chunks.contains(chunkEntity.id)) {
                chunks.remove(chunkEntity.id)
                guild.chunks = chunks
                queueGuildSave(guildId)
            }
        })
    }

    //endregion

    //region BUY & SELL ORDERS

    private fun getOrder(orderId: ObjectId): OrderEntity? {
        return orderDAO.findOne("id", orderId)
    }

    fun createBuyOrder(uuid: UUID, item: ItemStack, amount: Int, price: Int) {

        // Don't move this to Async portion. It uses Bukkit API to access ItemStack persistent data which is not thread-safe
        val itemName = if (item.getId() != null) {
            "${item.getId()!!}"
        } else {
            item.type.name
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            getCurrentBuyOrdersAsync(uuid) {
                if (it.size < 6) {
                    val orderEntity = OrderEntity()
                    orderEntity.buyer = uuid
                    orderEntity.item = itemName
                    orderEntity.amount = amount
                    orderEntity.price = price
                    orderEntity.placedDate = Date()
                    orderEntity.buying = true

                    orderDAO.save(orderEntity)
                }
            }
        })
    }

    fun createSellOrder(uuid: UUID, item: ItemStack, price: Int) {

        // Don't move this to Async portion. It uses Bukkit API to access ItemStack persistent data which is not thread-safe
        val itemName = if (item.getId() != null) {
            "${item.getId()!!}"
        } else {
            item.type.name
        }

        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            getCurrentSellOrdersAsync(uuid) {
                if (it.size < 6) {
                    val orderEntity = OrderEntity()
                    orderEntity.seller = uuid
                    orderEntity.item = itemName
                    orderEntity.amount = item.amount
                    orderEntity.price = price
                    orderEntity.placedDate = Date()
                    orderEntity.buying = false

                    orderDAO.save(orderEntity)
                }
            }
        })
    }

    fun getItemNameSync(orderId: ObjectId): String? {
        return getOrder(orderId)?.item
    }

    fun getItemAmountSync(orderId: ObjectId): Int? {

        return getOrder(orderId)?.amount
    }

    fun getItemPriceSync(orderId: ObjectId): Int? {

        return getOrder(orderId)?.price
    }

    fun getSellerSync(orderId: ObjectId): UUID? {
        return getOrder(orderId)?.seller
    }

    fun setSellerSync(orderId: ObjectId, seller: UUID) {
        val order = getOrder(orderId) ?: return
        order.seller = seller
        if (order.seller != null && order.buyer != null) {
            order.completedDate = Date(System.currentTimeMillis())
        }
        orderDAO.save(order)
    }

    fun setCollectedSync(orderId: ObjectId) {
        val order = getOrder(orderId) ?: return

        order.collected = true
        orderDAO.save(order)
    }

    fun getBuyerSync(orderId: ObjectId): UUID? {
        return getOrder(orderId)?.buyer
    }

    fun setBuyerSync(orderId: ObjectId, buyer: UUID) {
        val order = getOrder(orderId) ?: return
        order.buyer = buyer
        if (order.buyer != null && order.seller != null) {
            order.completedDate = Date(System.currentTimeMillis())
        }
        orderDAO.save(order)
    }

    fun getDatePlacedSync(orderId: ObjectId): Date? {
        return getOrder(orderId)?.placedDate
    }

    fun deleteOrder(orderId: ObjectId) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            datastore.findAndDelete(orderDAO.createQuery().field("id").equal(orderId))
        })
    }

    fun getUnfilledBuyOrdersOfOthersAsync(uuid: UUID, amount: Int, page: Int, search: String? = null, callback: Consumer<List<ObjectId>>) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            val query = orderDAO.createQuery()
            query.or(query.criteria("buying").equal(true), query.criteria("buying").equal(null))
                    .and(query.criteria("buyer").notEqual(uuid),
                            query.criteria("seller").equal(null))

            val entityList = query.asList(FindOptions().limit(amount).skip(page * amount))
                    .sortedWith { o1, o2 -> o1.placedDate!!.compareTo(o2.placedDate) }


            val result = mutableListOf<ObjectId>()

            for (entity in entityList) {
                if (search != null) {
                    val itemName = entity.item ?: continue
                    if (itemName.toIntOrNull() != null) {
                        val item = plugin.itemsConfig.getItem(itemName.toInt())
                        val realName = ChatColor.stripColor(item?.getDisplayName()) ?: continue

                        if (realName.toLowerCase().replace("_", " ").contains(search.toLowerCase())) {
                            result.add(entity.id!!)
                        }
                    } else {
                        if (itemName.toLowerCase().replace("_", " ").contains(search.toLowerCase())) {
                            result.add(entity.id!!)
                        }
                    }

                } else {
                    result.add(entity.id!!)
                }
            }

            callback.accept(result)
        })
    }

    fun getCurrentBuyOrdersAsync(uuid: UUID, callback: Consumer<List<ObjectId>>) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            val query = orderDAO.createQuery()
            query.or(query.criteria("buying").equal(true), query.criteria("buying").equal(null))
                    .and(query.criteria("buyer").equal(uuid),
                            query.criteria("collected").equal(false))

            val entityList = query.asList().sortedWith { o1, o2 -> o1.placedDate!!.compareTo(o2.placedDate) }

            val result = mutableListOf<ObjectId>()

            for (entity in entityList) {
                result.add(entity.id!!)
            }

            callback.accept(result)
        })
    }

    fun hasUncollectedBuyOrdersAsync(uuid: UUID, callback: Consumer<Boolean>) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            val query = orderDAO.createQuery()
            query.or(query.criteria("buying").equal(true), query.criteria("buying").equal(null))
                    .and(query.criteria("buyer").equal(uuid),
                            query.criteria("seller").notEqual(null),
                            query.criteria("collected").equal(false))

            val entityList = query.asList()

            callback.accept(entityList.isNotEmpty())
        })
    }

    fun getUnfilledSellOrdersOfOthersAsync(uuid: UUID, amount: Int, page: Int, search: String? = null, callback: Consumer<List<ObjectId>>) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            val entityList = datastore.createQuery(OrderEntity::class.java)
                    .field("buying").equal(false)
                    .field("seller").notEqual(uuid)
                    .field("buyer").equal(null)
                    .asList(FindOptions().limit(amount).skip(page * amount))
                    .sortedWith { o1, o2 -> o1.placedDate!!.compareTo(o2.placedDate) }


            val result = mutableListOf<ObjectId>()

            for (entity in entityList) {
                if (search != null) {
                    val itemName = entity.item ?: continue
                    if (itemName.toIntOrNull() != null) {
                        val item = plugin.itemsConfig.getItem(itemName.toInt())
                        val realName = ChatColor.stripColor(item?.getDisplayName()) ?: continue

                        if (realName.toLowerCase().replace("_", " ").contains(search.toLowerCase())) {
                            result.add(entity.id!!)
                        }
                    } else {
                        if (itemName.toLowerCase().replace("_", " ").contains(search.toLowerCase())) {
                            result.add(entity.id!!)
                        }
                    }
                } else {
                    result.add(entity.id!!)
                }
            }

            callback.accept(result)
        })
    }

    fun getCurrentSellOrdersAsync(uuid: UUID, callback: Consumer<List<ObjectId>>) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            val entityList = orderDAO.createQuery()
                    .field("buying").equal(false)
                    .field("seller").equal(uuid)
                    .field("collected").equal(false)
                    .sortedWith { o1, o2 -> o1.placedDate!!.compareTo(o2.placedDate) }

            val result = mutableListOf<ObjectId>()

            for (entity in entityList) {
                result.add(entity.id!!)
            }

            callback.accept(result)
        })
    }

    fun hasUncollectedSellOrdersAsync(uuid: UUID, callback: Consumer<Boolean>) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            val entityList = orderDAO.createQuery()
                    .field("buying").equal(false)
                    .field("seller").equal(uuid)
                    .field("buyer").notEqual(null)
                    .field("collected").equal(false)

            callback.accept(entityList.asList().isNotEmpty())
        })
    }
    //endregion

    //region LEADERBOARDS

    fun getMasteryTop(masteryName: String, callback: Consumer<List<UUID>>) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {

            val query = datastore.find(UserEntity::class.java)

            val topUsers = query.field("masteriesInfo.${masteryName.toLowerCase()}_mastery").greaterThan(0)
                    .order("-masteriesInfo.${masteryName.toLowerCase()}_mastery").asList(FindOptions().limit(10))

            val uuids = mutableListOf<UUID>()

            for (user in topUsers) {
                uuids.add(user.uuid!!)
            }

            callback.accept(uuids)
        })
    }

    fun getEconomyTop(callback: Consumer<List<UUID>>) {
        Bukkit.getScheduler().runTaskAsynchronously(plugin, Runnable {
            val query = datastore.find(UserEntity::class.java)

            val topUsers = query.field("economyInfo.total_money").greaterThan(0).order("-economyInfo.total_money").asList(FindOptions().limit(10))

            val uuids = mutableListOf<UUID>()

            for (user in topUsers) {
                uuids.add(user.uuid!!)
            }

            callback.accept(uuids)
        })
    }

    //endregion

}

private class ChunkDAO(ds: Datastore)
    : BasicDAO<ChunkEntity, String>(ChunkEntity::class.java, ds)

@Entity(value = "Chunk", noClassnameStored = true)
private class ChunkEntity {

    @Id
    var id: ObjectId? = null

    @Indexed
    var world: String? = null

    @Indexed
    var x: Int? = null

    @Indexed
    var z: Int? = null

    var guild: ObjectId? = null

    fun getChunk(): Chunk? {
        val world = this.world
        val x = this.x
        val z = this.z
        if (world == null || x == null || z == null) {
            return null
        }
        val realWorld = Bukkit.getWorld(world) ?: return null

        return realWorld.getChunkAt(x, z)
    }
}

@Embedded
class LocationInfo {
    var world: String? = null
    var x: Double? = null
    var y: Double? = null
    var z: Double? = null
    var pitch: Float? = null
    var yaw: Float? = null

    fun getLocation(): Location? {
        val world = this.world?.let { Bukkit.getWorld(it) } ?: return null
        val x = this.x
        val y = this.y
        val z = this.z
        val pitch = this.pitch
        val yaw = this.yaw

        if (x == null || y == null || z == null) {
            return null
        }

        if (pitch != null && yaw != null) {
            return Location(world, x, y, z, yaw, pitch)
        }

        return Location(world, x, y, z)
    }

    companion object {
        fun createNew(location: Location): LocationInfo {
            val spawnInfo = LocationInfo()

            spawnInfo.world = location.world.name
            spawnInfo.x = location.x
            spawnInfo.y = location.y
            spawnInfo.z = location.z
            spawnInfo.pitch = location.pitch
            spawnInfo.yaw = location.yaw

            return spawnInfo
        }
    }
}

private class GuildDAO(ds: Datastore)
    : BasicDAO<GuildEntity, String>(GuildEntity::class.java, ds)

@Entity(value = "Guild", noClassnameStored = true)
private class GuildEntity {

    @Id
    var id: ObjectId? = null

    @Indexed(options = IndexOptions(unique = true))
    var name: String? = null

    @Indexed(options = IndexOptions(unique = true))
    var tag: String? = null

    var users: List<UUID>? = null

    @Property("owner_uuid")
    var owner: UUID? = null

    var leaders: List<UUID>? = null

    var chunks: List<ObjectId>? = null

    @Property("created_date")
    var createdDate: Date? = null

    @Embedded
    var spawn: LocationInfo? = null
}

@Embedded
private class UserEconomyInfo {
    @Property("total_money")
    var totalMoney: Int? = null

    @Property("bank_balance")
    var bankBalance: Int? = null

    @Property("coin_pouch")
    var coinPouch: Int? = null

    companion object {
        fun createNew(): UserEconomyInfo {
            val ecoInfo = UserEconomyInfo()

            ecoInfo.bankBalance = 0
            ecoInfo.coinPouch = 0
            ecoInfo.totalMoney = 0

            return ecoInfo
        }
    }
}

@Embedded
private class UserMasteriesInfo {
    @Property("magic_mastery")
    var magicMastery: Int? = null

    companion object {
        fun createNew(): UserMasteriesInfo {
            val masteriesInfo = UserMasteriesInfo()

            masteriesInfo.magicMastery = 0

            return masteriesInfo
        }
    }
}

private class UserDAO(ds: Datastore)
    : BasicDAO<UserEntity, String>(UserEntity::class.java, ds)

@Entity(value = "User", noClassnameStored = true)
private class UserEntity {

    @Id
    var uuid: UUID? = null

    var username: String? = null

    var guild: ObjectId? = null

    @Embedded
    var economyInfo: UserEconomyInfo? = null

    @Embedded
    var masteriesInfo: UserMasteriesInfo? = null

    @Property("register_date")
    var registerDate: Date? = null

    @Property("last_seen_date")
    var lastSeenDate: Date? = null

    @Property("ip_history")
    var ipHistory: List<String>? = null

    @Property("name_history")
    var nameHistory: List<String>? = null

    @Property("discord_id")
    var discordId: String? = null
}

private class OrderDAO(ds: Datastore)
    : BasicDAO<OrderEntity, String>(OrderEntity::class.java, ds)

@Entity(value = "Order", noClassnameStored = true)
private class OrderEntity {

    @Id
    var id: ObjectId? = null

    var buying: Boolean? = true

    var buyer: UUID? = null

    var seller: UUID? = null

    var collected: Boolean = false

    /***
     * Either Bukkit Material name or Chocolate Item ID
     */
    var item: String? = null

    var amount: Int? = null

    var price: Int? = null

    @Property("date_placed")
    var placedDate: Date? = null

    @Property("date_completed")
    var completedDate: Date? = null

}

