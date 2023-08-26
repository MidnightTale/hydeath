package xyz.hynse.hydeath;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.event.world.WorldLoadEvent;
import org.bukkit.event.world.WorldUnloadEvent;
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

        setKeepInventoryForAllWorlds(true);
    }

    @Override
    public void onDisable() {
        setKeepInventoryForAllWorlds(false);
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

    @EventHandler
    public void onWorldLoad(WorldLoadEvent event) {
        setKeepInventory(event.getWorld(), true);
    }

    @EventHandler
    public void onWorldUnload(WorldUnloadEvent event) {
        setKeepInventory(event.getWorld(), false);
    }

    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        if (event.getPlugin().equals(this)) {
            setKeepInventoryForAllWorlds(false);
        }
    }

    private void setKeepInventoryForAllWorlds(boolean value) {
        for (World world : getServer().getWorlds()) {
            setKeepInventory(world, value);
        }
    }

    private void setKeepInventory(World world, boolean value) {
        world.setGameRuleValue("keepInventory", String.valueOf(value));
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
        String worldName = player.getWorld().getName();

        String message = ChatColor.GRAY + "[" + ChatColor.RED + "☠" + ChatColor.GRAY + "] " +
                ChatColor.YELLOW + playerName + ChatColor.RED + " has died at " +
                ChatColor.WHITE + "X: " + ChatColor.GRAY + x + ChatColor.DARK_GRAY + ", " +
                ChatColor.WHITE + "Y: " + ChatColor.GRAY + y + ChatColor.DARK_GRAY + ", " +
                ChatColor.WHITE + "Z: " + ChatColor.GRAY + z + ChatColor.WHITE + " in " +
                ChatColor.AQUA + worldName + ChatColor.GRAY + ".";

        player.sendMessage(message);


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