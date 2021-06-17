package com.master.musicroomserver.service

import java.util.*

interface PlaylistListener {

    fun onNextSong(previousSongFileName: Optional<String>, nextSongFileName: String, roomCode: String)

    fun onPlaylistEnded(roomCode: String)
}
