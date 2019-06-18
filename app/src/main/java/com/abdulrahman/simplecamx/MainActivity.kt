package com.abdulrahman.simplecamx

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import com.abdulrahman.simplecamx.utli.FLAGS_FULLSCREEN
import java.io.File

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        window.decorView.systemUiVisibility = FLAGS_FULLSCREEN
    }


    companion object{
        fun getOutputDirectory(context: Context) : File{
           val appContext = context.applicationContext
            val mediaDir = context.externalMediaDirs.firstOrNull()?.let {
                File(it,appContext.resources.getString(R.string.app_name)).apply { mkdirs() }
            }
            return if (mediaDir != null && mediaDir.exists()) mediaDir else appContext.filesDir
        }
    }
}
