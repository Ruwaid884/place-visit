package com.example.happyplaces.utils

import android.content.Context
import android.location.Address
import android.location.Geocoder
import android.os.AsyncTask
import java.util.*

class GetAdressFromLatLng(context:Context,private val lat:Double,
                          private val long:Double )
    :AsyncTask<Void, String,String>() {

    private val geocoder:Geocoder = Geocoder(context, Locale.getDefault())
    private lateinit var mAddressListner: AddressListner

    override fun doInBackground(vararg params: Void?): String {
       try {
           val addressList:List<Address>? = geocoder.getFromLocation(lat,long,1)

           if(addressList!=null && addressList.isNotEmpty()){
               val address: Address = addressList[0]
               val sb = StringBuilder()
               for(i in 0..address.maxAddressLineIndex){

                   sb.append(address.getAddressLine(i)).append(" ")
               }
               sb.deleteCharAt(sb.length-1)
               return sb.toString()
           }
       }catch (e:Exception){
           e.printStackTrace()
       }
        return ""
    }

    override fun onPostExecute(resultString: String?) {
        if(resultString == null){
            mAddressListner.onError()
        }else{
            mAddressListner.onAddressFound(resultString)
        }
        super.onPostExecute(resultString)
    }

    fun setAddressListner(addressListner:AddressListner){
        mAddressListner = addressListner
    }

    fun getAddress(){
        execute()
    }

    interface AddressListner{
        fun onAddressFound(address:String?)
        fun onError()
    }

}