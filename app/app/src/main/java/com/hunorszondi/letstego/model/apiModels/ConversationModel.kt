package com.hunorszondi.letstego.model.apiModels

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ConversationModel(
    @Expose
    @SerializedName("_id")
    val conversationId: String,
    @Expose
    val members: MutableList<String>
)