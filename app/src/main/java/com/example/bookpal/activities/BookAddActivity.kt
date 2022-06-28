package com.example.bookpal.activities

import android.Manifest
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.Menu
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.widget.PopupMenu
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.example.bookpal.MyApplication
import com.example.bookpal.R
import com.example.bookpal.databinding.ActivityBookAddBinding
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage

class BookAddActivity : AppCompatActivity() {


    private lateinit var binding: ActivityBookAddBinding

    private companion object{

        private const val CAMERA_REQUEST_CODE = 100
        private const val  STORAGE_REQUEST_CODE = 200
    }


    private lateinit var firebaseAuth: FirebaseAuth


    private var imageUri: Uri? = null


    private lateinit var progressDialog: ProgressDialog

    private lateinit var cameraPermissions: Array<String>
    private lateinit var storagePermissions: Array<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBookAddBinding.inflate(layoutInflater)
        setContentView(binding.root)


        cameraPermissions = arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        storagePermissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)


        firebaseAuth = FirebaseAuth.getInstance()


        progressDialog = ProgressDialog(this)
        progressDialog.setTitle("Please wait")
        progressDialog.setCanceledOnTouchOutside(false)


        binding.backFab.setOnClickListener {
            onBackPressed()
        }


        binding.pickImageFab.setOnClickListener {
            showImageAttachMenu()
        }


        binding.submitBtn.setOnClickListener {
            validateData()
        }

    }

    private var name = ""
    private var genre = ""
    private var author = ""

    private fun validateData() {

        name = binding.nameEt.text.toString().trim()
        genre = binding.genreEt.text.toString().trim()
        author = binding.authorEt.text.toString().trim()


        if (name.isEmpty()) {
            MyApplication.toast(this, "Enter Book Name...")
        } else if (genre.isEmpty()) {
            MyApplication.toast(this, "Enter Book Genre...")
        } else if (author.isEmpty()) {
            MyApplication.toast(this, "Enter Book Author...")
        } else {
            val timestamp = System.currentTimeMillis()
            if (imageUri == null) {

                updateBookInfo("", timestamp)
            } else {

                uploadBookImage(timestamp)
            }
        }

    }



    private fun uploadBookImage(timestamp: Long) {
        progressDialog.setMessage("Uploading book...")
        progressDialog.show()


        val filePathAndName = "BookImages/$timestamp"


        val reference = FirebaseStorage.getInstance().getReference(filePathAndName)
        reference.putFile(imageUri!!)
            .addOnSuccessListener { taskSnapshot ->

                val uriTask: Task<Uri> = taskSnapshot.storage.downloadUrl
                while (!uriTask.isSuccessful);
                val uploadedImageUrl = "${uriTask.result}"

                updateBookInfo(uploadedImageUrl, timestamp)
            }
            .addOnFailureListener { e ->

                progressDialog.dismiss()
                MyApplication.toast(this, "Failed to upload image due to ${e.message}")
            }
    }

    private fun updateBookInfo(uploadedImageUrl: String, timestamp: Long) {
        progressDialog.setMessage("Updating book info...")


        val hashmap: HashMap<String, Any> = HashMap()
        hashmap["id"] = "$timestamp"
        hashmap["name"] = "$name"
        hashmap["genre"] = "$genre"
        hashmap["author"] = "$author"
        hashmap["uid"] = "${firebaseAuth.uid}"
        hashmap["timestamp"] = timestamp
        if (imageUri != null) {
            hashmap["bookImage"] = uploadedImageUrl
        }


        val reference = FirebaseDatabase.getInstance().getReference("Books")
        reference.child("$timestamp")
            .updateChildren(hashmap)
            .addOnSuccessListener {
             
                progressDialog.dismiss()
                MyApplication.toast(this, "Book Added...")
            }
            .addOnFailureListener { e ->
               
                progressDialog.dismiss()
                MyApplication.toast(this, "Failed to add due to ${e.message}")
            }
    }

    private fun showImageAttachMenu() {



        val popupMenu = PopupMenu(this, binding.profileIv)
        popupMenu.menu.add(Menu.NONE, 0, 0, "Gallery")
        popupMenu.menu.add(Menu.NONE, 1, 1, "Camera")
        popupMenu.show()



        popupMenu.setOnMenuItemClickListener { item ->

            val id = item.itemId
            if (id == 0) {
                if (checkStoragePermission()) {
                    pickImageGallery()
                } else {
                    requestStoragePermission()
                }
            } else if (id == 1) {
                if (checkCameraPermission()) {
                    pickImageCamera()
                } else {
                    requestCameraPermission()
                }
            }

            true
        }
    }

    private fun pickImageCamera() {

        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Temp_Title")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Temp_Description")

        imageUri = contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)
        cameraActivityResultLauncher.launch(intent)
    }

    private fun pickImageGallery() {

        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        galleryActivityResultLauncher.launch(intent)
    }


    private val cameraActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->

            if (result.resultCode == RESULT_OK) {
                val data = result.data

                try {
                    Glide.with(this)
                        .load(imageUri)
                        .placeholder(R.drawable.ic_person_gray)
                        .into(binding.profileIv)
                } catch (e: Exception) {

                }
            } else {

                MyApplication.toast(this, "Cancelled")
            }
        }
    )


    private val galleryActivityResultLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult(),
        ActivityResultCallback<ActivityResult> { result ->

            if (result.resultCode == RESULT_OK) {
                val data = result.data
                imageUri = data!!.data

                try {
                    Glide.with(this)
                        .load(imageUri)
                        .placeholder(R.drawable.ic_person_gray)
                        .into(binding.profileIv)
                } catch (e: Exception) {

                }
            } else {

                MyApplication.toast(this, "Cancelled")
            }
        }
    )

    private fun checkStoragePermission(): Boolean {

        return ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermission() {

        ActivityCompat.requestPermissions(this, storagePermissions,
            STORAGE_REQUEST_CODE
        )
    }

    private fun checkCameraPermission(): Boolean {

        val result = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.CAMERA
        ) == PackageManager.PERMISSION_GRANTED
        val result1 = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
        return result && result1
    }

    private fun requestCameraPermission() {

        ActivityCompat.requestPermissions(this, cameraPermissions,
            CAMERA_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (grantResults.isNotEmpty()) {
                    val cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    val storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED
                    if (cameraAccepted && storageAccepted) {

                        pickImageCamera()
                    } else {

                        MyApplication.toast(this, "Camera & Storage both permissions are necessary...")
                    }
                } else {
                }
            }
            STORAGE_REQUEST_CODE -> {
                if (grantResults.isNotEmpty()) {
                    val storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    if (storageAccepted) {

                        pickImageGallery()
                    } else {

                        MyApplication.toast(this, "Storage permissions necessary...")
                    }
                } else {
                }
            }
        }
    }

}
