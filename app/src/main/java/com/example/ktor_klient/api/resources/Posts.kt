package com.example.ktor_klient.api.resources
import io.ktor.resources.*
import kotlinx.serialization.*

@Serializable
@Resource("/post")
class Posts() {
    @Serializable
    @Resource("{id}")
    class Id(val parent: Posts = Posts(), val id: Int)

}