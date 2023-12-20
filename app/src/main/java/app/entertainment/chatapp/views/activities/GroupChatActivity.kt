package app.entertainment.chatapp.views.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import app.entertainment.chatapp.R
import app.entertainment.chatapp.adapters.GroupChatAdapter
import app.entertainment.chatapp.databinding.ActivityGroupChatBinding
import app.entertainment.chatapp.models.ChatMessage
import app.entertainment.chatapp.models.Group
import app.entertainment.chatapp.viewmodel.CurrentUserViewModel
import app.entertainment.chatapp.viewmodel.GroupChatViewModel
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class GroupChatActivity : AppCompatActivity() {
    private val binding by lazy { ActivityGroupChatBinding.inflate(layoutInflater) }

    private val senderUid by lazy { FirebaseAuth.getInstance().uid }

    private val group by lazy { intent?.getSerializableExtra("group") as Group }

    private val groupName by lazy { intent?.getStringExtra("group_name") }

    private val messagesList by lazy { ArrayList<ChatMessage>() }

    private val chatViewModel by viewModels<GroupChatViewModel>()

    private val currentUserViewModel: CurrentUserViewModel by viewModels()

    private val toolbar by lazy { binding.groupChatToolbar }

    private lateinit var chatAdapter: GroupChatAdapter

    private lateinit var currentUserName: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = ContextCompat.getColor(this, R.color.border)
        setContentView(binding.root)

        setupToolBarItems()

        initRecyclerView()

        observeViewModel()

        group.name?.let { groupName ->
            chatViewModel.getMessages(groupName) {
                if (FirebaseAuth.getInstance().currentUser == null)
                    return@getMessages

                Toast.makeText(
                    this@GroupChatActivity,
                    "${javaClass.simpleName}: ${(it as? Exception)?.message}",
                    Toast.LENGTH_LONG
                ).show()
            }
        }

        binding.groupTypeMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.groupImgSend.visibility = View.VISIBLE
            }

            override fun afterTextChanged(s: Editable?) {
                if (s!!.trim().isEmpty())
                    binding.groupImgSend.visibility = View.GONE
            }
        })

        binding.groupImgSend.setOnClickListener {
            sendMessage()
        }
    }

    private fun initRecyclerView() {
        binding.groupRvSingleChat.apply {
            layoutManager = LinearLayoutManager(this@GroupChatActivity)
            setHasFixedSize(true)
            chatAdapter = GroupChatAdapter(this@GroupChatActivity, messagesList)
            adapter = chatAdapter
        }
    }

    private fun setupToolBarItems() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.groupChatProfileName.text = group.name ?: groupName

        binding.groupBackViewGroup.setOnClickListener {
            super.onBackPressed()
            finish()
        }

//        loadImageFromUrl(this@ChatActivity, userImageUri, object : ImageLoadCallback {
//            override fun onSuccess(bitmapDrawable: BitmapDrawable) {
//                runOnUiThread {
//                    binding.chatProfileImage.setImageDrawable(bitmapDrawable)
//                }
//            }
//
//            override fun onFailure(error: Exception) {
//                runOnUiThread {
//                    Toast.makeText(this@ChatActivity, error.message, Toast.LENGTH_LONG).show()
//                }
//            }
//        })
//
        Glide.with(this)
            .load(group.imageUri?.toUri() ?: R.drawable.avatar)
            .placeholder(R.drawable.avatar)
            .into(binding.groupChatProfileImage)
    }

    private fun observeViewModel() {
        chatViewModel.dataSnapshot.observe(this) { messages ->
            messagesList.clear()
            messagesList.addAll(messages)

            runOnUiThread {
                chatAdapter.updateChat(messagesList)
                binding.groupRvSingleChat.scrollToPosition(chatAdapter.itemCount - 1) // scroll to new message automatically
            }
        }

        currentUserViewModel.also { it.getCurrentUser() }.user.observe(this) {
            currentUserName = it?.name.toString()
        }
    }

    private fun sendMessage() {
        val edtMessageBody = binding.groupTypeMessage

        val chatMessage = ChatMessage(messageBody = edtMessageBody.text.toString(), senderId = senderUid, senderName = currentUserName)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                chatViewModel.sendMessage(group.name!!, chatMessage)
                withContext(Dispatchers.Main) {
                    edtMessageBody.text = null
                }
            } catch (exception: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@GroupChatActivity, exception.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_add_user, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.add_user -> {
                Intent(this@GroupChatActivity, ShowAllUsersActivity::class.java).also {
                    it.putExtra("group", group)
                    it.putExtra("source", javaClass.simpleName)
                    startActivity(it)
                }
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}