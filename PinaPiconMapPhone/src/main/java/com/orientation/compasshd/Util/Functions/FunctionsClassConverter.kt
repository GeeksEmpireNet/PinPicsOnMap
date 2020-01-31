/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/30/20 4:30 PM
 * Last modified 1/30/20 3:07 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package net.geeksempire.chat.vicinity.Util.FunctionsClass

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.graphics.drawable.VectorDrawable
import android.util.Base64
import com.orientation.compasshd.R
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.URL
import java.nio.charset.Charset
import java.security.InvalidKeyException
import java.util.*
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec
import javax.inject.Inject
import javax.inject.Singleton

interface InterfaceConverter {

}
@Singleton
class FunctionsClassConverter @Inject constructor (var context: Context) : InterfaceConverter {

    @Throws(Exception::class)
    fun generateEncryptionKey(passwordKey: String): SecretKeySpec {

        return SecretKeySpec(passwordKey.toByteArray(), "AES")
    }

    private fun generatePasswordKey(rawString: String): String {
        val rawPasswordString = rawString + "0000000000000000"
        val passwordKey: String = rawPasswordString.substring(0, 16)
        return passwordKey
    }

    @Throws(InvalidKeyException::class)
    fun encryptEncodedData(plainText: String, rawString: String): ByteArray {
        //First Encode
        //Second Encrypt

        val encodedText: String = encodeStringBase64(plainText)

        val cipher = Cipher.getInstance("AES/ECB/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, generateEncryptionKey(generatePasswordKey(rawString)))

        return cipher.doFinal(encodedText.toByteArray(Charset.defaultCharset()))
    }

    @Throws(Exception::class)
    fun decryptEncodedData(encryptedByteArray: ByteArray, rawString: String): String? {
        //First Decrypt
        //Second Decode
        var plainText: String? = null

        try {
            var cipherD: Cipher? = null
            cipherD = Cipher.getInstance("AES/ECB/PKCS5Padding")
            cipherD!!.init(Cipher.DECRYPT_MODE, generateEncryptionKey(generatePasswordKey(rawString)))
            val decryptString = String(cipherD.doFinal(encryptedByteArray), Charset.defaultCharset())

            plainText = decodeStringBase64(decryptString)
        } catch (e: Exception) {
            e.printStackTrace()

            plainText = encryptedByteArray.toString()
        }

        return plainText
    }

    @Throws(Exception::class)
    fun encodeStringBase64(plainText: String): String {
        return Base64.encodeToString(plainText.toByteArray(), Base64.DEFAULT)
    }

    @Throws(Exception::class)
    fun decodeStringBase64(encodedText: String): String {
        return String(Base64.decode(encodedText, Base64.DEFAULT))
    }

    fun rawStringToByteArray(rawString: String): ByteArray {
        var listOfRawString = rawString.replace("[", "").replace("]", "").split(",")

        var resultByteArray = ByteArray(listOfRawString.size)
        for (aByte in listOfRawString.withIndex()) {
            try {
                resultByteArray[aByte.index] = aByte.value.replace("\\s".toRegex(), "").toByte()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        return resultByteArray
    }

    fun drawableToBitmap(drawable: Drawable?): Bitmap? {
        var bitmap: Bitmap? = null
        if (drawable is VectorDrawable) {
            val vectorDrawable = drawable as VectorDrawable?
            bitmap = Bitmap.createBitmap(
                    vectorDrawable!!.intrinsicWidth,
                    vectorDrawable.intrinsicHeight,
                    Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap!!)
            vectorDrawable.setBounds(0, 0, canvas.width, canvas.height)
            vectorDrawable.draw(canvas)
        } else if (drawable is BitmapDrawable) {
            val bitmapDrawable = drawable as BitmapDrawable?
            if (bitmapDrawable!!.bitmap != null) {
                bitmap = bitmapDrawable.bitmap
            }
        } else if (drawable is LayerDrawable) {
            val layerDrawable = drawable as LayerDrawable?

            bitmap = Bitmap.createBitmap(
                    layerDrawable!!.intrinsicWidth,
                    layerDrawable.intrinsicHeight,
                    Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(bitmap!!)
            layerDrawable.setBounds(0, 0, canvas.width, canvas.height)
            layerDrawable.draw(canvas)
        } else {
            bitmap = Bitmap.createBitmap(drawable!!.intrinsicWidth, drawable.intrinsicHeight, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap!!)
            drawable.setBounds(0, 0, canvas.width, canvas.height)
            drawable.draw(canvas)
        }
        return bitmap
    }

    fun bitmapToDrawable(bitmap: Bitmap?): Drawable {
        return BitmapDrawable(context.resources, bitmap)
    }

    fun convertImageUrlToDrawable(url: String): Drawable? {
        try {
            val inputStream = URL(url).content as InputStream
            return Drawable.createFromStream(inputStream, "src name")
        } catch (e: Exception) {
            return context.getDrawable(R.drawable.ic_launcher)!!
        }
    }

    fun byteToDrawable(byteImage: ByteArray): Drawable {
        val bitmap = BitmapFactory.decodeByteArray(byteImage, 0, byteImage.size)
        return bitmapToDrawable(bitmap)
    }

    fun drawableToByte(drawable: Drawable): ByteArray {
        val byteArrayOutputStream = ByteArrayOutputStream()
        drawableToBitmap(drawable)!!.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream)
        return byteArrayOutputStream.toByteArray()
    }

    fun convertMilliSecondToDate(milliSecond: Long, timeZone: TimeZone): String {

        val calendar = Calendar.getInstance()
        calendar.timeZone = timeZone
        calendar.timeInMillis = milliSecond

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)

        return "${hour}:${minute}:${second} | ${month}-${day}-${year}"
    }

    fun convertStringResourcesNameToID(resourceName: String): Int {
        return context.getResources().getIdentifier(resourceName, "string", context.packageName)
    }
}