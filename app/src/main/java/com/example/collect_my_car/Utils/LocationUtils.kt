package com.example.collect_my_car.Utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.text.TextUtils
import java.io.IOException
import java.util.*

object  LocationUtils {

    fun getAddressFromLocation(context: Context?, location: Location):String{

        var result = StringBuilder()
        val geoCoder = Geocoder(context, Locale.getDefault())
        val addressList: List<Address>?

        return  try{

            addressList = geoCoder.getFromLocation(location.latitude!!, location.longitude!!, 1)

            if (addressList != null && addressList.size > 0)

            {
                if (addressList[0].locality != null && !TextUtils.isEmpty((addressList[0].locality)))
                {
                    //If Address has a City Field

                    result.append(addressList[0].locality)
                }

                else if (addressList[0].subAdminArea != null && !TextUtils.isEmpty((addressList[0].subAdminArea)))
                {
                    //If Address Does Not have a City Field so we look for a subadminarea

                    result.append(addressList[0].subAdminArea)
                }

                else if (addressList[0].adminArea != null && !TextUtils.isEmpty((addressList[0].adminArea)))
                {
                    //If Address Does Not have subadminarea so we look for a adminarea

                    result.append(addressList[0].adminArea)
                }

                else
                {
                    //If Address Does Not have adminarea so we look for a Country

                    result.append(addressList[0].countryName)
                }

                //Final Result Will Apply Country Code
                result.append(addressList[0].countryCode)

            }
                result.toString()

        }catch (e:IOException){

            result.toString()

        }



    }


}