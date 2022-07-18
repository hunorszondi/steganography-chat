package com.hunorszondi.letstego.model.apiModels

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class ContactDetail(
    @Expose
    @SerializedName("_id")
    val userId: String,
    @Expose
    val userName: String,
    @Expose
    val displayName: String,
    @Expose
    val email: String,
    @Expose
    val photo: String?
)