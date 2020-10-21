package io.github.jamessoda.mc.chocolate.db.entities

import io.github.jamessoda.mc.chocolate.db.entities.guild.GuildEntity
import org.bson.types.ObjectId
import org.mongodb.morphia.annotations.*
import java.util.*

@Entity(value = "User", noClassnameStored = true)
class UserEntity {

    @Id
    var id: ObjectId? = null

    @Indexed(options = IndexOptions(unique = true))
    var uuid: String? = null

    @Indexed
    var username: String? = null

    var ip: Int? = null

    var balance: Int? = null

    var guild: ObjectId? = null

    @Property("player_kills")
    var playerKills: Int? = null
    var deaths: Int? = null

    @Property("register_date")
    var registerDate: Date? = null

    @Property("last_seen_date")
    var lastSeenDate: Date? = null

    @Property("ip_history")
    var ipHistory: List<String>? = null

    @Property("name_history")
    var nameHistory: List<String>? = null

}