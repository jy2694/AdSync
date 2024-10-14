package io.github.jy2694.adSync.entity;

import io.github.jy2694.adSync.AdSync;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class SyncTransaction {
    private UUID transactionId;
    private Set<String> requireEntities = new HashSet<>();

    public SyncTransaction(UUID transactionId) {
        this.transactionId = transactionId;
    }

    public void addRequireEntity(String entityName) {
        requireEntities.add(entityName);
    }

    public void run(){

    }
}
