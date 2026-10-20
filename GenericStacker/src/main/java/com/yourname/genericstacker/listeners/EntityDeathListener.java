package com.yourname.genericstacker.listeners;

import com.yourname.genericstacker.GenericStacker;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Animals;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class EntityDeathListener implements Listener {

    private final GenericStacker plugin;
    private final Random random = new Random();

    public EntityDeathListener(GenericStacker plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        LivingEntity entity = event.getEntity();
        if (!isValidMob(entity)) return;

        if (entity.hasMetadata("JustSpawnedTime")) {
            long spawnTime = entity.getMetadata("JustSpawnedTime").get(0).asLong();
            if (System.currentTimeMillis() - spawnTime < 500) return;
        }

        PersistentDataContainer pdc = entity.getPersistentDataContainer();
        if (!pdc.has(plugin.getStackKey(), PersistentDataType.INTEGER)) return;

        int stackSize = pdc.getOrDefault(plugin.getStackKey(), PersistentDataType.INTEGER, 1);

        if (entity.hasMetadata("DropRateLevel")) {
            int dropLevel = entity.getMetadata("DropRateLevel").get(0).asInt();
            int tier = Math.max(0, Math.min(4, dropLevel - 1));

            int minDrops, maxDrops;
            switch (tier) {
                case 4 -> { minDrops = 7; maxDrops = 9; }
                case 3 -> { minDrops = 6; maxDrops = 8; }
                case 2 -> { minDrops = 5; maxDrops = 7; }
                case 1 -> { minDrops = 3; maxDrops = 5; }
                default -> { minDrops = 1; maxDrops = 3; }
            }

            List<ItemStack> originalDrops = new ArrayList<>(event.getDrops());
            event.getDrops().clear();

            int baseAmount = minDrops + random.nextInt((maxDrops - minDrops) + 1);
            double bonusMultiplier = 1.0;

            Player killer = entity.getKiller();
            if (killer != null) {
                ItemStack weapon = killer.getInventory().getItemInMainHand();
                if (weapon != null && weapon.hasItemMeta() && weapon.getItemMeta().hasEnchant(Enchantment.LOOT_BONUS_MOBS)) {
                    int lootingLevel = weapon.getItemMeta().getEnchantLevel(Enchantment.LOOT_BONUS_MOBS);
                    double roll = random.nextDouble();

                    if (lootingLevel == 1 && roll < 0.25) bonusMultiplier = 1.25;
                    else if (lootingLevel == 2 && roll < 0.375) bonusMultiplier = 1.375;
                    else if (lootingLevel >= 3 && roll < 0.50) bonusMultiplier = 1.50;
                }
            }

            int finalAmount = (int) Math.round(baseAmount * bonusMultiplier);
            event.getDrops().add(new ItemStack(getPrimaryDropMaterial(entity.getType()), Math.min(64, finalAmount)));

            for (ItemStack drop : originalDrops) {
                if (drop != null && !isPrimaryDrop(drop)) {
                    event.getDrops().add(drop);
                }
            }
        }

        if (stackSize > 1) {
            int newCount = stackSize - 1;
            LivingEntity respawned = (LivingEntity) entity.getWorld().spawnEntity(entity.getLocation(), entity.getType());

            PersistentDataContainer newPdc = respawned.getPersistentDataContainer();
            newPdc.set(plugin.getStackKey(), PersistentDataType.INTEGER, newCount);
            respawned.setMetadata("JustSpawnedTime", new FixedMetadataValue(plugin, System.currentTimeMillis()));

            if (entity.hasMetadata("DropRateLevel")) {
                respawned.setMetadata("DropRateLevel", entity.getMetadata("DropRateLevel").get(0));
            }

            updateStackName(respawned, newCount);
        }
    }

    public static void updateStackName(LivingEntity entity, int size) {
        String baseName = formatEntityNameAllCaps(entity.getType().name());
        String formattedTag = ChatColor.YELLOW + "" + ChatColor.BOLD + String.format("%,d", size) + "X "
                + ChatColor.GOLD + "" + ChatColor.BOLD + baseName;

        entity.setCustomName(formattedTag);
        entity.setCustomNameVisible(true);
    }

    private static String formatEntityNameAllCaps(String rawName) {
        return rawName.replace("_", " ").toUpperCase();
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

    private Material getPrimaryDropMaterial(EntityType type) {
        return switch (type) {
            case IRON_GOLEM -> Material.IRON_INGOT;
            case ZOMBIE -> Material.ROTTEN_FLESH;
            case SKELETON -> Material.BONE;
            case CREEPER -> Material.GUNPOWDER;
            case SPIDER -> Material.STRING;
            case SLIME -> Material.SLIME_BALL;
            case BLAZE -> Material.BLAZE_ROD;
            case ENDERMAN -> Material.ENDER_PEARL;
            case PIG -> Material.PORKCHOP;
            case COW -> Material.BEEF;
            case CHICKEN -> Material.CHICKEN;
            case SHEEP -> Material.MUTTON;
            default -> Material.IRON_INGOT;
        };
    }

    private boolean isPrimaryDrop(ItemStack item) {
        String name = item.getType().name();
        return name.contains("INGOT") || name.contains("LEATHER") || name.contains("FEATHER") ||
                name.contains("ROTTEN_FLESH") || name.contains("BONE") || name.contains("GUNPOWDER") ||
                name.contains("STRING") || name.contains("SLIME_BALL") || name.contains("BLAZE_ROD") ||
                name.contains("ENDER_PEARL") || name.contains("PORKCHOP") || name.contains("BEEF") ||
                name.contains("CHICKEN") || name.contains("MUTTON");
    }
}