package me.loovcik.minimalPrice.managers;

import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.meta.trim.TrimPattern;
import org.jetbrains.annotations.NotNull;
import me.loovcik.core.ChatHelper;
import me.loovcik.minimalPrice.MinimalPrice;
import me.loovcik.minimalPrice.gui.Actions;
import me.loovcik.minimalPrice.types.Book;
import me.loovcik.minimalPrice.types.Item;
import me.loovcik.minimalPrice.types.Spawner;
import me.loovcik.minimalPrice.types.Trim;

import java.util.*;
import java.util.stream.Collectors;

@SuppressWarnings("CallToPrintStackTrace")
public class ConfigurationManager extends me.loovcik.core.managers.ConfigurationManager
{
	/** Wewnętrzna obsługa pliku konfiguracyjnego **/
	private final FileConfiguration config;
	private final MinimalPrice plugin;

	public final cGeneral general = new cGeneral();
	public final cHooks hooks = new cHooks();
	public final cReactions reactions = new cReactions();
	public final cPenalty penalty = new cPenalty();
	public final cMessages messages = new cMessages();
	public final cGui gui = new cGui();
	public final cCommands commands = new cCommands();

	@Override
	public void load(){
		general.prefix = getString("global.prefix", "&8[MP]&r", "Prefix używany na czacie (nie uwzględniany przy komunikatach\no odmowie sprzedaży - tam należy ręcznie dać taki\nsam prefix jak pluginu\nktórego dotyczy)");
		general.debug = getBoolean("global.debug", false, "Włączenie wyświetla dodatkowe informacje debugowania\nNormalnie nie należy tego włączać");
		general.enabled = getBoolean("global.enabled", true, "Możliwość wyłączenia działania pluginu bez konieczności\njego usuwania");

		hooks.placeholderAPI = getBoolean("hooks.placeholderAPI", true, "Włącza obsługę PlaceholderAPI w komunikatach");
		hooks.playerAuctions.enabled = getBoolean("hooks.playerAuctions.enabled", true, "Włącz integrację z PlayerAuctions (/ah)");
		hooks.quickShop.enabled = getBoolean("hooks.quickShop.enabled", false, "Włącza integrację z QuickShop (chestshopy)");
		hooks.playerAuctions.denyMessage = getString("hooks.playerAuctions.denyMessage", "&8[&lRynek&r&8]&r &cNie możesz wystawić tego przedmiotu. Jego minimalna cena wynosi &6%minPrice%&c!", "Wiadomość wysyłana do gracza, który\npróbuje wystawić przedmiot na /ah poniżej\nceny minimalnej");
		hooks.quickShop.denyMessage = getString("hooks.quickShop.denyMessage", "&8[&lQS&r&8]&r &cNie możesz sprzedawać tego przedmiotu poniżej ceny &6%minPrice%&c!", "Wiadomość wysyłana do gracza, który\npróbuje sprzedawać przedmiot w chestshopie poniżej\nceny minimalnej");
		hooks.quickShop.fixOnJoin = getBoolean("hooks.quickShop.fixOnJoin", false, "Sprawdza sklepy gracza podczas jego dołączania\nna serwer i koryguje ceny");
		hooks.quickShop.fixOnStart = getBoolean("hooks.quickShop.fixOnStart", false, "Poprawia niezgodne ceny przy starcie");
		hooks.quickShop.fixOnShopLoad = getBoolean("hooks.quickShop.fixOnShopLoad", true, "Poprawia niezgodne ceny w momencie\nwczytywania sklepu przez plugin");

		penalty.enabled = getBoolean("penalty.enabled", false, "Włącza funkcję karania za zaniżanie cen");
		penalty.amount = getDouble("penalty.amount", 200.0, "Określa wysokość kary za próbę sprzedaży\nponiżej ceny minimalnej");
		penalty.message = getString("penalty.message", "&cZ Twojego konta została pobrana kara &6$%amount%&c za próbę sprzedaży poniżej ceny minimalnej", "Komunikat wysłany do gracza, który\nzostanie obciążony karą za próbę sprzedaży\nprzedmiotów poniżej ich ceny minimalnej");

		reactions.commands = getList("reactions.commands", List.of(), "Poniższe komendy zostaną wykonane, jeśli gracz\nspróbuje sprzedać przedmiot poniżej ceny minimalnej.\nDostępne placeholdery:\n- %player%\n- %minPrice%\n- %price%\n- %item%");

		ConfigurationSection itemsSection = getConfigurationSection("prices.items", Map.of());
		ConfigurationSection booksSection = getConfigurationSection("prices.books", Map.of());
		ConfigurationSection trimsSection = getConfigurationSection("prices.trims", Map.of());
		ConfigurationSection spawnersSection = getConfigurationSection("prices.spawners", Map.of());

		// Load items
		plugin.storage.getItems().addAll(itemsSection.getValues(false).entrySet()
				.stream()
				.map(entry -> {
					Item item = new Item();
					item.material = Material.getMaterial(entry.getKey().toUpperCase());
					if (item.material == null) {
						ChatHelper.console("&cUnable to resolve "+entry.getKey()+" material!");
						return null;
					}
					if (item.material.equals(Material.ENCHANTED_BOOK)){
						ChatHelper.console("&cEnchanted book cannot be declared on items section!");
						return null;
					}
					if (entry.getValue() instanceof Integer integer) item.price = Double.parseDouble(String.valueOf(integer));
					else item.price = (double) entry.getValue();
					if (plugin.configuration.general.debug)
						ChatHelper.console("&aAdded item "+item.material.name().toLowerCase()+": &6$"+item.price);
					return item;
				})
				.filter(Objects::nonNull)
				.collect(Collectors.toCollection(HashSet::new)));

		// Load books
		plugin.storage.getBooks().addAll(booksSection.getValues(false).keySet()
				.stream()
				.map(configuredEnchantment -> {
					Book book = new Book();

					NamespacedKey namespacedKey = new NamespacedKey("minecraft", configuredEnchantment.toLowerCase());
					Registry<@NotNull Enchantment> enchantmentsRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.ENCHANTMENT);
					book.enchantment = enchantmentsRegistry.get(namespacedKey);
					if (book.enchantment == null)
					{
						ChatHelper.console("&cUnable to resolve book enchant " + configuredEnchantment + "!");
						return null;
					}
					ConfigurationSection bookEnchantSection = booksSection.getConfigurationSection(configuredEnchantment);
					if (bookEnchantSection == null)
					{
						ChatHelper.console("&cBad levels configuration for " + configuredEnchantment + " book!");
						return null;
					}
					for (var enchantLevel : bookEnchantSection.getValues(false).entrySet())
					{
						try
						{
							int level = Integer.parseInt(enchantLevel.getKey());
							double price = 0;
							if (enchantLevel.getValue() instanceof Integer integer) price = Double.parseDouble(String.valueOf(integer));
							else price = (double) enchantLevel.getValue();
							book.levels.put(level, price);
							if (plugin.configuration.general.debug)
								ChatHelper.console("&aAdded book "+book.enchantment.getKey().getKey()+" "+level+": &6$"+price);
						}
						catch (ClassCastException e)
						{
							ChatHelper.console("&cPrice for " + configuredEnchantment + " " + enchantLevel.getKey() + " have wrong format!");
							return null;
						}
					}
					return book;
				})
				.filter(Objects::nonNull)
				.collect(Collectors.toCollection(HashSet::new)));

