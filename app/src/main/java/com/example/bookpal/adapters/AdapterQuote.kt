package com.example.bookpal.adapters

import android.app.ProgressDialog
import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.example.bookpal.MyApplication
import com.example.bookpal.R
import com.example.bookpal.databinding.DialogQuoteAddBinding
import com.example.bookpal.databinding.RowQuoteBinding
import com.example.bookpal.filters.FilterBook
import com.example.bookpal.filters.FilterQuote
import com.example.bookpal.models.ModelBook
import com.example.bookpal.models.ModelQuote
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdapterQuote : RecyclerView.Adapter<AdapterQuote.HolderQuote>, Filterable {


    val context: Context


    var quoteArrayList: ArrayList<ModelQuote>
    val filterList: ArrayList<ModelQuote>

    private var filter: FilterQuote? = null


    private lateinit var binding: RowQuoteBinding


    private var firebaseAuth: FirebaseAuth

    private var progressDialog: ProgressDialog

    private companion object {
        private const val TAG = "ADAPTER_QUOTE_TAG"
    }


    constructor(context: Context, commentArrayList: ArrayList<ModelQuote>) {
        this.context = context
        this.quoteArrayList = commentArrayList
        this.filterList = commentArrayList


        firebaseAuth = FirebaseAuth.getInstance()


        progressDialog = ProgressDialog(context)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderQuote {
        binding = RowQuoteBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderQuote(binding.root)
    }

    override fun onBindViewHolder(holder: HolderQuote, position: Int) {
        val model = quoteArrayList[position]

        val id = model.id
        val bookId = model.bookId
        val uid = model.uid
        val quote = model.quote
        val pageNumber = model.pageNumber
        val timestamp = model.timestamp

        val date = MyApplication.formatTimeStamp(timestamp)


        holder.dateTv.text = date
        holder.quoteTv.text = quote
        holder.quotePageNumberTv.text = pageNumber

        checkIsFavorite(model, holder)


        holder.itemView.setOnClickListener {
            if (firebaseAuth.currentUser != null && firebaseAuth.uid == uid) {
                quoteOptionsDialog(model, holder)
            }
        }

        holder.favoriteBtn.setOnClickListener {
            val isInMyFavorite = model.isInMyFavorite
            if (isInMyFavorite) {
                removeFromFavorite(model)
            } else {
                addToFavorite(model)
            }
        }
    }

    private fun checkIsFavorite(model: ModelQuote, holder: HolderQuote) {
        val bookId = model.id
        Log.d(TAG, "checkIsFavorite: Checking if book $bookId is in fav or not")

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .child("FavoriteQuotes")
            .child(bookId)
            .addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val isInMyFavorite = snapshot.exists()
                    model.isInMyFavorite = isInMyFavorite
                    Log.d(TAG, "onDataChange: Book $bookId Exists: $isInMyFavorite")
                    if (isInMyFavorite) {
                        //available in favorite
                        Log.d(TAG, "onDataChange: available in favorite ")

                        holder.favoriteBtn.setImageResource(R.drawable.ic_favorite_filled)
                    } else {
                        //not available in favorite
                        Log.d(TAG, "onDataChange: not available in favorite")
                        //set drawable top icon
                        holder.favoriteBtn.setImageResource(R.drawable.ic_favorite_outline)
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                }
            })
    }

    private fun addToFavorite(model: ModelQuote) {
        val quoteId = model.id
        val bookId = model.bookId
        Log.d(TAG, "addToFavorite: Adding $quoteId to fav")
        val timestamp = System.currentTimeMillis()


        val hashMap = HashMap<String, Any>()
        hashMap["quoteId"] = quoteId
        hashMap["bookId"] = bookId
        hashMap["timestamp"] = timestamp


        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!)
            .child("FavoriteQuotes")
            .child(quoteId)
            .setValue(hashMap)
            .addOnSuccessListener {

                Log.d(TAG, "addToFavorite: Added to fav")
                MyApplication.toast(context, "Added to favorite")

            }.addOnFailureListener { e ->
                Log.d(TAG, "addToFavorite: Failed to add to fav due to ${e.message}")
                MyApplication.toast(context, "Failed to add to fav due to ${e.message}")
            }
    }

    private fun removeFromFavorite(model: ModelQuote) {
        val quoteId = model.id
        Log.d(TAG, "removeFromFavorite: Removing $quoteId from fav")

        val firebaseAuth = FirebaseAuth.getInstance();

        val ref = FirebaseDatabase.getInstance().getReference("Users")
        ref.child(firebaseAuth.uid!!).child("FavoriteQuotes").child(quoteId)
            .removeValue()
            .addOnSuccessListener {
                Log.d(TAG, "removeFromFavorite: Removed from fav")
                MyApplication.toast(context, "Removed from favorite")
            }
            .addOnFailureListener { e ->
                Log.d(TAG, "removeFromFavorite: Failed to remove from fav due to ${e.message}")
                MyApplication.toast(context, "Failed to remove from fav due to ${e.message}")
            }
    }

    private fun quoteOptionsDialog(model: ModelQuote, holder: HolderQuote){
        val options = arrayOf("Edit", "Delete")

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Choose Options")
            .setItems(options){d,i->
                if (i == 0) {
                    //Edit
                    editQuoteDialog(model, holder)
                }
                else if (i == 1) {
                    //Delete
                    deleteQuoteDialog(model, holder)
                }
            }
            .show()
    }

    private fun deleteQuoteDialog(model: ModelQuote, holder: HolderQuote) {

        val builder = AlertDialog.Builder(context)
        builder.setTitle("Delete Quote")
            .setMessage("Are you sure you want to delete this quote?")
            .setPositiveButton("DELETE") { d, z ->


                val ref = FirebaseDatabase.getInstance().getReference("Quotes")
                ref.child(model.id)
                    .removeValue()
                    .addOnSuccessListener {
                        MyApplication.toast(context, "Comment deleted...")
                    }
                    .addOnFailureListener { e ->
                        MyApplication.toast(context, "Failed to delete comment due to ${e.message}")
                    }

            }
            .setNegativeButton("CANCEL") { d, z ->
                d.dismiss()
            }
            .show()
    }

    private fun editQuoteDialog(model: ModelQuote, holder: HolderQuote) {
        val quoteAddBinding = DialogQuoteAddBinding.inflate(LayoutInflater.from(context))

        quoteAddBinding.titleTv.text = "Edit Quote"
        quoteAddBinding.quoteEt.setText(model.quote)
        quoteAddBinding.pageNumberEt.setText(model.pageNumber)


        val builder = AlertDialog.Builder(context, R.style.CustomDialog)
        builder.setView(quoteAddBinding.root)

        val alertDialog = builder.create()
        alertDialog.show()


        quoteAddBinding.backBtn.setOnClickListener { alertDialog.dismiss() }


        quoteAddBinding.submitBtn.setOnClickListener {

            val quote = quoteAddBinding.quoteEt.text.toString().trim()
            val quotePage = quoteAddBinding.pageNumberEt.text.toString().trim()

            if (quote.isEmpty()) {
                MyApplication.toast(context, "Enter Quote....")
            } else if (quotePage.isEmpty()) {
                MyApplication.toast(context, "Enter Page Number....")
            } else {
                alertDialog.dismiss()

                progressDialog.setMessage("Updating Quote")
                progressDialog.show()

                val hashMap = HashMap<String, Any>()
                hashMap["quote"] = "$quote"
                hashMap["pageNumber"] = "$quotePage"


                val ref = FirebaseDatabase.getInstance().getReference("Quotes")
                ref.child(model.id)
                    .updateChildren(hashMap)
                    .addOnSuccessListener {
                        progressDialog.dismiss()
                        MyApplication.toast(context, "Quote update...")
                    }
                    .addOnFailureListener { e ->
                        progressDialog.dismiss()
                        MyApplication.toast(    context, "Failed to update quote due to ${e.message}")
                    }
            }
        }
    }



    override fun getFilter(): Filter {
        if (filter == null) {
            filter = FilterQuote(filterList, this)
        }
        return filter as FilterQuote
    }

    override fun getItemCount(): Int {
        return quoteArrayList.size
    }


    inner class HolderQuote(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val quoteTv = binding.quoteTv
        val quotePageNumberTv = binding.quotePageNumberTv
        val dateTv = binding.dateTv
        val favoriteBtn = binding.favoriteBtn
    }

}