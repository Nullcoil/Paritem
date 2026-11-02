package net.nullcoil.paritem;

import net.fabricmc.api.ModInitializer;
import net.nullcoil.paritem.blocks.ModBlockEntities;
import net.nullcoil.paritem.blocks.ModBlockSets;
import net.nullcoil.paritem.blocks.ModBlocks;
import net.nullcoil.paritem.entity.ModEntities;
import net.nullcoil.paritem.gui.ModMenuTypes;
import net.nullcoil.paritem.items.ModItems;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Paritem implements ModInitializer {
	public static final String MOD_ID = "paritem";
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	private static final class SingletonHolder {
		private static final Paritem INSTANCE = new Paritem();
	}

	@Override
	public void onInitialize() {
		ModBlockSets.register();
		ModBlocks.register();
		ModBlockEntities.register();
		ModEntities.register();
		ModItems.register();
		ModMenuTypes.register();
		LOGGER.info("Finished registering for Paritem.");
	}
}