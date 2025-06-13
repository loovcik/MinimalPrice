package me.loovcik.minimalPrice.dependencies;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import me.loovcik.core.ChatHelper;
import me.loovcik.minimalPrice.MinimalPrice;

@SuppressWarnings("UnstableApiUsage")
public class PlaceholderAPI
{
	public boolean isEnabled() {
		return Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null && Bukkit.getPluginManager().isPluginEnabled("PlaceholderAPI");
	}

	public String process(OfflinePlayer op, String input){
		if (isEnabled()) return me.clip.placeholderapi.PlaceholderAPI.setPlaceholders(op, input);
		return input;
	}

	public String process(String input){
		return process(null, input);
	}

	public PlaceholderAPI(MinimalPrice plugin){
		if (isEnabled()) {
			ChatHelper.console("PlaceholderAPI support: <green>Yes</green> ("+ Bukkit.getPluginManager().getPlugin("PlaceholderAPI").getPluginMeta().getVersion() + ")");
		}
		else ChatHelper.console("PlaceholderAPI support: <red>No</red>");
	}
}