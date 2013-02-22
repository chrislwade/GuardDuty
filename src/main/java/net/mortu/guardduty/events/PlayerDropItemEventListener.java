package net.mortu.guardduty.events;

import net.mortu.guardduty.GuardDuty;
import net.mortu.guardduty.Utils;
import net.mortu.guardduty.guard.Guard;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerDropItemEvent;

public class PlayerDropItemEventListener implements Listener {
	
	final private GuardDuty plugin;
	
	public PlayerDropItemEventListener(GuardDuty plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		if (!plugin.getConfig().getBoolean("inventory.prevent-drops", true))
			return;
		if (event.getPlayer() == null)
			return;
		
		Guard guard = plugin.getGuardDutyManager().getGuard(event.getPlayer());
		
		if (!guard.isOnDuty())
			return;
		
		if (Utils.isItemListed(event.getItemDrop().getItemStack().getTypeId(), "allowed-drops"))
			return;

		event.setCancelled(true);
	}

}
