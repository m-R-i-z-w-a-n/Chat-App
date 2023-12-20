package app.entertainment.chatapp.adapters

import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.net.toUri
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import app.entertainment.chatapp.R
import app.entertainment.chatapp.databinding.ItemGroupBinding
import app.entertainment.chatapp.databinding.ItemProfileImageDialogBinding
import app.entertainment.chatapp.models.Group
import app.entertainment.chatapp.utils.GROUPS_NODE
import app.entertainment.chatapp.utils.GroupDiffUtil
import app.entertainment.chatapp.views.activities.GroupChatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GroupsAdapter(private val context: Context, private var groups: List<Group>) :
    RecyclerView.Adapter<GroupsAdapter.GroupViewHolder>() {

    inner class GroupViewHolder(val binding: ItemGroupBinding) :
        RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder {
        return GroupViewHolder(ItemGroupBinding.inflate(LayoutInflater.from(context), parent, false))
    }

    override fun getItemCount(): Int = groups.size

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        val currentGroup = groups[position]

        holder.binding.apply {
            groupName.text = currentGroup.name
            Glide.with(context)
                .load(currentGroup.imageUri?.toUri() ?: R.drawable.avatar)
                .placeholder(R.drawable.avatar)
                .into(groupImage)

            // TODO: Find a way to remove it from GroupsAdapter and move it to relevant repository class
            val dbRef = Firebase.database.reference

            dbRef.child(GROUPS_NODE).child(currentGroup.name!!)
                .child("latestMessage")
                .addValueEventListener(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        groupLatestMessage.text = snapshot.getValue(String::class.java)
                    }

                    override fun onCancelled(error: DatabaseError) {
                        if (FirebaseAuth.getInstance().currentUser == null)
                            dbRef.removeEventListener(this)
                    }
                })

            group.setOnClickListener {
                Intent(context, GroupChatActivity::class.java).also {
                    it.putExtra("group", currentGroup)
                    context.startActivity(it)
                }
            }

            groupImage.setOnClickListener {
                val image = (groupImage.drawable as? BitmapDrawable)?.bitmap

                val dialogBinding =
                    ItemProfileImageDialogBinding.inflate(LayoutInflater.from(context), null, false)

                val dialog = Dialog(context).apply {
                    setContentView(dialogBinding.root)

                    dialogBinding.imageDialog.setImageBitmap(image)
                    dialogBinding.name.text = currentGroup.name
                }
                dialog.show()
            }
        }
    }

    fun updateGroups(newGroupList: List<Group>) {
        CoroutineScope(Dispatchers.IO).launch {
            val userDiffUtil = GroupDiffUtil(groups, newGroupList)
            val diffResults = DiffUtil.calculateDiff(userDiffUtil)
            withContext(Dispatchers.Main) {
                groups = newGroupList
                diffResults.dispatchUpdatesTo(this@GroupsAdapter)
            }
        }
    }

}