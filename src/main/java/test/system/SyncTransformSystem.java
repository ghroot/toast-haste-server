package test.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Profile;
import com.artemis.systems.IntervalIteratingSystem;
import com.nhnent.haste.framework.SendOptions;
import com.nhnent.haste.protocol.data.DataObject;
import com.nhnent.haste.protocol.messages.EventMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.MessageCodes;
import test.MyPeer;
import test.Profiler;
import test.WorldDimensions;
import test.component.PeerComponent;
import test.component.SpatialComponent;
import test.component.SyncTransformComponent;
import test.component.TransformComponent;

import javax.vecmath.Point2i;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Profile(enabled = true, using = Profiler.class)
public class SyncTransformSystem extends IntervalIteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(SyncTransformSystem.class);

	private ComponentMapper<TransformComponent> transformComponentMapper;
	private ComponentMapper<SyncTransformComponent> syncTransformComponentMapper;
	private ComponentMapper<PeerComponent> peerComponentMapper;
	private ComponentMapper<SpatialComponent> spatialComponentMapper;

	private final float SYNC_THRESHOLD = 0.05f;
	private final float SYNC_THRESHOLD_SQUARED = SYNC_THRESHOLD * SYNC_THRESHOLD;

	private Map<String, MyPeer> peers;
	private WorldDimensions worldDimensions;
	private Map<Point2i, Set<Integer>> spatialHashes;

	private float[] reusablePosition;
	private Point2i reusableHash;
	private Set<Integer> reusableNearbyPeerEntities;

	public SyncTransformSystem(Map<String, MyPeer> peers, WorldDimensions worldDimensions, Map<Point2i, Set<Integer>> spatialHashes) {
		super(Aspect.all(TransformComponent.class, SyncTransformComponent.class), 0.2f);
		this.peers = peers;
		this.worldDimensions = worldDimensions;
		this.spatialHashes = spatialHashes;
		reusablePosition = new float[2];
		reusableHash = new Point2i();
		reusableNearbyPeerEntities = new HashSet<Integer>();
	}

	@Override
	protected void inserted(int entity) {
		syncTransformComponentMapper.get(entity).lastSyncedPosition.set(transformComponentMapper.get(entity).position);
	}

	@Override
	protected void process(int entity) {
		TransformComponent transformComponent = transformComponentMapper.get(entity);
		SyncTransformComponent syncTransformComponent = syncTransformComponentMapper.get(entity);

		if (transformComponent.position.distanceSquared(syncTransformComponent.lastSyncedPosition) >= SYNC_THRESHOLD_SQUARED) {
			reusablePosition[0] = transformComponent.position.x;
			reusablePosition[1] = transformComponent.position.y;
			EventMessage positionMessage = new EventMessage(MessageCodes.SET_POSITION, new DataObject()
					.set(MessageCodes.SET_POSITION_ENTITY_ID, entity)
					.set(MessageCodes.SET_POSITION_POSITION, reusablePosition));
			Set<Integer> nearbyPeerEntities = getNearbyPeerEntities(entity);
			for (int nearbyPeerEntity : nearbyPeerEntities) {
				MyPeer peer = peers.get(peerComponentMapper.get(nearbyPeerEntity).name);
				if (peer != null) {
					peer.send(positionMessage, SendOptions.ReliableSend);
				}
			}
			syncTransformComponent.lastSyncedPosition.set(transformComponent.position);
		}
	}

	private Set<Integer> getNearbyPeerEntities(int entity) {
		reusableNearbyPeerEntities.clear();
		SpatialComponent spatialComponent = spatialComponentMapper.get(entity);
		if (spatialComponent.memberOfSpatialHash != null) {
			for (int x = spatialComponent.memberOfSpatialHash.x - worldDimensions.sectionSize; x <= spatialComponent.memberOfSpatialHash.x + worldDimensions.sectionSize; x += worldDimensions.sectionSize) {
				for (int y = spatialComponent.memberOfSpatialHash.y - worldDimensions.sectionSize; y <= spatialComponent.memberOfSpatialHash.y + worldDimensions.sectionSize; y += worldDimensions.sectionSize) {
					reusableHash.set(x, y);
					if (spatialHashes.containsKey(reusableHash)) {
						for (int nearbyEntity : spatialHashes.get(reusableHash)) {
							if (peerComponentMapper.has(nearbyEntity)) {
								reusableNearbyPeerEntities.add(nearbyEntity);
							}
						}
					}
				}
			}
		}
		return reusableNearbyPeerEntities;
	}
}
