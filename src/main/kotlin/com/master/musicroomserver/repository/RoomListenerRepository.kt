package com.master.musicroomserver.repository

import com.master.musicroomserver.model.RoomListenerEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface RoomListenerRepository : JpaRepository<RoomListenerEntity, Long> {

    fun findByRoomCodeAndName(roomCode: String, name: String): Optional<RoomListenerEntity>
}
