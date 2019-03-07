package se.mickelus.tetra.module;


import com.google.common.collect.Streams;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ResourceLocation;
import org.apache.commons.lang3.ArrayUtils;
import se.mickelus.tetra.NBTHelper;
import se.mickelus.tetra.TetraMod;
import se.mickelus.tetra.capabilities.Capability;
import se.mickelus.tetra.items.ItemModular;
import se.mickelus.tetra.module.data.ImprovementData;
import se.mickelus.tetra.module.data.ModuleData;
import se.mickelus.tetra.util.CastOptional;

import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

public abstract class ItemModuleMajor<T extends ModuleData> extends ItemModule<T> {

    protected ImprovementData[] improvements = new ImprovementData[0];

    public ItemModuleMajor(String slotKey, String moduleKey) {
        super(slotKey, moduleKey);
    }

    public int getImprovementLevel(String improvementKey, ItemStack itemStack) {
        NBTTagCompound tag = NBTHelper.getTag(itemStack);
        if (tag.hasKey(slotKey + ":" + improvementKey)) {
            return NBTHelper.getTag(itemStack).getInteger(slotKey + ":" + improvementKey);
        }
        return -1;
    }

    public ImprovementData[] getImprovements(ItemStack itemStack) {
        NBTTagCompound tag = NBTHelper.getTag(itemStack);
        return Arrays.stream(improvements)
            .filter(improvement -> tag.hasKey(slotKey + ":" + improvement.key))
            .filter(improvement -> improvement.level == tag.getInteger(slotKey + ":" + improvement.key))
            .toArray(ImprovementData[]::new);
    }

    public boolean acceptsImprovement(String improvementKey) {
        return Arrays.stream(improvements)
                .map(improvement -> improvement.key)
                .anyMatch(improvementKey::equals);
    }

    public boolean acceptsImprovementLevel(String improvementKey, int level) {
        return Arrays.stream(improvements)
                .filter(improvement -> improvementKey.equals(improvement.key))
                .anyMatch(improvement -> level == improvement.level);
    }

    public void addImprovement(ItemStack itemStack, String improvementKey, int level) {
        removeCollidingImprovements(itemStack, improvementKey, level);
        NBTHelper.getTag(itemStack).setInteger(slotKey + ":" + improvementKey, level);
    }

    public static void addImprovement(ItemStack itemStack, String slot, String improvement, int level) {
        ItemModular item = (ItemModular) itemStack.getItem();
        CastOptional.cast(item.getModuleFromSlot(itemStack, slot), ItemModuleMajor.class)
                .ifPresent(module -> module.addImprovement(itemStack, improvement, level));
    }

    public void removeCollidingImprovements(ItemStack itemStack, String improvementKey, int level) {
        Arrays.stream(improvements)
                .filter(improvement -> improvementKey.equals(improvement.key))
                .filter(improvement -> level == improvement.level)
                .filter(improvement -> improvement.group != null)
                .map(improvement -> improvement.group)
                .findFirst()
                .ifPresent(group -> Arrays.stream(getImprovements(itemStack))
                        .filter(improvement -> group.equals(improvement.group))
                        .forEach(improvement -> removeImprovement(itemStack, slotKey, improvement.key)));
    }

    public static void removeImprovement(ItemStack itemStack, String slot, String improvement) {

        NBTHelper.getTag(itemStack).removeTag(slot + ":" + improvement);
    }

    @Override
    public ItemStack[] removeModule(ItemStack targetStack) {
        ItemStack[] salvage = super.removeModule(targetStack);

        NBTTagCompound tag = NBTHelper.getTag(targetStack);
        Arrays.stream(improvements)
            .map(improvement -> slotKey + ":" + improvement.key)
            .forEach(tag::removeTag);

        return salvage;
    }

    @Override
    public double getDamageModifier(ItemStack itemStack) {
        return Arrays.stream(getImprovements(itemStack))
                .mapToDouble(improvement -> improvement.damage)
                .sum() + super.getDamageModifier(itemStack);
    }

    @Override
    public double getDamageMultiplierModifier(ItemStack itemStack) {
        return Arrays.stream(getImprovements(itemStack))
                .map(improvement -> improvement.damageMultiplier)
                .reduce((float) super.getDamageMultiplierModifier(itemStack), (a, b) -> a * b);
    }

    @Override
    public double getSpeedModifier(ItemStack itemStack) {
        return Arrays.stream(getImprovements(itemStack))
                .mapToDouble(improvement -> improvement.attackSpeed)
                .sum() + super.getSpeedModifier(itemStack);
    }

