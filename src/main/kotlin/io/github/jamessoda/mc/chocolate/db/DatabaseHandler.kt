package io.github.jamessoda.mc.chocolate.db

import com.mongodb.MongoClient
import com.mongodb.WriteConcern
import io.github.jamessoda.mc.chocolate.db.entities.guild.GuildDAO
import io.github.jamessoda.mc.chocolate.db.entities.guild.GuildEntity
import io.github.jamessoda.mc.chocolate.db.entities.UserDAO
import io.github.jamessoda.mc.chocolate.db.entities.UserEntity
import org.bson.types.ObjectId
import org.bukkit.Bukkit
import org.bukkit.OfflinePlayer
import org.bukkit.entity.Player
import org.mongodb.morphia.Datastore
import org.mongodb.morphia.Morphia
import org.mongodb.morphia.query.Query
import java.util.*

class DatabaseHandler() {

    private val mongoClient: MongoClient = MongoClient()
    private val morphia: Morphia = Morphia()

    private val datastore: Datastore
    private val userDAO: UserDAO
    private val guildDAO: GuildDAO

    init {
        morphia.map(UserEntity::class.java)
        morphia.map(GuildEntity::class.java)
        datastore = morphia.createDatastore(mongoClient, "chocolate_db")
        userDAO = UserDAO(datastore)
        guildDAO = GuildDAO(datastore)
        datastore.ensureIndexes()

    }

    fun playerHasUser(player: Player) : Boolean {
        return userDAO.findOne("uuid", player.uniqueId.toString()) != null
    }

    fun getUserFromPlayer(player: Player) : UserEntity {
        var user: UserEntity? = userDAO.findOne("uuid", player.uniqueId.toString())

        if(user == null) {
            user = UserEntity()
            user.uuid = player.uniqueId.toString()
            user.username = player.name

            user.registerDate = Date(System.currentTimeMillis())
            user.balance = 0
            userDAO.save(user)
        }
        return user
    }

    fun getOfflinePlayerFromUser(user: UserEntity) : OfflinePlayer {
        return Bukkit.getOfflinePlayer(UUID.fromString(user.uuid))
    }

    fun getUserFromId(id: ObjectId) : UserEntity {
        return userDAO.findOne("_id", id)
    }

    fun getGuild(player: Player) : GuildEntity? {
        return getGuild(getUserFromPlayer(player))
    }

    fun getGuild(user: UserEntity) : GuildEntity? {
        return guildDAO.findOne("id", user.guild)
    }

    fun saveUser(user: UserEntity) {
        userDAO.save(user, WriteConcern.ACKNOWLEDGED)
    }

    fun saveGuild(guild: GuildEntity) {
        guildDAO.save(guild, WriteConcern.ACKNOWLEDGED)
    }

    fun createUserQuery() : Query<UserEntity> {
        return datastore.createQuery(UserEntity::class.java)
    }
}