package com.master.musicroomserver.repository

import com.master.musicroomserver.model.SongEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface SongRepository : JpaRepository<SongEntity, Long> {

    fun findByFileName(fileName: String): SongEntity?

    fun findByRoomCode(roomCode: String): List<SongEntity>
}
