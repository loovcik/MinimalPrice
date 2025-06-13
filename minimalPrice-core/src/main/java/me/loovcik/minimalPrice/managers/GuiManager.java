package me.loovcik.minimalPrice.managers;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import me.loovcik.minimalPrice.MinimalPrice;
import me.loovcik.minimalPrice.gui.Actions;
import me.loovcik.minimalPrice.gui.DetailedGui;
import me.loovcik.minimalPrice.gui.MainGui;

public final class GuiManager
{
	public static void showCategories(Player viewer){
		if (viewer == null) return;
		new MainGui(MinimalPrice.getInstance(), viewer).show(viewer);
	}

	public static void showDetailed(Player viewer, Actions category, Component categoryName){
		if (viewer == null || category == Actions.NONE) return;
		new DetailedGui(MinimalPrice.getInstance(), viewer, category, categoryName, 1).show(viewer);
	}

	public static void showDetailed(Player viewer, Actions category, Component categoryName, int page){
		if (viewer == null || category == Actions.NONE) return;
		new DetailedGui(MinimalPrice.getInstance(), viewer, category, categoryName, page).show(viewer);
	}

	public static void closeMenu(Inventory inventory){
		if (inventory == null) return;
		inventory.close();
	}

	private GuiManager() {}
}