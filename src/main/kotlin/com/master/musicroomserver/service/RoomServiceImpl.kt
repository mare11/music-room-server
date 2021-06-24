package com.master.musicroomserver.service

import com.master.musicroomserver.exception.AlreadyExistsException
import com.master.musicroomserver.exception.BadRequestException
import com.master.musicroomserver.exception.NotFoundException
import com.master.musicroomserver.model.*
import com.master.musicroomserver.model.RoomEntity
import com.master.musicroomserver.repository.ListenerRepository
import com.master.musicroomserver.repository.RoomRepository
import com.master.musicroomserver.repository.SongRepository
import com.master.musicroomserver.util.mapRoomDetailsFromEntity
import com.master.musicroomserver.util.mapRoomFromEntity
import com.master.musicroomserver.util.mapSongFromEntity
import org.springframework.messaging.simp.SimpMessagingTemplate
import org.springframework.stereotype.Service
import org.springframework.web.multipart.MultipartFile
import uk.co.caprica.vlcj.player.MediaPlayerFactory
import java.io.File
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*
import javax.annotation.PreDestroy
import kotlin.concurrent.schedule

@Service
class RoomServiceImpl(
    val roomRepository: RoomRepository,
    val listenerRepository: ListenerRepository,
    val songRepository: SongRepository,
    val webSocketTemplate: SimpMessagingTemplate
) : RoomService, PlaylistListener {

    private val roomPlaylistMap: MutableMap<String, PlaylistService> = HashMap()

    private val path: String by lazy {
        val resource = this.javaClass.classLoader.getResource("")
        val file = File(resource?.path + "/files")
        if (!file.exists()) {
            file.mkdir()
        }
        file.path
    }

    private val mediaPlayerFactory: MediaPlayerFactory = MediaPlayerFactory(listOf("--no-sout-all", "--sout-keep"))

    override fun getRoomByCode(roomCode: String): Room {
        val roomEntity = getRoomEntityByCode(roomCode)
        return mapRoomFromEntity(roomEntity)
    }

    override fun getRoomsByCodes(roomCodes: List<String>): List<Room> {
        return roomRepository.findByCodeIn(roomCodes).map { mapRoomFromEntity(it) }
    }

    override fun createRoom(room: Room): Room {
        val roomEntity = RoomEntity(room.name, room.code)
        roomRepository.save(roomEntity)
        return mapRoomFromEntity(roomEntity)
    }

    override fun connectListener(roomCode: String, listener: Listener): RoomDetails {
        val roomListenerEntityOptional = listenerRepository.findByRoomCodeAndName(roomCode, listener.name)
        if (!roomListenerEntityOptional.isPresent) {
            val roomEntity = getRoomEntityByCode(roomCode)
            val listenerEntity = ListenerEntity(listener.name, roomEntity)
            listenerRepository.save(listenerEntity)
//            webSocketTemplate.convertAndSend("/topic/room/{roomCode}/listeners", roomEntity.listeners)
            return mapRoomDetailsFromEntity(roomEntity, getElapsedSongDuration(roomCode))
        } else {
            throw AlreadyExistsException("Listener name '${listener.name}' already taken in room with code '$roomCode'")
        }
    }

    override fun disconnectListener(roomCode: String, listener: Listener) {
        val listenerEntityOptional = listenerRepository.findByRoomCodeAndName(roomCode, listener.name)
        if (listenerEntityOptional.isPresent) {
            val listenerEntity = listenerEntityOptional.get()
            listenerRepository.delete(listenerEntity)
//            webSocketTemplate.convertAndSend("/topic/room/{roomCode}/listeners", roomEntity.listeners)
        } else {
            throw NotFoundException("Listener '${listener.name}' not found in room with code '$roomCode'")
        }
    }

    override fun addSongToRoomPlaylist(
        roomCode: String,
        file: MultipartFile,
        name: String,
        duration: Long
    ): RoomDetails {
        if (file.isEmpty) {
            throw BadRequestException("Empty file uploaded")
        }
        val roomEntity = getRoomEntityByCode(roomCode)
        val fileName = UUID.randomUUID().toString()
        Files.write(Paths.get(getSongFilePath(fileName)), file.bytes)
        val songEntity = SongEntity(name, duration, fileName, roomEntity)
        songRepository.save(songEntity)

        if (roomPlaylistMap.containsKey(roomCode)) {
            roomPlaylistMap[roomCode]?.addSongToPlaylist(fileName)
        } else {
            val playlistService = PlaylistService(roomCode, path, this, mediaPlayerFactory)
            roomPlaylistMap[roomCode] = playlistService
            val timer = Timer()
            println("Waiting 1 second to start streaming...")
            timer.schedule(1000) {
                playlistService.play(fileName)
                timer.cancel()
                println("Stream starting...")
            }
        }

        return mapRoomDetailsFromEntity(roomEntity, getElapsedSongDuration(roomCode))
    }

    override fun onNextSong(previousSongFileName: Optional<String>, nextSongFileName: String, roomCode: String) {
        if (previousSongFileName.isPresent) {
            println("Song ${previousSongFileName.get()} finished!")
            val songEntityOptional = songRepository.findByFileName(previousSongFileName.get())
            if (songEntityOptional.isPresent) {
                songRepository.delete(songEntityOptional.get())
                Files.delete(Paths.get(getSongFilePath(previousSongFileName.get())))
            }
        }

        val songEntityOptional = songRepository.findByFileName(nextSongFileName)
        if (songEntityOptional.isPresent) {
            val song = mapSongFromEntity(songEntityOptional.get())
            println("Next song: ${song.name}")
            webSocketTemplate.convertAndSend("/topic/room/$roomCode/stream", song)
        }
    }

    override fun onPlaylistEnded(roomCode: String) {
        // there should be only 1 song left to be removed in the end
        songRepository.findByRoomCode(roomCode).forEach {
            songRepository.delete(it)
            Files.delete(Paths.get(getSongFilePath(it.fileName)))
        }
        roomPlaylistMap.remove(roomCode)
    }

    private fun getSongFilePath(fileName: String): String {
        return this.path + "/" + fileName
    }

    private fun getRoomEntityByCode(roomCode: String): RoomEntity {
        return roomRepository.findByCode(roomCode)
            .orElseThrow { NotFoundException("Room with code '$roomCode' not found!") }
    }

    private fun getElapsedSongDuration(roomCode: String): Long {
        return roomPlaylistMap[roomCode]?.getCurrentPlayerTime() ?: 0L
    }

    @PreDestroy
    private fun cleanUp() {
        mediaPlayerFactory.release()
    }

}
