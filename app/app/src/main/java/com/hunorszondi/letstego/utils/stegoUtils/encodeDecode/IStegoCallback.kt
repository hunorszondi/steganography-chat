package com.hunorszondi.letstego.utils.stegoUtils.encodeDecode

/**
 * Callback interface, used for communication between Steganography util and it user.
 */
interface IStegoCallback {
    /**
     * Call when encode or decode starts
     */
    fun onStartProcess()

    /**
     * Call when encode or decode process is over
     * @param result SteganoData object
     */
    fun onCompleteProcess(result: SteganoData)
}