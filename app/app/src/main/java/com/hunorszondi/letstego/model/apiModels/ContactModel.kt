package com.hunorszondi.letstego.model.apiModels

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import com.hunorszondi.letstego.R
import com.hunorszondi.letstego.utils.ResourceUtil

data class ContactModel(
    @Expose
    val userName: String,
    @Expose
    val details: ContactDetail?,
    @Expose
    var lastMessage: String?,
    @Expose
    var lastMessageDate: Long?,
    @Expose
    var hasUnreadMessages: Boolean?
)




