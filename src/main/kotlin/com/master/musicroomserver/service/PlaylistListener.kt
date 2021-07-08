package com.master.musicroomserver.service

interface PlaylistListener {

    fun onNextSong(previousSongFileName: String?, nextSongFileName: String, roomCode: String)

    fun onPlaylistEnded(roomCode: String)
}
