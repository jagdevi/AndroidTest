package com.test.androidtest

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.support.v4.app.ActivityCompat
import android.support.v4.content.ContextCompat
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_camera.*
import java.io.File
import java.io.FileOutputStream

class CameraActivity : AppCompatActivity(), View.OnClickListener {

    private val imageCaptureResult = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_camera)

        checkPermissionGranted()
        btn_capture_image.setOnClickListener(this)
        btn_next.setOnClickListener(this)

    }

    private fun checkPermissionGranted() : Boolean{
        return if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE), 0)
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
        when(view?.id){
            R.id.btn_capture_image ->
                takeImageFromCamera()

            R.id.btn_next ->
                validateInputFields()
        }
    }

    private fun validateInputFields() {

    }
}
