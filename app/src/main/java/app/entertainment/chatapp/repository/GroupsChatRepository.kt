package app.entertainment.chatapp.repository

import app.entertainment.chatapp.models.ChatMessage
import app.entertainment.chatapp.utils.CHATS_NODE
import app.entertainment.chatapp.utils.ChatCallbackResponse
import app.entertainment.chatapp.utils.GROUPS_NODE
import app.entertainment.chatapp.utils.MESSAGES_NODE
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.launch

class GroupsChatRepository {
    private val dbRef by lazy { Firebase.database.reference }
    private val currentUser by lazy { Firebase.auth.currentUser }

    suspend fun sendMessage(
        groupName: String,
        chatMessage: ChatMessage
    ) {
        val randomKey = dbRef.push().key

        val latestMessage = mapOf("latestMessage" to chatMessage.messageBody)
        try {
            coroutineScope {
                launch(Dispatchers.IO) {
                    async{
                        dbRef.child(GROUPS_NODE).child(groupName)
                            .child(MESSAGES_NODE).child(randomKey!!)
                            .setValue(chatMessage)
                    }.await()

                    async{ dbRef.child(GROUPS_NODE).child(groupName).updateChildren(latestMessage) }.await()
                }
            }
        } catch (exception: Exception) {
            throw exception
        }
    }

    fun getMessages(groupName: String, callback: ChatCallbackResponse) {
        dbRef.child(GROUPS_NODE)
            .child(groupName)
            .child(MESSAGES_NODE)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    callback.onSuccess(snapshot)
                }

                override fun onCancelled(error: DatabaseError) {
                    if (currentUser == null) {
                        dbRef.removeEventListener(this)
                        return
                    }

                    callback.onFailure(error)
                }
            })
    }

    fun getLatestMessage(groupName: String, callback: ChatCallbackResponse) {
        dbRef.child(GROUPS_NODE).child(groupName)
            .child("latestMessage")
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    callback.onSuccess(snapshot.getValue(String::class.java) as String)
                }

                override fun onCancelled(error: DatabaseError) {
                    if (currentUser == null)
                        dbRef.removeEventListener(this)
                    callback.onFailure(error.toException())
                }
            })
    }

    suspend fun getMessages(senderRoom: String): Flow<DataSnapshot> = callbackFlow {
        if (currentUser == null) {
            close(IllegalStateException("User not authenticated"))
            return@callbackFlow
        }

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                trySend(snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                if (currentUser != null)
                    close(error.toException())
            }

        }

        if (currentUser == null) {
            dbRef.removeEventListener(valueEventListener)
            return@callbackFlow
        }

        dbRef.child(CHATS_NODE).child(senderRoom)
            .child(MESSAGES_NODE).addValueEventListener(valueEventListener)

        awaitClose {
            dbRef.child(CHATS_NODE).child(senderRoom).child(MESSAGES_NODE)
                .removeEventListener(valueEventListener)
        }
    }.catch {
        throw it
    }
}