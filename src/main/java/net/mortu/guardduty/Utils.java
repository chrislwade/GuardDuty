package net.mortu.guardduty;

import java.util.Collection;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

public class Utils {
	
	public static GuardDuty getPlugin() {
		return (GuardDuty) Bukkit.getPluginManager().getPlugin("GuardDuty");
	}
	
	public static Logger getLogger() {
		return getPlugin().getLogger();
	}
	
	public static FileConfiguration getConfig() {
		return getPlugin().getConfig();
	}
	
	public static String getMessage(String key) {
		return getConfig().getString("messages." + key, "&4No message defined for key = 'messages." + key + "'.");
	}
	
	public static String formatMessage(String message, Object... args) {
		return ChatColor.translateAlternateColorCodes('&', String.format(message, args));
	}

	public static void sendMessage(Player player, String message, Object... args) {
		player.sendMessage(formatMessage(message, args));
	}
	
	@SuppressWarnings("rawtypes")
	public static String collectionToString(Collection collection) {
		return collection.toString().replace("[", "").replace("]", "");
	}

	public static boolean isItemListed(int item, String node) {
		List<Integer> allowedItems = getConfig().getIntegerList(node);

		if (allowedItems.isEmpty())
			return false;

		if (allowedItems.contains(item))
			return true;

		return false;
	}

}
