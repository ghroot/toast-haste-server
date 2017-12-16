package com.vast.interact;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.vast.Properties;
import com.vast.component.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConstructableInteractionHandler extends AbstractInteractionHandler {
	private static final Logger logger = LoggerFactory.getLogger(ConstructableInteractionHandler.class);

	private ComponentMapper<Constructable> constructableMapper;
	private ComponentMapper<Sync> syncMapper;
	private ComponentMapper<State> stateMapper;

	public ConstructableInteractionHandler() {
		super(Aspect.all(Player.class), Aspect.all(Constructable.class));
	}

	@Override
	public boolean canInteract(int playerEntity, int constructableEntity) {
		return !constructableMapper.get(constructableEntity).isComplete();
	}

	@Override
	public boolean attemptStart(int playerEntity, int constructableEntity) {
		stateMapper.get(playerEntity).name = "building";
		syncMapper.create(playerEntity).markPropertyAsDirty(Properties.STATE);
		return true;
	}

	@Override
	public boolean process(int playerEntity, int constructableEntity) {
		Constructable constructable = constructableMapper.get(constructableEntity);
		constructable.buildTime += world.getDelta();
		syncMapper.create(constructableEntity).markPropertyAsDirty(Properties.PROGRESS);
		if (constructable.isComplete()) {
			constructableMapper.remove(constructableEntity);
			return true;
		} else {
			return false;
		}
	}

	@Override
	public void stop(int playerEntity, int buildingEntity) {
		stateMapper.get(playerEntity).name = null;
		syncMapper.create(playerEntity).markPropertyAsDirty(Properties.STATE);
	}
}
