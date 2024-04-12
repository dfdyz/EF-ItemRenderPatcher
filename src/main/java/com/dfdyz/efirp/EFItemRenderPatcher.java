package com.dfdyz.efirp;

import com.dfdyz.efirp.client.Loader.PatchedItemSkinLoader;
import com.mojang.logging.LogUtils;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.RegistryEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.IModBusEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.fml.loading.FMLEnvironment;
import org.slf4j.Logger;
import yesman.epicfight.api.client.model.ItemSkins;
import yesman.epicfight.api.client.model.Meshes;
import yesman.epicfight.gameasset.Armatures;

import java.util.stream.Collectors;

// The value here should match an entry in the META-INF/mods.toml file
@Mod(EFItemRenderPatcher.MODID)
public class EFItemRenderPatcher
{
    // Directly reference a slf4j logger
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final String MODID = "ef_irp";

    public EFItemRenderPatcher()
    {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::setup);
        if(FMLEnvironment.dist == Dist.CLIENT){
            bus.addListener(this::registerClientReloadListnerEvent);
        }
        else {
            LOGGER.warn("[EpicFight ItemRenderPatcher] This is a client mod, please not add it to server.");
        }
        MinecraftForge.EVENT_BUS.register(this);
    }

    private void setup(final FMLCommonSetupEvent event)
    {

    }

    private void registerClientReloadListnerEvent(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new PatchedItemSkinLoader());
    }

}
