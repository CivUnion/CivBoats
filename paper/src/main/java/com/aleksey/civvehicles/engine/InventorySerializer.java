package com.aleksey.civvehicles.engine;

import org.bukkit.inventory.ItemStack;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

public class InventorySerializer {
    private final static int RowLength = 9;
    private final static int MaxSlots = 54;

    public static int getPageSize(int inventorySize, int pageIndex) {
        int slotsPerPage = inventorySize <= MaxSlots ? inventorySize : MaxSlots - RowLength;
        int pageCount = inventorySize / slotsPerPage;

        return pageIndex < pageCount
                ? slotsPerPage
                : inventorySize % slotsPerPage;
    }

    public static byte[] serialize(ItemStack[][] pages) {
        int size = 0;
        for (ItemStack[] items : pages)
            size += items.length;

        try (ByteArrayOutputStream stream = new ByteArrayOutputStream())
        {
            stream.write(size);

            for (ItemStack[] items : pages) {
                for (ItemStack item : items) {
                    if (item != null) {
                        byte[] serialized = item.serializeAsBytes();

                        stream.write(0xff & (serialized.length >> 8));
                        stream.write(0xff & serialized.length);
                        stream.write(serialized);
                    } else {
                        stream.write(0);
                        stream.write(0);
                    }
                }
            }

            return stream.toByteArray();
        } catch (Exception ex) {
            ex.printStackTrace();
        }

        return null;
    }

    public static ItemStack[][] deserialize(byte[] input, int inventorySize) {
        int slotsPerPage = inventorySize <= MaxSlots ? inventorySize : MaxSlots - RowLength;
        int pageCount = inventorySize / slotsPerPage;
        if (inventorySize % slotsPerPage != 0)
            pageCount++;

        ItemStack[][] pages = new ItemStack[pageCount][];
        for (int i = 0; i < pages.length; i++) {
            int pageSize = getPageSize(inventorySize, i);
            pages[i] = new ItemStack[pageSize];
        }

        if (input == null)
            return pages;

        if (pageCount == 1)
            readOnePage(input, inventorySize, pages[0]);
        else
            readMultiplePages(input, pages);

        return pages;
    }

    private static void readMultiplePages(byte[] input, ItemStack[][] pages) {
        int pageIndex = 0;
        int slotIndex = 0;
        int slotsPerPage = MaxSlots - RowLength;
        ItemStack[] items = pages[0];

        try (ByteArrayInputStream stream = new ByteArrayInputStream(input)) {
            int size = stream.read();

            for (int i = 0; i < size; i++) {
                items[slotIndex++] = readItem(stream);

                if (slotIndex == slotsPerPage) {
                    pageIndex++;
                    if (pageIndex == pages.length)
                        break;

                    items = pages[pageIndex];
                    slotIndex = 0;
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void readOnePage(byte[] input, int inventorySize, ItemStack[] items) {
        try (ByteArrayInputStream stream = new ByteArrayInputStream(input)) {
            int size = Math.min(stream.read(), inventorySize);

            for (int i = 0; i < size; i++)
                items[i] = readItem(stream);
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static ItemStack readItem(ByteArrayInputStream stream) {
        int itemSize = (stream.read() << 8) | stream.read();
        if (itemSize == 0)
            return null;

        byte[] itemBytes = new byte[itemSize];

        stream.read(itemBytes, 0, itemSize);

        return ItemStack.deserializeBytes(itemBytes);
    }
}
