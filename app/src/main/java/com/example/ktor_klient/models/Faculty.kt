package com.example.ktor_klient.models

import kotlinx.serialization.Serializable

@Serializable
data class Faculty(
    val Id: Int,
    var NameFaculty: String,
    var ShortNameFaculty: String
)

@Serializable
data class FacultyRequest(
    val NameFaculty: String,
    val ShortNameFaculty: String
)