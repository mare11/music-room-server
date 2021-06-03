package com.master.musicroomserver.service

import uk.co.caprica.vlcj.binding.internal.libvlc_media_t
import uk.co.caprica.vlcj.binding.internal.libvlc_state_t
import uk.co.caprica.vlcj.binding.internal.libvlc_state_t.libvlc_Ended
import uk.co.caprica.vlcj.player.MediaPlayerFactory
import uk.co.caprica.vlcj.player.list.MediaListPlayer
import uk.co.caprica.vlcj.player.list.MediaListPlayerEventAdapter

class PlaylistService constructor(
    private val roomCode: String,
    private val path: String,
    private val listener: PlaylistListener,
    mediaPlayerFactory: MediaPlayerFactory
) {

    private val serverHost = "192.168.1.8"
    private var serverPort = 5555
    private val mediaListPlayer = mediaPlayerFactory.newMediaListPlayer()
    private val mediaList = mediaPlayerFactory.newMediaList()
    private var finishedItems = 0

    fun play(songFileName: String) {
        mediaList.setStandardMediaOptions(getStreamOptions(roomCode))
        mediaList.addMedia("$path/$songFileName")
        println("Streaming files from path:$path")

        mediaListPlayer.mediaList = mediaList

        mediaListPlayer.addMediaListPlayerEventListener(object : MediaListPlayerEventAdapter() {

            override fun nextItem(mediaListPlayer: MediaListPlayer?, item: libvlc_media_t?, itemMrl: String?) {
                finishedItems++
                val currentItemIndex = mediaList.items().indexOfFirst { it.mrl().equals(itemMrl) }
                if (currentItemIndex > 0) {
                    val previousItem = mediaList.items()[currentItemIndex - 1]
                    println("Found previous item, name:${previousItem.name()}, mrl:${previousItem.mrl()} on index:${currentItemIndex - 1}")
                    // example: name=files/59ed9546-a527-432b-b91c-e16f86e510e5.mp3
                    if (previousItem.name().contains("/")) {
                        val previousItemFileName = previousItem.name().substring(previousItem.name().indexOf("/") + 1)
                        listener.onSongFinished(previousItemFileName, roomCode)
                    }
                }
            }

            override fun mediaStateChanged(p0: MediaListPlayer?, p1: Int) {
                if (libvlc_Ended == libvlc_state_t.state(p1) && finishedItems == mediaList.size()) {
                    println("Playlist finished, cleaning up...")
                    cleanUp()
                    listener.onPlaylistFinished(roomCode)
                }
            }

        })
        mediaListPlayer.play()
    }

    fun addSongToPlaylist(songFileName: String) {
        mediaList.addMedia("$path/$songFileName")
    }

    private fun getStreamOptions(roomCode: String): String {
        return ":sout=#rtp{sdp=rtsp://$serverHost:$serverPort/$roomCode,mux=ts}"
    }

    private fun cleanUp() {
        mediaList.release()
        mediaListPlayer.release()
    }
}
