package com.abdulrahman.simplecamx.utli

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.exifinterface.media.ExifInterface
import java.io.File
import java.lang.IllegalArgumentException

abstract class ImageUtils {

    companion object {
        private fun decodeExifOrientation(orientation: Int, isFacing:Boolean): Matrix {
            val matrix = Matrix()
            when (orientation) {
                ExifInterface.ORIENTATION_NORMAL -> Unit
                ExifInterface.ORIENTATION_UNDEFINED -> Unit
                ExifInterface.ORIENTATION_ROTATE_90 -> matrix.postRotate(90f)
                ExifInterface.ORIENTATION_ROTATE_180 -> matrix.postRotate(180f)
                ExifInterface.ORIENTATION_ROTATE_270 -> matrix.postRotate(270f)
                ExifInterface.ORIENTATION_FLIP_HORIZONTAL -> matrix.postScale(-1f, 1f)
                ExifInterface.ORIENTATION_FLIP_VERTICAL -> matrix.postScale(1f, -1f)
                ExifInterface.ORIENTATION_TRANSPOSE -> {
                    matrix.postScale(-1f, 1f)
                    matrix.postRotate(270f)
                }
                ExifInterface.ORIENTATION_TRANSVERSE -> {
                    matrix.postScale(-1f, 1f)
                    matrix.postRotate(90f)
                }
                else -> throw IllegalArgumentException("invalid orientation")
            }
            //Solve mirror capture when use facing lens .
            if (isFacing) {
                matrix.postScale(-1f, 1.0f)
            }
            return matrix
        }

        fun decodeBitmap(file: File, isFacing: Boolean): Bitmap {
            val exif = ExifInterface(file.absolutePath)
            val transformation = decodeExifOrientation(
                exif.getAttributeInt(
                    ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_ROTATE_90
                ), isFacing
            )
            val bitmap = BitmapFactory.decodeFile(file.absolutePath)
            return Bitmap.createBitmap(
                BitmapFactory.decodeFile(file.absolutePath),
                0, 0, bitmap.width, bitmap.height, transformation, true
            )
        }
    }
}