package com.aleksey.civvehicles.config;

import org.bukkit.TreeSpecies;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.logging.Logger;

public class ConfigManager {
    private static final double MAX_SPEED_RATIO = 0.4;
    private static final double MAX_LIGHT = 15;

    private final Logger _logger;

    private BoatInfo _defaultBoat;
    private HashMap<TreeSpecies, BoatInfo> _boats;
    private MinecartInfo _defaultMinecart;

    private BoatInfo getBoat(TreeSpecies wood) {
        BoatInfo info = _boats.get(wood);
        return info != null
                ? info
                : _defaultBoat;
    }

    public boolean isAllowBoatRightClickInteract() {
        return _defaultBoat.allowRightClickInteract;
    }

    public int getBoatInventorySize(TreeSpecies wood) {
        return getBoat(wood).inventorySize;
    }

    public double getMinecartMaxSpeed(byte lightFromSky) {
        if (lightFromSky == 0 || _defaultMinecart.maxSpeed == _defaultMinecart.maxSpeedFromSky) {
            return _defaultMinecart.maxSpeed * MAX_SPEED_RATIO;
        }

        if (!_defaultMinecart.maxSpeedFromSkyVariable || _defaultMinecart.maxSpeedFromSky < _defaultMinecart.maxSpeed) {
            return _defaultMinecart.maxSpeedFromSky;
        }

        double increase = (_defaultMinecart.maxSpeedFromSky - _defaultMinecart.maxSpeed) * lightFromSky / MAX_LIGHT;

        return _defaultMinecart.maxSpeed + increase;
    }

    public ConfigManager(Logger logger) {
        _logger = logger;
    }

    public void load(FileConfiguration file) {
        readBoatSettings(file);
        readMinecartSettings(file);
    }

    private void readBoatSettings(FileConfiguration file) {
        _defaultBoat = new BoatInfo();
        _defaultBoat.allowRightClickInteract = file.getBoolean("Boats.Default.AllowRightClickInteract", false);
        _defaultBoat.inventorySize = adjustBoatInventorySize(file.getInt("Boats.Default.BoatInventory", 0));

        _boats = new HashMap<>();

        readBoatTypes(file.getConfigurationSection("Boats"));
    }

    private void readBoatTypes(ConfigurationSection config) {
        if (config == null)
            return;

        for (String key : config.getKeys(false)) {
            if (!config.isConfigurationSection(key))
                continue;

            TreeSpecies wood;

            switch (key.toUpperCase()) {
                case "OAK" -> wood = TreeSpecies.GENERIC;
                case "REDWOOD" -> wood = TreeSpecies.REDWOOD;
                case "BIRCH" -> wood = TreeSpecies.BIRCH;
                case "JUNGLE" -> wood = TreeSpecies.JUNGLE;
                case "ACACIA" -> wood = TreeSpecies.ACACIA;
                case "DARK_OAK" -> wood = TreeSpecies.DARK_OAK;
                case "DEFAULT" -> {
                    continue;
                }
                default -> {
                    _logger.warning("Unsupported boat type: " + key);
                    continue;
                }
            }

            ConfigurationSection boatConfig = config.getConfigurationSection(key);

            BoatInfo info = new BoatInfo();
            info.inventorySize = adjustBoatInventorySize(boatConfig.getInt("Inventory", 0));

            _boats.put(wood, info);
        }
    }

    private static int adjustBoatInventorySize(int size) {
        return size > 255
                ? 255
                : (size / 9) * 9;
    }

    private void readMinecartSettings(FileConfiguration file) {
        _defaultMinecart = new MinecartInfo();
        _defaultMinecart.maxSpeed = file.getDouble("Minecarts.Default.MaxSpeed", 1.0);
        _defaultMinecart.maxSpeedFromSky = file.getDouble("Minecarts.Default.MaxSpeedFromSky", 1.0);
        _defaultMinecart.maxSpeedFromSkyVariable = file.getBoolean("Minecarts.Default.MaxSpeedFromSkyVariable", false);
    }
}
