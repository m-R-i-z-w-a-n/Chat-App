package app.entertainment.chatapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import app.entertainment.chatapp.databinding.ItemReceivedGroupBinding
import app.entertainment.chatapp.databinding.ItemSentBinding
import app.entertainment.chatapp.models.ChatMessage
import app.entertainment.chatapp.utils.ChatDiffUtils
import app.entertainment.chatapp.utils.convertToHHMM
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GroupChatAdapter(private val context: Context, private var chatMessages: List<ChatMessage>): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    inner class SenderViewHolder(val binding: ItemSentBinding) : RecyclerView.ViewHolder(binding.root)

    inner class ReceiverViewHolder(val binding: ItemReceivedGroupBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_SENT)
            SenderViewHolder(ItemSentBinding.inflate(LayoutInflater.from(context), parent, false))
        else
            ReceiverViewHolder(ItemReceivedGroupBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun getItemViewType(position: Int): Int {
        val currentChat = chatMessages[position]

        return if (FirebaseAuth.getInstance().uid.equals(currentChat.senderId))
            ITEM_SENT
        else
            ITEM_RECEIVED
    }

    override fun getItemCount(): Int = chatMessages.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val currentMessage = chatMessages[position]
        if (holder.javaClass == SenderViewHolder::class.java) {
            val viewHolder = holder as SenderViewHolder
            viewHolder.binding.apply {
                messageSent.text = currentMessage.messageBody
                timestampMessageSent.text = convertToHHMM(currentMessage.messageTime!!)
                dateSent.text = currentMessage.messageDate ?: ""
            }
        }
        else {
            val viewHolder = holder as ReceiverViewHolder
            viewHolder.binding.apply {
                messageReceivedGroup.text = currentMessage.messageBody
                nameTag.text = currentMessage.senderName
                timestampMessageReceivedGroup.text = convertToHHMM(currentMessage.messageTime!!)
                dateReceivedGroup.text = currentMessage.messageDate ?: ""
            }

        }
    }

    fun updateChat(newChats: List<ChatMessage>) {
        CoroutineScope(Dispatchers.IO).launch {
            val chatDiffUtil = ChatDiffUtils(chatMessages, newChats)
            val diffResults = DiffUtil.calculateDiff(chatDiffUtil)
            withContext(Dispatchers.Main) {
                chatMessages = newChats
                diffResults.dispatchUpdatesTo(this@GroupChatAdapter)
            }
        }
    }

    companion object {
        private const val ITEM_SENT = 1
        private const val ITEM_RECEIVED = 2
    }
}