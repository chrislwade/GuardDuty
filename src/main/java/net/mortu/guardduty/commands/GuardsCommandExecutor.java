package net.mortu.guardduty.commands;

import java.util.ArrayList;
import java.util.Collection;
import net.mortu.guardduty.GuardDuty;
import net.mortu.guardduty.Utils;
import net.mortu.guardduty.guard.Guard;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class GuardsCommandExecutor implements CommandExecutor {

	private final GuardDuty plugin;

	public GuardsCommandExecutor(GuardDuty plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command,
			String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage("You must be a player!");
			return false;
		}

		Player player = (Player) sender;

		if (player.hasPermission("guardduty.guards")) {
			Collection<Guard> guards = plugin.getGuardDutyManager().getOnDutyGuards();
			ArrayList<String> names = new ArrayList<String>();
			if (guards.size() > 0) {
				for (Guard guard : guards)
					names.add(guard.getDisplayName());
				Utils.sendMessage(player, Utils.getMessage("guards.on-duty.any"), guards.size(), Utils.collectionToString(names));
			} else
				Utils.sendMessage(player, Utils.getMessage("guards.on-duty.none"));
			
			names.clear();
			guards = plugin.getGuardDutyManager().getOffDutyGuards();
			if (guards.size() > 0) {
				for (Guard guard : guards)
					names.add(guard.getDisplayName());
				Utils.sendMessage(player, Utils.getMessage("guards.off-duty.any"), guards.size(), Utils.collectionToString(names));
			} else
				Utils.sendMessage(player, Utils.getMessage("guards.off-duty.none"));
		} else
			Utils.sendMessage(player, Utils.getMessage("guards.permission"));

		return true;
	}

}
