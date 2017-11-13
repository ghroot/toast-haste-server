package com.vast.interact;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.vast.component.Delete;
import com.vast.component.Harvestable;
import com.vast.component.Interactable;
import com.vast.component.Inventory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HarvestableInteractionHandler extends AbstractInteractionHandler {
	private static final Logger logger = LoggerFactory.getLogger(HarvestableInteractionHandler.class);

	private ComponentMapper<Harvestable> harvestableMapper;
	private ComponentMapper<Inventory> inventoryMapper;
	private ComponentMapper<Delete> deleteMapper;

	public HarvestableInteractionHandler() {
		super(Aspect.all(Inventory.class), Aspect.all(Interactable.class, Harvestable.class));
	}

	@Override
	public boolean process(int playerEntity, int harvestableEntity) {
		Harvestable harvestable = harvestableMapper.get(harvestableEntity);
		harvestable.durability--;
		if (harvestable.durability % 50 == 0) {
			logger.debug("Player entity {} is harvesting entity {}, durability left: {}", playerEntity, harvestableEntity, harvestable.durability);
		}
		if (harvestable.durability <= 0) {
			inventoryMapper.get(playerEntity).add(harvestable.itemType, harvestable.itemCount);
			deleteMapper.create(harvestableEntity).reason = "harvested";
			return true;
		} else {
			return false;
		}
	}
}