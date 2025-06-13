package me.loovcik.minimalPrice.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.InventoryHolder;
import me.loovcik.minimalPrice.gui.Gui;
import me.loovcik.minimalPrice.gui.GuiType;

public class InventoryClickEventListener implements Listener
{
	@EventHandler
	private void onInventoryClick(InventoryClickEvent event){
		if (event.getWhoClicked() instanceof Player)
			if (event.getCurrentItem() != null && event.getCurrentItem().getItemMeta() != null){
				InventoryHolder holder = event.getInventory().getHolder();
				if (holder instanceof Gui gui)
					if (gui.getGuiType() == GuiType.CLICKABLE){
						event.setCancelled(true);
						if (event.getCurrentItem() == null) return;
						gui.handleMenu(event);
					}
			}
	}
}