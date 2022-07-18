package com.hunorszondi.letstego.model.apiModels

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class MessageModel(
    @Expose
    @SerializedName("_id")
    val messageId: String?,
    @Expose
    val conversationId: String,
    @Expose
    val authorName: String,
    @Expose
    val content: String,
    @Expose
    val photo: String?,
    @Expose
    val thumbnail: String?,
    @Expose
    val date: Long
)