import PushNotifications from 'pusher-push-notifications-node'
import config from '../config'

/**
 * Sends system notification to the client about new message
 * 
 * @param {String} toUserId user to send
 * @param {String} fromUserDisplayName display name of the sender
 * @param {String} message text of the message
 */
export const sendNotification = async (toUserId, fromUserDisplayName, message) => {
    const pushNotifications = new PushNotifications({
        instanceId: config.pusher_instance_id,
        secretKey: config.pusher_secret_key
    })

    try {
        const response = await pushNotifications.publish(
            [`${toUserId}`],
            {
              fcm: {
                notification: {
                  title: fromUserDisplayName,
                  body: message
                }
              }
            }
          )
        console.log('Just published:', response.publishId)
    } catch (error) {
        console.log('Error:', error)
    }
}