package com.example.posserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class PosServerApplication

fun main(args: Array<String>) {
    runApplication<PosServerApplication>(*args)
}
