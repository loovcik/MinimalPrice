package me.loovcik.minimalPrice.v6008.listeners;

import com.ghostchu.quickshop.api.event.details.ShopPriceChangeEvent;
import me.loovcik.minimalPrice.api.quickshop.Api;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import me.loovcik.core.ChatHelper;

public class PriceChanged implements Listener
{
	private final Api api;

	@EventHandler
	private void onShopPriceChanged(ShopPriceChangeEvent event){
		if (!api.isEnabled.get()) return;
		if (api.isDebug.get())
			ChatHelper.console("Checking legacy 'ShopPriceChangeEvent'...");
		double price = event.getNewPrice();
		ItemStack itemStack = event.getShop().getItem();
		Player player = null;
		if (event.getShop().getOwner().getUniqueIdOptional().isPresent())
			player = Bukkit.getPlayer(event.getShop().getOwner().getUniqueIdOptional().get());

		if (!api.process.apply(itemStack, price, player))
			event.setCancelled(true, "Zbyt niska cena");
	}

	public PriceChanged(Api api){
		this.api = api;
	}
}