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
import androidx.lifecycle.lifecycleScope
import app.entertainment.chatapp.R
import app.entertainment.chatapp.databinding.ActivityProfileInfoBinding
import app.entertainment.chatapp.utils.ImageUrlCallbackResponse
import app.entertainment.chatapp.viewmodel.CurrentUserViewModel
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ProfileInfoActivity : AppCompatActivity() {
    private val binding by lazy { ActivityProfileInfoBinding.inflate(layoutInflater) }

    private val currentUserViewModel by viewModels<CurrentUserViewModel>()

    private var imageUri: Uri? = null
    private var wasPictureChanged: Boolean = false

    private val progressDialog by lazy {
        ProgressDialog(this@ProfileInfoActivity).also {
            it.setMessage("Updating user info...")
            it.setCancelable(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        setContentView(binding.root)

        currentUserViewModel.also { it.getCurrentUser() }.user.observe(this) {
            runOnUiThread(progressDialog::cancel)

            binding.name.setText(it.name)

            Glide.with(this@ProfileInfoActivity)
                .load(Uri.parse(it.imageUri ?: return@observe))
                .placeholder(R.drawable.avatar)
                .into(binding.imgProfile)
        }

        binding.imgProfile.setOnClickListener {
            chooseImageActivityLauncher.launch(
                Intent().also {
                    it.type = "image/*"
                    it.action = Intent.ACTION_GET_CONTENT
                }
            )
        }

        binding.btnUpdateProfile.setOnClickListener {
            val userName = binding.name
            if (userName.text.isEmpty()) {
                binding.name.error = "You must provide a name!"
                return@setOnClickListener
            }

            progressDialog.show()

            val user = mutableMapOf<String, String?>("name" to userName.text.toString().trim())

            imageUri?.let {
                if (wasPictureChanged)
                    user["imageUri"] = it.toString()
            }

            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    currentUserViewModel.updateUserInfo(user)
                } catch (exception: Exception) {
                    withContext(Dispatchers.Main) {
                        progressDialog.cancel()
                        Toast.makeText(this@ProfileInfoActivity, exception.message, Toast.LENGTH_LONG).show()
                    }
                }
            }

            wasPictureChanged = false
        }

        binding.btnSignOut.setOnClickListener {
            FirebaseAuth.getInstance().signOut()
            Intent(this@ProfileInfoActivity, LoginActivity::class.java).also {
                startActivity(it)
            }
            finishAffinity()
            return@setOnClickListener
        }
    }

    private val chooseImageActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result: ActivityResult ->
        if (result.resultCode == RESULT_OK) {
            val intent = result.data
            imageUri = intent?.data
            binding.imgProfile.setImageURI(imageUri)
            wasPictureChanged = true
        }
    }
}