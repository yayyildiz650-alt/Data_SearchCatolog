package com.example.data_searchcatolog.retrofit

import com.example.data_searchcatolog.model.ProductResponse
import retrofit2.Response
import retrofit2.http.GET

interface ProductApiService {

    @GET("Procuct_Data")
    suspend fun getProducts(): Response<ProductResponse>
}
