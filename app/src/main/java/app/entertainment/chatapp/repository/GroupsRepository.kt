package app.entertainment.chatapp.repository

import app.entertainment.chatapp.models.Group
import app.entertainment.chatapp.models.User
import app.entertainment.chatapp.utils.GROUPS_NODE
import app.entertainment.chatapp.utils.getImageDownloadUrl
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.tasks.await

class GroupsRepository {
    private val dbRef by lazy { Firebase.database.reference }
    private val groupsRef = dbRef.child(GROUPS_NODE)

    private val currentUser by lazy { Firebase.auth.currentUser }

    private val storageRef by lazy { FirebaseStorage.getInstance().reference }
    private val imageReference by lazy { storageRef.child("groups") }

    fun getGroups(callback: (List<Group>) -> Unit) {
        groupsRef
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val groups = mutableListOf<Group>()
                    for (child in snapshot.children)
                        if (child.child("users").hasChild(currentUser?.uid!!))
                            child.getValue(Group::class.java)?.let { groups.add(it) }

                    callback(groups)
                }

                override fun onCancelled(error: DatabaseError) {
                    if (currentUser == null)
                        dbRef.removeEventListener(this)
                }
            })
    }

    suspend fun createGroup(groupName: String, profileImageUri: String? = null) {
        try {
            val groups = groupsRef.get().await()

            for (group in groups.children) {
                if (group.key?.lowercase() == groupName.lowercase()) {
                    throw Exception("There is already a group with that name, choose a different name")
                }
            }

            var imageUrl: String? = null
            if (profileImageUri.toString().startsWith("content://"))
                imageUrl = profileImageUri?.let {
                    getImageDownloadUrl(
                        it,
                        imageReference.child(groupName)
                    )
                }

            val profileImg = mapOf("name" to groupName, "imageUri" to imageUrl)

            groupsRef.child(groupName).setValue(profileImg).await()
        } catch (ex: Exception) {
            throw ex
        }
    }

    suspend fun getGroupUsers(group: Group): DataSnapshot = groupsRef.child(group.name!!).child("users").get().await()

    suspend fun addUserToGroup(groupName: String, user: User) {
        try {
            val userMap = mapOf("name" to user.name, "uid" to user.uid)

            groupsRef.child(groupName)
                .child("users").child(user.uid!!)
                .updateChildren(userMap).await()
        } catch (ex: Exception) {
            throw ex
        }
    }
}