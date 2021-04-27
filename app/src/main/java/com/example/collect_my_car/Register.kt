package com.example.collect_my_car

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.collect_my_car.Model.Common
import com.example.collect_my_car.Model.DriverInfoModel
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import kotlinx.android.synthetic.main.activity_register.*
import java.util.*


class Register : AppCompatActivity() {

    private lateinit var database: FirebaseDatabase
    private lateinit var driverInfoRef: DatabaseReference

    private var fileUri : Uri? = null

    private var licenceFileUri : Uri? = null

    //lateinit var licenceImage: String

    var licenceImage: String? = null

    var buttonID = false

    //var photos = "test"

         override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_register)

             //var photos = "test"


             //photo_button_user.setOnClickListener {

               //  Log.d("MainActivity", "OnClick")

               //  val intent = Intent(Intent.ACTION_PICK)
               //  intent.type = "image/*"
               //  startActivityForResult(intent, 1)

             //}

             licence_upload_button_register.setOnClickListener {


                 buttonID = true

                 Log.d("Register", "Button ID $buttonID")

                 selectLicenceDocuments()


             }

             iv_profileImage.setOnClickListener {

                 selectImage()

             }


            register_button_register.setOnClickListener {

                register()

            }

             not_signed_up.setOnClickListener {

                 Log.d("MainActivity", "Show Login Activity")

                 val intent = Intent(this, LoginActivity::class.java)

                 startActivity(intent)

             }

    }

    private fun selectLicenceDocuments() {

        ImagePicker.with(this)
                .crop().compress(1024).maxResultSize(1080, 1080).start()

    }

    private fun selectImage() {

        ImagePicker.with(this)
            .crop().compress(1024).maxResultSize(1080, 1080).start()


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {

        Log.d("MainActivity", "Photo was Selected 1")

        super.onActivityResult(requestCode, resultCode, data)

        when(resultCode){
            Activity.RESULT_OK -> {

                if(buttonID){

                    buttonID = false


                    licenceFileUri = data?.data

                    Log.d("Register", "licenceFileUri " + licenceFileUri)


                }

                else{




                    fileUri = data?.data

                    Log.d("Register", "fileUri " + fileUri)

                    iv_profileImage.setImageURI(fileUri)

                }

            }

            ImagePicker.RESULT_ERROR -> {

                Toast.makeText(this, ImagePicker.getError(data), Toast.LENGTH_SHORT).show()

            }

                else -> {

                    Toast.makeText(this, "Image Selection Cancelled", Toast.LENGTH_SHORT).show()

                }



        }


    }

    private fun register(){

        database = FirebaseDatabase.getInstance()
        driverInfoRef = database.getReference(Common.DRIVER_INFO_REFERENCE)



        val model = DriverInfoModel()

        model.name = username_editText_register.text.toString()
        model.phone = phone_editText_register.text.toString()
        model.email = email_editText_register.text.toString()
        val password = password_editText_register.text.toString()
        val confirmPassword = confirm_password_editText_register.text.toString()

        model.address1 = address1_editText_register.text.toString()
        model.address2 = address2_editText_register.text.toString()
        model.county = county_editText_register.text.toString()

        model.licenceSurname = licence_surname_editText_register.text.toString()
        model.licenceFirstName = licence_firstname_editText_register.text.toString()
        val licenceBOD = licence_dob_editText_register.text.toString()
        val licenceIssueDate = licence_issue_editText_register.text.toString()
        val licenceExpiryDate = licence_expiry_editText_register.text.toString()
        val licenceDriverNumber = licence_driver_number_editText_register.text.toString()
        model.licenceNumber = licence_number_editText_register.text.toString()

        if(model.name.isEmpty()){

            Toast.makeText(this, "ERROR - Please Enter a Name", Toast.LENGTH_SHORT).show()

            return

        }

        else if(model.phone.isEmpty()){

            Toast.makeText(this, "ERROR - Please Enter a Phone Number", Toast.LENGTH_SHORT).show()

            return

        }

        else if(model.email.isEmpty()){

            Toast.makeText(this, "ERROR - Please Enter a Email Address", Toast.LENGTH_SHORT).show()

            return

        }

        else if(model.address1!!.isEmpty()){

            Toast.makeText(this, "ERROR - Please Enter an Address Line 1", Toast.LENGTH_SHORT).show()

            return

        }

        else if(model.address2!!.isEmpty()){

            Toast.makeText(this, "ERROR - Please Enter an Address Line 2", Toast.LENGTH_SHORT).show()

            return

        }

        else if(model.county!!.isEmpty()){

            Toast.makeText(this, "ERROR - Please Enter a County", Toast.LENGTH_SHORT).show()

            return

        }

        else if(password.isEmpty()){

            Toast.makeText(this, "ERROR - Please Enter a Password", Toast.LENGTH_SHORT).show()

            return

        }

        else if(confirmPassword.isEmpty()){

            Toast.makeText(this, "ERROR - Please Enter a Confirm Password", Toast.LENGTH_SHORT).show()

            return

        }

        else if(model.licenceSurname!!.isEmpty()){

            Toast.makeText(this, "ERROR - Please Enter Licence Surname", Toast.LENGTH_SHORT).show()

            return

        }

        else if(model.licenceFirstName!!.isEmpty()){

            Toast.makeText(this, "ERROR - Please Enter Licence First Name", Toast.LENGTH_SHORT).show()

            return

        }

        else if(licenceBOD.isEmpty()){

            Toast.makeText(this, "ERROR - Please Enter Licence Date of Birth", Toast.LENGTH_SHORT).show()

            return

        }

        else if(licenceIssueDate.isEmpty()){

            Toast.makeText(this, "ERROR - Please Enter Licence Issue Date", Toast.LENGTH_SHORT).show()

            return

        }

        else if(licenceExpiryDate.isEmpty()){

            Toast.makeText(this, "ERROR - Please Enter Licence Expiry Date", Toast.LENGTH_SHORT).show()

            return

        }

        else if(licenceDriverNumber!!.isEmpty()){

            Toast.makeText(this, "ERROR - Please Enter Licence Driver Number", Toast.LENGTH_SHORT).show()

            return

        }

        else if(model.licenceNumber!!.isEmpty()){

            Toast.makeText(this, "ERROR - Please Enter Licence Number", Toast.LENGTH_SHORT).show()

            return

        }

        if(password != confirmPassword){

            Toast.makeText(this, "ERROR - Confirmation Password Does Not Match, Try Again", Toast.LENGTH_SHORT).show()

            return

        }

        if(fileUri == null) {

            Toast.makeText(this, "ERROR - Please Select A Photo", Toast.LENGTH_SHORT).show()

            return

        }

        if(licenceFileUri== null){

            Toast.makeText(this, "ERROR - Please Upload Licence Details 1", Toast.LENGTH_SHORT).show()

            return

        }

        Log.d("MainActivity", "Email is : " + model.email)
        Log.d("MainActivity", "Password is : $password")

        //Firebase Authentication to Create a User with Email and Password
        FirebaseAuth.getInstance().createUserWithEmailAndPassword(model.email, password)
            .addOnCompleteListener{

                if(!it.isSuccessful) return@addOnCompleteListener


                Log.d("Main", "Successfully Create User with UID: ${it.result?.user!!.uid}")

                uploadImageToFireBaseStorage()
            }

            .addOnFailureListener{

                Log.d("Main", "ERROR - Failed to Create User : ${it.message}")

                Toast.makeText(this, "ERROR - Fail to Create User : ${it.message}", Toast.LENGTH_SHORT).show()

            }
    }

    private fun uploadImageToFireBaseStorage() {

        if(fileUri == null) {

            Toast.makeText(this, "ERROR - Please Select A Photo", Toast.LENGTH_SHORT).show()

            return
        }

        if(licenceFileUri== null){

            Toast.makeText(this, "ERROR - Please Upload Licence Details 2", Toast.LENGTH_SHORT).show()

            return

        }

        val filename = UUID.randomUUID().toString()

        val ref = FirebaseStorage.getInstance().getReference("/images/$filename")

        ref.putFile(fileUri!!)
            .addOnSuccessListener {

                Log.d("Main", "Successfully Uploaded Image :  ${it.metadata?.path}")

                ref.downloadUrl.addOnSuccessListener {

                    //it.toString()

                    Log.d("Main", "Image File Location : $it")

                   saveDriverInfo(it.toString())

                   // var profileImageUrl: String  = it.toString()

                    Log.d("Main", "Image File Location : $it")

                }

            }

/*        val ref2 = FirebaseStorage.getInstance().getReference("/licencedetails/$filename")

        ref2.putFile(licenceFileUri!!)
                .addOnSuccessListener {

                    Log.d("Main", "Successfully Uploaded Image :  ${it.metadata?.path}")

                    ref2.downloadUrl.addOnSuccessListener {

                        //it.toString()

                        Log.d("Main", "Licence Image File Location : $it")

                        saveLicenceDocument(it.toString())

                        Log.d("Main", "saveLicenceDocument: " + it.toString())

                        //licenceImage = it.toString()

                        //var profileImageUrl: String  = it.toString()

                        // Log.d("Main", "licenceImage : " + licenceImage)

                        Log.d("Main", "Licence Image File Location : $it")

                    }

                }*/

    }

/*    private fun saveLicenceDocument(profileImageUrla: String){

*//*        val model = UserModel()
*//**//*
        database = FirebaseDatabase.getInstance()
        userInfoRef = database.getReference(Common.USER_INFO_REFERENCE)*//**//*

        model.licenceDocuments = profileImageUrl*//*

        val model = DriverInfoModel()

        model.licenceDocuments = profileImageUrla

        licenceImage = profileImageUrla

        Log.d("Main", "licenceImage : " + licenceImage)

        Log.d("Main", "model.insuranceDocuments : " + model.licenceDocuments)


*//*
        userInfoRef.child(FirebaseAuth.getInstance().currentUser!!.uid)
            .setValue(model)*//*


    }*/

    private fun saveDriverInfo(profileImageUrl: String){

        //var photos = "test"

        val filename = UUID.randomUUID().toString()

        val ref2 = FirebaseStorage.getInstance().getReference("/licencedetails/$filename")

        ref2.putFile(licenceFileUri!!)
                .addOnSuccessListener {

                    Log.d("Main", "Successfully Uploaded Image :  ${it.metadata?.path}")

                    ref2.downloadUrl.addOnSuccessListener {

                        Log.d("Main", "Licence Image File Location : $it")

                        Log.d("Main", "saveLicenceDocument: " + it.toString())

                        //val model = DriverInfoModel()

                        var photos = it.toString()

                        Log.d("Main", "Photos: " + photos)

                        //model.licenceDocuments = photos

                        Log.d("Main", "Licence Image File Location : $it")



        val model = DriverInfoModel()

        database = FirebaseDatabase.getInstance()
        driverInfoRef = database.getReference(Common.DRIVER_INFO_REFERENCE)

/*        if(licenceImage== null){

            Toast.makeText(this, "ERROR - Please Upload Licence Details", Toast.LENGTH_SHORT).show()

            return

        }*/

        //model.licenceDocuments = licenceImage

        //Log.d("Main", "saveUserInfo licenceImage : " + model.licenceDocuments)

/*        if(licenceImage== null){

            Toast.makeText(this, "ERROR - Please Upload Licence Details", Toast.LENGTH_SHORT).show()

            return
        }*/

        //model.licenceDocuments = photos


        model.image = profileImageUrl
        model.name = username_editText_register.text.toString()
        model.phone = phone_editText_register.text.toString()
        model.email = email_editText_register.text.toString()

        model.address1 = address1_editText_register.text.toString()
        model.address2 = address2_editText_register.text.toString()
        model.county = county_editText_register.text.toString()

        model.licenceSurname = licence_surname_editText_register.text.toString()
        model.licenceFirstName = licence_firstname_editText_register.text.toString()
        model.licenceBOD = licence_dob_editText_register.text.toString().toInt()
        model.licenceIssueDate = licence_issue_editText_register.text.toString().toInt()
        model.licenceExpiryDate = licence_expiry_editText_register.text.toString().toInt()
        model.licenceDriverNumber = licence_driver_number_editText_register.text.toString().toInt()
        model.licenceNumber = licence_number_editText_register.text.toString()

        model.licenceDocuments = photos //Image

        Log.d("Main", "PHOTO MODEL : " + model.licenceDocuments)


        driverInfoRef.child(FirebaseAuth.getInstance().currentUser!!.uid)
            .setValue(model)

        val intent = Intent(this, LoginActivity::class.java)

        startActivity(intent)

        finish()

    }

                }

    }


}