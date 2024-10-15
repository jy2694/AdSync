package io.github.jy2694.adSync.entity.transaction;

import io.github.jy2694.adSync.event.TransactionProcessHandler;
import lombok.Getter;
import lombok.Setter;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class Transaction {

    public static PreemptTransactionBuilder preempt(){
        return new PreemptTransactionBuilder();
    }
    public static ReleaseTransactionBuilder release(){
        return new ReleaseTransactionBuilder();
    }
    public static LoadTransactionBuilder load(){
        return new LoadTransactionBuilder();
    }
    public static StoreTransactionBuilder store(){
        return new StoreTransactionBuilder();
    }

    protected UUID transactionId;
    protected Set<String[]> requireEntities;
    @Getter
    @Setter
    protected TransactionResult result;
    protected Runnable task;

    protected Transaction(Set<String[]> requireEntities) {
        this.transactionId = UUID.randomUUID();
        this.requireEntities = requireEntities;
        result = TransactionResult.NOT_EXECUTED;
    }
    public void queue(){
        result = TransactionResult.QUEUED;
        TransactionProcessHandler.transactionProcessQueue.offer(this);
    }
    public @NotNull Runnable getTask(){
        return task;
    }
    @Getter
    public static class PreemptTransactionBuilder {
        private final Set<String[]> requireEntities = new HashSet<>();

        public PreemptTransactionBuilder addRequireEntity(String entityId, String key){
            requireEntities.add(new String[]{entityId, key});
            return this;
        }

        public PreemptTransaction build() {
            return new PreemptTransaction(this);
        }
    }
    @Getter
    public static class ReleaseTransactionBuilder {
        private final Set<String[]> requireEntities = new HashSet<>();

        public ReleaseTransactionBuilder addRequireEntity(String entityId, String key){
            requireEntities.add(new String[]{entityId, key});
            return this;
        }

        public ReleaseTransaction build() {
            return new ReleaseTransaction(this);
        }
    }
    @Getter
    public static class LoadTransactionBuilder {
        private final Set<String[]> requireEntities = new HashSet<>();
        public LoadTransactionBuilder addRequireEntity(String entityId, String key){
            requireEntities.add(new String[]{entityId, key});
            return this;
        }

        public LoadTransaction build() {
            return new LoadTransaction(this);
        }
    }
    @Getter
    public static class StoreTransactionBuilder {
        private final Set<String[]> requireEntities = new HashSet<>();
        private final Map<String, Object> linkedObjects = new ConcurrentHashMap<>();

        public StoreTransactionBuilder addRequireEntity(String entityId, String key, Object object){
            requireEntities.add(new String[]{entityId, key});
            linkedObjects.put(entityId + ":" + key, object);
            return this;
        }
        public StoreTransaction build() {
            return new StoreTransaction(this);
        }
    }
}
