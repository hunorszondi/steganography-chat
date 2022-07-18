package com.hunorszondi.letstego.model.apiModels

import com.google.gson.annotations.Expose
import java.io.File

data class UpdateRequestModel(
    @Expose
    var displayName: String?,
    @Expose
    var password: String?,
    @Expose
    var email: String?,
    @Expose
    var photo: String?
) {
    constructor(): this(null, null, null, null)
}




