package com.test.androidtest

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.util.Log

class DatabaseHandler(context: Context) : SQLiteOpenHelper(context,
        DatabaseHandler.DB_NAME, null, DatabaseHandler.DB_VERSION) {


    override fun onUpgrade(databse: SQLiteDatabase?, p1: Int, p2: Int) {
        val dropImgTable = "DROP TABLE IF EXISTS $IMG_TABLE_NAME"
        val dropPolyTable = "DROP TABLE IF EXISTS $POLY_TABLE_NAME"
        databse?.execSQL(dropImgTable)
        databse?.execSQL(dropPolyTable)
        onCreate(databse)
    }

    override fun onCreate(databse: SQLiteDatabase?) {

        val createImageTable = "CREATE TABLE $IMG_TABLE_NAME (" +
                ID + " INTEGER," +
                ImageBlob + " TEXT);"
        val createPolyTable = "CREATE TABLE $POLY_TABLE_NAME (" +
                ObjectId + " INTEGER," +
                PolyPoints + " TEXT);"
        databse?.execSQL(createImageTable)
        databse?.execSQL(createPolyTable)
    }

    fun addImageData(imageData: ImageData) : Boolean {

        val contentValues = ContentValues().apply {
            this.put(ID,imageData.Id)
            this.put(ImageBlob, imageData.ImageBlob)
        }
        val insertImageData = writableDatabase.
                insert(IMG_TABLE_NAME, null, contentValues)
        writableDatabase.close()

        return (Integer.parseInt("$insertImageData") != -1)
    }

    fun addPolyData(polygonData: PolygonData) : Boolean {
        val contentValues = ContentValues().apply {
            this.put(ObjectId,polygonData.ObjectId)
            this.put(PolyPoints, polygonData.PolyPoints)
        }
        val insertPolyData = writableDatabase.
                insert(POLY_TABLE_NAME, null, contentValues)
        writableDatabase.close()

        return (Integer.parseInt("$insertPolyData") != -1)
    }

    fun getStorageDB(){
        val selectQuery = "SELECT  * FROM $POLY_TABLE_NAME"
        val cursor = writableDatabase.rawQuery(selectQuery, null)
        if (cursor != null) {
            cursor.moveToFirst()
            while (cursor.moveToNext()) {
                Log.d("ImagePathId",cursor.getString(cursor.getColumnIndex(ObjectId)))
                Log.d("ImagePath",cursor.getString(cursor.getColumnIndex(PolyPoints)))
            }
        }
        cursor.close()

    }
    companion object {
        private val DB_VERSION = 1
        private val DB_NAME = "Task"
        private val POLY_TABLE_NAME = "PolygonData"
        private val IMG_TABLE_NAME = "ImageData"
        private val ID = "Id"
        private val ImageBlob = "ImageBlob"
        private val ObjectId = "ObjectId"
        private val PolyPoints = "PolyPoints"
    }
}