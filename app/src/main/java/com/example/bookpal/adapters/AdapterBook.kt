package com.example.bookpal.adapters

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.bookpal.MyApplication
import com.example.bookpal.R
import com.example.bookpal.activities.BookDetailsActivity
import com.example.bookpal.databinding.RowBookBinding
import com.example.bookpal.filters.FilterBook
import com.example.bookpal.models.ModelBook
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

class AdapterBook : RecyclerView.Adapter<AdapterBook.HolderBook>, Filterable {

    private var context: Context
    var bookArrayList: ArrayList<ModelBook>
    private var filterList: ArrayList<ModelBook>

    private var filter: FilterBook? = null

    private var firebaseAuth: FirebaseAuth

    private companion object {
        private const val TAG = "ADAPTER_BOOK_TAG"
    }

    constructor(context: Context, bookArrayList: ArrayList<ModelBook>) {
        this.context = context
        this.bookArrayList = bookArrayList
        this.filterList = bookArrayList
        firebaseAuth = FirebaseAuth.getInstance()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HolderBook {
        val binding = RowBookBinding.inflate(LayoutInflater.from(context), parent, false)

        return HolderBook(binding)
    }

    override fun onBindViewHolder(holder: HolderBook, position: Int) {
        val model = bookArrayList[position]

        val id = model.id
        val name = model.name
        val author = model.author
        val genre = model.genre
        val bookImage = model.bookImage

        holder.titleTv.text = name
        holder.authorTv.text = author
        holder.genreTv.text = genre

        try {
            Glide.with(context)
                .load(bookImage)
                .placeholder(R.drawable.ic_twotone_book_genre_black)
                .into(holder.bookIv)
        } catch (e: Exception) {
            Log.e(TAG, "onBindViewHolder: ", e)
        }


        holder.itemView.setOnClickListener {
            val bookId = model.id
            val intent = Intent(context, BookDetailsActivity::class.java)
            intent.putExtra("bookId", bookId)
            context.startActivity(intent)
        }

    }

    override fun getItemCount(): Int {
        return bookArrayList.size
    }

    override fun getFilter(): Filter {
        if (filter == null) {
            filter = FilterBook(filterList, this)
        }
        return filter as FilterBook
    }

    inner class HolderBook(binding: RowBookBinding) : RecyclerView.ViewHolder(binding.root) {
        val bookIv = binding.bookIv
        val titleTv = binding.titleTv
        val genreTv = binding.genreTv
        val authorTv = binding.authorTv
    }

}