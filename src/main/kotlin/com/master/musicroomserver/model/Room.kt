package com.master.musicroomserver.model

data class Room(val name: String, val code: String, val listeners: List<RoomListener> = emptyList())
