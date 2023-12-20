package app.entertainment.chatapp.viewmodel

import android.app.Activity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.entertainment.chatapp.utils.LoginCallback
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.PhoneAuthCredential
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit

class LoginViewModel : ViewModel() {
    private val firebaseAuth = FirebaseAuth.getInstance()
    private val mUser = MutableLiveData<FirebaseUser>()

    val user: LiveData<FirebaseUser>
        get() = mUser

    fun sendVerificationCode(
        phoneNumber: String,
        activity: Activity,
        callback: PhoneAuthProvider.OnVerificationStateChangedCallbacks
    ) {
//        firebaseAuth.firebaseAuthSettings.setAppVerificationDisabledForTesting(true)
        PhoneAuthProvider.verifyPhoneNumber(
            PhoneAuthOptions.newBuilder(firebaseAuth).apply {
                setActivity(activity)
                setPhoneNumber(phoneNumber)
                setTimeout(60L, TimeUnit.SECONDS)
                setCallbacks(callback)
            }.build()
        )
    }

    fun verifyCode(verificationId: String, code: String) = viewModelScope.launch(Dispatchers.IO) {
        val credential = PhoneAuthProvider.getCredential(verificationId, code)
        try {
            signInWithPhoneAuthCredential(credential)
        } catch (exception: Exception) {
            throw exception
        }
    }

    private suspend fun signInWithPhoneAuthCredential(
        credential: PhoneAuthCredential,
    ) {
        try {
            val result = firebaseAuth.signInWithCredential(credential).await()
            mUser.postValue(result.user)
        } catch (exception: Exception) {
            throw exception
        }
    }
}