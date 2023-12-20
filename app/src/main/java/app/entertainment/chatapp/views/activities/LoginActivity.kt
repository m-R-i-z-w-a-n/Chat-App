package app.entertainment.chatapp.views.activities

import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import app.entertainment.chatapp.R
import app.entertainment.chatapp.databinding.ActivityLoginBinding
import app.entertainment.chatapp.utils.LoginCallback
import app.entertainment.chatapp.utils.USER_NODE
import app.entertainment.chatapp.viewmodel.LoginViewModel
import com.google.firebase.FirebaseException
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthProvider
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val viewModel by viewModels<LoginViewModel>()

    private val dbRef by lazy { FirebaseDatabase.getInstance().reference }

    private val progressDialog by lazy {
        ProgressDialog(this@LoginActivity).also {
            it.setMessage("Waiting for OTP...")
            it.setCancelable(false)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.statusBarColor = ContextCompat.getColor(this, R.color.white)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0)
        binding.phoneNumber.requestFocus()

        viewModel.user.observe(this) { user ->
            if (user != null) {
                dbRef.child(USER_NODE).addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        if (snapshot.hasChild(user.uid)) {
                            // Go to main activity if user already has data
                            Intent(this@LoginActivity, MainActivity::class.java).also { intent ->
                                startActivity(intent)
                            }
                            finishAffinity()
                            return
                        } else {
                            Intent(this@LoginActivity, ProfileSetupActivity::class.java).also {
                                startActivity(it)
                            }
                            finishAffinity()
                            return
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Toast.makeText(this@LoginActivity, error.message, Toast.LENGTH_LONG).show()
                    }
                })
                return@observe
            }
        }

        binding.sendCode.setOnClickListener sendCode@{
            val phoneNumber = binding.phoneNumber.text.toString()

            if (phoneNumber.isEmpty()) {
                binding.phoneNumber.error = "Enter a phone number first!"
                return@sendCode
            }

            progressDialog.show()

            viewModel.sendVerificationCode(
                phoneNumber,
                this,
                object : PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
                    override fun onVerificationCompleted(credential: PhoneAuthCredential) {}

                    override fun onVerificationFailed(exception: FirebaseException) {
                        exception.printStackTrace()
                        Toast.makeText(this@LoginActivity, "Login Activity: ${exception.message}", Toast.LENGTH_LONG)
                            .show()
                        progressDialog.dismiss()
                    }

                    override fun onCodeSent(
                        verificationId: String,
                        token: PhoneAuthProvider.ForceResendingToken
                    ) {
                        binding.sendCode.visibility = View.GONE
                        binding.otpView.apply {
                            visibility = View.VISIBLE
                            requestFocus()
                        }

                        progressDialog.dismiss()

                        binding.otpView.setOtpCompletionListener { code ->
                            progressDialog.setMessage("Verifying OTP...")
                            progressDialog.show()
                            CoroutineScope(Dispatchers.IO).launch {
                                try {
                                    viewModel.verifyCode(verificationId, code)
                                } catch (exception : Exception) {
                                    withContext(Dispatchers.Main) {
                                        progressDialog.cancel()
                                        Toast.makeText(this@LoginActivity, exception.message, Toast.LENGTH_LONG).show()
                                    }
                                }
                            }
                        }
                    }
                })
        }
    }
}