package com.hunorszondi.letstego.utils.stegoUtils.utils

import android.annotation.SuppressLint
import android.util.Log

import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

/**
 * Helps in data encryption and decryption
 */
class CryptoUtil {

   companion object {
       private const val CRYPTO_TYPE = "AES"
       /**
        * Encryption Method, working with AES
        *
        * @param message to encrypt,
        * @param secretKey key for encryption
        * @return Encrypted Message, String
        */
       @SuppressLint("GetInstance")
       @Throws(Exception::class)
       fun encryptMessage(message: String, secretKey: String): String {

           val aesKey = SecretKeySpec(secretKey.toByteArray(), CRYPTO_TYPE)
           val cipher: Cipher = Cipher.getInstance(CRYPTO_TYPE)

           cipher.init(Cipher.ENCRYPT_MODE, aesKey)

           return android.util.Base64.encodeToString(cipher.doFinal(message.toByteArray()), 0)
       }

       /**
        * Decryption Method
        *
        * @param encryptedMessage to decrypt,
        * @param secretKey key for decryption
        * @return Message, String
        */
       @SuppressLint("GetInstance")
       @Throws(Exception::class)
       fun decryptMessage(encryptedMessage: String, secretKey: String): String {

           Log.d("Decrypt", "message: + $encryptedMessage")
           val aesKey = SecretKeySpec(secretKey.toByteArray(), CRYPTO_TYPE)
           val cipher: Cipher = Cipher.getInstance(CRYPTO_TYPE)

           cipher.init(Cipher.DECRYPT_MODE, aesKey)
           val decrypted: String
           val decoded: ByteArray = android.util.Base64.decode(encryptedMessage.toByteArray(), 0)
           decrypted = String(cipher.doFinal(decoded))

           return decrypted
       }
   }

}