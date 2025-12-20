package com.ebc.crud_notes.network

import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.converter.gson.GsonConverterFactory

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

    private const val BANXICO_BASE_URL = "https://www.banxico.org.mx/"

    val banxicoApi: BanxicoApi by lazy {
        Retrofit.Builder()
            .baseUrl(BANXICO_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(BanxicoApi::class.java)
    }



}