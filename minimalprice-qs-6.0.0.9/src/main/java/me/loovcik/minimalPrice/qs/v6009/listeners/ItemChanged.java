package me.loovcik.minimalPrice.qs.v6009.listeners;

import com.ghostchu.quickshop.api.event.settings.type.ShopItemEvent;
import me.loovcik.minimalPrice.api.quickshop.Api;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import me.loovcik.core.ChatHelper;

public class ItemChanged implements Listener
{
	private final Api api;

	@EventHandler
	private void onItemChanged(ShopItemEvent event){
		if (!api.isEnabled.get()) return;
		if (!event.shop().isLoaded()) return;
		if (api.isDebug.get())
			ChatHelper.console("Checking modern 'ShopItemEvent'...");
		double price = event.shop().getPrice();
		ItemStack itemStack = event.updated();
		Player player = event.shop().getOwner().getBukkitPlayer().get();
		if (!api.process.apply(itemStack, price, player)) event.setCancelled(true, "Zbyt niska cena");
	}

	public ItemChanged(Api api){
		this.api = api;
	}
}