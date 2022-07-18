import socketioMdolue from 'socket.io'
let io

/**
 * Initializes the web socket connection. Call only once, when app starts
 * 
 * @param {Server} server server for the socket
 */
export const startSocket = server => {
  io = socketioMdolue(server)
  console.log('Start socket called')
  io.on('connection', () => {
    console.log('New client connected to server')
  })
}

/**
 * Updates a conversation with json type message
 * 
 * @param {number} conversationId id of conversation
 * @param {String} message.authorName sender username
 * @param {String} message.content the message
 * @param {String} message.photo encoded photo url
 * @param {String} message.thumbnail thumbnail url
 * @param {String} message.date date of the message
 */
export const updateChat = function (conversationId, message) {
  console.log(`New socket emit in conversation: ${conversationId}`)
  io.emit(conversationId, message)
}

/**
 * Updates the contact list of [userId]
 * 
 * @param {String} userId user id
 * @param {String} userName username to update in list
 * @param {String} lastMessage text of the new message
 * @param {Number} lastMessageDate timestamp of new message
 * @param {Boolean} hasUnreadMessages always sends true
 */
export const updateContacts = function (userId, message) {
  console.log(`New socket emit in to user: ${userId}`)
  io.emit(`${userId}_contact_update`, message)
}
