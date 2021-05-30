package com.master.musicroomserver.service

import com.master.musicroomserver.exception.AlreadyExistsException
import com.master.musicroomserver.exception.NotFoundException
import com.master.musicroomserver.model.Room
import com.master.musicroomserver.model.RoomEntity
import com.master.musicroomserver.model.RoomListener
import com.master.musicroomserver.model.RoomListenerEntity
import com.master.musicroomserver.repository.RoomListenerRepository
import com.master.musicroomserver.repository.RoomRepository
import com.master.musicroomserver.util.mapRoomFromEntity
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service

@Service
class RoomServiceImpl(
    val roomRepository: RoomRepository,
    val roomListenerRepository: RoomListenerRepository,
    val webSocketTemplate: SimpMessagingTemplate
) : RoomService {

    override fun getRoomByCode(roomCode: String): Room {
        val roomEntity = getRoomEntityByCode(roomCode)
        return mapRoomFromEntity(roomEntity)
    }

    override fun createRoom(room: Room): Room {
        val roomEntity = RoomEntity(room.name, room.code)
        roomRepository.save(roomEntity)
        return mapRoomFromEntity(roomEntity)
    }

    override fun connectListener(roomCode: String, roomListener: RoomListener): Room {
        val roomListenerEntityOptional = roomListenerRepository.findByRoomCodeAndName(roomCode, roomListener.name)
        if (!roomListenerEntityOptional.isPresent) {
            val roomEntity = getRoomEntityByCode(roomCode)
            val roomListenerEntity = RoomListenerEntity(roomListener.name, roomEntity)
            roomListenerRepository.save(roomListenerEntity)
//            webSocketTemplate.convertAndSend("/topic/room/{roomCode}/listeners", roomEntity.listeners)
            return mapRoomFromEntity(roomEntity)
        } else {
            throw AlreadyExistsException("Listener name '${roomListener.name}' already taken in room with code '$roomCode'")
        }
    }

    override fun disconnectListener(roomCode: String, roomListener: RoomListener): Room {
        val roomListenerEntityOptional = roomListenerRepository.findByRoomCodeAndName(roomCode, roomListener.name)
        if (roomListenerEntityOptional.isPresent) {
            val roomEntity = getRoomEntityByCode(roomCode)
            val roomListenerEntity = roomListenerEntityOptional.get()
            roomListenerRepository.delete(roomListenerEntity)
//            webSocketTemplate.convertAndSend("/topic/room/{roomCode}/listeners", roomEntity.listeners)
            return mapRoomFromEntity(roomEntity)
        } else {
            throw NotFoundException("Listener '${roomListener.name}' not found in room with code '$roomCode'")
        }
    }

    private fun getRoomEntityByCode(roomCode: String): RoomEntity {
        return roomRepository.findByCode(roomCode)
            .orElseThrow { NotFoundException("Room with code '$roomCode' not found!") }
    }
}
