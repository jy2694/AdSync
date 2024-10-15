# AdSync
![](https://img.shields.io/badge/Paper-1.20+-blue?style=flat-square)
![](https://img.shields.io/badge/OpenJDK-17+-red?style=flat-square)

This plugin was developed for data sharing between servers connected by Velocity. 
When you need to use common data, it puts it in a database to manage it. 
If multiple servers access the same database at the same time and try to modify it, 
only one data will be reflected and the other will be overwritten. 
To prevent this from happening, we provide object access locking to allow loading 
and saving only on servers that have preempted object permissions. This is convenient 
because objects are auto-serialized when saved and auto-deserialized when loaded.

---
## Initialization

1. Place the plugin file (.jar) in the plugins folder within the server folder.
2. Run the server once and the file “plugins/AdSync/config.yml” will be created.
3. Write the config.yml according to your environment and run the server again.
```yaml
# Fill in your Redis connection information.
database:
  host: 'localhost'
  port: 6379
  password: ''
# Wait timeout
# Maximum time to wait if another server is already preempted
# If set to 0, no wait timeout is applied (unlimited wait).
preempt-wait-timeout: 30
```
---
## Usage

To use shared objects in the database, use the
``Preempt -> Load -> Use -> Save -> Release``
in that order.

#### Common

All operations run as transactions.
Transactions have a ``unique ID``, and users can use that ID to determine if a transaction has completed.
When a transaction is enqueued, a ``TransactionQueuedEvent`` is triggered.
When a transaction finishes executing, a ``TransactionCompletedEvent`` is triggered.

You can check the status of a transaction at ``[transaction object].getResult()``.

The types of transaction results are as follows
* PREEMPTED : Preempt transaction is completed
* RELEASED : Release transaction is completed
* STORED : Store transaction is completed
* LOADED : Load transaction is completed
* PARTIALLY_PREEMPTED : Preempt Transaction is working and only partially preempted
* PARTIALLY_STORED : Store Transaction is working and only partially stored
* PARTIALLY_LOADED : Load Transaction is working and only partially loaded
* QUEUED : Transaction is enqueued and waiting for processing
* NOT_EXECUTED : Transaction has only been created and not enqueued.
* FAILED : Failed to process transaction

Transaction processing failures occur **when you try to load or store an unpreempted object**, or **when you fail to preempt an object for too long (Wait timeout)**.

#### Preempting

```java
import io.github.jy2694.adSync.entity.transaction.Transaction;

Transaction transaction = Transaction.preempt().addRequireEntity("player_data", player.getUniqueId()).build();
transaction.queue();
```

Create and queue a transaction as above.

#### Loading

```java
import io.github.jy2694.adSync.entity.transaction.Transaction;

Transaction transaction = Transaction.load().addRequireEntity("player_data", player.getUniqueId()).build();
transaction.queue();
```

Create and queue a transaction as above. 

And when the transaction completes, you'll get the loaded object as shown below.
```java
@EventHandler
public void onTransactionCompleted(TransactionCompletedEvent event){
    Transaction transaction = event.getTransaction();
    if(transaction instanceof LoadTransaction loadTransaction){
        PlayerData playerData = (PlayerData) loadTransaction.getLoadedObject("player_data", "40142c3f-4bf0-4d3e-93a8-0389ba53c08d");
    }
}
```

#### Storing

```java
import io.github.jy2694.adSync.entity.transaction.Transaction;

Transaction transaction = Transaction.store().addRequireEntity("player_data", player.getUniqueId(), playerDataObject).build();
transaction.queue();
```

Create and queue a transaction as above.

#### Releasing

```java
import io.github.jy2694.adSync.entity.transaction.Transaction;

Transaction transaction = Transaction.release().addRequireEntity("player_data", player.getUniqueId()).build();
transaction.queue();
```

Create and queue a transaction as above.
