package com.example.collect_my_car

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.util.Log
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

    companion object{

        val MESSGAE = "Message"
    }

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

            Log.d("`Login Activity`", "Show Register Activity")

            val intent = Intent(this, Register::class.java)

            startActivity(intent)

        }


    }

    override fun onStop() {

        //if(firebaseAuth != null && listener != null) firebaseAuth.removeAuthStateListener(listener)
        super.onStop()
    }

    @SuppressLint("StringFormatInvalid")
    private fun signIn(){

        val email = email_editText_login.text.toString()
        val password = password_editText_login.text.toString()

        Log.d("Login", "Attempt Login with Email / Password : $email/***")

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

                Log.d("Main", "Successfully Signed in with User with UID: ${it.result?.user!!.uid}")

                /*val intent = Intent(this, MapsActivity::class.java) //HomeActivity

               // intent.putExtra(MESSGAE, email)

                startActivity(intent)*/

               /* FirebaseInstanceId.getInstance().instanceId
                    .addOnFailureListener { e->


                        Toast.makeText(this, e.message + "TEST", Toast.LENGTH_SHORT).show()

                    }

                    .addOnSuccessListener { instanceIdResult ->


                        Log.d("TOKEN", instanceIdResult.token + " NOTIFY")
                        UserUtils.updateToken(this@LoginActivity, instanceIdResult.token)



                    }*/

                firebaseAuth.addAuthStateListener(listener)

                //getUserFromFirebase()

            }

            .addOnFailureListener{

                Log.d("Main", "ERROR - Failed to Sign in with User : ${it.message}")

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


                    Log.d("TOKEN", token)
                    UserUtils.updateToken(this@LoginActivity, token)



                }


            getUserFromFirebase()

        }

        }

/*        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.d("TOKEN2", "Fetching FCM registration token failed", task.exception)
                return@OnCompleteListener
            }

            // Get new FCM registration token
            val token = task.result

            Log.d("TOKEN2", token)

            // Log and toast
            val msg = getString(R.string.msg_token_fmt, token)
            Log.d("TOKEN2", msg)
            Toast.makeText(baseContext, msg, Toast.LENGTH_SHORT).show()
        })*/
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

        //intent.putExtra(MESSGAE, email)

        startActivity(intent)

        finish()



    }


}