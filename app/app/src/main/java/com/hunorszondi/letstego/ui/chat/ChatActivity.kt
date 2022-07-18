package com.hunorszondi.letstego.ui.chat

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.hunorszondi.letstego.R

/**
 * Run behind of every fragment related with chat
 */
class ChatActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)
    }
}
