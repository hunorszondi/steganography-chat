import multer from 'multer'
import path from 'path'
import fs from 'fs'
import { updateChat, updateContacts } from '../socket/socketIO.mjs'
import {
  insertMessageWithObject,
  getAllContactsOfUser,
  createConversation,
  addUserToContactsService,
  removeUserFromContactsService,
  getMessagesOfConversation,
  getConversationById,
  addNewMessageToContacts,
  seenAllMessagesFromContact
} from '../services/chatService'
import { getUserByUserName } from '../services/userService'
import { makeSuccessResponse, makErrorResponse } from '../services/responseBuilder'
import { deleteEncodedPicture, getThumbnailImage } from '../services/imageS3Util'
import { sendNotification } from '../services/pushNotificationService'

const encodedImagesDir = `${path.resolve(path.dirname(''))}/imageData/encodedImages`

/**
 * Defines where to store the uploaded images
 */
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    cb(null, `${encodedImagesDir}/${req.params.conversationId}`)
  },
  filename: (req, file, cb) => {
    cb(null, `${req.params.conversationId}_${Date.now()}.png`)
  }
})

export const upload = multer({ storage: storage })

/**
 * Uploads a message with an image
 * 
 * @param {String} request.files['photo'][0].location uploaded photo URL
 * @param {String} request.files['thumbnail'][0].location uploaded photo thumbnail URL
 * @param {String} request.params.conversationId id of conversation to post to
 * @param {String} request.body.authorName user who posted
 * @param {String} request.body.content text of the message
 * @param response 
 */
export const uploadImage = async (request, response) => {
  try {
    var imageName = request.files['photo'][0].location
    var thumbnailName = request.files['thumbnail'][0].location

    const valid = await validMessageUpload(request.params.conversationId, request.body.authorName, request.body.content, false)
    if(valid.status != 200) {
      await deleteEncodedPicture(imageName)
      await deleteEncodedPicture(thumbnailName)
      response.status(valid.status)
      response.json(makErrorResponse(valid.message))
      return
    }

    const receiveDate = Date.now()

    // persist image name to database
    const imageMessage = {
      conversationId: request.params.conversationId,
      authorName: request.body.authorName,
      content: request.body.content,
      photo: imageName,
      thumbnail: thumbnailName,
      date: receiveDate
    }

    await insertMessageWithObject(imageMessage)

    // emmit to event to user1_user2 chat room, that he has a new image message
    updateChat(request.params.conversationId, imageMessage)
    createNotificaton(request.params.conversationId, 
      imageMessage.authorName, 
      "sent an image",
      receiveDate)

    response.status(200)
    response.json(makeSuccessResponse(imageMessage))
  } catch (err) {
    response.status(500)
    response.json(makErrorResponse('Message insertion failed. More details: ' + JSON.stringify(err)))
  }
}

/**
 * Downloads an encoded image
 * 
 * @param {String} request.params.conversationId id of conversation
 * @param {String} request.params.imageName url of the image
 * @param response 
 */
export const downloadImage = (request, response) => {
  // send image to client
  try {
    const imagePath = `${encodedImagesDir}/${request.params.conversationId}/${request.params.imageName}`
    if (fs.existsSync(imagePath)) {
      response.sendFile(imagePath)
    } else {
      response.status(404)
      response.json(makErrorResponse(`Requested image ${request.params.imageName} not found.`))
    }

  } catch (err) {
    response.status(500)
    response.json(makErrorResponse(`Downloading image failed. Server error occured: ${JSON.stringify(err)}`))
  }
}

/**
 * Download the thumbnail of an encoded image
 * 
 * @param {String} request.params.imageName  url of the thumbnail image
 * @param response 
 */
export const getPictureThumbnail = async (request, response) => {
  try {
    const image = await getThumbnailImage(request.params.imageName)
    if (image) {
      response.type('image/jpeg')
      response.send(image)
    } else {
      response.status(404)
      response.json(makErrorResponse(`Requested image thumbnail not found.`))
    }

  } catch (err) {
    response.status(500)
    response.json(makErrorResponse(`Downloading image failed. Server error occured: ${JSON.stringify(err)}`))
  }
}

/**
 * Uploads a simple message (just text, no image)
 * 
 * @param {String} request.params.conversationId id of conversation to post to
 * @param {String} request.body.authorName user who posted
 * @param {String} request.body.content text of the message
 * @param response 
 */
export const simpleMessage = async (request, response) => {
  try {
    const valid = await validMessageUpload(request.params.conversationId, request.body.authorName, request.body.content, true)
    if(valid.status != 200) {
      response.status(valid.status)
      response.json(makErrorResponse(valid.message))
      return
    }

    const receiveDate = Date.now()

    // persist message to database
    const message = {
      conversationId: request.params.conversationId,
      authorName: request.body.authorName,
      content: request.body.content,
      date: receiveDate
    }

    await insertMessageWithObject(message)

    // emmit to event to user1_user2 chat room, that he has a new message
    updateChat(request.params.conversationId, message)
    createNotificaton(request.params.conversationId,
      message.authorName,
      message.content,
      receiveDate)

    response.status(200)
    response.json(makeSuccessResponse(message))
  } catch (err) {
    response.status(500)
    response.json(makErrorResponse('Message insertion failed. More details: ' + JSON.stringify(err)))
  }
}

