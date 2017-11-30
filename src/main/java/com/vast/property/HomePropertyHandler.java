package com.vast.property;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.MessageCodes;
import com.vast.Properties;
import com.vast.component.Home;

public class HomePropertyHandler implements PropertyHandler {
	private ComponentMapper<Home> homeMapper;

	private double[] reusablePosition;

	public HomePropertyHandler() {
		reusablePosition = new double[2];
	}

	@Override
	public int getProperty() {
		return Properties.HOME;
	}

	@Override
	public void decorateDataObject(int entity, DataObject dataObject, boolean force) {
		if (homeMapper.has(entity)) {
			Home home = homeMapper.get(entity);
			reusablePosition[0] = home.position.x;
			reusablePosition[1] = home.position.y;
			dataObject.set(MessageCodes.PROPERTY_HOME, reusablePosition);
		}
	}
}
