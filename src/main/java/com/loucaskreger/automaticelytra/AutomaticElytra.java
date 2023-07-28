package com.loucaskreger.automaticelytra;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
public class AutomaticElytra implements ModInitializer {
    public static final String MOD_ID = "automaticelytra";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Automatic Elytra started");

    }

}
