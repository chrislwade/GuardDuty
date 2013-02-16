package net.mortu.guardduty;

import java.io.File;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.milkbowl.vault.economy.Economy;
import net.milkbowl.vault.economy.EconomyResponse;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;

public class GuardDuty extends JavaPlugin implements Listener {

	/*
	 * TODO:
	 * - add ability to force guard on duty upon join
	 * - add separate on/off duty permissions to work with forced on-duty option
	 * - support auto-equipping armor
	 * - support defining kits directly (that can be auto-equipped)
	 * - support completely clearing inventory when going on/off duty
	 * - define a Guard glass that inherits from player (encapsulate guard related tasks)
	 * - keep track of and save accumulated on-duty time for guards
	 * - promotion eligibility announcements / automated promotions
	 * 
	 */

	private boolean oneTimeInitializationDone = false;
	private HashMap<String, Long> onDutyGuards = null;
	private GuardClockPunchTask payTask = null;
	private GuardAnnouncementTask announcementTask = null;
	private Economy economy = null;

	@Override
	public void onEnable() {
		if (!oneTimeInitializationDone) {
			getCommand("guard").setExecutor(new GuardDutyCommandExecutor(this));
			getCommand("guards").setExecutor(new GuardsCommandExecutor(this));
			oneTimeInitializationDone = true;
		}

		if (!new File(this.getDataFolder().getPath() + File.separatorChar + "config.yml").exists())
			saveDefaultConfig();

		onDutyGuards = new HashMap<String, Long>();
		startTasks();

		if (!setupEconomy()) 
			getLogger().warning("Guard payroll disabled due to no Vault dependency found!");

		getServer().getPluginManager().registerEvents(this, this);

		String message = getMessage("enabled");
		for (Player player : getServer().getOnlinePlayers())
			if (player.hasPermission("guardduty.guard"))
				sendMessage(player, message);
	}

	@Override
	public void onDisable() {
		HandlerList.unregisterAll((Listener) this);
		stopTasks();
		onDutyGuards.clear();
		onDutyGuards = null;
	}