		// Load trims
		plugin.storage.getTrims().addAll(trimsSection.getValues(false).entrySet()
				.stream()
				.map(configuredType -> {
					Trim trim = new Trim();
					try
					{
						NamespacedKey namespacedKey = new NamespacedKey("minecraft", configuredType.getKey().toLowerCase());
						Registry<@NotNull TrimPattern> trimPatternRegistry = RegistryAccess.registryAccess().getRegistry(RegistryKey.TRIM_PATTERN);
						trim.pattern = trimPatternRegistry.get(namespacedKey);
						//trim.pattern = Registry.TRIM_PATTERN.get(NamespacedKey.minecraft(configuredType.getKey().toLowerCase()));
						if (trim.pattern == null)
						{
							ChatHelper.console("&cUnable to resolve trim " + configuredType.getKey() + "!");
							return null;
						}

						double price = 0;
						if (configuredType.getValue() instanceof Integer integer) price = Double.parseDouble(String.valueOf(integer));
						else price = (double) configuredType.getValue();
						if (plugin.configuration.general.debug)
							ChatHelper.console("&aAdded trim "+ChatHelper.plainText(trim.pattern.description())+": &6$"+price);
						trim.price = price;
						return trim;
					}
					catch (Exception e){
						e.printStackTrace();
						ChatHelper.console("&cUnable to resolve trim " + configuredType.getKey() + "!");
						return null;
					}
				})
				.filter(Objects::nonNull)
				.collect(Collectors.toCollection(HashSet::new)));

