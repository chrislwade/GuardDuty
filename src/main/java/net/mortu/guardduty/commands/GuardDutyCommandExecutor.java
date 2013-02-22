package net.mortu.guardduty.commands;

import net.mortu.guardduty.GuardDuty;
import net.mortu.guardduty.Utils;
import net.mortu.guardduty.guard.Guard;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GuardDutyCommandExecutor implements CommandExecutor {

	private final GuardDuty plugin;

	public GuardDutyCommandExecutor(GuardDuty plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("You must be a player!");
			return false;
		}

		String action = args.length > 0 ? args[0] : "";
		Player player = (Player) sender;

		if (action.equalsIgnoreCase("on")) {
			doGuardOnDutyCommand(player);
		} else if (action.equalsIgnoreCase("off")) {
			doGuardOffDutyCommand(player);
		} else if (action.equalsIgnoreCase("reload")) {
			doGuardDutyReloadCommand(player);
		} else {
			Utils.sendMessage(player, command.getUsage());
		}

		return true;
	}

	private void doGuardOnDutyCommand(Player player) {
		if (!plugin.getGuardDutyManager().isGuard(player)) {
			Utils.sendMessage(player, Utils.getMessage("guard.on-duty.permission"));
			return;
		}
			
		Guard guard = plugin.getGuardDutyManager().getGuard(player);
		
		if (guard.isOnDuty())
			Utils.sendMessage(guard.getPlayer(), Utils.getMessage("guard.on-duty.already"));
		else
			plugin.getGuardDutyManager().setGuardOnDuty(guard);
	}

	private void doGuardOffDutyCommand(Player player) {
		if (!plugin.getGuardDutyManager().isGuard(player)) {		
			Utils.sendMessage(player, Utils.getMessage("guard.off-duty.permission"));
			return;
		}
		
		Guard guard = plugin.getGuardDutyManager().getGuard(player);
		
		if (guard.isOnDuty())
			plugin.getGuardDutyManager().setGuardOffDuty(guard);
		else
			Utils.sendMessage(guard.getPlayer(), Utils.getMessage("guard.off-duty.already"));
	}

	private void doGuardDutyReloadCommand(Player player) {
		if (player.hasPermission("guardduty.reload")) {
			plugin.reload();
			Utils.sendMessage(player, Utils.getMessage("reload.complete"));
		} else {
			Utils.sendMessage(player, Utils.getMessage("reload.permission"));
		}
	}

}
