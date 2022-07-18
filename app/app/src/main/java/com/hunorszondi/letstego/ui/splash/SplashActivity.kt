package com.hunorszondi.letstego.ui.splash


import android.content.Intent
import android.os.Bundle
import android.os.Handler

import java.util.concurrent.TimeUnit

import androidx.appcompat.app.AppCompatActivity
import com.hunorszondi.letstego.R
import com.hunorszondi.letstego.Session
import com.hunorszondi.letstego.ui.LoadingDialog
import com.hunorszondi.letstego.ui.auth.AuthActivity
import com.hunorszondi.letstego.ui.chat.ChatActivity

/**
 * First screen appearing when the app starts.
 */
class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        checkUserSession()
    }

    /**
     * Decides which screen to show depending on the stored user session
     */
    private fun checkUserSession() {
        if(Session.instance.isUserLoggedIn()) {
            openChat()
        } else {
            openLogin()
        }
    }

    /**
     * Opens AuthActivity -> LoginFragment
     */
    private fun openLogin() {
        Handler().postDelayed({
            startActivity(Intent(this, AuthActivity::class.java))
            finish()
        }, TimeUnit.SECONDS.toMillis(1))
    }

    /**
     * Opens ChatActivity -> ContactFragment
     */
    private fun openChat() {
        Handler().postDelayed({
            startActivity(Intent(this, ChatActivity::class.java))
            finish()
        }, TimeUnit.SECONDS.toMillis(1))
    }
}