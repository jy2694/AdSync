package io.github.jy2694.adSync.entity;

import java.util.UUID;

public class Message {
    private UUID messageId;
    private MessageType type;
    private String entityId;
    private String key;

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

    public UUID getMessageId() {
        return messageId;
    }

    public MessageType getType() {
        return type;
    }

    public String getEntityId() {
        return entityId;
    }

    public String getKey() {
        return key;
    }
}
