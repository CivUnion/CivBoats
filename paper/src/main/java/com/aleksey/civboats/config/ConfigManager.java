package com.aleksey.civboats.config;

import org.bukkit.TreeSpecies;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.HashMap;
import java.util.logging.Logger;

public class ConfigManager {
    private class BoatInfo {
        public int inventorySize;
    }

    private Logger _logger;
    private BoatInfo _defaultBoat;
    private HashMap<TreeSpecies, BoatInfo> _boats;

    private BoatInfo getBoat(TreeSpecies wood) {
        BoatInfo info = _boats.get(wood);
        return info != null
                ? info
                : _defaultBoat;
    }

    public int getInventorySize(TreeSpecies wood) {
        return getBoat(wood).inventorySize;
    }

    public ConfigManager(Logger logger) {
        _logger = logger;
    }

    public void load(FileConfiguration file) {
        _defaultBoat = new BoatInfo();
        _defaultBoat.inventorySize = adjustInventorySize(file.getInt("DefaultBoatInventory", 0));

        readBoats(file.getConfigurationSection("Boats"));
    }

    private void readBoats(ConfigurationSection config) {
        _boats = new HashMap<>();

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
                default -> {
                    _logger.warning("Unsupported boat type: " + key);
                    continue;
                }
            }

            ConfigurationSection boatConfig = config.getConfigurationSection(key);

            BoatInfo info = new BoatInfo();
            info.inventorySize = adjustInventorySize(boatConfig.getInt("Inventory", 0));

            _boats.put(wood, info);
        }
    }

    private static int adjustInventorySize(int size) {
        return size > 255
                ? 255
                : (size / 9) * 9;
    }
}
