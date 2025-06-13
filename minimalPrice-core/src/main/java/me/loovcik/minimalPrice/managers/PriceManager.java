package me.loovcik.minimalPrice.managers;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import me.loovcik.core.ChatHelper;
import me.loovcik.minimalPrice.MinimalPrice;
import me.loovcik.minimalPrice.prices.MPItemStack;

public final class PriceManager
{
	/** Sprawdza, czy podana cena spełnia wymogi ceny minimalnej */
	public static boolean check(ItemStack itemStack, double price){
		MPItemStack mpItemStack = new MPItemStack(itemStack);
		final MinimalPrice plugin = MinimalPrice.getInstance();
		if (plugin.configuration.general.debug)
			ChatHelper.console("check={material="+itemStack.getType()+", amount="+itemStack.getAmount()+", condition=[{price=$"+price+"} >= {price=$"+mpItemStack.price(itemStack.getAmount()).get()+"], result="+(price > mpItemStack.price(itemStack.getAmount()).get())+"}");
		return price >= mpItemStack.price(itemStack.getAmount()).get();
	}

	/** Zwraca cenę minimalną, jeśli podana cena jest mniejsza */
	public static double min(ItemStack itemStack, double price) {
		MPItemStack mpItemStack = new MPItemStack(itemStack);
		double result = Math.max(price, mpItemStack.price(itemStack.getAmount()).get());
		final MinimalPrice plugin = MinimalPrice.getInstance();
		if (plugin.configuration.general.debug)
			ChatHelper.console("min={material="+itemStack.getType()+", amount="+itemStack.getAmount()+", condition=[{price=$"+price+"} >= {price=$"+mpItemStack.price(itemStack.getAmount()).get()+"], result=$"+result+"}");
		return result;
	}

	/** Zwraca cenę minimalną danego przedmiotu */
	public static double get(ItemStack itemStack) {
		MPItemStack mpItemStack = new MPItemStack(itemStack);
		return mpItemStack.get();
	}

	/** Zwraca cenę minimalną danego materiału */
	public static double get(Material material){
		ItemStack itemStack = new ItemStack(material);
		return get(itemStack);
	}

	private PriceManager() {}
}