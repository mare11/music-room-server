package com.master.musicroomserver.service

import com.master.musicroomserver.model.Listener
import com.master.musicroomserver.model.Room
import org.springframework.web.multipart.MultipartFile

interface RoomService {

    fun getRoomByCode(roomCode: String): Room

    fun createRoom(room: Room): Room

    fun connectListener(roomCode: String, listener: Listener): Room

    fun disconnectListener(roomCode: String, listener: Listener): Room

    fun addSongToRoomPlaylist(roomCode: String, file: MultipartFile, name: String, duration: Long): Room
}
