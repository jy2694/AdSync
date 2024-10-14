package io.github.jy2694.adSync.util;

import io.github.jy2694.adSync.entity.Message;
import io.github.jy2694.adSync.entity.MessageType;
import io.github.jy2694.adSync.entity.PreemptResult;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseConnection {

    private static Map<String, Queue<UUID>> entityPreemptiveQueue = new ConcurrentHashMap<>();
    private static Map<UUID, String> requestMap = new ConcurrentHashMap<>();
    private static Map<String, UUID> preempted = new ConcurrentHashMap<>();

    private final String host;
    private final int port;
    private final String password;

    private RedisClient redisClient;
    private StatefulRedisConnection<String, String> connection;
    private StatefulRedisPubSubConnection<String, String> pubSubConnection;

    public DatabaseConnection(String host, int port, String password) {
        this.host = host;
        this.port = port;
        this.password = password;
    }

    public void connect(){
        if(isConnected()) return;
        redisClient = RedisClient.create("redis://"+password+"@"+host+":"+port+"/0");
        openConnection();
        openPubSubConnection();
    }

    private void openConnection(){
        connection = redisClient.connect();
    }

    private void openPubSubConnection(){
        pubSubConnection = redisClient.connectPubSub();
        pubSubConnection.addListener(new RedisPubSubAdapter<>() {
            @Override
            public void message(String channel, String l) {
                Message message = ObjectSerializer.deserializeObject(l, Message.class);
                if(requestMap.containsKey(message.getMessageId())) return;
                if(message.getType() == MessageType.PRE_PREEMPTIVE){
                    Queue<UUID> queue = entityPreemptiveQueue.computeIfAbsent(message.getEntityId()+":"+message.getKey(), k -> new LinkedList<>());
                    queue.offer(message.getMessageId());
                } else if(message.getType() == MessageType.UN_PREEMPTIVE){
                    Queue<UUID> queue = entityPreemptiveQueue.get(message.getEntityId()+":"+message.getKey());
                    queue.remove(message.getMessageId());
                    if(queue.peek() != null && requestMap.containsKey(queue.peek())){
                        UUID messageId = queue.poll();
                        String[] data = requestMap.get(messageId).split(":");
                        String entityId = data[0];
                        String key = data[1];
                        requestMap.remove(messageId);
                        preempted.put(entityId+":"+key, messageId);
                    }
                }
            }
        });
        pubSubConnection.async().subscribe("adsync");
    }

    public Object loadObject(String entityId, String key){
        UUID uuid = preempted.get(entityId + ":" + key);
        if(uuid == null) {
            //throw not preempted exception
        }
        return connection.sync().get(entityId + ":" + key);
    }

    public PreemptResult preemptObject(String entityId, String key){
        UUID uuid = preempted.get(entityId + ":" + key);
        if(uuid != null) return PreemptResult.PREEMPTED;
        Message message = new Message(MessageType.PRE_PREEMPTIVE, entityId, key);
        Queue<UUID> queue = entityPreemptiveQueue.computeIfAbsent(entityId+":"+key, k -> new LinkedList<>());
        queue.offer(message.getMessageId());
        requestMap.put(message.getMessageId(), entityId+":"+key);
        sendMessage(message);
        if(requestMap.containsKey(queue.peek())){
            requestMap.remove(message.getMessageId());
            queue.poll();
            preempted.put(entityId+":"+key, message.getMessageId());
            return PreemptResult.PREEMPTED;
        }
        return PreemptResult.WAIT;
    }

    public void unPreemptObject(String entityId, String key, Object object){
        UUID uuid = preempted.get(entityId + ":" + key);
        if(uuid == null) {
            //throw not preempted exception
        }
        connection.sync().set(entityId + ":" + key, ObjectSerializer.serializeObject(object));
        preempted.remove(entityId + ":" + key);
        Message message = new Message(uuid, MessageType.UN_PREEMPTIVE, entityId, key);
        sendMessage(message);
    }

    public void sendMessage(Message message){
        pubSubConnection.async().publish("adsync", ObjectSerializer.serializeObject(message));
    }

    public void disconnect(){
        if(!isConnected()) return;
        connection.close();
        connection = null;
    }

    public boolean isConnected(){
        return connection!= null && connection.isOpen();
    }
}
