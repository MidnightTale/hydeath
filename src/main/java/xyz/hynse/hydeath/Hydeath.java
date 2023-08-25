package xyz.hynse.hydeath;

import org.bukkit.entity.ExperienceOrb;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Item;

public final class Hydeath extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
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
                item.setCanMobPickup(true);
                item.setInvulnerable(true);
                item.setGlowing(true);
                item.setUnlimitedLifetime(true);
            }
        }
    }
}
