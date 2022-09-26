package com.example.ktor_klient.api.resources

import io.ktor.resources.*
import kotlinx.serialization.*

@Serializable
@Resource("/faculty")
class Faculties() {
    @Serializable
    @Resource("{id}")
    class Id(val parent: Faculties = Faculties(), val id: Int)
}