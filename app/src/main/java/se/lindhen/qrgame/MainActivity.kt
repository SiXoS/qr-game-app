package se.lindhen.qrgame

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.DisplayMetrics
import android.util.Log
import android.view.View
import android.view.Window
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.gms.tasks.Task
import com.google.mlkit.vision.barcode.Barcode
import com.google.mlkit.vision.barcode.BarcodeScanner
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min

class MainActivity : AppCompatActivity() {

    private lateinit var cameraLayout: ConstraintLayout
    private lateinit var scanQrButton: Button
    private var analysisUseCase: ImageAnalysis? = null
    private var previewUseCase: Preview? = null
    private var cameraProvider: ProcessCameraProvider? = null
    private var cameraActive = false
    private lateinit var cameraSelector: CameraSelector
    private lateinit var previewView: PreviewView
    private lateinit var barcodeScanner: BarcodeScanner
    private val screenAspectRatio: Int
        get() {
            // Get screen metrics used to setup camera for full screen resolution
            val metrics = DisplayMetrics().also { previewView.display?.getRealMetrics(it) }
            return aspectRatio(metrics.widthPixels, metrics.heightPixels)
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if(Build.VERSION.SDK_INT >= 30) {
            window.setDecorFitsSystemWindows(false)
            window.requestFeature(Window.FEATURE_NO_TITLE)
        }
        setContentView(R.layout.activity_main)
        previewView = findViewById(R.id.preview_view)
        cameraLayout = findViewById(R.id.qr_scan_view)
        findViewById<Button>(R.id.qr_scan_cancel).setOnClickListener { if (cameraActive) turnOffCamera() }

        barcodeScanner = BarcodeScanning.getClient(
            BarcodeScannerOptions.Builder()
                .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                .build()
        )

        scanQrButton = findViewById(R.id.scan_qr_button)
        scanQrButton.setOnClickListener {
            scanQr()
        }
        findViewById<Button>(R.id.load_qr_button).setOnClickListener {
            loadQrFromGallery()
        }
        findViewById<Button>(R.id.game_history_button).setOnClickListener {
            startActivity(Intent(this, GameHistoryActivity::class.java))
        }
    }

    override fun onBackPressed() {
        if (cameraActive) {
            turnOffCamera()
        } else {
            super.onBackPressed()
        }
    }

