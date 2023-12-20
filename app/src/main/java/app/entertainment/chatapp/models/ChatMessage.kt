package app.entertainment.chatapp.models

import java.text.SimpleDateFormat
import java.util.Date

data class ChatMessage(
    val senderId: String? = null,
    val receiverId: String? = null,
    val senderName: String? = null,
    val messageBody: String? = null,
    val messageTime: Long? = System.currentTimeMillis(),
    val messageDate: String? = SimpleDateFormat("dd-MM-yyyy").format(Date())
)
