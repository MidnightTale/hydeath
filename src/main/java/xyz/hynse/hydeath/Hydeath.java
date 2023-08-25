package xyz.hynse.hydeath;


import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.ExperienceOrb;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.entity.Item;

public final class Hydeath extends JavaPlugin implements Listener {

    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(this, this);
        setKeepInventoryRule(true);
    }
    @Override
    public void onDisable() {
        setKeepInventoryRule(false);
    }

    private void setKeepInventoryRule(boolean value) {
        Bukkit.getGlobalRegionScheduler().run(this, scheduledTask -> {
            for (World world : Bukkit.getWorlds()) {
                world.setGameRuleValue("keepInventory", String.valueOf(value));
            }
        });
    }

    @EventHandler
    public void onPlayerDeath(PlayerDeathEvent event) {
        Scheduler.runTaskForEntity(event.getEntity(), this, () -> {
            event.getDrops().forEach(itemStack -> {
                Item item = event.getEntity().getWorld().dropItemNaturally(event.getEntity().getLocation(), itemStack);
                item.setCanMobPickup(true);
                item.setInvulnerable(true);
                item.setGlowing(true);
                item.setUnlimitedLifetime(true);
            });
            //event.getEntity().getInventory().clear();
            int expToDrop = event.getEntity().getTotalExperience();
            while (expToDrop > 0) {
                int expAmount = Math.min(expToDrop, 100);
                event.getEntity().getWorld().spawn(event.getEntity().getLocation(), ExperienceOrb.class)
                        .setExperience(expAmount);
                expToDrop -= expAmount;
            }
            event.setDroppedExp(0);
            //event.getEntity().setLevel(0);
        }, 1);
    }
}