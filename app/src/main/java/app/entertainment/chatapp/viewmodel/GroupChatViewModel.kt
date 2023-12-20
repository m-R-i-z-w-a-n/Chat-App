package app.entertainment.chatapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.entertainment.chatapp.models.ChatMessage
import app.entertainment.chatapp.repository.GroupsChatRepository
import app.entertainment.chatapp.utils.ChatCallbackResponse
import com.google.firebase.database.DataSnapshot
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class GroupChatViewModel(
    private val repository: GroupsChatRepository = GroupsChatRepository()
) : ViewModel() {
    private val mDataSnapshot = MutableLiveData<List<ChatMessage>>()
    private val mMessages = MutableLiveData<List<ChatMessage>>()

    val dataSnapshot: LiveData<List<ChatMessage>>
        get() = mDataSnapshot

    fun sendMessage(groupName: String, chatMessage: ChatMessage) =
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.sendMessage(groupName, chatMessage)
            } catch (exception: Exception) {
                throw exception
            }
        }

    fun getMessages(groupName: String, callback: (Any) -> Unit) {
        repository.getMessages(groupName, object : ChatCallbackResponse {
            override fun onSuccess(response: Any?) {
                val messagesList = mutableListOf<ChatMessage>()
                for (child in (response as DataSnapshot).children)
                    child.getValue(ChatMessage::class.java)?.let { messagesList.add(it) }
                mDataSnapshot.postValue(messagesList)
            }

            override fun onFailure(error: Any) {
                callback(error)
            }
        })
    }

    fun getLatestMessage(groupName: String, callback: ChatCallbackResponse) {
        repository.getLatestMessage(groupName, callback)
    }

    override fun onCleared() {
        viewModelScope.cancel()
        super.onCleared()
    }
}