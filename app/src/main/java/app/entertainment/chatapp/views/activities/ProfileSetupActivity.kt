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
import app.entertainment.chatapp.databinding.ActivityProfileSetupBinding
import app.entertainment.chatapp.models.User
import app.entertainment.chatapp.utils.getImageDownloadUrl
import app.entertainment.chatapp.viewmodel.UsersViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ProfileSetupActivity : AppCompatActivity() {
    private val binding by lazy { ActivityProfileSetupBinding.inflate(layoutInflater) }

    private val dbRef by lazy { FirebaseDatabase.getInstance().reference }
    private val storageRef by lazy { FirebaseStorage.getInstance().reference }

    private val currentUser by lazy { FirebaseAuth.getInstance().currentUser }

    private val imageReference by lazy { currentUser?.uid?.let { storageRef.child("profiles/$it") } }

    private var imageUri: Uri? = null

    private var wasPictureSelected: Boolean = false

    private val usersViewModel by viewModels<UsersViewModel>()

    private val progressDialog by lazy {
        ProgressDialog(this@ProfileSetupActivity).also {
            it.setMessage("Setting up user...")
            it.setCancelable(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)

        setContentView(binding.root)

        supportActionBar?.hide()

        binding.profileImage.setOnClickListener {
            chooseImageActivityLauncher.launch(
                Intent().also {
                    it.type = "image/*"
                    it.action = Intent.ACTION_GET_CONTENT
                }
            )
        }

        binding.btnSetupProfile.setOnClickListener {
            if (binding.name.text.isEmpty()) {
                binding.name.error = "Enter a name to continue!"
                return@setOnClickListener
            }

            progressDialog.show()

            CoroutineScope(Dispatchers.IO).launch {
                if (wasPictureSelected) {
                    imageReference?.let { imageReference ->
                        addUserInfo(
                            getImageDownloadUrl(imageUri.toString(), imageReference)
                        )
                    }
                } else
                    addUserInfo()
            }
        }
    }

    private suspend fun addNewUser(user: User) {
        try {
            usersViewModel.addUserToDatabase(user)
            Intent(this, MainActivity::class.java).also { intent ->
                startActivity(intent)
            }
            finishAffinity()
        } catch (exception: Exception) {
            withContext(Dispatchers.Main) {
                progressDialog.cancel()
                Toast.makeText(this@ProfileSetupActivity, exception.message, Toast.LENGTH_LONG).show()
            }
        } finally {
            imageUri = null
        }
    }

    private suspend fun addUserInfo(url: String? = null) {
        currentUser?.let {
            addNewUser(
                User(
                    it.phoneNumber,
                    binding.name.text.toString(),
                    url,
                    "Hi, I am ${binding.name.text}",
                    it.uid
                )
            )
        }
    }

    private val chooseImageActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val intent = result.data
            imageUri = intent?.data
            binding.profileImage.setImageURI(imageUri)
            wasPictureSelected = true
        }
    }
}