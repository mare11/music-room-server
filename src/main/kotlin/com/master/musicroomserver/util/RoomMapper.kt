package com.master.musicroomserver.util

import com.master.musicroomserver.model.Room
import com.master.musicroomserver.model.RoomEntity
import com.master.musicroomserver.model.RoomListener
import com.master.musicroomserver.model.RoomListenerEntity

fun mapRoomFromEntity(roomEntity: RoomEntity): Room {
    return Room(roomEntity.name, roomEntity.code, roomEntity.listeners.map { mapRoomListenerFromEntity(it) })
}

fun mapRoomListenerFromEntity(roomListenerEntity: RoomListenerEntity): RoomListener {
    return RoomListener(roomListenerEntity.name, roomListenerEntity.connectedAt.toString())
}
