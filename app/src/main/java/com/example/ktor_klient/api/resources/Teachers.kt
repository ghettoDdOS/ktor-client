package com.example.ktor_klient.api.resources

import io.ktor.resources.*
import kotlinx.serialization.*

@Serializable
@Resource("/teacher")
class Teachers() {
    @Serializable
    @Resource("{id}")
    class Id(val parent: Teachers = Teachers(), val id: Int)
    @Serializable
    @Resource("chair")
    class Chair(val parent: Teachers = Teachers()) {
        @Serializable
        @Resource("{id}")
        class Id(val parent: Chair = Chair(), val id: Int)
    }
}