package me.loovcik.minimalPrice.managers;

import me.loovcik.minimalPrice.MinimalPrice;
import me.loovcik.minimalPrice.dependencies.PlaceholderAPI;
import me.loovcik.minimalPrice.dependencies.PlayerAuctions;
import me.loovcik.minimalPrice.dependencies.QS;
import me.loovcik.minimalPrice.dependencies.Vault;

public class DependenciesManager
{
	private final MinimalPrice plugin;
	public PlaceholderAPI placeholderAPI;
	public PlayerAuctions playerAuctions;
	public Vault vault;
	public QS quickShop;

	private void create(){
		vault = new Vault(plugin);
		placeholderAPI = new PlaceholderAPI(plugin);
		playerAuctions = new PlayerAuctions(plugin);
		quickShop = new QS(plugin);
	}

	public DependenciesManager(MinimalPrice plugin){
		this.plugin = plugin;
		create();
	}
}