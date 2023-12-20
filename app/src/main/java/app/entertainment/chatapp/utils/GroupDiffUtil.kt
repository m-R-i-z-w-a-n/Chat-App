package app.entertainment.chatapp.utils

import androidx.recyclerview.widget.DiffUtil
import app.entertainment.chatapp.models.Group
import app.entertainment.chatapp.models.User

class GroupDiffUtil(
    private val oldGroupList: List<Group>,
    private val newGroupList: List<Group>
) : DiffUtil.Callback() {
    override fun getOldListSize(): Int = oldGroupList.size

    override fun getNewListSize(): Int = newGroupList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldGroupList[oldItemPosition].javaClass == newGroupList[newItemPosition].javaClass
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldGroupList[oldItemPosition].hashCode() == newGroupList[newItemPosition].hashCode()
    }
}