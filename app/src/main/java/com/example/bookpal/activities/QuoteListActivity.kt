package com.example.bookpal.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.annotation.Nullable
import com.example.bookpal.adapters.AdapterQuote
import com.example.bookpal.databinding.ActivityQuoteListBinding
import com.example.bookpal.models.ModelBook
import com.example.bookpal.models.ModelQuote
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class QuoteListActivity : AppCompatActivity() {

    private lateinit var binding: ActivityQuoteListBinding


    private lateinit var firebaseAuth: FirebaseAuth

    private companion object {
        private const val TAG = "QUOTE_LIST_FAV_TAG"
    }

    private lateinit var quoteArrayList: ArrayList<ModelQuote>

    private lateinit var adapterQuote: AdapterQuote

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuoteListBinding.inflate(layoutInflater)
        setContentView(binding.root)

        firebaseAuth = FirebaseAuth.getInstance()

        loadQuotes()

        binding.searchEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {

            }

            override fun onTextChanged(s: CharSequence, p1: Int, p2: Int, p3: Int) {
                try {
                    adapterQuote.filter.filter(s)
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

    private fun loadQuotes() {
        quoteArrayList = ArrayList()

        adapterQuote = AdapterQuote(this, quoteArrayList)
        binding.quotesRv.adapter = adapterQuote

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child("${firebaseAuth.uid}")
            .child("FavoriteQuotes")
            .addChildEventListener(object : ChildEventListener {
                override fun onChildAdded(dataSnapshot: DataSnapshot, @Nullable previousChildName: String?) {

                    val quoteId = "${dataSnapshot.child("quoteId").value}"
                    Log.d(TAG, "onChildAdded: quoteId: $quoteId")

                    if (quoteId != "null"){
                        val refQuote = FirebaseDatabase.getInstance().getReference("Quotes")
                        refQuote.child(quoteId)
                            .addListenerForSingleValueEvent(object: ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {
                                    val modelPost: ModelQuote? = snapshot.getValue(ModelQuote::class.java)
                                    modelPost?.let { quoteArrayList.add(it) }
                                    adapterQuote.notifyItemInserted(quoteArrayList.size - 1)
                                }

                                override fun onCancelled(error: DatabaseError) {

                                }
                            })
                    }

                }

                override fun onChildChanged(dataSnapshot: DataSnapshot, @Nullable previousChildName: String?) {
                    Log.i(TAG, "child changed")

                    val modelPost: ModelQuote? = dataSnapshot.getValue(ModelQuote::class.java)
                    for (i in 0 until quoteArrayList.size) {
                        if (quoteArrayList[i].id == modelPost?.id) {
                            quoteArrayList[i] = modelPost
                            Log.i(TAG, "changed child found at position: $i")
                            adapterQuote.notifyItemChanged(i)
                            break
                        }
                    }
                }

                override fun onChildRemoved(dataSnapshot: DataSnapshot) {
                    Log.i(TAG, "child removed")
                    val quoteId = "${dataSnapshot.child("quoteId").value}"

                    val modelPost = ModelQuote()
                    modelPost.id = quoteId

                    for (i in 0 until quoteArrayList.size) {
                        if (quoteArrayList[i].id == modelPost.id) {
                            quoteArrayList.removeAt(i)
                            Log.i(TAG, "removed child found at position: $i bookId: $quoteId")
                            adapterQuote.notifyItemRemoved(i)
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