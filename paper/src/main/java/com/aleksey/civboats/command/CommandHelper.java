package com.aleksey.civboats.command;

import com.aleksey.civboats.engine.BoatInventoryHelper;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandHelper {
    private BoatInventoryHelper _inventoryHelper;

    public CommandHelper(BoatInventoryHelper inventoryHelper) {
        _inventoryHelper = inventoryHelper;
    }
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player))
            return false;

        BoatInventoryHelper.OpenResult result = _inventoryHelper.openInventory(player, player.getVehicle());

        switch(result) {
            case NotInBoat -> player.sendMessage(ChatColor.YELLOW + "You are not in boat!");
            case NoInventory -> player.sendMessage(ChatColor.YELLOW + "This boat does not have inventory.");
            case Used -> player.sendMessage(ChatColor.YELLOW + "This inventory is already in use by someone else.");
        }

        return true;
    }
}
