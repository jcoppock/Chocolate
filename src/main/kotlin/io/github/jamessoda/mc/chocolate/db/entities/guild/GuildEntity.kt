package io.github.jamessoda.mc.chocolate.db.entities.guild

import io.github.jamessoda.mc.chocolate.db.entities.UserEntity
import org.bson.types.ObjectId
import org.mongodb.morphia.annotations.*
import java.util.*

@Entity(value = "Guild", noClassnameStored = true)
class GuildEntity {

    @Id
    var id: ObjectId? = null

    @Indexed(options = IndexOptions(unique = true))
    var name: String? = null

    @Indexed(options = IndexOptions(unique = true))
    var tag: String? = null

    var users: List<ObjectId>? = null

    var owner: ObjectId? = null

    @Property("created_date")
    var createdDate: Date? = null
}