package com.master.musicroomserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import uk.co.caprica.vlcj.discovery.NativeDiscovery


@SpringBootApplication
class MusicRoomServerApplication

fun main(args: Array<String>) {
    NativeDiscovery().discover()
    runApplication<MusicRoomServerApplication>(*args)
}
