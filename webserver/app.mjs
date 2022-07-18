import createError from 'http-errors'
import express from 'express'
import cors from 'cors'
import logger from 'morgan'
import router from './routes'
import jwt from './services/jwt'
import { makErrorResponse } from './services/responseBuilder.mjs'

let app = express()

app.use(logger('dev'))
app.use(express.urlencoded({ extended: false }))
app.use(express.json())
app.use(cors())

app.use(jwt())

app.use('/', router)

// catch 404 and forward to error handler
app.use((req, res, next) => {
  next(createError(404))
})

// error handler
const errorHandler = (err, req, res) => {
  if (typeof (err) === 'string') {
    return res.status(400).json(makErrorResponse(err))
  }

  if (err.name === 'ValidationError') {
    // mongoose validation error
    return res.status(400).json(makErrorResponse(err.message))
  }

  if (err.name === 'UnauthorizedError') {
    // jwt authentication error
    return res.status(401).json(makErrorResponse('Invalid Token'))
  }

  // default to 500 server error
  return res.status(500).json(makErrorResponse(err.message))
}

app.use(errorHandler)

export default app
