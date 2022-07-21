/*
 * This file is part of ComputerCraft - http://www.computercraft.info
 * Copyright Daniel Ratcliffe, 2011-2022. Do not distribute without permission.
 * Send enquiries to dratcliffe@gmail.com
 */
package dan200.computercraft.shared.turtle.core;

import dan200.computercraft.api.turtle.*;
import dan200.computercraft.shared.TurtleUpgrades;
import dan200.computercraft.shared.turtle.blocks.TileTurtle;
import dan200.computercraft.shared.util.InventoryUtil;
import dan200.computercraft.shared.util.ItemStorage;
import dan200.computercraft.shared.util.WorldUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class TurtleEquipCommand implements ITurtleCommand
{
    private final TurtleSide side;

    public TurtleEquipCommand( TurtleSide side )
    {
        this.side = side;
    }

    @Nonnull
    @Override
    public TurtleCommandResult execute( @Nonnull ITurtleAccess turtle )
    {
        // Determine the upgrade to equipLeft
        ITurtleUpgrade newUpgrade;
        ItemStack newUpgradeStack;
        Container inventory = turtle.getInventory();
        ItemStack selectedStack = inventory.getItem( turtle.getSelectedSlot() );
        if( !selectedStack.isEmpty() )
        {
            newUpgradeStack = selectedStack.copy();
            newUpgrade = TurtleUpgrades.get( newUpgradeStack );
            if( newUpgrade == null ) return TurtleCommandResult.failure( "Not a valid upgrade" );
        }
        else
        {
            newUpgradeStack = ItemStack.EMPTY;
            newUpgrade = null;
        }

        int oldUpgradeSlot = TileTurtle.NORMAL_INVENTORY_SIZE + side.ordinal();
        ItemStack oldUpgradeStack = inventory.getItem(oldUpgradeSlot).copy();
        ITurtleUpgrade oldUpgrade = turtle.getUpgrade( side );

        // Do the swapping:
        if( oldUpgradeStack != null )
        {
            // Remove installed upgrade
            InventoryUtil.takeItems(1, ItemStorage.wrap(inventory), oldUpgradeSlot, 1, oldUpgradeSlot);
        }
        if( newUpgradeStack != null )
        {
            // Consume new upgrades item
            InventoryUtil.takeItems( 1, ItemStorage.wrap( inventory ), turtle.getSelectedSlot(), 1, turtle.getSelectedSlot() );
        }
        if ( oldUpgradeStack != null )
        {
            // Store old upgrades item
            ItemStack remainder = InventoryUtil.storeItems( oldUpgradeStack, ItemStorage.wrap( inventory ), turtle.getSelectedSlot() );
            if( !remainder.isEmpty() )
            {
                // If there's no room for the items, drop them
                BlockPos position = turtle.getPosition();
                WorldUtil.dropItemStack( remainder, turtle.getLevel(), position, turtle.getDirection() );
            }
        }
        if ( newUpgradeStack != null )
        {
            InventoryUtil.storeItems(newUpgradeStack, ItemStorage.wrap(inventory), oldUpgradeSlot);
        }

        turtle.setUpgrade( side, newUpgrade );

        // Animate
        if( newUpgrade != null || oldUpgrade != null )
        {
            turtle.playAnimation( TurtleAnimation.WAIT );
        }

        return TurtleCommandResult.success();
    }
}
