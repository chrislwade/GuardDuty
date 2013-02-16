package net.mortu.guardduty;

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
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
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
			plugin.sendMessage(player, command.getUsage());
		}

		return true;
	}

	private void doGuardOnDutyCommand(Player guard) {
		if (guard.hasPermission("guardduty.guard")) {
			if (!plugin.isGuardOnDuty(guard)) {
				plugin.setGuardOnDuty(guard);
				plugin.sendMessage(guard,
						plugin.getMessage("guard.on-duty.on-duty"));
			} else {
				plugin.sendMessage(guard,
						plugin.getMessage("guard.on-duty.already"));
			}
		} else {
			plugin.sendMessage(guard,
					plugin.getMessage("guard.on-duty.permission"));
		}
	}

	private void doGuardOffDutyCommand(Player guard) {
		if (guard.hasPermission("guardduty.guard")) {
			if (plugin.isGuardOnDuty(guard)) {
				plugin.setGuardOffDuty(guard);
				plugin.sendMessage(guard,
						plugin.getMessage("guard.off-duty.off-duty"));

			} else {
				plugin.sendMessage(guard,
						plugin.getMessage("guard.off-duty.already"));
			}
		} else {
			plugin.sendMessage(guard,
					plugin.getMessage("guard.off-duty.permission"));
		}
	}

	private void doGuardDutyReloadCommand(Player player) {
		if (player.hasPermission("guardduty.reload")) {
			plugin.reload();
			plugin.sendMessage(player, plugin.getMessage("reload.complete"));
		} else {
			plugin.sendMessage(player, plugin.getMessage("reload.permission"));
		}
	}

}
