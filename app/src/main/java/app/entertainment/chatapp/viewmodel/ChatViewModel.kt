package app.entertainment.chatapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.entertainment.chatapp.models.ChatMessage
import app.entertainment.chatapp.repository.ChatRepository
import app.entertainment.chatapp.utils.ChatCallbackResponse
import com.google.firebase.database.DataSnapshot
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class ChatViewModel(
    private val repository: ChatRepository = ChatRepository()
) : ViewModel() {
    private val mChatMessages = MutableLiveData<List<ChatMessage>>()
    private val mMessages = MutableLiveData<List<ChatMessage>>()

    val chatMessages: LiveData<List<ChatMessage>>
        get() = mChatMessages

    fun sendMessage(
        senderRoom: String,
        receiverRoom: String,
        receiverUid: String,
        chatMessage: ChatMessage
    ) = viewModelScope.launch(Dispatchers.IO) {
        try {
            repository.sendMessage(senderRoom, receiverRoom, receiverUid, chatMessage)
        } catch (exception: Exception) {
            throw exception
        }
    }

    fun getMessages(senderRoom: String, callback: (Any?) -> Unit) {
        repository.getMessages(senderRoom, object : ChatCallbackResponse {
            override fun onSuccess(response: Any?) {
                val messagesList = mutableListOf<ChatMessage>()
                for (child in (response as DataSnapshot).children)
                    child.getValue(ChatMessage::class.java)?.let { messagesList.add(it) }
                mChatMessages.postValue(messagesList)
            }

            override fun onFailure(error: Any) {
                callback(error)
            }
        })
    }

    fun getLatestMessage(senderRoom: String, callback: ChatCallbackResponse) {
        repository.getLatestMessage(senderRoom, callback)
    }

    override fun onCleared() {
        viewModelScope.cancel()
        super.onCleared()
    }
}