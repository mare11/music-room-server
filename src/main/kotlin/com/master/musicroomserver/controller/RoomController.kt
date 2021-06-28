package com.master.musicroomserver.controller

import com.master.musicroomserver.model.Room
import com.master.musicroomserver.model.RoomDetails
import com.master.musicroomserver.service.RoomService
import org.springframework.http.MediaType.APPLICATION_JSON_VALUE
import org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE
import org.springframework.http.ResponseEntity
import org.springframework.http.ResponseEntity.ok
import org.springframework.web.bind.annotation.*
import org.springframework.web.multipart.MultipartFile

@RestController()
@RequestMapping("/api/rooms")
class RoomController(val roomService: RoomService) {

    @GetMapping("/{code}", produces = [APPLICATION_JSON_VALUE])
    fun getRoomByCode(@PathVariable code: String): ResponseEntity<Room> {
        val room = roomService.getRoomByCode(code)
        return ok().body(room)
    }

    @GetMapping("/", produces = [APPLICATION_JSON_VALUE])
    fun getRoomsByCodes(@RequestParam codes: List<String>): ResponseEntity<List<Room>> {
        val rooms = roomService.getRoomsByCodes(codes)
        return ok().body(rooms)
    }

    @PostMapping("/", consumes = [APPLICATION_JSON_VALUE], produces = [APPLICATION_JSON_VALUE])
    fun createRoom(@RequestBody room: Room): ResponseEntity<Room> {
        val newRoom = roomService.createRoom(room)
        return ok().body(newRoom)
    }

    @PutMapping("/{code}/connect", produces = [APPLICATION_JSON_VALUE])
    fun connectToRoom(@PathVariable code: String, @RequestParam listener: String): ResponseEntity<RoomDetails> {
        val room = roomService.connectListener(code, listener)
        return ok().body(room)
    }

    @PutMapping("/{code}/disconnect")
    fun disconnectFromRoom(@PathVariable code: String, @RequestParam listener: String): ResponseEntity<Unit> {
        roomService.disconnectListener(code, listener)
        return ok().build()
    }

    @PostMapping("/{code}/upload", consumes = [MULTIPART_FORM_DATA_VALUE], produces = [APPLICATION_JSON_VALUE])
    fun addSongToRoomPlaylist(
        @PathVariable code: String, @RequestParam file: MultipartFile,
        @RequestParam name: String, @RequestParam duration: Long, @RequestParam uploader: String
    ): ResponseEntity<RoomDetails> {
        val room = roomService.addSongToRoomPlaylist(code, file, name, duration, uploader)
        return ok().body(room)
    }
}
