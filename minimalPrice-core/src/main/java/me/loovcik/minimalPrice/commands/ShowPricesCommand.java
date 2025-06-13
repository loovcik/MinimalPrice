package me.loovcik.minimalPrice.commands;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import me.loovcik.core.ChatHelper;
import me.loovcik.core.commands.SimpleCommand;
import me.loovcik.minimalPrice.MinimalPrice;
import me.loovcik.minimalPrice.managers.GuiManager;

public class ShowPricesCommand extends SimpleCommand
{
	private final MinimalPrice plugin;

	@Override
	public boolean execute(@NotNull CommandSender sender, String[] args){
		if (sender instanceof Player player){
			GuiManager.showCategories(player);
			return true;
		}
		else ChatHelper.message(sender, plugin.configuration.messages.playerOnlyCommand);

		return true;
	}

	public ShowPricesCommand(MinimalPrice plugin){
		super(plugin, plugin.configuration.commands.main.command, plugin.configuration.commands.main.aliases.toArray(new String[0]), "Wyświetla gui z cenami minimalnymi", plugin.configuration.commands.main.permission);
		this.plugin = plugin;
	}
}