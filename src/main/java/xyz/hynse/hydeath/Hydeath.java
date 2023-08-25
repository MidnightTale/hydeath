package xyz.hynse.hydeath;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import xyz.hynse.hydeath.Scheduler;

public final class Hydeath extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Scheduler.runTaskForEntity(event.getEntity(), this, () -> {
            for (ItemStack itemStack : event.getDrops()) {
                Item item = event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), itemStack);
                item.canMobPickup();
                item.setInvulnerable(true);
                item.setTicksLived(200);
                item.setWillAge(true);
                item.setGlowing(true);
            }
        }, 1);
    }
}
