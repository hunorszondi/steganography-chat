import mongoose from 'mongoose'
import config from '../config'

const Schema = mongoose.Schema
const mongoDB = config.dbConnection

const UserSchema = new Schema()

const ContactSchema = new Schema()
ContactSchema.add({
  userName: { type: String, required: true },
  details: { type: UserSchema },
  lastMessage: { type: String },
  lastMessageDate: { type: Number },
  hasUnreadMessages: { type: Boolean }
})

UserSchema.add({
  userName: { type: String, unique: true, required: true },
  displayName: { type: String, required: true },
  password: { type: String }, // hash
  email: { type: String, required: true },
  photo: { type: String },
  contacts: { type: [ContactSchema] }
})

const MessageSchema = new Schema({
  conversationId: { type: String, required: true },
  authorName: { type: String, required: true },
  content: { type: String },
  photo: { type: String },
  thumbnail: { type: String },
  date: { type: Number, default: Date.now() }
})

const ConversationSchema = new Schema({
  members: [String] // usernames
})

/**
 * Initializes the database connection. Call only once, when app starts
 * @returns {Promise} connection result
 */
export const initDb = async function () {
  // CONNECTION EVENTS
  // When successfully connected
  mongoose.connection.on('connected', function () {
    console.log('Mongoose default connection open to ' + mongoDB)
  })

  // If the connection throws an error
  mongoose.connection.on('error', function (err) {
    console.log('Mongoose default connection error: ' + err)
    mongoose.disconnect()
  })

  // When the connection is disconnected
  mongoose.connection.on('disconnected', function () {
    console.log('Mongoose default connection disconnected')
    setTimeout(() => {
      mongoose.connect(mongoDB)
    }, 5000)
  })

  try {
    mongoose.set('useNewUrlParser', true)
    await mongoose.connect(mongoDB)
  } catch (err) {
    err.dbConnection = mongoDB
    throw err
  }
}

export const UserModel = mongoose.model('UserModel', UserSchema, 'user_collection')
export const ContactModel = mongoose.model('ContactModel', ContactSchema, 'contact_collection')
export const ConversationModel = mongoose.model('ConversationModel', ConversationSchema, 'conversation_collection')
export const MessageModel = mongoose.model('MessageModel', MessageSchema, 'message_collection')