		// Load spawners
		plugin.storage.getSpawners().addAll(spawnersSection.getValues(false).entrySet()
				.stream()
				.map(configuredType -> {
					Spawner spawner = new Spawner();
					try
					{
						spawner.spawnerType = Registry.ENTITY_TYPE.get(NamespacedKey.minecraft(configuredType.getKey()));
						if (spawner.spawnerType == null){
							ChatHelper.console("<red>Unable to resolve spawner type "+configuredType.getKey()+"!");
							return null;
						}
						double price = 0;
						if (configuredType.getValue() instanceof Integer integer) price = Double.parseDouble(String.valueOf(configuredType.getValue()));
						else price = (double) configuredType.getValue();
						if (plugin.configuration.general.debug)
							ChatHelper.console("&aAdded spawner type "+spawner.spawnerType.getKey().getKey()+": &6$"+price);
						spawner.price = price;
						return spawner;
					}
					catch (Exception e){
						e.printStackTrace();
						ChatHelper.console("&cUnable to resolve spawner " + configuredType.getKey() + "!");
						return null;
					}
				})
				.filter(Objects::nonNull)
				.collect(Collectors.toCollection(HashSet::new)));

		gui.category.title = getString("gui.category.title", "Ceny minimalne");
		gui.category.rows = getInt("gui.category.rows", 4);
		ConfigurationSection guiCategoryItemsSection = getConfigurationSection("gui.category.items", Map.of());
		gui.category.items.addAll(guiCategoryItemsSection.getValues(false).keySet()
				.stream()
				.map(key -> {
					try {
						cGuiItem item = new cGuiItem();
						item.slot = Integer.parseInt(key);
						ConfigurationSection itemSection = guiCategoryItemsSection.getConfigurationSection(key);
						if (itemSection == null)
						{
							ChatHelper.console("&cBad gui configuration for " + key + " index!");
							return null;
						}
						item.name = itemSection.getString("name");
						String materialName = itemSection.getString("material");
						if (materialName == null) materialName = "REDSTONE";
						item.material = Material.getMaterial(materialName);
						if (item.material == null) {
							ChatHelper.console("<red>Unknown material "+itemSection.getString("material")+" in gui items "+item.slot);
							return null;
						}
						item.type = Actions.valueOf(itemSection.getString("type"));
						item.lore.addAll(itemSection.getStringList("lore"));
						return item;
					}
					catch (Exception e){
						ChatHelper.console("<red>Unable to parse "+key+" gui position");
						e.printStackTrace();
						return null;
					}
				})
				.filter(Objects::nonNull)
				.collect(Collectors.toCollection(HashSet::new)));

		gui.details.title = getString("gui.details.title", "Ceny minimalne - %category%");
		gui.details.rows = getInt("gui.details.rows", 5);
		gui.details.template.name = getString("gui.details.template.name", "<gold>%name%</gold>");
		gui.details.template.lore.addAll(getList("gui.details.template.lore", List.of()));
		gui.details.back.use = getBoolean("gui.details.back.use", true);
		gui.details.back.material = Material.valueOf(getString("gui.details.back.material", "ARROW"));
		gui.details.back.name = getString("gui.details.back.name", "<gold>< Powrót</gold>");
		gui.details.back.slot = getInt("gui.details.back.slot", 30);
		gui.details.back.lore = getList("gui.details.back.lore", List.of());

		gui.details.close.use = getBoolean("gui.details.close.use", false);
		gui.details.close.material = Material.valueOf(getString("gui.details.close.material", "BARRIER"));
		gui.details.close.name = getString("gui.details.close.name", "<red>Zamknij</red>");
		gui.details.close.slot = getInt("gui.details.close.slot", 30);
		gui.details.close.lore = getList("gui.details.close.lore", List.of());

