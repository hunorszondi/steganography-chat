package com.hunorszondi.letstego.api.repository

import com.hunorszondi.letstego.api.ApiServiceInterface
import com.hunorszondi.letstego.model.apiModels.*
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class UserRepository(private val api : ApiServiceInterface): BaseRepository() {

    suspend fun authenticateUser(userName: String, password: String) : BaseResponse<UserModel>?{

        val authBody = AuthRequestModel(userName, password)

        return safeApiCall(
            call = { api.authenticateUser(authBody).await()},
            errorMessage = "Error authentication failed"
        )
    }

    suspend fun registerUser(regModel: RegisterRequestModel) : BaseResponse<String>?{

        val userResponse: BaseResponse<String>?

        if(regModel.file != null) {
            val photo = File(regModel.file)
            val fileReqBody = RequestBody.create(MediaType.parse("image/*"), photo)
            val imagePart = MultipartBody.Part.createFormData("file", photo.name, fileReqBody)

            val userNameReqBody = RequestBody.create(MediaType.parse("text/plain"), regModel.userName)
            val displayNameReqBody = RequestBody.create(MediaType.parse("text/plain"), regModel.displayName)
            val passwordReqBody = RequestBody.create(MediaType.parse("text/plain"), regModel.password)
            val emailReqBody = RequestBody.create(MediaType.parse("text/plain"), regModel.email)

            userResponse = safeApiCall(
                call = { api.registerUserWithImage(imagePart,
                    userNameReqBody,
                    displayNameReqBody,
                    passwordReqBody,
                    emailReqBody).await() },
                errorMessage = "Error registration with image failed"
            )
        } else {
            userResponse = safeApiCall(
                call = { api.registerUserWithoutImage(regModel).await() },
                errorMessage = "Error registration without image failed"
            )
        }

        return userResponse
    }

    suspend fun updateUser(userId: String, updateModel: UpdateRequestModel) : BaseResponse<UserModel>?{
        val userResponse: BaseResponse<UserModel>?

        if(updateModel.photo != null) {
            val photo = File(updateModel.photo)
            val fileReqBody = RequestBody.create(MediaType.parse("image/*"), photo)
            val imagePart = MultipartBody.Part.createFormData("file", photo.name, fileReqBody)

            val displayNameReqBody = RequestBody.create(MediaType.parse("text/plain"), updateModel.displayName?:"")
            val passwordReqBody = RequestBody.create(MediaType.parse("text/plain"), updateModel.password?:"")
            val emailReqBody = RequestBody.create(MediaType.parse("text/plain"), updateModel.email?:"")

            userResponse = safeApiCall(
                call = { api.updateUserWithImage(userId,
                    imagePart,
                    displayNameReqBody,
                    passwordReqBody,
                    emailReqBody).await() },
                errorMessage = "Error profile update with image failed"
            )
        } else {
            userResponse = safeApiCall(
                call = { api.updateUserWithoutImage(userId, updateModel).await() },
                errorMessage = "Error profile update without image failed"
            )
        }

        return userResponse
    }

    suspend fun getUserByUserName(userName: String) : BaseResponse<UserModel>?{

        return safeApiCall(
        call = { api.getUserByUserName(userName).await()},
        errorMessage = "Error fetching user by id failed"
    )
    }

    suspend fun deleteUserAccount(userId: String) : BaseResponse<String>?{

        return safeApiCall(
        call = { api.deleteUserAccount(userId).await()},
        errorMessage = "Error deleting user by id failed"
    )
    }
}