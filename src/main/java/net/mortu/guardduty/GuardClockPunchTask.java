package net.mortu.guardduty;

import org.bukkit.entity.Player;

public class GuardClockPunchTask implements Runnable {

	private final GuardDuty plugin;
	private int id = -1;

	public GuardClockPunchTask(GuardDuty plugin) {
		this.plugin = plugin;

		Long interval = plugin.getConfig().getLong("schedules.salaries", 60L) * 20L;
		id = plugin.getServer().getScheduler()
				.scheduleSyncRepeatingTask(plugin, this, interval, interval);
		if (id == -1) {
			plugin.getLogger()
					.warning(
							"Failed to start GuardDuty.GuardClockPunchTask -- guards will not be paid.");
		}
	}

	@Override
	public void run() {
		for (String playerName : plugin.getOnDutyGuards()) {
			Player guard = plugin.getServer().getPlayer(playerName);

			if (guard != null) {
				plugin.punchGuardClock(guard);
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
