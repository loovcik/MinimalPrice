package me.loovcik.minimalPrice.qs.v6009.listeners;

import com.ghostchu.quickshop.api.event.general.ShopSignUpdateEvent;
import me.loovcik.minimalPrice.api.quickshop.Api;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import me.loovcik.core.ChatHelper;

public class Interact implements Listener
{
	private final Api api;

	@EventHandler(ignoreCancelled = true)
	private void onInteract(ShopSignUpdateEvent event)
	{
		if (!api.isEnabled.get()) return;
		if (!event.getShop().isLoaded()) return;
		double oldPrice = event.getShop().getPrice();
		double acceptablePrice = api.min.apply(event.getShop().getItem(), oldPrice);
		if (acceptablePrice > oldPrice) {
			Bukkit.getScheduler().runTask(api.plugin, () -> {
				event.getShop().setPrice(acceptablePrice);
				event.getShop().setDirty();
				event.getShop().update();
				if (api.isDebug.get())
					ChatHelper.console("QSModernInteract={material="+event.getShop().getItem().getType()+", price=$"+oldPrice+", acceptablePrice=$"+acceptablePrice+", result=<red>changed</red>}");
			});
		}
		else if (api.isDebug.get())
			ChatHelper.console("QSModernInteract={material="+event.getShop().getItem().getType()+", price=$"+event.getShop().getPrice()+", acceptablePrice=$"+acceptablePrice+", result=<green>OK</green>}");
	}

	public Interact(Api api){
		this.api = api;
	}
}