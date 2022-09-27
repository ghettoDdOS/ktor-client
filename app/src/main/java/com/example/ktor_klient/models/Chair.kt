package com.example.ktor_klient.models

import kotlinx.serialization.Serializable

@Serializable
data class Chair(
    val Id: Int,
    var Faculty: Faculty,
    var NameChair: String,
    var ShortNameChair: String
)
@Serializable
data class ChairRequest(
    var Faculty: Int,
    var NameChair: String,
    var ShortNameChair: String
)
