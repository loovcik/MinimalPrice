package me.loovcik.minimalPrice;

import me.loovcik.minimalPrice.commands.CheckPriceCommand;
import me.loovcik.minimalPrice.commands.ShowPricesCommand;
import me.loovcik.minimalPrice.listeners.InventoryClickEventListener;
import me.loovcik.minimalPrice.listeners.PlayerAuctionSellListener;
import me.loovcik.minimalPrice.listeners.QuickShop.PlayerJoin;
import me.loovcik.minimalPrice.managers.CommandManager;
import me.loovcik.minimalPrice.managers.ConfigurationManager;
import me.loovcik.minimalPrice.managers.DependenciesManager;
import me.loovcik.minimalPrice.prices.Storage;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import me.loovcik.core.ChatHelper;
import me.loovcik.core.commands.SimpleCommand;

@SuppressWarnings({"CallToPrintStackTrace", "UnstableApiUsage"})
public final class MinimalPrice extends JavaPlugin {

    private static MinimalPrice instance;
    public ConfigurationManager configuration;
    public DependenciesManager dependencies;
    public Storage storage;
    public CommandManager commandManager;

    /** Pobiera instancję pluginu */
    public static MinimalPrice getInstance() { return instance; }

    @Override
    public void onLoad(){
        instance = this;
    }

    @Override
    public void onEnable()
    {
        ChatHelper.setPlugin(this);
        ChatHelper.console("Author: <gold>Loovcik</gold>");
        ChatHelper.console("Version: <gold>" + getPluginMeta().getVersion());

        saveDefaultConfig();

        dependencies = new DependenciesManager(this);
        storage = new Storage();
        configuration = new ConfigurationManager(this);
        configuration.load();
        ChatHelper.setPrefix(configuration.general.prefix);
        commandManager = new CommandManager(this);

        if (configuration.hooks.playerAuctions.enabled){
            try
            {
                getServer().getPluginManager().registerEvents(new PlayerAuctionSellListener(this), this);
                ChatHelper.console("PlayerAuction listeners: <green>registered</green>");
            }
            catch (Exception e){
                ChatHelper.console("PlayerAuction listeners: <red>not registered</red>");
                e.printStackTrace();
            }
        }
        if (configuration.hooks.quickShop.enabled){
            Bukkit.getPluginManager().registerEvents(new PlayerJoin(this), this);
            dependencies.quickShop.getApi().registerListeners();
            if (configuration.hooks.quickShop.fixOnStart)
                dependencies.quickShop.fixPrices();
        }

        getServer().getPluginManager().registerEvents(new InventoryClickEventListener(), this);
        registerCommands();
    }

    @Override
    public void onDisable()
    {
        unregisterCommands();
        dependencies.quickShop.unregister();
    }

    /** Rejestruje komendy pluginu */
    private void registerCommands(){
        new CheckPriceCommand(this).register();
        new ShowPricesCommand(this).register();
        SimpleCommand.scheduleCommandSync(this);
    }

    /** Wyrejestrowuje komendy pluginu */
    private void unregisterCommands(){
        SimpleCommand.getCommands().forEach(SimpleCommand::unregister);
        SimpleCommand.scheduleCommandSync(this);

    }
}