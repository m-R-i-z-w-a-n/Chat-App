package app.entertainment.chatapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.entertainment.chatapp.models.User
import app.entertainment.chatapp.repository.CurrentUserRepository
import app.entertainment.chatapp.utils.ImageUrlCallbackResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class CurrentUserViewModel(private val repository: CurrentUserRepository = CurrentUserRepository()) :
    ViewModel() {
    private val mUser = MutableLiveData<User>()
    private val mImageUri = MutableLiveData<String>()

    val user: LiveData<User>
        get() = mUser

    val imageUri: LiveData<String>
        get() = mImageUri

    fun getCurrentUser() {
        repository.getCurrentUser { currentUser ->
            mUser.postValue(currentUser)
            mImageUri.postValue(currentUser.imageUri ?: return@getCurrentUser)
        }
    }

    fun updateUserInfo(newValue: MutableMap<String, String?>) =
        viewModelScope.launch(Dispatchers.IO) {
            try {
                repository.updateUserInfo(newValue)
            } catch (exception: Exception) {
                throw exception
            }
        }
}