package com.example.collect_my_car

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.collect_my_car.Adapter.HistoryAdapter
import com.example.collect_my_car.Model.Common
import com.example.collect_my_car.Model.TripPlanModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.activity_recyclerview.*
import java.util.*

class History : AppCompatActivity(), HistoryAdapter.OnItemClickListener {

    private var mAuth: FirebaseAuth? = null
    private var mUser: FirebaseUser? = null
    var db: DatabaseReference? = null

    //Variables

    private lateinit var database: FirebaseDatabase
    private lateinit var userInfoRef: DatabaseReference

    val ITEM_COUNT = 21
    var total_item = 0
    var last_visable_item = 0

    //lateinit var adapter: DriverAdapter

    var isLoading = false
    var isMaxData = false

    var last_node: String? = ""
    var last_key: String? = ""

    companion object{

        val MESSGAE = "Message"
    }

    private lateinit var posts: MutableList<TripPlanModel>
    lateinit var testList: List<TripPlanModel>
    lateinit var arrayTest: ArrayList<TripPlanModel>

    lateinit var adapter: HistoryAdapter


    lateinit var toggle: ActionBarDrawerToggle

    private lateinit var image_avatar: ImageView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recyclerview)

        posts = mutableListOf()

        adapter = HistoryAdapter(this, posts, this)

        recycler_view.adapter = adapter

        recycler_view.layoutManager = LinearLayoutManager(this)

        mAuth = FirebaseAuth.getInstance()

        mUser = mAuth!!.currentUser
        val userID = mUser!!.uid


        database = FirebaseDatabase.getInstance()
        //val userInfoRef = database.getReference(Common.USER_INFO_REFERENCE).child(userID).child("Collections")//.child("Test")//.child("MYzpNmIdZjgREGhR9_k")//.child("MYzomuuS0csr4LGlhYu")//.child(userID).child("Collections").child("4464204406145883896")//.orderByKey()//child("2185699360067625422")//.child(userID).child("Collections").child("4464204406145883896")

        val userInfoRef = database.getReference(Common.TRIP)

        userInfoRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                posts.clear()

                for (ds in snapshot.children) {

                    val id = ds.key

                    //val text = ds.child("user:").getValue(TripPlanModel::class.java)
//                    val time = ds.child("driver").getValue(UserModel::class.java)
                    Log.d("Test ", "Key " + id )//+ " Test " + time)
                    //Log.d("Test ", "User " + text )

                    val userInfoRef3 = database.getReference(Common.TRIP).child(id!!).child("driver")//orderByChild("user").equalTo(userID)

                    userInfoRef3.addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(snapshot2: DataSnapshot) {

                            //for (ds2 in snapshot.children) {

                            //val id2 = snapshot2.getValue(TripPlanModel::class.java)
                            //val text = ds.child("user:").getValue(TripPlanModel::class.java)

                            Log.d("Test ", "Find 2.0  " + snapshot2)

                            if(snapshot2.value?.equals(userID) == true) {

                                Log.d("Test ", "FOUND  " + snapshot2) //Displays This: DataSnapshot { key = user, value = QdCe2CjMTvbWCGHvAoHXGQehGL33 }

                                Log.d("Test ", "FOUND KEY  " + snapshot2.key)
                            }


                        }

                        override fun onCancelled(error: DatabaseError) {


                        }
                    })
                    val userInfoRef4 = database.getReference(Common.TRIP).child(id!!)//.child("user")//orderByChild("user").equalTo(userID)

                    userInfoRef4.addListenerForSingleValueEvent(object : ValueEventListener{
                        override fun onDataChange(snapshot3: DataSnapshot) {

                            //for (ds2 in snapshot.children) {

                            val search = snapshot3.child("driver").value

                            val search2 = snapshot3.key


                            if (search != null) {

                                if (search == userID){

                                    Log.d("Test ", "SUCCESSFUL " + search)
                                    Log.d("Test ", "SUCCESSFUL KEY " + search2)

                                    //val testMap = HashMap<String, TripPlanModel>()


                                    val postList = snapshot3.getValue(TripPlanModel::class.java)/* ?: return

                                    testMap[snapshot3.key!!] = postList*/

                                    //Log.d("Test", "TESTMAP $testMap")

                                    Log.d("Test", "SnapShot $postList") //This Works to get User / destinationString


                                    if (postList != null) {

                                        posts.add(postList)
                                    }

                                    adapter.notifyDataSetChanged()

                                }
                            }
                            //for (ds2 in snapshot.children) {

                            //val id2 = snapshot2.getValue(TripPlanModel::class.java)
                            //val text = ds.child("user:").getValue(TripPlanModel::class.java)

                            Log.d("Test ", "FIND 2.0000  " + snapshot3)

                            if(snapshot3.value?.equals(userID) == true) {

                                Log.d("Test ", "FOUND  2 " + snapshot3) //Displays This: DataSnapshot { key = user, value = QdCe2CjMTvbWCGHvAoHXGQehGL33 }

                                Log.d("Test ", "FOUND KEY  2 " + snapshot3.key)

                                Log.d("Test ", "FOUND KEY  2 " + snapshot3.value)



                            }

                            //}

                        }

                        override fun onCancelled(error: DatabaseError) {


                        }
                    })



                    /* //val userInfoRef2 = database.getReference(Common.USER_INFO_REFERENCE).child(userID).child("Collections").child(id!!)//.child("driverInfoModel")

                     val userInfoRef2 = database.getReference(Common.TRIP).child(id!!)//.orderByChild("user").equalTo(userID)//.child("driverInfoModel")

                     //val username = ds.child("user").getValue(TripPlanModel::class.java)
                     //Log.d("Test ", "Find  " + userInfoRef2 )

                     //Log.d("Test", "Username " + username)

                     userInfoRef2.addListenerForSingleValueEvent(object : ValueEventListener{

                         override fun onDataChange(snapshot: DataSnapshot) {


                             // val username = snapshot.child("user").getValue(TripPlanModel::class.java)

                             val postList = snapshot.getValue(TripPlanModel::class.java)

                             //Log.d("Test", "SnapShot $postList") //This Works to get User / destinationString


                             if (postList != null) {

                                 posts.add(postList)

                             }

                             adapter.notifyDataSetChanged()
                             //}
                         }


                         override fun onCancelled(error: DatabaseError) {

                         }


                     })*/




                    //     adapter.notifyDataSetChanged()









                }


            }

            override fun onCancelled(error: DatabaseError) {

            }




        })



    }

    override fun onResume() {
        super.onResume()

        //posts.clear()

        adapter.notifyDataSetChanged()
    }

    override fun onItemClick(position: Int) {

        Toast.makeText(this, "Item $position clicked", Toast.LENGTH_SHORT).show()
        val clickedItem = posts[position]



        var collectionNumber = posts.elementAt(position).collectionNumber



        //Log.d("ViewCollection", "LIST POSITION " + posts.indexOf(clickedItem.user))

        Log.d("ViewCollection", "ELEMENT AT  POSITION " + posts.elementAt(position))

        Log.d("ViewCollection", "ELEMENT AT  POSITION USER " + posts.elementAt(position).user)

        Log.d("ViewCollection", "ELEMENT AT  POSITION USER " + posts.elementAt(position).collectionNumber)


        val intent = Intent(this, UserHistory::class.java)

        intent.putExtra(MESSGAE,collectionNumber)

        startActivity(intent)


    }

    override fun onBackPressed() {

        //posts.clear()

        finish()

    }
}