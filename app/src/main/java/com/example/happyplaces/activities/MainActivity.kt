package com.example.happyplaces.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.example.happyplaces.R
import com.example.happyplaces.adapters.HappyPlacesAdapter
import com.example.happyplaces.database.DatabaseHandler
import com.example.happyplaces.databinding.ActivityMainBinding
import com.example.happyplaces.models.HappyPlaceModel
import com.example.happyplaces.utils.SwipeToDeleteCallback
import com.example.happyplaces.utils.SwipeToEditCallback


class MainActivity : AppCompatActivity() {

    private var binding:ActivityMainBinding?=null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding?.root)

        binding?.placeAdder?.setOnClickListener{
       val intent= Intent(this, AddHappyPlaceActivity::class.java)
            startActivityForResult(intent,ADD_PLACE_ACTIVITY_REQUEST_CODE)
        }


        getHappyPlacesListFromLocalDB()
    }


    private fun SetupHappyPlacesRecyclerView(happyPlaceModel: ArrayList<HappyPlaceModel>){


        binding?.rvHappyPlacesList?.layoutManager = LinearLayoutManager(this)


        binding?.rvHappyPlacesList!!.setHasFixedSize(true)

        val placesAdapter = HappyPlacesAdapter(this,happyPlaceModel)
        binding?.rvHappyPlacesList!!.adapter = placesAdapter


        placesAdapter.setOnClickListner(object : HappyPlacesAdapter.OnClickListener{
            override fun onClick(position: Int, model: HappyPlaceModel) {
                val intent = Intent(this@MainActivity,
                    MainActivity2::class.java)

                intent.putExtra(EXTRA_PLACE_DETAILS,model)
                startActivity(intent)

            }
        })


        val editSwipeHandler = object : SwipeToEditCallback(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                val adapter = binding?.rvHappyPlacesList?.adapter as HappyPlacesAdapter
                adapter.notifyEditItem(this@MainActivity,viewHolder.adapterPosition,
                    ADD_PLACE_ACTIVITY_REQUEST_CODE)

            }
        }

        val editItemTouchHelper = ItemTouchHelper(editSwipeHandler)
        editItemTouchHelper.attachToRecyclerView(binding?.rvHappyPlacesList)


        val deleteSwipeHandler = object : SwipeToDeleteCallback(this){
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {

                val adapter = binding?.rvHappyPlacesList?.adapter as HappyPlacesAdapter
                adapter.removeAt(viewHolder.adapterPosition)

                getHappyPlacesListFromLocalDB()

            }
        }

        val DeleteItemTouchHelper = ItemTouchHelper(deleteSwipeHandler)
        DeleteItemTouchHelper.attachToRecyclerView(binding?.rvHappyPlacesList)

    }
    private fun getHappyPlacesListFromLocalDB(){
        val dbHandler = DatabaseHandler(this)
        val getHappyPlacesList : ArrayList<HappyPlaceModel> = dbHandler.getHappyPlacesList()

        if(getHappyPlacesList.size>0){
           binding?.rvHappyPlacesList?.visibility = View.VISIBLE
            binding?.tvNoRecordsAvailable?.visibility = View.GONE
            SetupHappyPlacesRecyclerView(getHappyPlacesList)
            }
        else{
            binding?.rvHappyPlacesList?.visibility = View.GONE
            binding?.tvNoRecordsAvailable?.visibility = View.VISIBLE
        }


    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == ADD_PLACE_ACTIVITY_REQUEST_CODE){
            if(resultCode == Activity.RESULT_OK)
                getHappyPlacesListFromLocalDB()
        }
        else{
            Log.e("Activity","Cancelled or Back pressed")
        }
    }
    companion object{
       var ADD_PLACE_ACTIVITY_REQUEST_CODE = 1

        var EXTRA_PLACE_DETAILS = "extra_place_details"
    }
}