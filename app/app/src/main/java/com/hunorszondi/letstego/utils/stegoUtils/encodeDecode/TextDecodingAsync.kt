package com.hunorszondi.letstego.utils.stegoUtils.encodeDecode

import android.os.AsyncTask
import android.util.Log

import com.hunorszondi.letstego.utils.stegoUtils.utils.ImageByteUtils

/**
 * All those method in EncodeDecode class are used to decode secret message in image.
 * All the tasks will run in background thanks to AsyncTask.
 */
class TextDecodingAsync(
    private val IStegoCallback: IStegoCallback
) : AsyncTask<SteganoData, Void, SteganoData>() {

    private val result: SteganoData = SteganoData()

    override fun onPostExecute(steganoData: SteganoData) {
        super.onPostExecute(steganoData)
        IStegoCallback.onCompleteProcess(result)
    }

    override fun doInBackground(vararg steganoData: SteganoData): SteganoData {

        if (steganoData.isNotEmpty()) {

            val imageSteganography = steganoData[0]

            val bitmap = imageSteganography.image

            val srcEncodedList = ImageByteUtils.splitImage(bitmap!!)

            val decodedMessage = EncodeDecode.decodeMessage(srcEncodedList)

            Log.d(TAG, "Decoded_Message : $decodedMessage")

            if (!ImageByteUtils.isStringEmpty(decodedMessage)) {
                result.isDecoded = true
            }

            val decryptedMessage = SteganoData.decryptMessage(decodedMessage, imageSteganography.secretKey!!)
            Log.d(TAG, "Decrypted message : $decryptedMessage")

            if (!ImageByteUtils.isStringEmpty(decryptedMessage)) {

                result.isSecretKeyWrong = false

                result.message = decryptedMessage

                for (bitm in srcEncodedList) {
                    bitm.recycle()
                }

                System.gc()
            }
        }

        return result
    }

    companion object {
        private val TAG = TextDecodingAsync::class.java.name
    }
}
