package test.system;

import com.artemis.Archetype;
import com.artemis.ArchetypeBuilder;
import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Profile;
import com.artemis.io.JsonArtemisSerializer;
import com.artemis.io.SaveFileFormat;
import com.artemis.managers.WorldSerializationManager;
import com.artemis.systems.IntervalSystem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import test.Profiler;
import test.component.*;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

@Profile(enabled = true, using = Profiler.class)
public class WorldSerializationSystem extends IntervalSystem {
	private static final Logger logger = LoggerFactory.getLogger(WorldSerializationSystem.class);

	private final float RANDOMIZATION_AREA_SIZE = 400.0f;

	private ComponentMapper<PeerComponent> peerComponentMapper;
	private ComponentMapper<TransformComponent> transformComponentMapper;
	private ComponentMapper<SyncTransformComponent> syncTransformComponentMapper;
	private ComponentMapper<AIComponent> aiComponentMapper;
	private ComponentMapper<TypeComponent> typeComponentMapper;
	private ComponentMapper<CollisionComponent> collisionComponentMapper;

	private String snapshotFileName;
	private Archetype aiArchetype;
	private Archetype treeArchetype;

	public WorldSerializationSystem() {
		super(Aspect.all(), 10.0f);
	}

	@Override
	protected void initialize() {
		aiArchetype = new ArchetypeBuilder()
				.add(AIComponent.class)
				.add(TypeComponent.class)
				.add(TransformComponent.class)
				.add(SpatialComponent.class)
				.add(CollisionComponent.class)
				.add(SyncTransformComponent.class)
				.build(world);

		treeArchetype = new ArchetypeBuilder()
				.add(TypeComponent.class)
				.add(TransformComponent.class)
				.add(SpatialComponent.class)
				.add(CollisionComponent.class)
				.build(world);

		WorldSerializationManager worldSerializationManager = world.getSystem(WorldSerializationManager.class);

		worldSerializationManager.setSerializer(new JsonArtemisSerializer(world));
		snapshotFileName = "snapshot.json";

//		worldSerializationManager.setSerializer(new KryoArtemisSerializer(world));
//		snapshotFileName = "snapshot.bin";

		loadWorld();
	}

	@Override
	protected void processSystem() {
		saveWorld();
	}

	private void saveWorld() {
		logger.debug("Serializing world to snapshot {}", snapshotFileName);
		WorldSerializationManager worldSerializationManager = world.getSystem(WorldSerializationManager.class);
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		worldSerializationManager.save(baos, new SaveFileFormat(world.getAspectSubscriptionManager().get(Aspect.all()).getEntities()));
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(snapshotFileName);
			baos.writeTo(fileOutputStream);
			fileOutputStream.close();
		} catch (Exception exception) {
			logger.error("Error saving world", exception);
		}
	}

	private void loadWorld() {
		try {
			WorldSerializationManager worldSerializationManager = world.getSystem(WorldSerializationManager.class);
			FileInputStream fileInputStream = new FileInputStream(snapshotFileName);
			SaveFileFormat saveFileFormat = worldSerializationManager.load(fileInputStream, SaveFileFormat.class);
			logger.info("Loading world from snapshot {}", snapshotFileName);
			for (int i = 0; i < saveFileFormat.entities.size(); i++) {
				int entity = saveFileFormat.entities.get(i);
				if (peerComponentMapper.has(entity)) {
					logger.info("Loaded entity: {} (peer)", entity);
				} else {
					logger.info("Loaded entity: {} ({})", entity, typeComponentMapper.get(entity).type);
				}
			}
		} catch (Exception exception) {
			if (exception instanceof FileNotFoundException) {
				logger.info("No snapshot file found, creating a new world");
				createWorld();
			} else {
				logger.error("Error loading world", exception);
			}
		}
	}

	private void createWorld() {
		for (int i = 0; i < 500; i++) {
			int aiEntity = world.create(aiArchetype);
			typeComponentMapper.get(aiEntity).type = "ai";
			transformComponentMapper.get(aiEntity).position.set(-RANDOMIZATION_AREA_SIZE / 2 + (float) Math.random() * RANDOMIZATION_AREA_SIZE, -RANDOMIZATION_AREA_SIZE / 2 + (float) Math.random() * RANDOMIZATION_AREA_SIZE);
			logger.info("Creating AI entity: {}", aiEntity);
		}

		for (int i = 0; i < 50; i++) {
			int treeEntity = world.create(treeArchetype);
			typeComponentMapper.get(treeEntity).type = "tree";
			transformComponentMapper.get(treeEntity).position.set(-RANDOMIZATION_AREA_SIZE / 2 + (float) Math.random() * RANDOMIZATION_AREA_SIZE, -RANDOMIZATION_AREA_SIZE / 2 + (float) Math.random() * RANDOMIZATION_AREA_SIZE);
			collisionComponentMapper.get(treeEntity).isStatic = true;
			collisionComponentMapper.get(treeEntity).radius = 0.1f;
			logger.info("Creating tree entity: {}", treeEntity);
		}
	}
}
