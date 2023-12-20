package app.entertainment.chatapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.entertainment.chatapp.models.User
import app.entertainment.chatapp.repository.UsersRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class UsersViewModel(private val usersRepository: UsersRepository = UsersRepository()) :
    ViewModel() {
    init {
        getUsers()
    }

    private val mUsers = MutableLiveData<List<User>>()

    val users: LiveData<List<User>>
        get() = mUsers

    private fun getUsers() {
        usersRepository.getUsers { users ->
            mUsers.postValue(users)
        }
    }

    fun addUserToDatabase(user: User) = viewModelScope.launch(Dispatchers.IO) {
        try {
            usersRepository.addUserToDatabase(user)
        } catch (ex: Exception) {
            throw ex
        }
    }

    suspend fun isChatInitiated(senderId: String, receiverId: String): Boolean = try {
        usersRepository.isChatInitiated(senderId, receiverId)
    } catch (ex: Exception) {
        throw ex
    }
}