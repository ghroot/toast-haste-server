package com.vast.system;

import com.artemis.*;
import com.artemis.io.JsonArtemisSerializer;
import com.artemis.managers.WorldSerializationManager;
import com.vast.component.*;
import com.vast.data.WorldConfiguration;
import com.vast.data.*;
import com.vast.prefab.*;
import fastnoise.FastNoise;

import javax.vecmath.Point2f;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

public class CreationManager extends BaseSystem {
	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Type> typeMapper;
	private ComponentMapper<SubType> subTypeMapper;
	private ComponentMapper<Collision> collisionMapper;
	private ComponentMapper<Inventory> inventoryMapper;
	private ComponentMapper<Harvestable> harvestableMapper;
	private ComponentMapper<AI> aiMapper;
	private ComponentMapper<Constructable> constructableMapper;
	private ComponentMapper<Container> containerMapper;
	private ComponentMapper<Fueled> fueledMapper;
	private ComponentMapper<Parent> parentMapper;
	private ComponentMapper<SyncPropagation> syncPropagationMapper;
	private ComponentMapper<Speed> speedMapper;
	private ComponentMapper<Growing> growingMapper;
	private ComponentMapper<Plantable> plantableMapper;
	private ComponentMapper<Group> groupMapper;
	private ComponentMapper<Teach> teachMapper;
	private ComponentMapper<Owner> ownerMapper;

	private WorldConfiguration worldConfiguration;
	private Random random;

	private WorldPrefab worldPrefab;
	private PlayerPrefab playerPrefab;
	private Map<String, VastPrefab> terrainPrefabs;
	private Map<String, VastPrefab> animalPrefabs;
	private Map<String, VastPrefab> buildingPrefabs;
	private Map<String, VastPrefab> pickupPrefabs;

	private int nextAnimalGroupId = 0;

	public CreationManager(WorldConfiguration worldConfiguration, Random random) {
		this.worldConfiguration = worldConfiguration;
		this.random = random;
	}

	@Override
	protected void initialize() {
		WorldSerializationManager worldSerializationManager = world.getSystem(WorldSerializationManager.class);
		worldSerializationManager.setSerializer(new JsonArtemisSerializer(world));

		worldPrefab = new WorldPrefab(world);
		playerPrefab = new PlayerPrefab(world);
		terrainPrefabs = new HashMap<>();
		terrainPrefabs.put("tree", new TerrainPrefabs.TreePrefab(world));
		terrainPrefabs.put("rock", new TerrainPrefabs.RockPrefab(world));
		animalPrefabs = new HashMap<>();
		animalPrefabs.put("rabbitAdult", new AnimalPrefabs.RabbitAdultPrefab(world));
		animalPrefabs.put("rabbitYoung", new AnimalPrefabs.RabbitAdultPrefab(world));
		animalPrefabs.put("deerAdult", new AnimalPrefabs.RabbitAdultPrefab(world));
		animalPrefabs.put("deerYoung", new AnimalPrefabs.RabbitAdultPrefab(world));
		buildingPrefabs = new HashMap<>();
		buildingPrefabs.put("chest", new BuildingPrefabs.ChestPrefab(world));
		buildingPrefabs.put("fireplace", new BuildingPrefabs.FireplacePrefab(world));
		buildingPrefabs.put("planter", new BuildingPrefabs.PlanterPrefab(world));
		buildingPrefabs.put("torch", new BuildingPrefabs.TorchPrefab(world));
		buildingPrefabs.put("wall", new BuildingPrefabs.WallPrefab(world));
		pickupPrefabs = new HashMap<>();
		pickupPrefabs.put("harvestedResources", new PickupPrefabs.HarvestedResourcesTemplate(world));
		pickupPrefabs.put("woodPile", new PickupPrefabs.WoodPileTemplate(world));
		pickupPrefabs.put("stonePile", new PickupPrefabs.StonePileTemplate(world));
	}

	@Override
	protected void processSystem() {
	}

