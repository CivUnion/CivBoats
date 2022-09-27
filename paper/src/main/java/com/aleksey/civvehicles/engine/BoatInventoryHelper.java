package com.aleksey.civvehicles.engine;

import com.aleksey.civvehicles.config.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.*;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class BoatInventoryHelper {
    private final static int RowLength = 9;
    private final static int MaxSlots = 54;
    private final static int PrevButtonSlotIndex = 54 - 9;
    private final static int NextButtonSlotIndex = 54 - 1;

    public enum OpenResult { Success, NotInBoat, NoInventory, Used }

    private class InventoryInfo {
        public UUID boatId;
        public Inventory inventory;
        public ItemStack[][] pages;
        public int pageIndex;
        public int inventorySize;
    }

    private final NamespacedKey _inventoryId;
    private final NamespacedKey _inventoryPageId;
    private final ConfigManager _config;
    private final HashMap<UUID, InventoryInfo> _boatToInventory = new HashMap<>();
    private final HashMap<Inventory, InventoryInfo> _inventories = new HashMap<>();

    public BoatInventoryHelper(JavaPlugin plugin, ConfigManager config) {
        _inventoryId = new NamespacedKey(plugin, "boat_inventory");
        _inventoryPageId = new NamespacedKey(plugin, "boat_inventory_page");
        _config = config;
    }

    public void closeInventory(Entity vehicle) {
        if (vehicle == null || vehicle.getType() != EntityType.BOAT)
            return;

        Boat boat = (Boat)vehicle;
        if (_config.getInventorySize(boat.getWoodType()) <= 0)
            return;

        InventoryInfo info = _boatToInventory.get(vehicle.getUniqueId());
        if (info != null)
            info.inventory.close();
    }

    public boolean clickInventory(Inventory inventory, int clickedSlotIndex) {
        InventoryInfo info = _inventories.get(inventory);
        if (info == null || info.pages.length == 1)
            return false;

        if (clickedSlotIndex == PrevButtonSlotIndex && info.pageIndex > 0) {
            saveInventoryToCache(info);
            info.pageIndex--;
            loadInventory(info);
            return true;
        }

        if (clickedSlotIndex == NextButtonSlotIndex && info.pageIndex < info.pages.length - 1) {
            saveInventoryToCache(info);
            info.pageIndex++;
            loadInventory(info);
            return true;
        }

        return clickedSlotIndex >= info.pages[info.pageIndex].length;
    }

    public OpenResult openInventory(Player player, Entity vehicle) {
        if (vehicle == null || vehicle.getType() != EntityType.BOAT)
            return OpenResult.NotInBoat;

        Boat boat = (Boat)vehicle;
        if (_config.getInventorySize(boat.getWoodType()) <= 0)
            return OpenResult.NoInventory;

        InventoryInfo info = _boatToInventory.get(vehicle.getUniqueId());

        if (info == null) {
            byte[] inventoryData = boat.getPersistentDataContainer().get(_inventoryId, PersistentDataType.BYTE_ARRAY);
            Integer pageIndex = boat.getPersistentDataContainer().get(_inventoryPageId, PersistentDataType.INTEGER);

            info = createInventoryInfo(boat, inventoryData, pageIndex);

            _boatToInventory.put(boat.getUniqueId(), info);
            _inventories.put(info.inventory, info);
        } else if (!info.inventory.getViewers().isEmpty()) {
            return info.inventory.getViewers().get(0) != player ? OpenResult.Used : OpenResult.Success;
        }

        loadInventory(info);

        player.openInventory(info.inventory);

        return OpenResult.Success;
    }

    private InventoryInfo createInventoryInfo(Boat boat, byte[] inventoryData, Integer pageIndex) {
        int inventorySize = _config.getInventorySize(boat.getWoodType());
        InventoryInfo info = new InventoryInfo();
        info.boatId = boat.getUniqueId();
        info.inventory = createInventory(inventorySize);
        info.pageIndex = pageIndex != null ? pageIndex : 0;
        info.pages = InventorySerializer.deserialize(inventoryData, inventorySize);
        info.inventorySize = inventorySize;

        return info;
    }

    private static Inventory createInventory(int inventorySize) {
        int size = Math.min(inventorySize, MaxSlots);
        return Bukkit.getServer().createInventory(null, size, Component.text("Boat's Inventory"));
    }

    private void loadInventory(InventoryInfo info) {
        if (info.pageIndex < 0 || info.pageIndex >= info.pages.length)
            info.pageIndex = 0;

        ItemStack[] items = info.pages[info.pageIndex];

        info.inventory.clear();

        if (items != null) {
            for (int i = 0; i < items.length; i++) {
                ItemStack item = items[i];
                if (item != null)
                    info.inventory.setItem(i, item);
            }
        }

        fillNonUsedSpace(info);
        addPaging(info);
    }

    private void fillNonUsedSpace(InventoryInfo info) {
        if (info.pages.length == 1 || info.pageIndex != info.pages.length - 1)
            return;

        int startSlot = info.pages[info.pageIndex].length;
        int endSlot = MaxSlots - RowLength;

        for (int i = startSlot; i < endSlot; i++)
            info.inventory.setItem(i, createItem(Material.WHITE_STAINED_GLASS_PANE, 1, ""));
    }

    private void addPaging(InventoryInfo info) {
        if (info.pages.length == 1)
            return;

        int pageIndex = info.pageIndex;
        int startSlot = RowLength * 5;

        ItemStack prevPage = pageIndex == 0
                ? createItem(Material.WHITE_STAINED_GLASS_PANE, 1, "")
                : createItem(Material.ARROW, 1, "Show previous page");

        ItemStack nextPage = pageIndex < info.pages.length - 1
                ? createItem(Material.ARROW, 1, "Show next page")
                : createItem(Material.WHITE_STAINED_GLASS_PANE, 1, "");

        info.inventory.setItem(startSlot++, prevPage);
        info.inventory.setItem(startSlot++, createItem(Material.WHITE_STAINED_GLASS_PANE, 1, ""));
        info.inventory.setItem(startSlot++, createItem(Material.WHITE_STAINED_GLASS_PANE, 1, ""));
        info.inventory.setItem(startSlot++, createItem(Material.WHITE_STAINED_GLASS_PANE, 1, ""));
        info.inventory.setItem(startSlot++, createItem(Material.OAK_SIGN, pageIndex + 1, "Page " + Integer.toString(pageIndex + 1)));
        info.inventory.setItem(startSlot++, createItem(Material.WHITE_STAINED_GLASS_PANE, 1, ""));
        info.inventory.setItem(startSlot++, createItem(Material.WHITE_STAINED_GLASS_PANE, 1, ""));
        info.inventory.setItem(startSlot++, createItem(Material.WHITE_STAINED_GLASS_PANE, 1, ""));
        info.inventory.setItem(startSlot, nextPage);
    }

    private static ItemStack createItem(Material material, int count, String name) {
        ItemStack item = new ItemStack(material, count);
        TextComponent displayName = Component.text(name).color(TextColor.color(252, 168, 0));
        ItemMeta meta = item.getItemMeta();

        meta.displayName(displayName);
        item.setItemMeta(meta);

        return item;
    }

    public void saveInventory(Inventory inventory, HumanEntity player) {
        InventoryInfo info = _inventories.get(inventory);
        if (info == null)
            return;

        Entity boat = Bukkit.getEntity(info.boatId);
        if (boat == null)
            return;

        saveInventoryToCache(info);

        if (isEmpty(info.pages)) {
            boat.getPersistentDataContainer().remove(_inventoryId);
            boat.getPersistentDataContainer().remove(_inventoryPageId);
        } else {
            byte[] serialized = InventorySerializer.serialize(info.pages);
            boat.getPersistentDataContainer().set(_inventoryId, PersistentDataType.BYTE_ARRAY, serialized);
            boat.getPersistentDataContainer().set(_inventoryPageId, PersistentDataType.INTEGER, info.pageIndex);
        }

        List<HumanEntity> viewers = inventory.getViewers();
        if (viewers.isEmpty() || viewers.size() == 1 && viewers.get(0) == player) {
            _boatToInventory.remove(info.boatId);
            _inventories.remove(inventory);
        }
    }

    private boolean isEmpty(ItemStack[][] pages) {
        for (ItemStack[] items : pages) {
            for (ItemStack item : items) {
                if (item != null)
                    return false;
            }
        }

        return true;
    }

    private void saveInventoryToCache(InventoryInfo info) {
        ItemStack[] items = info.pages[info.pageIndex];
        if (items == null)
            info.pages[info.pageIndex] = items = new ItemStack[MaxSlots];

        int pageSize = InventorySerializer.getPageSize(info.inventorySize, info.pageIndex);

        for (int i = 0; i < pageSize; i++)
            items[i] = info.inventory.getItem(i);
    }

    public void removeInventory(Entity vehicle) {
        if (vehicle.getType() != EntityType.BOAT)
            return;

        ItemStack[][] pages;
        InventoryInfo info = _boatToInventory.get(vehicle.getUniqueId());

        if (info != null) {
            saveInventoryToCache(info);
            pages = info.pages;

            info.inventory.close();
        } else {
            byte[] inventoryData = vehicle.getPersistentDataContainer().get(_inventoryId, PersistentDataType.BYTE_ARRAY);

            if (inventoryData == null)
                return;

            Boat boat = (Boat)vehicle;
            int inventorySize = _config.getInventorySize(boat.getWoodType());

            pages = InventorySerializer.deserialize(inventoryData, inventorySize);
        }

        Location location = vehicle.getLocation().add(0, 0.5, 0);

        for (ItemStack[] items : pages) {
            for (ItemStack item : items) {
                if (item != null)
                    location.getWorld().dropItemNaturally(location, item);
            }
        }
    }
}
