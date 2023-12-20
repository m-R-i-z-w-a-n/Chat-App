package app.entertainment.chatapp.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import app.entertainment.chatapp.models.Group
import app.entertainment.chatapp.models.User
import app.entertainment.chatapp.repository.GroupsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class GroupsViewModel(private val groupsRepository: GroupsRepository = GroupsRepository()) :
    ViewModel() {
    init {
        getGroups()
    }

    private val mGroups = MutableLiveData<List<Group>>()

    val groups: LiveData<List<Group>>
        get() = mGroups

    private fun getGroups() {
        groupsRepository.getGroups {
            mGroups.postValue(it)
        }
    }

    fun createGroup(groupName: String, profileImageUri: String? = null) =
        viewModelScope.launch(Dispatchers.IO) {
            try {
                groupsRepository.createGroup(groupName, profileImageUri)
            } catch (ex: Exception) {
                throw ex
            }
        }

    suspend fun isUserInGroup(group: Group, user: User): Boolean {
        val snapshot = groupsRepository.getGroupUsers(group)

        for (child in snapshot.children) {
            if (child.key == user.uid)
                return true
        }
        return false
    }

    fun addUserToGroup(groupName: String, user: User) = viewModelScope.launch(Dispatchers.IO) {
        try {
            groupsRepository.addUserToGroup(groupName, user)
        } catch (ex: Exception) {
            throw ex
        }
    }
}