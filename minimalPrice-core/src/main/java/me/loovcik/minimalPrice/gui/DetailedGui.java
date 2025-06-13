package me.loovcik.minimalPrice.gui;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.persistence.PersistentDataType;
import me.loovcik.core.ChatHelper;
import me.loovcik.core.types.PlaceholderComponent;
import me.loovcik.minimalPrice.MinimalPrice;
import me.loovcik.minimalPrice.managers.ConfigurationManager;
import me.loovcik.minimalPrice.managers.GuiManager;
import me.loovcik.minimalPrice.managers.PriceManager;
import me.loovcik.minimalPrice.types.Book;
import me.loovcik.minimalPrice.types.Item;
import me.loovcik.minimalPrice.types.Spawner;
import me.loovcik.minimalPrice.types.Trim;

import java.util.*;
import java.util.stream.Collectors;

public class DetailedGui extends Gui
{
	private final Actions category;
	private final Component categoryName;
	private final int page;
	private int maxPages;
	private int itemsCount;
	private int maxItemsPerPage;

	private void calculate() {
		maxItemsPerPage = (rows-1)*9;

		if (category == Actions.ITEMS)
			itemsCount = plugin.storage.getItems().size();
		else if (category == Actions.BOOKS)
			itemsCount = plugin.storage.getBooks().size();
		else if (category == Actions.TRIMS)
			itemsCount = plugin.storage.getTrims().size();
		else if (category == Actions.SPAWNERS)
			itemsCount = plugin.storage.getSpawners().size();

		double division = (double) itemsCount / maxItemsPerPage;
		maxPages = Math.max(1, (int)Math.ceil(division));
	}

	private int calculateSlot(int slot) {
		return slot - ((page - 1) * maxItemsPerPage);
	}

	private Map<Integer, ItemStack> createItems(){
		Map<Integer, ItemStack> result = new HashMap<>();
		ConfigurationManager.cGuiTemplateItem template = plugin.configuration.gui.details.template;

		int slot = 0;
		int destinationSlot;
		Map<String, String> replacements = new HashMap<>();
		if (category == Actions.ITEMS){
			Set<Item> sorted = new HashSet<>(plugin.storage.getItems()).stream().sorted(
					(Comparator.comparing(item -> item.material.name()))).collect(Collectors.toCollection(LinkedHashSet::new));

			for (Item item : sorted){
				destinationSlot = calculateSlot(slot);
				if (destinationSlot >= maxItemsPerPage) {
					slot++;
					continue;
				}
				if (destinationSlot < 0) {
					slot++;
					continue;
				}

				List<String> lores = new ArrayList<>();
				for (String lore : template.lore){
					if (lore.contains("%level%") || lore.contains("%mob%")) continue;
					lores.add(lore.replaceAll("%price%", String.format("%.2f", PriceManager.get(item.material))));
				}
				result.put(destinationSlot, createItem(item.material, ChatHelper.minimessage(template.name, Map.of("%name%", ChatHelper.asTranslatable(item.material.translationKey()))), lores, destinationSlot, Actions.NONE));
				slot++;
			}
		}

		else if (category == Actions.BOOKS){
			Set<Book> sorted = new HashSet<>(plugin.storage.getBooks()).stream().sorted(
					(Comparator.comparing(item -> item.enchantment.getKey().value()))).collect(Collectors.toCollection(LinkedHashSet::new));

			for (Book item : sorted){
				destinationSlot = calculateSlot(slot);
				if (destinationSlot >= maxItemsPerPage) {
					slot++;
					continue;
				}
				if (destinationSlot < 0) {
					slot++;
					continue;
				}
				List<String> lores = new ArrayList<>();
				String priceLore;

				for (String lore : template.lore){
					if (lore.contains("%level%")) {
						priceLore = lore;
						for (Map.Entry<Integer, Double> entry : item.levels.entrySet()){
							lores.add(priceLore.replaceAll("%price%", String.format("%.2f", entry.getValue()))
									.replaceAll("%level%", String.valueOf(entry.getKey())
									)
							);
						}
					}
					else if (lore.contains("%price%") || lore.contains("%mob%")) continue;
					else lores.add(lore);
				}
				result.put(destinationSlot, createItem(Material.ENCHANTED_BOOK, item.enchantment.description(), lores, destinationSlot, Actions.NONE));
				slot++;
			}
		}

		else if (category == Actions.TRIMS){
			Set<Trim> sorted = new HashSet<>(plugin.storage.getTrims()).stream().sorted(
					(Comparator.comparing(item -> item.pattern.key().value()))).collect(Collectors.toCollection(LinkedHashSet::new));

			for (Trim item : sorted){
				destinationSlot = calculateSlot(slot);
				if (destinationSlot >= maxItemsPerPage) {
					slot++;
					continue;
				}
				if (destinationSlot < 0) {
					slot++;
					continue;
				}
				List<String> lores = new ArrayList<>();
				ItemStack itemStack = new ItemStack(Material.valueOf(item.pattern.key().value().toUpperCase()+"_armor_trim_smithing_template".toUpperCase()));
				for (String lore : template.lore){
					if (lore.contains("%level%") || lore.contains("%mob%")) continue;
					lores.add(lore.replaceAll("%price%", String.format("%.2f", item.price)));
				}
				result.put(destinationSlot, createItem(itemStack.getType(), ChatHelper.noItalic(item.pattern.description()), lores, destinationSlot, Actions.NONE));
				slot++;
			}
		}

		else if (category == Actions.SPAWNERS){
			Set<Spawner> sorted = new HashSet<>(plugin.storage.getSpawners()).stream().sorted(
					(Comparator.comparing(item -> item.spawnerType.getKey().value()))).collect(Collectors.toCollection(LinkedHashSet::new));

			for (Spawner item : sorted){
				destinationSlot = calculateSlot(slot);
				if (destinationSlot >= maxItemsPerPage) {
					slot++;
					continue;
				}
				if (destinationSlot < 0) {
					slot++;
					continue;
				}
				List<Component> lores = new ArrayList<>();
				ItemStack itemStack = new ItemStack(Material.valueOf(item.spawnerType.getKey().getKey().toUpperCase()+"_spawn_egg".toUpperCase()));
				for (String lore : template.lore){
					if (lore.contains("%level%")) continue;
					PlaceholderComponent component = PlaceholderComponent.create(lore);
					component.placeholder("%price%", String.format("%.2f", item.price));
					component.translatableKey("%mob%", item.spawnerType.translationKey());
					lores.add(component.component());
				}
				result.put(destinationSlot, createComponentItem(itemStack.getType(), ChatHelper.asTranslatable(item.spawnerType.translationKey()), lores, destinationSlot, Actions.NONE));
				slot++;
			}
		}

		if (plugin.configuration.gui.details.close.use){
			result.put(
					plugin.configuration.gui.details.close.slot,
					createItem(
							plugin.configuration.gui.details.close.material,
							plugin.configuration.gui.details.close.name,
							plugin.configuration.gui.details.close.lore,
							plugin.configuration.gui.details.close.slot,
							Actions.CLOSE));
		}
		if (plugin.configuration.gui.details.back.use){
			result.put(
					plugin.configuration.gui.details.back.slot,
					createItem(
							plugin.configuration.gui.details.back.material,
							plugin.configuration.gui.details.back.name,
							plugin.configuration.gui.details.back.lore,
							plugin.configuration.gui.details.back.slot,
							Actions.BACK));
		}

		result.putAll(createNavBar());
		return result;
	}

