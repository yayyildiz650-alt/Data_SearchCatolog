package com.example.data_searchcatolog.ui.screens

import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Label
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.compose.SubcomposeAsyncImage
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.graphics.drawscope.clipRect
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.platform.LocalDensity
import com.example.data_searchcatolog.AiProductAnalyzer
import com.example.data_searchcatolog.AiProductMatch
import com.example.data_searchcatolog.model.ProductModel
import com.example.data_searchcatolog.room.AppDatabase

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    imageUri: Uri?,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val analyzer = remember { AiProductAnalyzer() }
    val database = remember { AppDatabase.getDatabase(context) }
    
    var analysisResult by remember { mutableStateOf<AiProductMatch?>(null) }
    var similarProducts by remember { mutableStateOf<List<ProductModel>>(emptyList()) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var retryTrigger by remember { mutableStateOf(0) }

    // Arka plan gradyanı
    val backgroundGradient = Brush.linearGradient(
        colors = listOf(
            Color(0xFF3A1C71),
            Color(0xFFD76D77),
            Color(0xFF283C86)
        )
    )

    // Görseli analize gönder
    LaunchedEffect(imageUri, retryTrigger) {
        if (imageUri != null && analysisResult == null && !isLoading) {
            isLoading = true
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    val source = ImageDecoder.createSource(context.contentResolver, imageUri)
                    ImageDecoder.decodeBitmap(source) { decoder, _, _ ->
                        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                        decoder.isMutableRequired = true
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, imageUri)
                }
                
                val result = analyzer.analyzeImage(bitmap)
                result.onSuccess { match ->
                    analysisResult = match
                    var products = database.productDao().findSimilarProducts(
                        category = match.category,
                        subCategory = match.subCategory,
                        color = match.color,
                        material = match.material,
                        brand = if (match.brand != "Bilinmiyor") match.brand else null
                    )
                    
                    if (products.isEmpty() && match.searchKeywords.isNotEmpty()) {
                        val keywordProducts = mutableListOf<ProductModel>()
                        match.searchKeywords.take(3).forEach { key ->
                            val found = database.productDao().searchByKeyword(key)
                            keywordProducts.addAll(found)
                        }
                        products = keywordProducts.distinctBy { it.id }
                    }
                    similarProducts = products
                }.onFailure {
                    errorMessage = it.message ?: "Bilinmeyen bir hata oluştu"
                }
            } catch (e: Exception) {
                errorMessage = "Görsel işleme hatası: ${e.localizedMessage}"
            } finally {
                isLoading = false
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundGradient)
    ) {
        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                CenterAlignedTopAppBar(
                    title = { 
                        Text("Analiz Sonucu", fontWeight = FontWeight.Bold, color = Color.White) 
                    },
                    colors = TopAppBarDefaults.centerAlignedTopAppBarColors(containerColor = Color.Transparent)
                )
            }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Görsel Önizleme + Tarama Animasyonu
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(260.dp)
                        .clip(RoundedCornerShape(24.dp))
                ) {
                    Card(
                        modifier = Modifier.fillMaxSize(),
                        elevation = CardDefaults.cardElevation(12.dp),
                        shape = RoundedCornerShape(24.dp)
                    ) {
                        if (imageUri != null) {
                            AsyncImage(
                                model = imageUri,
                                contentDescription = "Seçilen Görsel",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Crop
                            )
                        }
                    }
                    
                    // Lazer Tarama Animasyonu (Sadece yüklenirken)
                    if (isLoading) {
                        ScanningOverlay()
                    }
                }
                
                Spacer(modifier = Modifier.height(12.dp))

                // Glassmorphism Bilgi Kartı
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(28.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f)),
                    elevation = CardDefaults.cardElevation(8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    ) {
                        if (isLoading) {
                            ProfessionalLoadingView()
                        } else if (errorMessage != null) {
                            val friendlyMessage = when {
                                errorMessage!!.contains("503") || errorMessage!!.contains("high demand") -> 
                                    "Yapay zeka şu an çok yoğun. Lütfen birkaç saniye bekleyip tekrar dene."
                                errorMessage!!.contains("429") ->
                                    "Çok fazla istek gönderildi. Lütfen bir dakika bekleyin."
                                errorMessage!!.contains("Network") || errorMessage!!.contains("Unable to resolve host") ->
                                    "İnternet bağlantısı kurulamadı. Lütfen bağlantınızı kontrol edin."
                                else -> "Analiz sırasında bir sorun oluştu: ${errorMessage!!.take(50)}..."
                            }
                            ErrorView(friendlyMessage, onRetry = { 
                                errorMessage = null
                                analysisResult = null
                                retryTrigger++
                            })
                        } else if (analysisResult != null) {
                            ResultContent(analysisResult!!)
                        }
                    }
                }
                
                // Benzer Ürünler Listesi
                if (analysisResult != null && !isLoading) {
                    Spacer(modifier = Modifier.height(16.dp))
                    SimilarProductsHeader()
                    
                    if (similarProducts.isNotEmpty()) {
                        LazyRow(
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 4.dp),
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(similarProducts) { product ->
                                SimilarProductCard(product)
                            }
                        }
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(100.dp)
                                .clip(RoundedCornerShape(24.dp))
                                .background(Color.White.copy(alpha = 0.15f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                "Uygun benzer ürün bulunamadı.", 
                                color = Color.White.copy(alpha = 0.7f),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                    }
                }
                
                Spacer(modifier = Modifier.height(40.dp))
            }
        }
    }
}

