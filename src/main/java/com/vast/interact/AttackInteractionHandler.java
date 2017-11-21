package com.vast.interact;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.vast.Properties;
import com.vast.component.*;
import com.vast.system.TimeManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class AttackInteractionHandler extends AbstractInteractionHandler {
	private static final Logger logger = LoggerFactory.getLogger(AttackInteractionHandler.class);

	private ComponentMapper<Attack> attackMapper;
	private ComponentMapper<Health> healthMapper;
	private ComponentMapper<Event> eventMapper;
	private ComponentMapper<Delete> deleteMapper;
	private ComponentMapper<Sync> syncMapper;

	public AttackInteractionHandler() {
		super(Aspect.all(Attack.class), Aspect.all(Health.class));
	}

	@Override
	public void start(int attackEntity, int healthEntity) {
	}

	@Override
	public boolean process(int attackEntity, int healthEntity) {
		Attack attack = attackMapper.get(attackEntity);
		float time = world.getSystem(TimeManager.class).getTime();
		if (time - attack.lastAttackTime >= attack.cooldown) {
			Health health = healthMapper.get(healthEntity);
			health.takeDamage(1);
			eventMapper.create(attackEntity).name = "attacked";
			logger.debug("Entity {} is attacking entity {}, health left: {}", attackEntity, healthEntity, health.health);
			if (health.isDead()) {
				logger.debug("Entity {} was killed by entity {}", healthEntity, attackEntity);
				deleteMapper.create(healthEntity).reason = "killed";
				return true;
			} else {
				syncMapper.create(healthEntity).markPropertyAsDirty(Properties.HEALTH);
			}
			attack.lastAttackTime = time;
		}
		return false;
	}

	@Override
	public void stop(int attackEntity, int healthEntity) {
	}
}