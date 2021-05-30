package com.master.musicroomserver.model

import org.hibernate.annotations.CreationTimestamp
import java.time.LocalDateTime
import javax.persistence.*

@Entity(name = "room_listener")
class RoomListenerEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(nullable = false)
    var name: String,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    var room: RoomEntity,
    @CreationTimestamp
    var connectedAt: LocalDateTime? = null
) {
    constructor(name: String, room: RoomEntity) :
            this(null, name, room, null)
}
