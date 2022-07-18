package com.hunorszondi.letstego.utils.stegoUtils.encodeDecode

import android.graphics.Bitmap
import android.graphics.Color
import android.util.Log
import com.hunorszondi.letstego.utils.stegoUtils.utils.ImageByteUtils

import java.nio.charset.Charset
import java.util.ArrayList
import java.util.Vector

class EncodeDecode {

    companion object {
        private val TAG = EncodeDecode::class.java.name

        private const val START_MESSAGE_SIGN = "@!#"
        private const val END_MESSAGE_SIGN = "#!@"
        private const val ISO = "ISO-8859-1"

        private val binary = intArrayOf(16, 8, 0)
        private val andByte = byteArrayOf(0xC0.toByte(), 0x30, 0x0C, 0x03)
        private val toShift = intArrayOf(6, 4, 2, 0)

        /**
         * Represents the core of 2 bit Encoding
         *
         * @param integerPixelArray The integer RGB array
         * @param imageColumns Image width
         * @param imageRows Image height
         * @param messageEncodingStatus object
         * @return byte encoded pixel array
         */

        private fun encodeMessage(
            integerPixelArray: IntArray, imageColumns: Int, imageRows: Int,
            messageEncodingStatus: MessageEncodingStatus
        ): ByteArray {

            val channels = 3
            var shiftIndex = 4
            val result = ByteArray(imageRows * imageColumns * channels)
            var resultIndex = 0

            for (row in 0 until imageRows) {

                for (col in 0 until imageColumns) {

                    //2D matrix in 1D
                    val element = row * imageColumns + col

                    var tmp: Byte

                    for (channelIndex in 0 until channels) {

                        if (!messageEncodingStatus.isMessageEncoded) {

                            tmp = (integerPixelArray[element]
                                    shr binary[channelIndex]
                                    and 0xFF
                                    and 0xFC
                                    or (messageEncodingStatus.byteArrayMessage!![messageEncodingStatus.currentMessageIndex].toInt()
                                        shr toShift[shiftIndex++ % toShift.size]
                                        and 0x3)).toByte()

                            if (shiftIndex % toShift.size == 0) {
                                messageEncodingStatus.incrementMessageIndex()
                            }

                            if (messageEncodingStatus.currentMessageIndex == messageEncodingStatus.byteArrayMessage!!.size) {

                                messageEncodingStatus.setMessageEncoded()
                            }
                        } else {
                            tmp = (integerPixelArray[element] shr binary[channelIndex] and 0xFF).toByte()
                        }

                        result[resultIndex++] = tmp
                    }
                }
            }

            return result
        }

        /**
         * Implements the above method on the list of chunk image list.
         *
         * @param splitImages list of chunk images
         * @param encryptedMessageParam string
         * @return Encoded list of chunk images
         */
        fun encodeMessage(
            splitImages: List<Bitmap>,
            encryptedMessageParam: String
        ): List<Bitmap> {
            var encryptedMessage = encryptedMessageParam

            val result = ArrayList<Bitmap>(splitImages.size)

            encryptedMessage += END_MESSAGE_SIGN
            encryptedMessage = START_MESSAGE_SIGN + encryptedMessage

            val byteEncryptedMessage = encryptedMessage.toByteArray(Charset.forName(ISO))

            val message = MessageEncodingStatus(byteEncryptedMessage, encryptedMessage)

            Log.i(TAG, "Message length " + byteEncryptedMessage.size)

            for (bitmap in splitImages) {

                if (!message.isMessageEncoded) {

                    val width = bitmap.width
                    val height = bitmap.height

                    val oneD = IntArray(width * height)
                    bitmap.getPixels(oneD, 0, width, 0, 0, width, height)

                    val density = bitmap.density

                    val encodedImage = encodeMessage(oneD, width, height, message)

                    val oneDMod = ImageByteUtils.byteArrayToIntArray(encodedImage)

                    val encodedBitmap = Bitmap.createBitmap(
                        width, height,
                        Bitmap.Config.ARGB_8888
                    )
                    encodedBitmap.density = density

                    var masterIndex = 0

                    for (j in 0 until height) {
                        for (i in 0 until width) {
                            encodedBitmap.setPixel(
                                i, j, Color.argb(
                                    0xFF,
                                    oneDMod[masterIndex] shr 16 and 0xFF,
                                    oneDMod[masterIndex] shr 8 and 0xFF,
                                    oneDMod[masterIndex++] and 0xFF
                                )
                            )
                        }
                    }
                    result.add(encodedBitmap)

                } else {
                    result.add(bitmap.copy(bitmap.config, false))
                }
            }

            return result
        }

        /**
         * Decoding method of 2 bit encoding.
         *
         * @param bytePixelArray The byte array image
         * @param messageDecodingStatus object
         * @return Void
         */
        private fun decodeMessage(
            bytePixelArray: ByteArray,
            messageDecodingStatus: MessageDecodingStatus
        ) {

            val byteEncryptedMessage = Vector<Byte>()

            var shiftIndex = 4

            var tmp: Byte = 0x00

            var testIteration = 0

            for (bytePixel in bytePixelArray) {

                tmp = (tmp.toInt()
                        or (bytePixel.toInt()
                            shl toShift[shiftIndex % toShift.size]
                            and andByte[shiftIndex++ % toShift.size].toInt())).toByte()

                if (shiftIndex % toShift.size == 0) {
                    byteEncryptedMessage.addElement(tmp)

                    val lastCharInByte = byteArrayOf(byteEncryptedMessage.elementAt(byteEncryptedMessage.size - 1))
                    val lastChar = String(lastCharInByte, Charset.forName(ISO))

                    if (messageDecodingStatus.message!!.endsWith(END_MESSAGE_SIGN)) {

                        //fixing ISO-8859-1 decoding
                        val temp = ByteArray(byteEncryptedMessage.size)

                        for (index in temp.indices)
                            temp[index] = byteEncryptedMessage[index]


                        val fixedString = String(temp, Charset.forName(ISO))

                        messageDecodingStatus.message = fixedString.substring(0, fixedString.length - 1)
                        //end fixing

                        messageDecodingStatus.setEnded()

                        break
                    } else {
                        messageDecodingStatus.message = messageDecodingStatus.message!! + lastChar
                        if (messageDecodingStatus.message!!.length == START_MESSAGE_SIGN.length && START_MESSAGE_SIGN != messageDecodingStatus.message) {
                            messageDecodingStatus.message = ""
                            messageDecodingStatus.setEnded()

                            break
                        }
                    }

                    tmp = 0x00
                    testIteration++
                }
            }

            if (!ImageByteUtils.isStringEmpty(messageDecodingStatus.message)) {
                try {
                    messageDecodingStatus.message = messageDecodingStatus.message!!.substring(
                        START_MESSAGE_SIGN.length,
                        messageDecodingStatus.message!!
                            .length - END_MESSAGE_SIGN.length
                    )
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }

        /**
         * Takes the list of encoded chunk images and decodes it.
         *
         * @param encodedImages list of encode chunk images
         * @return encrypted message String
         */
        fun decodeMessage(encodedImages: List<Bitmap>): String? {

            val messageDecodingStatus = MessageDecodingStatus()

            for (image in encodedImages) {
                val pixels = IntArray(image.width * image.height)

                image.getPixels(
                    pixels,
                    0,
                    image.width,
                    0,
                    0,
                    image.width,
                    image.height
                )

                val pixelBytes: ByteArray

                pixelBytes = ImageByteUtils.convertArray(pixels)

                decodeMessage(pixelBytes, messageDecodingStatus)

                if (messageDecodingStatus.isEnded)
                    break
            }

            return messageDecodingStatus.message
        }

        /**
         * Calculate the numbers of pixel needed
         *
         * @param message Message to encode
         * @return The number of pixel integer
         */
        fun numberOfPixelForMessage(message: String): Int {
            var msg = message

            msg += END_MESSAGE_SIGN
            msg = START_MESSAGE_SIGN + msg

            return msg.toByteArray(Charset.forName(ISO)).size * 4 / 3
        }

        private class MessageDecodingStatus internal constructor() {

            internal var message: String? = null
            internal var isEnded: Boolean = false
                private set

            init {
                message = ""
                isEnded = false
            }

            internal fun setEnded() {
                this.isEnded = true
            }


        }

        private class MessageEncodingStatus internal constructor(byteArrayMessage: ByteArray, var message: String?) {
            internal var isMessageEncoded: Boolean = false
                private set
            internal var currentMessageIndex: Int = 0
                set
            internal var byteArrayMessage: ByteArray? = null
                set

            init {
                this.isMessageEncoded = false
                this.currentMessageIndex = 0
                this.byteArrayMessage = byteArrayMessage
            }

            internal fun incrementMessageIndex() {
                currentMessageIndex++
            }

            internal fun setMessageEncoded() {
                this.isMessageEncoded = true
            }
        }
    }

}
