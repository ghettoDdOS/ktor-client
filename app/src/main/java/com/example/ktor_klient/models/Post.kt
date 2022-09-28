package com.example.ktor_klient.models

import kotlinx.serialization.Serializable

@Serializable
data class Post(
    val Id: Int,
    val NamePost: String
)