		commands.main.command = getString("commands.main.command", "minimalprices");
		commands.main.aliases.clear();
		commands.main.aliases.addAll(getList("commands.main.aliases", List.of("cenyminimalne")));
		commands.main.permission = getString("commands.main.permission", "minimalprice.command.gui");
		commands.check.command = getString("commands.check.command", "checkprice");
		commands.check.aliases.clear();
		commands.check.aliases.addAll(getList("commands.check.aliases", List.of("sprawdzcene")));
		commands.check.permission = getString("commands.check.permission", "minimalprice.command.check");
		commands.check.details.command = getString("commands.check.details.command", "details");
		commands.check.details.aliases.clear();
		commands.check.details.aliases.addAll(getList("commands.check.details.aliases", List.of("szczegoly")));
		commands.check.details.permission = getString("commands.check.details.permission", "minimalprice.command.check.details");

		messages.noItemHeld = getString("messages.noItemHeld", "<red>Musisz trzymać przedmiot w łapce", "Komunikat wyświetlany, gdy gracz chce sprawdzić\ncenę minimalną przedmiotu, ale nie trzyma\nniczego w łapce");
		messages.playerOnlyCommand = getString("messages.playerOnlyCommand", "<red>Tej komendy nie można wykonać z konsoli", "Komunikat przy próbie wykonania\nkomendy z konsoli, jeśli jest\nona przeznaczona tylko dla graczy");
		messages.noRestrictionItem = getString("messages.noRestrictionItem", "<green>Ten przedmiot nie posiada ceny minimalnej", "Komunikat pokazywany, gdy gracz\nsprawdza cenę minimalną przedmiotu\nktóry jej nie posiada");
		messages.quickMinimalPrice = getString("messages.quickPrice", "<gray>Minimalna cena: <gold>$%price%</gold>", "Komunikat przy sprawdzaniu minimalnej ceny\nprzedmiotu w wersji skróconej");
		messages.noPermissions = getString("messages.noPermissions", "<red>Nie możesz użyć tej komendy");
		messages.detailedMinimalPrice.header = getString("messages.detailed.header", "<gray>Minimalna cena: <gold>%price%</gold> z powodu:");
		messages.detailedMinimalPrice.base = getString("messages.detailed.base", "<gray>Cena bazowa: <gold>$%price%</gold>");
		messages.detailedMinimalPrice.enchantmentHeader = getString("messages.detailed.enchantments.header", "<gray>+ Zaklęcia: <gold>$%price%</gold>");
		messages.detailedMinimalPrice.enchantmentList = getString("messages.detailed.enchantments.list", "<gray>   > %name% <green>%level%</green>: <gold>$%price%</gold>");
		messages.detailedMinimalPrice.trimHeader = getString("messages.detailed.trim.header", "<gray>+ Zdobienie: <gold>$%price%</gold>");
		messages.detailedMinimalPrice.trimName = getString("messages.detailed.trim.name", "<gray>   > %name%: <gold>$%price%</gold>");
		messages.detailedMinimalPrice.trimMaterial = getString("messages.detailed.trim.material", "<gray>   > %name%: <gold>$%price%</gold>");
		messages.detailedMinimalPrice.spawnerType = getString("messages.detailed.spawnerType", "<gray>(rodzaj <gold>%mob%</gold>)");

