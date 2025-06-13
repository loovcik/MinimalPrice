package me.loovcik.minimalPrice.qs.v6009;

import com.ghostchu.quickshop.api.shop.Shop;
import me.loovcik.minimalPrice.api.quickshop.Api;
import me.loovcik.minimalPrice.qs.v6009.listeners.*;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import me.loovcik.core.ChatHelper;

import java.util.List;

public class Api6009 extends Api {
	public boolean isDebug() {
		return isDebug.get();
	}

	@Override
	public void fixPrices(Player player) {
		if (isDebug())
			ChatHelper.console("&7Checking &6"+player.getName()+"&7 chestshops...");
		fixPrices(getAPI().getShopManager().getAllShops(player.getUniqueId()));
	}

	@Override
	public void fixPrices() {
		if (isDebug())
			ChatHelper.console("&7Checking all chestshops...");
		fixPrices(getAPI().getShopManager().getAllShops());
	}

	@Override
	public void registerListeners() {
		if (isDebug())
			ChatHelper.console("Registering [<green>Modern</green>] QuickShop listeners...");
		Bukkit.getPluginManager().registerEvents(new PriceChanged(this), plugin);
		Bukkit.getPluginManager().registerEvents(new ShopCreated(this), plugin);
		Bukkit.getPluginManager().registerEvents(new ShopLoad(this), plugin);
		Bukkit.getPluginManager().registerEvents(new Interact(this), plugin);
		Bukkit.getPluginManager().registerEvents(new ItemChanged(this), plugin);
		if (isDebug())
			ChatHelper.console("QuickShop [<green>Modern</green>] listeners <green>registered</green>");
	}

	private void fixPrices(List<Shop> shops){
		Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
			int i = 0;
			for (Shop shop : shops)
			{
				i++;
				double acceptablePrice = min.apply(shop.getItem(), shop.getPrice());
				if (!check.apply(shop.getItem(), shop.getPrice())) {

					double oldPrice = shop.getPrice();
					int finalI = i;
					Bukkit.getScheduler().runTask(plugin, () -> {
						shop.setPrice(acceptablePrice);
						shop.setDirty();
						shop.update();
						if (isDebug())
							ChatHelper.console("fixPrices(shops)={pos="+finalI+", material="+shop.getItem().getType()+", price=$"+oldPrice+", acceptablePrice=$"+acceptablePrice+", result=<red>changed</red>}");
					});
				}
				else if (isDebug())
					ChatHelper.console("fixPrices(shops)={pos="+i+", material="+shop.getItem().getType()+", price=$"+shop.getPrice()+", acceptablePrice=$"+acceptablePrice+", result=<green>OK</green>}");
			}
			if (isDebug())
				ChatHelper.console("fixPrices(shops)={checked="+i+"}");
		}, 20);
	}

	public Api6009(Plugin plugin) {
		super(plugin);
	}
}