package com.master.musicroomserver

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import uk.co.caprica.vlcj.discovery.NativeDiscovery
import uk.co.caprica.vlcj.player.MediaPlayer
import uk.co.caprica.vlcj.player.MediaPlayerEventAdapter
import uk.co.caprica.vlcj.player.MediaPlayerFactory
import uk.co.caprica.vlcj.player.headless.HeadlessMediaPlayer


@SpringBootApplication
class MusicRoomServerApplication

fun main(args: Array<String>) {
    NativeDiscovery().discover()
    runApplication<MusicRoomServerApplication>(*args)
//    streamAudio()
//    streamAudioOld()
}

//private fun streamAudio() {
//    val path = "D:\\Downloads\\Music\\Adriatique - Craft [SIAMESE010].mp3"
//    val options = formatRtspStream("192.168.1.4", 5555, "demo")
//
//    println("Streaming '$path' to '$options'")
//
//    val mediaPlayerFactory = MediaPlayerFactory()
//    val mediaPlayer = mediaPlayerFactory.mediaPlayers().newMediaPlayer()
//    val log: NativeLog = mediaPlayerFactory.application().newLog()
//    log.level = LogLevel.DEBUG
//    log.addLogListener { level, module, file, line, name, header, id, message ->
//        System.out.printf("[%-20s] (%-20s) %7s: %s\n", module, name, level, message)
//    }
//    val latch = CountDownLatch(1)
//
//    mediaPlayer.events().addMediaPlayerEventListener(object : MediaPlayerEventAdapter() {
//        override fun opening(mediaPlayer: MediaPlayer?) {
//            super.opening(mediaPlayer)
//            println("OPENING!!!")
//        }
//
//        override fun playing(mediaPlayer: MediaPlayer?) {
//            super.playing(mediaPlayer)
//            println("PLAYING!!!")
//        }
//
//        override fun paused(mediaPlayer: MediaPlayer?) {
//            super.paused(mediaPlayer)
//            println("PAUSED!!!")
//        }
//
//        override fun finished(mediaPlayer: MediaPlayer?) {
//            super.finished(mediaPlayer)
//            println("FINISHED!!!")
//            latch.countDown()
//        }
//
//        override fun stopped(mediaPlayer: MediaPlayer?) {
//            super.stopped(mediaPlayer)
//            println("STOPPED!!!")
//        }
//
//        override fun error(mediaPlayer: MediaPlayer?) {
//            super.error(mediaPlayer)
//            println("ERROR!!!")
//            latch.countDown()
//        }
//
//        override fun mediaPlayerReady(mediaPlayer: MediaPlayer?) {
//            super.mediaPlayerReady(mediaPlayer)
//            println("READY!!!")
//        }
//    })
//
//    mediaPlayer.media().play(
//        path, options,
//        ":no-sout-rtp-sap",
//        ":no-sout-standard-sap",
//        ":sout-all",
//        ":sout-keep",
//        ":rtsp-timeout=180"
//    ) // TODO: doesn't work, client should send periodic requests to server
//    // so it knows that the connection is alive, no timeout setting should be changed
//
//    latch.await()
//    log.release()
//    mediaPlayer.release()
//    mediaPlayerFactory.release()
//
////    Thread.currentThread().join()
//}

private lateinit var mediaPlayer: HeadlessMediaPlayer

private fun streamAudioOld() {
    val factory = MediaPlayerFactory(mutableListOf("--vout", "dummy"))
    mediaPlayer = factory.newHeadlessMediaPlayer()

    mediaPlayer.addMediaPlayerEventListener(object : MediaPlayerEventAdapter() {
        override fun stopped(mediaPlayer: MediaPlayer?) {
            exit(0)
        }

        override fun finished(mediaPlayer: MediaPlayer?) {
            exit(0)
        }

        override fun error(mediaPlayer: MediaPlayer?) {
            exit(1)
        }
    })
//    val path = "D:\\Downloads\\Music\\Adriatique - Craft [SIAMESE010].mp3"
    val path = "D:\\Downloads\\Music\\Avicii - Broken Arrows.mp3"
    val options = formatRtspStream("192.168.1.8", 5555, "demo")
    println("Streaming '$path' to '$options'")
    mediaPlayer.playMedia(
        path,
        options,
        ":no-sout-rtp-sap",
        ":no-sout-standard-sap",
        ":sout-all",
        ":sout-keep"
    )
}

private fun exit(result: Int) {
    mediaPlayer.release()
//    exitProcess(result)
}

private fun formatRtspStream(serverAddress: String, serverPort: Int, id: String): String {
    val sb = StringBuilder(60)
    sb.append(":sout=#rtp{sdp=rtsp://@")
    sb.append(serverAddress)
    sb.append(':')
    sb.append(serverPort)
    sb.append('/')
    sb.append(id)
    sb.append("}")
    return sb.toString()
}
