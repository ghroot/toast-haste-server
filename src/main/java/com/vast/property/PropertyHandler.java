package com.vast.property;

import com.nhnent.haste.protocol.data.DataObject;

public interface PropertyHandler {
	byte getProperty();
	boolean decorateDataObject(int entity, DataObject dataObject, boolean force);
}
