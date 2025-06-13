package me.loovcik.minimalPrice.listeners.QuickShop;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import me.loovcik.core.ChatHelper;
import me.loovcik.minimalPrice.MinimalPrice;

public class PlayerJoin implements Listener
{
	private final MinimalPrice plugin;

	@EventHandler(ignoreCancelled = true)
	public void onPlayerJoin(PlayerJoinEvent event){
		if (!plugin.configuration.general.enabled) return;
		if (plugin.configuration.hooks.quickShop.fixOnJoin){
			if (plugin.configuration.general.debug)
				ChatHelper.console("Checking 'PlayerJoinEvent'...");
			plugin.dependencies.quickShop.fixPrices(event.getPlayer());
		}
	}

	public PlayerJoin(MinimalPrice plugin){
		this.plugin = plugin;
	}
}