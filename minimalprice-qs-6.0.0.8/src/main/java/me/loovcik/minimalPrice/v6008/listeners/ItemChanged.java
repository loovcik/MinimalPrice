package me.loovcik.minimalPrice.v6008.listeners;

import com.ghostchu.quickshop.api.event.details.ShopItemChangeEvent;
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
	private void onItemChanged(ShopItemChangeEvent event){
		if (!api.isEnabled.get()) return;
		if (!event.getShop().isLoaded()) return;
		if (api.isDebug.get())
			ChatHelper.console("Checking legacy 'ShopItemChangeEvent'...");
		double price = event.getShop().getPrice();
		ItemStack itemStack = event.getNewItem();
		Player player = event.getShop().getOwner().getBukkitPlayer().get();
		if (!api.process.apply(itemStack, price, player)) event.setCancelled(true, "Zbyt niska cena");
	}

	public ItemChanged(Api api){
		this.api = api;
	}
}