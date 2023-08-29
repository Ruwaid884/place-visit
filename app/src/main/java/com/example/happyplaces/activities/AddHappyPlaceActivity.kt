package com.example.happyplaces.activities


import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Context
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.icu.lang.UCharacter.GraphemeClusterBreak.T
import android.location.Address
import android.location.Location
import android.location.LocationManager
import android.net.Uri
import android.os.Build.VERSION_CODES.P
import android.os.Bundle
import android.os.Looper
import android.provider.MediaStore
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat.startActivityForResult

import com.example.happyplaces.R
import com.example.happyplaces.database.DatabaseHandler
import com.example.happyplaces.databinding.ActivityHappyPlacesBinding
import com.example.happyplaces.models.HappyPlaceModel
import com.example.happyplaces.utils.GetAdressFromLatLng
import com.google.android.gms.location.*
import com.google.android.libraries.places.api.Places
import com.google.android.libraries.places.api.model.AutocompletePrediction
import com.google.android.libraries.places.api.model.Place
import com.google.android.libraries.places.internal.zzdx
import com.google.android.libraries.places.widget.Autocomplete
import com.google.android.libraries.places.widget.AutocompleteActivity
import com.google.android.libraries.places.widget.model.AutocompleteActivityMode
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.*


class AddHappyPlaceActivity : AppCompatActivity(), View.OnClickListener {
    private var cal = Calendar.getInstance()
    private lateinit var dateSetListner:DatePickerDialog.OnDateSetListener
    private var saveImageToInternalStrorage : Uri? = null
    private var mLatitude :Double = 0.0
    private var mLongitude : Double = 0.0

    private var mHappyPlacesDetails:HappyPlaceModel? = null
    private lateinit var mFusedLocationProviderClient: FusedLocationProviderClient



    var binding:ActivityHappyPlacesBinding? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHappyPlacesBinding.inflate(layoutInflater)
        setContentView(binding?.root)










        setSupportActionBar(binding?.toolbarAddPlace)
        val actionbar = supportActionBar
        if(actionbar!=null){
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            actionbar.title = "Set place"

        }
        binding?.toolbarAddPlace?.setNavigationOnClickListener{
            onBackPressed()
        }

        mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)


        if(!Places.isInitialized()){
            Places.initialize(this@AddHappyPlaceActivity,R.string.YOUR_API_KEY.toString())

        }

        if(intent.hasExtra(MainActivity.EXTRA_PLACE_DETAILS)){
            mHappyPlacesDetails = intent.getSerializableExtra(MainActivity.EXTRA_PLACE_DETAILS)
            as HappyPlaceModel
        }

        dateSetListner= DatePickerDialog.OnDateSetListener{
            view, year, month, dayOfMonth ->
            cal.set(Calendar.YEAR,year)
            cal.set(Calendar.MONTH,month)
            cal.set(Calendar.DAY_OF_MONTH,dayOfMonth)

            updateDateInView()
        }
        updateDateInView()

        if(mHappyPlacesDetails!=null){
            supportActionBar?.title = "Edit Happy Place"
            binding?.title?.setText(mHappyPlacesDetails?.title)
            binding?.description?.setText(mHappyPlacesDetails?.description)
            binding?.date?.setText(mHappyPlacesDetails?.date)
            binding?.location?.setText(mHappyPlacesDetails?.location)
            mLatitude = mHappyPlacesDetails!!.latitude
            mLongitude = mHappyPlacesDetails!!.longitude

            saveImageToInternalStrorage = Uri.parse(
                mHappyPlacesDetails!!.image
            )

            binding?.image?.setImageURI(saveImageToInternalStrorage)

            binding?.save?.text = "UPDATE"

        }
        binding?.date?.setOnClickListener(this)

        binding?.addImage?.setOnClickListener(this)
        binding?.save?.setOnClickListener(this)
        binding?.location?.setOnClickListener(this)
        binding?.myLocation?.setOnClickListener(this)


    }
    private fun isLocationEnabled():Boolean{
        val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)||
        locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }
