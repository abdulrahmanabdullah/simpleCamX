package com.abdulrahman.simplecamx.utli

import android.view.View
import com.google.android.material.snackbar.Snackbar


fun View.showSnackbar(str:String){
    Snackbar.make(this,str,Snackbar.LENGTH_LONG).show()
}


const val FLAGS_FULLSCREEN =
    View.SYSTEM_UI_FLAG_LOW_PROFILE or
            View.SYSTEM_UI_FLAG_FULLSCREEN or
            View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY or
            View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION or
            View.SYSTEM_UI_FLAG_HIDE_NAVIGATION


