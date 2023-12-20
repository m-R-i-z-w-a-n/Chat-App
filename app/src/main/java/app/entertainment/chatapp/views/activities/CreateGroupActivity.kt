package app.entertainment.chatapp.views.activities

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import app.entertainment.chatapp.R
import app.entertainment.chatapp.databinding.ActivityCreateGroupBinding
import app.entertainment.chatapp.models.User
import app.entertainment.chatapp.viewmodel.CurrentUserViewModel
import app.entertainment.chatapp.viewmodel.GroupsViewModel

class CreateGroupActivity : AppCompatActivity() {
    private val binding by lazy { ActivityCreateGroupBinding.inflate(layoutInflater) }

    private var imageUri: Uri? = null
    private var wasPictureChanged = false

    private val groupsViewModel: GroupsViewModel by viewModels()
    private val currentUserViewModel: CurrentUserViewModel by viewModels()
    private lateinit var currentUser: User

    private val progressDialog by lazy {
        ProgressDialog(this@CreateGroupActivity).also {
            it.setMessage("Creating a new group...")
            it.setCancelable(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        setContentView(binding.root)

        binding.groupProfileImg.setOnClickListener {
            chooseImageActivityLauncher.launch(
                Intent().also {
                    it.type = "image/*"
                    it.action = Intent.ACTION_GET_CONTENT
                }
            )
        }

        currentUserViewModel.also { it.getCurrentUser() }.user.observe(this) {
            currentUser = it
        }

        binding.createGroup.setOnClickListener {
            val name = binding.groupName
            if (name.text.trim().isEmpty()) {
                Toast.makeText(
                    this@CreateGroupActivity,
                    "Group must have a name!",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }

            progressDialog.show()

            try {
                groupsViewModel.createGroup(name.text.toString().trim(), imageUri.toString())
                groupsViewModel.addUserToGroup(name.text.toString().trim(), currentUser)

                Toast.makeText(
                    this@CreateGroupActivity,
                    "Created a ${name.text.toString().trim()} group",
                    Toast.LENGTH_SHORT
                ).show()
                super.onBackPressed()
            } catch (ex: Exception) {
                // Handle the exception on the main thread
                Toast.makeText(this@CreateGroupActivity, ex.message, Toast.LENGTH_LONG).show()

            } finally {
                // Cancel the progress dialog on the main thread
                progressDialog.cancel()
            }
        }

        binding.cancel.setOnClickListener {
            super.onBackPressed()
            finish()
        }
    }

    private val chooseImageActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val intent = result.data
            imageUri = intent?.data
            binding.groupProfileImg.setImageURI(imageUri)
            wasPictureChanged = true
        }
    }
}