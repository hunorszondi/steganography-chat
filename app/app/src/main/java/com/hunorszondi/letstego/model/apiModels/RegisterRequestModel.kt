package com.hunorszondi.letstego.model.apiModels

import com.google.gson.annotations.Expose
import java.io.File

data class RegisterRequestModel(
    @Expose
    val userName: String,
    @Expose
    val displayName: String,
    @Expose
    val password: String,
    @Expose
    val email: String,
    @Expose
    val file: String?
)




