package me.loovcik.minimalPrice.dependencies;

import me.loovcik.minimalPrice.api.quickshop.Api;
import me.loovcik.minimalPrice.listeners.QuickShopListeners;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import me.loovcik.core.ChatHelper;
import me.loovcik.minimalPrice.MinimalPrice;
import me.loovcik.minimalPrice.managers.PriceManager;

@SuppressWarnings({"UnstableApiUsage", "CallToPrintStackTrace"})
public class QS
{
	private final MinimalPrice plugin;
	private Api api;
	private boolean isExists;
	private boolean isModern;

	public boolean isExists() { return this.isExists; }

	public Api getApi() { return api; }

	public void register() {
		if (Bukkit.getPluginManager().getPlugin("QuickShop-Hikari") != null) {
			if (Bukkit.getPluginManager().isPluginEnabled("QuickShop-Hikari")) {
				isExists = true;
				try {
					Class.forName("com.ghostchu.quickshop.api.event.settings.type.ShopItemEvent");
					isModern = true;
					try {
						Class<?> clazz = Class.forName("me.loovcik.minimalPrice.qs.v6009.Api6009");
						api = (Api) clazz.getDeclaredConstructor(Plugin.class).newInstance(plugin);
					}
					catch (Exception noSuchMethodException) {
						isModern = false;
						isExists = false;
						ChatHelper.console("<red>Internal error - Unable to register Modern QuickShop API for version 6.0.0.9!</red>");
						noSuchMethodException.printStackTrace();
					}
				}
				catch (ClassNotFoundException e) {
					isModern = false;
					try {
						Class<?> clazz = Class.forName("me.loovcik.minimalPrice.v6008.Api6008");
						api = (Api) clazz.getDeclaredConstructor(Plugin.class).newInstance(plugin);
					}
					catch (Exception exception) {
						isExists = false;
						ChatHelper.console("<red>Internal error - Unable to register Legacy QuickShop API for version 6.0.0.8!</red>");
						exception.printStackTrace();
					}
				}
			}
			else isExists = false;
		}

		if (isExists) {
			ChatHelper.console("QuickShop support: <green>Yes</green> " + (isModern ? "[<green>Modern</green>]" : "[<red>Legacy</red>]") + " (" + Bukkit.getPluginManager().getPlugin("QuickShop-Hikari").getPluginMeta().getVersion() + ")");
			assert api != null;
			api.min = (PriceManager::min);
			api.check = (PriceManager::check);
			api.isDebug = () -> plugin.configuration.general.debug;
			api.isEnabled = () -> isExists;
			api.fixOnShopLoad = () -> plugin.configuration.hooks.quickShop.fixOnShopLoad;
			api.process = ((itemStack, aDouble, player) -> QuickShopListeners.process(plugin, itemStack, aDouble, player));
		}
		else ChatHelper.console("QuickShop support: <red>No</red>");
	}

	public void unregister() {
		api = null;
		isExists = false;
		isModern = false;
	}

	public void fixPrices(Player player){
		if (!plugin.configuration.general.enabled || !plugin.configuration.hooks.quickShop.enabled) return;
		api.fixPrices(player);
	}

	public void fixPrices(){
		if (!plugin.configuration.general.enabled || !plugin.configuration.hooks.quickShop.enabled) return;
		api.fixPrices();
	}

	public QS(MinimalPrice plugin){
		this.plugin = plugin;
		register();
	}
}