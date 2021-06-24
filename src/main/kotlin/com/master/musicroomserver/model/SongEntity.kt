package com.master.musicroomserver.model

import javax.persistence.*

@Entity(name = "song")
class SongEntity(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    var id: Long? = null,
    @Column(nullable = false)
    var name: String,
    @Column(nullable = false)
    var duration: Long,
    @Column(nullable = false)
    var fileName: String,
    @Column(nullable = false)
    var uploader: String,
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "room_id", nullable = false)
    var room: RoomEntity
) {
    constructor(name: String, duration: Long, fileName: String, uploader: String, room: RoomEntity) :
            this(null, name, duration, fileName, uploader, room)

}
