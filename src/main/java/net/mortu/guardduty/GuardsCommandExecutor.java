package net.mortu.guardduty;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Server;
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
			if (plugin.getOnDutyGuardsCount() > 0) {
				List<String> guardNames = new ArrayList<String>();
				Server server = plugin.getServer();
				for (String guardName : plugin.getOnDutyGuards()) {
					guardNames
							.add(server.getPlayer(guardName).getDisplayName());
				}
				plugin.sendMessage(player, plugin.getMessage("guards.any"),
						plugin.getOnDutyGuardsCount(), guardNames.toString()
								.replace("[", "").replace("]", ""));
			} else {
				plugin.sendMessage(player, plugin.getMessage("guards.none"));
			}
		} else {
			plugin.sendMessage(player, plugin.getMessage("guards.permission"));
		}

		return true;
	}

}
