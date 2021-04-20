package com.example.collect_my_car

import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.example.collect_my_car.Model.Common
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.activity_navigation_drawer_driver.*

class NavigationDrawerDriver : AppCompatActivity() {

    lateinit var toggle: ActionBarDrawerToggle

    private lateinit var image_avatar : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_navigation_drawer_driver)

        toggle = ActionBarDrawerToggle(this, drawerLayout, R.string.open, R.string.closed)

        drawerLayout.addDrawerListener(toggle)

        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navView.setNavigationItemSelectedListener {

            when(it.itemId){

                R.id.mItem1 -> {

                    intent = Intent(this, MapsActivity::class.java)

                    startActivity(intent)

                    Toast.makeText(applicationContext, "Maps", Toast.LENGTH_SHORT).show()
                }

                R.id.mItem2 -> {

                    intent = Intent(this, NavigationDrawerDriver::class.java)

                    startActivity(intent)

                    Toast.makeText(applicationContext, "Navigation Activity", Toast.LENGTH_SHORT).show()
                }

                R.id.mItem3 -> {

                    intent = Intent(this, HomeActivity::class.java)

                    startActivity(intent)

                    Toast.makeText(applicationContext, "Sensor Activity", Toast.LENGTH_SHORT).show()
                }

                R.id.mItem4 -> {

                    intent = Intent(this, GraphActivity::class.java)

                    startActivity(intent)

                    Toast.makeText(applicationContext, "Graph Activity", Toast.LENGTH_SHORT).show()
                }

                R.id.mItem5 -> {

                    val builder = AlertDialog.Builder(this)

                    builder.setTitle("Logout")
                        .setMessage("Do you really want to Logout?")
                        .setNegativeButton("Cancel", {dialogInterface, _ -> dialogInterface.dismiss()})

                        .setPositiveButton("Logout") { dialogInterface, _ ->

                            FirebaseAuth.getInstance().signOut()

                            val intent = Intent(this, LoginActivity::class.java)
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                            startActivity(intent)
                            finish()

                        }.setCancelable(false)

                    val dialog = builder.create()
                    dialog.setOnShowListener {

                        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(resources.getColor(R.color.red))
                        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(resources.getColor(R.color.blueInk))

                    }

                    dialog.show()
                }

            }

            true

        }

        val headerView = navView.getHeaderView(0)
        val navName = headerView.findViewById<android.view.View>(R.id.nav_name) as TextView
        val navPhone = headerView.findViewById<android.view.View>(R.id.nav_phone) as TextView
        image_avatar = headerView.findViewById<android.view.View>(R.id.nav_imageView) as ImageView

        navName.setText(Common.buildNavMessage())
        navPhone.setText(Common.currentUser!!.phone)

        if(Common.currentUser != null && Common.currentUser!!.image != null){

            Glide.with(this).load(Common.currentUser!!.image).into(image_avatar)

        }


    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (toggle.onOptionsItemSelected(item)){

            return true

        }



        return super.onOptionsItemSelected(item)
    }
}