package me.loovcik.minimalPrice.dependencies;

import com.olziedev.playerauctions.api.PlayerAuctionsAPI;
import me.loovcik.core.ChatHelper;
import org.bukkit.Bukkit;
import me.loovcik.minimalPrice.MinimalPrice;

@SuppressWarnings("deprecation")
public class PlayerAuctions
{
	private PlayerAuctionsAPI api;

	public boolean isSupported(){
		return PlayerAuctionsAPI.getInstance() != null;
	}

	public PlayerAuctionsAPI getApi(){
		return api;
	}

	public void register(){
		api = PlayerAuctionsAPI.getInstance();
		if (api == null)
			ChatHelper.console("PlayerAuctions support: <red>No</red>");
		ChatHelper.console("PlayerAuctions support: <green>Yes</green> ("+ Bukkit.getPluginManager().getPlugin("PlayerAuctions").getDescription().getVersion() + ")");
		if (api == null)
			api = PlayerAuctionsAPI.getInstance();
	}

	public PlayerAuctions(MinimalPrice plugin){
		register();
	}
}