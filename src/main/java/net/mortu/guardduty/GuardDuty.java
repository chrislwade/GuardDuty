package net.mortu.guardduty;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;
import net.mortu.guardduty.GuardDutyCommandExecutor;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class GuardDuty extends JavaPlugin implements Listener {
	
	private boolean oneTimeInitializationDone = false;
	private HashMap<String, Long> onDutyGuards = null;
	private GuardClockPunchTask payTask = null;
	private GuardAnnouncementTask announcementTask = null;
	private Economy economy = null;
	
	@Override
	public void onEnable() {
		// register commands
		if (!oneTimeInitializationDone) {
			getCommand("guard").setExecutor(new GuardDutyCommandExecutor(this));
			oneTimeInitializationDone = true;
		}
		
		if (!new File(this.getDataFolder().getPath() + File.separatorChar + "config.yml").exists())
            saveDefaultConfig();
		
		onDutyGuards = new HashMap<String, Long>();
		payTask = new GuardClockPunchTask(this);
		announcementTask = new GuardAnnouncementTask(this);
		
		if (!setupEconomy()) {
			getLogger().warning("Guard payroll disabled due to no Vault dependency found!");
		}
		
		getServer().getPluginManager().registerEvents(this, this);
	}
	
	@Override
	public void onDisable() {
		HandlerList.unregisterAll((Listener) this);
		announcementTask.stopTask();
		announcementTask = null;
		payTask.stopTask();
		payTask = null;
		onDutyGuards.clear();
		onDutyGuards = null;
	}
	
	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		
		RegisteredServiceProvider<Economy> provider = getServer().getServicesManager().getRegistration(Economy.class);
		if (provider == null) {
			return false;
		}
		
		economy = provider.getProvider();
		return economy != null;
	}
	
	@EventHandler
	public void onPlayerLeave(PlayerQuitEvent event) {
		if (event.getPlayer() != null) {
			Player guard = event.getPlayer();
			if (isGuardOnDuty(guard)) {
				setGuardOffDuty(guard);
			}
		}
	}
	
	public boolean isGuardOnDuty(Player guard) {
		return onDutyGuards.containsKey(guard.getName());
	}
	
	public boolean isKosActive() {
		return getOnDutyGuardsCount() < getConfig().getInt("thresholds.kos", 3);
	}
	
	public void setGuardOnDuty(Player guard) {
		onDutyGuards.put(guard.getName(), 0L);
	}
	
	public void setGuardOffDuty(Player guard) {
		Long intervals = getGuardIntervals(guard);
		Double salary = getGuardSalary(guard);
		payGuard(guard, intervals * salary);
		onDutyGuards.remove(guard.getName());
	}
	
	public Set<String> getOnDutyGuards() {
		return onDutyGuards.keySet();
	}
	
	public Integer getOnDutyGuardsCount() {
		return onDutyGuards.size();
	}
	
	public Long getGuardIntervals(Player guard) {
		return onDutyGuards.get(guard.getName());
	}

	public void punchGuardClock(Player guard) {
		onDutyGuards.put(guard.getName(), onDutyGuards.get(guard.getName()) + 1L);
	}
	
	public String getMessage(String key) {
		return getConfig().getString("messages." + key, "&4No message defined for key = 'messages." + key + "'.");
	}
	
	public void sendMessage(Player player, String message, Object... args) {
		player.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format(message, args)));
	}
	
	public Double getGuardSalary(Player guard) {
		Map<String, Object> salaries = getConfig().getConfigurationSection("salaries").getValues(false);
		for (String salaryPermission : salaries.keySet()) {
			String permission = salaryPermission.replace('-', '.');
			if (guard.hasPermission(permission)) {
				return Double.valueOf(salaries.get(salaryPermission).toString());
			}
		}
		return 0.0;
	}

	public void payGuard(Player guard, Double salary) {
		if (economy != null && salary > 0.0) {
			EconomyResponse response = economy.depositPlayer(guard.getName(), salary);
			if (response.transactionSuccess()) {
				sendMessage(guard, getMessage("payroll"), economy.format(salary));
			}
		}
	}
}
