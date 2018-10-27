package com.test.androidtest

import android.Manifest
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Color
import android.os.Bundle
import android.support.v4.app.ActivityCompat
import android.support.v7.app.AlertDialog
import android.support.v7.app.AppCompatActivity
import android.util.Log
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.Toast
import com.mapbox.android.core.location.LocationEnginePriority
import com.mapbox.android.core.location.LocationEngineProvider
import com.mapbox.mapboxsdk.Mapbox
import com.mapbox.mapboxsdk.annotations.MarkerOptions
import com.mapbox.mapboxsdk.annotations.PolygonOptions
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory
import com.mapbox.mapboxsdk.geometry.LatLng
import com.mapbox.mapboxsdk.maps.MapboxMap
import kotlinx.android.synthetic.main.activity_map.*


class MapActivity : AppCompatActivity() {

    private lateinit var currentLatLong : LatLng
    private var mapboxMap: MapboxMap? = null
    private var polygon = ArrayList<LatLng>()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        initializeView(savedInstanceState)
    }


    private fun initializeView(savedInstanceState: Bundle?) {
        Mapbox.getInstance(this, Utils.mapAccessToken)
        mapView.onCreate(savedInstanceState)
        initializeMapBox()
        getPolygonLandId()
        onButtonClickListener()
    }

    private fun initializeMapBox() {
        val latitude = intent.hasExtra(Utils.latitude).let {
            intent.getDoubleExtra(Utils.latitude, 0.0)
        }
        val longitude = intent.hasExtra(Utils.latitude).let {
            intent.getDoubleExtra(Utils.longitude, 0.0)
        }
        currentLatLong = LatLng(latitude,longitude)

        mapView.getMapAsync {
            mapboxMap = it
            getBoundaries(it)
        }
    }

    /**
     * function to store lat and long of user touch
     * @param mapboxMap as Map
     */
    private fun getBoundaries(mapboxMap: MapboxMap?) {
        mapboxMap?.addOnMapClickListener {
            Log.d("current",it.toString())
            polygon.add(LatLng(it.latitude, it.longitude))
            mapboxMap.addMarker(MarkerOptions().position(LatLng(it.latitude, it.longitude)))
        }
        mapboxMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong, 11.0))
    }


    private fun onButtonClickListener(){
        btn_draw_polygon.setOnClickListener {

            if(polygon.size > 2)
                drawPolygon()
            else
                Toast.makeText(this,getString(R.string.polyError),Toast.LENGTH_SHORT).show()
        }
        btn_save.setOnClickListener {
            if(polygon.size > 0)
                getPolygonLandId()
            else
                Toast.makeText(this,getString(R.string.saveError),Toast.LENGTH_SHORT).show()
        }
    }

    private fun drawPolygon(){
        mapboxMap?.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLong, 11.0))
        mapboxMap?.addPolygon(PolygonOptions()
                .addAll(polygon)
                .fillColor(Color.parseColor("#3F51B5")))
    }

    private fun getPolygonLandId(){
        val editText = EditText(this)
        editText.setSingleLine()
        editText.hint = getString(R.string.enterId)

        val alertBuilder = AlertDialog.Builder(this)
            .setTitle(getString(R.string.savePlot))
                .setEditText(editText)

        alertBuilder.setPositiveButton(getString(R.string.save), { dialog, which ->
            dialog.dismiss()
        })

        alertBuilder.show()
    }

    /**
     * function to set editext fit in AlertDialog
     */
    val Float.toPx: Int
        get() = (this * Resources.getSystem().displayMetrics.density).toInt()

    private fun AlertDialog.Builder.setEditText(editText: EditText): AlertDialog.Builder {

        val container = FrameLayout(context)
        container.addView(editText)
        val containerParams = FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.WRAP_CONTENT
        )
        val marginHorizontal = 48F
        val marginTop = 16F
        containerParams.topMargin = (marginTop / 2).toPx
        containerParams.leftMargin = marginHorizontal.toInt()
        containerParams.rightMargin = marginHorizontal.toInt()
        container.layoutParams = containerParams

        val superContainer = FrameLayout(context)
        superContainer.addView(container)

        setView(superContainer)

        return this
    }

}