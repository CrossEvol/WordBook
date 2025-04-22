package com.crossevol.wordbook

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform