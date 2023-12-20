package app.entertainment.chatapp.utils

import android.graphics.drawable.BitmapDrawable

interface ImageLoadCallback {
    fun onSuccess(bitmapDrawable: BitmapDrawable)
    fun onFailure(error: Exception)
}

interface ImageUrlCallbackResponse {
    fun onSuccess(url: String?)
    fun onFailure(error: Exception)
}

interface ChatCallbackResponse {
    fun onSuccess(response : Any? = null)
    fun onFailure(error: Any)
}

interface LoginCallback {
    fun onSuccess(response: String)
    fun onFailure(error: Exception)
}
