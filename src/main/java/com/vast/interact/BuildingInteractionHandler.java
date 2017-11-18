package com.vast.interact;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.vast.component.Building;
import com.vast.component.Player;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BuildingInteractionHandler extends AbstractInteractionHandler {
	private static final Logger logger = LoggerFactory.getLogger(BuildingInteractionHandler.class);

	private ComponentMapper<Building> buildingMapper;

	public BuildingInteractionHandler() {
		super(Aspect.all(Player.class), Aspect.all(Building.class));
	}

	@Override
	public boolean process(int playerEntity, int buildingEntity) {
		Building building = buildingMapper.get(buildingEntity);
		building.progress++;
		if (building.progress % 25 == 0) {
			logger.debug("Player entity {} is building entity {}, progress: {}", playerEntity, buildingEntity, building.progress);
		}
		if (building.progress >= 100) {
			return true;
		} else {
			return false;
		}
	}
}