package com.hunorszondi.letstego.api

import com.hunorszondi.letstego.model.apiModels.*
import kotlinx.coroutines.Deferred
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*


/**
 * Collection of backend api endpoints
 */
interface ApiServiceInterface {

    @GET("/chat/contacts/{username}")
    fun getContacts(@Path("username") userName: String): Deferred<Response<BaseResponse<MutableList<ContactModel>>>>

    @PUT("/chat/contacts/{username}/add/{usertoadd}")
    fun addUserToContacts(@Path("username") userName: String,
                          @Path("usertoadd") userToAdd: String): Deferred<Response<BaseResponse<ContactModel>>>

    @DELETE("/chat/contacts/{username}/remove/{usertoremove}")
    fun removeUserFromContacts(@Path("username") userName: String,
                               @Path("usertoremove") userToRemove: String): Deferred<Response<BaseResponse<String>>>

    @GET("/chat/message/fromuser/{fromusername}/touser/{tousername}")
    fun getAllMessages(@Path("fromusername") fromUserName: String,
                       @Path("tousername") toUserName: String): Deferred<Response<BaseResponse<ConversationContentModel>>>

    @GET("/chat/seen/fromuser/{fromusername}/touser/{tousername}")
    fun seenAllMessages(@Path("fromusername") fromUserName: String,
                       @Path("tousername") toUserName: String): Deferred<Response<BaseResponse<String>>>

    @POST("/chat/message/{conversationId}")
    fun sendSimpleMessage(@Path("conversationId") id: String,
                          @Body message: MessageRequestModel): Deferred<Response<BaseResponse<MessageModel>>>

    @Multipart
    @POST("/chat/image/{conversationId}")
    fun sendImageMessage(@Path("conversationId") conversationId: String,
                         @Part photo: MultipartBody.Part,
                         @Part thumbnail: MultipartBody.Part,
                         @Part("authorName") authorName: RequestBody,
                         @Part("content") content: RequestBody
    ): Deferred<Response<BaseResponse<MessageModel>>>

    @POST("/users/authenticate")
    fun authenticateUser(@Body auth: AuthRequestModel): Deferred<Response<BaseResponse<UserModel>>>

    @Multipart
    @POST("/users/register")
    fun registerUserWithImage(@Part file: MultipartBody.Part,
                     @Part("userName") userName: RequestBody,
                     @Part("displayName") displayName: RequestBody,
                     @Part("password") password: RequestBody,
                     @Part("email") email: RequestBody): Deferred<Response<BaseResponse<String>>>

    @POST("/users/register")
    fun registerUserWithoutImage(@Body registerModel: RegisterRequestModel): Deferred<Response<BaseResponse<String>>>

    @GET("/users/{username}")
    fun getUserByUserName(@Path("username") userName: String): Deferred<Response<BaseResponse<UserModel>>>

    @Multipart
    @PUT("/users/id/{userid}")
    fun updateUserWithImage(@Path("userid") userId: String,
                       @Part file: MultipartBody.Part,
                       @Part("displayName") displayName: RequestBody,
                       @Part("password") password: RequestBody,
                       @Part("email") email: RequestBody
    ): Deferred<Response<BaseResponse<UserModel>>>

    @PUT("/users/id/{userid}")
    fun updateUserWithoutImage(@Path("userid") userId: String,
                               @Body updateBody: UpdateRequestModel
    ): Deferred<Response<BaseResponse<UserModel>>>

    @DELETE("/users/id/{userid}")
    fun deleteUserAccount(@Path("userid") userId: String): Deferred<Response<BaseResponse<String>>>
}