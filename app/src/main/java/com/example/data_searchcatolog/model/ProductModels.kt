package com.example.data_searchcatolog.model

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

// 1. Retrofit için dış sarmalayıcı (data listesini karşılamak için)
data class ProductResponse(
    @SerializedName("data")
    val data: List<ProductModel>,
)

// 2. Room Tablomuz ve Ana Ürün Modelimiz
@Entity(tableName = "products")
data class ProductModel(
    @PrimaryKey
    @SerializedName("id")
    val id: Int,

    @SerializedName("sku")
    val sku: String?,

    @SerializedName("title")
    val title: String?,

    @SerializedName("description")
    val description: String?,

    @SerializedName("slug")
    val slug: String?,

    @SerializedName("brand")
    val brand: String?,

    @SerializedName("category")
    val category: String?,

    @SerializedName("subCategory")
    val subCategory: String?,

    @SerializedName("categoryName")
    val categoryName: String?,

    @SerializedName("color")
    val color: String?,

    @SerializedName("material")
    val material: String?,

    @SerializedName("attributes")
    val attributes: List<String>?,

    @SerializedName("unitPrice")
    val unitPrice: Double?,

    @androidx.room.Embedded(prefix = "price_")
    @SerializedName("price")
    val price: PriceModel?,

    @SerializedName("currency")
    val currency: String?,

    @SerializedName("rating")
    val rating: Double?,

    @SerializedName("reviewCount")
    val reviewCount: Int?,

    @SerializedName("stock")
    val stock: Int?,

    @SerializedName("inStock")
    val inStock: Boolean?,

    @SerializedName("availabilityStatus")
    val availabilityStatus: String?,

    @SerializedName("badges")
    val badges: List<String>?,

    @SerializedName("isNew")
    val isNew: Boolean?,

    @SerializedName("isBestSeller")
    val isBestSeller: Boolean?,

    @SerializedName("thumbnail")
    val thumbnail: String?,

    @SerializedName("images")
    val images: List<String>?,
)

// 3. Fiyat detaylarını tutan yardımcı model
data class PriceModel(
    @SerializedName("amount")
    val amount: Double?,

    @SerializedName("currency")
    val currency: String?,

    @SerializedName("formatted")
    val formatted: String?,

    @SerializedName("compareAt")
    val compareAt: Double?,

    @SerializedName("compareAtFormatted")
    val compareAtFormatted: String?,

    @SerializedName("discountPercentage")
    val discountPercentage: Double?,

    @SerializedName("savings")
    val savings: Double?,

    @SerializedName("savingsFormatted")
    val savingsFormatted: String?,
)
