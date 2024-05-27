package xyz.hynse.hydeath;

import org.bukkit.ChatColor;
import org.bukkit.World;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.metadata.FixedMetadataValue;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.util.Vector;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.*;

public final class Hydeath extends JavaPlugin implements Listener {

    private double spreadAmount;
    private boolean canMobPickup;
    private boolean invulnerable;
    private boolean glowing;
    private boolean unlimitedLifetime;
    private boolean canOwnerPickupOnly;
    private int expDropPercent;
    private Map<UUID, List<Item>> playerItems = new HashMap<>();

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
            canMobPickup = itemSettingsSection.getBoolean("canMobPickup", false);
            invulnerable = itemSettingsSection.getBoolean("invulnerable", true);
            glowing = itemSettingsSection.getBoolean("glowing", true);
            unlimitedLifetime = itemSettingsSection.getBoolean("unlimitedLifetime", true);
            canOwnerPickupOnly = itemSettingsSection.getBoolean("canOwnerPickupOnly", true);
        }
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, Command command, @NotNull String label, String[] args) {
        if (command.getName().equalsIgnoreCase("hydeathreload")) {
            reloadConfig();
            loadConfig();
            sender.sendMessage("Hydeath configuration reloaded.");
            return true;
        } else if (command.getName().equalsIgnoreCase("unlock")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                UUID playerUUID = player.getUniqueId();

                if (playerItems.containsKey(playerUUID)) {
                    List<Item> items = playerItems.get(playerUUID);
                    for (Item item : items) {
                        item.removeMetadata("owner", this);
                    }
                    playerItems.remove(playerUUID);
                    player.sendMessage(ChatColor.GREEN + "Your items have been unlocked and can now be picked up by other players.");
                } else {
                    player.sendMessage(ChatColor.RED + "You have no locked items.");
                }
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
                return true;
            }
        } else if (command.getName().equalsIgnoreCase("unlocknear")) {
            if (sender instanceof Player) {
                Player player = (Player) sender;
                int range = 10; // Default range
                if (args.length > 0) {
                    try {
                        range = Integer.parseInt(args[0]);
                    } catch (NumberFormatException e) {
                        sender.sendMessage(ChatColor.RED + "Invalid range specified. Using default range (10 blocks).");
                    }
                }

                List<Item> nearbyItems = new ArrayList<>();
                for (Entity entity : player.getNearbyEntities(range, range, range)) {
                    if (entity instanceof Item) {
                        Item item = (Item) entity;
                        nearbyItems.add(item);
                    }
                }

                if (!nearbyItems.isEmpty()) {
                    for (Item item : nearbyItems) {
                        item.removeMetadata("owner", this);
                    }
                    sender.sendMessage(ChatColor.GREEN + "Items near you have been unlocked and can now be picked up by other players.");
                } else {
                    sender.sendMessage(ChatColor.RED + "There are no locked items near you.");
                }
                return true;
            } else {
                sender.sendMessage(ChatColor.RED + "This command can only be used by players.");
            }
        }
        return true;
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();
        UUID playerUUID = player.getUniqueId();
        World world = player.getWorld();
        String keepInventoryValue = world.getGameRuleValue("keepInventory");
        boolean keepInventory = Boolean.parseBoolean(keepInventoryValue);


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
        if (keepInventory) {
            player.getInventory().clear();
        } else {
            event.getDrops().clear();
        }

        List<Item> droppedItems = new ArrayList<>();

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

                if (canOwnerPickupOnly) {
                    item.setMetadata("owner", new FixedMetadataValue(this, player.getUniqueId().toString()));
                    droppedItems.add(item);
                    playerItems.put(playerUUID, droppedItems);
                }

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
    @EventHandler
    public void onPlayerPickupItem(PlayerPickupItemEvent event) {
        Item item = event.getItem();
        Player player = event.getPlayer();
        if (item.hasMetadata("owner")) {
            UUID ownerUUID = UUID.fromString(item.getMetadata("owner").get(0).asString());
            if (!player.getUniqueId().equals(ownerUUID)) {
                event.setCancelled(true);
            }
        }
    }
}