import expressJwt from 'express-jwt'
import config from '../config'
import { getUserById } from './userService'

const jwt = () => {
    const secret = config.secret;
    return expressJwt({ secret, isRevoked }).unless({
        path: [
            // public routes that don't require authentication
            '/users/authenticate',
            '/users/register'
        ]
    })
}

const isRevoked = async (req, payload, done) => {
    const user = await getUserById(payload.sub);

    // revoke token if user no longer exists
    if (!user) {
        return done(null, true)
    }

    done()
}

export default jwt