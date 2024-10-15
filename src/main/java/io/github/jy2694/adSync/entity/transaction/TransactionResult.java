package io.github.jy2694.adSync.entity.transaction;

public enum TransactionResult {
    PREEMPTED,
    RELEASED,
    STORED,
    LOADED,
    PARTIALLY_PREEMPTED,
    PARTIALLY_STORED,
    PARTIALLY_LOADED,
    QUEUED,
    NOT_EXECUTED,
    FAILED
}
