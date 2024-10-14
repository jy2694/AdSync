package io.github.jy2694.adSync.entity;

import io.github.jy2694.adSync.AdSync;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class SyncTransaction {

    private static Map<UUID, SyncTransaction> transactions = new ConcurrentHashMap<>();

    private UUID transactionId;
    private Set<String> requireEntities = new HashSet<>();

    public SyncTransaction(UUID transactionId) {
        this.transactionId = transactionId;
    }

    public void addRequireEntity(String entityName) {
        requireEntities.add(entityName);
    }

    public void run(){
       transactions.put(transactionId, this);
    }
}
