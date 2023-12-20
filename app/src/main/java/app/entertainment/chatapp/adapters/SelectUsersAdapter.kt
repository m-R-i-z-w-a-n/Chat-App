package app.entertainment.chatapp.adapters

import android.app.Dialog
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import app.entertainment.chatapp.R
import app.entertainment.chatapp.databinding.ItemProfileImageDialogBinding
import app.entertainment.chatapp.databinding.ItemUserBinding
import app.entertainment.chatapp.models.Group
import app.entertainment.chatapp.models.User
import app.entertainment.chatapp.utils.GROUPS_NODE
import app.entertainment.chatapp.utils.UserDiffUtil
import com.bumptech.glide.Glide
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SelectUsersAdapter(
    private val context: Context,
    private val group: Group,
    private var users: List<User>,
    private val onItemClickCallback: (user: User) -> Unit
) :
    RecyclerView.Adapter<SelectUsersAdapter.UserViewHolder>() {

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

            latestMessage.text = null

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
                onItemClickCallback(currentUser)
            }
        }
    }

    fun updateUsers(newUserList: List<User>) {
        CoroutineScope(Dispatchers.IO).launch {
            val userDiffUtil = UserDiffUtil(users, newUserList)
            val diffResults = DiffUtil.calculateDiff(userDiffUtil)
            withContext(Dispatchers.Main) {
                users = newUserList
                diffResults.dispatchUpdatesTo(this@SelectUsersAdapter)
            }
        }
    }
}
