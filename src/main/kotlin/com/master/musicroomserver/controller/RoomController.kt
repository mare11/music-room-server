package com.master.musicroomserver.controller

import com.master.musicroomserver.model.Room
import com.master.musicroomserver.model.RoomListener
import com.master.musicroomserver.service.RoomService
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.*

@RestController()
@RequestMapping("/api/rooms")
class RoomController(val roomService: RoomService) {

    @GetMapping("/{code}", produces = [APPLICATION_JSON_VALUE])
    fun getRoomByCode(@PathVariable code: String): ResponseEntity<Room> {
        val room = roomService.getRoomByCode(code)
        return ok().body(room)
    }

    @PostMapping("/", consumes = [APPLICATION_JSON_VALUE], produces = [APPLICATION_JSON_VALUE])
    fun createRoom(@RequestBody room: Room): ResponseEntity<Room> {
        val newRoom = roomService.createRoom(room)
        return ok().body(newRoom)
    }

    @PutMapping("/{code}/connect", consumes = [APPLICATION_JSON_VALUE], produces = [APPLICATION_JSON_VALUE])
    fun connectToRoom(@PathVariable code: String, @RequestBody roomListener: RoomListener): ResponseEntity<Room> {
        val room = roomService.connectListener(code, roomListener)
        return ok().body(room)
    }

    @PutMapping("/{code}/disconnect", consumes = [APPLICATION_JSON_VALUE], produces = [APPLICATION_JSON_VALUE])
    fun disconnectFromRoom(@PathVariable code: String, @RequestBody roomListener: RoomListener): ResponseEntity<Room> {
        val room = roomService.disconnectListener(code, roomListener)
        return ok().body(room)
    }
}
