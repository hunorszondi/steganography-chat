import jwt from 'jsonwebtoken'
import bcrypt from 'bcryptjs'
import config from '../config'
import { UserModel } from '../models/database.mjs'

/**
 * Validate email address 
 * 
 * @param {String} email 
 * @returns {Boolean} validity
 */
const validateEmail = (email) => {
    const re = /^(([^<>()[\]\\.,;:\s@"]+(\.[^<>()[\]\\.,;:\s@"]+)*)|(".+"))@((\[[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\.[0-9]{1,3}\])|(([a-zA-Z\-0-9]+\.)+[a-zA-Z]{2,}))$/
    return re.test(String(email).toLowerCase())
}

/**
 * Validate username
 * 
 * @param {String} userName 
 * @returns {Boolean} validity
 */
const validateUserName = (userName) => {
    const re = /^[a-zA-Z0-9_]+$/
    return re.test(String(userName))
}

/**
 * Validate password (length min 8 char, one lowercase, one uppercase, one number)
 * 
 * @param {String} password 
 * @returns {Boolean} validity
 */
const validatePassword = (password) => {
    const re = /(?=.*\d)(?=.*[a-z])(?=.*[A-Z])[0-9a-zA-Z]{8,}/
    return re.test(String(password))
}

/**
 * Authenticate user
 * 
 * @param {String} userName username
 * @param {String} password password
 * @returns user profile details without password and access token
 */
export const authenticateUser = async (userName, password) => {
    const user = await UserModel.findOne({ userName })
    if (user && bcrypt.compareSync(password, user.password)) {
        // eslint-disable-next-line no-unused-vars
        const { password, ...userWithoutPassword } = user.toObject()

        const payload = {
            sub: user._id,
            name: user.userName,
            iat: Date.now()
          }

        const token = jwt.sign(payload, config.secret, {expiresIn: '1d'})
        return {
            ...userWithoutPassword,
            token
        }
    }
}

/**
 * Get all users
 * 
 * @returns {UserModel[]} list of all users
 */
export const getAllUsers = async () => {
    return await UserModel.find().select('-password')
}

/**
 * Get user by username
 * 
 * @param {String} userName username
 * @returns {UserModel} user profile 
 */
export const getUserByUserName = async (userName) => {
    return await UserModel.findOne({ userName }).select('-password')
}

/**
 * Get user profile picture url
 * 
 * @param {String} userName username
 * @returns {String} profile picture url
 */
export const getUserPictureName = async (userName) => {
    const user = await UserModel.findOne({ userName }).select('photo')
    return user.photo
}

/**
 * Get user profile by user id
 * 
 * @param {String} id iser id
 * @returns {UserModel} user profile
 */
export const getUserById = async (id) => {
    return await UserModel.findById(id).select('-password')
}

/**
 * Insert new user in database
 * 
 * @param {String} userParam.userName username
 * @param {String} userParam.password password
 * @param {String} userParam.email email address
 * @param {String} userParam.displayName name to display to other users
 * @param {String} userParam.photo photo url
 * @returns {UserModel} new user profile
 */
export const createUser = async (userParam) => {
    // check required fields 
    if (!userParam.email ||
        !userParam.displayName ||
        !userParam.userName ||
        !userParam.password) {
        throw 'Missing user data'
    }

    // validate
    if (await UserModel.findOne({ userName: userParam.userName })) {
        throw `Username "${userParam.userName}" is already taken`
    }

    if(!validateUserName(userParam.userName)) {
            throw 'Username can contain only lowercase/uppercase letters, numbers or _'
    }

    if (!validateEmail(userParam.email)) {
        throw 'Invalid email address'
    }

    if(!validatePassword(userParam.password)) {
        throw 'Password should contain minimum 8 characters and at least one lowercase, one uppercase, one number'
    }

    const user = new UserModel(userParam)

    // hash password
    if (userParam.password) {
        user.password = bcrypt.hashSync(userParam.password, 10)
    }

    // save user
    return await user.save()
}

/**
 * Update existing user in database
 * 
 * @param {String} id user id
 * @param {String} userParam.userName userName, optional
 * @param {String} userParam.password password, optional
 * @param {String} userParam.email email address, optional
 * @param {String} userParam.displayName name to display to other users, optional
 * @param {String} userParam.photo photo url, optional
 * @returns {UserModel} updated user profile
 */
export const updateUser = async (id, userParam) => {
    const user = await UserModel.findById(id);

    // validate
    if (!user) throw 'User not found'
    if (userParam.userName && user.userName !== userParam.userName) {
        throw 'Username can not be updated'
    }

    if (userParam.email) {
        if(!validateEmail(userParam.email)) {
            throw 'Invalid email address'
        } else {
            user.email = userParam.email
        }
    }

    if (userParam.password) {
        if(!validatePassword(userParam.password)) {
            throw 'Password should contain minimum 8 characters and at least one lowercase, one uppercase, one number'
        } else {
            // hash password if it was entered
            user.password = bcrypt.hashSync(userParam.password, 10)
        }
    }

    if(userParam.displayName) {
        user.displayName = userParam.displayName
    }

    if(userParam.photo) {
        user.photo = userParam.photo
    }

    return await user.save()
}

/**
 * Delete user by [id]
 * 
 * @param {String} id user id
 */
export const deleteUserById = async (id) => {
    await UserModel.findByIdAndDelete(id)
}

/**
 * Delete user by [userName]
 * 
 * @param {String} userName username
 */
export const deleteUserByUserName = async (userName) => {
    await UserModel.findOneAndRemove({ userName })
}
