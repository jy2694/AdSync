package io.github.jy2694.adSync;

import io.github.jy2694.adSync.config.PluginConfiguration;
import io.github.jy2694.adSync.util.DatabaseConnection;
import lombok.Getter;
import org.bukkit.plugin.java.JavaPlugin;

public final class AdSync extends JavaPlugin {

    @Getter
    private static AdSync instance;
    @Getter
    private DatabaseConnection databaseConnection;
    private PluginConfiguration pluginConfiguration;

    @Override
    public void onEnable() {
        instance = this;
        loadConfiguration();
        establishConnection();
    }

    @Override
    public void onDisable() {
        disconnect();
    }

    private void loadConfiguration(){
        pluginConfiguration = new PluginConfiguration();
        pluginConfiguration.load();
    }

    private void establishConnection(){
        databaseConnection = new DatabaseConnection(
                pluginConfiguration.getHost(),
                pluginConfiguration.getPort(),
                pluginConfiguration.getPassword()
        );
        databaseConnection.connect();
    }

    private void disconnect(){
        if(databaseConnection!= null && databaseConnection.isConnected())
            databaseConnection.disconnect();
    }

    public long getWaitTimeout(){
        return pluginConfiguration.getWaitTimeout();
    }
}
