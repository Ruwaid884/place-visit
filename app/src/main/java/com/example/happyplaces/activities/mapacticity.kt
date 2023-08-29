package com.example.happyplaces.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.happyplaces.R
import com.example.happyplaces.databinding.ActivityMain2Binding
import com.example.happyplaces.databinding.ActivityMapacticityBinding
import com.example.happyplaces.models.HappyPlaceModel
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class mapacticity : AppCompatActivity(),OnMapReadyCallback{
    private var binding:ActivityMapacticityBinding? = null
    private var mHappyPlaceDetail: HappyPlaceModel? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapacticityBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            mHappyPlaceDetail  = intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS)
                    as HappyPlaceModel


        }
        if(mHappyPlaceDetail!=null){
            setSupportActionBar(binding?.toolbarMap)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar!!.title = mHappyPlaceDetail!!.title


            binding?.toolbarMap?.setNavigationOnClickListener{
                onBackPressed()
            }

            val supportMapFragment: SupportMapFragment =
                supportFragmentManager.findFragmentById(R.id.map) as
                        SupportMapFragment
            supportMapFragment.getMapAsync(this)

        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        val position = LatLng(mHappyPlaceDetail!!.latitude,mHappyPlaceDetail!!.longitude)
        googleMap!!.addMarker(MarkerOptions().position(position).title(mHappyPlaceDetail!!.location))
        val newLatlngZoom = CameraUpdateFactory.newLatLngZoom(position, 15f)
        googleMap.animateCamera(newLatlngZoom)
    }
}