@Composable
fun SimilarProductCard(product: ProductModel) {
    // Log ekleyerek linki kontrol edelim
    LaunchedEffect(product.id) {
        android.util.Log.d("SimilarProducts", "Ürün: ${product.title}, Resim: ${product.thumbnail}")
    }

    Card(
        modifier = Modifier
            .width(160.dp)
            .height(280.dp) // Sabit yükseklik ile tüm kartlar aynı hizada olur
            .padding(vertical = 4.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(4.dp)
    ) {
        Column {
            SubcomposeAsyncImage(
                model = product.thumbnail ?: product.images?.firstOrNull(),
                contentDescription = product.title,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp) // Görsel yüksekliği sabitlendi
                    .clip(RoundedCornerShape(topStart = 20.dp, topEnd = 20.dp)),
                contentScale = ContentScale.Fit, // Ürünün tamamını görmek için Fit daha iyidir
                loading = {
                    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(modifier = Modifier.size(24.dp), strokeWidth = 2.dp)
                    }
                },
                error = {
                    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFF1F1F1)), contentAlignment = Alignment.Center) {
                        Icon(Icons.Default.ImageNotSupported, contentDescription = null, tint = Color.LightGray)
                    }
                }
            )
            
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp),
                verticalArrangement = Arrangement.SpaceBetween // Yazı ve fiyatı uçlara yayar
            ) {
                Text(
                    text = product.title ?: "İsimsiz Ürün",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    maxLines = 2,
                    minLines = 2, // 1 satırlık isimlerde de aynı boşluğu bırakır
                    overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
                    color = Color(0xFF1E1E2C),
                    lineHeight = 18.sp
                )
                
                Text(
                    text = product.price?.formatted ?: "",
                    style = MaterialTheme.typography.titleMedium,
                    color = Color(0xFF5B16D0),
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
fun SimilarProductsHeader() {
    Row(
        modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(Icons.Default.Compare, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "Eşleşen Benzer Ürünler",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun ScanningOverlay() {
    val infiniteTransition = rememberInfiniteTransition(label = "Scanner")
    val lineY by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LinePosition"
    )

    val density = LocalDensity.current
    val strokeWidthPx = with(density) { 4.dp.toPx() }

    Canvas(modifier = Modifier.fillMaxSize()) {
        val height = size.height
        val width = size.width
        val currentY = height * lineY

        clipRect {
            // Lazer Çizgisi
            drawLine(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF6A11CB).copy(alpha = 0f),
                        Color(0xFF2575FC).copy(alpha = 0.8f),
                        Color(0xFF6A11CB).copy(alpha = 0f)
                    ),
                    startY = currentY - 40,
                    endY = currentY + 40
                ),
                start = Offset(0f, currentY),
                end = Offset(width, currentY),
                strokeWidth = strokeWidthPx
            )
            
            // Üst Karartma
            drawRect(
                color = Color(0xFF2575FC).copy(alpha = 0.1f),
                size = Size(width, currentY)
            )
        }
    }
}

@Composable
fun ProfessionalLoadingView() {
    val infiniteTransition = rememberInfiniteTransition(label = "Loading")
    
    // AI Yıldızı Dönüşü
    val rotation by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(3000, easing = LinearEasing)
        ),
        label = "Rotation"
    )

    // Metin Parlaması
    val textAlpha by infiniteTransition.animateFloat(
        initialValue = 0.3f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "Alpha"
    )

    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 40.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(contentAlignment = Alignment.Center) {
            // Dönen Dış Halka
            CircularProgressIndicator(
                modifier = Modifier.size(80.dp),
                color = Color(0xFF6A11CB).copy(alpha = 0.2f),
                strokeWidth = 2.dp
            )
            // AI İkonu
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                modifier = Modifier
                    .size(40.dp)
                    .rotate(rotation),
                tint = Color(0xFF6A11CB)
            )
        }
        
        Spacer(modifier = Modifier.height(24.dp))
        
        Text(
            text = "Yapay Zeka İnceliyor",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF1E1E2C),
            modifier = Modifier.alpha(textAlpha)
        )
        Text(
            text = "Görseldeki detaylar analiz ediliyor...",
            style = MaterialTheme.typography.bodySmall,
            color = Color.Gray,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}


