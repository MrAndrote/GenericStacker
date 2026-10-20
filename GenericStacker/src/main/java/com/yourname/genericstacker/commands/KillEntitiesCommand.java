package com.yourname.genericstacker.commands;

import com.yourname.genericstacker.GenericStacker;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Animals;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;

public class KillEntitiesCommand implements CommandExecutor {

    private final GenericStacker plugin;

    public KillEntitiesCommand(GenericStacker plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("genericstacker.admin")) {
            sender.sendMessage(ReloadCommand.PREFIX + ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        int removedCount = 0;
        for (World world : Bukkit.getWorlds()) {
            for (Entity entity : world.getEntities()) {
                if (isValidMob(entity)) {
                    LivingEntity mob = (LivingEntity) entity;
                    mob.getPersistentDataContainer().remove(plugin.getStackKey());
                    mob.remove();
                    removedCount++;
                }
            }
        }

        sender.sendMessage(ReloadCommand.PREFIX + ChatColor.GREEN + "Removed " + ChatColor.YELLOW + String.format("%,d", removedCount) + ChatColor.GREEN + " stacked entities across all worlds!");
        return true;
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