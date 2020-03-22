package com.berd.qscore.features.shared.api

import com.amplifyframework.core.model.Model
import com.amplifyframework.datastore.generated.model.Event
import com.amplifyframework.datastore.generated.model.Location
import com.amplifyframework.datastore.generated.model.User


interface SimpleModel<T : Model> {
    val model: T
}

data class SimpleEvent(
    val userSub: String,
    val timestamp: String,
    val lat: String,
    val lng: String,
    val atHome: Location,
    val activity: String
) : SimpleModel<Event> {
    override val model
        get() = Event.Builder()
            .userSub(userSub)
            .timestamp(timestamp)
            .lat(lat)
            .lng(lng)
            .atHome(atHome)
            .activity(activity)
            .build()
}

data class SimpleUser(
    val id: String,
    val sub: String,
    val avatar: String
) : SimpleModel<User> {
    override val model
        get() = User.Builder()
            .sub(sub)
            .id(id)
            .avatar(avatar)
            .build()
}
