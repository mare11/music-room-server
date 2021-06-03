package com.master.musicroomserver.util

import com.master.musicroomserver.model.*
import com.master.musicroomserver.model.Room

fun mapRoomFromEntity(roomEntity: RoomEntity): Room {
    return Room(
        roomEntity.name,
        roomEntity.code,
        roomEntity.listeners.map { mapListenerFromEntity(it) },
        roomEntity.songs.map { mapSongFromEntity(it) })
}

fun mapListenerFromEntity(listenerEntity: ListenerEntity): Listener {
    return Listener(listenerEntity.name, listenerEntity.connectedAt.toString())
}

fun mapSongFromEntity(songEntity: SongEntity): Song {
    return Song(songEntity.name, songEntity.duration)
}
