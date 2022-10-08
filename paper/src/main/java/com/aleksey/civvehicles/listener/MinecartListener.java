package com.aleksey.civvehicles.listener;

import com.aleksey.civvehicles.config.ConfigManager;
import org.bukkit.entity.Minecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.vehicle.VehicleMoveEvent;

public class MinecartListener implements Listener {
    private final ConfigManager _config;

    public MinecartListener(ConfigManager config) {
        _config = config;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVehicleMove(VehicleMoveEvent event) {
        if (event.getVehicle() instanceof Minecart minecart) {
            byte lightFromSky = minecart.getLocation().getBlock().getLightFromSky();
            double maxSpeed = _config.getMinecartMaxSpeed(lightFromSky);

            minecart.setMaxSpeed(maxSpeed);
        }
    }
}
