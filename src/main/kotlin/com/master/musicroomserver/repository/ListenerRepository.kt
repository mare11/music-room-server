package com.master.musicroomserver.repository

import com.master.musicroomserver.model.ListenerEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface ListenerRepository : JpaRepository<ListenerEntity, Long> {

    fun findByRoomCodeAndName(roomCode: String, name: String): Optional<ListenerEntity>
}
