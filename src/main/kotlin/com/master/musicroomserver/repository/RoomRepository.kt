package com.master.musicroomserver.repository

import com.master.musicroomserver.model.RoomEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository
import java.util.*

@Repository
interface RoomRepository : JpaRepository<RoomEntity, Long> {

    fun findByCode(code: String): Optional<RoomEntity>
    
    fun findByCodeIn(codes: List<String>): List<RoomEntity>
}
