package com.example.bookpal.activities

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.example.bookpal.Constants
import com.example.bookpal.databinding.ActivityDashboardBinding
import com.google.firebase.auth.FirebaseAuth

class DashboardActivity : AppCompatActivity() {


    private lateinit var binding: ActivityDashboardBinding


    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)


        firebaseAuth = FirebaseAuth.getInstance()
        checkUser()


        binding.logoutBtn.setOnClickListener {
            firebaseAuth.signOut()
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }


        binding.profileBtn.setOnClickListener {
            startActivity(Intent(this, ProfileEditActivity::class.java))
        }


        binding.myBooksBtn.setOnClickListener {
            startActivity(Intent(this, BookListMyActivity::class.java))
        }


        binding.favoritesBtn.setOnClickListener {
            startActivity(Intent(this, QuoteListActivity::class.java))
        }


        binding.alreadyReadBtn.setOnClickListener {
            val intent = Intent(this, BookListLibraryActivity::class.java)
            intent.putExtra("library", Constants.BOOK_LIBRARY_ALREADY_READ)
            startActivity(intent)
        }


        binding.currentlyReadingBtn.setOnClickListener {
            val intent = Intent(this, BookListLibraryActivity::class.java)
            intent.putExtra("library", Constants.BOOK_LIBRARY_CURRENTLY_READING)
            startActivity(intent)
        }


        binding.wantToReadBtn.setOnClickListener {
            val intent = Intent(this, BookListLibraryActivity::class.java)
            intent.putExtra("library", Constants.BOOK_LIBRARY_WANT_TO_READ)
            startActivity(intent)
        }

    }


    private fun checkUser() {

        val firebaseUser = firebaseAuth.currentUser
        if (firebaseUser == null){

            binding.subTitleTv.text = "Not Logged In"

            binding.profileBtn.visibility = View.GONE
            binding.logoutBtn.visibility = View.GONE
        }
        else{

            val email = firebaseUser.email

            binding.subTitleTv.text = email


            binding.profileBtn.visibility = View.VISIBLE
            binding.logoutBtn.visibility = View.VISIBLE
        }
    }
}