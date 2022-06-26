package com.example.bookpal

import android.app.Application
import android.content.Context
import android.text.format.DateFormat
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import java.util.*
import kotlin.collections.HashMap

class MyApplication:Application() {

    override fun onCreate() {
        super.onCreate()
    }

    companion object{


        fun formatTimeStamp(timestamp: Long) : String{
            val cal = Calendar.getInstance(Locale.ENGLISH)
            cal.timeInMillis = timestamp
            return  DateFormat.format("dd/MM/yyyy", cal).toString()
        }



        public fun removeFromFavorite(context: Context, bookId: String){
            val TAG = "REMOVE_FAV_TAG"
            Log.d(TAG, "removeFromFavorite: Removing from fav")

            val firebaseAuth = FirebaseAuth.getInstance();

            val ref = FirebaseDatabase.getInstance().getReference("Users")
            ref.child(firebaseAuth.uid!!).child("Favorites").child(bookId)
                .removeValue()
                .addOnSuccessListener {
                    Log.d(TAG, "removeFromFavorite: Removed from fav")
                    Toast.makeText(context, "Removed from favorite", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e->
                    Log.d(TAG, "removeFromFavorite: Failed to remove from fav due to ${e.message}")
                    Toast.makeText(context, "Failed to remove from fav due to ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        fun toast(context: Context, message: String){
            Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

}