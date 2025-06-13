package me.loovcik.minimalPrice.qs.v6009.listeners;

import com.ghostchu.quickshop.api.event.management.ShopCreateEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import me.loovcik.minimalPrice.api.quickshop.Api;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import me.loovcik.core.ChatHelper;

import java.util.Optional;

public class ShopCreated implements Listener
{
	private final Api api;

	@EventHandler
	private void onShopCreate(ShopCreateEvent event){
		if (!api.isEnabled.get()) return;
		if (api.isDebug.get())
			ChatHelper.console("Checking modern 'ShopCreateEvent'...");

		Optional<Shop> shop = event.shop();
		if (shop.isPresent()) {
			double price = shop.get().getPrice();
			ItemStack itemStack = shop.get().getItem();
			Player player = null;
			if (event.user().getBukkitPlayer().isPresent())
				player = event.user().getBukkitPlayer().get();

			if (!api.process.apply(itemStack, price, player))
			{
				event.setCancelled(true, "Zbyt niska cena");
			}
		}

	}

	public ShopCreated(Api api){
		this.api = api;
	}
}