/**
 * Returns all contacts of a user
 * 
 * @param {String} request.params.username username 
 * @param response 
 */
export const getAllContacts = async (request, response) => {
  const userName = request.params.username
  try {
    const contacts = await getAllContactsOfUser(userName)
    // send in response
    response.status(200)
    response.json(makeSuccessResponse(contacts))
  } catch (err) {
    response.status(500)
    response.json(makErrorResponse('A database error occured. More details: ' + JSON.stringify(err)))
  }
}

/**
 * Adds a new contact to a user
 * 
 * @param {String} request.params.username username to add the new contact to
 * @param {String} request.params.usertoadd new contacts username
 * @param response 
 */
export const addUserToContacts = async (request, response) => {
  const userName = request.params.username
  const userNameToAdd = request.params.usertoadd
  try {
    const userAdded = await addUserToContactsService(userName, userNameToAdd)

    // send in response
    response.status(200)
    response.json(makeSuccessResponse(userAdded))
  } catch (err) {
    response.status(500)
    response.json(makErrorResponse(JSON.stringify(err)))
  }
}

/**
 * Removes a contact from user contact list
 * 
 * @param {String} request.params.username username to remove contact from
 * @param {String} request.params.usertoremove username to remove
 * @param response 
 */
export const removeUserFromContacts = async (request, response) => {
  const userName = request.params.username
  const userNameToRemove = request.params.usertoremove
  try {
    await removeUserFromContactsService(userName, userNameToRemove)

    // send in response
    response.status(200)
    response.json(makeSuccessResponse('User successfully removed from contacts'))
  } catch (err) {
    response.status(500)
    response.json(makErrorResponse('A database error occured. More details: ' + JSON.stringify(err)))
  }
}

/**
 * Get all messages of two users
 * 
 * @param {String} request.params.fromusername current username
 * @param {String} request.params.tousername other username
 * @param response 
 */
export const getAllMessages = async (request, response) => {
  const fromUserName = request.params.fromusername
  const toUserName = request.params.tousername

  try {
    const conversationId = await createConversation(fromUserName, toUserName)
    const messages = await getMessagesOfConversation(conversationId)

    seenAllMessagesFromContact(fromUserName, toUserName)
    const fromUser = await getUserByUserName(fromUserName)
    updateContacts(fromUser._id, {
      userName: toUserName,
      hasUnreadMessages: false
    })

    // send in response
    response.status(200)
    response.json(makeSuccessResponse({conversationId, messages}))
  } catch (err) {
    response.status(500)
    response.json(makErrorResponse('A database error occured. More details: ' + JSON.stringify(err)))
  }
}

/**
 * Turns unseen messages to seen
 * 
 * @param {String} request.params.fromusername current username
 * @param {String} request.params.tousername other username 
 * @param response 
 */
export const seenMessages = async (request, response) => {
  const fromUserName = request.params.fromusername
  const toUserName = request.params.tousername

  try {
    seenAllMessagesFromContact(fromUserName, toUserName)
    const fromUser = await getUserByUserName(fromUserName)
    updateContacts(fromUser._id, {
      userName: toUserName,
      hasUnreadMessages: false
    })

    // send in response
    response.status(200)
    response.json(makeSuccessResponse("Data saved"))
  } catch (err) {
    response.status(500)
    response.json(makErrorResponse('A database error occured. More details: ' + JSON.stringify(err)))
  }
}

/**
 * Initializes the notification sending to a conversation
 * 
 * @param {String} conversationId to send the notification
 * @param {String} authorName from whom
 * @param {String} message text of the message
 * @param {Number} date date of the message
 */
async function createNotificaton(conversationId, authorName, message, date) {
  const conversation = await getConversationById(conversationId)

  const fromUser = await getUserByUserName(authorName)

  conversation.members.forEach(async member => {
    if(member != authorName) {
      const user = await getUserByUserName(member)
      sendNotification(user._id, fromUser.displayName, message)
      addNewMessageToContacts(authorName, user.userName, message, date)
      updateContacts(user._id, {
        userName: fromUser.userName,
        lastMessage: message,
        lastMessageDate: date,
        hasUnreadMessages: true
      })
    }
  })
}

/**
 * Validates the properties of a message, like does the conversation exists
 * or has a user the right to post to that conversation 
 * 
 * @param {String} conversationId going the message to
 * @param {String} authorName from whom
 * @param {String} content text of the message
 * @param {Boolean} hasToCheckContent is the content necessary
 */
async function validMessageUpload(conversationId, authorName, content, hasToCheckContent) {
  const conversation = await getConversationById(conversationId)
  // check existance of conversation
  if(!conversation) {
    return {
      status: 404,
      message: 'Conversation does not exists!'
    }
  }

  // check the right to post to this conversation
  if(!conversation.members.includes(authorName)) {
    return {
      status: 403,
      message: 'You have no right to post in this conversation!'
    }
  }

  // check field completion
  if (!authorName || (hasToCheckContent && !content)) {
    return {
      status: 400,
      message: 'authorName or content is missing!'
    }
  }
  return {
    status: 200
  }
}
