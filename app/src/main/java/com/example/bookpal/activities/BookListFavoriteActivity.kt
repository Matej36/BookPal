package com.example.bookpal.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.annotation.Nullable
import androidx.appcompat.app.AppCompatActivity
import com.example.bookpal.adapters.AdapterBook
import com.example.bookpal.databinding.ActivityBookListFavoriteBinding
import com.example.bookpal.models.ModelBook
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class BookListFavoriteActivity : AppCompatActivity() {


    private lateinit var binding: ActivityBookListFavoriteBinding


    private lateinit var firebaseAuth: FirebaseAuth

    private companion object {
        private const val TAG = "BOOK_LIST_FAV_TAG"
    }

    private lateinit var bookArrayList: ArrayList<ModelBook>

    private lateinit var adapterBook: AdapterBook

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookListFavoriteBinding.inflate(layoutInflater)
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

    }

    private fun loadBooks() {
        bookArrayList = ArrayList()

        adapterBook = AdapterBook(this, bookArrayList)
        binding.booksRv.adapter = adapterBook

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child("${firebaseAuth.uid}")
            .child("Favorites")
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, @Nullable previousChildName: String?) {

                    val bookId = "${dataSnapshot.child("bookId").value}"
                    Log.d(TAG, "onChildAdded: bookId: $bookId")

                    if (bookId != "null"){
                        val ref = FirebaseDatabase.getInstance().getReference("Books")
                        ref.child("$bookId")
                            .addListenerForSingleValueEvent(object: ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val modelPost: ModelBook? = snapshot.getValue(ModelBook::class.java)
                                    modelPost?.let { bookArrayList.add(it) }
                                    adapterBook.notifyItemInserted(bookArrayList.size - 1)
                                }

                                override fun onCancelled(error: DatabaseError) {

                                }
                            })
                    }

                }

                override fun onChildChanged(dataSnapshot: DataSnapshot, @Nullable previousChildName: String?) {
                    Log.i(TAG, "child changed")

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
                    Log.i(TAG, "child removed")
                    val bookId = "${dataSnapshot.child("bookId").value}"

                    val modelPost = ModelBook()
                    modelPost.id = bookId

                    for (i in 0 until bookArrayList.size) {
                        if (bookArrayList[i].id == modelPost.id) {
                            bookArrayList.removeAt(i)
                            Log.i(TAG, "removed child found at position: $i bookId: $bookId")
                            adapterBook.notifyItemRemoved(i)
                            break
                        }
                    }
                }

                override fun onChildMoved(snapshot: DataSnapshot, @Nullable previousChildName: String?) {

                }

                override fun onCancelled(error: DatabaseError) {}
            })
    }

}