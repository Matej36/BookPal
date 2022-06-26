package com.example.bookpal.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import com.example.bookpal.adapters.AdapterBook
import com.example.bookpal.databinding.ActivityBookListMyBinding
import com.example.bookpal.models.ModelBook
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase

class BookListMyActivity : AppCompatActivity() {


    private lateinit var binding: ActivityBookListMyBinding


    private lateinit var firebaseAuth: FirebaseAuth

    private companion object {
        private const val TAG = "BOOK_LIST_MY_TAG"
    }

    private lateinit var bookArrayList: ArrayList<ModelBook>

    private lateinit var adapterBook: AdapterBook

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookListMyBinding.inflate(layoutInflater)
        setContentView(binding.root)


        firebaseAuth = FirebaseAuth.getInstance()

        loadBooks()


        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence, p1: Int, p2: Int, p3: Int) {

                try {
                    adapterBook.filter.filter(s)
                } catch (e: Exception) {
                    Log.d(TAG, "onTextChanged: ${e.message}")
                }
            }

            override fun afterTextChanged(p0: Editable?) {

            }
        })


        binding.backBtn.setOnClickListener {
            onBackPressed()
        }

        binding.addBookBtn.setOnClickListener {
            startActivity(Intent(this, BookAddActivity::class.java))
        }

    }

    private fun loadBooks() {
        bookArrayList = ArrayList()

        adapterBook = AdapterBook(this, bookArrayList)
        binding.booksRv.adapter = adapterBook

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.orderByChild("uid").equalTo("${firebaseAuth.uid}")
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, @Nullable previousChildName: String?) {
                    Log.d(TAG, "onChildAdded: ")
                    val modelPost: ModelBook? = dataSnapshot.getValue(ModelBook::class.java)
                    modelPost?.let { bookArrayList.add(it) }
                    adapterBook.notifyItemInserted(bookArrayList.size - 1)
                }

                override fun onChildChanged(dataSnapshot: DataSnapshot, @Nullable previousChildName: String?) {
                    Log.d(TAG, "onChildChanged: ")
                    val modelPost: ModelBook? = dataSnapshot.getValue(ModelBook::class.java)
                    for (i in 0 until bookArrayList.size) {
                        if (bookArrayList[i].id == modelPost?.id) {
                            bookArrayList[i] = modelPost
                            Log.i(TAG, "changed child found at position: $i")
                            adapterBook.notifyItemChanged(i)
                            break
                        }
                    }
                }

                override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                    Log.d(TAG, "onChildRemoved: ")
                    val modelPost: ModelBook? = dataSnapshot.getValue(ModelBook::class.java)
                    for (i in 0 until bookArrayList.size) {
                        if (bookArrayList[i].id == modelPost?.id) {
                            bookArrayList.removeAt(i)
                            Log.i(TAG, "removed child found at position: $i")
                            adapterBook.notifyItemRemoved(i)
                            break
                        }
                    }
                }

                override fun onChildMoved(snapshot: DataSnapshot, @Nullable previousChildName: String?) {
                    Log.d(TAG, "onChildMoved: ")
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.d(TAG, "onCancelled: ")
                }
            })
    }

}