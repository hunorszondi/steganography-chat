package com.hunorszondi.letstego.api.repository

import com.hunorszondi.letstego.api.ApiServiceInterface
import com.hunorszondi.letstego.model.apiModels.*
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.MultipartBody
import java.io.File


class ChatRepository(private val api : ApiServiceInterface) : BaseRepository() {

    suspend fun getContacts(userName: String) : BaseResponse<MutableList<ContactModel>>?{

        return safeApiCall(
            call = { api.getContacts(userName).await()},
            errorMessage = "Error Fetching Contacts"
        )

    }

    suspend fun addUserToContacts(userName: String, userToAdd: String) : BaseResponse<ContactModel>?{

        return safeApiCall(
            call = { api.addUserToContacts(userName, userToAdd).await()},
            errorMessage = "Error adding contact"
        )

    }

    suspend fun removeUserFromContacts(userName: String, userToRemove: String) : BaseResponse<String>?{

        return safeApiCall(
            call = { api.removeUserFromContacts(userName, userToRemove).await()},
            errorMessage = "Error removing contact"
        )

    }

    suspend fun getAllMessages(fromUserName: String, toUserName: String) : BaseResponse<ConversationContentModel>?{

        return safeApiCall(
            call = { api.getAllMessages(fromUserName, toUserName).await()},
            errorMessage = "Error Fetching Messages"
        )

    }

    suspend fun seenAllMessages(fromUserName: String, toUserName: String) : BaseResponse<String>?{

        return safeApiCall(
            call = { api.seenAllMessages(fromUserName, toUserName).await()},
            errorMessage = "Error setting data"
        )

    }

    suspend fun sendSimpleMessage(message: MessageRequestModel) : BaseResponse<MessageModel>?{

        return safeApiCall(
            call = { api.sendSimpleMessage(message.conversationId, message).await()},
            errorMessage = "Error sending simple message failed"
        )

    }

    suspend fun sendImageMessage(message: MessageRequestModel) : BaseResponse<MessageModel>? {

        val photoFile = File(message.file)
        val fileReqBody = RequestBody.create(MediaType.parse("image/*"), photoFile)
        val imagePart = MultipartBody.Part.createFormData("photo", photoFile.name, fileReqBody)

        val thumbnailFile = File(message.thumbnail)
        val thumbnailReqBody = RequestBody.create(MediaType.parse("image/*"), thumbnailFile)
        val thumbnailPart = MultipartBody.Part.createFormData("thumbnail", thumbnailFile.name, thumbnailReqBody)

        val authorNameReqBody = RequestBody.create(MediaType.parse("text/plain"), message.authorName)
        val contentReqBody = RequestBody.create(MediaType.parse("text/plain"), message.content)

        return safeApiCall(
            call = { api.sendImageMessage(message.conversationId,
                imagePart,
                thumbnailPart,
                authorNameReqBody,
                contentReqBody).await() },
            errorMessage = "Error sending image message failed"
        )
    }

}