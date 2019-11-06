package se.mickelus.tetra;

import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.criterion.ItemPredicate;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScreenManager;
import net.minecraft.client.renderer.model.*;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.inventory.container.ContainerType;
import net.minecraft.item.Item;
import net.minecraft.potion.Effect;
import net.minecraft.tileentity.TileEntityType;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.gen.feature.Feature;
import net.minecraft.world.gen.feature.OreFeature;
import net.minecraft.world.gen.feature.OreFeatureConfig;
import net.minecraft.world.storage.loot.LootPool;
import net.minecraft.world.storage.loot.LootTable;
import net.minecraft.world.storage.loot.conditions.LootConditionManager;
import net.minecraft.world.storage.loot.functions.LootFunctionManager;
import net.minecraftforge.client.event.ModelBakeEvent;
import net.minecraftforge.client.event.TextureStitchEvent;
import net.minecraftforge.client.model.BakedItemModel;
import net.minecraftforge.client.model.ModelLoader;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.extensions.IForgeContainerType;
import net.minecraftforge.event.LootTableLoadEvent;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.server.FMLServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import se.mickelus.tetra.advancements.*;
import se.mickelus.tetra.blocks.ITetraBlock;
import se.mickelus.tetra.blocks.geode.*;
import se.mickelus.tetra.blocks.workbench.BlockWorkbench;
import se.mickelus.tetra.blocks.workbench.TileEntityWorkbench;
import se.mickelus.tetra.blocks.workbench.WorkbenchContainer;
import se.mickelus.tetra.blocks.workbench.gui.WorkbenchGui;
import se.mickelus.tetra.client.model.BakedItemModelWrapper;
import se.mickelus.tetra.client.model.BakedWrapper;
import se.mickelus.tetra.client.model.ModularOverrideList;
import se.mickelus.tetra.data.DataHandler;
import se.mickelus.tetra.items.ITetraItem;
import se.mickelus.tetra.items.ItemPredicateModular;
import se.mickelus.tetra.items.TetraItemGroup;
import se.mickelus.tetra.items.duplex_tool.ItemDuplexToolModular;
import se.mickelus.tetra.items.forged.*;
import se.mickelus.tetra.items.journal.ItemJournal;
import se.mickelus.tetra.items.sword.ItemSwordModular;
import se.mickelus.tetra.items.toolbelt.ItemToolbeltModular;
import se.mickelus.tetra.items.toolbelt.gui.ToolbeltGui;
import se.mickelus.tetra.loot.FortuneBonusCondition;
import se.mickelus.tetra.loot.FortuneBonusFunction;
import se.mickelus.tetra.loot.SetMetadataFunction;
import se.mickelus.tetra.module.ItemEffectHandler;
import se.mickelus.tetra.module.ItemUpgradeRegistry;
import se.mickelus.tetra.module.improvement.HonePacket;
import se.mickelus.tetra.module.improvement.SettlePacket;
import se.mickelus.tetra.network.GuiHandlerRegistry;
import se.mickelus.tetra.network.PacketHandler;
import se.mickelus.tetra.proxy.ClientProxy;
import se.mickelus.tetra.proxy.IProxy;
import se.mickelus.tetra.proxy.ServerProxy;

import java.io.File;
import java.net.URISyntaxException;
import java.util.Arrays;
import java.util.Optional;
import java.util.logging.Level;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
@Mod(TetraMod.MOD_ID)
public class TetraMod {
    private static final Logger LOGGER = LogManager.getLogger();

    public static final String MOD_ID = "tetra";

    public static IProxy proxy = DistExecutor.runForDist(() -> ClientProxy::new, () -> ServerProxy::new);

    public static TetraMod instance;

    private static Item[] items;
    private static Block[] blocks;

    public TetraMod() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::setup);

        ItemPredicate.register(new ResourceLocation("tetra:modular_item"), ItemPredicateModular::new);

        LootConditionManager.registerCondition(new FortuneBonusCondition.Serializer());
        LootFunctionManager.registerFunction(new FortuneBonusFunction.Serializer());
        LootFunctionManager.registerFunction(new SetMetadataFunction.Serializer());

//        try {
            new DataHandler();
