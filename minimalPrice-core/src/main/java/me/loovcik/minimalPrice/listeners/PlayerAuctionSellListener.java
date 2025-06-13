package me.loovcik.minimalPrice.listeners;

import com.olziedev.playerauctions.api.events.auction.PlayerAuctionSellEvent;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import me.loovcik.core.ChatHelper;
import me.loovcik.minimalPrice.MinimalPrice;
import me.loovcik.minimalPrice.managers.PriceManager;

import java.util.HashMap;
import java.util.Map;

public class PlayerAuctionSellListener implements Listener
{
	private final MinimalPrice plugin;

	@EventHandler(ignoreCancelled = true)
	private void onPlayerSellItem(PlayerAuctionSellEvent event){
		if (!plugin.configuration.general.enabled) return;
		if (plugin.dependencies.playerAuctions.isSupported() && plugin.configuration.hooks.playerAuctions.enabled){
			if (!event.getPlayerAuction().getCurrency().getName().equalsIgnoreCase("Vault Currency")) return;

			double price = event.getPlayerAuction().getPrice();
			ItemStack item = event.getPlayerAuction().getItem();
			double acceptablePrice = PriceManager.min(item, price);
			if (acceptablePrice > price){
				Map<String, String> replacements = new HashMap<>();
				replacements.put("%minPrice%", String.format("%.2f", acceptablePrice));
				replacements.put("%price%", String.format("%.2f", price));
				replacements.put("%player%", event.getSeller().getName());
				replacements.put("%item%", item.getType().name());
				ChatHelper.message(false, event.getSeller(), plugin.configuration.hooks.playerAuctions.denyMessage, replacements);
				ChatHelper.console("&eGracz "+event.getSeller().getName()+" próbował sprzedać &6"+item.getAmount()+"x "+item.getType().name()+"&r na aukcji za $"+String.format("%.2f", price)+". Minimalna cena to $"+String.format("%.2f", acceptablePrice));

				if (plugin.configuration.penalty.enabled && plugin.dependencies.vault.isSupported()){
					if (plugin.dependencies.vault.getEconomy().has(event.getSeller().getPlayer(), plugin.configuration.penalty.amount)){
						plugin.dependencies.vault.getEconomy().withdrawPlayer(event.getSeller().getPlayer(), plugin.configuration.penalty.amount);
						ChatHelper.message(event.getSeller(), "");
					}
				}
				event.setCancelled(true);
				plugin.commandManager.run(event.getSeller(), plugin.configuration.reactions.commands, replacements);
			}
		}
	}

	public PlayerAuctionSellListener(MinimalPrice plugin){
		this.plugin = plugin;
	}
}