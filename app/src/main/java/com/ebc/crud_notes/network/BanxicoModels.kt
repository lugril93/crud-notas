package com.ebc.crud_notes.network
import com.google.gson.annotations.SerializedName



data class BanxicoResponse(
    @SerializedName("bmx") val bmx: Bmx
)

data class Bmx(
    @SerializedName("series") val series: List<Serie>
)

data class Serie(
    @SerializedName("idSerie") val idSerie: String,
    @SerializedName("titulo") val titulo: String,
    @SerializedName("datos") val datos: List<Dato>
)

data class Dato(
    @SerializedName("fecha") val fecha: String,
    @SerializedName("dato") val dato: String
)
