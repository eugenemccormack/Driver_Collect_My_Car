package com.example.collect_my_car

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.collect_my_car.Model.Common
import com.example.collect_my_car.Model.DriverInfoModel
import com.example.collect_my_car.Utils.UserUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity: AppCompatActivity(){

    private lateinit var database : FirebaseDatabase
    private lateinit var driverInfoRef : DatabaseReference
    private lateinit var firebaseAuth : FirebaseAuth
    private lateinit var listener: FirebaseAuth.AuthStateListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_login)

        login_button_login.setOnClickListener {

            signIn()

        }

        not_signed_up.setOnClickListener {

            val intent = Intent(this, Register::class.java)

            startActivity(intent)
        }
    }


    private fun signIn(){

        val email = email_editText_login.text.toString().trim()
        val password = password_editText_login.text.toString().trim()

        if(email.isEmpty()){

            Toast.makeText(this, "ERROR - Please Enter a Email Address", Toast.LENGTH_SHORT).show()

            return

        }

        else if(password.isEmpty()){

            Toast.makeText(this, "ERROR - Please Enter a Password", Toast.LENGTH_SHORT).show()

            return
        }


        FirebaseAuth.getInstance().signInWithEmailAndPassword(email, password)

            .addOnCompleteListener{

                if(!it.isSuccessful) return@addOnCompleteListener

                firebaseAuth.addAuthStateListener(listener)

            }

            .addOnFailureListener{

                Toast.makeText(this, "ERROR - Failed to Sign in with User : ${it.message}", Toast.LENGTH_SHORT).show()
            }


        firebaseAuth = FirebaseAuth.getInstance()
        listener = FirebaseAuth.AuthStateListener { myFirebaseAuth ->

            val user = myFirebaseAuth.currentUser

            if(user != null){

                FirebaseMessaging.getInstance().token
                        .addOnFailureListener { e->


                            Toast.makeText(this, e.message, Toast.LENGTH_SHORT).show()

                        }

                        .addOnSuccessListener { token ->

                            UserUtils.updateToken(this@LoginActivity, token)
                        }

                getUserFromFirebase()
            }
        }
    }


    private fun getUserFromFirebase(){

        database = FirebaseDatabase.getInstance()
        driverInfoRef = database.getReference(Common.DRIVER_INFO_REFERENCE)
        firebaseAuth = FirebaseAuth.getInstance()

        driverInfoRef.child(FirebaseAuth.getInstance().currentUser!!.uid)
                .addListenerForSingleValueEvent(object : ValueEventListener {

                    override fun onDataChange(p0: DataSnapshot) {

                        if(p0.exists()){

                            val model = p0.getValue(DriverInfoModel::class.java)

                            goToMapsActivity(model)
                        }
                    }

                    override fun onCancelled(p0: DatabaseError) {

                        Toast.makeText(this@LoginActivity, p0.message, Toast.LENGTH_SHORT).show()
                    }
                })
    }

    private fun goToMapsActivity(model: DriverInfoModel?) {

        Common.currentUser = model

        val intent = Intent(this, MapsActivity::class.java)

        startActivity(intent)

        finish()
    }
}