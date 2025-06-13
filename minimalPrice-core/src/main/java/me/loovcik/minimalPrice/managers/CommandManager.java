package me.loovcik.minimalPrice.managers;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import me.loovcik.minimalPrice.MinimalPrice;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CommandManager
{
	private final MinimalPrice plugin;

	/**
	 * Wykonuje listę komend, w których zamienia placeholder %player% na nazwę gracza,
	 * a także ogólne placeholdery obsługiwane przez PlaceholderAPI
	 * @param commands Lista komend do wykonania
	 * @param player Kontekst gracza
	 */
	public void run(List<String> commands, Player player){
		Map<String, String> replacements = new HashMap<>();
		replacements.put("%player%", player.getName());
		if (plugin.dependencies.placeholderAPI.isEnabled())
			for (int i = 0; i < commands.size(); i++){
				String command = commands.get(i);
				command = plugin.dependencies.placeholderAPI.process(player.getPlayer(), command);
				commands.set(i, command);
			}

		run(commands, replacements);
	}

	/**
	 * Wykonuje listę komend, uwzględniając listę zamienników
	 * @param commands Komendy do wykonania
	 * @param replacements Lista zamienników
	 */
	public void run(List<String> commands, Map<String, String> replacements){
		if (commands == null || commands.isEmpty())
			return;
		for (String command : commands){
			for (Map.Entry<String, String> entry : replacements.entrySet()){
				command = command.replaceAll(entry.getKey(), entry.getValue());
			}
			String finalCommand = command;
			Bukkit.getScheduler().runTask(plugin, () -> {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
			});
		}
	}

	/**
	 * Wykonuje listę komend, uwzględniając listę zamienników
	 * @param commands Komendy do wykonania
	 * @param replacements Lista zamienników
	 */
	public void run(Player player, List<String> commands, Map<String, String> replacements){
		if (commands == null || commands.isEmpty())
			return;
		for (String command : commands){
			for (Map.Entry<String, String> entry : replacements.entrySet()){
				command = command.replaceAll(entry.getKey(), entry.getValue());
			}

			if (plugin.dependencies.placeholderAPI.isEnabled())
				command = plugin.dependencies.placeholderAPI.process(player, command);
			String finalCommand = command;
			Bukkit.getScheduler().runTask(plugin, () -> {
				Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
			});
		}
	}

	/**
	 * Wykonuje komendę uwzględniając zamienniki oraz ogólne placeholdery obsługiwane
	 * przez PlaceholderAPI
	 * @param player Kontekst gracza
	 * @param command Komenda do wykonania
	 * @param replacements Lista zamienników
	 */
	public void run(Player player, String command, Map<String, String> replacements){
		if (plugin.dependencies.placeholderAPI.isEnabled())
			command = plugin.dependencies.placeholderAPI.process(player, command);
		run(command, replacements);
	}

	/**
	 * Wykonuje komendę uwzględniając zamienniki
	 * @param command Komenda do wykonania
	 * @param replacement Lista zamienników
	 */
	public void run(String command, Map<String, String> replacement){
		List<String> commands = new ArrayList<>();
		commands.add(command);
		run(commands, replacement);
	}

	public CommandManager(MinimalPrice plugin){
		this.plugin = plugin;
	}
}