package com.example.cameraapp

import android.content.Context
import android.content.pm.PackageManager
import android.graphics.ImageFormat
import android.graphics.SurfaceTexture
import android.hardware.camera2.CameraAccessException
import android.hardware.camera2.CameraCaptureSession
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraDevice
import android.hardware.camera2.CameraManager
import android.hardware.camera2.CameraMetadata
import android.hardware.camera2.CaptureRequest
import android.hardware.camera2.TotalCaptureResult
import android.media.Image
import android.media.ImageReader
import android.os.Bundle
import android.util.SparseIntArray
import android.view.Surface
import android.view.TextureView
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import android.Manifest
import android.app.Activity
import android.content.Intent
import android.os.Environment
import android.util.Log
import com.example.cameraapp.databinding.ActivityCameraBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class Camera : AppCompatActivity() {
    private lateinit var textureView: TextureView
    private var cameraDevice: CameraDevice? = null
    private var captureSession: CameraCaptureSession? = null
    private lateinit var captureRequestBuilder: CaptureRequest.Builder
    private lateinit var imageReader: ImageReader
    private lateinit var cameraCharacteristics: CameraCharacteristics
    private lateinit var binding: ActivityCameraBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= ActivityCameraBinding.inflate(layoutInflater)
        setContentView(binding.root)

        textureView = findViewById(R.id.textureView)
        textureView.surfaceTextureListener = textureListener

        val captureButton: Button = findViewById(R.id.button_capture)
        captureButton.setOnClickListener { captureImage() }

        binding.backArrow.setOnClickListener {
            finish()
        }

        requestCameraPermission()
    }

    private fun requestCameraPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), REQUEST_CAMERA_PERMISSION)
        }
    }

    private val textureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(surface: SurfaceTexture, width: Int, height: Int) {
            openCamera()
        }

        override fun onSurfaceTextureSizeChanged(surface: SurfaceTexture, width: Int, height: Int) {}
        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean = false
        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {}
    }

    private fun openCamera() {
        val manager = getSystemService(Context.CAMERA_SERVICE) as CameraManager
        try {
            val cameraId = manager.cameraIdList[0]
            cameraCharacteristics = manager.getCameraCharacteristics(cameraId)
            val map = cameraCharacteristics.get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP)

            imageReader = ImageReader.newInstance(640, 480, ImageFormat.JPEG, 1)
            imageReader.setOnImageAvailableListener(imageAvailableListener, null)

            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.CAMERA
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return
            }
            manager.openCamera(cameraId, stateCallback, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private val stateCallback = object : CameraDevice.StateCallback() {
        override fun onOpened(camera: CameraDevice) {
            cameraDevice = camera
            startPreview()
        }

        override fun onDisconnected(camera: CameraDevice) {
            cameraDevice?.close()
        }

        override fun onError(camera: CameraDevice, error: Int) {
            cameraDevice?.close()
            cameraDevice = null
        }
    }

    private fun startPreview() {
        if (cameraDevice == null || !textureView.isAvailable || imageReader == null) return

        val texture = textureView.surfaceTexture ?: return
        texture.setDefaultBufferSize(textureView.width, textureView.height)
        val surface = Surface(texture)

        try {
            captureRequestBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW)
            captureRequestBuilder.addTarget(surface)

            cameraDevice!!.createCaptureSession(listOf(surface, imageReader.surface), object : CameraCaptureSession.StateCallback() {
                override fun onConfigured(session: CameraCaptureSession) {
                    if (cameraDevice == null) return

                    captureSession = session
                    captureRequestBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)

                    try {
                        captureSession!!.setRepeatingRequest(captureRequestBuilder.build(), null, null)
                    } catch (e: CameraAccessException) {
                        e.printStackTrace()
                    }
                }

                override fun onConfigureFailed(session: CameraCaptureSession) {}
            }, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun captureImage() {
        if (cameraDevice == null) return

        try {
            val captureBuilder = cameraDevice!!.createCaptureRequest(CameraDevice.TEMPLATE_STILL_CAPTURE)
            captureBuilder.addTarget(imageReader.surface)
            captureBuilder.set(CaptureRequest.CONTROL_MODE, CameraMetadata.CONTROL_MODE_AUTO)

            // Adjust the JPEG orientation based on device rotation and sensor orientation
            val rotation = windowManager.defaultDisplay.rotation
            val sensorOrientation = cameraCharacteristics.get(CameraCharacteristics.SENSOR_ORIENTATION) ?: 0
            val jpegOrientation = getOrientation(rotation, sensorOrientation)
            captureBuilder.set(CaptureRequest.JPEG_ORIENTATION, jpegOrientation)

            val captureListener = object : CameraCaptureSession.CaptureCallback() {
                override fun onCaptureCompleted(session: CameraCaptureSession, request: CaptureRequest, result: TotalCaptureResult) {
                    super.onCaptureCompleted(session, request, result)
                    startPreview()
                }
            }

            captureSession?.stopRepeating()
            captureSession?.capture(captureBuilder.build(), captureListener, null)
        } catch (e: CameraAccessException) {
            e.printStackTrace()
        }
    }

    private fun getOrientation(rotation: Int, sensorOrientation: Int): Int {
        val orientations = mapOf(
            Surface.ROTATION_0 to 0,
            Surface.ROTATION_90 to 90,
            Surface.ROTATION_180 to 180,
            Surface.ROTATION_270 to 270
        )
        val deviceOrientation = orientations[rotation] ?: 0
        return (sensorOrientation - deviceOrientation + 360) % 360
    }


    private val imageAvailableListener = ImageReader.OnImageAvailableListener { reader ->
        var image: Image? = null
        try {
            image = reader.acquireLatestImage()
            val buffer = image.planes[0].buffer
            val bytes = ByteArray(buffer.remaining())
            buffer.get(bytes)
            val imagePath = saveImage(bytes)

            // Start preview activity
            val intent = Intent(this, Preview::class.java)
            intent.putExtra("imagePath", imagePath)
            startActivityForResult(intent, REQUEST_PREVIEW)
        } finally {
            image?.close()
        }
    }

    private fun saveImage(bytes: ByteArray): String {
        val picturesDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        if (picturesDir != null) {
            if (!picturesDir.exists()) {
                picturesDir.mkdirs()
            }
        }
        val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
        val fileName = "IMG_$timeStamp.jpg"
        val file = File(picturesDir, fileName)
        var output: FileOutputStream? = null
        try {
            output = FileOutputStream(file)
            output.write(bytes)
        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            output?.close()
        }
        return file.absolutePath
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_PREVIEW) {
            if (resultCode == Activity.RESULT_CANCELED) {
                // Retake the image, restart preview
                startPreview()
            }
        }
    }

    companion object {
        private const val REQUEST_CAMERA_PERMISSION = 200
        private const val REQUEST_PREVIEW = 201
    }
}


