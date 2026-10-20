package com.yourname.genericstacker.tasks;

import com.yourname.genericstacker.GenericStacker;
import com.yourname.genericstacker.listeners.EntityDeathListener;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

public class EntityStackTask extends BukkitRunnable {

    private final GenericStacker plugin;

    public EntityStackTask(GenericStacker plugin) {
        this.plugin = plugin;
    }

    @Override
    public void run() {
        int maxStack = plugin.getMaxStack();
        double mergeRadius = plugin.getMergeRadius();

        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (!(entity instanceof LivingEntity) || entity instanceof Item) continue;
                LivingEntity target = (LivingEntity) entity;
                if (target.isDead()) continue;

                PersistentDataContainer targetPdc = target.getPersistentDataContainer();
                int targetSize = targetPdc.getOrDefault(plugin.getStackKey(), PersistentDataType.INTEGER, 1);

                if (targetSize == 1 && target.getCustomName() == null) {
                    EntityDeathListener.updateStackName(target, 1);
                }

                if (targetSize >= maxStack) continue;

                for (Entity nearby : target.getNearbyEntities(mergeRadius, mergeRadius, mergeRadius)) {
                    if (!(nearby instanceof LivingEntity) || nearby instanceof Item) continue;
                    LivingEntity source = (LivingEntity) nearby;

                    if (source.isDead() || source.equals(target) || source.getType() != target.getType()) continue;

                    PersistentDataContainer sourcePdc = source.getPersistentDataContainer();
                    int sourceSize = sourcePdc.getOrDefault(plugin.getStackKey(), PersistentDataType.INTEGER, 1);

                    if (targetSize + sourceSize <= maxStack) {
                        targetSize += sourceSize;
                        targetPdc.set(plugin.getStackKey(), PersistentDataType.INTEGER, targetSize);
                        source.remove();
                        EntityDeathListener.updateStackName(target, targetSize);
                    } else {
                        int spaceLeft = maxStack - targetSize;
                        targetSize = maxStack;
                        targetPdc.set(plugin.getStackKey(), PersistentDataType.INTEGER, targetSize);

                        sourceSize -= spaceLeft;
                        sourcePdc.set(plugin.getStackKey(), PersistentDataType.INTEGER, sourceSize);

                        EntityDeathListener.updateStackName(source, sourceSize);
                        EntityDeathListener.updateStackName(target, targetSize);
                        break;
                    }
                }
            }
        }
    }
}