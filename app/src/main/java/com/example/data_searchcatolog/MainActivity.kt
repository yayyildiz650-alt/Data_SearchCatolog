package com.example.data_searchcatolog

import android.Manifest
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.data_searchcatolog.retrofit.RetrofitClient
import com.example.data_searchcatolog.room.AppDatabase
import com.example.data_searchcatolog.ui.screens.CameraScreen
import com.example.data_searchcatolog.ui.screens.ResultScreen
import com.example.data_searchcatolog.ui.theme.DataSearchCatologTheme
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DataSearchCatologTheme {
                var currentScreen by rememberSaveable { mutableStateOf("home") }
                var capturedImageUri by rememberSaveable { mutableStateOf<Uri?>(null) }
                
                // İzin Diyaloğu State'leri
                var showCameraRationale by rememberSaveable { mutableStateOf(false) }
                var showGalleryRationale by rememberSaveable { mutableStateOf(false) }
                var showSettingsDialog by rememberSaveable { mutableStateOf(false) }

                val cameraPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    Log.d("MainActivity", "Kamera İzni Sonucu: $isGranted")
                    if (isGranted) {
                        Log.d("MainActivity", "Kamera İzni Verildi -> Ekrana Geçiliyor")
                        currentScreen = "camera"
                    } else {
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA)) {
                            showSettingsDialog = true
                        } else {
                            showCameraRationale = true
                        }
                    }
                }

                val galleryLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.PickVisualMedia()
                ) { uri ->
                    Log.d("MainActivity", "Galeri Seçim Sonucu: $uri")
                    uri?.let {
                        capturedImageUri = it
                        currentScreen = "result"
                    }
                }

                val galleryPermissionLauncher = rememberLauncherForActivityResult(
                    contract = ActivityResultContracts.RequestPermission()
                ) { isGranted ->
                    Log.d("MainActivity", "Galeri İzni Sonucu: $isGranted")
                    if (isGranted) {
                        Log.d("MainActivity", "Galeri İzni Verildi -> Galeri Açılıyor")
                        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                    } else {
                        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                            Manifest.permission.READ_MEDIA_IMAGES
                        } else {
                            Manifest.permission.READ_EXTERNAL_STORAGE
                        }
                        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                            showSettingsDialog = true
                        } else {
                            showGalleryRationale = true
                        }
                    }
                }

                when (currentScreen) {
                    "home" -> {
                        HomeScreen(
                            onCameraClick = {
                                Log.d("MainActivity", "HomeScreen: Kamera Tıklandı")
                                when {
                                    ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED -> {
                                        currentScreen = "camera"
                                    }
                                    ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.CAMERA) -> {
                                        showCameraRationale = true
                                    }
                                    else -> {
                                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                                    }
                                }
                            },
                            onGalleryClick = {
                                Log.d("MainActivity", "HomeScreen: Galeri Tıklandı")
                                val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                    Manifest.permission.READ_MEDIA_IMAGES
                                } else {
                                    Manifest.permission.READ_EXTERNAL_STORAGE
                                }
                                
                                when {
                                    ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED -> {
                                        Log.d("MainActivity", "İzin zaten var -> Galeri açılıyor")
                                        galleryLauncher.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                                    }
                                    ActivityCompat.shouldShowRequestPermissionRationale(this, permission) -> {
                                        showGalleryRationale = true
                                    }
                                    else -> {
                                        galleryPermissionLauncher.launch(permission)
                                    }
                                }
                            },
                            onImageSelected = { uri ->
                                capturedImageUri = uri
                                currentScreen = "result"
                            }
                        )
                    }
                    "camera" -> {
                        BackHandler {
                            capturedImageUri = null
                            currentScreen = "home"
                        }
                        CameraScreen(
                            onImageCaptured = { uri ->
                                capturedImageUri = uri
                                currentScreen = "result"
                                Log.d("MainActivity", "Fotoğraf çekildi: $uri")
                            },
                            onClose = {
                                capturedImageUri = null
                                currentScreen = "home"
                            }
                        )
                    }
                    "result" -> {
                        BackHandler {
                            capturedImageUri = null
                            currentScreen = "home"
                        }
                        ResultScreen(
                            imageUri = capturedImageUri,
                            onBack = { 
                                capturedImageUri = null
                                currentScreen = "home" 
                            }
                        )
                    }
                }

                // İzin Diyalogları
                if (showCameraRationale) {
                    PermissionDialog(
                        title = "Kamera İzni Gerekli",
                        description = "Ürün fotoğrafı çekebilmek için kamera iznine ihtiyacımız var.",
                        onConfirm = {
                            showCameraRationale = false
                            cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                        },
                        onDismiss = { showCameraRationale = false }
                    )
                }

                if (showGalleryRationale) {
                    PermissionDialog(
                        title = "Galeri İzni Gerekli",
                        description = "Galeriden ürün seçebilmek için dosya erişim iznine ihtiyacımız var.",
                        onConfirm = {
                            showGalleryRationale = false
                            val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                Manifest.permission.READ_MEDIA_IMAGES
                            } else {
                                Manifest.permission.READ_EXTERNAL_STORAGE
                            }
                            galleryPermissionLauncher.launch(permission)
                        },
                        onDismiss = { showGalleryRationale = false }
                    )
                }

                if (showSettingsDialog) {
                    PermissionDialog(
                        title = "İzinler Devre Dışı",
                        description = "Görünüşe göre izinleri kalıcı olarak reddettiniz. Lütfen ayarlardan manuel olarak etkinleştirin.",
                        confirmText = "Ayarlara Git",
                        onConfirm = {
                            showSettingsDialog = false
                            openAppSettings()
                        },
                        onDismiss = { showSettingsDialog = false }
                    )
                }
            }
        }

        testDatabase()
    }

    private fun openAppSettings() {
        val intent = android.content.Intent(
            android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
            Uri.fromParts("package", packageName, null)
        )
        startActivity(intent)
    }

    private fun testDatabase() {
        lifecycleScope.launch {
            try {
                val database = AppDatabase.getDatabase(applicationContext)
                val productDao = database.productDao()
                val response = RetrofitClient.apiService.getProducts()
                if (response.isSuccessful && response.body() != null) {
                    productDao.insertAllProducts(response.body()!!.data)
                    Log.d("RoomTest", "Ürünler başarıyla kaydedildi")
                }
            } catch (e: Exception) {
                Log.e("RoomTest", "Hata: ${e.localizedMessage}")
            }
        }
    }
}

@Composable
fun PermissionDialog(
    title: String,
    description: String,
    confirmText: String = "Tekrar Dene",
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = title) },
        text = { Text(text = description) },
        confirmButton = {
            Button(onClick = onConfirm) {
                Text(text = confirmText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "İptal")
            }
        }
    )
}

@Preview(showBackground = true)
@Composable
fun SearchPreview() {
    DataSearchCatologTheme {
        HomeScreen(onCameraClick = {}, onGalleryClick = {}, onImageSelected = {})
    }
}