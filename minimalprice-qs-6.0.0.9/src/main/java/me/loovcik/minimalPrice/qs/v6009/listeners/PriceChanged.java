package me.loovcik.minimalPrice.qs.v6009.listeners;

import com.ghostchu.quickshop.api.event.settings.type.ShopPriceEvent;
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
	private void onShopPriceChanged(ShopPriceEvent event){
		if (!api.isEnabled.get()) return;
		if (event.phase().cancellable()) {
			if (api.isDebug.get())
				ChatHelper.console("Checking modern 'ShopPriceEvent'...");
			double price = event.updated();
			ItemStack itemStack = event.shop().getItem();
			Player player = null;
			if (event.shop().getOwner().getUniqueIdOptional().isPresent())
				player = Bukkit.getPlayer(event.shop().getOwner().getUniqueIdOptional().get());

			if (!api.process.apply(itemStack, price, player)) {
				if (event.phase().cancellable())
					event.setCancelled(true, "Zbyt niska cena");
			}
		}
	}

	public PriceChanged(Api api){
		this.api = api;
	}
}