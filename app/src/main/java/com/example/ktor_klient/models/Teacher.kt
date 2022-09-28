package com.example.ktor_klient.models

import kotlinx.serialization.Serializable

@Serializable
data class Teacher(
    val Id: Int,
    var Chair: Int,
    var Post: Post,
    var FirstName: String,
    var SecondName: String,
    var LastName: String,
    var Phone: String,
    var Email: String
)

@Serializable
data class TeacherRequest(
    val Chair: Int,
    val Post: Int,
    val FirstName: String,
    val SecondName: String,
    val LastName: String,
    val Phone: String,
    val Email: String
)