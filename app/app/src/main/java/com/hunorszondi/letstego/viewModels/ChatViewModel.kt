package com.hunorszondi.letstego.viewModels

import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.gson.Gson
import com.hunorszondi.letstego.R
import com.hunorszondi.letstego.Session
import com.hunorszondi.letstego.api.ApiClient
import com.hunorszondi.letstego.api.ApiException
import com.hunorszondi.letstego.api.repository.ChatRepository
import com.hunorszondi.letstego.model.apiModels.ContactModel
import com.hunorszondi.letstego.model.apiModels.MessageModel
import com.hunorszondi.letstego.model.apiModels.MessageRequestModel
import com.hunorszondi.letstego.utils.ChatSocket
import com.hunorszondi.letstego.utils.ResourceUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import org.json.JSONObject
import kotlin.coroutines.CoroutineContext

/**
 * Handles the logic and the data management for the chat and contact list part of the app
 */
class ChatViewModel : ViewModel() {
    //----------------------------API request utilities-----------------------------------
    private val parentJob = Job()

    private val coroutineContext: CoroutineContext
        get() = parentJob + Dispatchers.Default

    private val scope = CoroutineScope(coroutineContext)

    private val repository : ChatRepository = ChatRepository(ApiClient.apiService)

    //----------------------------------Contact Fragment related---------------------------------

    val contacts: MutableLiveData<MutableList<ContactModel>> by lazy {
        MutableLiveData<MutableList<ContactModel>>()
    }

    var currentContact: ContactModel? = null

    //----------------------------------Chat Fragment related------------------------------------

    val messages: MutableLiveData<MutableList<MessageModel>> by lazy {
        MutableLiveData<MutableList<MessageModel>>()
    }

    var currentConversationId: String? = null
    val chatSocket: ChatSocket = ChatSocket.instance

