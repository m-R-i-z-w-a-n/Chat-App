package app.entertainment.chatapp.views.fragments

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import app.entertainment.chatapp.R
import app.entertainment.chatapp.adapters.UsersAdapter
import app.entertainment.chatapp.databinding.FragmentUsersBinding
import app.entertainment.chatapp.models.User
import app.entertainment.chatapp.viewmodel.UsersViewModel
import app.entertainment.chatapp.views.activities.ShowAllUsersActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * A simple [Fragment] subclass.
 * Use the [UsersFragment#newInstance] factory method to
 * create an instance of this fragment.
 */
class UsersFragment : Fragment(R.layout.fragment_users) {

    private var _binding: FragmentUsersBinding? = null

    private val binding get() = _binding!!

    private lateinit var userAdapter: UsersAdapter

    private val usersViewModel by viewModels<UsersViewModel>()

    private val senderUid by lazy { FirebaseAuth.getInstance().uid }
    private lateinit var receiverUid: String

    private val dialog by lazy {
        ProgressDialog(requireContext()).also {
            it.setMessage("Loading users...")
            it.setCancelable(false)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentUsersBinding.bind(view)

        dialog.show()

        initRecyclerView()

        val fab = activity?.findViewById(R.id.floating_button) as FloatingActionButton
        fab.setImageResource(R.drawable.baseline_sms_24)
        fab.setOnClickListener {
            Intent(requireContext(), ShowAllUsersActivity::class.java).also {
                it.putExtra("source", javaClass.simpleName)
                requireContext().startActivity(it)
            }
        }
    }

    private fun initRecyclerView() {
        val users = mutableListOf<User>()

        binding.rvUsers.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            userAdapter = UsersAdapter(requireContext(), users)
            adapter = userAdapter
        }

        usersViewModel.users.observe(requireActivity()) { userList ->
            lifecycleScope.launch(Dispatchers.IO) {
                val filteredList = filterUsers(userList)
                withContext(Dispatchers.Main) {
                    dialog.cancel()
                    userAdapter.updateUsers(filteredList)
                }
            }
        }
    }

    private suspend fun filterUsers(userList: List<User>): MutableList<User> {
        val mutableUserList = userList.toMutableList()

        val iterator = mutableUserList.iterator()
        while (iterator.hasNext()) {
            val user = iterator.next()
            receiverUid = user.uid!!

            try {
                if (!usersViewModel.isChatInitiated(senderUid!!, receiverUid))
                    iterator.remove()
            } catch (ex: Exception) {
                Toast.makeText(requireContext(), ex.message, Toast.LENGTH_SHORT).show()
            }
        }

        return mutableUserList
    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}