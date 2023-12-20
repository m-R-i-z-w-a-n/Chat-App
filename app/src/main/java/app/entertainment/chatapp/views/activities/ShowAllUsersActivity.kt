package app.entertainment.chatapp.views.activities

import android.app.ProgressDialog
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import app.entertainment.chatapp.R
import app.entertainment.chatapp.adapters.SelectUsersAdapter
import app.entertainment.chatapp.adapters.UsersAdapter
import app.entertainment.chatapp.databinding.ActivityShowAllUsersBinding
import app.entertainment.chatapp.models.Group
import app.entertainment.chatapp.models.User
import app.entertainment.chatapp.viewmodel.GroupsViewModel
import app.entertainment.chatapp.viewmodel.UsersViewModel
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ShowAllUsersActivity : AppCompatActivity() {
    private val binding by lazy { ActivityShowAllUsersBinding.inflate(layoutInflater) }

    private val usersViewModel by viewModels<UsersViewModel>()
    private val groupsViewModel by viewModels<GroupsViewModel>()

    private lateinit var selectUsersAdapter: SelectUsersAdapter
    private lateinit var usersAdapter: UsersAdapter

    private val group by lazy { intent?.getSerializableExtra("group") as? Group }

    private val intentSource by lazy { intent?.getStringExtra("source") }

    private val senderUid by lazy { FirebaseAuth.getInstance().uid }
    private lateinit var receiverUid: String

    private val progressDialog by lazy {
        ProgressDialog(this).also {
            it.setMessage("Loading users...")
            it.setCancelable(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        setContentView(binding.root)
    }

    override fun onResume() {
        super.onResume()

        initRecyclerView()
    }

    private fun initRecyclerView() {
        val users = mutableListOf<User>()

        binding.showUsers.rvUsers.apply {
            layoutManager = LinearLayoutManager(this@ShowAllUsersActivity)
            setHasFixedSize(true)
            if (group == null) {
                usersAdapter =
                    UsersAdapter(this@ShowAllUsersActivity, users, this@ShowAllUsersActivity)
                adapter = usersAdapter
            } else {
                selectUsersAdapter =
                    SelectUsersAdapter(this@ShowAllUsersActivity, group!!, users) { currentUser ->
                        groupsViewModel.addUserToGroup(group?.name!!, currentUser)

                        Toast.makeText(this@ShowAllUsersActivity, "Added ${currentUser.name} to group ${group?.name}", Toast.LENGTH_SHORT).show()
                        super.onBackPressed()
                    }
                adapter = selectUsersAdapter
            }
        }

        usersViewModel.users.observe(this) { userList ->
            progressDialog.show()
            lifecycleScope.launch(Dispatchers.IO) {
                val mutableUserList = filterUsersList(userList)

                withContext(Dispatchers.Main) {
                    progressDialog.cancel()
                    if (mutableUserList.isEmpty())
                        binding.noUsersNotice.visibility = View.VISIBLE

                    group?.let {
                        selectUsersAdapter.updateUsers(mutableUserList)
                    } ?: usersAdapter.updateUsers(mutableUserList)
                }
            }
        }
    }

    private suspend fun filterUsersList(userList: List<User>): MutableList<User> {
        val mutableUserList = userList.toMutableList()

        if (intentSource == "GroupChatActivity") {
            val iterator = mutableUserList.iterator()
            while (iterator.hasNext()) {
                val user = iterator.next()
                if (groupsViewModel.isUserInGroup(group!!, user))
                    iterator.remove()
            }
        } else {
            val iterator = mutableUserList.iterator()
            while (iterator.hasNext()) {
                val user = iterator.next()
                receiverUid = user.uid!!

                try {
                    if (usersViewModel.isChatInitiated(senderUid!!, receiverUid))
                        iterator.remove()
                } catch (ex: Exception) {
                    Toast.makeText(this@ShowAllUsersActivity, ex.message, Toast.LENGTH_SHORT).show()
                }
            }
        }

        return mutableUserList
    }
}