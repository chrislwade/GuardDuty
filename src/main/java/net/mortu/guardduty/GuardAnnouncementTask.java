package net.mortu.guardduty;

import org.bukkit.entity.Player;

public class GuardAnnouncementTask implements Runnable {

	private final GuardDuty plugin;
	private int id = -1;

	public GuardAnnouncementTask(GuardDuty plugin) {
		this.plugin = plugin;

		Long interval = plugin.getConfig().getLong("schedules.announcements",
				600L) * 20L;
		id = plugin.getServer().getScheduler()
				.scheduleSyncRepeatingTask(plugin, this, interval, interval);
		if (id == -1) {
			plugin.getLogger()
					.warning(
							"Failed to start GuardDuty.GuardAnnouncementTask -- guard announcements will not be made.");
		}
	}

	@Override
	public void run() {
		if (plugin.getOnDutyGuardsCount() > plugin.getConfig().getInt(
				"thresholds.announcements", 0)) {
			String message = plugin
					.getMessage(plugin.isKosActive() ? "announcements.kos-active"
							: "announcements.kos-inactive");

			for (Player player : plugin.getServer().getOnlinePlayers()) {
				plugin.sendMessage(player, message);
			}
		}
	}

	public boolean stopTask() {
		if (id != -1) {
			plugin.getServer().getScheduler().cancelTask(id);
			return true;
		}
		return false;
	}

}