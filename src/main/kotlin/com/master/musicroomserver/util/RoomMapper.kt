package com.master.musicroomserver.util

import com.master.musicroomserver.model.*

fun mapRoomFromEntity(roomEntity: RoomEntity): Room {
    return Room(roomEntity.name, roomEntity.code, roomEntity.listeners.size)
}

fun mapRoomDetailsFromEntity(roomEntity: RoomEntity, elapsedDuration: Long): RoomDetails {
    return RoomDetails(
        roomEntity.name,
        roomEntity.code,
        roomEntity.listeners.map { mapListenerFromEntity(it) },
        roomEntity.songs.drop(1).map { mapSongFromEntity(it) },
        roomEntity.songs.firstOrNull()?.let { CurrentSong(mapSongFromEntity(it), elapsedDuration) }
    )
}

fun mapListenerFromEntity(listenerEntity: ListenerEntity): Listener {
    return Listener(listenerEntity.name, listenerEntity.connectedAt.toString())
}

fun mapSongFromEntity(songEntity: SongEntity): Song {
    return Song(songEntity.name, songEntity.duration, songEntity.uploader)
}
