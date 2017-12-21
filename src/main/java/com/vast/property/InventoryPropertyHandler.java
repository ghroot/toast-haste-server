package com.vast.property;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.component.Inventory;
import com.vast.data.Properties;

public class InventoryPropertyHandler implements PropertyHandler {
	private ComponentMapper<Inventory> inventoryMapper;

	@Override
	public int getProperty() {
		return Properties.INVENTORY;
	}

	@Override
	public boolean decorateDataObject(int entity, DataObject dataObject, boolean force) {
		if (inventoryMapper.has(entity)) {
			dataObject.set(Properties.INVENTORY, inventoryMapper.get(entity).items);
			return true;
		}
		return false;
	}
}
