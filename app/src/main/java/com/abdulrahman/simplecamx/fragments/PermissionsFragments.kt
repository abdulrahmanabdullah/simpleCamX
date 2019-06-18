package com.abdulrahman.simplecamx.fragments

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.NavOptions
import androidx.navigation.Navigation
import com.abdulrahman.simplecamx.R

private const val PERMISSION_REQUEST_CODE = 123
private val PERMISSIONS = arrayOf(Manifest.permission.CAMERA)

class PermissionsFragments : Fragment() {

    private val navOptions = NavOptions.Builder()
        .setPopUpTo(R.id.permissionsFragments2, true).build()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (!hasPerimssions()) {
            requestPermissions(PERMISSIONS, PERMISSION_REQUEST_CODE)

        } else {
            Navigation.findNavController(requireActivity(), R.id.container_fragment)
                .navigate(R.id.action_to_camera_Fragment,null,navOptions)
        }
    }


    private fun hasPerimssions() = PERMISSIONS.all {
        ContextCompat.checkSelfPermission(requireContext(), it) == PackageManager.PERMISSION_GRANTED
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if(requestCode == PERMISSION_REQUEST_CODE){
            // If all permissions granted drive app to CameraFragment
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
                Toast.makeText(context,"Permission granted",Toast.LENGTH_LONG).show()
                Navigation.findNavController(requireActivity(),R.id.container_fragment)
                    .navigate(R.id.action_to_camera_Fragment,null,navOptions)
            }else{
                Toast.makeText(context,"Permission granted",Toast.LENGTH_LONG).show()
            }
        }
    }
}