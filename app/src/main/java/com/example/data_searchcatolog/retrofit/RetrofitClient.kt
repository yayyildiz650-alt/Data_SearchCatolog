package com.example.data_searchcatolog.retrofit

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // GitHub Raw veya veriyi barındırdığın kök (base) URL
    // Örn: "https://raw.githubusercontent.com/kullaniciadi/repo-adi/main/"

    private const val BASE_URL = "https://raw.githubusercontent.com/yayyildiz650-alt/Catolog_Data/main/"
    val apiService: ProductApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ProductApiService::class.java)
    }
}