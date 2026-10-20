package com.yourname.genericstacker;

import com.yourname.genericstacker.commands.KillEntitiesCommand;
import com.yourname.genericstacker.commands.ReloadCommand;
import com.yourname.genericstacker.listeners.EntityDeathListener;
import com.yourname.genericstacker.listeners.EntitySpawnListener;
import com.yourname.genericstacker.tasks.EntityStackTask;
import org.bukkit.NamespacedKey;
import org.bukkit.plugin.java.JavaPlugin;

public final class GenericStacker extends JavaPlugin {

    private NamespacedKey stackKey;
    private int maxStack;
    private double mergeRadius;

    @Override
    public void onEnable() {
        this.stackKey = new NamespacedKey(this, "generic_stack_size");

        saveDefaultConfig();
        reloadConfigSettings();

        var pm = getServer().getPluginManager();
        pm.registerEvents(new EntitySpawnListener(this), this);
        pm.registerEvents(new EntityDeathListener(this), this);

        if (getCommand("genericstacker") != null) {
            getCommand("genericstacker").setExecutor(new ReloadCommand(this));
        }

        if (getCommand("killallentities") != null) {
            getCommand("killallentities").setExecutor(new KillEntitiesCommand(this));
        }

        new EntityStackTask(this).runTaskTimer(this, 0L, 40L);
        getLogger().info("GenericStacker enabled! Max stack limit: " + maxStack);
    }

    public void reloadConfigSettings() {
        reloadConfig();
        this.maxStack = getConfig().getInt("max-stack", 100000);
        this.mergeRadius = getConfig().getDouble("merge-radius", 4.0);
    }

    @Override
    public void onDisable() {
        getLogger().info("GenericStacker disabled.");
    }

    public NamespacedKey getStackKey() { return stackKey; }
    public int getMaxStack() { return maxStack; }
    public double getMergeRadius() { return mergeRadius; }
}