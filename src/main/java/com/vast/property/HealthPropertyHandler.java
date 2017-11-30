package com.vast.property;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.MessageCodes;
import com.vast.Properties;
import com.vast.component.Health;

public class HealthPropertyHandler implements PropertyHandler {
	private ComponentMapper<Health> healthMapper;

	@Override
	public int getProperty() {
		return Properties.HEALTH;
	}

	@Override
	public void decorateDataObject(int entity, DataObject dataObject, boolean force) {
		if (healthMapper.has(entity)) {
			dataObject.set(MessageCodes.PROPERTY_HEALTH, healthMapper.get(entity).health);
		}
	}
}
