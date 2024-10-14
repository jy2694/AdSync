package io.github.jy2694.adSync;

import io.github.jy2694.adSync.util.DatabaseConnection;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public final class AdSync extends JavaPlugin {

    private static AdSync instance;
    private DatabaseConnection databaseConnection;

    @Override
    public void onEnable() {
        instance = this;
        databaseConnection = new DatabaseConnection("localhost", 6379, "password");
    }

    @Override
    public void onDisable() {

    }

    public DatabaseConnection getDatabaseConnection() {
        return databaseConnection;
    }

    public static AdSync getInstance() {
        return instance;
    }
}
