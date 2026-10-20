package com.yourname.genericstacker.commands;

import com.yourname.genericstacker.GenericStacker;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class ReloadCommand implements CommandExecutor {

    private final GenericStacker plugin;
    public static final String PREFIX = ChatColor.DARK_GRAY + "[" + ChatColor.GOLD + "GenericSpawners" + ChatColor.DARK_GRAY + "] " + ChatColor.RESET;

    public ReloadCommand(GenericStacker plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("genericstacker.admin")) {
            sender.sendMessage(PREFIX + ChatColor.RED + "You do not have permission to use this command.");
            return true;
        }

        if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
            plugin.reloadConfigSettings();
            sender.sendMessage(PREFIX + ChatColor.GREEN + "Configuration reloaded successfully! Max stack is now: " + ChatColor.YELLOW + plugin.getMaxStack());
            return true;
        }

        sender.sendMessage(PREFIX + ChatColor.YELLOW + "Usage: /genericstacker reload");
        return true;
    }
}