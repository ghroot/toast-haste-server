package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.utils.IntBag;
import com.nhnent.haste.protocol.data.DataObject;
import com.nhnent.haste.protocol.messages.EventMessage;
import com.vast.Metrics;
import com.vast.component.Active;
import com.vast.component.Player;
import com.vast.component.Sync;
import com.vast.component.SyncPropagation;
import com.vast.network.MessageCodes;
import com.vast.network.VastPeer;
import com.vast.property.PropertyHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

public class SyncSystem extends AbstractNearbyEntityIteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(SyncSystem.class);

	private ComponentMapper<Sync> syncMapper;
	private ComponentMapper<SyncPropagation> syncPropagationMapper;
	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Active> activeMapper;

	private Set<PropertyHandler> propertyHandlers;
	private Map<String, VastPeer> peers;
	private Metrics metrics;
	private EventMessage reusableMessage;

	public SyncSystem(Set<PropertyHandler> propertyHandlers, Map<String, VastPeer> peers, Metrics metrics) {
		super(Aspect.all(Sync.class, SyncPropagation.class));
		this.propertyHandlers = propertyHandlers;
		this.peers = peers;
		this.metrics = metrics;

		reusableMessage = new EventMessage(MessageCodes.UPDATE_PROPERTIES);
	}

	@Override
	protected void initialize() {
		for (PropertyHandler propertyHandler : propertyHandlers) {
			world.inject(propertyHandler);
		}
	}

	@Override
	public void inserted(IntBag entities) {
	}

	@Override
	public void removed(IntBag entities) {
	}

	@Override
	protected void process(int syncEntity, Set<Integer> nearbyEntities) {
		Sync sync = syncMapper.get(syncEntity);

		boolean reliable = false;
		boolean atLeastOnePropertySet = false;
		reusableMessage.getDataObject().clear();
		reusableMessage.getDataObject().set(MessageCodes.UPDATE_PROPERTIES_ENTITY_ID, syncEntity);
		DataObject propertiesDataObject = new DataObject();
		reusableMessage.getDataObject().set(MessageCodes.UPDATE_PROPERTIES_PROPERTIES, propertiesDataObject);
		for (PropertyHandler syncHandler : propertyHandlers) {
			byte property = syncHandler.getProperty();
			if (sync.isPropertyDirty(property)) {
				SyncPropagation syncPropagation = syncPropagationMapper.get(syncEntity);
				if (syncPropagation.isNearbyPropagation(property)) {
					if (syncHandler.decorateDataObject(syncEntity, propertiesDataObject, false)) {
						atLeastOnePropertySet = true;
						metrics.incrementSyncedProperty(property);
					}
				}
				if (!reliable && syncPropagation.isReliable(property)) {
					reliable = true;
				}
			}
		}
		if (atLeastOnePropertySet) {
			for (int nearbyEntity : nearbyEntities) {
				if (playerMapper.has(nearbyEntity) && activeMapper.has(nearbyEntity)) {
					VastPeer nearbyPeer = peers.get(playerMapper.get(nearbyEntity).name);
					if (reliable) {
						nearbyPeer.send(reusableMessage);
					} else {
						nearbyPeer.sendUnreliable(reusableMessage);
					}
				}
			}
		}

		if (playerMapper.has(syncEntity) && activeMapper.has(syncEntity)) {
			reliable = false;
			atLeastOnePropertySet = false;
			reusableMessage.getDataObject().clear();
			reusableMessage.getDataObject().set(MessageCodes.UPDATE_PROPERTIES_ENTITY_ID, syncEntity);
			propertiesDataObject = new DataObject();
			reusableMessage.getDataObject().set(MessageCodes.UPDATE_PROPERTIES_PROPERTIES, propertiesDataObject);
			for (PropertyHandler syncHandler : propertyHandlers) {
				byte property = syncHandler.getProperty();
				if (sync.isPropertyDirty(property)) {
					SyncPropagation syncPropagation = syncPropagationMapper.get(syncEntity);
					if (syncPropagation.isOwnerPropagation(property)) {
						if (syncHandler.decorateDataObject(syncEntity, propertiesDataObject, false)) {
							atLeastOnePropertySet = true;
							metrics.incrementSyncedProperty(property);
						}
					}
					if (!reliable && syncPropagation.isReliable(property)) {
						reliable = true;
					}
				}
			}
			if (atLeastOnePropertySet) {
				VastPeer nearbyPeer = peers.get(playerMapper.get(syncEntity).name);
				if (reliable) {
					nearbyPeer.send(reusableMessage);
				} else {
					nearbyPeer.sendUnreliable(reusableMessage);
				}
			}
		}

		syncMapper.remove(syncEntity);
	}
}
