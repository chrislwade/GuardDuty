package net.mortu.guardduty.events;

import net.mortu.guardduty.GuardDuty;
import net.mortu.guardduty.Utils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinEventListener implements Listener {
	
	final private GuardDuty plugin;
	
	public PlayerJoinEventListener(GuardDuty plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (event.getPlayer() == null)
			return;

		Player player = event.getPlayer();
		if (plugin.getGuardDutyManager().isGuard(player)) {
			Utils.sendMessage(player, Utils.getMessage("enabled"));
			plugin.getGuardDutyManager().add(player);
		}
	}

}
