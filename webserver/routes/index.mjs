import express from 'express'
import {
    authenticate,
    getAll,
    getByUserName,
    register,
    getById,
    update,
    deleteUser,
    getUserProfilePicture
} from '../controller/userController.mjs'
import {
    getAllContacts,
    addUserToContacts,
    removeUserFromContacts,
    getAllMessages,
    simpleMessage,
    uploadImage,
    getPictureThumbnail,
    seenMessages
} from '../controller/messageController.mjs'
import { uploadEncodedImage, uploadProfilePicture } from '../services/imageS3Util'

const router = express.Router()

/**
 * Get contact list
 */
router.get('/chat/contacts/:username', getAllContacts)

/**
 * Add user to contact list
 */
router.put('/chat/contacts/:username/add/:usertoadd', addUserToContacts)

/**
 * Remove user from contact list
 */
router.delete('/chat/contacts/:username/remove/:usertoremove', removeUserFromContacts)

/**
 * Get messages
 */
router.get('/chat/message/fromuser/:fromusername/touser/:tousername', getAllMessages)

/**
 * Seen all messages
 */
router.get('/chat/seen/fromuser/:fromusername/touser/:tousername', seenMessages)

/**
 * Send simple message
 */
router.post('/chat/message/:conversationId', simpleMessage)

/**
 *  Send image message
 */
router.post(
    '/chat/image/:conversationId', 
    uploadEncodedImage.fields([{ name: 'photo', maxCount: 1 }, { name: 'thumbnail', maxCount: 1 }]),
    uploadImage
)

/**
 * Send image thumbnail
 */
router.get('/chat/image/:imageName', getPictureThumbnail)

/**
 * Autenticate user
 */
router.post('/users/authenticate', authenticate)

/**
 * Register user
 */
router.post('/users/register', uploadProfilePicture.single('file'), register)

/**
 * Get all users
 */
router.get('/users/', getAll)

/**
 * Get user by username
 */
router.get('/users/:username', getByUserName)

/**
 * Get user by id
 */
router.get('/users/id/:id', getById)

/**
 * Update user information
 */
router.put('/users/id/:id', uploadProfilePicture.single('file'), update)

/**
 * Delete user by username
 */
router.delete('/users/:username', deleteUser)

/**
 * Delete user by id
 */
router.delete('/users/id/:id', deleteUser)

/**
 * Get user profile picture
 */
router.get('/users/profilepicture/:username', getUserProfilePicture)

export default router
