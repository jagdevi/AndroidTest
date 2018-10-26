package com.test.androidtest

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import com.mapbox.android.core.location.LocationEnginePriority
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.android.core.permissions.PermissionsManager
import com.mapbox.mapboxsdk.Mapbox
import kotlinx.android.synthetic.main.activity_camera.*
import java.io.File
import java.io.FileOutputStream
import java.text.DecimalFormat

class CameraActivity : AppCompatActivity(), View.OnClickListener {

    //MapBox token
    private val mapAccessToken = "pk.eyJ1IjoiamFnZGV2aSIsImEiOiJjam5wa2RxZWYxdGRkM2twa285dDU3MmN2In0.Knj8iSlFMJdoTDXZc8dc-g"
    private val PERMISSION_REQUEST_CODE = 101
    private val imageCaptureResult = 1
    private lateinit var progressDialog: AlertDialog

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        checkPermissionGranted()
        progressDialog = showCustomProgressDialog(this)
        btn_capture_image.setOnClickListener(this)
        btn_next.setOnClickListener(this)
        editText_geoTag.setOnClickListener(this)
    }

    private fun checkPermissionGranted() : Boolean{

        val cameraPermission = ContextCompat.checkSelfPermission(this,
                Manifest.permission.CAMERA)
        val storageWritePermission = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        val locationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
        val fineLocationPermission = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)

        val permissionList = ArrayList<String>()
        if(cameraPermission != PackageManager.PERMISSION_GRANTED)
            permissionList.add(Manifest.permission.CAMERA)

        if(storageWritePermission != PackageManager.PERMISSION_GRANTED)
            permissionList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        if(fineLocationPermission != PackageManager.PERMISSION_GRANTED)
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION)

        if(locationPermission != PackageManager.PERMISSION_GRANTED)
            permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION)

        return if(permissionList.isNotEmpty() && permissionList.size > 0){
            ActivityCompat.requestPermissions(this,permissionList.toTypedArray(), PERMISSION_REQUEST_CODE)
            false
        }else
            true

    }

    /**
     * function to take picture from camera via Intent
     */
    private fun takeImageFromCamera() {
        if (checkPermissionGranted()) {
            startActivityForResult(Intent(MediaStore.ACTION_IMAGE_CAPTURE), imageCaptureResult)
        }else
            checkPermissionGranted()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == imageCaptureResult){
            if (data != null) {
                val bitmap = data.extras?.get("data") as Bitmap
                iv_show_image.setImageBitmap(bitmap)
                saveImage(bitmap)
            }
        }
    }

    /**
     * function to save captured image to memory card
     * @param bitmap as Image bitmap
     */
    private fun saveImage(bitmap: Bitmap) {
        val rootDir = Environment.getExternalStorageDirectory().toString()
        val imageDir = File(rootDir+"/capturedImage/")
        if(!imageDir.exists())
            imageDir.mkdirs()

        val saveFile = File(imageDir,"image.jpg")
        if(saveFile.exists())
            saveFile.delete()
        try {
            val outStream = FileOutputStream(saveFile)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outStream)
            Log.d("ImageCapture","ImageCaptureSuccess$saveFile")
            outStream.flush()
            outStream.close()
        }catch (e: Exception)
        {
            Log.d("ImageCapture","ImageCaptureFail")
            e.printStackTrace()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == 0) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED
                    && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                takeImageFromCamera()
            }
        }

    }

    override fun onClick(view: View?) {
        when(view?.id) {
            R.id.btn_capture_image ->
                takeImageFromCamera()

            R.id.btn_next -> {
                if (validateInputFields())
                    Log.d("dff","sdf")
                }

            R.id.editText_geoTag ->
                initializeMapBox()
        }
    }

    /**
     * function to take users current latitude and longitude
     */
    private fun getCurrentLatLong() {
        val locationEngine = LocationEngineProvider(this).obtainBestLocationEngineAvailable()
        locationEngine.priority = LocationEnginePriority.HIGH_ACCURACY
        locationEngine.activate()
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            val location = locationEngine.lastLocation
            val latitude = DecimalFormat("##.####").format(location.latitude)
            val longitude = DecimalFormat("##.####").format(location.longitude)
            editText_geoTag.setText("$latitude , $longitude")
            dismissProgressDialog()
        }
    }

    private fun initializeMapBox(){
        progressDialog?.show()
        Mapbox.getInstance(this,mapAccessToken)
        if(!checkPermissionGranted())
            return

        if(PermissionsManager.areLocationPermissionsGranted(this)){
            getCurrentLatLong()
        }else
            checkPermissionGranted()

    }

    private fun validateInputFields() : Boolean{
        val objectId = editText_objectId.text?.trim().toString()
        val latLong = editText_geoTag.text?.trim().toString()
        val featuredCount = editText_featureCount.text?.trim().toString()
        return when {
            objectId.isEmpty() -> {
                textInput_ObjectId.error = "Enter object id"
                false
            }
            latLong.isEmpty() -> {
                textInput_ObjectId.isErrorEnabled = false
                textInput_geoTag.error = "Enter geo tag"
                false
            }
            featuredCount.isEmpty() -> {
                textInput_geoTag.isErrorEnabled = false
                textInput_featureCount.error = "Enter feature count"
                false
            }
            else -> {
                textInput_ObjectId.isErrorEnabled = false
                textInput_geoTag.isErrorEnabled = false
                textInput_featureCount.isErrorEnabled = false
                true
            }
        }
    }

    /**
     * function to show custom progress dialog
     */
    private fun showCustomProgressDialog(context: Context) : AlertDialog {
        val alertBuilder = AlertDialog.Builder(context)
        val dialog = LayoutInflater.from(context).inflate(R.layout.custom_progressbar,null)
        val alertDialog = alertBuilder.create()
        alertDialog.window.setBackgroundDrawableResource(android.R.color.transparent)
        alertDialog.setView(dialog)
        alertDialog.setCanceledOnTouchOutside(false)

        return alertDialog
    }

    /**
     * function to dismiss custom progress dialog
     */
    private fun dismissProgressDialog() {
        if(progressDialog.isShowing)
            progressDialog.dismiss()
    }
}