	@EventHandler
	public void onPlayerPickupItem(PlayerPickupItemEvent event) {
		if (!getConfig().getBoolean("inventory.auto-dispose", true))
			return;
		if (event.getPlayer() == null)
			return;

		Player guard = event.getPlayer();

		if (!isGuardOnDuty(guard))
			return;
		
		ItemStack item = event.getItem().getItemStack();
		
		if (isItemListed(item.getTypeId(), "allowed-pickups"))
			return;
		
		if (isItemListed(item.getTypeId(), "notify-pickups"))
			sendMessage(guard, getMessage("guard.drop-disposed"), item.getType().toString());

		event.getItem().remove();
		event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerDropItem(PlayerDropItemEvent event) {
		if (!getConfig().getBoolean("inventory.prevent-drops", true))
			return;
		if (event.getPlayer() == null)
			return;

		Player guard = event.getPlayer();

		if (!isGuardOnDuty(guard))
			return;
	
		if (isItemListed(event.getItemDrop().getItemStack().getTypeId(), "allowed-drops"))
			return;

		event.setCancelled(true);
	}

	@EventHandler
	public void onPlayerDeath(PlayerDeathEvent event) {
		if (getConfig().getBoolean("inventory.drop-on-death", false))
			return;
		if (event.getEntity() == null)
			return;

		Player guard = event.getEntity();

		if (!isGuardOnDuty(guard))
			return;

		event.getDrops().clear();
		sendMessage(guard, getMessage("guard.drops-disposed"));
	}

	@EventHandler
	public void onPlayerJoin(PlayerJoinEvent event) {
		if (event.getPlayer() == null)
			return;

		Player player = event.getPlayer();
		if (player.hasPermission("guardduty.guard"))
			sendMessage(player, getMessage("enabled"));
	}

	@EventHandler
	public void onPlayerQuit(PlayerQuitEvent event) {
		if (event.getPlayer() == null)
			return;

		Player player = event.getPlayer();
		if (player.hasPermission("guardduty.guard") && isGuardOnDuty(player))
			setGuardOffDuty(player);
	}

	public void reload() {
		stopTasks();
		reloadConfig();
		startTasks();
	}

	private void startTasks() {
		payTask = new GuardClockPunchTask(this);
		announcementTask = new GuardAnnouncementTask(this);
	}

	private void stopTasks() {
		announcementTask.stopTask();
		announcementTask = null;
		payTask.stopTask();
		payTask = null;
	}

	private boolean setupEconomy() {
		if (getServer().getPluginManager().getPlugin("Vault") == null)
			return false;

		RegisteredServiceProvider<Economy> provider = getServer()
				.getServicesManager().getRegistration(Economy.class);
		if (provider == null)
			return false;

		economy = provider.getProvider();
		return economy != null;
	}

	public boolean isGuardOnDuty(Player guard) {
		return onDutyGuards.containsKey(guard.getName());
	}

	public boolean isKosActive() {
		return getOnDutyGuardsCount() < getConfig().getInt("thresholds.kos", 3);
	}

	private boolean isItemListed(int item, String mode) {
		List<Integer> allowedItems = getConfig().getIntegerList(mode);

		if (allowedItems.isEmpty())
			return false;

		if (allowedItems.contains(item))
			return true;

		return false;
	}

	private void removeArmor(Player guard) {
		if (!getConfig().getBoolean("inventory.remove-off-duty", true))
			return;

		PlayerInventory inventory = guard.getInventory();

		ItemStack item = null;
		int slot = -1;

		slot = inventory.firstEmpty();
		if (slot > -1) {
			item = inventory.getHelmet();
			inventory.setHelmet(new ItemStack(0));
			inventory.setItem(slot, item);
		}
		slot = inventory.firstEmpty();
		if (slot > -1) {
			item = inventory.getChestplate();
			inventory.setChestplate(new ItemStack(0));
			inventory.setItem(slot, item);
		}
		slot = inventory.firstEmpty();
		if (slot > -1) {
			item = inventory.getLeggings();
			inventory.setLeggings(new ItemStack(0));
			inventory.setItem(slot, item);
		}
		slot = inventory.firstEmpty();
		if (slot > -1) {
			item = inventory.getBoots();
			inventory.setBoots(new ItemStack(0));
			inventory.setItem(slot, item);
		}

		sendMessage(guard, getMessage("guard.off-duty.removed-armor"));
	}

	private void wearArmor(Player guard) {
		if (!getConfig().getBoolean("inventory.wear-on-duty", true))
			return;
	}

	private void announceGuardStatus(Player guard, String mode) {
		if (getConfig().getBoolean("announce." + mode, true))
			for (Player player : getServer().getOnlinePlayers())
				sendMessage(player, getMessage("guard." + mode + ".announcement"), guard.getDisplayName());
	}

	public void setGuardOnDuty(Player guard) {
		onDutyGuards.put(guard.getName(), 0L);
		sendMessage(guard, getMessage("guard.on-duty.ready-up"));
		wearArmor(guard);
		announceGuardStatus(guard, "on-duty");
	}

	public void setGuardOffDuty(Player guard) {
		Long intervals = getGuardIntervals(guard);
		Double salary = getGuardSalary(guard);
		payGuard(guard, intervals * salary);
		onDutyGuards.remove(guard.getName());
		removeArmor(guard);
		announceGuardStatus(guard, "off-duty");
	}

	public Set<String> getOnDutyGuards() {
		return onDutyGuards.keySet();
	}
	
	public Set<String> getOffDutyGuards() {
		Set<String> names = new TreeSet<String>();
		
		for (Player player : getServer().getOnlinePlayers())
			if (player.hasPermission("guardduty.guard"))
				names.add(player.getName());
		
		names.removeAll(getOnDutyGuards());
		
		return names;
	}

	public Integer getOnDutyGuardsCount() {
		return onDutyGuards.size();
	}
	
	public Integer getOffDutyGuardsCount() {
		return getOffDutyGuards().size();
	}
	
	public Long getGuardIntervals(Player guard) {
		return onDutyGuards.get(guard.getName());
	}

	public void punchGuardClock(Player guard) {
		onDutyGuards.put(guard.getName(),
				onDutyGuards.get(guard.getName()) + 1L);
	}

	public String getMessage(String key) {
		return getConfig().getString("messages." + key,
				"&4No message defined for key = 'messages." + key + "'.");
	}

	public void sendMessage(Player player, String message, Object... args) {
		player.sendMessage(ChatColor.translateAlternateColorCodes('&',
				String.format(message, args)));
	}

	public Double getGuardSalary(Player guard) {
		Map<String, Object> salaries = getConfig().getConfigurationSection(
				"salaries").getValues(false);
		for (String permission : salaries.keySet())
			if (guard.hasPermission("guardduty.salary." + permission))
				return Double.valueOf(salaries.get(permission).toString());
		return 0.0;
	}

	public void payGuard(Player guard, Double salary) {
		if (economy != null && salary > 0.0) {
			EconomyResponse response = economy.depositPlayer(guard.getName(), salary);
			if (response.transactionSuccess())
				sendMessage(guard, getMessage("guard.payroll"), economy.format(salary));
		}
	}

}
