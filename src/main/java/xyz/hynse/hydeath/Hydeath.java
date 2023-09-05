package xyz.hynse.hydeath;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;

public final class Hydeath extends JavaPlugin implements Listener {

    private double spreadAmount;
    private boolean canMobPickup;
    private boolean invulnerable;
    private boolean glowing;
    private boolean unlimitedLifetime;
    private int expDropPercent;

    @Override
    public void onEnable() {

        loadConfig();
        getServer().getPluginManager().registerEvents(this, this);
        expDropPercent = getConfig().getInt("expDropPercent", 100);

        // Register the reload command
        Objects.requireNonNull(getCommand("hydeathreload")).setExecutor(this);
    }
    private void loadConfig() {
        File configFile = new File(getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            saveDefaultConfig();
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);

        // Load values from the config.yml
        spreadAmount = config.getDouble("spreadAmount", 0.2);

        ConfigurationSection itemSettingsSection = config.getConfigurationSection("itemSettings");
        if (itemSettingsSection != null) {
            canMobPickup = itemSettingsSection.getBoolean("canMobPickup", true);
            invulnerable = itemSettingsSection.getBoolean("invulnerable", true);
            glowing = itemSettingsSection.getBoolean("glowing", true);
            unlimitedLifetime = itemSettingsSection.getBoolean("unlimitedLifetime", true);
        }
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
        if (command.getName().equalsIgnoreCase("hydeathreload")) {
            reloadConfig();
            loadConfig();
            sender.sendMessage("Hydeath configuration reloaded.");
            return true;
        }
        return false;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        // Get player's location and world
        int x = player.getLocation().getBlockX();
        int y = player.getLocation().getBlockY();
        int z = player.getLocation().getBlockZ();
        String deathSymbol = "\u2620";
        String worldName = player.getWorld().getName();
        String color1 = String.valueOf(net.md_5.bungee.api.ChatColor.of("#fc0303"));
        String color3 = String.valueOf(net.md_5.bungee.api.ChatColor.of("#5c5c5c"));
        String color4 = String.valueOf(net.md_5.bungee.api.ChatColor.of("#ffffff"));
        String text2nd = "\u2514";
        String worldcolor;
        String worldcolor2;
        String formattedWorldName;
        switch (worldName.toLowerCase()) {
            case "world":
                worldcolor = String.valueOf(net.md_5.bungee.api.ChatColor.of("#46f057"));
                worldcolor2 = String.valueOf(net.md_5.bungee.api.ChatColor.of("#5dc267"));
                formattedWorldName = "Overworld";
                break;
            case "world_nether":
                worldcolor = String.valueOf(net.md_5.bungee.api.ChatColor.of("#ff3826"));
                worldcolor2 = String.valueOf(net.md_5.bungee.api.ChatColor.of("#d4574c"));
                formattedWorldName = "Nether";
                break;
            case "world_the_end":
                worldcolor = String.valueOf(net.md_5.bungee.api.ChatColor.of("#af54ff"));
                worldcolor2 = String.valueOf(net.md_5.bungee.api.ChatColor.of("#8e62b5"));
                formattedWorldName = "The End";
                break;
            default:
                worldcolor = String.valueOf(net.md_5.bungee.api.ChatColor.of("#8a8a8a"));
                worldcolor2 = String.valueOf(net.md_5.bungee.api.ChatColor.of("#999999"));
                formattedWorldName = "Unknown";
        }

        // Get the default death message from the event
        String defaultDeathMessage = event.getDeathMessage();

        // Create the custom death message
        String deathMessage = color1 + deathSymbol + " " + color4 + defaultDeathMessage + "\n" + color3 + text2nd + " [" + worldcolor + formattedWorldName + color3 + ": " + ChatColor.RESET + worldcolor2 + x + color3 + ", " + worldcolor2 + y + color3 + ", " + worldcolor2 + z + color3 + "]";

        // Set the custom death message
        event.setDeathMessage(deathMessage);

        // Store the player's inventory contents
        ItemStack[] originalInventory = player.getInventory().getContents();

        // Clear the player's inventory
        event.getDrops().clear();

        // Drop the stored items
        for (ItemStack itemStack : originalInventory) {
            if (itemStack != null) {
                Item item = player.getWorld().dropItemNaturally(player.getLocation(), itemStack);
                item.setCanMobPickup(canMobPickup);
                item.setInvulnerable(invulnerable);
                item.setGlowing(glowing);
                item.setUnlimitedLifetime(unlimitedLifetime);

                // Adjust item's velocity for spread
                Vector velocity = new Vector(
                        Math.random() * spreadAmount - spreadAmount / 2,
                        Math.random() * (spreadAmount / 3) - (spreadAmount / 3) / 2,
                        Math.random() * spreadAmount - spreadAmount / 2
                );
                item.setVelocity(velocity);

                int playerTotalExp = ExperienceUtil.getPlayerExp(player); // Get the player's total experience
                int expToDrop = (playerTotalExp * expDropPercent) / 100; // Calculate experience to drop based on the percentage

                // Clear player's experience
                player.setLevel(0);
                player.setExp(0);

                // Drop the experience orbs
                while (expToDrop > 0) {
                    int orbValue = Math.min(expToDrop, 100);  // Orbs can only hold a maximum of 100 experience
                    player.getWorld().spawn(player.getLocation(), ExperienceOrb.class).setExperience(orbValue);
                    expToDrop -= orbValue;
                }
            }
        }
    }
}