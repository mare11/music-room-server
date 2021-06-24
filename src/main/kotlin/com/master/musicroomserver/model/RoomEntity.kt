package com.master.musicroomserver.model

import javax.persistence.*
import javax.persistence.GenerationType.IDENTITY

@Entity(name = "room")
class RoomEntity(
    @Id @GeneratedValue(strategy = IDENTITY)
    var id: Long? = null,
    @Column(nullable = false)
    var name: String,
    @Column(nullable = false, unique = true)
    var code: String,
    @OneToMany(mappedBy = "room", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("connectedAt")
    var listeners: MutableList<ListenerEntity> = ArrayList(),
    @OneToMany(mappedBy = "room", cascade = [CascadeType.ALL], orphanRemoval = true)
    @OrderBy("id")
    var songs: MutableList<SongEntity> = ArrayList()
) {
    constructor(name: String, code: String) :
            this(null, name, code, ArrayList(), ArrayList())
}
