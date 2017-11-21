package com.vast.system;

import com.artemis.BaseSystem;
import com.vast.Metrics;

public class MetricsManager extends AbstractProfiledBaseSystem {
	private Metrics metrics;

	private long lastTime;

	public MetricsManager(Metrics metrics) {
		this.metrics = metrics;
	}

	@Override
	protected void initialize() {
		super.initialize();

		lastTime = System.currentTimeMillis();
	}

	@Override
	protected void processSystem() {
		metrics.setTimePerFrameMs((int) (System.currentTimeMillis() - lastTime));
		lastTime = System.currentTimeMillis();
	}

	public void setSystemProcessingTime(BaseSystem system, int processingTime) {
		metrics.setSystemProcessingTime(system, processingTime);
	}
}
