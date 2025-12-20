package com.ebc.crud_notes.network


import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface BanxicoApi {
@GET("SieAPIRest/service/v1/series/{idSerie}/datos/oportuno")
suspend fun getDatoOportuno(
    @Path("idSerie") idSerie: String = "SF43718",
    @Header("Bmx-Token") token: String): BanxicoResponse

}