package com.example.data_searchcatolog

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data_searchcatolog.ui.components.ImageSourcePicker

@Composable
fun HomeScreen(
    onCameraClick: () -> Unit,
    onGalleryClick: () -> Unit,
    onImageSelected: (android.net.Uri) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }

    // =========================================================================
    // 1. ÖZEL AI GRADIENT ARKA PLAN RENKLERİ
    // =========================================================================
    val backgroundGradient = Brush.verticalGradient(
        colors = listOf(
            Color(0xFF2D1B69), // Koyu Viyola
            Color(0xFF3A1C71), // Derin Mor
            Color(0xFFD76D77), // Sıcak Mercan
        )
    )

    val buttonGradient = Brush.horizontalGradient(
        colors = listOf(Color(0xFF6A11CB), Color(0xFF2575FC))
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = backgroundGradient)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 40.dp), // Alt tarafa nefes aldıracak boşluğu ekledik
            verticalArrangement = Arrangement.SpaceEvenly,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // =====================================================================
            // 2. HEADER / KARŞILAMA ALANI
            // =====================================================================
            Spacer(modifier = Modifier.height(48.dp)) // 64 -> 48

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp)
            ) {
                Text(
                    text = "Merhaba 👋",
                    fontSize = 16.sp, // 18 -> 16
                    color = Color.White.copy(alpha = 0.8f),
                    fontWeight = FontWeight.Medium
                )
                Text(
                    text = "AI Katalog Asistanı",
                    fontSize = 28.sp, // 32 -> 28
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                    lineHeight = 34.sp
                )
                Text(
                    text = "Görsel zeka ile aradığın ürünü saniyeler içinde bul.",
                    fontSize = 14.sp, // 15 -> 14
                    color = Color.White.copy(alpha = 0.7f),
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp)) // 32 -> 24

            // =====================================================================
            // 3. ÖNE ÇIKAN ÖZELLİKLER (BADGES)
            // =====================================================================
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                FeatureBadge(
                    icon = Icons.Default.Inventory2,
                    text = "1000+ Ürün",
                    modifier = Modifier.weight(1f)
                )
                FeatureBadge(
                    icon = Icons.Default.Bolt,
                    text = "Hızlı Analiz",
                    modifier = Modifier.weight(1f)
                )
                FeatureBadge(
                    icon = Icons.Default.Psychology,
                    text = "Akıllı Eşleşme",
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp)) // 40 -> 24

            // =====================================================================
            // 4. MERKEZİ GLASSMORPHISM AKSİYON KARTI
            // =====================================================================
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 24.dp)
                    .shadow(24.dp, RoundedCornerShape(32.dp), spotColor = Color.Black.copy(alpha = 0.2f)),
                shape = RoundedCornerShape(32.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.5f))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 24.dp, vertical = 20.dp), // 28 -> 20/24
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(64.dp) // 72 -> 64
                            .clip(CircleShape)
                            .background(Color(0xFF6A11CB).copy(alpha = 0.1f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.AutoAwesome,
                            contentDescription = null,
                            modifier = Modifier.size(32.dp),
                            tint = Color(0xFF6A11CB)
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp)) // 20 -> 12

                    Text(
                        text = "Ürün Araması Başlat",
                        fontSize = 20.sp, // 22 -> 20
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(0xFF1E1E2C)
                    )

                    Spacer(modifier = Modifier.height(8.dp)) // 12 -> 8

                    Text(
                        text = "Fotoğrafını çekin veya galeriden yükleyin, yapay zeka veritabanını tarasın.",
                        fontSize = 13.sp, // 14 -> 13
                        textAlign = TextAlign.Center,
                        color = Color.Gray,
                        lineHeight = 18.sp
                    )

                    Spacer(modifier = Modifier.height(24.dp)) // 32 -> 24

                    Button(
                        onClick = { showPicker = true },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp) // 60 -> 54
                            .shadow(8.dp, RoundedCornerShape(18.dp)),
                        shape = RoundedCornerShape(18.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                        contentPadding = PaddingValues()
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(buttonGradient),
                            contentAlignment = Alignment.Center
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.PhotoCamera, null, tint = Color.White, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(10.dp))
                                Text("ŞİMDİ TARA", fontWeight = FontWeight.Bold, fontSize = 15.sp, letterSpacing = 1.sp)
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp)) // 40 -> 24

            // =====================================================================
            // 5. NASIL ÇALIŞIR BÖLÜMÜ
            // =====================================================================
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 28.dp)
            ) {
                Text(
                    text = "Nasıl Çalışır?",
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
                Spacer(modifier = Modifier.height(12.dp)) // Tekrar 12'ye çektim, SpaceEvenly halledecek

                WorkStep(number = "1", title = "Görsel Seç", desc = "Ürünün net bir fotoğrafını çekin.")
                WorkStep(number = "2", title = "AI Analizi", desc = "Yapay zeka özellikleri saniyeler içinde çıkarsın.")
                WorkStep(number = "3", title = "Sonuçları Gör", desc = "Veritabanındaki en benzer ürünleri incele.")
            }
        }
    }

    if (showPicker) {
        ImageSourcePicker(
            onDismissRequest = { showPicker = false },
            onCameraClick = {
                showPicker = false
                onCameraClick()
            },
            onGalleryClick = {
                showPicker = false
                onGalleryClick()
            }
        )
    }
}

@Composable
fun FeatureBadge(icon: ImageVector, text: String, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = Color.White.copy(alpha = 0.15f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, Color.White.copy(alpha = 0.2f))
    ) {
        Column(
            modifier = Modifier.padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.height(4.dp))
            Text(text = text, fontSize = 10.sp, color = Color.White, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }
}

@Composable
fun WorkStep(number: String, title: String, desc: String) {
    Row(
        modifier = Modifier.padding(vertical = 16.dp), // 14 -> 16 (Daha dolgun dursun)
        verticalAlignment = Alignment.Top
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(Color.White.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            Text(text = number, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
        Spacer(modifier = Modifier.width(16.dp))
        Column {
            Text(text = title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(text = desc, color = Color.White.copy(alpha = 0.6f), fontSize = 13.sp)
        }
    }
}
