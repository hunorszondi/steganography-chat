package com.hunorszondi.letstego.viewModels

import android.util.Log
import androidx.lifecycle.ViewModel
import com.hunorszondi.letstego.Session
import com.hunorszondi.letstego.api.ApiClient
import com.hunorszondi.letstego.api.ApiException
import com.hunorszondi.letstego.api.repository.UserRepository
import com.hunorszondi.letstego.model.apiModels.RegisterRequestModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.coroutines.CoroutineContext

/**
 * Authentication logic and the data management
 */
class LoginViewModel : ViewModel() {
    private val parentJob = Job()

    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Default

    private val scope = CoroutineScope(coroutineContext)

    private val repository: UserRepository = UserRepository(ApiClient.apiService)

    /**
     * Authenticates a user in the backend
     *
     * @param userName userName
     * @param password password
     * @param callback communicate back to the caller environment
     */
    fun login(userName: String, password: String, callback: (Boolean, String)->Unit) {
        scope.launch {
            try {
                val user = repository.authenticateUser(userName, password)
                if(user?.data != null) {
                    user.data.password = password
                    Session.instance.login(user.data)
                    callback(true, "Login successful")
                } else {
                    callback(false, user?.error?:"Unknown error no throw")
                }
            } catch (error: ApiException) {
                callback(false, error.serverError?:"Unknown error with throw")
            }
        }
    }

    /**
     * Registers a user in the backend
     *
     * @param userName userName
     * @param password password
     * @param displayName a name to be displayed for other users
     * @param email email address
     * @param photo optional, a file path to an image
     * @param callback communicate back to the caller environment
     */
    fun register(userName: String,
                 password: String,
                 displayName: String,
                 email: String,
                 photo: String?,
                 callback: (Boolean, String)->Unit) {

        if(userName.isEmpty() || password.isEmpty() || displayName.isEmpty() || email.isEmpty()) {
            callback(false, "Please fill each field to register")
            return
        }

        val registerModel = RegisterRequestModel(userName, displayName, password, email, photo)

        scope.launch {
            try {
                val result = repository.registerUser(registerModel)
                if(result?.data != null) {
                    callback(true, result.data)
                } else {
                    callback(false, result?.error?:"Unknown error no throw")
                }
            } catch (error: ApiException) {
                Log.d("LoginViewModel", error.serverError?:"Unknown error with throw")
                callback(false, error.serverError?:"Unknown error with throw")
            }
        }
    }
}
