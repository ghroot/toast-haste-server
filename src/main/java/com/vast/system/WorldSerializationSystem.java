package com.vast.system;

import com.artemis.Aspect;
import com.artemis.BaseSystem;
import com.artemis.ComponentMapper;
import com.artemis.EntitySubscription;
import com.artemis.annotations.All;
import com.artemis.io.JsonArtemisSerializer;
import com.artemis.io.KryoArtemisSerializer;
import com.artemis.io.SaveFileFormat;
import com.artemis.managers.WorldSerializationManager;
import com.artemis.utils.IntBag;
import com.vast.component.*;
import com.vast.data.Metrics;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class WorldSerializationSystem extends BaseSystem {
	private static final Logger logger = LoggerFactory.getLogger(WorldSerializationSystem.class);

	private static final int SAVE_NOTIFICATION_TIME = 10;

	private CreationManager creationManager;
	private ComponentMapper<Event> eventMapper;
	private ComponentMapper<Observer> observerMapper;
	private ComponentMapper<Observed> observedMapper;
	private ComponentMapper<Known> knownMapper;

	@All(Observer.class)
	private EntitySubscription observerSubscription;

	private String snapshotFile;
	private Metrics metrics;
	private float saveInterval;

	private float timeSinceLastSave;
	private boolean save;

	public WorldSerializationSystem(String snapshotFile, Metrics metrics, float saveInterval) {
		this.snapshotFile = snapshotFile;
		this.metrics = metrics;
		this.saveInterval = saveInterval;
	}

	@Override
	protected void initialize() {
		loadWorld();
	}

	@Override
	protected void processSystem() {
		if (save) {
			saveWorld();
			save = false;
			timeSinceLastSave = 0f;
		} else {
			float notificationTime = saveInterval - SAVE_NOTIFICATION_TIME;
			if (timeSinceLastSave < notificationTime && timeSinceLastSave + world.delta >= notificationTime) {
				notifyAllActivePlayers();
			}
			timeSinceLastSave += world.delta;
			if (timeSinceLastSave >= saveInterval) {
				save = true;
			}
		}
	}

	private void notifyAllActivePlayers() {
		int worldEntity = world.getAspectSubscriptionManager().get(Aspect.all(Time.class)).getEntities().get(0);
		eventMapper.create(worldEntity).addEntry("message")
				.setData("Saving world in " + SAVE_NOTIFICATION_TIME + " seconds...").setAllPropagation();
	}

	// TODO: Can this be done in a thread?
	private void saveWorld() {
		if (snapshotFile == null) {
			return;
		}

		disconnectObservers();

		long startTime = System.currentTimeMillis();
		WorldSerializationManager worldSerializationManager = world.getSystem(WorldSerializationManager.class);
		if (snapshotFile.endsWith(".json")) {
			worldSerializationManager.setSerializer(new JsonArtemisSerializer(world));
		} else {
			worldSerializationManager.setSerializer(new KryoArtemisSerializer(world));
		}
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		IntBag entitiesToSerialize = world.getAspectSubscriptionManager().get(Aspect.all().exclude(Transient.class)).getEntities();
		logger.info("Serializing world ({} entities) to snapshot file {}", entitiesToSerialize.size(), snapshotFile);
		worldSerializationManager.save(byteArrayOutputStream, new SaveFileFormat(entitiesToSerialize));
		int generateDuration = (int) (System.currentTimeMillis() - startTime);
		startTime = System.currentTimeMillis();
		try {
			FileOutputStream fileOutputStream = new FileOutputStream(snapshotFile);
			byteArrayOutputStream.writeTo(fileOutputStream);
			fileOutputStream.close();
			byteArrayOutputStream.close();
			int saveDuration = (int) (System.currentTimeMillis() - startTime);
			logger.info("Serialization completed successfully, size: {} bytes (generate: {} ms, save: {} ms)", byteArrayOutputStream.size(), generateDuration, saveDuration);
		} catch (Exception exception) {
			logger.error("Error saving world", exception);
		}

		reconnectObservers();

		if (metrics != null) {
			metrics.setLastSerializeTime(System.currentTimeMillis());
		}
	}

	private void disconnectObservers() {
		IntBag observerEntities = observerSubscription.getEntities();
		for (int i = 0; i < observerEntities.size(); i++) {
			int observerEntity = observerEntities.get(i);
			Observer observer = observerMapper.get(observerEntity);
			observedMapper.get(observer.observedEntity).observerEntity = -1;
			for (int j = 0; j < observer.knowEntities.size(); j++) {
				knownMapper.get(observer.knowEntities.get(j)).knownByEntities.removeValue(observerEntity);
			}
		}
	}

	private void reconnectObservers() {
		IntBag observerEntities = observerSubscription.getEntities();
		for (int i = 0; i < observerEntities.size(); i++) {
			int observerEntity = observerEntities.get(i);
			Observer observer = observerMapper.get(observerEntity);
			observedMapper.get(observer.observedEntity).observerEntity = observerEntity;
			for (int j = 0; j < observer.knowEntities.size(); j++) {
				knownMapper.get(observer.knowEntities.get(j)).knownByEntities.add(observerEntity);
			}
		}
	}

	private void loadWorld() {
		try {
			if (snapshotFile == null) {
				throw new FileNotFoundException();
			}
			WorldSerializationManager worldSerializationManager = world.getSystem(WorldSerializationManager.class);
			FileInputStream fileInputStream = new FileInputStream(snapshotFile);
			if (snapshotFile.endsWith(".json")) {
				worldSerializationManager.setSerializer(new JsonArtemisSerializer(world));
			} else {
				worldSerializationManager.setSerializer(new KryoArtemisSerializer(world));
			}
			SaveFileFormat saveFileFormat = worldSerializationManager.load(fileInputStream, SaveFileFormat.class);
			logger.info("Loading world from snapshot file {}, size: {} bytes", snapshotFile, fileInputStream.getChannel().size());
			fileInputStream.close();
			logger.debug("Loaded {} entities", saveFileFormat.entities.size());
			if (metrics != null) {
				metrics.setLastSerializeTime(System.currentTimeMillis());
			}
		} catch (Exception exception) {
			if (exception instanceof FileNotFoundException) {
				logger.info("No snapshot file found, creating a new world");
				creationManager.createWorld();
				if (metrics != null) {
					metrics.setLastSerializeTime(System.currentTimeMillis());
				}
			} else {
				logger.error("Error loading world", exception);
			}
		}
	}
}
