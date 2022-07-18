package com.hunorszondi.letstego.model.apiModels

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

data class UserModel(
    @Expose
    @SerializedName("_id")
    val userId: String,
    @Expose
    val userName: String,
    @Expose
    val displayName: String,
    @Expose
    var password: String,
    @Expose
    val email: String,
    @Expose
    val photo: String?,
    @Expose
    val contacts: ArrayList<ContactModel>,
    @Expose
    var token: String

)




