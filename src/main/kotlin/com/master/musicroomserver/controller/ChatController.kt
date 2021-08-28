package com.master.musicroomserver.controller

import com.master.musicroomserver.model.Message
import org.springframework.messaging.handler.annotation.DestinationVariable
import org.springframework.messaging.handler.annotation.MessageMapping
import org.springframework.messaging.handler.annotation.Payload
import org.springframework.messaging.handler.annotation.SendTo
import org.springframework.stereotype.Controller

@Controller
class ChatController {

    @MessageMapping("/room/{roomCode}/chat")
    @SendTo("/topic/room/{roomCode}/chat")
    fun sendMessage(@DestinationVariable roomCode: String, @Payload message: Message): Message {
        println("Message received and sent: $message for room code: $roomCode")
        return message
    }

}
