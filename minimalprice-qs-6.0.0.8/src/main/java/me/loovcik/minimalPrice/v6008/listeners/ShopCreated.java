package me.loovcik.minimalPrice.v6008.listeners;

import com.ghostchu.quickshop.api.event.modification.ShopCreateEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import me.loovcik.minimalPrice.api.quickshop.Api;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import me.loovcik.core.ChatHelper;

public class ShopCreated implements Listener
{
	private final Api api;

	@EventHandler
	private void onShopCreate(ShopCreateEvent event){
		if (!api.isEnabled.get()) return;
		if (api.isDebug.get())
			ChatHelper.console("Checking legacy 'ShopCreateEvent'...");

		Shop shop = event.getShop();
		double price = shop.getPrice();
		ItemStack itemStack = shop.getItem();
		Player player = null;
		if (event.getCreator().getBukkitPlayer().isPresent())
			player = event.getCreator().getBukkitPlayer().get();

		if (!api.process.apply(itemStack, price, player))
		{
			ChatHelper.console("Event CreateShop cancelled");
			event.setCancelled(true, "Zbyt niska cena");

		}

	}

	public ShopCreated(Api api){
		this.api = api;
	}
}