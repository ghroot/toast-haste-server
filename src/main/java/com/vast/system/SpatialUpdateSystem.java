package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.vast.component.Spatial;
import com.vast.component.Static;
import com.vast.component.Transform;
import com.vast.data.SpatialHash;
import com.vast.data.WorldConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.vecmath.Point2f;
import java.util.Map;

public class SpatialUpdateSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(SpatialUpdateSystem.class);

	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Spatial> spatialMapper;

	private WorldConfiguration worldConfiguration;
	private Map<Integer, IntBag> spatialHashes;

	public SpatialUpdateSystem(WorldConfiguration worldConfiguration, Map<Integer, IntBag> spatialHashes) {
		super(Aspect.all(Transform.class, Spatial.class).exclude(Static.class));
		this.worldConfiguration = worldConfiguration;
		this.spatialHashes = spatialHashes;
	}

	@Override
	public void inserted(IntBag entities) {
	}

	@Override
	public void removed(IntBag entities) {
	}

	@Override
	protected void process(int entity) {
		Transform transform = transformMapper.get(entity);
		Spatial spatial = spatialMapper.get(entity);
		if (spatial.lastUsedPosition == null || !spatial.lastUsedPosition.equals(transform.position)) {
			updateSpatialHash(entity);
		}
	}

	private void updateSpatialHash(int entity) {
		Transform transform = transformMapper.get(entity);
		Spatial spatial = spatialMapper.get(entity);

		if (spatial.memberOfSpatialHash != null) {
			spatialHashes.get(spatial.memberOfSpatialHash.getUniqueKey()).removeValue(entity);
		} else {
			spatial.memberOfSpatialHash = new SpatialHash();
		}

		spatial.memberOfSpatialHash.setXY(
				Math.round(transform.position.x / worldConfiguration.sectionSize) * worldConfiguration.sectionSize,
				Math.round(transform.position.y / worldConfiguration.sectionSize) * worldConfiguration.sectionSize);

		IntBag entitiesInHash = spatialHashes.get(spatial.memberOfSpatialHash.getUniqueKey());
		if (entitiesInHash == null) {
			entitiesInHash = new IntBag();
			spatialHashes.put(spatial.memberOfSpatialHash.getUniqueKey(), entitiesInHash);
		}
		entitiesInHash.add(entity);

		if (spatial.lastUsedPosition == null) {
			spatial.lastUsedPosition = new Point2f(transform.position);
		} else {
			spatial.lastUsedPosition.set(transform.position);
		}
	}
}
