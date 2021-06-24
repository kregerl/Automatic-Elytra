package com.loucaskreger.automaticelytra;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(AutomaticElytra.MOD_ID)
public class AutomaticElytra {
    // Directly reference a log4j logger.
    public static final String MOD_ID = "automaticelytra";
    private static final Logger LOGGER = LogManager.getLogger();

    public AutomaticElytra() {
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        bus.addListener(this::setupCommon);
        bus.addListener(this::setupClient);
    }

    private void setupCommon(final FMLCommonSetupEvent event) {

    }

    private void setupClient(final FMLClientSetupEvent event) {

    }

}
