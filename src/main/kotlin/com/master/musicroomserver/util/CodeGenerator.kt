package com.master.musicroomserver.util

object CodeGenerator {
    private const val ROOM_CODE_LENGTH = 6
    private val charPool: List<Char> = ('a'..'z') + ('A'..'Z') + ('0'..'9')

    fun generateRoomCode(): String {
        return buildString {
            repeat(ROOM_CODE_LENGTH) {
                append(charPool.random())
            }
        }
    }
}
