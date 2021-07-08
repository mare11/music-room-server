package com.master.musicroomserver.service

import com.master.musicroomserver.model.Room
import com.master.musicroomserver.model.RoomDetails
import org.springframework.web.multipart.MultipartFile

interface RoomService {

    fun getRoomByCode(roomCode: String): Room

    fun getRoomsByCodes(roomCodes: List<String>): List<Room>

    fun createRoom(name: String): Room

    fun connectListener(roomCode: String, listenerName: String): RoomDetails

    fun disconnectListener(roomCode: String, listenerName: String)

    fun addSongToRoomPlaylist(
        roomCode: String,
        file: MultipartFile,
        name: String,
        duration: Long,
        uploader: String
    ): RoomDetails

    fun skipSongForRoom(roomCode: String)
}
