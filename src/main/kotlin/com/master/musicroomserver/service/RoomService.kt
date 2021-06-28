package com.master.musicroomserver.service

import com.master.musicroomserver.model.Room
import com.master.musicroomserver.model.RoomDetails
import org.springframework.web.multipart.MultipartFile

interface RoomService {

    fun getRoomByCode(roomCode: String): Room

    fun getRoomsByCodes(roomCodes: List<String>): List<Room>

    fun createRoom(room: Room): Room

    fun connectListener(roomCode: String, listenerName: String): RoomDetails

    fun disconnectListener(roomCode: String, listenerName: String): Unit

    fun addSongToRoomPlaylist(
        roomCode: String,
        file: MultipartFile,
        name: String,
        duration: Long,
        uploader: String
    ): RoomDetails
}
