package io.github.jy2694.adSync.util;

import io.github.jy2694.adSync.entity.Message;
import io.github.jy2694.adSync.entity.MessageType;
import io.github.jy2694.adSync.entity.transaction.PreemptTransaction;
import io.github.jy2694.adSync.entity.transaction.TransactionResult;
import io.github.jy2694.adSync.event.*;
import io.github.jy2694.adSync.exception.NotPreemptedException;
import io.lettuce.core.RedisClient;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.pubsub.RedisPubSubAdapter;
import io.lettuce.core.pubsub.StatefulRedisPubSubConnection;
import org.bukkit.Bukkit;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class DatabaseConnection {

    /**
     * Message queues waiting to preempt a specific object
     */
    private static final Map<String, Queue<UUID>> entityPreemptiveQueue = new ConcurrentHashMap<>();
    /**
     * Map to store calling entity IDs and keys per message ID
     */
    private static final Map<UUID, String> requestMap = new ConcurrentHashMap<>();
    /**
     * Map that records which messages preempted objects were preempted from
     */
    private static final Map<String, UUID> preempted = new ConcurrentHashMap<>();

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

    /**
     * Redis connection establish method
     */
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
                    Bukkit.getPluginManager().callEvent(new ObjectPrePreemptEvent(
                            message.getMessageId(),
                            message.getEntityId(),
                            message.getKey(), false));
                    Queue<UUID> queue = entityPreemptiveQueue.computeIfAbsent(message.getEntityId()+":"+message.getKey(), k -> new LinkedList<>());
                    queue.offer(message.getMessageId());
                } else if(message.getType() == MessageType.RELEASE){
                    Queue<UUID> queue = entityPreemptiveQueue.get(message.getEntityId()+":"+message.getKey());
                    queue.remove(message.getMessageId());
                    Bukkit.getPluginManager().callEvent(new ObjectReleasedEvent(
                            message.getMessageId(),
                            message.getEntityId(),
                            message.getKey(), false));
                    if(queue.peek() != null && requestMap.containsKey(queue.peek())){
                        UUID messageId = queue.poll();
                        String[] data = requestMap.get(messageId).split(":");
                        String entityId = data[0];
                        String key = data[1];
                        requestMap.remove(messageId);
                        preempted.put(entityId+":"+key, messageId);
                        Bukkit.getPluginManager().callEvent(new ObjectPreemptedEvent(messageId, entityId, key));
                        PreemptTransaction syncTransaction = PreemptTransaction.linkedTransactions.get(messageId);
                        PreemptTransaction.linkedTransactions.remove(messageId);
                        if(PreemptTransaction.linkedTransactions.entrySet().stream()
                                .noneMatch(entry -> entry.getValue().equals(syncTransaction))){
                            syncTransaction.setResult(TransactionResult.PREEMPTED);
                            Bukkit.getPluginManager().callEvent(new TransactionCompletedEvent(syncTransaction));
                        }
                    }
                }
            }
        });
        pubSubConnection.async().subscribe("advanced_sync");
    }

    /**
     * Load object from database.
     * @param entityId entity id(entity identifier)
     * @param key key(object identifier)
     * @return loaded object
     * @throws NotPreemptedException if object is not preempted
     */
    public Object loadObject(String entityId, String key) throws NotPreemptedException {
        Bukkit.getPluginManager().callEvent(new ObjectPreLoadEvent(entityId, key));
        UUID uuid = preempted.get(entityId + ":" + key);
        if(uuid == null) throw new NotPreemptedException("Object not preempted");
        Object object = connection.sync().get(entityId + ":" + key);
        Bukkit.getPluginManager().callEvent(new ObjectLoadedEvent(entityId, key, object));
        return object;
    }

    /**
     * Load object from database.
     * @param entityClass class of the object to load
     * @param key key(object identifier)
     * @return loaded object
     * @throws NotPreemptedException if object is not preempted
     */
    public <T> T loadObject(Class<T> entityClass, String key) throws NotPreemptedException {
        return entityClass.cast(loadObject(entityClass.getName(), key));
    }

    /**
     * Store object to database.
     * @param entityId entity id(entity identifier)
     * @param key key(object identifier)
     * @param object object to store
     * @throws NotPreemptedException if object is not preempted
     */
    public void storeObject(String entityId, String key, Object object) throws NotPreemptedException {
        Bukkit.getPluginManager().callEvent(new ObjectPreStoreEvent(entityId, key));
        UUID uuid = preempted.get(entityId + ":" + key);
        if(uuid == null) throw new NotPreemptedException("Object not preempted");
        connection.sync().set(entityId + ":" + key, ObjectSerializer.serializeObject(object));
        Bukkit.getPluginManager().callEvent(new ObjectStoredEvent(entityId, key, object));
    }

    /**
     * Store object to database
     * @param entityClass class of the object to store
     * @param key key(object identifier)
     * @param object object to store
     * @throws NotPreemptedException if object is not preempted
     */
    public <T> void storeObject(Class<T> entityClass, String key, T object) throws NotPreemptedException {
        storeObject(entityClass.getName(), key, object);
    }
    /**
     * Object preemption methods. If it is already preempted, it will be placed in the preemption queue with the enqueue
     * @param entityId entity id(entity identifier)
     * @param key key(object identifier)
     * @return null if immediately preempted or already preempted, or Message ID if waiting to be preempted.
     */
    public synchronized UUID preemptObject(String entityId, String key){
        UUID uuid = preempted.get(entityId + ":" + key);
        if(uuid == null) return null;
        Message message = new Message(MessageType.PRE_PREEMPTIVE, entityId, key);
        Bukkit.getPluginManager().callEvent(new ObjectPrePreemptEvent(uuid, entityId, key, true));
        Queue<UUID> queue = entityPreemptiveQueue.computeIfAbsent(entityId+":"+key, k -> new LinkedList<>());
        queue.offer(message.getMessageId());
        requestMap.put(message.getMessageId(), entityId+":"+key);
        sendMessage(message);
        if(requestMap.containsKey(queue.peek())){
            requestMap.remove(message.getMessageId());
            queue.poll();
            preempted.put(entityId+":"+key, message.getMessageId());
            Bukkit.getPluginManager().callEvent(new ObjectPreemptedEvent(uuid, entityId, key));
            return null;
        }
        return uuid;
    }

    /**
     * Object preemption methods. If it is already preempted, it will be placed in the preemption queue with the enqueue
     * @param entityClass class of the object to preempt
     * @param key key(object identifier)
     * @return null if immediately preempted or already preempted, or Message ID if waiting to be preempted.
     */
    public synchronized <T> UUID preemptObject(Class<T> entityClass, String key){
        return preemptObject(entityClass.getName(), key);
    }

    /**
     * Release object method . If it is waiting, remove it from the preemptive queue.
     * @param messageId The message ID that preempted the object or the message ID that is waiting for it
     * @param entityId entity id(entity identifier)
     * @param key key(object identifier)
     */
    public void releaseObject(UUID messageId, String entityId, String key){
        Bukkit.getPluginManager().callEvent(new ObjectPreReleaseEvent(messageId, entityId, key));
        preempted.remove(entityId + ":" + key);
        Message message = new Message(messageId, MessageType.RELEASE, entityId, key);
        sendMessage(message);
        Bukkit.getPluginManager().callEvent(new ObjectReleasedEvent(
                message.getMessageId(),
                message.getEntityId(),
                message.getKey(), true));
    }

    /**
     * Release object method . If it is waiting, remove it from the preemptive queue.
     * @param messageId The message ID that preempted the object or the message ID that is waiting for it
     * @param entityClass class of the object to release
     * @param key key(object identifier)
     */
    public void releaseObject(UUID messageId, Class<?> entityClass, String key){
        releaseObject(messageId, entityClass.getName(), key);
    }
    /**
     * Release object method. If not preempted, not working
     * @param entityId entity id(entity identifier)
     * @param key key(object identifier)
     */
    public synchronized void releaseObject(String entityId, String key){
        UUID uuid = preempted.get(entityId + ":" + key);
        if(uuid == null) return;
        releaseObject(uuid, entityId, key);
    }

    /**
     * Release object method. If not preempted, not working
     * @param entityClass class of the object to release
     * @param key key(object identifier)
     */
    public synchronized void releaseObject(Class<?> entityClass, String key){
        releaseObject(entityClass.getName(), key);
    }

    /**
     * Release all objects method. If preempt wait message exists, it will be released.
     */
    public void releaseAllObjects(){
        //TODO - release all
    }

    /**
     * Send message to Redis PubSub channel.
     * @param message message to send
     */
    public void sendMessage(Message message){
        pubSubConnection.async().publish("advanced_sync", ObjectSerializer.serializeObject(message));
    }

    /**
     * Close Redis connection.
     */
    public void disconnect(){
        if(!isConnected()) return;
        connection.close();
        connection = null;
    }

    /**
     * Check if Redis connection is open.
     * @return true if connected, false otherwise.
     */
    public boolean isConnected(){
        return connection!= null && connection.isOpen();
    }
}
