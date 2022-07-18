package com.hunorszondi.letstego

/**
 * Stores all the necessary configurations for the app
 */
data class Config(
    val apiBaseUrl: String = "http://Letstego-env-nodejs.naxbyup3ku.eu-central-1.elasticbeanstalk.com",
    val pusherInstance: String = "79da0046-c72a-47f2-bf19-fc58d34d1047"
)