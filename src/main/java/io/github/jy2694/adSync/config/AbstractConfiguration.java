package io.github.jy2694.adSync.config;

import io.github.jy2694.adSync.AdSync;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;

public abstract class AbstractConfiguration {
    private final File file;
    protected final FileConfiguration config;

    private AbstractConfiguration(File file){
        this.file = file;
        if(!file.exists()){
            AdSync.getInstance().saveResource(file.getName(), false);
        }
        this.config = YamlConfiguration.loadConfiguration(file);
    }

    public AbstractConfiguration(String fileName){
        this(new File(AdSync.getInstance().getDataFolder(), fileName));
    }

    public abstract void load();

    public void save(){
        try{
            config.save(file);
        }catch(Exception ignore){ }
    }
}
