package me.loovcik.minimalPrice.gui;

import net.kyori.adventure.text.Component;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import me.loovcik.minimalPrice.MinimalPrice;
import me.loovcik.minimalPrice.managers.ConfigurationManager;
import me.loovcik.minimalPrice.managers.GuiManager;

import java.util.HashMap;
import java.util.Map;

public class MainGui extends Gui {

	private Map<Integer, ItemStack> createItems(){
		Map<Integer, ItemStack> result = new HashMap<>();
		for (ConfigurationManager.cGuiItem item : plugin.configuration.gui.category.items){
			int slot = item.slot;
			ItemStack addedItem = createItem(item.material, item.name, item.lore, item.slot, item.type);

			result.put(slot, addedItem);
		}
		return result;
	}

	@Override
	public void handleMenu(InventoryClickEvent event){
		ItemStack item = event.getCurrentItem();
		if (item == null || !item.hasItemMeta()) return;

		NamespacedKey slotKey = new NamespacedKey(plugin, "slot");
		NamespacedKey actionKey = new NamespacedKey(plugin, "action");

		final ItemMeta meta = item.getItemMeta();
		Component categoryName = meta.displayName();
		@SuppressWarnings("DataFlowIssue") int slot = meta.getPersistentDataContainer().has(slotKey, PersistentDataType.INTEGER) ? meta.getPersistentDataContainer().get(slotKey, PersistentDataType.INTEGER) : 0;
		Actions action = Actions.valueOf(meta.getPersistentDataContainer().has(actionKey, PersistentDataType.STRING) ? meta.getPersistentDataContainer().get(actionKey, PersistentDataType.STRING) : "NONE");
		if (action == Actions.NONE) return;
		switch (action) {
			case CLOSE -> GuiManager.closeMenu(event.getClickedInventory());
			case ITEMS -> GuiManager.showDetailed(viewer, Actions.ITEMS, categoryName);
			case BOOKS -> GuiManager.showDetailed(viewer, Actions.BOOKS, categoryName);
			case TRIMS -> GuiManager.showDetailed(viewer, Actions.TRIMS, categoryName);
			case SPAWNERS -> GuiManager.showDetailed(viewer, Actions.SPAWNERS, categoryName);
		}
	}

	public MainGui(MinimalPrice plugin, Player viewer){
		super(plugin, viewer, plugin.configuration.gui.category.title, plugin.configuration.gui.category.rows, GuiType.CLICKABLE);
		this.setItems(createItems());
	}
}