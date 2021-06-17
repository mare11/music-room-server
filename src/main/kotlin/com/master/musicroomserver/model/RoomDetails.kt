package com.master.musicroomserver.model

data class RoomDetails(
    val name: String,
    val code: String,
    val listeners: List<Listener> = emptyList(),
    val playlist: List<Song> = emptyList()
)
