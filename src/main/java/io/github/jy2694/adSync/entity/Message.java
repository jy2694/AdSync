package io.github.jy2694.adSync.entity;

import lombok.Getter;

import java.util.UUID;

@Getter
public class Message {
    private final UUID messageId;
    private final MessageType type;
    private final String entityId;
    private final String key;

    public Message(UUID messageId, MessageType type, String entityId, String key) {
        this.messageId = messageId;
        this.type = type;
        this.entityId = entityId;
        this.key = key;
    }

    public Message(MessageType type, String entityId, String key) {
        this.messageId = UUID.randomUUID();
        this.type = type;
        this.entityId = entityId;
        this.key = key;
    }

}
