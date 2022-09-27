package com.example.ktor_klient.models

import kotlinx.serialization.Serializable

@Serializable
data class Faculty(
    val Id: Int,
    val NameFaculty: String,
    val ShortNameFaculty: String
)