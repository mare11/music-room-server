package com.master.musicroomserver.service

import com.master.musicroomserver.exception.AlreadyExistsException
import com.master.musicroomserver.exception.BadRequestException
import com.master.musicroomserver.exception.NotFoundException
import com.master.musicroomserver.model.*
import com.master.musicroomserver.repository.ListenerRepository
import com.master.musicroomserver.repository.RoomRepository
import com.master.musicroomserver.repository.SongRepository
import com.master.musicroomserver.util.*
import com.master.musicroomserver.util.GeneratorUtil.generateRoomCode
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

    override fun createRoom(name: String): Room {
        val roomEntity = RoomEntity(name, generateRoomCode())
        roomRepository.save(roomEntity)
        return mapRoomFromEntity(roomEntity)
    }

    override fun connectListener(roomCode: String, listenerName: String): RoomDetails {
        val roomListenerEntity = listenerRepository.findByRoomCodeAndName(roomCode, listenerName)
        if (roomListenerEntity == null) {
            val roomEntity = getRoomEntityByCode(roomCode)
            val listenerEntity = ListenerEntity(listenerName, roomEntity)
            listenerRepository.save(listenerEntity)
            webSocketTemplate.convertAndSend(
                "/topic/room/$roomCode/listener/connect",
                mapListenerFromEntity(listenerEntity)
            )
            return mapRoomDetailsFromEntity(roomEntity, getElapsedSongDuration(roomCode))
        } else {
            throw AlreadyExistsException("Listener name '$listenerName' already taken in room with code '$roomCode'")
        }
    }

    override fun disconnectListener(roomCode: String, listenerName: String) {
        val listenerEntity = listenerRepository.findByRoomCodeAndName(roomCode, listenerName)
        if (listenerEntity != null) {
            listenerRepository.delete(listenerEntity)
            webSocketTemplate.convertAndSend("/topic/room/$roomCode/listener/disconnect", listenerName)
        } else {
            throw NotFoundException("Listener '$listenerName' not found in room with code '$roomCode'")
        }
    }

    override fun addSongToRoomPlaylist(
        roomCode: String,
        file: MultipartFile,
        name: String,
        duration: Long,
        uploader: String
    ): RoomDetails {
        if (file.isEmpty) {
            throw BadRequestException("Empty file uploaded")
        }
        val roomEntity = getRoomEntityByCode(roomCode)
        val fileName = UUID.randomUUID().toString()
        Files.write(Paths.get(getSongFilePath(fileName)), file.bytes)
        val songEntity = SongEntity(name, duration, fileName, uploader, roomEntity)
        songRepository.save(songEntity)

        if (roomPlaylistMap.containsKey(roomCode)) {
            roomPlaylistMap[roomCode]?.addSongToPlaylist(fileName)
            webSocketTemplate.convertAndSend("/topic/room/$roomCode/song/add", mapSongFromEntity(songEntity))
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

    override fun skipSongForRoom(roomCode: String) {
        roomPlaylistMap[roomCode]?.skipSong()
    }

    override fun onNextSong(previousSongFileName: String?, nextSongFileName: String, roomCode: String) {
        if (previousSongFileName != null) {
            println("Song $previousSongFileName finished!")
            songRepository.findByFileName(previousSongFileName)?.let {
                songRepository.delete(it)
                Files.delete(Paths.get(getSongFilePath(previousSongFileName)))
                webSocketTemplate.convertAndSend(
                    "/topic/room/$roomCode/song/end",
                    mapSongFromEntity(it)
                )
            }
        }

        songRepository.findByFileName(nextSongFileName)?.let {
            val song = mapSongFromEntity(it)
            println("Next song: ${song.name}")
            webSocketTemplate.convertAndSend("/topic/room/$roomCode/song/next", song)
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
        return roomRepository.findByCode(roomCode) ?: throw NotFoundException("Room with code '$roomCode' not found!")
    }

    private fun getElapsedSongDuration(roomCode: String): Long {
        return roomPlaylistMap[roomCode]?.getCurrentPlayerTime() ?: 0L
    }

    @PreDestroy
    private fun cleanUp() {
        mediaPlayerFactory.release()
    }

}