	public void createWorld() {
		worldPrefab.createEntity();

		FastNoise noise1 = new FastNoise((int) (random.nextDouble() * 10000000));
		FastNoise noise2 = new FastNoise((int) (random.nextDouble() * 10000000));
		for (float x = -worldConfiguration.width / 2f; x < worldConfiguration.width / 2f; x += 3f) {
			for (float y = -worldConfiguration.height / 2f; y < worldConfiguration.height / 2f; y += 3f) {
				if (noise1.GetSimplex(x, y) > 0.35f) {
					createTree(new Point2f(x - 1f + random.nextFloat() * 2f, y - 1f + random.nextFloat() * 2f), false);
				}
				if (noise1.GetWhiteNoise(x, y) > 0.9f) {
					createRock(new Point2f(x, y));
				}
				if (noise2.GetWhiteNoise(x, y) > 0.99f) {
					createAnimalGroup(new Point2f(x, y), random.nextFloat() < 0.5f ? "rabbit" : "deer");
				}
			}
		}
	}

	public int createPlayer(String name, int subType, boolean fakePlayer) {
		int playerEntity = playerPrefab.createEntity();
		playerMapper.get(playerEntity).name = name;
		subTypeMapper.get(playerEntity).subType = subType;
		transformMapper.get(playerEntity).position.set(getRandomPositionInWorld());
		if (fakePlayer) {
			aiMapper.create(playerEntity).behaviourName = "human";
		}

		return playerEntity;
	}

	public int createTree(Point2f position, boolean growing) {
		int treeEntity = terrainPrefabs.get("tree").createEntity();
		subTypeMapper.get(treeEntity).subType = (int) (random.nextFloat() * 6);
		transformMapper.get(treeEntity).position.set(position);
		transformMapper.get(treeEntity).rotation = random.nextFloat() * 360;
		if (growing) {
			growingMapper.create(treeEntity).timeLeft = /*60f*/10f;
		}

		return treeEntity;
	}

	public int createRock(Point2f position) {
		int rockEntity = terrainPrefabs.get("rock").createEntity();
		subTypeMapper.get(rockEntity).subType = (int) (random.nextFloat() * 3);
		transformMapper.get(rockEntity).position.set(position);
		transformMapper.get(rockEntity).rotation = random.nextFloat() * 360;

		return rockEntity;
	}

	private void createAnimalGroup(Point2f position, String animalName) {
		int groupId = nextAnimalGroupId++;
		createAnimal(animalName + "Adult", new Point2f(position.x - 1f + 2f * random.nextFloat(), position.y - 1f + 2f * random.nextFloat()), groupId);
		int numberOfYoung = 1 + random.nextInt(2);
		for (int i = 0; i < numberOfYoung; i++) {
			createAnimal(animalName + "Young", new Point2f(position.x - 1f + 2f * random.nextFloat(), position.y - 1f + 2f * random.nextFloat()), groupId);
		}
	}

	public int createAnimal(String key, Point2f position, int groupId) {
		int animalEntity = animalPrefabs.get(key).createEntity();
		transformMapper.get(animalEntity).position.set(position);
		groupMapper.get(animalEntity).id = groupId;

		return animalEntity;
	}

	public int createBuilding(String key, Point2f position, float rotation, String owner) {
		int buildingEntity = buildingPrefabs.get(key).createEntity();
		transformMapper.get(buildingEntity).position.set(position);
		transformMapper.get(buildingEntity).rotation = rotation;
		ownerMapper.get(buildingEntity).name = owner;

		return buildingEntity;
	}

	public int createPickup(String key, Point2f position) {
		int pickupEntity = pickupPrefabs.get(key).createEntity();
		transformMapper.get(pickupEntity).position.set(position);
		transformMapper.get(pickupEntity).rotation = random.nextFloat() * 360f;
		return pickupEntity;
	}

	public int createPickup(String key, Point2f position, Inventory items) {
		int pickupEntity = createPickup(key, position);
		inventoryMapper.get(pickupEntity).set(items);
		return pickupEntity;
	}

	private Point2f getRandomPositionInWorld() {
		return new Point2f(-worldConfiguration.width / 2f + random.nextFloat() * worldConfiguration.width, -worldConfiguration.height / 2f + random.nextFloat() * worldConfiguration.height);
	}
}
