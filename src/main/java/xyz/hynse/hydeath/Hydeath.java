package xyz.hynse.hydeath;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Item;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;

public final class Hydeath extends JavaPlugin implements Listener {

    private double spreadAmount;
    private int experienceDropPercentage;
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
        experienceDropPercentage = config.getInt("experienceDropPercentage", 100);

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

        // Store the player's inventory contents
        ItemStack[] originalInventory = player.getInventory().getContents();

        // Clear the player's inventory
        player.getInventory().clear();

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

                // Calculate and drop experience orbs
                int expToDrop = playerTotalExp * experienceDropPercentage / 100;
                int maxExpPerOrb = 30; // Set your desired maximum experience per orb

                int orbsSpawned = 0;
                while (expToDrop > 0 && orbsSpawned < 100) {  // Limit to a reasonable number of orbs
                    int currentExp = Math.min(expToDrop, maxExpPerOrb);

                    // Create experience orbs with a delay
                    Scheduler.runTaskForEntity(event.getEntity(), this, () -> {
                        player.getWorld().spawn(player.getLocation(), ExperienceOrb.class).setExperience(currentExp);
                    }, orbsSpawned * 5);  // Delay between orbs (adjust the delay as needed)

                    // Calculate remaining experience and adjust maxExpPerOrb
                    int remainingExp = expToDrop - currentExp;
                    maxExpPerOrb = remainingExp / (player.getLevel() + 1);

                    expToDrop -= currentExp;
                    orbsSpawned++;
                }

                // Clear player's experience
                player.setLevel(0);
                player.setExp(0);
            }
        }
    }
}