@Composable
fun ErrorView(message: String, onRetry: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            imageVector = Icons.Default.ErrorOutline, 
            contentDescription = null, 
            tint = Color.Red, 
            modifier = Modifier.size(48.dp)
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(text = message, color = Color.Red, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
        Button(
            onClick = onRetry,
            modifier = Modifier.padding(top = 16.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color.Red)
        ) {
            Text("Tekrar Dene", color = Color.White)
        }
    }
}


@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ResultContent(match: AiProductMatch) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(bottom = 12.dp)
        ) {
            Icon(Icons.Default.AutoAwesome, contentDescription = null, tint = Color(0xFF5B16D0), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = "Ürün Özellikleri",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E2C)
            )
        }

        // Özellikleri 2'li Kolon Halinde Göster (Kompakt Grid)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            maxItemsInEachRow = 2,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // FlowRow içinde weight çalışmaz, bu yüzden yaklaşık %50 genişlik veriyoruz
            val itemModifier = Modifier.fillMaxWidth(0.485f)
            
            InfoCard(label = "Marka", value = match.brand, icon = Icons.Default.Business, modifier = itemModifier)
            InfoCard(label = "Kategori", value = match.categoryDisplay ?: match.category, icon = Icons.Default.Category, modifier = itemModifier)
            InfoCard(label = "Alt Kategori", value = match.subCategoryDisplay ?: match.subCategory, icon = Icons.AutoMirrored.Filled.Label, modifier = itemModifier)
            InfoCard(label = "Renk", value = match.colorDisplay ?: match.color, icon = Icons.Default.Palette, modifier = itemModifier)
            InfoCard(label = "Materyal", value = match.material, icon = Icons.Default.Category, modifier = itemModifier)
            InfoCard(label = "Stil/Desen", value = match.styleOrPattern, icon = Icons.Default.Brush, modifier = itemModifier)
        }

        if (match.displayKeywords.isNotEmpty()) {
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "Anahtar Kelimeler", 
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF1E1E2C)
            )
            FlowRow(
                modifier = Modifier.padding(top = 10.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                match.displayKeywords.take(6).forEach { keyword ->
                    Surface(
                        color = Color(0xFF5B16D0).copy(alpha = 0.08f),
                        shape = RoundedCornerShape(12.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF5B16D0).copy(alpha = 0.2f))
                    ) {
                        Text(
                            text = keyword,
                            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color(0xFF5B16D0),
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun InfoCard(label: String, value: String?, icon: ImageVector, modifier: Modifier = Modifier) {
    if (!value.isNullOrBlank()) {
        Surface(
            modifier = modifier,
            color = Color(0xFFF8F9FA),
            shape = RoundedCornerShape(16.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE9ECEF))
        ) {
            Row(
                modifier = Modifier.padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(30.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0xFF5B16D0).copy(alpha = 0.08f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp),
                        tint = Color(0xFF5B16D0)
                    )
                }
                
                Spacer(modifier = Modifier.width(10.dp))
                
                Column {
                    Text(
                        text = label, 
                        style = MaterialTheme.typography.labelSmall, 
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                    Text(
                        text = value, 
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = if (value == "Bilinmiyor") Color.Gray else Color(0xFF1E1E2C),
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                }
            }
        }
    } else {
        // Boşluk dengesi için görünmez ama yer kaplayan kutu
        Spacer(modifier = modifier.height(50.dp))
    }
}

