package com.berd.qscore.features.shared.api

import com.amplifyframework.datastore.generated.model.Event
import com.amplifyframework.datastore.generated.model.Location
import com.amplifyframework.datastore.generated.model.User

object Models {
    fun event(
        userSub: String,
        timestamp: String,
        lat: String,
        lng: String,
        atHome: Location,
        activity: String
    ) = Event.Builder()
        .userSub(userSub)
        .timestamp(timestamp)
        .lat(lat)
        .lng(lng)
        .atHome(atHome)
        .activity(activity)
        .build()

    fun user(
        id: String,
        sub: String,
        avatar: String
    ) = User.Builder()
        .sub(sub)
        .id(id)
        .avatar(avatar)
}
