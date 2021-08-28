package com.master.musicroomserver.service

import uk.co.caprica.vlcj.binding.internal.libvlc_media_t
import uk.co.caprica.vlcj.binding.internal.libvlc_state_t
import uk.co.caprica.vlcj.binding.internal.libvlc_state_t.libvlc_Ended
import uk.co.caprica.vlcj.player.MediaPlayerFactory
import uk.co.caprica.vlcj.player.list.MediaListPlayer
import uk.co.caprica.vlcj.player.list.MediaListPlayerEventAdapter

class PlaylistService(
    private val roomCode: String,
    private val path: String,
    private val listener: PlaylistListener,
    mediaPlayerFactory: MediaPlayerFactory
) {

    private var serverPort = 5555
    private val mediaPlayer = mediaPlayerFactory.newHeadlessMediaPlayer()
    private val mediaListPlayer = mediaPlayerFactory.newMediaListPlayer()
    private val mediaList = mediaPlayerFactory.newMediaList()
    private var itemsPlayed = 0

    fun startStream(songFileName: String) {
        mediaList.setStandardMediaOptions(getStreamOptions(roomCode))
        mediaList.addMedia("$path/$songFileName")

        mediaListPlayer.mediaList = mediaList
        mediaListPlayer.setMediaPlayer(mediaPlayer)

        mediaListPlayer.addMediaListPlayerEventListener(object : MediaListPlayerEventAdapter() {

            override fun nextItem(mediaListPlayer: MediaListPlayer, item: libvlc_media_t, itemMrl: String) {
                itemsPlayed++
                val nextItemIndex = mediaList.items().indexOfFirst { it.mrl().equals(itemMrl) }
                val nextItem = mediaList.items()[nextItemIndex]
                val nextItemFileName = extractSongFileName(nextItem.name())
                if (nextItemIndex > 0) {
                    val previousItem = mediaList.items()[nextItemIndex - 1]
                    val previousItemFileName = extractSongFileName(previousItem.name())
                    listener.onNextSong(previousItemFileName, nextItemFileName, roomCode)
                } else {
                    listener.onNextSong(null, nextItemFileName, roomCode)
                }
            }

            override fun mediaStateChanged(mediaListPlayer: MediaListPlayer, newState: Int) {
                if (libvlc_Ended == libvlc_state_t.state(newState) && itemsPlayed == mediaList.size()) {
                    cleanUp()
                    listener.onPlaylistEnded(roomCode)
                }
            }

        })
        mediaListPlayer.play()
    }

    fun addSongToPlaylist(songFileName: String) {
        mediaList.addMedia("$path/$songFileName")
    }

    fun playNextSong() {
        if (mediaPlayer.isPlaying && itemsPlayed < mediaList.size()) {
            mediaListPlayer.playNext()
        }
    }

    fun getCurrentPlayerTime(): Long {
        return if (mediaPlayer.isPlaying) {
            mediaPlayer.time
        } else {
            println("Current player time is 0 since player is not playing")
            0L
        }
    }

    private fun getStreamOptions(roomCode: String): String {
        return ":sout=#rtp{sdp=rtsp://:$serverPort/$roomCode,mux=ts}"
    }

    private fun extractSongFileName(songFileName: String): String {
        // example: name=files/59ed9546-a527-432b-b91c-e16f86e510e5
        if (songFileName.contains("/")) {
            return songFileName.substring(songFileName.indexOf("/") + 1)
        }
        return songFileName
    }

    private fun cleanUp() {
        mediaList.release()
        mediaPlayer.release()
        mediaListPlayer.release()
    }
}
