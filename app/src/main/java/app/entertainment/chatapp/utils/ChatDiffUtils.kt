package app.entertainment.chatapp.utils

import androidx.recyclerview.widget.DiffUtil
import app.entertainment.chatapp.models.ChatMessage

class ChatDiffUtils(
    private val oldChats: List<ChatMessage>,
    private val newChats: List<ChatMessage>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldChats.size

    override fun getNewListSize(): Int = newChats.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldChats[oldItemPosition].javaClass == newChats[newItemPosition].javaClass
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldChats[oldItemPosition].hashCode() == newChats[newItemPosition].hashCode()
    }
}