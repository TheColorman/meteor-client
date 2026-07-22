/*
 * This file is part of the Meteor Client distribution (https://github.com/MeteorDevelopment/meteor-client).
 * Copyright (c) Meteor Development.
 */

package meteordevelopment.meteorclient.utils.render;

import net.minecraft.block.Block;
import net.minecraft.component.ComponentMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.text.Text;

/**
 * Creates display-only {@link ItemStack}s that can be used for rendering
 * before item components are bound (i.e. before joining a world).
 */
public class DisplayItemUtils {
    private DisplayItemUtils() {}

    public static ItemStack toStack(Item item) {
        if (item == Items.AIR) return ItemStack.EMPTY;
        return new ItemStack(directHolder(item));
    }

    public static ItemStack toStack(Block block) {
        return toStack(block.asItem());
    }

    public static ItemStack toStack(Item item, int count) {
        if (item == Items.AIR) return ItemStack.EMPTY;
        return new ItemStack(directHolder(item), count);
    }

    @SuppressWarnings("deprecation")
    private static RegistryEntry<Item> directHolder(Item item) {
        // ComponentMap components = ComponentMap.builder()
        //     .addAll(DataComponentTypes.DEFAULT_ITEM_COMPONENTS)
        //     .set(DataComponentTypes.ITEM_MODEL, item.registryEntry.key().identifier())
        //     .set(DataComponentTypes.ITEM_NAME, Text.translatable(item.getTranslationKey()))
        //     .build();
        return RegistryEntry.of(item);
    }
}
