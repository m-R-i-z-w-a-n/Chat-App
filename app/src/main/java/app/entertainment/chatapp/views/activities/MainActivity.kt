package app.entertainment.chatapp.views.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import app.entertainment.chatapp.R
import app.entertainment.chatapp.databinding.ActivityMainBinding
import app.entertainment.chatapp.viewmodel.CurrentUserViewModel
import app.entertainment.chatapp.views.fragments.GroupsFragment
import app.entertainment.chatapp.views.fragments.UsersFragment
import com.bumptech.glide.Glide
import de.hdodenhof.circleimageview.CircleImageView


class MainActivity : AppCompatActivity() {
    private val binding by lazy { ActivityMainBinding.inflate(layoutInflater) }

    private val toolbar by lazy { binding.toolbar }
    private val toolbarView by lazy { toolbar.rootView }

    private val currentUserViewModel by viewModels<CurrentUserViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.statusBarColor = ContextCompat.getColor(this, R.color.white)
        setContentView(binding.root)

        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayShowTitleEnabled(false)


        val profileImageView = toolbarView.findViewById<CircleImageView>(R.id.img_profile_toolbar)

        currentUserViewModel.also { it.getCurrentUser() }.imageUri.observe(this) {
            Glide.with(this)
                .load(Uri.parse(it))
                .placeholder(R.drawable.avatar)
                .into(profileImageView)
        }

        profileImageView.setOnClickListener {
            Intent(this@MainActivity, ProfileInfoActivity::class.java).also {
                startActivity(it)
            }
        }


        val fragment = supportFragmentManager.findFragmentById(R.id.fragment_container)
        if (fragment == null)
        // Add the fragment to the activity
            supportFragmentManager.beginTransaction().apply {
                add(R.id.fragment_container, UsersFragment())
                commit()
            }

        binding.people.setOnClickListener {
            replaceFragment(UsersFragment())

            it.backgroundTintList =
                ContextCompat.getColorStateList(this@MainActivity, R.color.border)
            binding.txtPeople.setTextColor(resources.getColor(R.color.white, resources.newTheme()))

            binding.groups.apply {
                setBackgroundResource(R.drawable.right_border)
                backgroundTintList =
                    ContextCompat.getColorStateList(this@MainActivity, R.color.white)
            }
            binding.txtGroups.setTextColor(resources.getColor(R.color.black, resources.newTheme()))
        }

        binding.groups.setOnClickListener {
            replaceFragment(GroupsFragment())

            it.backgroundTintList =
                ContextCompat.getColorStateList(this@MainActivity, R.color.border)
            binding.txtGroups.setTextColor(resources.getColor(R.color.white, resources.newTheme()))

            binding.people.apply {
                setBackgroundResource(R.drawable.left_border)
                backgroundTintList =
                    ContextCompat.getColorStateList(this@MainActivity, R.color.white)
            }
            binding.txtPeople.setTextColor(resources.getColor(R.color.black, resources.newTheme()))
        }
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, fragment)
            addToBackStack(null)
            commit()
        }
    }
}