package com.master.musicroomserver.repository

import com.master.musicroomserver.model.RoomEntity
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface RoomRepository : JpaRepository<RoomEntity, Long> {

    fun findByCode(code: String): RoomEntity?

    fun findByCodeIn(codes: List<String>): List<RoomEntity>
}