	private Map<Integer, ItemStack> createNavBar() {
		Map<Integer, ItemStack> result = new HashMap<>();

		ItemStack itemStack = null;
		ItemMeta meta = null;
		int slot = 0;

		// Poprzednia strona
		if (page > 1) {
			itemStack = new ItemStack(Material.PLAYER_HEAD);
			itemStack.editMeta(SkullMeta.class, skullMeta -> {
				final UUID uuid = UUID.randomUUID();
				final PlayerProfile playerProfile = Bukkit.createProfile(uuid, uuid.toString().substring(0, 16));
				playerProfile.setProperty(new ProfileProperty("textures", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNzZlYmFhNDFkMWQ0MDVlYjZiNjA4NDViYjlhYzcyNGFmNzBlODVlYWM4YTk2YTU1NDRiOWUyM2FkNmM5NmM2MiJ9fX0="));
				skullMeta.setPlayerProfile(playerProfile);
			});

			NamespacedKey slotKey = new NamespacedKey(plugin, "slot");
			NamespacedKey actionKey = new NamespacedKey(plugin, "action");

			meta = itemStack.getItemMeta();
			meta.displayName(ChatHelper.minimessage("<italic:false><gold>Poprzednia strona</gold>"));

			slot = getSlot(rows, 4);

			meta.getPersistentDataContainer().set(slotKey, PersistentDataType.INTEGER, slot);
			meta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, Actions.PREV.name());

			itemStack.setItemMeta(meta);
			result.put(slot, itemStack);
		}

		// Bieżąca strona
		itemStack = new ItemStack(Material.PLAYER_HEAD);
		itemStack.editMeta(SkullMeta.class, skullMeta -> {
			final UUID uuid = UUID.randomUUID();
			final PlayerProfile playerProfile = Bukkit.createProfile(uuid, uuid.toString().substring(0, 16));
			playerProfile.setProperty(new ProfileProperty("textures", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDAxYWZlOTczYzU0ODJmZGM3MWU2YWExMDY5ODgzM2M3OWM0MzdmMjEzMDhlYTlhMWEwOTU3NDZlYzI3NGEwZiJ9fX0="));
			skullMeta.setPlayerProfile(playerProfile);
		});
		NamespacedKey slotKey = new NamespacedKey(plugin, "slot");
		NamespacedKey actionKey = new NamespacedKey(plugin, "action");

		meta = itemStack.getItemMeta();
		meta.displayName(ChatHelper.minimessage("<italic:false><gold>Bieżąca strona "+page+"/"+maxPages+"</gold>"));

		slot = getSlot(rows, 5);

		meta.getPersistentDataContainer().set(slotKey, PersistentDataType.INTEGER, slot);
		meta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, Actions.NONE.name());

		itemStack.setItemMeta(meta);
		result.put(slot, itemStack);

		// Następna strona
		if (maxPages > 1 && page != maxPages) {
			itemStack = new ItemStack(Material.PLAYER_HEAD);
			itemStack.editMeta(SkullMeta.class, skullMeta -> {
				final UUID uuid = UUID.randomUUID();
				final PlayerProfile playerProfile = Bukkit.createProfile(uuid, uuid.toString().substring(0, 16));
				playerProfile.setProperty(new ProfileProperty("textures", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODM5OWU1ZGE4MmVmNzc2NWZkNWU0NzJmMzE0N2VkMTE4ZDk4MTg4NzczMGVhN2JiODBkN2ExYmVkOThkNWJhIn19fQ=="));
				skullMeta.setPlayerProfile(playerProfile);
			});
			slotKey = new NamespacedKey(plugin, "slot");
			actionKey = new NamespacedKey(plugin, "action");

			meta = itemStack.getItemMeta();
			meta.displayName(ChatHelper.minimessage("<italic:false><gold>Następna strona</gold>"));

			slot = getSlot(rows, 6);

			meta.getPersistentDataContainer().set(slotKey, PersistentDataType.INTEGER, slot);
			meta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, Actions.NEXT.name());

			itemStack.setItemMeta(meta);
			result.put(slot, itemStack);
		}

		// Powrót
		itemStack = new ItemStack(Material.PLAYER_HEAD);
		itemStack.editMeta(SkullMeta.class, skullMeta -> {
			final UUID uuid = UUID.randomUUID();
			final PlayerProfile playerProfile = Bukkit.createProfile(uuid, uuid.toString().substring(0, 16));
			playerProfile.setProperty(new ProfileProperty("textures", "eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODVlOWUwODk2ZGU1ZjY0MmRmMTgwOTY0YzU3NTZlYjc3ZDUyYjdkYzdkYWYyMWU4YjkxN2RlZDdkYWQxZDJmOCJ9fX0="));
			skullMeta.setPlayerProfile(playerProfile);
		});
		slotKey = new NamespacedKey(plugin, "slot");
		actionKey = new NamespacedKey(plugin, "action");

		meta = itemStack.getItemMeta();
		meta.displayName(ChatHelper.minimessage("<italic:false><gold>Powrót do kategorii</gold>"));

		slot = getSlot(rows, 1);

		meta.getPersistentDataContainer().set(slotKey, PersistentDataType.INTEGER, slot);
		meta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, Actions.BACK.name());

		itemStack.setItemMeta(meta);
		result.put(slot, itemStack);

		return result;
	}

	@Override
	public void handleMenu(InventoryClickEvent event) {
		ItemStack item = event.getCurrentItem();
		if (item == null || !item.hasItemMeta()) return;

		NamespacedKey slotKey = new NamespacedKey(plugin, "slot");
		NamespacedKey actionKey = new NamespacedKey(plugin, "action");

		final ItemMeta meta = item.getItemMeta();
		Actions action = Actions.valueOf(meta.getPersistentDataContainer().has(actionKey, PersistentDataType.STRING) ? meta.getPersistentDataContainer().get(actionKey, PersistentDataType.STRING) : "NONE");
		if (action == Actions.NONE) return;

		switch (action) {
			case CLOSE -> GuiManager.closeMenu(event.getClickedInventory());
			case PREV -> GuiManager.showDetailed(viewer, category, categoryName, Math.max(1, page - 1));
			case NEXT -> GuiManager.showDetailed(viewer, category, categoryName, Math.min(maxPages, page + 1));
			case BACK -> GuiManager.showCategories(viewer);
		}
	}

	public DetailedGui(MinimalPrice plugin, Player viewer, Actions category, Component categoryName, int page){
		super(plugin, viewer, plugin.configuration.gui.details.title.replaceAll("%category%", ChatHelper.plainText(categoryName)), plugin.configuration.gui.details.rows, GuiType.CLICKABLE);
		this.category = category;
		this.page = page;
		this.categoryName = categoryName;
		calculate();
		this.setItems(createItems());
	}

	public DetailedGui(MinimalPrice plugin, Player viewer, Actions category, int page){
		super(plugin, viewer, plugin.configuration.gui.details.title, plugin.configuration.gui.details.rows, GuiType.CLICKABLE);
		this.category = category;
		this.page = page;
		this.categoryName = ChatHelper.minimessage(category.name());
		calculate();
		this.setItems(createItems());
	}
}