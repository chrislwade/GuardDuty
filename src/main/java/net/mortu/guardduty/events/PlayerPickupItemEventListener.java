package net.mortu.guardduty.events;

import net.mortu.guardduty.GuardDuty;
import net.mortu.guardduty.Utils;
import net.mortu.guardduty.guard.Guard;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerPickupItemEventListener implements Listener {
	
	final private GuardDuty plugin;
	
	public PlayerPickupItemEventListener(GuardDuty plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		if (!plugin.getConfig().getBoolean("inventory.auto-dispose", true))
			return;
		if (event.getPlayer() == null)
			return;

		Guard guard = plugin.getGuardDutyManager().getGuard(event.getPlayer());
		
		if (!guard.isOnDuty())
			return;
		
		ItemStack item = event.getItem().getItemStack();
		
		if (Utils.isItemListed(item.getTypeId(), "allowed-pickups"))
			return;
		
		if (Utils.isItemListed(item.getTypeId(), "notify-pickups"))
			Utils.sendMessage(guard.getPlayer(), Utils.getMessage("guard.drop-disposed"), item.getType().toString());
		
		event.getItem().remove();
		event.setCancelled(true);
	}

}
