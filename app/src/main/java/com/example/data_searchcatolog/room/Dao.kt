package com.example.data_searchcatolog.room

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.data_searchcatolog.model.ProductModel

@Dao
interface ProductDao {

    // Ürünleri veritabanına ekle (Aynı id gelirse eskisinin üstüne yazar)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllProducts(products: List<ProductModel>)

    // Tüm ürünleri listele
    @Query("SELECT * FROM products")
    suspend fun getAllProducts(): List<ProductModel>

    // Başlığa veya markaya göre arama yap (Arama kataloğunun kalbi!)
    @Query("SELECT * FROM products WHERE title LIKE '%' || :searchQuery || '%' OR brand LIKE '%' || :searchQuery || '%'")
    suspend fun searchProducts(searchQuery: String): List<ProductModel>

    // Veritabanındaki toplam ürün sayısını getir
    @Query("SELECT COUNT(*) FROM products")
    suspend fun getProductCount(): Int

    // Tabloyu temizle (gerekirse yeni veri çekmeden önce)
    @Query("DELETE FROM products")
    suspend fun clearAll(): Int

    // Yapay zeka verileriyle benzer ürünleri filtrele (Gelişmiş Stratejik Sorgu)
    @Query("""
        SELECT * FROM products 
        WHERE 
            (
                -- 1. Kategori ve Marka eşleşmesi (En güçlü filtre)
                (:category IS NOT NULL AND (category LIKE '%' || :category || '%' OR categoryName LIKE '%' || :category || '%'))
                AND (:brand IS NOT NULL AND (brand LIKE '%' || :brand || '%' OR title LIKE '%' || :brand || '%'))
            )
            OR
            (
                -- 2. Kategori ve Renk eşleşmesi
                (:category IS NOT NULL AND (category LIKE '%' || :category || '%' OR categoryName LIKE '%' || :category || '%'))
                AND (:color IS NOT NULL AND color LIKE '%' || :color || '%')
            )
            OR
            (
                -- 3. Marka ve Alt Kategori eşleşmesi
                (:brand IS NOT NULL AND (brand LIKE '%' || :brand || '%' OR title LIKE '%' || :brand || '%'))
                AND (:subCategory IS NOT NULL AND (subCategory LIKE '%' || :subCategory || '%' OR title LIKE '%' || :subCategory || '%'))
            )
            OR
            (
                -- 4. Materyal ve Kategori eşleşmesi
                (:material IS NOT NULL AND (title LIKE '%' || :material || '%' OR description LIKE '%' || :material || '%'))
                AND (:category IS NOT NULL AND (category LIKE '%' || :category || '%' OR categoryName LIKE '%' || :category || '%'))
            )
        ORDER BY 
            (CASE WHEN brand LIKE '%' || :brand || '%' THEN 2 ELSE 0 END) +
            (CASE WHEN category LIKE '%' || :category || '%' THEN 1 ELSE 0 END) DESC
        LIMIT 20
    """)
    suspend fun findSimilarProducts(
        category: String?,
        subCategory: String?,
        color: String?,
        material: String?,
        brand: String?
    ): List<ProductModel>

    // Anahtar kelimelere göre çoklu arama
    @Query("""
        SELECT * FROM products 
        WHERE title LIKE '%' || :keyword || '%' 
           OR description LIKE '%' || :keyword || '%'
           OR category LIKE '%' || :keyword || '%'
        LIMIT 10
    """)
    suspend fun searchByKeyword(keyword: String): List<ProductModel>
}
