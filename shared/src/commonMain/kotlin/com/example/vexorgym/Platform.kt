package com.example.vexorgym

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform