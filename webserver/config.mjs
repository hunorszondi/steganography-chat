/* eslint-disable no-undef */
import dotenv from 'dotenv'

dotenv.config()

/**
 * Configuration object which contains the environment variables
 * and the default value for them
 */
const config = {
    /**
     * Database configuration
     */
    dbUser: process.env.monogo_user || 'hunor',
    dbPassword: process.env.monogo_password || '8RrIfU8fpimiSN4P',
    dbHost: process.env.monogo_host || 'cluster0-nhauj.mongodb.net/letstego?retryWrites=true',

    /**
     * Auth JWT secret key
     */
    secret: 'a titkos kulcs',

    /**
     * AWS S3 configuration
     */
    aws_bucket: process.env.aws_bucket || 'elasticbeanstalk-eu-central-1-476648829645',
    aws_secretAccessKey: process.env.aws_secretAccessKey || 'g2F4KzwKAtSzSjHYODbnrq1NWuEUVV4ipoZFPP1w',
    aws_accessKeyId: process.env.aws_accessKeyId || 'AKIAING4GMRQKO4FYGBQ',
    aws_region: process.env.aws_region || 'eu-central-1',

    pusher_instance_id: process.env.pusher_instance_id || '79da0046-c72a-47f2-bf19-fc58d34d1047',
    pusher_secret_key: process.env.pusher_secret_key || '1E788B8F51B0F644E063A1737FDC3284EED997F3E4D8ACF0C5021BC618F533A8',

    Env: process.env.NODE_ENV || 'developer',
    LogLevel: process.env.LOG_LEVEL || 'info'
  }

  config.dbConnection = `mongodb+srv://${config.dbUser}:${config.dbPassword}@${config.dbHost}`
  
  export default config

