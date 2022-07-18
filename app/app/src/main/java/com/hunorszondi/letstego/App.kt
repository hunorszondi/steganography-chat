package com.hunorszondi.letstego

import android.app.Application
import android.app.NotificationManager
import android.content.Context
import com.hunorszondi.letstego.utils.ChatSocket
import com.pusher.pushnotifications.PushNotifications

import com.hunorszondi.letstego.utils.ResourceUtil
import android.content.Context.NOTIFICATION_SERVICE

/**
 * Entering point of the application. Used for initializing utils.
 */
class App : Application() {

    override fun onCreate() {
        super.onCreate()
        ResourceUtil.createInstance(applicationContext)
        ChatSocket.createInstance()
        Session.createInstance(applicationContext)
        PushNotifications.start(applicationContext, Config().pusherInstance)
    }
}