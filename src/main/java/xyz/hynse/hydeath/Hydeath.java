package xyz.hynse.hydeath;

import org.bukkit.Bukkit;
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

    @Override
    public void onEnable() {

        loadConfig();
        getServer().getPluginManager().registerEvents(this, this);

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
        String playerName = player.getName();

        // Get player's location and world
        int x = player.getLocation().getBlockX();
        int y = player.getLocation().getBlockY();
        int z = player.getLocation().getBlockZ();

        String deathSymbol = "\u2620 ";
        String worldName = player.getWorld().getName();
        String color1 = String.valueOf(net.md_5.bungee.api.ChatColor.of("#fc0303"));
        String color2 = String.valueOf(net.md_5.bungee.api.ChatColor.of("#ff6e6e"));
        String color3 = String.valueOf(net.md_5.bungee.api.ChatColor.of("#dedede"));
        String color3_1 = String.valueOf(net.md_5.bungee.api.ChatColor.of("#d1d1d1"));
        String color3_2 = String.valueOf(net.md_5.bungee.api.ChatColor.of("#b5b5b5"));
        String color4 = String.valueOf(net.md_5.bungee.api.ChatColor.of("#ffffff"));

        // Get the default death message from the event
        String defaultDeathMessage = event.getDeathMessage();

        // Create the custom death message
        String deathMessage = color1 + deathSymbol + color4 + defaultDeathMessage +
                color3_1 + " (" + color3_1 + "X: " + color2  + x + color3_2 + ", " +
                color3_1 + "Y: " + color2  + y + color3_2 + ", " +
                color3_1 + "Z: " + color2  + z + color3_1 + " in " +
                color2 + worldName + color3_1 + ")";

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

                int playerTotalExp = ExperienceUtil.getPlayerExp(player);
                while (playerTotalExp > 0) {
                    int currentExp = Math.min(playerTotalExp, 100);
                    player.getWorld().spawn(player.getLocation(), ExperienceOrb.class).setExperience(currentExp);
                    playerTotalExp -= currentExp;
                }

                // Clear player's experience
                player.setLevel(0);
                player.setExp(0);
            }
        }
    }
}