    //----------------------------------Encode Fragment related------------------------------------
    val selectedEncodedImageToSend: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }
    val selectedThumbnailToSend: MutableLiveData<String> by lazy {
        MutableLiveData<String>()
    }

    //-------------------------------------------METHODS-----------------------------------------

    //----------------------------Contact Fragment related methods-------------------------------

    /**
     * Fetch contact list for the logged in user, orders it, and maps to a displayable format
     *
     * @param callback communicate back to the caller environment
     */
    fun fetchContacts(callback: (Boolean, String) -> Unit) {

        if(!contacts.value.isNullOrEmpty()) {
            contacts.postValue(contacts.value)
            callback(true, "Success")
            return
        }

        if(Session.instance.currentUser != null){
            scope.launch {
                try {
                    val contactList = repository.getContacts(Session.instance.currentUser!!.userName)
                    if(contactList?.data != null) {

                        val safeList: MutableList<ContactModel> =
                            contactList.data
                                .filter { it != null }
                                .map {
                                    if (it.lastMessage == null) {
                                        it.lastMessage = ResourceUtil.instance.getString(R.string.say_hi)
                                    }
                                    if (it.lastMessageDate == null) {
                                        it.lastMessageDate = 0
                                    }
                                    it
                                }
                                .toMutableList()
                        safeList
                            .sortWith(compareByDescending<ContactModel> { it.lastMessageDate }
                            .thenBy {it.details!!.displayName})
                        contacts.postValue(safeList)
                        chatSocket.connectToContacts(Session.instance.currentUser!!.userId, ::newContactUpdate)

                        callback(true, "Success")
                    } else {
                        callback(false, contactList?.error!!)
                    }
                } catch (error: ApiException) {
                    Log.d("ChatViewModel", error.serverError?:"Unknown error with throw")
                    callback(false, error.serverError?:"Unknown error with throw")
                }
            }
        }

    }

    /**
     * Called by the [chatSocket] when an update arrives to contact list
     *
     * @param messageJSON update in JSON format
     */
    private fun newContactUpdate(messageJSON: JSONObject) {
        val messageObjectString: String = messageJSON.toString()
        val message: ContactModel = Gson().fromJson(messageObjectString, ContactModel::class.java)
        if(Session.instance.currentUser!!.userName != message.userName) {
            val newContacts: MutableList<ContactModel> = contacts.value!!.map {
                if (it.userName == message.userName) {
                    if (message.hasUnreadMessages != null) {
                        it.hasUnreadMessages = message.hasUnreadMessages
                    }
                    if (message.lastMessage != null) {
                        it.lastMessage = message.lastMessage
                    }
                    if (message.lastMessageDate != null) {
                        it.lastMessageDate = message.lastMessageDate
                    }
                }
                it
            }.toMutableList()
            newContacts.sortWith(compareByDescending<ContactModel> { it.lastMessageDate }
                    .thenBy {it.details!!.displayName})
            contacts.postValue(newContacts)
        }
    }

    /**
     * Adds new contact to the contact list if [contactName] exists in the database
     *
     * @param contactName contact to add
     * @param callback communicate back to the caller environment
     */
    fun addContact(contactName: String, callback: (Boolean, String) -> Unit) {
        if(Session.instance.currentUser != null){
            scope.launch {
                try {
                    val newContact = repository.addUserToContacts(Session.instance.currentUser!!.userName, contactName)
                    if(newContact?.data != null) {

                        if(newContact.data.lastMessage == null) {
                            newContact.data.lastMessage = ResourceUtil.instance.getString(R.string.say_hi)
                            newContact.data.lastMessageDate = 0
                        }

                        val newContactList: MutableList<ContactModel>
                        if(contacts.value != null) {
                            newContactList = contacts.value!!
                            newContactList.add(newContact.data)
                            newContactList.sortWith(compareByDescending<ContactModel> { it.lastMessageDate }
                                .thenBy {it.details!!.displayName})
                        } else {
                            newContactList = mutableListOf(newContact.data)
                        }
                        contacts.postValue(newContactList)
                        callback(true,"")
                    } else {
                        callback(false, newContact?.error!!)
                    }
                } catch (error: ApiException) {
                    Log.d("ChatViewModel", error.serverError?:"Unknown error with throw")
                    callback(false, error.serverError?:"Unknown error with throw")
                }
            }
        }
    }

    /**
     * Removes a contact from contact list
     *
     * @param position position of the contact to be removed in the list
     * @param callback communicate back to the caller environment
     */
    fun removeContactByPosition(position: Int, callback: (Boolean, String) -> Unit) {
        val contactName = contacts.value?.get(position)?.userName
        if(Session.instance.currentUser != null){
            scope.launch {
                try {
                    val result = repository.removeUserFromContacts(Session.instance.currentUser!!.userName, contactName!!)
                    if(result?.data != null) {
                        val newContacts = contacts.value
                        newContacts!!.removeAt(position)
                        contacts.postValue(newContacts)
                        callback(true, result.data)
                    } else {
                        callback(false, result?.error!!)
                    }
                } catch (error: ApiException) {
                    Log.d("ChatViewModel", error.serverError?:"Unknown error with throw")
                    callback(false, error.serverError?:"Unknown error with throw")
                }
            }
        }
    }

    /**
     * Stops listening to contact list updates coming from [chatSocket]
     */
    fun exitContacts(){
        chatSocket.disconnectFromContacts()
    }

    //----------------------------Chat Fragment related methods----------------------------------

    /**
     * Fetch messages for the active conversation between the logged user and one of its contacts
     *
     * @param callback communicate back to the caller environment
     */
    fun fetchMessages(callback: (Boolean, String) -> Unit) {
        if(!messages.value.isNullOrEmpty()) {
            messages.postValue(messages.value)
            callback(true, "Success")
            return
        }

        if(Session.instance.currentUser != null && currentContact != null){
            scope.launch {
                try {
                    val conversation = repository.getAllMessages(Session.instance.currentUser!!.userName, currentContact!!.userName)
                    if(conversation?.data != null) {
                        messages.postValue(conversation.data.messages)
                        currentConversationId = conversation.data.conversationId
                        chatSocket.connectToConversation(currentConversationId!!, ::newMessageArrived)
                        callback(true, "Success")
                    } else {
                        callback(false, conversation?.error!!)
                    }
                } catch (error: ApiException) {
                    Log.d("ChatViewModel", error.serverError?:"Unknown error with throw")
                    callback(false, error.serverError?:"Unknown error with throw")
                }
            }
        }
    }

    /**
     * Called by the [chatSocket] when a new message has been posted to the conversation
     *
     * @param messageJSON update in JSON format
     */
    private fun newMessageArrived(messageJSON: JSONObject) {
        val messageObjectString: String = messageJSON.toString()
        val message: MessageModel = Gson().fromJson(messageObjectString, MessageModel::class.java)
        if(message.authorName != Session.instance.currentUser!!.userName){
            addMessageToList(message)
        }
    }

    /**
     * Usually is called by the View. Selects the message sending method,
     * than calls the right function to send a message
     *
     * @param content text of the message
     * @param callback communicate back to the caller environment
     */
    fun sendMessage(content: String, callback: (Boolean, String)->Unit) {
        if(selectedEncodedImageToSend.value != null) {
            sendImageMessage(content, callback)
        } else {
            sendSimpleMessage(content, callback)
        }
    }

    /**
     * Sends a message to the active conversation without an image attachment
     *
     * @param content text of the message
     * @param callback communicate back to the caller environment
     */
    private fun sendSimpleMessage(content: String, callback: (Boolean, String)->Unit) {
        if( currentConversationId == null ){
            callback(false, "No conversationId")
            return
        }
        val messageRequestModel = MessageRequestModel(currentConversationId!!, Session.instance.currentUser!!.userName,
            content, null, null)

        if(Session.instance.currentUser != null){
            addMessageToList("Sending...", null)
            scope.launch {
                try {
                    val result = repository.sendSimpleMessage(messageRequestModel)
                    removeLastMessageFromList()
                    if(result?.data != null) {
                        addMessageToList(result.data)
                    } else {
                        callback(false, result?.error!!)
                    }
                } catch (error: ApiException) {
                    Log.d("ChatViewModel", error.serverError?:"Unknown error with throw")
                    removeLastMessageFromList()
                    callback(false, error.serverError?:"Unknown error with throw")
                }
            }
        }
    }

    /**
     * Sends a message to the active conversation with an image attachment
     *
     * @param content text of the message
     * @param callback communicate back to the caller environment
     */
    private fun sendImageMessage(content: String, callback: (Boolean, String) -> Unit) {
        if( currentConversationId == null ){
            callback(false, "No conversationId")
            return
        }
        val messageRequestModel = MessageRequestModel(currentConversationId!!, Session.instance.currentUser!!.userName,
            content, selectedEncodedImageToSend.value, selectedThumbnailToSend.value)

        if(Session.instance.currentUser != null){
            addMessageToList("Sending...", null)
            scope.launch {
                try {
                    val result = repository.sendImageMessage(messageRequestModel)
                    removeLastMessageFromList()
                    if(result?.data != null) {
                        addMessageToList(result.data)
                        selectedEncodedImageToSend.postValue(null)
                        selectedThumbnailToSend.postValue(null)
                    } else {
                        callback(false, result?.error!!)
                    }
                } catch (error: ApiException) {
                    Log.d("ChatViewModel", error.serverError?:"Unknown error with throw")
                    removeLastMessageFromList()
                    callback(false, error.serverError?:"Unknown error with throw")
                }
            }
        }
    }

    /**
     * Adds a new message to the active conversation
     *
     * @param content text of the message
     * @param photo image attachment url or file path
     */
    private fun addMessageToList(content: String, photo: String?) {
        val newMessage = MessageModel(null,
            currentConversationId!!,
            Session.instance.currentUser!!.userName,
            content, photo, null, System.currentTimeMillis()/1000)
        val newMessages = messages.value
        newMessages?.add(newMessage)
        messages.postValue(newMessages)
    }

    /**
     * Adds a new message to the active conversation
     *
     * @param newMessage model which contains all the relevant information about the message
     */
    private fun addMessageToList(newMessage: MessageModel) {
        val newMessages = messages.value
        newMessages?.add(newMessage)
        messages.postValue(newMessages)
    }

    /**
     * Removes last message from conversation
     */
    private fun removeLastMessageFromList() {
        val newMessages = messages.value
        newMessages?.removeAt(newMessages.size-1)
        messages.postValue(newMessages)
    }

    /**
     * Stops listening to message updates coming from [chatSocket]
     */
    fun exitConversation() {
        selectedThumbnailToSend.postValue(null)
        selectedEncodedImageToSend.postValue(null)
        chatSocket.disconnectFromConversation()
        seenAllMessagesFromChat()
    }

    /**
     * Tells the backend that the user have seen all the messages from the active conversation
     */
    private fun seenAllMessagesFromChat() {
        scope.launch {
            try {
                repository.seenAllMessages(
                    Session.instance.currentUser!!.userName,
                    currentContact!!.userName
                )
            } catch (error: ApiException) {
                Log.d("ChatViewModel", error.serverError?:"Unknown error with throw")
            }
        }
    }
}