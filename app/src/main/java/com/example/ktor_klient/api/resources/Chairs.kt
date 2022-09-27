package com.example.ktor_klient.api.resources

import io.ktor.resources.*
import kotlinx.serialization.*

@Serializable
@Resource("/chair")
class Chairs() {
    @Serializable
    @Resource("{id}")
    class Id(val parent: Chairs = Chairs(), val id: Int)
    @Serializable
    @Resource("faculty")
    class Faculty(val parent: Chairs = Chairs()){
        @Serializable
        @Resource("{id}")
        class Id(val parent: Faculty = Faculty(), val id: Int)
    }

}