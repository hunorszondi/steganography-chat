import multer from 'multer'
import { makeSuccessResponse, makErrorResponse } from '../services/responseBuilder'
import { reduceImageSize, deleteProfilePicture } from '../services/imageS3Util'
import {
  authenticateUser,
  createUser,
  updateUser,
  deleteUserById,
  deleteUserByUserName,
  getAllUsers,
  getUserById,
  getUserByUserName,
  getUserPictureName
} from '../services/userService'
import { getProfilePicture } from '../services/imageS3Util.mjs';

const profileImagesDir = `imageData/profileImages/`

/**
* Defines where to store the uploaded images
*/
const storage = multer.diskStorage({
  destination: (req, file, cb) => {
    cb(null, profileImagesDir)
  },
  filename: (req, file, cb) => {
    cb(null, `${Date.now()}_${file.originalname}`)
  }
})

export const uploadProfilePic = multer({ storage: storage })

/**
 * Authenticate user
 * 
 * @param {String} request.body.userName username
 * @param {String} request.body.password password
 * @param response 
 */
export const authenticate = async (request, response) => {
  try {
    if (!request.body.userName || !request.body.password) {
      throw 'No given username or password'
    }
    const user = await authenticateUser(request.body.userName, request.body.password)
    if (user) {
      response.status(200)
      response.json(makeSuccessResponse(user))
    } else {
      response.status(400)
      response.json(makErrorResponse('Username or password is incorrect'))
    }
  } catch (err) {
    response.status(500)
    response.json(makErrorResponse('Authentication failed. More details: ' + JSON.stringify(err)))
  }
}

/**
 * Register new user
 * 
 * @param {String} request.file.location uploaded profile picture url
 * @param {String} request.body.userName username
 * @param {String} request.body.password password
 * @param {String} request.body.displayName displayName
 * @param {String} request.body.email email
 * @param response 
 */
export const register = async (request, response) => {
  try {
    if (request.file) {
      const url = request.file.location
      await reduceImageSize(`${profileImagesDir}${url.substring(url.lastIndexOf('/')+1)}`)
      request.body.photo = request.file.location
    }
    await createUser(request.body)
    response.json(makeSuccessResponse('Successful registration!'))
  } catch (err) {
    response.status(500)
    response.json(makErrorResponse('Registration failed. More details: ' + JSON.stringify(err)))
  }
}

/**
 * Get all users
 * 
 * @param response 
 */
export const getAll = async (_, response) => {
  try {
    const users = await getAllUsers()
    response.json(makeSuccessResponse(users))
  } catch (err) {
    response.status(500)
    response.json(makErrorResponse('Get all users failed. More details: ' + JSON.stringify(err)))
  }
}

/**
 * Get user profile by username
 * 
 * @param {String} request.params.username username 
 * @param response 
 */
export const getByUserName = async (request, response) => {
  try {
    const user = await getUserByUserName(request.params.username)
    if (user) {
      response.status(200)
      response.json(makeSuccessResponse(user))
    } else {
      response.status(404)
      response.json(makErrorResponse('Username does not exist'))
    }
  } catch (err) {
    response.status(500)
    response.json(makErrorResponse('Get user by username failed. More details: ' + JSON.stringify(err)))
  }
}

/**
 * Get user profile by user id
 * 
 * @param {String} request.params.id user id
 * @param response 
 */
export const getById = async (request, response) => {
  try {
    const user = await getUserById(request.params.id)
    if (user) {
      response.status(200)
      response.json(makeSuccessResponse(user))
    } else {
      response.status(404)
      response.json(makErrorResponse('Id does not exist'))
    }
  } catch (err) {
    if (err.name === 'CastError') {
      response.status(422)
      response.json(makErrorResponse('Wrong parameter format'))
    } else {
      response.status(500)
      response.json(makErrorResponse('Get user by id failed. More details: ' + JSON.stringify(err)))
    }
    response.status(500)
    response.json(makErrorResponse('Get user by id failed. More details: ' + JSON.stringify(err)))
  }
}

/**
 * Update user profile by user id
 * 
 * @param {String} request.file.location uploaded profile picture url, optional
 * @param {String} request.params.id user id
 * @param {String} request.body.password password, optional
 * @param {String} request.body.displayName displayName, optional
 * @param {String} request.body.email email, optional
 * @param response 
 */
export const update = async (request, response) => {
  try {
    if (request.file) {
      //process new image
      const url = request.file.location
      await reduceImageSize(`${profileImagesDir}${url.substring(url.lastIndexOf('/')+1)}`)
      request.body.photo = request.file.location

      //delete old picture
      const user = await getUserById(request.params.id)
      const oldImageUrl = await getUserPictureName(user.userName)
      deleteProfilePicture(oldImageUrl)
    }

    //update user information in database
    const user = await updateUser(request.params.id, request.body)
    response.json(makeSuccessResponse(user))
  } catch (err) {
    response.status(500)
    response.json(makErrorResponse('Updating user information failed. More details: ' + JSON.stringify(err)))
  }
}

/**
 * Delete user by id or username depending, which field has the request object
 * 
 * @param {String} request.params.id user id, optional
 * @param {String} request.params.username username, optional
 * @param response 
 */
export const deleteUser = async (request, response) => {
  try {
    let user

    if (request.params.id) {
      user = await getUserById(request.params.id)
      await deleteUserById(request.params.id)
    } else if (request.params.username) {
      user = await getUserByUserName(request.params.username)
      await deleteUserByUserName(request.params.username)
    }

    // delete user photo from online storage
    if (user.photo) {
      await deleteProfilePicture(`${profileImagesDir}${user.photo.substring(user.photo.lastIndexOf('/')+1)}`)
    }

    response.json(makeSuccessResponse('User successfully deleted'))
  } catch (err) {
    response.status(500)
    response.json(makErrorResponse('Deleting user failed. More details: ' + JSON.stringify(err)))
  }
}

/**
 * Get the profile picture of a user by its username
 * 
 * @param {String} request.params.username username 
 * @param response 
 */
export const getUserProfilePicture = async (request, response) => {
  try {
    const imageUrl = await getUserPictureName(request.params.username)
    const image = await getProfilePicture(imageUrl)
    if (image) {
      response.type('image/jpeg')
      response.send(image)
    } else {
      response.status(404)
      response.json(makErrorResponse(`Requested contacts profile picture not found.`))
    }

  } catch (err) {
    response.status(500)
    response.json(makErrorResponse(`Downloading image failed. Server error occured: ${JSON.stringify(err)}`))
  }
}
