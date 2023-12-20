package app.entertainment.chatapp.views.fragments

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import app.entertainment.chatapp.R
import app.entertainment.chatapp.adapters.GroupsAdapter
import app.entertainment.chatapp.databinding.FragmentGroupsBinding
import app.entertainment.chatapp.viewmodel.GroupsViewModel
import app.entertainment.chatapp.views.activities.CreateGroupActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase

/**
 * A simple [Fragment] subclass.
 * Use the [GroupsFragment#newInstance] factory method to
 * create an instance of this fragment.
 */
class GroupsFragment : Fragment(R.layout.fragment_groups) {

    private var _binding: FragmentGroupsBinding? = null

    private val binding get() = _binding!!

    private lateinit var chatAdapter: GroupsAdapter

    private val groupsViewModel: GroupsViewModel by viewModels()

    private val senderUid by lazy { Firebase.auth.currentUser?.uid }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        _binding = FragmentGroupsBinding.bind(view)

        val fab = activity?.findViewById(R.id.floating_button) as FloatingActionButton

        fab.setImageResource(R.drawable.baseline_add_24)

        fab.setOnClickListener {
            Intent(requireContext(), CreateGroupActivity::class.java).also {
                requireContext().startActivity(it)
            }
        }

        initRecyclerView()
    }

    private fun initRecyclerView() {
        val groupsAdapter = GroupsAdapter(requireContext(), emptyList())
        binding.rvGroups.apply {
            layoutManager = LinearLayoutManager(requireContext())
            setHasFixedSize(true)
            adapter = groupsAdapter
        }

        groupsViewModel.groups.observe(requireActivity()) {
            groupsAdapter.updateGroups(it)
        }
    }

    override fun onResume() {
        super.onResume()


    }

    override fun onDestroy() {
        _binding = null
        super.onDestroy()
    }
}