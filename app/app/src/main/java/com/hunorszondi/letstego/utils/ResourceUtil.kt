package com.hunorszondi.letstego.utils

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable

/**
 * Helps in reaching resources from classes without context
 */
class ResourceUtil private constructor(private val context: Context) {

    fun getString(id: Int): String {
        return context.resources.getString(id)
    }

    fun getString(id: Int, vararg params: String): String {
        return context.resources.getString(id, params)
    }

    fun getColor(id: Int): Int {
        return context.getColor(id)
    }

    companion object {

        lateinit var instance: ResourceUtil

        /**
         * Initializing ResourceUtil, in order to have access to global resources from anywhere in the app.
         * Call it only from App and only once!
         *
         * @param context Provide the application context
         */
        fun createInstance(context: Context) {
            instance = ResourceUtil(context)
        }
    }
}