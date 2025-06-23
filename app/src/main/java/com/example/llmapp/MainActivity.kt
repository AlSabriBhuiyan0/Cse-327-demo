package com.example.llmapp

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.llmapp.databinding.ActivityMainBinding
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private lateinit var authHelper: GoogleAuthHelper
    private lateinit var m1Model: InstructionFollowingModel
    private lateinit var m2Model: GemmaMultimodalModel
    private lateinit var notificationHelper: NotificationHelper
    private var currentUser: GoogleSignInAccount? = null
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        authHelper = GoogleAuthHelper(this, this)
        notificationHelper = NotificationHelper(this)
        checkLoginStatus()
        
        // Initialize models (do this in background)
        lifecycleScope.launch {
            withContext(Dispatchers.IO) {
                m1Model = InstructionFollowingModel(applicationContext)
                m2Model = GemmaMultimodalModel(applicationContext)
            }
            
            withContext(Dispatchers.Main) {
                binding.modelProgress.visibility = View.GONE
                enableUI()
            }
        }
        
        setupUI()
        
        PermissionHelper.checkAllPermissions(this) { allGranted ->
            if (!allGranted) {
                Toast.makeText(this, getString(R.string.permissions_warning), Toast.LENGTH_LONG).show()
            }
        }
    }
    
    private fun checkLoginStatus() {
        val account = GoogleSignIn.getLastSignedInAccount(this)
        if (account != null) {
            currentUser = account
            updateUI(true)
        } else {
            updateUI(false)
        }
    }
    
    private fun setupUI() {
        binding.loginButton.setOnClickListener {
            authHelper.signIn(this, RC_SIGN_IN)
        }
        
        binding.logoutButton.setOnClickListener {
            authHelper.signOut {
                currentUser = null
                updateUI(false)
            }
        }
        
        binding.m1Button.setOnClickListener {
            runM1Model()
        }
        
        binding.m2Button.setOnClickListener {
            runM2Model()
        }
        
        binding.captureImageButton.setOnClickListener {
            checkPermissionsAndCaptureImage()
        }
    }
    
    private fun runM1Model() {
        val prompt = binding.inputText.text.toString()
        if (prompt.isNotEmpty()) {
            binding.m1Output.text = getString(R.string.processing)
            lifecycleScope.launch {
                val response = m1Model.generateResponse(prompt)
                binding.m1Output.text = response
                notificationHelper.showModelCompletionNotification("M1 Model", response)
            }
        } else {
            Toast.makeText(this, "Please enter a prompt", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun runM2Model() {
        val prompt = binding.inputText.text.toString()
        val image = binding.previewImage.drawable?.toBitmap()
        
        if (prompt.isNotEmpty() || image != null) {
            binding.m2Output.text = getString(R.string.processing)
            lifecycleScope.launch {
                val response = m2Model.generateResponse(prompt, image)
                binding.m2Output.text = response
                notificationHelper.showModelCompletionNotification("M2 Model", response)
            }
        } else {
            Toast.makeText(this, "Please enter a prompt or capture an image", Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun checkPermissionsAndCaptureImage() {
        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.CAMERA,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
            ).withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    if (report.areAllPermissionsGranted()) {
                        startCamera()
                    }
                }
                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            }).check()
    }
    
    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()
            
            val preview = Preview.Builder()
                .build()
                .also {
                    it.setSurfaceProvider(binding.previewView.surfaceProvider)
                }
            
            val imageCapture = ImageCapture.Builder()
                .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
                .build()
            
            val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA
            
            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this, cameraSelector, preview, imageCapture
                )
                
                binding.captureButton.setOnClickListener {
                    captureImage(imageCapture)
                }
                binding.captureButton.visibility = View.VISIBLE
            } catch(exc: Exception) {
                Log.e(TAG, "Use case binding failed", exc)
            }
        }, ContextCompat.getMainExecutor(this))
    }
    
    private fun captureImage(imageCapture: ImageCapture) {
        val outputFileOptions = ImageCapture.OutputFileOptions
            .Builder(File(filesDir, "temp_image.jpg"))
            .build()
        
        imageCapture.takePicture(
            outputFileOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    val savedUri = output.savedUri ?: Uri.fromFile(File(filesDir, "temp_image.jpg"))
                    binding.previewImage.setImageURI(savedUri)
                    binding.previewImage.visibility = View.VISIBLE
                }
                override fun onError(exc: ImageCaptureException) {
                    Log.e(TAG, "Photo capture failed: ${exc.message}", exc)
                }
            }
        )
    }
    
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        
        if (requestCode == RC_SIGN_IN) {
            val account = authHelper.handleSignInResult(data)
            if (account != null) {
                currentUser = account
                updateUI(true)
            }
        }
    }
    
    private fun updateUI(isSignedIn: Boolean) {
        if (isSignedIn) {
            binding.loginButton.visibility = View.GONE
            binding.logoutButton.visibility = View.VISIBLE
            binding.userEmail.text = currentUser?.email ?: "Logged in"
            binding.userEmail.visibility = View.VISIBLE
        } else {
            binding.loginButton.visibility = View.VISIBLE
            binding.logoutButton.visibility = View.GONE
            binding.userEmail.visibility = View.GONE
            disableUI()
        }
    }
    
    private fun enableUI() {
        binding.inputText.isEnabled = true
        binding.m1Button.isEnabled = true
        binding.m2Button.isEnabled = true
        binding.captureImageButton.isEnabled = true
    }
    
    private fun disableUI() {
        binding.inputText.isEnabled = false
        binding.m1Button.isEnabled = false
        binding.m2Button.isEnabled = false
        binding.captureImageButton.isEnabled = false
    }
    
    companion object {
        private const val TAG = "MainActivity"
        private const val RC_SIGN_IN = 9001
    }
}

// Extension function to convert Drawable to Bitmap
fun Drawable.toBitmap(): Bitmap? {
    return if (this is android.graphics.drawable.BitmapDrawable) {
        this.bitmap
    } else {
        null
    }
} 