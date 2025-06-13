package me.loovcik.minimalPrice.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import me.loovcik.core.ChatHelper;
import me.loovcik.minimalPrice.MinimalPrice;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Gui implements InventoryHolder
{
	protected final MinimalPrice plugin;
	protected final String title;
	protected final int rows;
	protected final Map<Integer, ItemStack> items;
	protected final GuiType type;
	protected Inventory inventory;
	protected Player viewer;

	public GuiType getGuiType() { return type; }
	protected void setItems(Map<Integer, ItemStack> items){
		this.items.clear();
		this.items.putAll(items);
	}

	public void handleMenu(InventoryClickEvent event) {}
	public void onClose(InventoryCloseEvent event) {}

	@Override
	public @NotNull Inventory getInventory() { return inventory; }

	public void show(Player viewer){
		if (viewer == null) return;
		String title = this.title.replaceAll("%player%", viewer.getName());
		title = plugin.dependencies.placeholderAPI.process(title);
		int size = this.rows * 9;
		size = Math.max(Math.min(size, 54), 9);
		inventory = Bukkit.createInventory(this, size, ChatHelper.minimessage(title));
		for (Map.Entry<Integer, ItemStack> entry : items.entrySet())
			inventory.setItem(entry.getKey(), entry.getValue());
		viewer.openInventory(inventory);
	}

	public static int getSlot(int row, int column){
		row = Math.max(Math.min(row, 6), 1);
		column = Math.max(Math.min(column, 54), 1);
		return ((row - 1) * 9) + (column - 1);
	}

	protected ItemStack createItem(final Material material, final String name, final List<String> lore, int slot, Actions action) {
		final ItemStack item = new ItemStack(material, 1);
		final ItemMeta meta = item.getItemMeta();

		meta.displayName(ChatHelper.minimessage("<italic:false>"+name));
		List<Component> lores = new ArrayList<>();
		for (String text : lore){
			text = plugin.dependencies.placeholderAPI.process(viewer, "<italic:false>"+text);
			lores.add(ChatHelper.minimessage(text));
		}
		meta.lore(lores);
		NamespacedKey slotKey = new NamespacedKey(plugin, "slot");
		NamespacedKey actionKey = new NamespacedKey(plugin, "action");
		meta.getPersistentDataContainer().set(slotKey, PersistentDataType.INTEGER, slot);
		meta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, action.name());
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
		meta.addItemFlags(ItemFlag.HIDE_DESTROYS);
		item.setItemMeta(meta);

		return item;
	}

	protected ItemStack createItem(final Material material, Component translatable, final List<String> lore, int slot, Actions action){
		final ItemStack item = new ItemStack(material, 1);
		final ItemMeta meta = item.getItemMeta();

		meta.displayName(translatable);
		List<Component> lores = new ArrayList<>();
		for (String text : lore){
			text = plugin.dependencies.placeholderAPI.process(viewer, "<italic:false>"+text);
			lores.add(ChatHelper.minimessage(text));
		}
		meta.lore(lores);
		NamespacedKey slotKey = new NamespacedKey(plugin, "slot");
		NamespacedKey actionKey = new NamespacedKey(plugin, "action");
		meta.getPersistentDataContainer().set(slotKey, PersistentDataType.INTEGER, slot);
		meta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, action.name());
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
		meta.addItemFlags(ItemFlag.HIDE_DESTROYS);
		item.setItemMeta(meta);

		return item;
	}

	protected ItemStack createComponentItem(final Material material, Component translatable, final List<Component> componentLore, int slot, Actions action){
		final ItemStack item = new ItemStack(material, 1);
		final ItemMeta meta = item.getItemMeta();

		meta.displayName(translatable);
		meta.lore(componentLore);
		NamespacedKey slotKey = new NamespacedKey(plugin, "slot");
		NamespacedKey actionKey = new NamespacedKey(plugin, "action");
		meta.getPersistentDataContainer().set(slotKey, PersistentDataType.INTEGER, slot);
		meta.getPersistentDataContainer().set(actionKey, PersistentDataType.STRING, action.name());
		meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);
		meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
		meta.addItemFlags(ItemFlag.HIDE_ADDITIONAL_TOOLTIP);
		meta.addItemFlags(ItemFlag.HIDE_DESTROYS);
		item.setItemMeta(meta);

		return item;
	}

	public Gui(MinimalPrice plugin, Player viewer, String title, int rows, GuiType type){
		this.plugin = plugin;
		this.viewer = viewer;
		this.title = title;
		this.rows = Math.min(6, Math.max(1, rows));
		this.type = type;
		items = new LinkedHashMap<>();
	}
}