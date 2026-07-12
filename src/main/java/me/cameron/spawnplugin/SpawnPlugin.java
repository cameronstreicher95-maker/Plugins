package me.cameron.spawnplugin;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class SpawnPlugin extends JavaPlugin implements CommandExecutor {

    private File configFile;
    private FileConfiguration data;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.configFile = new File(getDataFolder(), "spawn.yml");
        if (!configFile.exists()) {
            try {
                configFile.createNewFile();
            } catch (IOException e) {
                getLogger().warning("Could not create spawn.yml: " + e.getMessage());
            }
        }
        this.data = YamlConfiguration.loadConfiguration(configFile);

        this.getCommand("spawn").setExecutor(this);
        this.getCommand("plsetspawn").setExecutor(this);
        getLogger().info("SpawnPlugin enabled!");
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("plsetspawn")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only players can set the spawn point.");
                return true;
            }

            if (!player.hasPermission("plspawn.admin")) {
                player.sendMessage("You do not have permission to use this command.");
                return true;
            }

            Location loc = player.getLocation();
            data.set("spawn.world", loc.getWorld().getName());
            data.set("spawn.x", loc.getX());
            data.set("spawn.y", loc.getY());
            data.set("spawn.z", loc.getZ());
            data.set("spawn.yaw", loc.getYaw());
            data.set("spawn.pitch", loc.getPitch());
            saveSpawnData();

            player.sendMessage("Spawn point set successfully!");
            return true;
        }

        if (command.getName().equalsIgnoreCase("spawn")) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage("Only players can use this command.");
                return true;
            }

            if (!player.hasPermission("plspawn.use")) {
                player.sendMessage("You do not have permission to use this command.");
                return true;
            }

            if (!data.contains("spawn.world")) {
                player.sendMessage("No spawn point has been set yet.");
                return true;
            }

            String worldName = data.getString("spawn.world");
            if (worldName == null || Bukkit.getWorld(worldName) == null) {
                player.sendMessage("The saved spawn world is unavailable.");
                return true;
            }

            Location spawn = new Location(
                    Bukkit.getWorld(worldName),
                    data.getDouble("spawn.x"),
                    data.getDouble("spawn.y"),
                    data.getDouble("spawn.z"),
                    (float) data.getDouble("spawn.yaw"),
                    (float) data.getDouble("spawn.pitch")
            );

            player.teleport(spawn);
            player.sendMessage("Teleported to spawn!");
            return true;
        }

        return false;
    }

    private void saveSpawnData() {
        try {
            data.save(configFile);
        } catch (IOException e) {
            getLogger().warning("Could not save spawn data: " + e.getMessage());
        }
    }
}
