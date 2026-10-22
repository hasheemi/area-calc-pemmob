package com.example.areacalc

import android.Manifest
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private lateinit var previewView: PreviewView
    private lateinit var shutter: ImageButton
    private lateinit var switchButton: Button
    private lateinit var galleryButton: Button
    private lateinit var tiltOverlay: TiltOverlayView
    private lateinit var sensorStatus: TextView
    private var imageCapture: ImageCapture? = null
    private var lensFacing = CameraSelector.LENS_FACING_BACK
    private var cameraStarting = false

    // Sensors: gyroscope + accelerometer drive the on-screen tilt/level indicator;
    // the light sensor drives the automatic torch/flash.
    private lateinit var sensorManager: SensorManager
    private var gyroSensor: Sensor? = null
    private var accelSensor: Sensor? = null
    private var lightSensor: Sensor? = null
    private var camera: androidx.camera.core.Camera? = null
    private var lastLux = -1f
    private var torchOn = false

    // Complementary-filter roll/pitch, tracked across gyro + accelerometer samples.
    private var roll = 0f
    private var pitch = 0f
    private var lastGyroTs = 0L

    private val sensorListener = object : SensorEventListener {
        override fun onSensorChanged(event: SensorEvent) {
            when (event.sensor.type) {
                Sensor.TYPE_GYROSCOPE -> {
                    val now = event.timestamp
                    if (lastGyroTs != 0L) {
                        val dt = (now - lastGyroTs) / 1_000_000_000f
                        if (dt > 0f && dt < 0.5f) {
                            // values are rad/s about the device axes.
                            roll += Math.toDegrees(event.values[0].toDouble()).toFloat() * dt
                            pitch += Math.toDegrees(event.values[1].toDouble()).toFloat() * dt
                            pushTilt()
                        }
                    }
                    lastGyroTs = now
                }
                Sensor.TYPE_ACCELEROMETER -> {
                    // The accelerometer gives an absolute (no-drift) reference.
                    val ax = event.values[0]
                    val ay = event.values[1]
                    if (ax != 0f || ay != 0f) {
                        val accelRoll = Math.toDegrees(kotlin.math.atan2(ax.toDouble(), ay.toDouble())).toFloat()
                        // Blend gyro (responsive) with accelerometer (drift-free).
                        val corrected = if (abs(accelRoll - roll) > 180f) {
                            if (accelRoll > roll) accelRoll - 360f else accelRoll + 360f
                        } else accelRoll
                        roll += (corrected - roll) * ACCEL_BLEND
                        pushTilt()
                    }
                }
                Sensor.TYPE_LIGHT -> {
                    val lux = if (event.values.isNotEmpty()) event.values[0] else 0f
                    lastLux = lux
                    applyAutoTorch(lux)
                }
            }
        }

        override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
    }

    private fun pushTilt() {
        tiltOverlay.updateTilt(roll, pitch)
    }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) {
        if (hasCameraPermission()) startCamera()
        else toast(getString(R.string.camera_permission_required))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        previewView = findViewById(R.id.previewView)
        shutter = findViewById(R.id.shutterButton)
        switchButton = findViewById(R.id.switchButton)
        galleryButton = findViewById(R.id.galleryButton)
        tiltOverlay = findViewById(R.id.tiltOverlay)
        sensorStatus = findViewById(R.id.sensorStatus)
        lensFacing = savedInstanceState?.getInt("lensFacing") ?: CameraSelector.LENS_FACING_BACK

        sensorManager = getSystemService(SENSOR_SERVICE) as SensorManager
        gyroSensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
        accelSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        lightSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)

        galleryButton.setOnClickListener {
            startActivity(Intent(this, GalleryActivity::class.java))
        }
        shutter.setOnClickListener {
            if (!hasCameraPermission() || imageCapture == null) requestPermissions()
            else if (Build.VERSION.SDK_INT <= 28 && !hasPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                requestPermissions()
            } else capturePhoto()
        }
        switchButton.setOnClickListener {
            lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                CameraSelector.LENS_FACING_FRONT
            } else CameraSelector.LENS_FACING_BACK
            startCamera()
        }
        if (hasCameraPermission()) startCamera() else requestPermissions()
    }

    override fun onResume() {
        super.onResume()
        if (hasCameraPermission() && imageCapture == null) startCamera()
        lastGyroTs = 0L
        gyroSensor?.let {
            sensorManager.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_GAME)
        }
        accelSensor?.let {
            sensorManager.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_UI)
        }
        lightSensor?.let {
            sensorManager.registerListener(sensorListener, it, SensorManager.SENSOR_DELAY_UI)
        }
    }

    override fun onPause() {
        super.onPause()
        sensorManager.unregisterListener(sensorListener)
        tiltOverlay.clear()
        lastGyroTs = 0L
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("lensFacing", lensFacing)
        super.onSaveInstanceState(outState)
    }

    private fun hasPermission(permission: String) =
        ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED

    private fun hasCameraPermission() = hasPermission(Manifest.permission.CAMERA)

    private fun requestPermissions() {
        val permissions = mutableListOf(Manifest.permission.CAMERA)
        if (Build.VERSION.SDK_INT <= 28) permissions += Manifest.permission.WRITE_EXTERNAL_STORAGE
        permissionLauncher.launch(permissions.toTypedArray())
    }

    private fun startCamera() {
        if (cameraStarting) return
        cameraStarting = true
        shutter.isEnabled = false
        switchButton.isEnabled = false
        val future = ProcessCameraProvider.getInstance(this)
        future.addListener({
            cameraStarting = false
            if (isDestroyed || isFinishing) return@addListener
            try {
                val provider = future.get()
                var selector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
                if (!provider.hasCamera(selector)) {
                    lensFacing = if (lensFacing == CameraSelector.LENS_FACING_BACK) {
                        CameraSelector.LENS_FACING_FRONT
                    } else CameraSelector.LENS_FACING_BACK
                    selector = CameraSelector.Builder().requireLensFacing(lensFacing).build()
                }
                val preview = Preview.Builder().build().also {
                    it.setSurfaceProvider(previewView.surfaceProvider)
                }
                val capture = ImageCapture.Builder()
                    .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                    .build()
                provider.unbindAll()
                camera = provider.bindToLifecycle(this, selector, preview, capture)
                imageCapture = capture
                // unbindAll() turns the torch off; sync our state before re-applying.
                torchOn = false
                // Re-apply the current light reading to the newly bound camera.
                if (lastLux >= 0f) applyAutoTorch(lastLux)
                switchButton.isEnabled = provider.hasCamera(CameraSelector.DEFAULT_BACK_CAMERA) &&
                    provider.hasCamera(CameraSelector.DEFAULT_FRONT_CAMERA)
                shutter.isEnabled = true
            } catch (exception: Exception) {
                imageCapture = null
                shutter.isEnabled = true
                toast(getString(R.string.camera_error))
            }
        }, ContextCompat.getMainExecutor(this))
    }

    @Suppress("DEPRECATION")
    private fun capturePhoto() {
        val capture = imageCapture ?: return
        val date = SimpleDateFormat("yyyyMMdd", Locale.US).format(Date())
        val name = "$date-${Random.nextLong(100000000, 1000000000)}.jpg"
        val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DCIM), "pemmobcam")
        val file = File(directory, name)
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            if (Build.VERSION.SDK_INT >= 29) {
                put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_DCIM}/pemmobcam/")
            }
        }
        val options = if (Build.VERSION.SDK_INT >= 29) {
            // MediaStore handles scoped storage and publishes the image after writing.
            ImageCapture.OutputFileOptions.Builder(
                contentResolver, MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values
            ).build()
        } else {
            if (!directory.exists() && !directory.mkdirs()) {
                toast(getString(R.string.save_error))
                return
            }
            ImageCapture.OutputFileOptions.Builder(file).build()
        }
        capture.targetRotation = previewView.display.rotation
        shutter.isEnabled = false
        galleryButton.isEnabled = false
        switchButton.isEnabled = false
        capture.takePicture(options, ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    // Scan only AFTER CameraX closes the completed file. On scoped storage,
                    // use the actual MediaStore path (it may rename a colliding filename).
                    val path = output.savedUri?.takeIf { it.scheme == "content" }?.let { uri ->
                        runCatching {
                            contentResolver.query(uri, arrayOf(MediaStore.Images.Media.DATA), null, null, null)?.use {
                                if (it.moveToFirst()) it.getString(0) else null
                            }
                        }.getOrNull()
                    } ?: file.absolutePath
                    MediaScannerConnection.scanFile(applicationContext, arrayOf(path), arrayOf("image/jpeg")) { _, _ ->
                        runOnUiThread {
                            if (!isDestroyed) {
                                restoreControls()
                                toast(getString(R.string.photo_saved, name))
                            }
                        }
                    }
                }

                override fun onError(exception: ImageCaptureException) {
                    restoreControls()
                    toast(getString(R.string.save_error))
                }
            })
    }

    private fun applyAutoTorch(lux: Float) {
        val cam = camera ?: return
        val auto = lux < AUTO_FLASH_LUX_THRESHOLD
        val hasFlash = cam.cameraInfo.hasFlashUnit()
        // Only touch the torch when the desired state actually changes.
        if (auto == torchOn) {
            updateSensorStatus(lux, torchOn)
            return
        }
        if (auto && !hasFlash) {
            updateSensorStatus(lux, false)
            toast(getString(R.string.auto_flash_unavailable))
            return
        }
        cam.cameraControl.enableTorch(auto)
        torchOn = auto
        updateSensorStatus(lux, torchOn)
    }

    private fun updateSensorStatus(lux: Float, torchOn: Boolean) {
        if (lux < 0f) {
            sensorStatus.visibility = TextView.GONE
            return
        }
        sensorStatus.visibility = TextView.VISIBLE
        val state = if (torchOn) getString(R.string.auto_flash_on) else getString(R.string.auto_flash_off)
        sensorStatus.text = String.format(Locale.US, "%.0f lux · %s", lux, state)
    }

    private fun restoreControls() {
        shutter.isEnabled = true
        galleryButton.isEnabled = true
        startCamera()
    }

    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_SHORT).show()

    companion object {
        // Below this ambient light level (lux) the torch/flash is switched on automatically.
        private const val AUTO_FLASH_LUX_THRESHOLD = 30f
        // Weight given to the accelerometer when correcting the gyro tilt (0..1).
        private const val ACCEL_BLEND = 0.12f
    }
}