@SuppressLint("MissingPermission")
    private fun requestNewLocationData(){
        var locationRequest = LocationRequest()
        locationRequest.priority= LocationRequest.PRIORITY_HIGH_ACCURACY
        locationRequest.interval = 1000
        locationRequest.numUpdates =1
        mFusedLocationProviderClient.requestLocationUpdates(locationRequest,
            locationCallback,
            Looper.myLooper())
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationresult: LocationResult?) {
            val mlastLocation: Location = locationresult!!.lastLocation
            mLongitude = mlastLocation.longitude
            mLatitude = mlastLocation.latitude

            val addressTask = GetAdressFromLatLng(this@AddHappyPlaceActivity, mLatitude, mLongitude)

            addressTask.setAddressListner(object : GetAdressFromLatLng.AddressListner {
                override fun onAddressFound(address: String?) {

                    binding?.location?.setText(address)
                }

                override fun onError() {
                    Log.e("Get Address::", "Something went wrong")
                }
            })
            addressTask.getAddress()
        }
    }

    override fun onClick(v: View?) {

        when(v!!.id){
            R.id.date ->{
                DatePickerDialog(this@AddHappyPlaceActivity,dateSetListner,
                    cal.get(Calendar.YEAR),
                    cal.get(Calendar.MONTH),
                    cal.get(Calendar.DAY_OF_MONTH)).show()
            }

            R.id.add_image ->{
                val pictureDialog = AlertDialog.Builder(this)
                pictureDialog.setTitle("Select Action")
                val pictureDialogItems = arrayOf("Select photo from Gallery",
                "Capture photo from camera")

                pictureDialog.setItems(pictureDialogItems){
                    dialog,which->
                    when(which){
                        0-> choosePhotoFromGallery()
                        1-> takePhotoFromCamera()
                    }
                }
                pictureDialog.show()
            }

            R.id.save -> {
                when{
                    binding?.title?.text.isNullOrEmpty()->{
                        Toast.makeText(this,"Please enter title",Toast.LENGTH_SHORT).show()
                    }
                    binding?.description?.text.isNullOrEmpty()->{
                        Toast.makeText(this,"Please enter Description",Toast.LENGTH_SHORT).show()
                    }
                    binding?.location?.text.isNullOrEmpty()->{
                        Toast.makeText(this,"Please enter location",Toast.LENGTH_SHORT).show()
                    }
                    saveImageToInternalStrorage == null ->{
                        Toast.makeText(this,"Please select an image",Toast.LENGTH_SHORT).show()
                    } else ->{
                        val happyPlaceModel= HappyPlaceModel(
                            if(mHappyPlacesDetails == null) 0 else mHappyPlacesDetails!!.id,
                            binding?.title?.text.toString(),
                            saveImageToInternalStrorage.toString(),
                            binding?.description?.text.toString(),
                            binding?.date?.text.toString(),
                            binding?.location?.text.toString(),
                            mLatitude,
                            mLongitude
                        )
                    val dbHandler = DatabaseHandler(this)
                    if(mHappyPlacesDetails == null){
                        val addHappyPlace = dbHandler.addHappyPlace(happyPlaceModel)
                        if(addHappyPlace>0){
                            setResult(Activity.RESULT_OK)
                            finish()
                    }
                    }
                        else{
                            val updateHappyPlace = dbHandler.updateHappyPlace(happyPlaceModel)
                            if(updateHappyPlace>0){
                                setResult(Activity.RESULT_OK)
                                finish()
                            }


                        }


                    }
                }

            }

            R.id.location ->{
                try {

                    val fields = listOf(
                        Place.Field.ID,Place.Field.NAME,Place.Field.LAT_LNG,
                        Place.Field.ADDRESS
                    )

                    val intent =
                        Autocomplete.IntentBuilder(AutocompleteActivityMode.FULLSCREEN,fields)
                            .build(this@AddHappyPlaceActivity)

                   startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE)
                }
                catch (e:Exception){
                    e.printStackTrace()
                }

            }
            R.id.my_location->{
                if(!isLocationEnabled()){
                    Toast.makeText(this,"Your location provider is turned off.Please turn it on",Toast.LENGTH_SHORT
                    ).show()
                    val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    startActivity(intent)
                } else{
                    Dexter.withActivity(this).withPermissions(
                        android.Manifest.permission.ACCESS_FINE_LOCATION,
                        android.Manifest.permission.ACCESS_COARSE_LOCATION
                    ).withListener(object : MultiplePermissionsListener {
                        override fun onPermissionsChecked(report: MultiplePermissionsReport?) {
                          if(report!!.areAllPermissionsGranted()){
                             requestNewLocationData()
                          }
                        }

                        override fun onPermissionRationaleShouldBeShown(
                            p0: MutableList<PermissionRequest>?,
                            p1: PermissionToken?
                        ) {
                            showRationalDialogForPermissions()
                        }
                        
                    }).onSameThread()
                        .check()

                }
            }

        }
    }


    public override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(resultCode== Activity.RESULT_OK){
            if(requestCode == GALLERY){
                if(data!=null){
                    val contentURI = data.data
                    try{
                        val selectedImageBitmap = MediaStore.Images.Media.getBitmap(this.contentResolver,contentURI)

                        saveImageToInternalStrorage= saveImagetoInternalStorage(selectedImageBitmap)

                        Log.e("Saved image","Path :: $saveImageToInternalStrorage")
                        binding?.image?.setImageBitmap(selectedImageBitmap)
                    }catch (e:IOException){
                        e.printStackTrace()
                        Toast.makeText(this@AddHappyPlaceActivity,
                            "Failed to load the image from Gallery!",
                            Toast.LENGTH_SHORT).show()
                    }

                }
            }else if(requestCode == CAMERA){
                val thumbnail: Bitmap = data!!.extras!!.get("data") as Bitmap

                saveImageToInternalStrorage= saveImagetoInternalStorage(thumbnail)

                Log.e("Saved image","Path :: $saveImageToInternalStrorage")
                binding?.image?.setImageBitmap(thumbnail)
        }
            else if(requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE){
                val place: Place = Autocomplete.getPlaceFromIntent(data!!)
                binding?.location?.setText(place.address)
                mLatitude =place.latLng!!.latitude
                mLongitude = place.latLng!!.longitude
            }
        }
    }

    private fun takePhotoFromCamera(){
        Dexter.withContext(this)
            .withPermissions(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
                android.Manifest.permission.CAMERA
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if(report.areAllPermissionsGranted()){
                        val galleryIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                        startActivityForResult(galleryIntent, CAMERA)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken?
                ) {
                    showRationalDialogForPermissions()
                }
            }).onSameThread().check()

    }

    private fun choosePhotoFromGallery() {
        Dexter.withContext(this)
            .withPermissions(
                android.Manifest.permission.READ_EXTERNAL_STORAGE,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if(report.areAllPermissionsGranted()){
                        val galleryIntent = Intent(Intent.ACTION_PICK,
                            MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                        startActivityForResult(galleryIntent, GALLERY)
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest?>?,
                    token: PermissionToken?
                ) {
                    showRationalDialogForPermissions()
                }
            }).onSameThread().check()

    }
    private fun showRationalDialogForPermissions(){
        AlertDialog.Builder(this).setMessage("it looks you have turned off permission required" +
                "It can be enabled under Application Settings"
        ).setPositiveButton("Go to Settings")
        {_,_ ->
            try{
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                val uri = Uri.fromParts("package",packageName,null)
                intent.data = uri
                startActivity(intent)
            } catch (e:ActivityNotFoundException){
                e.printStackTrace()
            }
        }.setNegativeButton("Cancel"){dialog,_->
            dialog.dismiss()
        }.show()

    }

    private fun updateDateInView(){
        val myFormat = "dd.MM.yyyy"
        val sdf = SimpleDateFormat(myFormat, Locale.getDefault())
        binding?.date?.setText(sdf.format(cal.time).toString())
    }

    private fun saveImagetoInternalStorage(bitmap: Bitmap):Uri{
        val wrapper = ContextWrapper(applicationContext)
        var file = wrapper.getDir(IMAGE_DIRECTORY, Context.MODE_PRIVATE)
        file = File(file,"${UUID.randomUUID()}.jpg")

        try{
            val stream:OutputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG,100,stream)
            stream.flush()
            stream.close()
        }catch (e:IOException){
            e.printStackTrace()
        }
        return Uri.parse(file.absolutePath)
    }
    companion object{
        private const val GALLERY=1
        private const val CAMERA =2
        private const val IMAGE_DIRECTORY = "HappyPlacesImages"
        private const val PLACE_AUTOCOMPLETE_REQUEST_CODE = 3
    }
}