package com.vast.order.handler.observer;

import com.artemis.ComponentMapper;
import com.artemis.World;
import com.vast.component.*;
import com.vast.data.Recipe;
import com.vast.data.Recipes;
import com.vast.network.Properties;
import com.vast.order.handler.AbstractOrderHandler;
import com.vast.order.request.observer.BuildOrderRequest;
import com.vast.order.request.observer.BuildStartOrderRequest;
import com.vast.order.request.OrderRequest;
import com.vast.system.CreationManager;

import javax.vecmath.Point2f;

public class BuildOrderHandler extends AbstractOrderHandler<BuildOrderRequest> {
	private World world;

	private ComponentMapper<Create> createMapper;
	private ComponentMapper<Owner> ownerMapper;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Interact> interactMapper;
	private ComponentMapper<Path> pathMapper;
	private ComponentMapper<Inventory> inventoryMapper;
	private ComponentMapper<Sync> syncMapper;
	private ComponentMapper<Event> eventMapper;
	private ComponentMapper<State> stateMapper;
	private ComponentMapper<Build> buildMapper;
	private ComponentMapper<Delete> deleteMapper;
	private ComponentMapper<Placeholder> placeholderMapper;

	private Recipes recipes;

	private CreationManager creationManager;

	public BuildOrderHandler(Recipes recipes) {
		this.recipes = recipes;
	}

	@Override
	public void initialize() {
		creationManager = world.getSystem(CreationManager.class);
	}

	@Override
	public boolean handlesRequest(OrderRequest request) {
		return request instanceof BuildOrderRequest;
	}

	@Override
	public boolean isOrderComplete(int observerEntity) {
		return !buildMapper.has(observerEntity);
	}

	@Override
	public void cancelOrder(int observerEntity) {
		if (buildMapper.has(observerEntity)) {
			deleteMapper.create(buildMapper.get(observerEntity).placeholderEntity).reason = "canceled";
			buildMapper.remove(observerEntity);
		}
	}

	@Override
	public boolean startOrder(int observerEntity, BuildOrderRequest buildOrderRequest) {
		if (buildOrderRequest instanceof BuildStartOrderRequest) {
			BuildStartOrderRequest buildStartOrderRequest = (BuildStartOrderRequest) buildOrderRequest;

			Recipe recipe = recipes.getRecipe(buildStartOrderRequest.getRecipeId());
			Transform orderTransform = transformMapper.get(observerEntity);

			Point2f buildPosition = new Point2f(orderTransform.position.x, orderTransform.position.y + 3f);
			int buildingPlaceholderEntity = creationManager.createBuildingPlaceholder(recipe.getEntityType(), buildPosition);
			stateMapper.get(buildingPlaceholderEntity).name = "placeholder";

			Build build = buildMapper.create(observerEntity);
			build.placeholderEntity = buildingPlaceholderEntity;
			build.recipe = recipe;

			syncMapper.create(buildingPlaceholderEntity).markPropertyAsDirty(Properties.VALID);

			return true;
		} else {
			return false;
		}
	}

	@Override
	public boolean modifyOrder(int observerEntity, BuildOrderRequest buildOrderRequest) {
//		if (buildOrderRequest instanceof BuildMoveOrderRequest) {
//			BuildMoveOrderRequest buildMoveOrderRequest = (BuildMoveOrderRequest) buildOrderRequest;
//
//			Build build = buildMapper.get(orderEntity);
//			Transform buildingPlaceholderTransform = transformMapper.get(build.placeholderEntity);
//			switch (buildMoveOrderRequest.getDirection()) {
//				case 0:
//					buildingPlaceholderTransform.position.y += 0.5f;
//					break;
//				case 1:
//					buildingPlaceholderTransform.position.x += 0.5f;
//					break;
//				case 2:
//					buildingPlaceholderTransform.position.y -= 0.5f;
//					break;
//				case 3:
//					buildingPlaceholderTransform.position.x -= 0.5f;
//					break;
//			}
//
//			syncMapper.create(build.placeholderEntity).markPropertyAsDirty(Properties.POSITION);
//			syncMapper.create(build.placeholderEntity).markPropertyAsDirty(Properties.VALID);
//
//			return true;
//		} else if (messageCode == MessageCodes.BUILD_ROTATE) {
//			int direction = (byte) dataObject.get(MessageCodes.BUILD_ROTATE_DIRECTION).value;
//			Build build = buildMapper.get(orderEntity);
//			Transform buildingPlaceholderTransform = transformMapper.get(build.placeholderEntity);
//			switch (direction) {
//				case 0:
//					buildingPlaceholderTransform.rotation += 10f;
//					if (buildingPlaceholderTransform.rotation >= 360f) {
//						buildingPlaceholderTransform.rotation -= 360f;
//					}
//					break;
//				case 1:
//					buildingPlaceholderTransform.rotation -= 10f;
//					if (buildingPlaceholderTransform.rotation < 0f) {
//						buildingPlaceholderTransform.rotation += 360f;
//					}
//					break;
//			}
//
//			syncMapper.create(build.placeholderEntity).markPropertyAsDirty(Properties.ROTATION);
//
//			return true;
//		} else if (messageCode == MessageCodes.BUILD_CONFIRM) {
//			Build build = buildMapper.get(orderEntity);
//			Placeholder placeholder = placeholderMapper.get(build.placeholderEntity);
//			if (placeholder.valid) {
//				Inventory inventory = inventoryMapper.get(orderEntity);
//				if (inventory.has(build.recipe.getCosts())) {
//					inventory.remove(build.recipe.getCosts());
//					syncMapper.create(orderEntity).markPropertyAsDirty(Properties.INVENTORY);
//
//					Transform placeholderTransform = transformMapper.get(build.placeholderEntity);
//
//					int buildingEntity = creationManager.createBuilding(build.recipe.getEntityType(),
//							placeholderTransform.position, placeholderTransform.rotation, playerMapper.get(orderEntity).name);
//					stateMapper.get(buildingEntity).name = "placed";
//					createMapper.create(buildingEntity).reason = "built";
//
//					interactMapper.create(orderEntity).entity = buildingEntity;
//
//					deleteMapper.create(build.placeholderEntity);
//					buildMapper.remove(orderEntity);
//
//					return true;
//				} else {
//					eventMapper.create(orderEntity).addEntry("message").setData("I don't have the required materials...").setOwnerPropagation();
//					return false;
//				}
//			} else {
//				eventMapper.create(orderEntity).addEntry("message").setData("I can't build there...").setOwnerPropagation();
//				return false;
//			}
//		} else if (messageCode == MessageCodes.BUILD_CANCEL) {
//			return false;
//		} else {
//			return false;
//		}

		return false;
	}
}