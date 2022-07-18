package com.hunorszondi.letstego.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.hunorszondi.letstego.Session
import com.hunorszondi.letstego.api.ApiClient
import com.hunorszondi.letstego.api.ApiException
import com.hunorszondi.letstego.api.repository.UserRepository
import com.hunorszondi.letstego.model.apiModels.UpdateRequestModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Handles the logic and data management for the profile view
 */
class ProfileViewModel : ViewModel() {
    private val parentJob = Job()

    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Default

    private val scope = CoroutineScope(coroutineContext)

    private val repository: UserRepository = UserRepository(ApiClient.apiService)

    /**
     * Updates user information in the backend
     *
     * @param password password
     * @param displayName a name to be displayed for other users
     * @param email email address
     * @param photo optional, a file path to an image
     * @param callback communicate back to the caller environment
     */
    fun updateProfile(password: String,
                 displayName: String,
                 email: String,
                 photo: String?,
                 callback: (Boolean, String)->Unit) {

        val user = Session.instance.currentUser!!

        if(password.isEmpty() || displayName.isEmpty() || email.isEmpty()) {
            callback(false, "Please fill every field")
            return
        }

        val updateModel = UpdateRequestModel()


        if(password != user.password) {
            updateModel.password = password
        }

        if(displayName != user.displayName) {
            updateModel.displayName = displayName
        }

        if(email != user.email) {
            updateModel.email = email
        }

        if(photo != null) {
            updateModel.photo = photo
        }

        scope.launch {
            try {
                val result = repository.updateUser(user.userId, updateModel)
                if(result?.data != null) {
                    result.data.password = password
                    result.data.token = Session.instance.currentUser!!.token
                    Session.instance.login(result.data)
                    callback(true, "User successfully updated")
                } else {
                    callback(false, result?.error?:"Unknown error no throw")
                }
            } catch (error: ApiException) {
                Log.d("ProfileViewModel", error.serverError?:"Unknown error with throw")
                callback(false, error.serverError?:"Unknown error with throw")
            }
        }
    }

    /**
     * Deletes the active user account
     *
     * @param callback communicate back to the caller environment
     */
    fun deleteProfile(callback: (Boolean, String)->Unit) {
        if(Session.instance.currentUser != null) {
            scope.launch {
                try {
                    val result = repository.deleteUserAccount(Session.instance.currentUser!!.userId)
                    if(result?.data != null) {
                        callback(true, result.data)
                    } else {
                        callback(false, result?.error?:"Unknown error no throw")
                    }
                } catch (error: ApiException) {
                    Log.d("ProfileViewModel", error.serverError?:"Unknown error with throw")
                    callback(false, error.serverError?:"Unknown error with throw")
                }
            }
        }
    }
}