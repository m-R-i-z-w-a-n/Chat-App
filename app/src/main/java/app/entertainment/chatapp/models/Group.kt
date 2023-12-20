package app.entertainment.chatapp.models

import java.io.Serializable

data class Group(
    val name: String? = null,
    val imageUri: String? = null,
    val groupMembers: List<User>? = null
) : Serializable
