package com.hunorszondi.letstego.utils.stegoUtils.encodeDecode

import android.graphics.Bitmap
import android.util.Log

import com.hunorszondi.letstego.utils.stegoUtils.utils.CryptoUtil
import com.hunorszondi.letstego.utils.stegoUtils.utils.ImageByteUtils

/**
 * Manages all necessary data for the algorithm and resolves its encryption
 */
class SteganoData {

    var message: String? = null
    var secretKey: String? = null
    var encryptedMessage: String? = null
    var image: Bitmap? = null
    var encodedImage: Bitmap? = null
    var isEncoded: Boolean? = null
    var isDecoded: Boolean? = null
    var isSecretKeyWrong: Boolean? = null

    private var encryptedZip: ByteArray? = null

    constructor() {
        this.isEncoded = false
        this.isDecoded = false
        this.isSecretKeyWrong = true
        this.message = ""
        this.secretKey = ""
        this.encryptedMessage = ""
        this.image = Bitmap.createBitmap(600, 600, Bitmap.Config.ARGB_8888)
        this.encodedImage = Bitmap.createBitmap(600, 600, Bitmap.Config.ARGB_8888)
        this.encryptedZip = ByteArray(0)
    }

    constructor(message: String, secretKey: String, image: Bitmap) {

        this.message = message
        this.secretKey = convertKeyTo128bit(secretKey)
        this.image = image


        this.encryptedZip = message.toByteArray()
        this.encryptedMessage = encryptMessage(message, this.secretKey!!)

        this.isEncoded = false
        this.isDecoded = false
        this.isSecretKeyWrong = true

        this.encodedImage = Bitmap.createBitmap(600, 600, Bitmap.Config.ARGB_8888)

    }

    constructor(secretKey: String, image: Bitmap) {
        this.secretKey = convertKeyTo128bit(secretKey)
        this.image = image

        this.isEncoded = false
        this.isDecoded = false
        this.isSecretKeyWrong = true

        this.message = ""
        this.encryptedMessage = ""
        this.encodedImage = Bitmap.createBitmap(600, 600, Bitmap.Config.ARGB_8888)
        this.encryptedZip = ByteArray(0)
    }

    companion object {

        private val TAG = SteganoData::class.java.name

        /**
         * Encrypts the message
         *
         * @param message Message to be encrypted
         * @param secretKey Key to the encryption
         * @return Encrypted value
         */
        private fun encryptMessage(message: String?, secretKey: String): String {
            Log.d(TAG, "Message : " + message!!)

            var encryptedMessage = ""
            if (!ImageByteUtils.isStringEmpty(secretKey)) {
                try {
                    encryptedMessage = CryptoUtil.encryptMessage(message, secretKey)
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            } else {
                encryptedMessage = message
            }

            Log.d(TAG, "Encrypted_message : $encryptedMessage")

            return encryptedMessage
        }

        /**
         * Decrypts the message
         *
         * @param message Value to be decrypted
         * @param secretKey Key to the decryption
         * @return Decrypted message
         */
        fun decryptMessage(message: String?, secretKey: String): String {
            var decryptedMessage = ""
            if (message != null) {
                if (!ImageByteUtils.isStringEmpty(secretKey)) {
                    try {
                        decryptedMessage = CryptoUtil.decryptMessage(message, secretKey)
                    } catch (e: Exception) {
                        Log.d(TAG, "Error : " + e.message + " , may be due to wrong key.")
                    }

                } else {
                    decryptedMessage = message
                }
            }

            return decryptedMessage
        }

        /**
         * Secret key conversion to 128 bit key
         *
         * @param secretKey Original secret key
         * @return 128 bit key
         */
        private fun convertKeyTo128bit(secretKey: String): String {

            var result = StringBuilder(secretKey)

            if (secretKey.length <= 16) {
                for (i in 0 until 16 - secretKey.length) {
                    result.append("#")
                }
            } else {
                result = StringBuilder(result.substring(0, 15))
            }

            Log.d(TAG, "Secret Key Length : " + result.toString().toByteArray().size)

            return result.toString()
        }
    }
}