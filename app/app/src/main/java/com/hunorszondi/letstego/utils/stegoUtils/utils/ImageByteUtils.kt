package com.hunorszondi.letstego.utils.stegoUtils.utils

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log

import java.util.ArrayList

class ImageByteUtils {

    companion object {
        private val SQUARE_BLOCK_SIZE = 512
        private val TAG = ImageByteUtils::class.java.name

        /**
         * Calculates the number of square block needed
         *
         * @param pixels number of pixels Integer
         * @return number of Square blocks Integer
         */
        fun squareBlockNeeded(pixels: Int): Int {
            val result: Int

            val quadratic = SQUARE_BLOCK_SIZE * SQUARE_BLOCK_SIZE
            val divisor = pixels / quadratic
            val remainder = pixels % quadratic

            result = divisor + if (remainder > 0) 1 else 0

            return result
        }

        /**
         * Splits the image into many images of ( SQUARE_BLOCK_SIZE ^2 ) size.
         *
         * @param bitmap original image in bitmap
         * @return List of splitted images List
         */
        fun splitImage(bitmap: Bitmap): List<Bitmap> {

            var chunkHeight: Int
            var chunkWidth: Int

            val chunkedImages = ArrayList<Bitmap>()

            var rows = bitmap.height / SQUARE_BLOCK_SIZE
            var cols = bitmap.width / SQUARE_BLOCK_SIZE

            val chunkHeightMod = bitmap.height % SQUARE_BLOCK_SIZE
            val chunkWidthMod = bitmap.width % SQUARE_BLOCK_SIZE

            if (chunkHeightMod > 0)
                rows++
            if (chunkWidthMod > 0)
                cols++

            var yCoordinate = 0

            // chunk each row
            for (x in 0 until rows) {

                var xCoordinate = 0

                // chunk each column
                for (y in 0 until cols) {

                    chunkHeight = SQUARE_BLOCK_SIZE
                    chunkWidth = SQUARE_BLOCK_SIZE

                    if (y == cols - 1 && chunkWidthMod > 0)
                        chunkWidth = chunkWidthMod

                    if (x == rows - 1 && chunkHeightMod > 0)
                        chunkHeight = chunkHeightMod

                    chunkedImages.add(Bitmap.createBitmap(bitmap, xCoordinate, yCoordinate, chunkWidth, chunkHeight))
                    xCoordinate += SQUARE_BLOCK_SIZE

                }

                yCoordinate += SQUARE_BLOCK_SIZE
            }

            return chunkedImages
        }

        /**
         * Merge all the chunk image list into one single image
         *
         * @param images List<Bitmap>,
         * @param originalHeight Integer
         * @param originalWidth Integer
         * @return Merged Image Bitmap
         */
        fun mergeImage(images: List<Bitmap>, originalHeight: Int, originalWidth: Int): Bitmap {

            var rows = originalHeight / SQUARE_BLOCK_SIZE
            var cols = originalWidth / SQUARE_BLOCK_SIZE

            val chunkHeightMod = originalHeight % SQUARE_BLOCK_SIZE
            val chunkWidthMod = originalWidth % SQUARE_BLOCK_SIZE

            if (chunkHeightMod > 0)
                rows++
            if (chunkWidthMod > 0)
                cols++

            Log.d(TAG, "Size width $originalWidth size height $originalHeight")
            val bitmap = Bitmap.createBitmap(originalWidth, originalHeight, Bitmap.Config.ARGB_4444)

            val canvas = Canvas(bitmap)

            var count = 0

            for (irows in 0 until rows) {
                for (icols in 0 until cols) {

                    canvas.drawBitmap(
                        images[count],
                        (SQUARE_BLOCK_SIZE * icols).toFloat(),
                        (SQUARE_BLOCK_SIZE * irows).toFloat(),
                        Paint()
                    )
                    count++

                }
            }

            return bitmap
        }

        /**
         * Converts the byte array to an integer array.
         *
         * @param byteArray Byte array
         * @return Integer Array
         */
        fun byteArrayToIntArray(byteArray: ByteArray): IntArray {
            val size = byteArray.size / 3

            System.runFinalization()
            System.gc()

            val intArray = IntArray(size)
            var offset = 0
            var index = 0

            while (offset < byteArray.size) {
                intArray[index++] = byteArrayToInt(byteArray, offset)
                offset += 3
            }

            return intArray
        }

        /**
         * Convert the byte array to an int.
         *
         * @param byteArray Byte array
         * @return Integer
         */
        fun byteArrayToInt(byteArray: ByteArray): Int {
            return byteArrayToInt(byteArray, 0)
        }

        /**
         * Convert the byte array to an int starting from the given offset.
         *
         * @param byteArray {the byte array},
         * @param offset {integer}
         * @return Integer
         */
        private fun byteArrayToInt(byteArray: ByteArray, offset: Int): Int {
            var value = 0x00000000
            val mask = 0x000000FF

            for (i in 0..2) {
                val shift = (3 - 1 - i) * 8
                value = value or ((byteArray[i + offset].toInt() and mask) shl shift)
            }

            value = value and 0x00FFFFFF

            return value
        }

        /**
         * Convert integer argbArray representing [argb] values to byte rgbArray
         * representing [rgb] values
         *
         * @param argbArray Integer argbArray representing [argb] values.
         * @return byte Array representing [rgb] values.
         */
        fun convertArray(argbArray: IntArray): ByteArray {

            val rgbArray = ByteArray(argbArray.size * 3)

            for (i in argbArray.indices) {
                rgbArray[i * 3] = (argbArray[i] shr 16 and 0xFF).toByte()
                rgbArray[i * 3 + 1] = (argbArray[i] shr 8 and 0xFF).toByte()
                rgbArray[i * 3 + 2] = (argbArray[i] and 0xFF).toByte()
            }

            return rgbArray
        }

        /**
         * Check whether the string is empty of not
         *
         * @param str String
         * @return boolean
         */
        fun isStringEmpty(str: String?): Boolean {
            var string = str
            var result = true

            if (string == null)
            else {
                string = string.trim { it <= ' ' }
                if (string.isNotEmpty() && string != "undefined")
                    result = false
            }

            return result
        }
    }

}
