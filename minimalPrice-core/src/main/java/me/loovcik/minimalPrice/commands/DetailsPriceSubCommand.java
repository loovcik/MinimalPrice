package me.loovcik.minimalPrice.commands;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import me.loovcik.core.types.PlaceholderComponent;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.command.CommandSender;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.jetbrains.annotations.NotNull;
import me.loovcik.core.ChatHelper;
import me.loovcik.core.commands.SimpleSubCommand;
import me.loovcik.minimalPrice.MinimalPrice;
import me.loovcik.minimalPrice.prices.MPItemStack;
import me.loovcik.minimalPrice.prices.Price;

import java.util.HashMap;
import java.util.Map;

public class DetailsPriceSubCommand extends SimpleSubCommand
{
	private final MinimalPrice plugin;

	@Override
	public boolean execute(@NotNull CommandSender sender, String[] args) {
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
			ChatHelper.message(false, player, "----------------------------------------------");

			replacements.put("%price%", String.format("%.2f", price.get()));
			ChatHelper.message(false, player, plugin.configuration.messages.detailedMinimalPrice.header, replacements);
			replacements.put("%price%", String.format("%.2f", price.base));
			ChatHelper.message(false, player, plugin.configuration.messages.detailedMinimalPrice.base, replacements);
			if (mpItemStack.getItemStack().getType().equals(Material.SPAWNER)) {
				if (itemStack.getType() == Material.SPAWNER)
					if (itemStack.hasItemMeta()){
						BlockStateMeta blockStateMeta = (BlockStateMeta) itemStack.getItemMeta();
						CreatureSpawner creatureSpawner = (CreatureSpawner) blockStateMeta.getBlockState();
						EntityType entityType = creatureSpawner.getSpawnedType();
						PlaceholderComponent component = PlaceholderComponent.create(plugin.configuration.messages.detailedMinimalPrice.spawnerType);
						if (entityType != null)
							component.translatableKey("%mob%", entityType.translationKey());
						ChatHelper.message(player, component.component());
					}
			}
			else {
				if (!price.getEnchantments().isEmpty()) {
					replacements.put("%price%", String.format("%.2f", price.sumEnchantments()));
					ChatHelper.message(false, player, plugin.configuration.messages.detailedMinimalPrice.enchantmentHeader, replacements);
					for (Map.Entry<String, Double> entry : price.getEnchantments().entrySet()) {
						String[] parts = entry.getKey().split(" ");
						PlaceholderComponent component = PlaceholderComponent.create(plugin.configuration.messages.detailedMinimalPrice.enchantmentList);
						NamespacedKey namespacedKey = new NamespacedKey("minecraft", parts[0].toLowerCase());
						Registry<@NotNull Enchantment> enchantmentsRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
						Enchantment enchantment = enchantmentsRegistry.get(namespacedKey);
						if (enchantment != null)
							component.placeholder("%name%", enchantment.description());
						else component.placeholder("%name%", parts[0].toUpperCase());
						component.placeholder("%level%", parts[1]);
						component.placeholder("%price%", String.format("%.2f", entry.getValue()));

						ChatHelper.message(player, component.component());
					}
				}
				if (price.trim != null){
					replacements.clear();
					replacements.put("%price%", String.format("%.2f", price.trim.getValue() + price.trimMaterial.getValue()));
					ChatHelper.message(false, player, plugin.configuration.messages.detailedMinimalPrice.trimHeader, replacements);
					PlaceholderComponent component = PlaceholderComponent.create(plugin.configuration.messages.detailedMinimalPrice.trimName);
					NamespacedKey namespacedKey = new NamespacedKey("minecraft", price.trim.getKey().toLowerCase());
					Registry<@NotNull TrimPattern> trimPatternRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.TRIM_PATTERN);
					TrimPattern trimPattern = trimPatternRegistry.get(namespacedKey);
					if (trimPattern != null)
						component.placeholder("%name%", trimPattern.description());
					component.placeholder("%price%", String.format("%.2f", price.trim.getValue()));
					ChatHelper.message(player, component.component());
					if (price.trimMaterial != null){
						component = PlaceholderComponent.create(plugin.configuration.messages.detailedMinimalPrice.trimMaterial);
						component.translatableKey("%name%", Registry.MATERIAL.get(new NamespacedKey("minecraft", price.trimMaterial.getKey().toLowerCase())).translationKey());
						component.placeholder("%price%", String.format("%.2f", price.trimMaterial.getValue()));
						ChatHelper.message(player, component.component());
					}
				}
			}
			ChatHelper.message(false, player, "----------------------------------------------");
			return true;
		}
		else ChatHelper.message(sender, plugin.configuration.messages.playerOnlyCommand);
		return true;
	}

	public DetailsPriceSubCommand(MinimalPrice plugin){
		super(plugin, plugin.configuration.commands.check.details.command, plugin.configuration.commands.check.details.aliases, plugin.configuration.commands.check.details.permission);
		this.plugin = plugin;
	}
}