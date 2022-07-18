package com.hunorszondi.letstego.utils

import android.util.Log
import com.hunorszondi.letstego.Config
import io.socket.client.IO
import io.socket.client.Socket
import java.net.URISyntaxException

/**
 * Used for creating and maintaining connection with backend through socket
 */
class ChatSocket {

    private lateinit var socket: Socket
    private var messageChannel: String? = null
    private var contactChannel: String? = null

    init {
        try {
            socket = IO.socket(Config().apiBaseUrl)
        } catch (error: URISyntaxException) {
            Log.d("SocketIO", "Socket initialization failed")
        }
    }

    /**
     * Connect socket to contact list channel
     *
     * @param userId of the logged in user
     * @param callback socket listener
     */
    fun <T>connectToContacts(userId: String, callback: (T)->Unit) {
        try {
            if(!socket.connected()) {
                socket.connect()
            }

            contactChannel = "${userId}_contact_update"

            socket.on(contactChannel) {
                val dataJSON: T = (it[0] as T)
                callback(dataJSON)
            }
        } catch (error: URISyntaxException) {
            Log.d("SocketIO", "Socket connection failed")
        }
    }

    /**
     * Connect socket on a conversation channel
     *
     * @param conversationId of the active conversation
     * @param callback socket listener
     */
    fun <T>connectToConversation(conversationId: String, callback: (T)->Unit) {
        try {
            if(!socket.connected()) {
                socket.connect()
            }

            messageChannel = conversationId

            socket.on(messageChannel) {
                val dataJSON: T = (it[0] as T)
                callback(dataJSON)
            }
        } catch (error: URISyntaxException) {
            Log.d("SocketIO", "Socket connection failed")
        }

    }

    /**
     * Returns if this object is listening to a conversation or not
     */
    fun isConnectedToMessage(): Boolean {
        return socket.connected() && messageChannel != null
    }

    /**
     * Disconnects this object from a conversation
     */
    fun disconnectFromConversation() {
        socket.off(messageChannel)
        messageChannel = null
    }

    /**
     * Disconnects this object from the contact list
     */
    fun disconnectFromContacts() {
        socket.off(contactChannel)
        contactChannel = null
    }

    companion object {

        lateinit var instance: ChatSocket
            private set

        /**
         * Initializing ChatSocket, in order to have access to shared preferences from anywhere in the app.
         * Call it only from App and only once!
         */
        fun createInstance() {
            instance = ChatSocket()
        }
    }
}