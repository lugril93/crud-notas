package com.ebc.crud_notes.network

import retrofit2.http.GET

interface GeekQuoteApi {

    @GET("quotes")
    suspend fun getRandomQuote (): String
}