package se.mickelus.tetra.blocks.geode;

import com.google.common.collect.ImmutableSet;
import net.minecraft.item.Item;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.registries.ObjectHolder;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.items.TetraItemGroup;
import se.mickelus.tetra.items.TetraItem;
import se.mickelus.tetra.network.PacketHandler;

import java.util.Set;

public class ItemPristineEmerald extends TetraItem {
    private static final String unlocalizedName = "pristine_emerald";

    @ObjectHolder(TetraMod.MOD_ID + ":" + unlocalizedName)
    public static ItemPristineEmerald instance;

    public ItemPristineEmerald() {
        super(new Properties().group(TetraItemGroup.instance));
        setRegistryName(unlocalizedName);
    }

    @Override
    public Set<ResourceLocation> getTags() {
        return ImmutableSet.of(new ResourceLocation("gems/emerald"));
    }
}
