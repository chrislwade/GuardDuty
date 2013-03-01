package net.mortu.guardduty.tasks;

import net.mortu.guardduty.GuardDuty;
import net.mortu.guardduty.Utils;

public class GuardAnnouncementTask implements Runnable {

	private final GuardDuty plugin;
	private int id = -1;

	public GuardAnnouncementTask(GuardDuty plugin) {
		this.plugin = plugin;

		Long interval = 20L * plugin.getConfig().getLong("schedules.announcements", 600L);
		id = plugin.getServer().getScheduler().scheduleSyncRepeatingTask(plugin, this, interval, interval);
		if (id == -1)
			plugin.getLogger().warning("Failed to start GuardDuty.GuardAnnouncementTask -- guard announcements will not be made.");
	}

	@Override
	public void run() {
		if (plugin.getGuardDutyManager().getOnDutyGuardsCount() > plugin.getConfig().getInt("thresholds.announcements", 0))
			plugin.getGuardDutyManager().broadcast(Utils.formatMessage(Utils.getMessage(plugin.getGuardDutyManager().isKosActive() ? "announcements.kos-active" : "announcements.kos-inactive")));
	}

	public boolean stopTask() {
		if (id == -1)
			return false;
		
		plugin.getServer().getScheduler().cancelTask(id);
		return true;
	}

}