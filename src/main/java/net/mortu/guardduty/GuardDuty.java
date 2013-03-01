package net.mortu.guardduty;

import java.io.File;
import net.milkbowl.vault.economy.Economy;
import net.mortu.guardduty.commands.*;
import net.mortu.guardduty.events.*;
import net.mortu.guardduty.tasks.*;

import org.bukkit.event.HandlerList;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class GuardDuty extends JavaPlugin {

	/*
	 * TODO:
	 * - add ability to force guard on duty upon join
	 * - add separate on/off duty permissions to work with forced on-duty option
	 * - support auto-equipping armor
	 * - support defining kits directly (that can be auto-equipped)
	 * - support completely clearing inventory when going on/off duty
	 * - keep track of and save accumulated on-duty time for guards
	 * - promotion eligibility announcements / automated promotions
	 * 
	 */

	private boolean oneTimeInitializationDone = false;
	private Economy economy = null;
	
	private GuardDutyManager guardDutyManager;
	
	@SuppressWarnings("unused") // FIXME: remove once we can unregister commands from bukkit 
	private GuardDutyCommandExecutor guardDutyCommandExecutor;
	@SuppressWarnings("unused") // FIXME: remove once we can unregister commands from bukkit
	private GuardsCommandExecutor guardsCommandExecutor;
	
	private DeathEventListener deathEventListener;
	private PlayerPickupItemEventListener playerPickupItemEventListener;
	private PlayerDropItemEventListener playerDropItemEventListener;
	private PlayerJoinEventListener playerJoinEventListener;
	private PlayerQuitEventListener playerQuitEventListener;
	
	private GuardAnnouncementTask announcementTask = null;	

	@Override
	public void onEnable() {
		if (!oneTimeInitializationDone) {
			guardDutyManager = new GuardDutyManager(this);
			
			getCommand("guard").setExecutor(guardDutyCommandExecutor = new GuardDutyCommandExecutor(this));
			getCommand("guards").setExecutor(guardsCommandExecutor = new GuardsCommandExecutor(this));
			
			if (!setupEconomy()) 
				getLogger().warning("Guard payroll disabled due to no Vault dependency found!");
			
			oneTimeInitializationDone = true;
		}

		if (!new File(this.getDataFolder().getPath() + File.separatorChar + "config.yml").exists())
			saveDefaultConfig();

		startTasks();
		registerEvents();
		
		guardDutyManager.guardBroadcast(Utils.formatMessage(Utils.getMessage("enabled")));
	}

	@Override
	public void onDisable() {
		unregisterEvents();
		stopTasks();
	}

	private void registerEvents() {
		getServer().getPluginManager().registerEvents(deathEventListener = new DeathEventListener(this), this);
		getServer().getPluginManager().registerEvents(playerPickupItemEventListener = new PlayerPickupItemEventListener(this), this);
		getServer().getPluginManager().registerEvents(playerDropItemEventListener = new PlayerDropItemEventListener(this), this);
		getServer().getPluginManager().registerEvents(playerJoinEventListener = new PlayerJoinEventListener(this), this);
		getServer().getPluginManager().registerEvents(playerQuitEventListener = new PlayerQuitEventListener(this), this);
	}

	private void unregisterEvents() {
		HandlerList.unregisterAll(deathEventListener);
		HandlerList.unregisterAll(playerPickupItemEventListener);
		HandlerList.unregisterAll(playerDropItemEventListener);
		HandlerList.unregisterAll(playerJoinEventListener);
		HandlerList.unregisterAll(playerQuitEventListener);
	}

	public void reload() {
		stopTasks();
		reloadConfig();
		startTasks();
	}

	private void startTasks() {
		announcementTask = new GuardAnnouncementTask(this);
	}

	private void stopTasks() {
		announcementTask.stopTask();
		announcementTask = null;
	}

	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null)
			return false;

		RegisteredServiceProvider<Economy> provider = getServer().getServicesManager().getRegistration(Economy.class);
		if (provider == null)
			return false;

		economy = provider.getProvider();
		return economy != null;
	}
	
	public Economy getEconomy() {
		return economy;
	}
	
	public GuardDutyManager getGuardDutyManager() {
		return guardDutyManager;
	}
	
}
