package me.loovcik.minimalPrice.qs.v6009.listeners;

import com.ghostchu.quickshop.api.event.management.ShopLoadEvent;
import com.ghostchu.quickshop.api.shop.Shop;
import me.loovcik.minimalPrice.api.quickshop.Api;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import me.loovcik.core.ChatHelper;

public class ShopLoad implements Listener
{
	private final Api api;

	@EventHandler(ignoreCancelled = true)
	private void onShopLoad(ShopLoadEvent event){
		if (!api.isEnabled.get()) return;
		if (api.fixOnShopLoad.get() && event.shop().isPresent()){
			Shop shop = event.shop().get();
			double oldPrice = shop.getPrice();
			double acceptablePrice = api.min.apply(shop.getItem(), oldPrice);
			if (acceptablePrice > oldPrice) {
				Bukkit.getScheduler().runTask(api.plugin, () -> {
					shop.setPrice(acceptablePrice);
					shop.setDirty();
					shop.update();
					if (api.isDebug.get())
						ChatHelper.console("QSModernShopLoad={material="+shop.getItem().getType()+", price=$"+oldPrice+", acceptablePrice=$"+acceptablePrice+", result=<red>changed</red>}");
				});
			}
			else if (api.isDebug.get())
				ChatHelper.console("QSModernShopLoad={material="+shop.getItem().getType()+", price=$"+shop.getPrice()+", acceptablePrice=$"+acceptablePrice+", result=<green>OK</green>}");
		}
	}

	public ShopLoad(Api api){
		this.api = api;
	}
}