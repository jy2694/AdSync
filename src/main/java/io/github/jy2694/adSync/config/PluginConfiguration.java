package io.github.jy2694.adSync.config;

import lombok.Getter;

@Getter
public class PluginConfiguration extends AbstractConfiguration{
    
    private String host;
    private int port;
    private String password;
    private int waitTimeout;
    
    public PluginConfiguration() {
        super("config.yml");
    }

    @Override
    public void load() {
        host = config.getString("database.host", "localhost");
        port = config.getInt("database.port", 6379);
        password = config.getString("database.password", "");
        waitTimeout = config.getInt("preemption-wait-timeout", 30);
    }

}
