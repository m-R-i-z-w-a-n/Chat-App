package app.entertainment.chatapp.adapters

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import app.entertainment.chatapp.R
import app.entertainment.chatapp.databinding.ItemProfileImageDialogBinding
import app.entertainment.chatapp.databinding.ItemUserBinding
import app.entertainment.chatapp.models.User
import app.entertainment.chatapp.utils.CHATS_NODE
import app.entertainment.chatapp.utils.UserDiffUtil
import app.entertainment.chatapp.views.activities.ChatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class UsersAdapter(
    private val context: Context,
    private var users: List<User>,
    private val activity: Activity? = null
) :
    RecyclerView.Adapter<UsersAdapter.UserViewHolder>() {

    inner class UserViewHolder(val binding: ItemUserBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        return UserViewHolder(ItemUserBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun getItemCount(): Int = users.size

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val currentUser = users[position]

        holder.binding.apply {
            userName.text = currentUser.name
            Glide.with(context)
                .load(Uri.parse(currentUser.imageUri ?: return))
                .placeholder(R.drawable.avatar)
                .into(userImage)

            val senderUid = FirebaseAuth.getInstance().uid
            val receiverUid = currentUser.uid
            val senderRoom = senderUid + receiverUid
            val receiverRoom = receiverUid + senderUid

            // TODO: Find a way to remove it from UsersAdapter and move it to relevant repository class
            val dbRef = FirebaseDatabase.getInstance().reference

            // Get data from relevant room
            val targetRoom = if (currentUser.uid == senderUid) senderRoom else receiverRoom

            dbRef.child(CHATS_NODE).child(targetRoom)
                .child("latestMessage")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        latestMessage.text = snapshot.getValue(String::class.java) ?: "Tap to start a chat"
                    }

                    override fun onCancelled(error: DatabaseError) {
                        if (FirebaseAuth.getInstance().currentUser == null)
                            dbRef.child(CHATS_NODE).child(senderRoom)
                                .child("latestMessage").removeEventListener(this)
                    }
                })

            userImage.setOnClickListener {
                val image = (userImage.drawable as? BitmapDrawable)?.bitmap

                val dialogBinding =
                    ItemProfileImageDialogBinding.inflate(LayoutInflater.from(context), null, false)

                val dialog = Dialog(context).apply {
                    setContentView(dialogBinding.root)

                    dialogBinding.imageDialog.setImageBitmap(image)
                    dialogBinding.name.text = currentUser.name
                }
                dialog.show()
            }

            user.setOnClickListener {
                Intent(context, ChatActivity::class.java).also {
                    it.putExtra("uid", currentUser.uid)
                    it.putExtra("name", currentUser.name)
                    it.putExtra("image_uri", currentUser.imageUri)
                    context.startActivity(it)
                    activity?.finish()
                }
            }
        }
    }

    fun updateUsers(newUserList: List<User>) {
        CoroutineScope(Dispatchers.IO).launch {
            val userDiffUtil = UserDiffUtil(users, newUserList)
            val diffResults = DiffUtil.calculateDiff(userDiffUtil)
            withContext(Dispatchers.Main) {
                users = newUserList
                diffResults.dispatchUpdatesTo(this@UsersAdapter)
            }
        }
    }
}