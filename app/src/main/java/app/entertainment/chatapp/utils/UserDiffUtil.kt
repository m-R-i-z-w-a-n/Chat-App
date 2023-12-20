package app.entertainment.chatapp.utils

import androidx.recyclerview.widget.DiffUtil
import app.entertainment.chatapp.models.User

class UserDiffUtil(
    private val oldUserList: List<User>,
    private val newUserList: List<User>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldUserList.size

    override fun getNewListSize(): Int = newUserList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldUserList[oldItemPosition].javaClass == newUserList[newItemPosition].javaClass
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldUserList[oldItemPosition].hashCode() == newUserList[newItemPosition].hashCode()
    }
}