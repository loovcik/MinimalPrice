package me.loovcik.minimalPrice.dependencies;


import me.loovcik.core.ChatHelper;
import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import me.loovcik.minimalPrice.MinimalPrice;

@SuppressWarnings("UnstableApiUsage")
public class Vault
{
	private final MinimalPrice plugin;
	private static Economy econ = null;

	public Economy getEconomy(){ return econ; }

	public boolean isSupported() { return econ != null; }

	public Vault(MinimalPrice plugin){
		this.plugin = plugin;
		if (!setupEconomy() ) {
			ChatHelper.console("Vault support: <red>No</red>");
			plugin.getLogger().severe(String.format("[%s] - Disabled due to no Vault dependency found!", plugin.getPluginMeta().getName()));
			plugin.getServer().getPluginManager().disablePlugin(plugin);
			return;
		}
		ChatHelper.console("Vault support: <green>Yes</green> ("+ Bukkit.getPluginManager().getPlugin("Vault").getPluginMeta().getVersion() + ")");
	}

	private boolean setupEconomy() {
		if (plugin.getServer().getPluginManager().getPlugin("Vault") == null) {
			return false;
		}
		RegisteredServiceProvider<Economy> rsp = plugin.getServer().getServicesManager().getRegistration(Economy.class);
		if (rsp == null) {
			return false;
		}
		econ = rsp.getProvider();
		return econ.isEnabled();
	}
}