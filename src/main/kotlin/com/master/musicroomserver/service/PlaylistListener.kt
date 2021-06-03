package com.master.musicroomserver.service

interface PlaylistListener {

    fun onSongFinished(songFileName: String, roomCode: String)

    fun onPlaylistFinished(roomCode: String)
}
