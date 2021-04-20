package com.example.collect_my_car.Services

import android.util.Log
import com.example.collect_my_car.Model.Common
import com.example.collect_my_car.Model.EventBus.DriverRequestReceived
import com.example.collect_my_car.Utils.UserUtils
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.greenrobot.eventbus.EventBus
import kotlin.random.Random

class MyFirebaseMessagingService: FirebaseMessagingService() {


    override fun onNewToken(token: String) {
        super.onNewToken(token)

        if(FirebaseAuth.getInstance().currentUser != null){

            UserUtils.updateToken(this,token)

        }
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)

        val data = remoteMessage.data

        if(data != null){

            if(data[Common.NOTI_TITLE].equals(Common.REQUEST_DRIVER_TITLE)){

                val driverRequestReceived = DriverRequestReceived()

                driverRequestReceived.key = data[Common.RIDER_KEY]
                driverRequestReceived.pickupLocation = data[Common.PICKUP_LOCATION]
                driverRequestReceived.pickupLocationString = data[Common.PICKUP_LOCATION_STRING]
                driverRequestReceived.destinationLocation = data[Common.DESTINATION_LOCATION]
                driverRequestReceived.destinationLocationString = data[Common.DESTINATION_LOCATION_STRING]

                EventBus.getDefault().postSticky(driverRequestReceived)

            }
            else {

                Common.showNotification(this, Random.nextInt(),
                        data[Common.NOTI_TITLE],
                        data[Common.NOTI_BODY],
                        null)

                Log.d("TOKEN", "onMessageReceived " + Common.showNotification(this, Random.nextInt(),
                        data[Common.NOTI_TITLE],
                        data[Common.NOTI_BODY],
                        null).toString())

                Log.d("TOKEN", "onMessageReceived 2 " + data[Common.NOTI_TITLE] + " " + data[Common.NOTI_BODY])

            }

        }
    }

}