    @Override
    public double getSpeedMultiplierModifier(ItemStack itemStack) {
        return Arrays.stream(getImprovements(itemStack))
                .map(improvement -> improvement.attackSpeedMultiplier)
                .reduce((float) super.getSpeedMultiplierModifier(itemStack), (a, b) -> a * b);
    }

    @Override
    public int getEffectLevel(ItemStack itemStack, ItemEffect effect) {
        return Arrays.stream(getImprovements(itemStack))
                .map(improvement -> improvement.effects)
                .mapToInt(effects -> effects.getLevel(effect))
                .sum() + super.getEffectLevel(itemStack, effect);
    }

    @Override
    public float getEffectEfficiency(ItemStack itemStack, ItemEffect effect) {
        return (float) Arrays.stream(getImprovements(itemStack))
                .map(improvement -> improvement.effects)
                .mapToDouble(effects -> effects.getEfficiency(effect))
                .sum() + super.getEffectLevel(itemStack, effect);
    }

    @Override
    public Collection<ItemEffect> getEffects(ItemStack itemStack) {
        return Streams.concat(
                super.getEffects(itemStack).stream(),
                Arrays.stream(getImprovements(itemStack))
                        .map(improvement -> improvement.effects)
                        .flatMap(effects -> effects.getValues().stream()))
                .distinct()
                .collect(Collectors.toSet());
    }

    @Override
    public int getCapabilityLevel(ItemStack itemStack, Capability capability) {
        return Arrays.stream(getImprovements(itemStack))
                .map(improvementData -> improvementData.capabilities)
                .mapToInt(capabilityData -> capabilityData.getLevel(capability))
                .sum() + super.getCapabilityLevel(itemStack, capability);
    }

    @Override
    public float getCapabilityEfficiency(ItemStack itemStack, Capability capability) {
        return (float) Arrays.stream(getImprovements(itemStack))
                .map(improvementData -> improvementData.capabilities)
                .mapToDouble(capabilityData -> capabilityData.getEfficiency(capability))
                .sum() + super.getCapabilityEfficiency(itemStack, capability);
    }

    @Override
    public Collection<Capability> getCapabilities(ItemStack itemStack) {
        return Streams.concat(
                super.getCapabilities(itemStack).stream(),
                Arrays.stream(getImprovements(itemStack))
                        .map(improvement -> improvement.capabilities)
                        .flatMap(capabilities -> capabilities.getValues().stream()))
                .distinct()
                .collect(Collectors.toSet());
    }

    @Override
    public int getIntegrityGain(ItemStack itemStack) {
        int moduleGain = super.getIntegrityGain(itemStack);
        int improvementGain = getImprovementIntegrityGain(itemStack);
        return moduleGain + improvementGain;
    }

    @Override
    public int getIntegrityCost(ItemStack itemStack) {
        int moduleCost = super.getIntegrityCost(itemStack);
        int improvementCost = getImprovementIntegrityCost(itemStack);
        return moduleCost + improvementCost;
    }

    private int getImprovementIntegrityGain(ItemStack itemStack) {
        return Arrays.stream(getImprovements(itemStack))
                .mapToInt(improvement -> improvement.integrity)
                .filter(integrity -> integrity > 0)
                .sum();
    }

    private int getImprovementIntegrityCost(ItemStack itemStack) {
        return Arrays.stream(getImprovements(itemStack))
                .mapToInt(improvement -> improvement.integrity)
                .filter(integrity -> integrity < 0)
                .sum();
    }

    protected ResourceLocation[] getAllImprovementTextures() {
        return Arrays.stream(improvements)
                .filter(improvement -> improvement.textured)
                .map(improvement -> "items/" + improvement.key)
                .map(resourceString -> new ResourceLocation(TetraMod.MOD_ID, resourceString))
                .toArray(ResourceLocation[]::new);
    }

    @Override
    public ResourceLocation[] getAllTextures() {
        return ArrayUtils.addAll(super.getAllTextures(), getAllImprovementTextures());
    }

    protected ResourceLocation[] getImprovementTextures(ItemStack itemStack) {
        return Arrays.stream(getImprovements(itemStack))
                .filter(improvement -> improvement.textured)
                .map(improvement -> "items/" + improvement.key)
                .map(resourceString -> new ResourceLocation(TetraMod.MOD_ID, resourceString))
                .toArray(ResourceLocation[]::new);
    }

    @Override
    public ResourceLocation[] getTextures(ItemStack itemStack) {
        return ArrayUtils.addAll(super.getTextures(itemStack), getImprovementTextures(itemStack));
    }
}
