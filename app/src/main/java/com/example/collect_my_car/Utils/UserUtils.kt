package com.example.collect_my_car.Utils

import android.content.Context
import android.view.View
import android.widget.FrameLayout
import android.widget.Toast
import com.example.collect_my_car.Model.Common
import com.example.collect_my_car.Model.EventBus.NotifyUserEvent
import com.example.collect_my_car.Model.FCMSendData
import com.example.collect_my_car.Model.TokenModel
import com.example.collect_my_car.R
import com.example.collect_my_car.Remote.IFCMService
import com.example.collect_my_car.Remote.RetroFitFCMClient
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus

object UserUtils {

    fun updateToken(context: Context, token: String) {

        val tokenModel = TokenModel()

        tokenModel.token = token;

        FirebaseDatabase.getInstance().getReference(Common.TOKEN_REFERENCE)
            .child(FirebaseAuth.getInstance().currentUser!!.uid)
            .setValue(tokenModel)
            .addOnFailureListener { e->

                Toast.makeText(context, e.message, Toast.LENGTH_LONG ).show()

            }

            .addOnSuccessListener {  }

    }

    fun sendDeclineRequest(context: Context, rootLayout: FrameLayout?, key: String) {

        val compositeDisposable = CompositeDisposable()

        val ifcmService = RetroFitFCMClient.instance!!.create(IFCMService::class.java)

        //Get Token

        FirebaseDatabase.getInstance()
            .getReference(Common.TOKEN_REFERENCE)
            .child(key)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.exists()){

                        val tokenModel = snapshot.getValue(TokenModel::class.java)

                        val notificationData : MutableMap<String, String> = HashMap()

                        notificationData.put(Common.NOTI_TITLE, Common.REQUEST_DRIVER_DECLINE)
                        notificationData.put(Common.NOTI_BODY, "Driver has Declined Collection")
                        notificationData.put(Common.DRIVER_KEY, FirebaseAuth.getInstance().currentUser!!.uid)


                        val fcmSendData = FCMSendData(tokenModel!!.token, notificationData)

                        compositeDisposable.add(ifcmService.sendNotification(fcmSendData)!!
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ fcmResponse ->

                                if(fcmResponse!!.success ==0){

                                    compositeDisposable.clear()

                                   Toast.makeText(context, context.getString(R.string.decline_failed), Toast.LENGTH_LONG ).show()

                                }

                                else{

                                    Toast.makeText(context, context.getString(R.string.decline_successful), Toast.LENGTH_LONG ).show()
                                }

                            },{t: Throwable? ->

                                compositeDisposable.clear()

                                Toast.makeText(context, t!!.message, Toast.LENGTH_LONG ).show()
                            }))
                    }
                    else{

                        compositeDisposable.clear()
                        Toast.makeText(context, context.getString(R.string.token_not_found), Toast.LENGTH_LONG ).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                    Toast.makeText(context, error.message, Toast.LENGTH_LONG ).show()
                }
            })
    }

    fun sendAcceptRequestToDriver(context: Context,  rootLayout: FrameLayout?, key: String, tripNumberId: String?) {

        val compositeDisposable = CompositeDisposable()

        val ifcmService = RetroFitFCMClient.instance!!.create(IFCMService::class.java)

        //Get Token

        FirebaseDatabase.getInstance()
            .getReference(Common.TOKEN_REFERENCE)
            .child(key)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.exists()){

                        val tokenModel = snapshot.getValue(TokenModel::class.java)

                        val notificationData : MutableMap<String, String> = HashMap()

                        notificationData.put(Common.NOTI_TITLE, Common.REQUEST_DRIVER_ACCEPT)
                        notificationData.put(Common.NOTI_BODY, "Driver has Accepted Collection")
                        notificationData.put(Common.DRIVER_KEY, FirebaseAuth.getInstance().currentUser!!.uid)
                        notificationData.put(Common.TRIP_KEY,tripNumberId!!)


                        val fcmSendData = FCMSendData(tokenModel!!.token, notificationData)

                        compositeDisposable.add(ifcmService.sendNotification(fcmSendData)!!
                            .subscribeOn(Schedulers.newThread())
                            .observeOn(AndroidSchedulers.mainThread())
                            .subscribe({ fcmResponse ->

                                if(fcmResponse!!.success ==0){

                                    compositeDisposable.clear()

                                    Toast.makeText(context, context.getString(R.string.accept_failed), Toast.LENGTH_LONG ).show()
                                }

                            },{t: Throwable? ->

                                compositeDisposable.clear()

                                Toast.makeText(context, t!!.message, Toast.LENGTH_LONG ).show()
                            }))
                    }

                    else{

                        compositeDisposable.clear()
                        Toast.makeText(context, context.getString(R.string.token_not_found), Toast.LENGTH_LONG ).show()
                    }
                }

                override fun onCancelled(error: DatabaseError) {

                    Toast.makeText(context, error.message, Toast.LENGTH_LONG ).show()
                }
            })
    }

    fun sendNotifyToUser(context: Context, view: android.view.View, key: String?) {

        val compositeDisposable = CompositeDisposable()

        val ifcmService = RetroFitFCMClient.instance!!.create(IFCMService::class.java)

        //Get Token

        FirebaseDatabase.getInstance()
                .getReference(Common.TOKEN_REFERENCE)
                .child(key!!)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {

                        if (snapshot.exists()){

                            val tokenModel = snapshot.getValue(TokenModel::class.java)

                            val notificationData : MutableMap<String, String> = HashMap()

                            notificationData.put(Common.NOTI_TITLE, context.getString(R.string.driver_arrived))
                            notificationData.put(Common.NOTI_BODY,  context.getString(R.string.your_driver_arrived))
                            notificationData.put(Common.DRIVER_KEY, FirebaseAuth.getInstance().currentUser!!.uid)
                            notificationData.put(Common.RIDER_KEY, key)

                            val fcmSendData = FCMSendData(tokenModel!!.token, notificationData)

                            compositeDisposable.add(ifcmService.sendNotification(fcmSendData)!!
                                    .subscribeOn(Schedulers.newThread())
                                    .observeOn(AndroidSchedulers.mainThread())
                                    .subscribe({ fcmResponse ->

                                        if(fcmResponse!!.success ==0){

                                            compositeDisposable.clear()

                                            Toast.makeText(context, context.getString(R.string.accept_failed), Toast.LENGTH_LONG ).show()

                                        }

                                        else{

                                            EventBus.getDefault().postSticky(NotifyUserEvent())

                                        }


                                    },{t: Throwable? ->

                                        compositeDisposable.clear()

                                        Toast.makeText(context, t!!.message, Toast.LENGTH_LONG ).show()



                                    }))
                        }

                        else{

                            compositeDisposable.clear()
                            Toast.makeText(context, context.getString(R.string.token_not_found), Toast.LENGTH_LONG ).show()

                        }

                    }

                    override fun onCancelled(error: DatabaseError) {

                        Toast.makeText(context, error.message, Toast.LENGTH_LONG ).show()

                    }
                })
    }

    fun sendDeclineRemoveTripRequest(context: Context, rootLayout: FrameLayout?, key: String, tripNumberId: String?) {

        val compositeDisposable = CompositeDisposable()

        val ifcmService = RetroFitFCMClient.instance!!.create(IFCMService::class.java)

        //Remove Trip ID from Firebase

        FirebaseDatabase.getInstance().getReference(Common.TRIP)
                .child(tripNumberId!!)
                .removeValue()
                .addOnFailureListener { e ->

                    Toast.makeText(context, e.message, Toast.LENGTH_LONG ).show()

                }.addOnSuccessListener {

                    //After Removing Successfully, we will notify the User

                    //Get Token

                    FirebaseDatabase.getInstance()
                            .getReference(Common.TOKEN_REFERENCE)
                            .child(key)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {

                                    if (snapshot.exists()){

                                        val tokenModel = snapshot.getValue(TokenModel::class.java)

                                        val notificationData : MutableMap<String, String> = HashMap()

                                        notificationData.put(Common.NOTI_TITLE, Common.REQUEST_DRIVER_DECLINE_AND_REMOVE_TRIP)
                                        notificationData.put(Common.NOTI_BODY, "Driver has Cancelled Collection")
                                        notificationData.put(Common.DRIVER_KEY, FirebaseAuth.getInstance().currentUser!!.uid)


                                        val fcmSendData = FCMSendData(tokenModel!!.token, notificationData)

                                        compositeDisposable.add(ifcmService.sendNotification(fcmSendData)!!
                                                .subscribeOn(Schedulers.newThread())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe({ fcmResponse ->

                                                    if(fcmResponse!!.success ==0){

                                                        compositeDisposable.clear()

                                                        Toast.makeText(context, context.getString(R.string.decline_failed), Toast.LENGTH_LONG ).show()

                                                    }

                                                    else{


                                                        Toast.makeText(context, context.getString(R.string.decline_successful), Toast.LENGTH_LONG ).show()

                                                    }




                                                },{t: Throwable? ->

                                                    compositeDisposable.clear()

                                                    Toast.makeText(context, t!!.message, Toast.LENGTH_LONG ).show()



                                                }))

                                    }

                                    else{

                                        compositeDisposable.clear()
                                        Toast.makeText(context, context.getString(R.string.token_not_found), Toast.LENGTH_LONG ).show()

                                    }

                                }

                                override fun onCancelled(error: DatabaseError) {

                                    Toast.makeText(context, error.message, Toast.LENGTH_LONG ).show()
                                }
                            })
                }
    }

    fun sendCompleteTripToUser(view: View,context: Context, key: String?,  tripNumberId: String?) {

        val compositeDisposable = CompositeDisposable()

        val ifcmService = RetroFitFCMClient.instance!!.create(IFCMService::class.java)

        //Remove Trip ID from Firebase

        //After Removing Successfully, we will notify the User

                    //Get Token

                    FirebaseDatabase.getInstance()
                            .getReference(Common.TOKEN_REFERENCE)
                            .child(key!!)
                            .addListenerForSingleValueEvent(object : ValueEventListener {
                                override fun onDataChange(snapshot: DataSnapshot) {

                                    if (snapshot.exists()){

                                        val tokenModel = snapshot.getValue(TokenModel::class.java)

                                        val notificationData : MutableMap<String, String> = HashMap()

                                        notificationData.put(Common.NOTI_TITLE, Common.USER_REQUEST_COMPLETE_TRIP)
                                        notificationData.put(Common.NOTI_BODY, "Drop-off Complete")
                                        notificationData.put(Common.TRIP_KEY, tripNumberId!!)


                                        val fcmSendData = FCMSendData(tokenModel!!.token, notificationData)

                                        compositeDisposable.add(ifcmService.sendNotification(fcmSendData)!!
                                                .subscribeOn(Schedulers.newThread())
                                                .observeOn(AndroidSchedulers.mainThread())
                                                .subscribe({ fcmResponse ->

                                                    if(fcmResponse!!.success ==0){

                                                        compositeDisposable.clear()

                                                        Toast.makeText(context, context.getString(R.string.complete_journey_failed), Toast.LENGTH_LONG ).show()

                                                    }

                                                    else{


                                                        Toast.makeText(context, context.getString(R.string.complete_journey_successful), Toast.LENGTH_LONG ).show()

                                                    }




                                                },{t: Throwable? ->

                                                    compositeDisposable.clear()

                                                    Toast.makeText(context, t!!.message, Toast.LENGTH_LONG ).show()



                                                }))

                                    }

                                    else{

                                        compositeDisposable.clear()
                                        Toast.makeText(context, context.getString(R.string.token_not_found), Toast.LENGTH_LONG ).show()

                                    }

                                }

                                override fun onCancelled(error: DatabaseError) {

                                    Toast.makeText(context, error.message, Toast.LENGTH_LONG ).show()
                                }
                            })
    }
}