package me.loovcik.minimalPrice.api.quickshop;

import com.ghostchu.quickshop.api.QuickShopAPI;
import org.apache.commons.lang3.function.TriFunction;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.Plugin;

import java.util.function.BiFunction;
import java.util.function.Supplier;

public abstract class Api {
	public Plugin plugin;

	public QuickShopAPI getAPI() {
		return QuickShopAPI.getInstance();
	}

	public Supplier<Boolean> isDebug;
	public Supplier<Boolean> isEnabled;
	public Supplier<Boolean> fixOnShopLoad;

	public BiFunction<ItemStack, Double, Double> min;
	public BiFunction<ItemStack, Double, Boolean> check;
	public TriFunction<ItemStack, Double, Player, Boolean> process;

	public abstract void registerListeners();
	public abstract void fixPrices(Player player);
	public abstract void fixPrices();

	public Api(Plugin plugin) {
		this.plugin = plugin;
	}
}