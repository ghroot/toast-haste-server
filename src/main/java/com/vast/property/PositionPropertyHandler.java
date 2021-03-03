package com.vast.property;

import com.artemis.ComponentMapper;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.component.SyncHistory;
import com.vast.component.Transform;
import com.vast.network.Properties;

import javax.vecmath.Point2f;
import javax.vecmath.Vector2f;

public class PositionPropertyHandler implements PropertyHandler {
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<SyncHistory> syncHistoryMapper;

	private float positionThreshold = 0.3f;

	private double[] reusablePosition;
	private Vector2f reusableVector;

	public PositionPropertyHandler(float positionThreshold) {
		this.positionThreshold = positionThreshold;

		reusablePosition = new double[2];
		reusableVector = new Vector2f();
	}

	@Override
	public byte getProperty() {
		return Properties.POSITION;
	}

	@Override
	public boolean decorateDataObject(int entity, DataObject dataObject, boolean force) {
		if (transformMapper.has(entity)) {
			Transform transform = transformMapper.get(entity);
			SyncHistory syncHistory = syncHistoryMapper.get(entity);

			float positionDifference = Float.MAX_VALUE;
			Point2f lastSyncedPosition = null;
			if (!force && syncHistory != null && syncHistory.syncedValues.containsKey(Properties.POSITION)) {
				lastSyncedPosition = (Point2f) syncHistory.syncedValues.get(Properties.POSITION);
				reusableVector.set(lastSyncedPosition.x - transform.position.x, lastSyncedPosition.y - transform.position.y);
				positionDifference = reusableVector.length();
			}
			if (force || positionDifference >= positionThreshold) {
				reusablePosition[0] = transform.position.x;
				reusablePosition[1] = transform.position.y;
				dataObject.set(Properties.POSITION, reusablePosition);
				if (syncHistory != null) {
					if (lastSyncedPosition != null) {
						lastSyncedPosition.set(transform.position);
					} else {
						syncHistory.syncedValues.put(Properties.POSITION, new Point2f(transform.position));
					}
				}
				return true;
			}
		}
		return false;
	}
}
