package me.loovcik.minimalPrice.listeners;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import me.loovcik.core.ChatHelper;
import me.loovcik.minimalPrice.MinimalPrice;
import me.loovcik.minimalPrice.managers.PriceManager;

import java.util.HashMap;
import java.util.Map;

public class QuickShopListeners
{
	public static boolean process(MinimalPrice plugin, ItemStack item, double price, Player player){
		if (!plugin.configuration.general.enabled) {
			return true;
		}

		if (plugin.dependencies.quickShop.isExists() && plugin.configuration.hooks.quickShop.enabled){
			double acceptablePrice = PriceManager.get(item);
			if (acceptablePrice > price) {
				Map<String, String> replacements = new HashMap<>();
				replacements.put("%minPrice%", String.format("%.2f", acceptablePrice));
				replacements.put("%price%", String.format("%.2f", price));
				if (player != null)
					replacements.put("%player%", player.getName());
				else
					replacements.put("%player%", "<Unknown>");
				replacements.put("%item%", item.getType().name());

				if (player != null)
				{
					ChatHelper.message(false, player, plugin.configuration.hooks.quickShop.denyMessage, replacements);
					ChatHelper.console("Gracz <gold>"+player.getName()+"</gold> próbował ustawić cenę <gold>"+item.getType().name()+"</gold> w chestshopie za <gold>$"+price+"</gold>. Minimalna cena to <gold>$"+acceptablePrice+"</gold>");
					plugin.commandManager.run(player, plugin.configuration.reactions.commands, replacements);
					if (plugin.configuration.penalty.enabled && plugin.dependencies.vault.isSupported()){
						if (plugin.dependencies.vault.getEconomy().has(player, plugin.configuration.penalty.amount))
							plugin.dependencies.vault.getEconomy().withdrawPlayer(player, plugin.configuration.penalty.amount);
					}
				}
				return false;
			}
		}
		return true;
	}
}