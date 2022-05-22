package com.aleksey.civboats.listener;

import com.aleksey.civboats.engine.BoatInventoryHelper;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;

public class BoatListener implements Listener {
    private final BoatInventoryHelper _inventoryHelper;

    public BoatListener(BoatInventoryHelper inventoryHelper) {
        _inventoryHelper = inventoryHelper;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        _inventoryHelper.removeInventory(event.getVehicle());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPlayerLeave(PlayerQuitEvent event) {
        _inventoryHelper.saveInventory(event.getPlayer().getOpenInventory().getTopInventory(), event.getPlayer());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onInventoryClosed(InventoryCloseEvent event) {
        _inventoryHelper.saveInventory(event.getInventory(), event.getPlayer());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player))
            return;

        if (_inventoryHelper.clickInventory(event.getClickedInventory(), event.getRawSlot()))
            event.setCancelled(true);
    }
}
