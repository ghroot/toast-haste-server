package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.vast.component.Event;
import com.vast.component.Observer;
import com.vast.data.WorldConfiguration;

public class DayNightCycleSystem extends IteratingSystem {
	private ComponentMapper<Event> eventMapper;

	private WorldConfiguration worldConfiguration;

	private TimeManager timeManager;
	private boolean changed;

	public DayNightCycleSystem(WorldConfiguration worldConfiguration) {
		super(Aspect.all(Observer.class));

		this.worldConfiguration = worldConfiguration;
	}

	@Override
	protected void initialize() {
		timeManager = world.getSystem(TimeManager.class);
		changed = false;
	}

	@Override
	protected void inserted(int observerEntity) {
		eventMapper.create(observerEntity).addEntry(isDay(timeManager.getTime()) ? "dayInital" : "nightInitial")
				.setOwnerPropagation();
	}

	@Override
	public void removed(IntBag entities) {
	}

	@Override
	protected void begin() {
		boolean wasDay = isDay(timeManager.getPreviousTime());
		boolean isDay = isDay(timeManager.getTime());
		changed = isDay != wasDay;
	}

	@Override
	protected void process(int observerEntity) {
		if (changed) {
			eventMapper.create(observerEntity).addEntry(isDay(timeManager.getTime()) ? "dayChanged" : "nightChanged")
				.setOwnerPropagation();
		}
	}

	private boolean isDay(float time) {
		float timeIntoDay = time % ((worldConfiguration.dayDuration + worldConfiguration.nightDuration) * 60.0f);
		return timeIntoDay < worldConfiguration.dayDuration * 60.0f;
	}
}
