package com.hunorszondi.letstego.model.apiModels

data class ConversationContentModel (
    val conversationId: String,
    val messages: MutableList<MessageModel>
)