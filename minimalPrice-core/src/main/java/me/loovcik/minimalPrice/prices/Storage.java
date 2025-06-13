package me.loovcik.minimalPrice.prices;

import me.loovcik.minimalPrice.types.Book;
import me.loovcik.minimalPrice.types.Item;
import me.loovcik.minimalPrice.types.Spawner;
import me.loovcik.minimalPrice.types.Trim;

import java.util.HashSet;
import java.util.Set;

public class Storage
{
	private final Set<Item> items;
	private final Set<Book> books;
	private final Set<Trim> trims;
	private final Set<Spawner> spawners;

	public Set<Item> getItems() { return items; }
	public Set<Book> getBooks() { return books; }
	public Set<Trim> getTrims() { return trims; }
	public Set<Spawner> getSpawners() { return spawners; }

	public Storage() {
		items = new HashSet<>();
		books = new HashSet<>();
		trims = new HashSet<>();
		spawners = new HashSet<>();
	}
}