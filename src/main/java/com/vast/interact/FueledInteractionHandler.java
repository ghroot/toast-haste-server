package com.vast.interact;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.vast.Properties;
import com.vast.component.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FueledInteractionHandler extends AbstractInteractionHandler {
	private static final Logger logger = LoggerFactory.getLogger(FueledInteractionHandler.class);

	private ComponentMapper<Fueled> fueledMapper;
	private ComponentMapper<Inventory> inventoryMapper;
	private ComponentMapper<Sync> syncMapper;
	private ComponentMapper<Message> messageMapper;

	public FueledInteractionHandler() {
		super(Aspect.all(Player.class, Inventory.class), Aspect.all(Fueled.class));
	}

	@Override
	public boolean canInteract(int playerEntity, int fueledEntity) {
		return !fueledMapper.get(fueledEntity).isFueled();
	}

	@Override
	public void start(int playerEntity, int fueledEntity) {
	}

	@Override
	public boolean process(int playerEntity, int fueledEntity) {
		Inventory inventory = inventoryMapper.get(playerEntity);
		Fueled fueled = fueledMapper.get(fueledEntity);

		if (inventory.has(fueled.cost)) {
			inventory.remove(fueled.cost);
			syncMapper.create(playerEntity).markPropertyAsDirty(Properties.INVENTORY);

			fueled.timeLeft = 60.0f;
			syncMapper.create(fueledEntity).markPropertyAsDirty(Properties.FUELED);
		} else {
			messageMapper.create(playerEntity).text = "I don't have the required materials...";
		}

		return true;
	}

	@Override
	public void stop(int playerEntity, int fueledEntity) {
	}
}
