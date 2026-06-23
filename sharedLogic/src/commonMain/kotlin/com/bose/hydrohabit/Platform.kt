package com.bose.hydrohabit

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform