package com.example.data_searchcatolog.room

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/*
 * =====================================================================
 * CONVERTERS (TİP DÖNÜŞTÜRÜCÜ) ÖZETİ
 * =====================================================================
 *
 * 1. SORUN (SQLite'ın Sınırı):
 *    - Room/SQLite yalnızca temel veri tiplerini (String, Int, Double, Boolean)
 *      tablo hücrelerine kaydedebilir.
 *    - Bizim modelimizdeki karmaşık tipleri (List<String>, Price_Model objesi vb.)
 *      doğrudan bir hücreye koyamaz.
 *
 * 2. ÇÖZÜM (Converters'ın Görevi):
 *    - KAYDEDERKEN  (from...): Listeyi veya objeyi JSON formatında tek parça
 *                              bir METNE (String) çevirip veritabanına yazar.
 *    - OKURKEN      (to...):   Veritabanındaki o JSON metnini okuyup tekrar
 *                              Kotlin'deki asıl nesnemize (List veya Obje) dönüştürür.
 * =====================================================================
 */
class Converters {
    private val gson = Gson()

    @TypeConverter
    fun fromStringList(list: List<String>?): String? {
        return list?.let { gson.toJson(it) }
    }

    @TypeConverter
    fun toStringList(data: String?): List<String>? {
        if (data.isNullOrEmpty()) return emptyList()
        val listType = object : TypeToken<List<String>>() {}.type
        return gson.fromJson(data, listType)
    }
}
