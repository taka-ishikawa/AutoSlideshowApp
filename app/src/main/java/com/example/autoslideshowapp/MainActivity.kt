package com.example.autoslideshowapp

import android.Manifest
import android.content.ContentUris
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.provider.MediaStore
import android.util.Log
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*

class MainActivity : AppCompatActivity(), View.OnClickListener {

    //permission/getContentsInfo
    val PERMISSIONS_REQUEST_CODE = 100
    var intGateKeeper: Int = 0

    //URI
    lateinit var imageUri: Uri
    var id: Long = 0
    var idInitial: Long = 0
    var idLength: Int = 0

    //button_status
    val button_status_play: Int = 0
    val button_status_pause: Int = 1
    var button_status: Int = button_status_play

    //timer
    var timer: Timer? = null
    var handler = Handler()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                getContentsInfo()
            } else {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
            }
        } else {
            getContentsInfo()
        }

        buttonNext.isEnabled = true
        buttonBack.isEnabled = true
        buttonPlayPause.isEnabled = true

        buttonNext.setOnClickListener(this)
        buttonBack.setOnClickListener(this)
        buttonPlayPause.setOnClickListener(this)
    }

    private fun getContentsInfo() {
        if (intGateKeeper == 0) {
            val resolver = contentResolver
            val cursor = resolver.query(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                null,
                null,
                null,
                null
            )

            if (cursor.moveToFirst()) {
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                id = cursor.getLong(fieldIndex)

                idInitial = cursor.getLong(fieldIndex)
                idLength = cursor.count

                imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, idInitial)
                imageView.setImageURI(imageUri)
                Log.d("imageUri", imageUri.toString())
            }
            cursor.close()

            intGateKeeper += 1
        }
    }

    override fun onClick(v: View?) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                getContentsInfo()
            } else {
                requestPermissions(arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSIONS_REQUEST_CODE)
                return
            }
        } else {
            getContentsInfo()
        }

        when(v?.id){
            R.id.buttonNext -> {
                slideNext()
            }

            R.id.buttonBack -> {
                if (id <= idInitial)
                    id = idInitial + (idLength - 1)
                else
                    id -= 1

                imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
                imageView.setImageURI(imageUri)
            }
            R.id.buttonPlayPause -> {
                when(button_status){
                    button_status_play -> {
                        buttonNext.isEnabled = false
                        buttonBack.isEnabled = false

                        timer = Timer()
                        timer!!.schedule((object : TimerTask() {
                            override fun run() {
                                handler.post { slideNext() }
                            }
                        }),2000, 2000)
                        button_status = button_status_pause
                        buttonPlayPause.text = getString(R.string.button_status_pause)
                    }
                    button_status_pause -> {
                        if (timer != null){
                            timer!!.cancel()
                            timer = null
                        }
                        button_status = button_status_play
                        buttonPlayPause.text = getString(R.string.button_status_play)
                        buttonNext.isEnabled = true
                        buttonBack.isEnabled = true
                    }
                }
            }
        }
    }

    private fun slideNext() {
        if (id >= idInitial + (idLength - 1))
            id = idInitial
        else
            id += 1

        imageUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)
        imageView.setImageURI(imageUri)
        Log.d("id_Log", id.toString())
    }
}
