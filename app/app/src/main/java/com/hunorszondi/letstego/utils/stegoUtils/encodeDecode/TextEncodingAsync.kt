package com.hunorszondi.letstego.utils.stegoUtils.encodeDecode

import android.os.AsyncTask
import com.hunorszondi.letstego.utils.stegoUtils.utils.ImageByteUtils


/**
 * All those method in EncodeDecode class are used to encode secret message in image.
 * All the tasks will run in background thanks to AsyncTask.
 */
class TextEncodingAsync(
    private val callbackInterface: IStegoCallback
) : AsyncTask<SteganoData, Int, SteganoData>() {

    private val result: SteganoData = SteganoData()
    private var maximumProgress: Int = 0

    override fun onPostExecute(textStegnography: SteganoData) {
        super.onPostExecute(textStegnography)
        callbackInterface.onCompleteProcess(result)
    }

    override fun doInBackground(vararg steganoData: SteganoData): SteganoData {

        maximumProgress = 0

        if (steganoData.isNotEmpty()) {

            val textStegnography = steganoData[0]

            val bitmap = textStegnography.image

            val originalHeight = bitmap!!.height
            val originalWidth = bitmap.width

            val srcList = ImageByteUtils.splitImage(bitmap)

            val encodedList = EncodeDecode.encodeMessage(srcList, textStegnography.encryptedMessage!!)

            for (bit in srcList) {
                bit.recycle()
            }

            System.gc()

            val srcEncoded = ImageByteUtils.mergeImage(encodedList, originalHeight, originalWidth)

            result.encodedImage = srcEncoded
            result.isEncoded = true
        }

        return result
    }

    companion object {
        private val TAG = TextEncodingAsync::class.java.name
    }
}