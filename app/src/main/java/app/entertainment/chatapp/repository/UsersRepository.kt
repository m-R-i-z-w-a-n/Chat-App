package app.entertainment.chatapp.repository

import app.entertainment.chatapp.models.ChatMessage
import app.entertainment.chatapp.models.User
import app.entertainment.chatapp.utils.CHATS_NODE
import app.entertainment.chatapp.utils.MESSAGES_NODE
import app.entertainment.chatapp.utils.USER_NODE
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.tasks.await

class UsersRepository {
    private val dbRef by lazy { FirebaseDatabase.getInstance().reference }

    private val currentUser by lazy { FirebaseAuth.getInstance().currentUser }

    fun getUsers(callback: (List<User>) -> Unit) {
        dbRef.child(USER_NODE)
            .addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = mutableListOf<User>()
                for (child in snapshot.children) {
                    // Don't include currently signed in user to list
                    if (child.getValue(User::class.java)?.uid == currentUser?.uid)
                        continue
                    users.add(child.getValue(User::class.java) ?: continue)
                }
                callback(users)
            }

            override fun onCancelled(error: DatabaseError) {
                if (currentUser == null)
                    dbRef.removeEventListener(this)
            }
        })
    }

    suspend fun isChatInitiated(senderId: String, receiverId: String): Boolean {
        try {
            val chatRooms = dbRef.child(CHATS_NODE).get().await()

            for (room in chatRooms.children) {
                room.child(MESSAGES_NODE).children.forEach {
                    val message = it.getValue(ChatMessage::class.java)

                    if (message?.senderId == senderId && message.receiverId == receiverId)
                        return true
                }
            }

            return false
        } catch (ex: Exception) {
            throw ex
        }
    }

    suspend fun getUsers(): Flow<MutableList<User>> = callbackFlow {
        if (currentUser == null) {
            close(IllegalStateException("User not authenticated"))
            return@callbackFlow
        }

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val users = mutableListOf<User>()
                for (child in snapshot.children) {
                    val user = child.getValue(User::class.java)
                    // Don't include currently signed in user to list
                    if (user?.uid == currentUser?.uid)
                        continue
                    users.add(user ?: continue)
                }
                trySend(users)
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

        dbRef.child(USER_NODE).addValueEventListener(valueEventListener)

        awaitClose { dbRef.removeEventListener(valueEventListener) }
    }.catch {
        throw it
    }

    suspend fun addUserToDatabase(user: User) {
        try {
            dbRef.child(USER_NODE).child(user.uid!!).setValue(user).await()
        } catch (exception: Exception) {
            throw exception
        }
    }
}