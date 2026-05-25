package com.sondz.autowater;

import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Son implements ModInitializer {
    public static final String MOD_ID = "autowater";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    @Override
    public void onInitialize() {
        LOGGER.info("Auto Water Clutch Mod đã được kích hoạt thành công!");
    }
}