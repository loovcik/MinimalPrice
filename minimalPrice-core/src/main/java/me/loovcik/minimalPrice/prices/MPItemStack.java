package me.loovcik.minimalPrice.prices;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.bukkit.Material;
import org.bukkit.Registry;
import org.bukkit.block.CreatureSpawner;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ArmorMeta;
import org.bukkit.inventory.meta.BlockStateMeta;
import org.bukkit.inventory.meta.EnchantmentStorageMeta;
import org.bukkit.inventory.meta.trim.ArmorTrim;
import me.loovcik.core.ChatHelper;
import me.loovcik.minimalPrice.MinimalPrice;
import me.loovcik.minimalPrice.types.Book;
import me.loovcik.minimalPrice.types.Item;
import me.loovcik.minimalPrice.types.Spawner;
import me.loovcik.minimalPrice.types.Trim;

import java.util.*;
import java.util.stream.Collectors;

public class MPItemStack
{
	private final MinimalPrice plugin;
	private final ItemStack itemStack;
	private final Price price;

	/** Pobiera przedmiot związany z ceną */
	public ItemStack getItemStack() { return itemStack; }

	/**
	 * Określa cenę minimalną, za ilość przedmiotów
	 * zgodną z ilością powiązanych przedmiotów
	 */
	public Price price() { return price(itemStack.getAmount()); }

	/**
	 * Określa cenę minimalną za określoną ilość przedmiotów
	 * danego typu
	 */
	public Price price(int amount){
		Price result = new Price();
		result.base = price.base * amount;
		if (!price.getEnchantments().isEmpty())
			for (Map.Entry<String, Double> enchant : price.getEnchantments().entrySet())
				result.getEnchantments().put(enchant.getKey(), enchant.getValue() * amount);
		if (price.trim != null)
			result.trim = new ImmutablePair<>(price.trim.getKey(), price.trim.getValue() * amount);
		if (price.trimMaterial != null)
			result.trimMaterial = new ImmutablePair<>(price.trimMaterial.getKey(), price.trimMaterial.getValue() * amount);
		return result;
	}

	/**
	 * Określa typ przedmiotu<br><br>
	 * W zależności od typu inaczej należy obliczać<br>
	 * cenę minimalną
	 */
	private ItemType getItemType(ItemStack itemStack){
		if (itemStack.getType().equals(Material.ENCHANTED_BOOK)) return ItemType.ENCHANTED_BOOK;
		if (ARMOR_TRIMS.contains(itemStack.getType())) return ItemType.TRIM;
		return ItemType.ITEM;
	}

	/** Pobiera finalną cenę przedmiotu */
	public double get() { return price.get(); }

	/** Oblicza cenę minimalną przedmiotu */
	private void calculateItem(){
		calculateItemBase();
		calculateEnchantments();
		calculateTrim();
		calculateSpawner();
		calculateBook();
		calculateSmithingTemplate();
	}

	/** Oblicza cenę bazową przedmiotu */
	private void calculateItemBase() {
		for (Item item : plugin.storage.getItems())
			if (item.material.equals(itemStack.getType()))
			{
				if (plugin.configuration.general.debug)
					ChatHelper.console("calculateItemBase={material=" + item.material + ", itemType="+getItemType(itemStack)+", price=$" + item.price + "}");
				price.base = item.price;
				return;
			}
		if (plugin.configuration.general.debug)
			ChatHelper.console("calculateItemBase={material=" + itemStack.getType() + ", itemType="+getItemType(itemStack)+", price=noLimit}");
	}

	/** Oblicza cenę bazową zaklętej książki */
	private void calculateBook(){
		if (itemStack.getType().equals(Material.ENCHANTED_BOOK)) {
			if (itemStack.hasItemMeta())
				if (itemStack.getItemMeta() instanceof EnchantmentStorageMeta meta)
					for (Map.Entry<Enchantment, Integer> enchant : meta.getStoredEnchants().entrySet())
					{
						double enchantPrice = getEnchantment(enchant.getKey(), enchant.getValue());
						if (plugin.configuration.general.debug)
							ChatHelper.console("calculateBook={material="+itemStack.getType()+", itemType="+getItemType(itemStack)+", enchant=["+enchant.getKey().getKey().value()+", "+enchant.getValue()+"], price=$"+enchantPrice+"}");
						price.getEnchantments().put(enchant.getKey().getKey().value()+" "+enchant.getValue(), enchantPrice);
					}
			if (plugin.configuration.general.debug && price.sumEnchantments() > 0)
				ChatHelper.console("calculateBook={material="+itemStack.getType()+", itemType="+getItemType(itemStack)+", totalPrice=$"+price.sumEnchantments()+"}");
		}
	}

