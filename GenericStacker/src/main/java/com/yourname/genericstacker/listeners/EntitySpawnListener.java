package com.yourname.genericstacker.listeners;

import com.yourname.genericstacker.GenericStacker;
import org.bukkit.entity.Animals;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

public class EntitySpawnListener implements Listener {

    private final GenericStacker plugin;

    public EntitySpawnListener(GenericStacker plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntitySpawn(EntitySpawnEvent event) {
        Entity spawnedEntity = event.getEntity();
        if (!isValidMob(spawnedEntity)) return;

        LivingEntity spawned = (LivingEntity) spawnedEntity;
        PersistentDataContainer spawnedPdc = spawned.getPersistentDataContainer();

        int maxStack = plugin.getMaxStack();
        double mergeRadius = plugin.getMergeRadius();

        if (!spawnedPdc.has(plugin.getStackKey(), PersistentDataType.INTEGER)) {
            spawnedPdc.set(plugin.getStackKey(), PersistentDataType.INTEGER, 1);
        }

        int spawnedSize = spawnedPdc.getOrDefault(plugin.getStackKey(), PersistentDataType.INTEGER, 1);

        for (Entity nearby : spawned.getNearbyEntities(mergeRadius, mergeRadius, mergeRadius)) {
            if (!isValidMob(nearby)) continue;
            LivingEntity target = (LivingEntity) nearby;

            if (target.isDead() || target.equals(spawned) || target.getType() != spawned.getType()) continue;

            PersistentDataContainer targetPdc = target.getPersistentDataContainer();
            int targetSize = targetPdc.getOrDefault(plugin.getStackKey(), PersistentDataType.INTEGER, 1);

            if (targetSize < maxStack) {
                int total = targetSize + spawnedSize;

                if (total <= maxStack) {
                    targetPdc.set(plugin.getStackKey(), PersistentDataType.INTEGER, total);
                    EntityDeathListener.updateStackName(target, total);
                    spawned.remove();
                    return;
                } else {
                    int spaceLeft = maxStack - targetSize;
                    targetPdc.set(plugin.getStackKey(), PersistentDataType.INTEGER, maxStack);
                    EntityDeathListener.updateStackName(target, maxStack);

                    spawnedSize -= spaceLeft;
                    spawnedPdc.set(plugin.getStackKey(), PersistentDataType.INTEGER, spawnedSize);
                    EntityDeathListener.updateStackName(spawned, spawnedSize);
                }
            }
        }

        EntityDeathListener.updateStackName(spawned, spawnedSize);
    }

    private boolean isValidMob(Entity entity) {
        if (!(entity instanceof LivingEntity) || entity instanceof ArmorStand || entity instanceof Player || entity instanceof Item) {
            return false;
        }
        return entity instanceof Animals || entity instanceof Monster ||
                entity instanceof org.bukkit.entity.WaterMob ||
                entity instanceof org.bukkit.entity.Villager ||
                entity instanceof org.bukkit.entity.Golem;
    }
}