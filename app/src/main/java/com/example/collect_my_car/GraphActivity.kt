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
import com.jjoe64.graphview.GraphView
import com.jjoe64.graphview.series.DataPoint
import com.jjoe64.graphview.series.LineGraphSeries
import kotlinx.android.synthetic.main.activity_graph.*


class GraphActivity : AppCompatActivity() {

    lateinit var toggle: ActionBarDrawerToggle

    private lateinit var image_avatar : ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_graph)

        toggle = ActionBarDrawerToggle(this, drawerLayoutGraph, R.string.open, R.string.closed)

        drawerLayoutGraph.addDrawerListener(toggle)

        toggle.syncState()

        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        navViewGraph.setNavigationItemSelectedListener {

            when(it.itemId){

                R.id.mItem1 -> {

                    intent = Intent(this, MapsActivity::class.java)

                    startActivity(intent)

                    Toast.makeText(applicationContext, "Maps", Toast.LENGTH_SHORT).show()
                }

                R.id.mItem2 -> {

                    intent = Intent(this, History::class.java)

                    startActivity(intent)

                    Toast.makeText(applicationContext, "History Activity", Toast.LENGTH_SHORT).show()
                }

                R.id.mItem3 -> {

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

        val headerView = navViewGraph.getHeaderView(0)
        val navName = headerView.findViewById<android.view.View>(R.id.nav_name) as TextView
        val navPhone = headerView.findViewById<android.view.View>(R.id.nav_phone) as TextView
        image_avatar = headerView.findViewById<android.view.View>(R.id.nav_imageView) as ImageView

        navName.setText("Welcome")//Common.buildNavMessage())
        navPhone.setText(Common.currentUser!!.phone)

        if(Common.currentUser != null && Common.currentUser!!.image != null){

            Glide.with(this).load(Common.currentUser!!.image).into(image_avatar)

        }

        val graph = findViewById<GraphView>(R.id.graph)
        //val graph = findViewById(R.id.graph) as GraphView
        //val series = LineGraphSeries<DataPoint>()

        val points = arrayOf( pointsValue(0,1),
            pointsValue(1,5),
            pointsValue(2,4),
            pointsValue(3,2),
            pointsValue(4,1),
             pointsValue(5,6),
             pointsValue(6,3)




            )
        val series = LineGraphSeries(points) //LineGraphSeries<DataPoint>(points)




        /*val series = LineGraphSeries(arrayOf(
            DataPoint(0, 1),
            DataPoint(1, 5),
            DataPoint(2, 3)*/
        //))
        graph.addSeries(series)

        graph.viewport.isXAxisBoundsManual = true
        graph.viewport.setMinX(1.0)
        graph.viewport.setMaxX(6.0)

        graph.viewport.isYAxisBoundsManual = true
        graph.viewport.setMinY(1.0)
        graph.viewport.setMaxY(10.0)

        graph.viewport.isScrollable = true
        graph.viewport.isScalable = true

        //series.setMode(series.Mode.CUBIC_BEZIER)
 /*
        val uploadLineDataSet = graph.viewport(series, "upload")
        uploadLineDataSet.mode = graph.Mode.CUBIC_BEZIER*/
    }

     fun pointsValue(a: Int, b: Int): DataPoint {
        return DataPoint(a.toDouble(), b.toDouble())
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (toggle.onOptionsItemSelected(item)){

            return true

        }



        return super.onOptionsItemSelected(item)
    }


}