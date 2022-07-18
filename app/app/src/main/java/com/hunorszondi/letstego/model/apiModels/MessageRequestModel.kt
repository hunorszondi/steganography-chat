package com.hunorszondi.letstego.model.apiModels

data class MessageRequestModel(
    val conversationId: String,
    val authorName: String,
    val content: String,
    val file: String?,
    val thumbnail: String?
)