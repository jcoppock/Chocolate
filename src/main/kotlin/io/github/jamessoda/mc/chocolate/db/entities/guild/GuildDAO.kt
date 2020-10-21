package io.github.jamessoda.mc.chocolate.db.entities.guild

import org.mongodb.morphia.Datastore
import org.mongodb.morphia.dao.BasicDAO

class GuildDAO(ds: Datastore)
    : BasicDAO<GuildEntity, String>(GuildEntity::class.java, ds) {}