		plugin.saveConfig();
	}

	private void structureConfig(){

		createSection("global", String.join(System.lineSeparator()));
		createSection("maxDifference",
				"""
						-----------------------------------------------------
						Ustawienia maksymalnego odchylenia ceny względem
						ustalonej ceny minimalnej
						-----------------------------------------------------
						""");
		createSection("hooks", """
						-----------------------------------------------------
						Ustawienia wsparcia pluginów zewnętrznych
						-----------------------------------------------------""");
		createSection("reactions", """
						-----------------------------------------------------
						Ustawienia zachowania po wykryciu próby
						sprzedaży poniżej minimalnej ceny
						-----------------------------------------------------
						""");
		createSection("penalty", """
						-----------------------------------------------------
						Pozwala na nakładanie kar finansowych na graczy,
						którzy próbują sprzedaży przedmiotów poniżej
						ich ceny minimalnej.
						-----------------------------------------------------
						""");
		createSection("prices", """
						-----------------------------------------------------
						Ustawienia cen minimalnych dla poszczególnych
						rodzajów przedmiotów
						-----------------------------------------------------
						""");
		createSection("prices.items", """

						Minimalne ceny przedmiotów
						
						ID itemów:
						https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/Material.html
						-----------------------------------------------------
						""");
		createSection("prices.books", """

						Minimalne ceny zaklętych książek i enchantów
						nałożonych na przedmioty
						
						ID Enchantów:
						https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/enchantments/Enchantment.html
						-----------------------------------------------------
						""");
		createSection("prices.trims", """

						Minimalne ceny zdobień, zarówno jako przedmiot
						jak i tych nałożonych na zbrojkę
						
						Nazwy trimów:
						https://hub.spigotmc.org/javadocs/bukkit/org/bukkit/inventory/meta/trim/TrimPattern.html
						-----------------------------------------------------
						""");
	}

	@Override
	public String getHeader()
	{
		List<String> globalTitle = new ArrayList<>();
		globalTitle.add("""
				  /\\/\\ (_)_ __ (_)_ __ ___   __ _| |   / _ \\_ __(_) ___ ___\s
				 /    \\| | '_ \\| | '_ ` _ \\ / _` | |  / /_)/ '__| |/ __/ _ \\
				/ /\\/\\ \\ | | | | | | | | | | (_| | | / ___/| |  | | (_|  __/
				\\/    \\/_|_| |_|_|_| |_| |_|\\__,_|_| \\/    |_|  |_|\\___\\___|
								                 (C) 2025 Loovcik\s
				
				=================================================================
				Plugin for setting minimum prices in PlayerAuctions and QuickShop
				=================================================================
				
				 Supported color formatting:
				   - Legacy &
				   - MiniMessage
				 
				=================================================================
				 
				 
				-----------------------------------------------------
				Ogólne ustawienia
				-----------------------------------------------------
				""");
		return String.join("\n", globalTitle);
	}

	public ConfigurationManager(MinimalPrice plugin){
		super(plugin);
		this.plugin = plugin;
		this.config = plugin.getConfig();
		config.options().copyDefaults(true);
		structureConfig();
	}

	public static class cGeneral {
		public boolean enabled;
		public String prefix;
		public boolean debug;
	}

	public static class cHooks {
		public boolean placeholderAPI;
		public cHookPaExt playerAuctions = new cHookPaExt();
		public cHookQsExt quickShop = new cHookQsExt();
	}

	public static class cHookPaExt {
		public boolean enabled;
		public String denyMessage;
	}

	public static class cHookQsExt {
		public boolean enabled;
		public String denyMessage;
		public boolean fixOnJoin;
		public boolean fixOnStart;
		public boolean fixOnShopLoad;
	}

	public static class cReactions {
		public List<String> commands = new ArrayList<>();
	}

	public static class cPenalty {
		public boolean enabled;
		public double amount;
		public String message;
	}

	public static class cMessages {
		public String noItemHeld;
		public String playerOnlyCommand;
		public String noRestrictionItem;
		public String quickMinimalPrice;
		public String noPermissions;
		public final cMessagesMinimalDetails detailedMinimalPrice = new cMessagesMinimalDetails();

	}

	public static class cMessagesMinimalDetails {
		public String header;
		public String base;
		public String enchantmentHeader;
		public String enchantmentList;
		public String trimHeader;
		public String trimName;
		public String trimMaterial;
		public String spawnerType;
	}

	public static class cGui {
		public final cCategoryGui category = new cCategoryGui();
		public final cDetailsGui details = new cDetailsGui();
	}

	public static class cCategoryGui {
		public String title;
		public int rows;
		public List<cGuiItem> items = new ArrayList<>();
	}

	public static class cDetailsGui {
		public String title;
		public int rows;
		public final cGuiTemplateItem template = new cGuiTemplateItem();
		public final cGuiSpecialItems back = new cGuiSpecialItems();
		public final cGuiSpecialItems close = new cGuiSpecialItems();
	}

	public static class cGuiItem {
		public Material material;
		public Actions type;
		public String name;
		public int slot;
		public List<String> lore = new ArrayList<>();
	}

	public static class cGuiTemplateItem {
		public String name;
		public List<String> lore = new ArrayList<>();
	}

	public static class cGuiSpecialItems {
		public boolean use;
		public Material material;
		public String name;
		public int slot;
		public List<String> lore = new ArrayList<>();
	}

	public static class cCommandItem {
		public String command;
		public List<String> aliases = new ArrayList<>();
		public String permission;
	}

	public static class cCheckCommand extends cCommandItem {
		public cCommandItem details = new cCommandItem();
	}
	public static class cCommands {
		public cCommandItem main = new cCommandItem();
		public cCheckCommand check = new cCheckCommand();
	}
}