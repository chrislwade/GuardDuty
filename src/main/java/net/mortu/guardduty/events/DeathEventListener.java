package net.mortu.guardduty.events;

import net.mortu.guardduty.GuardDuty;
import net.mortu.guardduty.Utils;
import net.mortu.guardduty.guard.Guard;
import net.mortu.guardduty.guard.PlayerGuard;

import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class DeathEventListener implements Listener {
	
	final private GuardDuty plugin;
	
	public DeathEventListener(GuardDuty plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (event.getEntity() == null)
			return;

		Guard guard = plugin.getGuardDutyManager().getGuard(event.getEntity());
		
		if (!(guard instanceof PlayerGuard))
			return;

		if (!guard.isOnDuty()) {
			taxGuard(guard, "off-duty");
			return;
		}
		
		taxGuard(guard, "on-duty");
		
		switch (plugin.getConfig().getString("inventory.action-on-death", "keep").toLowerCase()) {
		case "keep":
			keepItems(guard);
			event.getDrops().clear();
			break;
			
		case "dispose":
			Utils.sendMessage(guard.getPlayer(), Utils.getMessage("guard.drops-disposed"));
			event.getDrops().clear();
			break;
			
		default:
			break;
		}
	}
	
	private void keepItems(Guard guard) {
		final PlayerInventory guardInventory = guard.getPlayer().getInventory();
		final ItemStack[] armor = guardInventory.getArmorContents();
		final ItemStack[] inventory = guardInventory.getContents();
		Bukkit.getScheduler().scheduleSyncDelayedTask(plugin, new Runnable() {
			@Override
			public void run() {
				guardInventory.setArmorContents(armor);
				guardInventory.setContents(inventory);
			}
		});
	}
	
	private void taxGuard(Guard guard, String state) {
		Double tax = plugin.getConfig().getDouble("death-tax." + state, 0L); 
		if (tax > 0)
			Utils.sendMessage(guard.getPlayer(), Utils.getMessage("guard." + state + ".death"), tax);
	}


}
