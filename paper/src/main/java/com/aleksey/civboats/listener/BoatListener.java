package com.aleksey.civboats.listener;

import com.aleksey.civboats.config.ConfigManager;
import com.aleksey.civboats.engine.BoatInventoryHelper;
import org.bukkit.ChatColor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.vehicle.VehicleDestroyEvent;
import org.bukkit.event.vehicle.VehicleEnterEvent;
import org.bukkit.inventory.EquipmentSlot;

public class BoatListener implements Listener {
    private final ConfigManager _config;
    private final BoatInventoryHelper _inventoryHelper;

    public BoatListener(BoatInventoryHelper inventoryHelper, ConfigManager config) {
        _config = config;
        _inventoryHelper = inventoryHelper;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVehicleEnter(VehicleEnterEvent event) {
        _inventoryHelper.closeInventory(event.getVehicle());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onVehicleDestroy(VehicleDestroyEvent event) {
        _inventoryHelper.removeInventory(event.getVehicle());
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onPlayerInteractEntity(PlayerInteractEntityEvent event) {
        Player player = event.getPlayer();
        Entity vehicle = event.getRightClicked();

        if (_config.isAllowRightClickInteract()
                && event.getHand().equals(EquipmentSlot.HAND)
                && player.isSneaking()
                && vehicle.getPassengers().isEmpty()
        ) {
            BoatInventoryHelper.OpenResult result = _inventoryHelper.openInventory(player, vehicle);

            switch(result) {
                case NoInventory -> player.sendMessage(ChatColor.YELLOW + "This boat does not have inventory.");
                case Used -> player.sendMessage(ChatColor.YELLOW + "This inventory is already in use by someone else.");
                case Success -> event.setCancelled(true);
            }
        }
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
