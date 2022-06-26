package com.example.bookpal.activities

import android.app.ProgressDialog
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.bookpal.Constants
import com.example.bookpal.MyApplication
import com.example.bookpal.R
import com.example.bookpal.adapters.AdapterQuote
import com.example.bookpal.databinding.ActivityBookDetailsBinding
import com.example.bookpal.databinding.DialogQuoteAddBinding
import com.example.bookpal.models.ModelQuote
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class BookDetailsActivity : AppCompatActivity() {


    private lateinit var binding: ActivityBookDetailsBinding

    private companion object {
        private const val TAG = "BOOK_DETAILS_TAG"
    }


    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var progressDialog: ProgressDialog


    private lateinit var quoteArrayList: ArrayList<ModelQuote>

    private lateinit var adapterQuote: AdapterQuote

    private var bookId = ""
    private var quote = ""
    private var quotePage = ""

    private var isInMyFavorite = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.toolbarEditBtn.visibility = View.GONE
        binding.toolbarDeleteBtn.visibility = View.GONE

        bookId = intent.getStringExtra("bookId").toString()
        Log.d(TAG, "onCreate: bookId: $bookId")


        firebaseAuth = FirebaseAuth.getInstance()


        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)

        showBookDetails()
        showQuotes()
        checkIsFavorite()
        checkIsLibrary()



        binding.backBtn.setOnClickListener {
            onBackPressed()
        }


        binding.addQuoteBtn.setOnClickListener {
            addQuoteDialog()
        }


        binding.alreadyReadBtn.setOnClickListener {
            addToLibrary(Constants.BOOK_LIBRARY_ALREADY_READ)
        }

        binding.currentlyReadingBtn.setOnClickListener {
            addToLibrary(Constants.BOOK_LIBRARY_CURRENTLY_READING)
        }

        binding.wantToReadBtn.setOnClickListener {
            addToLibrary(Constants.BOOK_LIBRARY_WANT_TO_READ)
        }


        binding.favoriteBtn.setOnClickListener {
            if (isInMyFavorite) {
                removeFromFavorite()
            } else {
                addToFavorite()
            }
        }


        binding.toolbarEditBtn.setOnClickListener {
            val intent = Intent(this, BookEditActivity::class.java)
            intent.putExtra("bookId", bookId)
            startActivity(intent)
        }


        binding.toolbarDeleteBtn.setOnClickListener {
            val builder = AlertDialog.Builder(this)
            builder.setTitle("Delete Book?")
                .setMessage("Are you sure you want to delete this book?")
                .setPositiveButton("DELETE"){d,e->
                    deleteBook()
                }
                .setNegativeButton("CANCLE"){d,e->
                    d.dismiss()
                }
                .show()
        }

    }

    private fun checkIsLibrary(){
        Log.d(TAG, "checkIsLibrary: Checking if book $bookId is in library or not")

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .child("Library")
            .child(bookId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val isInMyLibrary = snapshot.exists()
                    Log.d(TAG, "checkIsLibrary onDataChange: Book $bookId Exists: $isInMyLibrary")
                    if (isInMyLibrary) {
                        val library = "${snapshot.child("library").value}" /*Already Read | Currently Reading | Want To Read*/
                        //available in library
                        Log.d(TAG, "checkIsLibrary onDataChange: available in library $library")

                        if (library.equals(Constants.BOOK_LIBRARY_ALREADY_READ, true)){
                            binding.alreadyReadBtn.setBackgroundColor(ContextCompat.getColor(this@BookDetailsActivity, R.color.teal_200))
                            binding.currentlyReadingBtn.setBackgroundColor(ContextCompat.getColor(this@BookDetailsActivity, R.color.purple_500))
                            binding.wantToReadBtn.setBackgroundColor(ContextCompat.getColor(this@BookDetailsActivity, R.color.purple_500))
                        }
                        else if (library.equals(Constants.BOOK_LIBRARY_CURRENTLY_READING, true)){
                            binding.alreadyReadBtn.setBackgroundColor(ContextCompat.getColor(this@BookDetailsActivity, R.color.purple_500))
                            binding.currentlyReadingBtn.setBackgroundColor(ContextCompat.getColor(this@BookDetailsActivity, R.color.teal_200))
                            binding.wantToReadBtn.setBackgroundColor(ContextCompat.getColor(this@BookDetailsActivity, R.color.purple_500))
                        }
                        else if (library.equals(Constants.BOOK_LIBRARY_WANT_TO_READ, true)){
                            binding.alreadyReadBtn.setBackgroundColor(ContextCompat.getColor(this@BookDetailsActivity, R.color.purple_500))
                            binding.currentlyReadingBtn.setBackgroundColor(ContextCompat.getColor(this@BookDetailsActivity, R.color.purple_500))
                            binding.wantToReadBtn.setBackgroundColor(ContextCompat.getColor(this@BookDetailsActivity, R.color.teal_200))
                        }
                    } else {

                        Log.d(TAG, "checkIsLibrary onDataChange: not available in library")
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun checkIsFavorite() {
        Log.d(TAG, "checkIsFavorite: Checking if book $bookId is in fav or not")

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .child("Favorites")
            .child(bookId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    isInMyFavorite = snapshot.exists()
                    Log.d(TAG, "onDataChange: Book $bookId Exists: $isInMyFavorite")
                    if (isInMyFavorite) {
                        //available in favorite
                        Log.d(TAG, "onDataChange: available in favorite ")
                        // set drawable top icon
                        binding.favoriteBtn.setIconResource(R.drawable.ic_favorite_filled)
                        binding.favoriteBtn.text = "Remove Favorite"
                    } else {
                        //not available in favorite
                        Log.d(TAG, "onDataChange: not available in favorite")
                        //set drawable top icon
                        binding.favoriteBtn.setIconResource(R.drawable.ic_favorite_outline)
                        binding.favoriteBtn.text = "Add Favorite"
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun showBookDetails() {
        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val id = snapshot.child("id").value
                    val name = snapshot.child("name").value
                    val genre = snapshot.child("genre").value
                    val author = snapshot.child("author").value
                    val bookImage = snapshot.child("bookImage").value
                    val uid = snapshot.child("uid").value

                    if ("${firebaseAuth.uid}" == "$uid") {
                        binding.toolbarEditBtn.visibility = View.VISIBLE
                        binding.toolbarDeleteBtn.visibility = View.VISIBLE
                    } else {
                        binding.toolbarEditBtn.visibility = View.GONE
                        binding.toolbarDeleteBtn.visibility = View.GONE
                    }

                    binding.titleTv.text = "$name"
                    binding.genreTv.text = "$genre"
                    binding.authorTv.text = "$author"

                    try {
                        Glide.with(this@BookDetailsActivity)
                            .load(bookImage)
                            .placeholder(R.drawable.ic_twotone_book_genre_black)
                            .into(binding.bookIv)
                    } catch (e: Exception) {
                        Log.e(TAG, "onDataChange: ", e)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun showQuotes() {

        quoteArrayList = ArrayList()


        val ref = FirebaseDatabase.getInstance().getReference("Quotes")
        ref.orderByChild("bookId").equalTo(bookId)
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                    quoteArrayList.clear()
                    for (ds in snapshot.children){

                        val model = ds.getValue(ModelQuote::class.java)

                        if (model?.uid == firebaseAuth.uid){
                            quoteArrayList.add(model!!)
                        }
                    }

                    adapterQuote = AdapterQuote(this@BookDetailsActivity, quoteArrayList)

                    binding.quotesRv.adapter = adapterQuote
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }


    private fun addQuoteDialog() {

        val quoteAddBinding = DialogQuoteAddBinding.inflate(LayoutInflater.from(this))


        val builder = AlertDialog.Builder(this, R.style.CustomDialog)
        builder.setView(quoteAddBinding.root)


        val alertDialog = builder.create()
        alertDialog.show()


        quoteAddBinding.backBtn.setOnClickListener { alertDialog.dismiss() }


        quoteAddBinding.submitBtn.setOnClickListener {

            quote = quoteAddBinding.quoteEt.text.toString().trim()
            quotePage = quoteAddBinding.pageNumberEt.text.toString().trim()

            if (quote.isEmpty()) {
                MyApplication.toast(this, "Enter Quote....")
            } else if (quotePage.isEmpty()) {
                MyApplication.toast(this, "Enter Page Number....")
            } else {
                alertDialog.dismiss()
                addQuote()
            }
        }
    }

    private fun addQuote() {

        progressDialog.setMessage("Adding Quote")
        progressDialog.show()


        val timestamp = System.currentTimeMillis()


        val hashMap = HashMap<String, Any>()
        hashMap["id"] = "$timestamp"
        hashMap["bookId"] = "$bookId"
        hashMap["uid"] = "${firebaseAuth.uid}"
        hashMap["quote"] = "$quote"
        hashMap["pageNumber"] = "$quotePage"
        hashMap["timestamp"] = timestamp


        val ref = FirebaseDatabase.getInstance().getReference("Quotes")
        ref.child("$timestamp")
            .setValue(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                MyApplication.toast(this, "Quote added...")
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                MyApplication.toast(    this, "Failed to add quote due to ${e.message}")
            }
    }

    private fun addToLibrary(library: String) {

        progressDialog.setMessage("Adding to $library list")
        progressDialog.show()


        val timestamp = System.currentTimeMillis()


        val hashMap = HashMap<String, Any>()
        hashMap["bookId"] = "$bookId"
        hashMap["library"] = "$library"
        hashMap["timestamp"] = timestamp


        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child("${firebaseAuth.uid}")
            .child("Library")
            .child(bookId)
            .updateChildren(hashMap)
            .addOnSuccessListener {
                progressDialog.dismiss()
                MyApplication.toast(this, "Added to $library list")
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                MyApplication.toast(    this, "Failed to add to $library list due to ${e.message}")
            }
    }

    private fun addToFavorite() {
        Log.d(TAG, "addToFavorite: Adding $bookId to fav")
        val timestamp = System.currentTimeMillis()


        val hashMap = HashMap<String, Any>()
        hashMap["bookId"] = bookId
        hashMap["timestamp"] = timestamp


        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .child("Favorites")
            .child(bookId)
            .setValue(hashMap)
            .addOnSuccessListener {

                Log.d(TAG, "addToFavorite: Added to fav")
                MyApplication.toast(this, "Added to favorite")

            }.addOnFailureListener { e ->
                Log.d(TAG, "addToFavorite: Failed to add to fav due to ${e.message}")
                MyApplication.toast(this, "Failed to add to fav due to ${e.message}")
            }
    }

    private fun removeFromFavorite() {
        Log.d(TAG, "removeFromFavorite: Removing $bookId from fav")

        val firebaseAuth = FirebaseAuth.getInstance();


        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .child("Favorites")
            .child(bookId)
            .removeValue()
            .addOnSuccessListener {
                Log.d(TAG, "removeFromFavorite: Removed from fav")
                MyApplication.toast(this, "Removed from favorite")
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "removeFromFavorite: Failed to remove from fav due to ${e.message}")
                MyApplication.toast(this, "Failed to remove from fav due to ${e.message}")
            }
    }

    private fun deleteBook() {
        progressDialog.setMessage("Deleting Book")
        progressDialog.show()

        val ref = FirebaseDatabase.getInstance().getReference("Books")
        ref.child(bookId)
            .removeValue()
            .addOnSuccessListener {
                progressDialog.dismiss()
                Log.d(TAG, "deleteBook: Successfully Deleted")
                MyApplication.toast(this, "Successfully Deleted")
                onBackPressed()
            }
            .addOnFailureListener { e ->
                progressDialog.dismiss()
                Log.d(TAG, "deleteBook: failed to delete due to ${e.message}")
                MyApplication.toast(this, "Failed to delete due to ${e.message}")
            }
    }

}