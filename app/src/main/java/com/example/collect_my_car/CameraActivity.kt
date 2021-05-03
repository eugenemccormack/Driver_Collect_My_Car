package com.example.collect_my_car

import android.app.Activity
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.collect_my_car.Model.Common
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.kusu.library.LoadingButton
import kotlinx.android.synthetic.main.activity_camera.*
import kotlinx.android.synthetic.main.activity_maps.*
import java.io.File
import java.util.*

private const val FILE_NAME = "photo.jpg"
private const val REQUEST_CODE = 42
private lateinit var photoFile: File

class CameraActivity : AppCompatActivity() {

    private lateinit var finish_Picture: LoadingButton

    private lateinit var database: FirebaseDatabase
    private lateinit var tripInfoRef: DatabaseReference

    private var fileUri : Uri? = null
    private var fileProvider: Uri? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        var counter: Int = 0;

        finish_Picture = findViewById(R.id.finish_Picture) as LoadingButton


        finish_Picture.setOnClickListener {

            finish()

        }

        btnTakePicture.setOnClickListener {

                counter++

            if (counter >= 4) {

                //finish_Picture.setBackgroundResource(R.drawable.button_border)

                finish_Picture.isEnabled = true

            }


                val takePictureIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                photoFile = getPhotoFile(FILE_NAME)

                fileProvider = FileProvider.getUriForFile(this, "com.example.collect_my_car.fileprovider", photoFile)

                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, fileProvider)

                if (takePictureIntent.resolveActivity(this.packageManager) != null) {

                    startActivityForResult(takePictureIntent, REQUEST_CODE)

                } else {

                    Toast.makeText(this, "Unable to open camera", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun getPhotoFile(fileName: String): File {

        val storageDirectory = getExternalFilesDir(Environment.DIRECTORY_PICTURES)

        return File.createTempFile(fileName, ".jpg", storageDirectory)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE && resultCode == Activity.RESULT_OK) {

            val takenImage = BitmapFactory.decodeFile(photoFile.absolutePath)

            imageView.setImageBitmap(takenImage)

            fileUri = data?.data

            uploadImageToFireBaseStorage()
        }

        else {

            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    private fun uploadImageToFireBaseStorage() {

        val cameraMode = intent.getStringExtra(MapsActivity.MESSGAE)
        val tripNumberId = intent.getStringExtra(MapsActivity.MESSGAE2)

        database = FirebaseDatabase.getInstance()

        tripInfoRef = database.getReference(Common.TRIP)

        if(fileProvider == null) {

            Toast.makeText(this, "ERROR - Please Select A Photo", Toast.LENGTH_SHORT).show()

            return
        }

        val filename = UUID.randomUUID().toString()

        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(fileProvider!!)
                .addOnSuccessListener { it ->

                    ref.downloadUrl.addOnSuccessListener {

                        val photos = it.toString()


                        if (cameraMode != null) {

                            tripInfoRef.child(tripNumberId!!).child(cameraMode).push().setValue(photos)
                        }
                        else{

                            Toast.makeText(this@CameraActivity, "Cannot Find Camera Mode", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
    }
}