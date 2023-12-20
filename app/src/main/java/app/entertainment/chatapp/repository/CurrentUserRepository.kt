package app.entertainment.chatapp.repository

import app.entertainment.chatapp.models.User
import app.entertainment.chatapp.utils.USER_NODE
import app.entertainment.chatapp.utils.getImageDownloadUrl
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.tasks.await

class CurrentUserRepository {
    private val dbRef by lazy { FirebaseDatabase.getInstance().reference }
    private val storageRef by lazy { FirebaseStorage.getInstance().reference }

    private val currentUser by lazy { FirebaseAuth.getInstance().currentUser }
    private val currentNodeRef by lazy {
        currentUser?.uid?.let {
            dbRef.child(USER_NODE).child(it)
        }
    }

    private val imageReference by lazy { currentUser?.uid?.let { storageRef.child("profiles/$it") } }

    private lateinit var user: User

    fun getCurrentUser(callback: (User) -> Unit) {
        dbRef.child(USER_NODE)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    for (child in snapshot.children) {
                        user = child.getValue(User::class.java)!!
                        if (user.uid == currentUser?.uid)
                            break
                    }
                    callback(user)
                }

                override fun onCancelled(error: DatabaseError) {
                    if (currentUser == null)
                        dbRef.removeEventListener(this)
                }
            })
    }

    suspend fun getCurrentUser(): Flow<User> = callbackFlow {
        if (currentUser == null) {
            close(IllegalStateException("User not authenticated"))
            return@callbackFlow
        }

        val valueEventListener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (child in snapshot.children) {
                    user = child.getValue(User::class.java)!!
                    if (user.uid == currentUser?.uid)
                        break
                }
                trySend(user)
            }

            override fun onCancelled(error: DatabaseError) {
                if (currentUser != null)
                    close(error.toException())
            }
        }

        dbRef.child(USER_NODE).addValueEventListener(valueEventListener)

        awaitClose { dbRef.child(USER_NODE).removeEventListener(valueEventListener) }
    }.catch {
        throw it
    }

    suspend fun updateUserInfo(
        newValue: MutableMap<String, String?>
    ) {
        val imageUri = newValue["imageUri"]
        if (newValue["name"] == user.name && imageUri == null)
            throw Exception("Update information first!")

        try {
            imageUri?.let {
                if (imageUri.startsWith("content://"))
                    newValue["imageUri"] = getImageDownloadUrl(imageUri, imageReference!!)
                currentNodeRef?.updateChildren(newValue as Map<String, Any?>)?.await()

            } ?: currentNodeRef?.updateChildren(newValue as Map<String, Any?>)?.await()
        } catch (exception: Exception) {
            throw exception
        }
    }
}
