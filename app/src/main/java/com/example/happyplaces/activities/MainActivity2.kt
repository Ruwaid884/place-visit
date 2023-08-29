package com.example.happyplaces.activities

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.happyplaces.R
import com.example.happyplaces.databinding.ActivityMain2Binding
import com.example.happyplaces.databinding.ActivityMainBinding
import com.example.happyplaces.models.HappyPlaceModel

class MainActivity2 : AppCompatActivity() {

    private var binding: ActivityMain2Binding?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMain2Binding.inflate(layoutInflater)
        setContentView(binding?.root)

        var happyPlaceDetails : HappyPlaceModel? = null
        if (intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            happyPlaceDetails = intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS)as HappyPlaceModel
        }
        if(happyPlaceDetails!=null){
            setSupportActionBar(binding?.toolbarHappyPlaceDetail)
            supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.title = happyPlaceDetails.title


            binding?.toolbarHappyPlaceDetail?.setNavigationOnClickListener{
                onBackPressed()
            }

            binding?.ivPlaceImage?.setImageURI(Uri.parse(happyPlaceDetails.image))
            binding?.tvDescription?.text = happyPlaceDetails.description
            binding?.tvLocation?.text = happyPlaceDetails.location
            binding?.button?.setOnClickListener{
                val intent = Intent(this,mapacticity::class.java)
                intent.putExtra(MainActivity.EXTRA_PLACE_DETAILS,happyPlaceDetails)
                startActivity(intent)
            }

        }
    }
}