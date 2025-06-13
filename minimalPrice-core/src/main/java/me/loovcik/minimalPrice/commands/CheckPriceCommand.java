package me.loovcik.minimalPrice.commands;

import me.loovcik.minimalPrice.MinimalPrice;
import org.bukkit.Material;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import me.loovcik.core.ChatHelper;
import me.loovcik.minimalPrice.prices.MPItemStack;
import me.loovcik.minimalPrice.prices.Price;
import me.loovcik.core.commands.SimpleCommand;

import java.util.HashMap;
import java.util.Map;

public class CheckPriceCommand extends SimpleCommand
{
	private final MinimalPrice plugin;

	@Override
	public boolean execute(@NotNull CommandSender sender, String[] args){
		if (args == null) return false;

		if (!super.execute(sender, args)) {
			if (sender instanceof Player player){
				ItemStack itemStack = player.getInventory().getItemInMainHand();
				if (itemStack.getType() == Material.AIR) {
					ChatHelper.message(player, plugin.configuration.messages.noItemHeld);
					return true;
				}

				MPItemStack mpItemStack = new MPItemStack(itemStack);
				Price price = mpItemStack.price();
				if (price.get() == 0) {
					ChatHelper.message(player, plugin.configuration.messages.noRestrictionItem);
					return true;
				}

				Map<String, String> replacements = new HashMap<>();

				replacements.put("%price%", String.format("%.2f", price.get()));
				ChatHelper.message(player, plugin.configuration.messages.quickMinimalPrice, replacements);
				return true;
			}
			else ChatHelper.message(sender, plugin.configuration.messages.playerOnlyCommand);
		}
		return true;
	}

	public CheckPriceCommand(MinimalPrice plugin){
		super(plugin, plugin.configuration.commands.check.command, plugin.configuration.commands.check.aliases.toArray(new String[0]), "Pozwala sprawdzić cenę minimalną trzymanego przedmiotu", plugin.configuration.commands.check.permission);
		this.plugin = plugin;

		registerSubCommand(new DetailsPriceSubCommand(plugin));
	}
}