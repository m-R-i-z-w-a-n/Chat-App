package app.entertainment.chatapp.utils

import android.content.Context
import android.graphics.BitmapFactory
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import com.google.firebase.storage.StorageReference
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.BufferedInputStream
import java.io.IOException
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.TimeUnit

/**
 * Loads image from URL as drawable resource
 *
 * @param context The context of activity
 * @param url target url to convert to image resource
 * @param callback callback to save Drawable or Error messages
 */
fun loadImageFromUrl(context: Context, url: String?, callback: ImageLoadCallback) {
    var connection: HttpURLConnection? = null
    var inputStream: BufferedInputStream? = null

    CoroutineScope(Dispatchers.IO).launch {
        try {
            connection = URL(url).openConnection() as HttpURLConnection
            connection?.doInput = true
            connection?.connect()
            inputStream = BufferedInputStream(connection?.inputStream)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            callback.onSuccess(BitmapDrawable(context.resources, bitmap))
        } catch (exception: IOException) {
            exception.printStackTrace()
            callback.onFailure(exception)
        } finally {
            inputStream?.close()
            connection?.disconnect()
        }
    }
}

/**
 * Uploads image to firebase storage and returns its url
 *
 * @param imageUri local uri of image
 * @param imageReference storage reference of file to be created in firebase storage
 *
 * @return url of uploaded image or throw exception if something goes wrong
 */
suspend fun getImageDownloadUrl(
    imageUri: String,
    imageReference: StorageReference,
): String {
    return try {
        imageReference.putFile(Uri.parse(imageUri)).await()
        imageReference.downloadUrl.await().toString()
    } catch (exception: Exception) {
        throw exception
    }
}

/**
 * Converts a duration in milliseconds to a string representation in the format HH:MM (hours and minutes).
 *
 * @param duration The duration in milliseconds to be converted.
 * @return A string in the format HH:MM representing the converted duration.
 */
fun convertToHHMM(duration: Long): String {
    // Convert the duration to minutes and seconds
    val hours =
        (TimeUnit.MILLISECONDS.toHours(duration) % TimeUnit.DAYS.toHours(1)) + 5L // Added 5 to get current timezone
    val minutes = TimeUnit.MILLISECONDS.toMinutes(duration) % TimeUnit.HOURS.toMinutes(1)

    // Format the hours and minutes as HH:MM
    return String.format("%02d:%02d", hours, minutes)
}
