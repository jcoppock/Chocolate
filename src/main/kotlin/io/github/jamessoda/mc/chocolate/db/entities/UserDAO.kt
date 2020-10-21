package io.github.jamessoda.mc.chocolate.db.entities

import org.mongodb.morphia.Datastore
import org.mongodb.morphia.dao.BasicDAO

class UserDAO(ds: Datastore)
    : BasicDAO<UserEntity, String>(UserEntity::class.java, ds) {}