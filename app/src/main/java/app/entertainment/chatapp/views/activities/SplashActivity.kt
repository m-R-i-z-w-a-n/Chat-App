package app.entertainment.chatapp.views.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import app.entertainment.chatapp.databinding.ActivitySplashBinding
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {
    private val binding by lazy { ActivitySplashBinding.inflate(layoutInflater) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        CoroutineScope(Dispatchers.Main).launch {
            delay(SPLASH_DISPLAY_LENGTH)

            binding.title.performClick()

            if (FirebaseAuth.getInstance().currentUser != null) {
                Intent(this@SplashActivity, MainActivity::class.java).also {
                    startActivity(it)
                }
                finish()
            } else {
                Intent(this@SplashActivity, LoginActivity::class.java).also {
                    startActivity(it)
                }
                finish()
            }
        }
    }

    companion object {
        private const val SPLASH_DISPLAY_LENGTH = 1000L
    }
}