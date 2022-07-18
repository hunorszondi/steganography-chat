/* eslint-disable no-unused-vars */
import { UserModel, MessageModel, ConversationModel } from '../models/database.mjs'
import { getUserByUserName } from './userService.mjs';
import { Contact } from '../models/models.mjs'

/**
 * Get all contacts of a user
 * 
 * @param {String} userName username to get the list from
 * @returns {UserModel[]} contact list
 */
export const getAllContactsOfUser = async (userName) => {
  const user = await UserModel.findOne({ userName }).exec()

  const promiseContacts = await user.contacts.map(async contact => {
    const user = await UserModel.findOne({ userName: contact.userName }).select('-password -contacts')
    contact.details = user
    return contact
  })
  const userContacts = await Promise.all(promiseContacts)
  return userContacts
}

/**
 * Add [userNameToAdd] to [currentUserName] contacts
 * 
 * @param {String} currentUserName current username
 * @param {String} userNameToAdd other username
 * @returns {Contact[]} contact item
 */
export const addUserToContactsService = async (currentUserName, userNameToAdd) => {
  if (currentUserName == userNameToAdd) {
    throw 'You can not add yourself to the contact list!'
  }
  const currentUserToUpdate = await UserModel.findOne({ userName: currentUserName }).exec()
  currentUserToUpdate.contacts.forEach(user => {
    if(user == null) {
      return
    }
    if (user.userName == userNameToAdd) {
      throw 'User already exists between your contacts!'
    }
  })

  const currentUser = await UserModel.findOne({ userName: currentUserName }).select('-password -contacts')

  const userToAddToUpdate = await UserModel.findOne({ userName: userNameToAdd }).exec()
  if(!userToAddToUpdate) {
    throw 'User does not exists!'
  }
  const userToAdd = await UserModel.findOne({ userName: userNameToAdd }).select('-password -contacts')

  currentUserToUpdate.contacts.push({
    userName: userToAdd.userName,
    hasUnreadMessages: false
  })
  await currentUserToUpdate.save()

  userToAddToUpdate.contacts.push({
    userName: currentUser.userName,
    hasUnreadMessages: false
  })
  await userToAddToUpdate.save()

  await createConversation(currentUserName, userNameToAdd)
  return {
    userName: userToAdd.userName,
    details: userToAdd,
    hasUnreadMessages: false
  }
}

/**
 * Remove [userNameToRemove] from [currentUserName] contacts
 * 
 * @param {String} currentUserName current username
 * @param {String} userNameToRemove other username
 */
export const removeUserFromContactsService = async (currentUserName, userNameToRemove) => {
  const currentUserToUpdate = await UserModel.findOne({ userName: currentUserName }).exec()
  currentUserToUpdate.contacts = currentUserToUpdate.contacts.filter((user) => {
    return user && user.userName != userNameToRemove
  })

  const userToAddToUpdate = await UserModel.findOne({ userName: userNameToRemove }).exec()
  userToAddToUpdate.contacts = userToAddToUpdate.contacts.filter((user) => {
    return user.userName != currentUserName
  })

  await currentUserToUpdate.save()
  await userToAddToUpdate.save()
}

/**
 * Creates a conversation between two users in the database
 * 
 * @param {String} userName1 
 * @param {String} userName2 
 * @returns {String} conversation id
 */
export const createConversation = async (userName1, userName2) => {
  let conversation = await getConversationId(userName1, userName2)
  if (!conversation) {
    conversation = new ConversationModel({
      members: [userName1, userName2]
    })
    conversation = await conversation.save()
  }
  return conversation._id
}

/**
 * Get conversation id by two users
 * 
 * @param {String} userName1 
 * @param {String} userName2 
 * @returns {String|null} conversation id or null if does not exists
 */
export const getConversationId = async (userName1, userName2) => {
  const conversations = await ConversationModel.find({
    members: userName1
  }).exec()
  const conversation = conversations.filter(item => item.members.includes(userName2))
  return conversation[0] ? conversation[0]._id : null
}

/**
 * Get conversation by id
 * 
 * @param {String} id conversation id
 * @returns {ConversationModel} conversation object
 */
export const getConversationById = async (id) => {
  return await ConversationModel.findById(id)
}

/**
 * Get messages of conversation by [conversationId]
 * 
 * @param {String} conversationId conversation id
 * @returns {MessageModel[]} list of messages of conversation
 */
export const getMessagesOfConversation = async (conversationId) => {
  return await MessageModel.find({ conversationId }).exec()
}

/**
 * Insert new message in database
 * 
 * @param {MessageModel} messageModel model contains all the necessary information about a message
 * @returns {MessageModel} saved message
 */
export const insertMessageWithObject = async (messageModel) => {
  const message = new MessageModel(messageModel)
  return await message.save()
}

/**
 * Insert new message in database
 * 
 * @param {String} authorName authors username
 * @param {String} conversationId conversation id
 * @param {String} content text of the message
 * @param {String} photo photo url
 * @param {String} thumbnail thumbnail url
 * @param {Number} date timestamp
 * @returns {MessageModel} saved message
 */
export const insertMessageWithParams = async (authorName, conversationId, content, photo, thumbnail, date) => {
  const message = new MessageModel({
    conversationId,
    authorName,
    content,
    photo,
    thumbnail,
    date
  })
  return await insertMessageWithObject(message)
}

/**
 * Updates last message from [fromUserName] in contact list of [toUserName]
 * 
 * @param {String} fromUserName received [toUserName] the message from
 * @param {String} toUserName whom contact list has to be updated
 * @param {String} message text of the message
 * @param {Number} date timestamp
 */
export const addNewMessageToContacts = async (fromUserName, toUserName, message, date) => {
    const toUser = await getUserByUserName(toUserName)
    toUser.contacts = toUser.contacts.map(contact => {
      if(contact.userName == fromUserName) {
        contact.lastMessage = message
        contact.lastMessageDate = date
        contact.hasUnreadMessages = true
      }
      return contact
    })

    toUser.save()
}

/**
 * Updates unread messages from [toUserName] in contact list of [fromUserName]
 * 
 * @param {String} fromUserName whom contact list has to be updated
 * @param {String} toUserName received [toUserName] the message from
 */
export const seenAllMessagesFromContact = async (fromUserName, toUserName) => {
  const fromUser = await getUserByUserName(fromUserName)
  fromUser.contacts = fromUser.contacts.map(contact => {
    if(contact.userName == toUserName) {
      contact.hasUnreadMessages = false
    }
    return contact
  })

  fromUser.save()
}
