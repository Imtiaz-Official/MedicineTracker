package com.example.medicinetracker.util

import android.content.Context
import java.io.InputStream
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

object AssetEncryptionUtil {
    private const val ALGORITHM = "AES/CBC/PKCS5Padding"
    
    init {
        System.loadLibrary("medicinetracker")
    }

    private external fun getNativeKey(): String
    private external fun getNativeIV(): String

    fun getDecryptedStream(context: Context, fileName: String): InputStream {
        val encryptedStream = context.assets.open(fileName)
        
        val key = getNativeKey().toByteArray()
        val iv = getNativeIV().toByteArray()
        
        val cipher = Cipher.getInstance(ALGORITHM)
        val keySpec = SecretKeySpec(key, "AES")
        val ivSpec = IvParameterSpec(iv)
        
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)
        
        return javax.crypto.CipherInputStream(encryptedStream, cipher)
    }
}
