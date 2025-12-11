package com.ebc.crud_notes.network

import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory

object ApiClient {
    //Esto sería cuando nuestro servicio está corriendo sobre local host
    //private const val BASE_URL = "http://10.0.2.2:8080"

    private const val BASE_URL = "https://loteriasvarias.onrender.com"

    val geetQuoteApi: GeekQuoteApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(ScalarsConverterFactory.create())
            .build()
            .create(GeekQuoteApi::class.java)
    }



}