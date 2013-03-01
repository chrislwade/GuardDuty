package net.mortu.guardduty.guard;

import java.util.Date;
import java.util.Map;

import net.milkbowl.vault.economy.EconomyResponse;
import net.mortu.guardduty.Utils;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.potion.PotionEffect;

public class PlayerGuard implements Guard {

	final private Player player;
	private boolean onDuty;
	private Date onDutyDate;
	private Date offDutyDate;

	public PlayerGuard(Player guard) {
		this.player = guard;
		this.onDuty = false;
	}
	
	public Player getPlayer() {
		return player;
	}
	
	public boolean isOnDuty() {
		return onDuty;
	}
	
	public Double getOnDutySeconds() {
		return ((new Date()).getTime() - onDutyDate.getTime()) / 1000.0;
	}
	
	public Double getOffDutySeconds() {
		return ((new Date()).getTime() - offDutyDate.getTime()) / 1000.0;
	}
	
	public void setOnDuty() {
		this.onDuty = true;
		this.onDutyDate = new Date();
		wearArmor();
		manageEffects("off-duty");
	}
	
	public void setOffDuty() {
		this.onDuty = false;
		this.offDutyDate = new Date();
		removeArmor();
		manageEffects("off-duty");
	}
	
	public String getName() {
		return player.getName();
	}
	
	public String getDisplayName() {
		return player.getDisplayName();
	}
	
	public void wearArmor() {
		Utils.sendMessage(player, Utils.getMessage("guard.on-duty.ready-up"));
	}
	
	public void removeArmor() {
		if (!Utils.getConfig().getBoolean("inventory.remove-off-duty", true))
			return;

		PlayerInventory inventory = player.getInventory();

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

		Utils.sendMessage(player, Utils.getMessage("guard.off-duty.removed-armor"));
	}
	
	public void manageEffects(String mode) {
		if (Utils.getConfig().getBoolean("potion-effects." + mode + ".remove", true)) {
			for (PotionEffect effect : getPlayer().getActivePotionEffects())
				player.removePotionEffect(effect.getType());
			Utils.sendMessage(player, Utils.getMessage("guard." + mode + ".removed-effects"));
		}
	}

	public boolean deposit(Double amount) {
		if (Utils.getPlugin().getEconomy() != null && amount > 0.0) {
			EconomyResponse response = Utils.getPlugin().getEconomy().depositPlayer(getName(), amount);
			return response.transactionSuccess();
		}
		return false;
	}
	
	public boolean withdraw(Double amount) {
		if (Utils.getPlugin().getEconomy() != null && amount > 0.0) {
			EconomyResponse response = Utils.getPlugin().getEconomy().withdrawPlayer(getName(), amount);
			return response.transactionSuccess();
		}
		return false;
	}

	public Double getSalary() {
		Map<String, Object> salaries = Utils.getConfig().getConfigurationSection("salaries").getValues(false);
		for (String permission : salaries.keySet())
			if (player.hasPermission("guardduty.salary." + permission))
				return Double.valueOf(salaries.get(permission).toString());
		return 0.0;
	}

}
