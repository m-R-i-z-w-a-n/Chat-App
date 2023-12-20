package app.entertainment.chatapp.models

import java.io.Serializable

data class User(
    val phoneNumber: String? = null,
    val name: String? = null,
    val imageUri: String? = null,
    val latestMessage: String? = null,
    val uid: String? = null
) : Serializable
