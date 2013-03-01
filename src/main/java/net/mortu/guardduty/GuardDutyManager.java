package net.mortu.guardduty;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.mortu.guardduty.guard.Guard;
import net.mortu.guardduty.guard.NullGuard;
import net.mortu.guardduty.guard.PlayerGuard;

import org.bukkit.entity.Player;

public class GuardDutyManager {
	
	final private GuardDuty plugin;
	private Map<String, PlayerGuard> guards;
	
	public GuardDutyManager(GuardDuty plugin) {
		this.plugin = plugin;
		guards = new HashMap<String, PlayerGuard>();
	}

	public boolean isGuard(Player player) {
		return player.hasPermission("guardduty.guard");
	}
	
	public Guard add(Player player) {
		return guards.put(player.getName(), new PlayerGuard(player));
	}
	
	public Guard remove(Player player) {
		Guard guard = guards.remove(player.getName());
		return guard != null ? guard : new NullGuard();
	}
	
	public Guard getGuard(Player player) {
		Guard guard = guards.get(player.getName());
		return guard != null ? guard : new NullGuard();
	}
	
	public boolean isKosActive() {
		return getOnDutyGuardsCount() < plugin.getConfig().getInt("thresholds.kos", 3);
	}
	
	public Collection<Guard> getOnDutyGuards() {
		List<Guard> onDutyGuards = new ArrayList<Guard>();
		for (Guard guard : guards.values())
			if (guard.isOnDuty())
				onDutyGuards.add(guard);
		return onDutyGuards;
	}

	public Collection<Guard> getOffDutyGuards() {
		List<Guard> offDutyGuards = new ArrayList<Guard>();
		for (Guard guard : guards.values())
			if (!guard.isOnDuty())
				offDutyGuards.add(guard);
		return offDutyGuards;
	}

	public Integer getOnDutyGuardsCount() {
		return getOnDutyGuards().size();
	}
	
	public Integer getOffDutyGuardsCount() {
		return getOffDutyGuards().size();
	}
	
	public void announceStatus(Guard guard, String mode) {
		if (Utils.getConfig().getBoolean("announce." + mode, true))
			broadcast(Utils.formatMessage(Utils.getMessage("guard." + mode + ".announcement"), guard.getDisplayName()));
	}
	
	public void guardBroadcast(String message) {
		plugin.getServer().broadcast(message, "guardduty.guard");
	}
	
	public void broadcast(String message) {
		plugin.getServer().broadcastMessage(message);
	}
	
	public void setGuardOnDuty(Guard guard) {
		guard.setOnDuty();
		announceStatus(guard, "on-duty");
	}
	
	public void setGuardOffDuty(Guard guard) {
		guard.setOffDuty();
		payGuard(guard);
		announceStatus(guard, "off-duty");
	}
	
	public void payGuard(Guard guard) {
		Double secondsOnDuty = guard.getOnDutySeconds();
		Long payPeriod = Utils.getConfig().getLong("schedules.salaries", 60L);
		Double salary = guard.getSalary();
		Double pay = Math.ceil(secondsOnDuty / payPeriod * salary);
		if (guard.deposit(pay))
			Utils.sendMessage(guard.getPlayer(), Utils.getMessage("guard.payroll"), Utils.getPlugin().getEconomy().format(pay));
	}

}
