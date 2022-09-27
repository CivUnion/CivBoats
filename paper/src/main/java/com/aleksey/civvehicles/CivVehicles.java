package com.aleksey.civvehicles;

import com.aleksey.civvehicles.command.CommandHelper;
import com.aleksey.civvehicles.config.ConfigManager;
import com.aleksey.civvehicles.engine.BoatInventoryHelper;
import com.aleksey.civvehicles.listener.BoatListener;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

public class CivVehicles extends JavaPlugin {
    private static CivVehicles _instance;
    public static CivVehicles getInstance() {
        return _instance;
    }

    private ConfigManager _config;
    private BoatInventoryHelper _inventoryHelper;
    private CommandHelper _commandHelper;

    @Override
    public void onEnable() {
        _instance = this;

        saveDefaultConfig();

        _config = new ConfigManager(getLogger());
        _config.load(getConfig());

        _inventoryHelper = new BoatInventoryHelper(this, _config);
        _commandHelper = new CommandHelper(_inventoryHelper);

        getServer().getPluginManager().registerEvents(new BoatListener(_inventoryHelper, _config), this);
    }

    @Override
    public void onDisable() {
        getServer().getScheduler().cancelTasks(this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        return _commandHelper.onCommand(sender, command, label, args);
    }
}
