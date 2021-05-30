package com.master.musicroomserver.service

import com.master.musicroomserver.model.Room
import com.master.musicroomserver.model.RoomListener

interface RoomService {

    fun getRoomByCode(roomCode: String): Room

    fun createRoom(room: Room): Room

    fun connectListener(roomCode: String, roomListener: RoomListener): Room

    fun disconnectListener(roomCode: String, roomListener: RoomListener): Room
}
