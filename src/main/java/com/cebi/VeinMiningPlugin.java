package com.cebi;

import org.bukkit.plugin.java.JavaPlugin;

public class VeinMiningPlugin extends JavaPlugin {

    @Override
    public void onEnable() {
        getLogger().info("VeinMiningPlugin has been enabled.");

        // Event listener kaydı
        getServer().getPluginManager().registerEvents(new BlockBreakListener(), this);
    }

    @Override
    public void onDisable() {
        getLogger().info("VeinMiningPlugin has been disabled.");
    }
}
