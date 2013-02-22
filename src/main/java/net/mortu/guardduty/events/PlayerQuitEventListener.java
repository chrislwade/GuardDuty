package net.mortu.guardduty.events;

import net.mortu.guardduty.GuardDuty;
import net.mortu.guardduty.guard.Guard;
import net.mortu.guardduty.guard.PlayerGuard;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerQuitEventListener implements Listener {
	
	final private GuardDuty plugin;
	
	public PlayerQuitEventListener(GuardDuty plugin) {
		this.plugin = plugin;
	}
	
	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		if (event.getPlayer() == null)
			return;

		Guard guard = plugin.getGuardDutyManager().getGuard(event.getPlayer());
		
		if (guard instanceof PlayerGuard) {
			if (guard.isOnDuty())
				plugin.getGuardDutyManager().setGuardOffDuty(guard);
			plugin.getGuardDutyManager().remove(guard.getPlayer());
		}
	}
	
}