	/** Oblicza minimalną cenę użytych zaklęć na przedmiocie */
	private void calculateEnchantments(){
		if (getItemType(itemStack) == ItemType.ITEM && !itemStack.getType().equals(Material.ENCHANTED_BOOK))
			if (itemStack.hasItemMeta())
				if (itemStack.getItemMeta().hasEnchants())
					for (Map.Entry<Enchantment, Integer> enchant : itemStack.getItemMeta().getEnchants().entrySet())
					{
						double enchantPrice = getEnchantment(enchant.getKey(), enchant.getValue());
						price.getEnchantments().put(enchant.getKey().getKey().value()+" "+enchant.getValue(), enchantPrice);
						if (plugin.configuration.general.debug)
							ChatHelper.console("calculateEnchantments={put{enchantment="+enchant.getKey().key().value()+", level="+enchant.getValue()+", price=$"+enchantPrice+"}}");
					}
		if (plugin.configuration.general.debug && !price.getEnchantments().isEmpty())
			ChatHelper.console("calculateEnchantments={material="+itemStack.getType()+", itemType="+getItemType(itemStack)+", total=$"+price.sumEnchantments()+"}");
	}

	/** Pobiera minimalną cenę danego zaklęcia */
	private double getEnchantment(Enchantment enchantment, Integer level){
		for (Book book : plugin.storage.getBooks()){
			if (book.enchantment.equals(enchantment)){
				if (book.levels.containsKey(level))
					return book.levels.get(level);
			}
		}
		return 0;
	}

	/** Oblicza cenę minimalną szablonu kowalskiego */
	private void calculateSmithingTemplate(){
		if (ARMOR_TRIMS.contains(itemStack.getType())){
			for (Trim trim : plugin.storage.getTrims())
				if ((trim.pattern.key().value()+"_armor_trim_smithing_template").equalsIgnoreCase(itemStack.getType().key().value())){
					price.base = trim.price;
					if (plugin.configuration.general.debug)
						ChatHelper.console("calculateSmithingTemplate={material="+itemStack.getType()+", smithingTemplate="+trim.pattern.key().value()+", itemType="+getItemType(itemStack)+", price=$"+trim.price+"}");
				}
		}
	}

	/**
	 * Oblicza cenę minimalną zdobienia wraz z materiałem użytym
	 * do jego wykonania
	 */
	private void calculateTrim(){
		if (itemStack.hasItemMeta())
			if (itemStack.getItemMeta() instanceof ArmorMeta armorMeta)
				if (armorMeta.hasTrim()){
					ArmorTrim armorTrim = armorMeta.getTrim();
					if (armorTrim != null)
						for (Trim trim:plugin.storage.getTrims())
							if (trim.pattern.key().value().equalsIgnoreCase(armorTrim.getPattern().key().value())){
								String trimName = armorTrim.getPattern().key().value();
								price.trim = new ImmutablePair<>(trimName, trim.price);
								Material usedMaterial = Registry.MATERIAL.get(armorTrim.getMaterial().key());
								if (usedMaterial != null)
									price.trimMaterial = new ImmutablePair<>(usedMaterial.name(), new MPItemStack(new ItemStack(usedMaterial, 1)).price.base);
								if (plugin.configuration.general.debug)
									ChatHelper.console("calculateTrim={material="+itemStack.getType()+", itemType="+getItemType(itemStack)+", trimName="+trimName+", usedMaterial="+usedMaterial+", price=$"+price.trimMaterial.getValue()+"}");
							}
				}
	}

	/**
	 * Oblicza cenę minimalną spawnera
	 * w zależności od typu spawnowanego moba
	 */
	private void calculateSpawner(){
		if (itemStack.getType() == Material.SPAWNER)
			if (itemStack.hasItemMeta()){
				BlockStateMeta blockStateMeta = (BlockStateMeta) itemStack.getItemMeta();
				CreatureSpawner creatureSpawner = (CreatureSpawner) blockStateMeta.getBlockState();
				for (Spawner spawner : plugin.storage.getSpawners()){
					if (spawner.spawnerType.equals(creatureSpawner.getSpawnedType())){
						if (plugin.configuration.general.debug)
							ChatHelper.console("calculateSpawner={material="+itemStack.getType()+", itemType="+getItemType(itemStack)+", entityType="+creatureSpawner.getSpawnedType()+", price=$"+spawner.price+"}");
						price.base = spawner.price;
					}
				}
			}
	}

	public MPItemStack(ItemStack itemStack){
		this.itemStack = itemStack;
		this.plugin = MinimalPrice.getInstance();
		this.price = new Price();
		calculateItem();
	}

	private static final Set<Material> ARMOR_TRIMS = Arrays.stream(Material.values())
			.filter(xMaterial -> xMaterial.name().toUpperCase().endsWith("TRIM_SMITHING_TEMPLATE"))
			.collect(Collectors.toCollection(() -> EnumSet.noneOf(Material.class)));


	private enum ItemType {
		ITEM,
		ENCHANTED_BOOK,
		TRIM,
		SPAWNER
	}
}