//        } catch (URISyntaxException e) {
//            TetraLogger.log(Level.SEVERE, e.getMessage());
//        }


        new ItemUpgradeRegistry();

        new TetraItemGroup();

        new GuiHandlerRegistry();

        CriteriaTriggers.register(BlockLookTrigger.instance);
        CriteriaTriggers.register(BlockUseCriterion.trigger);
        CriteriaTriggers.register(BlockInteractionCriterion.trigger);
        CriteriaTriggers.register(ModuleCraftCriterion.trigger);
        CriteriaTriggers.register(ImprovementCraftCriterion.trigger);

        MinecraftForge.EVENT_BUS.register(new ItemEffectHandler());
        // MinecraftForge.EVENT_BUS.register(this);
        MinecraftForge.EVENT_BUS.register(TetraMod.proxy);
        MinecraftForge.EVENT_BUS.register(BlockLookTrigger.instance);

        blocks = new Block[] {
                new BlockWorkbench(),
                new BlockGeode(),
        };

//        if (ConfigHandler.generateFeatures) {
//            blocks = ArrayUtils.addAll(blocks,
//                    new BlockHammerHead(),
//                    new BlockHammerBase(),
//                    new BlockForgedWall(),
//                    new BlockForgedPillar(),
//                    new BlockForgedPlatform(),
//                    new BlockForgedPlatformSlab(),
//                    new BlockForgedVent(),
//                    new BlockForgedContainer(),
//                    new BlockForgedCrate(),
//                    new BlockTransferUnit(),
//                    new BlockCoreExtractorBase(),
//                    new BlockCoreExtractorPiston(),
//                    new BlockCoreExtractorPipe(),
//                    new BlockSeepingBedrock()
//            );
//        }

        items = new Item[] {
                new ItemSwordModular(),
                new ItemGeode(),
                new ItemPristineLapis(),
                new ItemPristineEmerald(),
                new ItemPristineDiamond(),
//                new ItemToolbeltModular(),
//                new ItemDuplexToolModular(),
//                new ItemCellMagmatic(),
                new ItemBolt(),
                new ItemBeam(),
                new ItemMesh(),
                new ItemQuickLatch(),
                new ItemMetalScrap(),
                new ItemVentPlate(),
                new ItemJournal()
        };

        proxy.preInit(
                Arrays.stream(items)
                        .filter(item -> item instanceof ITetraItem)
                        .map(item -> (ITetraItem) item).toArray(ITetraItem[]::new),
                Arrays.stream(blocks)
                        .filter(block -> block instanceof ITetraBlock)
                        .map(block -> (ITetraBlock) block).toArray(ITetraBlock[]::new));
    }

    public void setup(FMLCommonSetupEvent event) {
        proxy.init(event);

//        ScreenManager.registerFactory(BlockForgedContainer.containerType, ForgedContainerScreen::new);

        PacketHandler packetHandler = new PacketHandler();

        Arrays.stream(items)
                .filter(item -> item instanceof ITetraItem)
                .map(item -> (ITetraItem) item)
                .forEach(item -> item.init(packetHandler));
        Arrays.stream(blocks)
                .filter(block -> block instanceof ITetraBlock)
                .map(block -> (ITetraBlock) block)
                .forEach(block -> block.init(packetHandler));

        packetHandler.registerPacket(HonePacket.class, HonePacket::new);
        packetHandler.registerPacket(SettlePacket.class, SettlePacket::new);

        proxy.postInit();
    }

    @SubscribeEvent
    public static void lootTableLoad(final LootTableLoadEvent event) {
        if (TetraMod.MOD_ID.equals(event.getName().getNamespace())) {
            LootTable lootTable = event.getTable();
            LootPool[] extendedPools = DataHandler.instance.getExtendedLootPools(event.getName());
            Optional.ofNullable(extendedPools)
                    .map(Arrays::stream)
                    .orElseGet(Stream::empty)
                    .forEach(lootTable::addPool);
        }
    }

    public void serverStarting(FMLServerStartingEvent event) {
        // TGenCommand.register(event.getCommandDispatcher());

        // todo 1.14: figure out feature generation again...
        // if (ConfigHandler.generateFeatures) { }
    }

    @SubscribeEvent
    public static void loadModels(final ModelBakeEvent event) {
        TetraLogger.log(event);
        ModelResourceLocation resourceLocation = new ModelResourceLocation(ItemSwordModular.instance.getRegistryName(), "inventory");
        IBakedModel oldModel = event.getModelRegistry().get(resourceLocation);
        BlockModel unbaked = (BlockModel) event.getModelLoader().getUnbakedModel(resourceLocation);

//        TextureAtlasSprite particleSprite = ModelLoader.defaultTextureGetter().apply(new ResourceLocation(unbaked.resolveTextureName("particle")));
//        IBakedModel model = new SimpleBakedModel.Builder(unbaked, ModularOverrideList.INSTANCE).setTexture(particleSprite).build();

        BakedWrapper wrapper = new BakedWrapper(event.getModelLoader(), ModelLoader.defaultTextureGetter(), ModelRotation.X0_Y0, net.minecraft.client.renderer.vertex.DefaultVertexFormats.ITEM);

        event.getModelRegistry().put(resourceLocation, wrapper);
    }

    @SubscribeEvent
    public static void provideTextures(final TextureStitchEvent.Pre event) {
        ItemUpgradeRegistry.instance.getAllModules().stream()
                .flatMap(itemModule -> Arrays.stream(itemModule.getAllTextures()))
                .forEach(event::addSprite);
    }

    @Mod.EventBusSubscriber(bus=Mod.EventBusSubscriber.Bus.MOD)
    public static class RegistryEvents {

        @SubscribeEvent
        public static void registerEffects(final RegistryEvent.Register<Effect> event) {
            event.getRegistry().register(new BleedingEffect());
            event.getRegistry().register(new EarthboundEffect());
        }

        @SubscribeEvent
        public static void registerContainerTypes(final RegistryEvent.Register<ContainerType<?>> event) {
//        event.getRegistry().register(IForgeContainerType.create(((windowId, inv, data) -> {
//            BlockPos pos = data.readBlockPos();
//            TileEntityForgedContainer te = (TileEntityForgedContainer) Minecraft.getInstance().world.getTileEntity(pos);
//            return new ForgedContainerContainer(windowId, te, inv, Minecraft.getInstance().player);
//        }))
//                .setRegistryName(MOD_ID, BlockForgedContainer.unlocalizedName));

            ContainerType workbenchContainerType = IForgeContainerType.create(((windowId, inv, data) -> {
                BlockPos pos = data.readBlockPos();
                TileEntityWorkbench te = (TileEntityWorkbench) Minecraft.getInstance().world.getTileEntity(pos);
                return new WorkbenchContainer(windowId, te, inv, Minecraft.getInstance().player);
            }))
                    .setRegistryName(MOD_ID, BlockWorkbench.unlocalizedName);
            event.getRegistry().register(workbenchContainerType);
        }

        @SubscribeEvent
        public static void registerBlocks(final RegistryEvent.Register<Block> event) {
            event.getRegistry().registerAll(blocks);
        }

        @SubscribeEvent
        public static void registerItems(final RegistryEvent.Register<Item> event) {
            event.getRegistry().registerAll(items);

            // todo 1.14: this is supposedly not needed, item rendering works?
//        if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT) {
//            Arrays.stream(items)
//                    .forEach(item -> {
//                        ModelLoader.setCustomModelResourceLocation(item, 0,
//                                new ModelResourceLocation(item.getRegistryName(), "inventory"));
//                    });
//        }

            Arrays.stream(blocks)
                    .filter(block -> block instanceof ITetraBlock)
                    .map(block -> (ITetraBlock) block)
                    .filter(ITetraBlock::hasItem)
                    .forEach(block -> block.registerItem(event.getRegistry()));
        }

        @SubscribeEvent
        public static void registerTileEntities(final RegistryEvent.Register<TileEntityType<?>> event) {
            // todo 1.14: workbench TE registry, do we really pass null here? (from mcjty tutorial)
            event.getRegistry().register(TileEntityType.Builder.create(TileEntityWorkbench::new, BlockWorkbench.instance)
                    .build(null)
                    .setRegistryName(MOD_ID, BlockWorkbench.unlocalizedName));
        }

        @SubscribeEvent
        public static void registerFeatures(final RegistryEvent.Register<Feature<?>> event) {

            // todo 1.14: this needs to be extended with alot of functionality
            event.getRegistry().register(new OreFeature(d ->
                    new OreFeatureConfig(OreFeatureConfig.FillerBlockType.NATURAL_STONE, BlockGeode.instance.getDefaultState(), 1)).setRegistryName(MOD_ID, BlockGeode.unlocalizedName));
        }
    }
}