    private fun loadQrFromGallery() {
        if (!isExternalStoragePermissionGranted()) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE), PERMISSION_STORAGE_REQUEST)
        } else {
            doLoadQrFromGallery()
        }
    }

    private fun isExternalStoragePermissionGranted() =
        ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED

    private fun doLoadQrFromGallery() {
        val imagePickIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(imagePickIntent, IMAGE_PICK_RESPONSE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == IMAGE_PICK_RESPONSE && resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            scanQrFromStorage(data.data!!)
        }
    }

    private fun startGameActivity(rawBytes: ByteArray) {
        val intent = Intent(this, GameActivity::class.java)
            .putExtra(GameActivity.EXTRA_BYTECODE_PARAMETER, rawBytes)
        startActivity(intent)
    }

    private fun scanQr() {
        if (isCameraPermissionGranted()) {
            setupCamera()
        } else {
            ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    PERMISSION_CAMERA_REQUEST
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_CAMERA_REQUEST) {
            if (isCameraPermissionGranted()) {
                setupCamera()
            } else {
                Toast.makeText(this, "Camera access denied", Toast.LENGTH_LONG).show()
            }
        } else if (requestCode == PERMISSION_STORAGE_REQUEST) {
            if (isExternalStoragePermissionGranted()) {
                doLoadQrFromGallery()
            } else {
                Toast.makeText(this, "Access to storage denied", Toast.LENGTH_LONG).show()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun setupCamera() {
        cameraSelector = CameraSelector.Builder().requireLensFacing(CameraSelector.LENS_FACING_BACK).build()
        cameraLayout.visibility = View.VISIBLE

        ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application))
            .get(CameraXViewModel::class.java)
            .processCameraProvider
            .observe(this, Observer { provider: ProcessCameraProvider? ->
                cameraProvider = provider
                if (isCameraPermissionGranted()) {
                    bindCameraUseCases()
                } else {
                    ActivityCompat.requestPermissions(
                        this,
                        arrayOf(Manifest.permission.CAMERA),
                        PERMISSION_CAMERA_REQUEST
                    )
                }
            }
            )
        cameraActive = true
    }

    private fun bindCameraUseCases() {
        bindPreviewUseCase()
        bindAnalyseUseCase()
    }

    private fun bindPreviewUseCase() {
        if (cameraProvider == null) {
            return
        }
        if (previewUseCase != null) {
            cameraProvider!!.unbind(previewUseCase)
        }

        previewUseCase = Preview.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(previewView.display.rotation)
            .build()
        previewUseCase!!.setSurfaceProvider(previewView.createSurfaceProvider())

        try {
            cameraProvider!!.bindToLifecycle(this, cameraSelector, previewUseCase)
        } catch (illegalStateException: IllegalStateException) {
            Log.e(TAG, illegalStateException.message ?: illegalStateException.toString())
        } catch (illegalArgumentException: IllegalArgumentException) {
            Log.e(TAG, illegalArgumentException.message ?: illegalArgumentException.toString())
        }
    }

    private fun bindAnalyseUseCase() {

        if (cameraProvider == null) {
            return
        }
        if (analysisUseCase != null) {
            cameraProvider!!.unbind(analysisUseCase)
        }

        analysisUseCase = ImageAnalysis.Builder()
            .setTargetAspectRatio(screenAspectRatio)
            .setTargetRotation(previewView.display.rotation)
            .build()

        // Initialize our background executor
        val cameraExecutor = Executors.newSingleThreadExecutor()

        analysisUseCase?.setAnalyzer(cameraExecutor, ImageAnalysis.Analyzer { imageProxy ->
            scanQrFromCamera(imageProxy)
        })

        try {
            cameraProvider!!.bindToLifecycle(this, cameraSelector, analysisUseCase)
        } catch (illegalStateException: IllegalStateException) {
            Log.e(TAG, illegalStateException.message!!)
        } catch (illegalArgumentException: IllegalArgumentException) {
            Log.e(TAG, illegalArgumentException.message!!)
        }
    }

    @SuppressLint("UnsafeExperimentalUsageError")
    private fun scanQrFromCamera(imageProxy: ImageProxy) {
        val inputImage = InputImage.fromMediaImage(imageProxy.image!!, imageProxy.imageInfo.rotationDegrees)

        scanQrInputImage(inputImage, true) { imageProxy.close() }
    }

    private fun scanQrFromStorage(selectedImage: Uri) {
        scanQrInputImage(InputImage.fromFilePath(this, selectedImage), false)
    }

    private fun scanQrInputImage(
        inputImage: InputImage,
        disableCameraOnComplete: Boolean,
        onComplete: ((Task<MutableList<Barcode>>) -> Unit)? = null
    ) {
        barcodeScanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                if (barcodes.isNotEmpty()) {
                    val rawBytes = barcodes[0].rawBytes!!
                    if (disableCameraOnComplete) {
                        turnOffCamera()
                    }
                    runOnUiThread {
                        startGameActivity(rawBytes)
                    }
                }
            }
            .addOnFailureListener {
                Log.e(TAG, it.message!!)
            }.addOnCompleteListener {
                // When the image is from CameraX analysis use case, must call image.close() on received
                // images when finished using them. Otherwise, new images may not be received or the camera
                // may stall.
                onComplete?.invoke(it)
            }
    }

    private fun isCameraPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(baseContext, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
    }

    private fun turnOffCamera() {
        if(previewUseCase != null) {
            cameraProvider!!.unbind(previewUseCase)
        }
        if(analysisUseCase != null) {
            cameraProvider!!.unbind(analysisUseCase)
        }
        cameraLayout.visibility = View.GONE
        cameraActive = false
    }

    /**
     *  [androidx.camera.core.ImageAnalysis],[androidx.camera.core.Preview] requires enum value of
     *  [androidx.camera.core.AspectRatio]. Currently it has values of 4:3 & 16:9.
     *
     *  Detecting the most suitable ratio for dimensions provided in @params by counting absolute
     *  of preview ratio to one of the provided values.
     *
     *  @param width - preview width
     *  @param height - preview height
     *  @return suitable aspect ratio
     */
    private fun aspectRatio(width: Int, height: Int): Int {
        val previewRatio = max(width, height).toDouble() / min(width, height)
        if (abs(previewRatio - RATIO_4_3_VALUE) <= abs(previewRatio - RATIO_16_9_VALUE)) {
            return AspectRatio.RATIO_4_3
        }
        return AspectRatio.RATIO_16_9
    }

    companion object {
        private val TAG = MainActivity::class.java.simpleName
        private const val PERMISSION_CAMERA_REQUEST = 1
        private const val PERMISSION_STORAGE_REQUEST = 2
        private const val IMAGE_PICK_RESPONSE = 10

        private const val RATIO_4_3_VALUE = 4.0 / 3.0
        private const val RATIO_16_9_VALUE = 16.0 / 9.0
    }
}