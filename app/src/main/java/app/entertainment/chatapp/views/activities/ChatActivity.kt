package app.entertainment.chatapp.views.activities

import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import app.entertainment.chatapp.R
import app.entertainment.chatapp.adapters.ChatAdapter
import app.entertainment.chatapp.databinding.ActivityChatBinding
import app.entertainment.chatapp.models.ChatMessage
import app.entertainment.chatapp.viewmodel.ChatViewModel
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChatActivity : AppCompatActivity() {
    private val binding by lazy { ActivityChatBinding.inflate(layoutInflater) }

    private val senderUid by lazy { FirebaseAuth.getInstance().uid }

    private val receiverUid by lazy { intent?.getStringExtra("uid") }
    private val receiverUserName by lazy { intent?.getStringExtra("name") }

    private val userImageUri by lazy { intent?.getStringExtra("image_uri") }

    private val messagesList by lazy { ArrayList<ChatMessage>() }

    private val chatViewModel by viewModels<ChatViewModel>()

    private val toolbar by lazy { binding.chatToolbar }

    private lateinit var chatAdapter: ChatAdapter

    private lateinit var senderRoom: String
    private lateinit var receiverRoom: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = ContextCompat.getColor(this, R.color.border)
        setContentView(binding.root)

        setupToolBarItems()

        senderRoom = senderUid + receiverUid
        receiverRoom = receiverUid + senderUid

        initRecyclerView()

        observeViewModel()

        chatViewModel.getMessages(senderRoom) {
            if (FirebaseAuth.getInstance().currentUser == null)
                return@getMessages

            Toast.makeText(
                this@ChatActivity,
                "${javaClass.simpleName}: ${(it as? Exception)?.message}",
                Toast.LENGTH_LONG
            ).show()
        }

        binding.typeMessage.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                binding.imgSend.visibility = View.VISIBLE
            }

            override fun afterTextChanged(s: Editable?) {
                if (s!!.trim().isEmpty())
                    binding.imgSend.visibility = View.GONE
            }
        })

        binding.imgSend.setOnClickListener {
            sendMessage()
        }
    }

    private fun initRecyclerView() {
        binding.rvSingleChat.apply {
            layoutManager = LinearLayoutManager(this@ChatActivity)
            setHasFixedSize(true)
            chatAdapter = ChatAdapter(this@ChatActivity, messagesList)
            adapter = chatAdapter
        }
    }

    private fun setupToolBarItems() {
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.chatProfileName.text = receiverUserName

        binding.backViewGroup.setOnClickListener {
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
            .load(Uri.parse(userImageUri))
            .placeholder(R.drawable.avatar)
            .into(binding.chatProfileImage)
    }

    private fun observeViewModel() {
        chatViewModel.chatMessages.observe(this) { messages ->
            messagesList.clear()
            messagesList.addAll(messages)

            runOnUiThread {
                chatAdapter.updateChat(messagesList)
                binding.rvSingleChat.scrollToPosition(chatAdapter.itemCount - 1) // scroll to new message automatically
            }
        }
    }

    private fun sendMessage() {
        val editText = binding.typeMessage
        val chatMessage = ChatMessage(messageBody = editText.text.toString(), senderId = senderUid, receiverId = receiverUid)

        lifecycleScope.launch(Dispatchers.IO) {
            try {
                chatViewModel.sendMessage(senderRoom, receiverRoom, receiverUid!!, chatMessage)
                withContext(Dispatchers.Main) {
                    editText.text = null
                }
            } catch (exception: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@ChatActivity, exception.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }
}