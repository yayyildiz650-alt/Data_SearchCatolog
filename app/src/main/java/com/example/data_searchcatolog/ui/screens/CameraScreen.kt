package com.example.data_searchcatolog.ui.screens

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import java.io.File
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import androidx.compose.material.icons.filled.FlashAuto
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn

import androidx.camera.core.Camera

@Composable
fun CameraScreen(
    onImageCaptured: (Uri) -> Unit,
    onClose: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val cameraExecutor: ExecutorService = remember { Executors.newSingleThreadExecutor() }
    
    // Kamera ve Kontrol Nesneleri
    var camera by remember { mutableStateOf<Camera?>(null) }
    
    // Flaş Durumu (Varsayılan: Kapalı)
    var flashMode by remember { mutableIntStateOf(ImageCapture.FLASH_MODE_OFF) }
    
    val imageCapture: ImageCapture = remember { 
        ImageCapture.Builder()
            .setFlashMode(flashMode)
            .build() 
    }
    
    // Flaş veya Fener (Torch) Değişikliğini Uygula
    LaunchedEffect(flashMode, camera) {
        imageCapture.flashMode = flashMode
        camera?.cameraControl?.enableTorch(flashMode == ImageCapture.FLASH_MODE_ON)
    }

    var isCameraReady by remember { mutableStateOf(false) }
    
    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        AndroidView(
            factory = { ctx ->
                Log.d("CameraScreen", "PreviewView factory çağrıldı")
                PreviewView(ctx).apply {
                    scaleType = PreviewView.ScaleType.FILL_CENTER
                    implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                }
            },
            modifier = Modifier.fillMaxSize(),
            update = { previewView ->
                val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
                cameraProviderFuture.addListener({
                    val cameraProvider = cameraProviderFuture.get()
                    val preview = Preview.Builder().build().also {
                        it.surfaceProvider = previewView.surfaceProvider
                    }
                    
                    try {
                        cameraProvider.unbindAll()
                        // Kamerayı değişkene atıyoruz ki flaşı kontrol edebilelim
                        val boundCamera = cameraProvider.bindToLifecycle(
                            lifecycleOwner,
                            CameraSelector.DEFAULT_BACK_CAMERA,
                            preview,
                            imageCapture
                        )
                        camera = boundCamera
                        Log.d("CameraScreen", "Kamera başarıyla bağlandı")
                        isCameraReady = true
                    } catch (e: Exception) {
                        Log.e("CameraScreen", "Kamera bağlama hatası", e)
                    }
                }, ContextCompat.getMainExecutor(context))
            }
        )
        
        if (!isCameraReady) {
            CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center),
                color = Color.White
            )
        }
        
        // Üst Kontroller (Kapat ve Flaş)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Kapat Butonu
            IconButton(onClick = onClose) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Kapat",
                    tint = Color.White,
                    modifier = Modifier.size(32.dp)
                )
            }
            
            // Flaş Butonu
            IconButton(
                onClick = {
                    flashMode = when (flashMode) {
                        ImageCapture.FLASH_MODE_OFF -> ImageCapture.FLASH_MODE_ON
                        ImageCapture.FLASH_MODE_ON -> ImageCapture.FLASH_MODE_AUTO
                        else -> ImageCapture.FLASH_MODE_OFF
                    }
                }
            ) {
                val (icon, tint) = when (flashMode) {
                    ImageCapture.FLASH_MODE_ON -> Icons.Default.FlashOn to Color.Yellow
                    ImageCapture.FLASH_MODE_AUTO -> Icons.Default.FlashAuto to Color.White
                    else -> Icons.Default.FlashOff to Color.White.copy(alpha = 0.6f)
                }
                Icon(
                    imageVector = icon,
                    contentDescription = "Flaş Modu",
                    tint = tint,
                    modifier = Modifier.size(32.dp)
                )
            }
        }
        
        // Çekme Butonu
        Box(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 64.dp)
        ) {
            Button(
                onClick = {
                    if (isCameraReady) {
                        Log.d("CameraScreen", "Fotoğraf çekme işlemi başlatıldı")
                        takePhoto(context, imageCapture, cameraExecutor, onImageCaptured)
                    }
                },
                modifier = Modifier
                    .size(84.dp)
                    .border(4.dp, Color.White, CircleShape),
                shape = CircleShape,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (isCameraReady) Color.White.copy(alpha = 0.5f) else Color.Gray
                )
            ) { }
        }
    }
    
    DisposableEffect(Unit) {
        onDispose {
            cameraExecutor.shutdown()
        }
    }
}

private fun takePhoto(
    context: Context,
    imageCapture: ImageCapture,
    executor: ExecutorService,
    onImageCaptured: (Uri) -> Unit
) {
    val photoFile = File(
        context.externalCacheDir,
        SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US).format(System.currentTimeMillis()) + ".jpg"
    )
    
    val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()
    
    imageCapture.takePicture(
        outputOptions,
        executor,
        object : ImageCapture.OnImageSavedCallback {
            override fun onImageSaved(outputFileResults: ImageCapture.OutputFileResults) {
                val savedUri = Uri.fromFile(photoFile)
                Log.d("CameraScreen", "Fotoğraf kaydedildi: $savedUri")
                onImageCaptured(savedUri)
            }
            
            override fun onError(exception: ImageCaptureException) {
                Log.e("CameraScreen", "Fotoğraf çekme hatası", exception)
            }
        }
    )
}