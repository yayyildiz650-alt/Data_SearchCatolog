package com.example.data_searchcatolog

import android.graphics.Bitmap
import com.google.ai.client.generativeai.GenerativeModel
import com.google.ai.client.generativeai.type.content
import com.google.ai.client.generativeai.type.generationConfig
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Yapay zekadan gelecek analiz sonucunu karşılayacak modelimiz.
 * Room DB sorguların için bu verileri filtre olarak kullanacaksın.
 */
data class AiProductMatch(
    val brand: String? = null,
    val category: String? = null, // DB Search (English, e.g., 'smartphones')
    val categoryDisplay: String? = null, // UI Display (Turkish, e.g., 'Akıllı Telefonlar')
    val subCategory: String? = null,
    val subCategoryDisplay: String? = null,
    val color: String? = null, // DB Search (English, e.g., 'black', 'beige')
    val colorDisplay: String? = null, // UI Display (Turkish)
    val material: String? = null,
    val styleOrPattern: String? = null,
    val searchKeywords: List<String> = emptyList(), // English keywords for DB search (e.g., 'Samsung', 'Fold', '5G')
    val displayKeywords: List<String> = emptyList(), // Turkish keywords for UI chips
)

class AiProductAnalyzer {

    // 1. PROMPT: Gemini'yi yönlendirdiğimiz sistem komutu ve JSON şeması
    private val systemPrompt = """
        Sen uzman bir e-ticaret görsel analiz asistanısın. 
        Görseldeki ürünü incele ve veritabanı araması ile kullanıcı gösterimi için KESİNLİKLE şu JSON formatında yanıt ver.
        
        GEÇERLİ KATEGORİLER (Sadece bu listeden seç):
        [apparel, electronics, furniture, groceries, home-decoration, kitchen-accessories, laptops, mens-shirts, mens-shoes, mens-watches, mobile-accessories, motorcycle, smartphones, sports-accessories, sunglasses, tablets, vehicle]
        
        TALİMATLAR:
        1. 'category' alanı yukarıdaki listeden KESİNLİKLE İNGİLİZCE seçilmeli.
        2. 'color' alanı KESİNLİKLE İNGİLİZCE (black, white, red, blue, grey, beige, navy, green) olmalı.
        3. 'searchKeywords' listesi KESİNLİKLE İNGİLİZCE olmalı ve ürünün markasını, modelini veya belirgin özelliğini içermeli (örn: ["Logitech", "Mouse", "Wireless"]).
        4. 'Display' ile biten alanlar ve 'displayKeywords' listesi KESİNLİKLE TÜRKÇE olmalı.
        5. Yanıtı SADECE ham JSON olarak döndür.
        
        {
          "brand": "Marka",
          "category": "listedeki_ingilizce_kategori",
          "categoryDisplay": "Türkçe Kategori",
          "subCategory": "smartphone/mouse/chair vb.",
          "subCategoryDisplay": "Türkçe Alt Kategori",
          "color": "English color",
          "colorDisplay": "Türkçe Renk",
          "material": "Türkçe Materyal",
          "styleOrPattern": "Türkçe Tarz",
          "searchKeywords": ["English", "search", "terms"],
          "displayKeywords": ["Türkçe", "anahtar", "kelimeler"]
        }
    """.trimIndent()

    // 2. MODEL TANIMI: Kullanıcının belirttiği model ismini koruyoruz ve 400 hatasını önlemek için JSON zorlamasını kaldırıyoruz
    private val generativeModel = GenerativeModel(
        modelName = "gemini-3.5-flash",
        apiKey = BuildConfig.GEMINI_API_KEY,
        generationConfig = generationConfig {
            // maxOutputTokens limitini kaldırarak modelin kendi varsayılan limitini kullanmasını sağlıyoruz
        }
    )

    private val gson = Gson()

    /**
     * Gelen ham metni temizleyerek saf JSON haline getirir.
     */
    private fun sanitizeJson(jsonString: String): String {
        var clean = jsonString.trim()
            .removePrefix("```json")
            .removePrefix("```")
            .removeSuffix("```")
            .trim()

        val lastBrace = clean.lastIndexOf('}')
        if (lastBrace != -1) {
            clean = clean.substring(0, lastBrace + 1)
        }
        return clean
    }

    /**
     * Kameradan alınan Bitmap resmini analiz eder ve Room'da filtrelemek üzere
     * Result<AiProductMatch> döndürür.
     */
    suspend fun analyzeImage(bitmap: Bitmap): Result<AiProductMatch> = withContext(Dispatchers.IO) {
        try {
            // TALİMATLARI İSTEĞİN İÇİNE TAŞIDIK (Daha uyumlu yöntem)
            val inputContent = content {
                image(bitmap)
                text(systemPrompt)
            }

            val response = generativeModel.generateContent(inputContent)
            val rawJson = response.text

            android.util.Log.d("GeminiAI", "Ham Yanıt: ${rawJson ?: "BOŞ"}")

            if (rawJson.isNullOrBlank()) {
                return@withContext Result.failure(Exception("Yapay zekadan boş yanıt geldi."))
            }

            val cleanJson = sanitizeJson(rawJson)
            val matchResult = gson.fromJson(cleanJson, AiProductMatch::class.java)
            Result.success(matchResult)

        } catch (e: Exception) {
            android.util.Log.e("GeminiHata", "KRİTİK HATA: ${e.message}", e)
            Result.failure(e)
        }
    }
}