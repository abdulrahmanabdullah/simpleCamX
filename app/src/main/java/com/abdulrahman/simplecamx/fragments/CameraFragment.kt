package com.abdulrahman.simplecamx.fragments

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.hardware.display.DisplayManager
import android.os.Build
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.Rational
import android.util.Size
import android.view.*
import android.widget.ImageButton
import android.widget.ImageView
import androidx.camera.core.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.abdulrahman.simplecamx.MainActivity
import com.abdulrahman.simplecamx.R
import com.abdulrahman.simplecamx.utli.AutoFitPreviewBuilder
import com.abdulrahman.simplecamx.utli.ImageUtils
import com.abdulrahman.simplecamx.utli.showSnackbar
import com.bumptech.glide.Glide
import kotlinx.coroutines.*
import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.*
import kotlin.coroutines.CoroutineContext


private const val FILE_NAME = "yyyy-MM-dd-HH-mm-ss"
private const val PHOTO_EXTENSION = ".JPG"

private fun createFile(baseFolder: File, format: String, extension: String): File {
    return File(baseFolder, SimpleDateFormat(format, Locale.US).format(System.currentTimeMillis()) + extension)
}

class CameraFragment : Fragment(), CoroutineScope {

    private val job = Job()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + job

    //Manage all Views in camera_ui_container.xml
    private lateinit var container: ConstraintLayout
    private lateinit var textureView: TextureView
    private lateinit var outputDirectory: File
    private var preview: Preview? = null
    private var lensFacing = CameraX.LensFacing.BACK
    // To determine which camera use
    private var isLensFacing = false
    private lateinit var bitmap: Bitmap
    private var imageCapture: ImageCapture? = null
    // Access all Views in camera_ui_container
    private lateinit var controlView: View
    private var displayId = -1
    private lateinit var displayManager: DisplayManager

    //Set preview and imageCapture
    private val displayListener = object : DisplayManager.DisplayListener {
        override fun onDisplayChanged(p0: Int) = view?.let {
            if (p0 == this@CameraFragment.displayId) {
                preview?.setTargetRotation(it.display.rotation)
                imageCapture?.setTargetRotation(it.display.rotation)
            }
        } ?: Unit


        override fun onDisplayAdded(p0: Int) {
            view?.showSnackbar("onDisplayAdded call")
        }


        override fun onDisplayRemoved(p0: Int) {
            view?.showSnackbar("onDisplayRemove call")
        }

    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.camera_fragment, container, false)
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        container = view as ConstraintLayout
        textureView = container.findViewById(R.id.texture_view)
        displayManager = textureView.context.getSystemService(Context.DISPLAY_SERVICE) as DisplayManager
        //Register this display
        displayManager.registerDisplayListener(displayListener, null)
        textureView.post {
            displayId = textureView.display.displayId
            updateCameraUI()
            startCamera()
        }
        outputDirectory = MainActivity.getOutputDirectory(requireContext())
    }

    //Image saver
    val imageCaptureListener = object : ImageCapture.OnImageSavedListener {
        override fun onImageSaved(file: File) {
            launch(Dispatchers.Main) {
                saveTempImage(file)
                hideCaptureBtn()
            }
        }

        override fun onError(useCaseError: ImageCapture.UseCaseError, message: String, cause: Throwable?) {
            Log.i("xyz", "some error occurred ${cause?.message}")
        }
    }

    private fun startCamera() {
        //Unbind all view to recompute new one .
        CameraX.unbindAll()

        val metrics = DisplayMetrics()
            .also { textureView.display.getRealMetrics(it) }
        val screenRatio = Rational(metrics.widthPixels, metrics.heightPixels)
        val screenSize = Size(metrics.widthPixels, metrics.heightPixels)

        val previewConfig = PreviewConfig.Builder().apply {
            setLensFacing(lensFacing)
            setTargetResolution(screenSize)
            setTargetAspectRatio(screenRatio)
            setTargetRotation(textureView.display.rotation)
        }.build()

        preview = AutoFitPreviewBuilder.build(previewConfig, textureView)

        val imageCaptureConfig = ImageCaptureConfig.Builder().apply {
            setLensFacing(lensFacing)
            setTargetResolution(screenSize)
            setCaptureMode(ImageCapture.CaptureMode.MIN_LATENCY)
            setTargetAspectRatio(screenRatio)
            setTargetRotation(textureView.display.rotation)
        }.build()
        imageCapture = ImageCapture(imageCaptureConfig)

        CameraX.bindToLifecycle(this, preview, imageCapture)
    }

    @SuppressLint("RestrictedApi")
    private fun updateCameraUI() {

        container.findViewById<ConstraintLayout>(R.id.camera_ui_container).let {
            container.removeView(it)
        }

        controlView = View.inflate(requireContext(), R.layout.camera_ui_container, container)

        //Set capture  and switch buttons visible
        restCaptureBtn()

        controlView.findViewById<ImageButton>(R.id.capture_btn).setOnClickListener {
            imageCapture?.let { imageCapture ->
                val photo = createFile(outputDirectory, FILE_NAME, PHOTO_EXTENSION)
                imageCapture.takePicture(photo, imageCaptureListener)
            }
            //If device under 23
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                container.postDelayed({
                    container.foreground = ColorDrawable(Color.WHITE)
                    container.postDelayed({
                        container.foreground = null
                    }, 50L)
                }, 100L)
            }

        }
        //Listener for imageButton to switch front and back camera .
        controlView.findViewById<ImageButton>(R.id.switcher_image_btn).setOnClickListener {
            lensFacing = if (CameraX.LensFacing.BACK == lensFacing) {
                isLensFacing = true
                CameraX.LensFacing.FRONT
            } else {
                isLensFacing = false
                CameraX.LensFacing.BACK
            }
            //Bind camera with switch cam
            try {
                CameraX.getCameraWithLensFacing(lensFacing)
                startCamera()
            } catch (e: Exception) {
                Log.d("xyz", " switch camera throw exception ${e.message}")
            }
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        displayManager.unregisterDisplayListener(displayListener)
    }


    override fun onDestroy() {
        job.cancel()
        super.onDestroy()
    }

    //Long process
    private suspend fun saveTempImage(file: File) {
        withContext(Dispatchers.IO) {
            bitmap = ImageUtils.decodeBitmap(file, isLensFacing)
        }
    }

    private fun hideCaptureBtn() {
        controlView.findViewById<ImageButton>(R.id.capture_btn).visibility = View.INVISIBLE
        controlView.findViewById<ImageButton>(R.id.switcher_image_btn).visibility = View.INVISIBLE
        controlView.findViewById<ImageView>(R.id.show_capture_img_view)
            .apply {
                visibility = View.VISIBLE
                Glide.with(context).load(bitmap).into(this)
            }
        controlView.findViewById<ImageButton>(R.id.close_capture_btn).apply {
            visibility = View.VISIBLE
            setOnClickListener {
                updateCameraUI()
            }
        }
    }

    //Set Capture buttons and switch button visible when click x icon .
    private fun restCaptureBtn() {
        controlView.findViewById<ImageButton>(R.id.capture_btn).visibility = View.VISIBLE
        controlView.findViewById<ImageButton>(R.id.switcher_image_btn).visibility = View.VISIBLE
        controlView.findViewById<ImageButton>(R.id.close_capture_btn).visibility = View.INVISIBLE
        controlView.findViewById<ImageView>(R.id.show_capture_img_view).visibility = View.INVISIBLE
    }
}