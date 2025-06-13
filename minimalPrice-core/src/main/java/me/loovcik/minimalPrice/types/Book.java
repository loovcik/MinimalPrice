package me.loovcik.minimalPrice.types;

import org.bukkit.enchantments.Enchantment;

import java.util.HashMap;
import java.util.Map;

/**
 * Klasa reprezentująca ceny zaklętych książek
 */
public class Book
{
	/** Rodzaj zaklęcia na książce */
	public Enchantment enchantment;

	/** Mapa cen za poszczególne poziomy zaklęć */
	public Map<Integer, Double> levels;

	public Book(){
		levels = new HashMap<>();
	}
}