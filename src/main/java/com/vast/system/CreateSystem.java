package com.vast.system;

import com.artemis.Aspect;
import com.artemis.ComponentMapper;
import com.artemis.annotations.Profile;
import com.artemis.systems.IteratingSystem;
import com.artemis.utils.IntBag;
import com.nhnent.haste.framework.SendOptions;
import com.nhnent.haste.protocol.data.DataObject;
import com.nhnent.haste.protocol.messages.EventMessage;
import com.vast.MessageCodes;
import com.vast.Profiler;
import com.vast.VastPeer;
import com.vast.component.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Set;

@Profile(enabled = true, using = Profiler.class)
public class CreateSystem extends IteratingSystem {
	private static final Logger logger = LoggerFactory.getLogger(CreateSystem.class);

	private ComponentMapper<Create> createMapper;
	private ComponentMapper<Scan> scanMapper;
	private ComponentMapper<Player> playerMapper;
	private ComponentMapper<Known> knownMapper;
	private ComponentMapper<Type> typeMapper;
	private ComponentMapper<Transform> transformMapper;
	private ComponentMapper<Interactable> interactableMapper;

	private Map<String, VastPeer> peers;

	private float[] reusablePosition;
	private EventMessage reusableEventMessage;

	public CreateSystem(Map<String, VastPeer> peers) {
		super(Aspect.all(Create.class, Transform.class, Type.class));
		this.peers = peers;

		reusablePosition = new float[2];
		reusableEventMessage = new EventMessage(MessageCodes.ENTITY_CREATED, new DataObject());
	}

	@Override
	protected void process(int createEntity) {
		IntBag activePlayerEntities = world.getAspectSubscriptionManager().get(Aspect.all(Player.class, Active.class, Known.class)).getEntities();
		for (int i = 0; i < activePlayerEntities.size(); i++) {
			int activePlayerEntity = activePlayerEntities.get(i);
			Set<Integer> knownEntities = knownMapper.get(activePlayerEntity).knownEntities;
			if (!knownEntities.contains(createEntity)) {
				VastPeer peer = peers.get(playerMapper.get(activePlayerEntity).name);
				String reason = createMapper.get(createEntity).reason;
				logger.debug("Notifying peer {} about new entity {} ({})", peer.getName(), createEntity, reason);
				Type type = typeMapper.get(createEntity);
				Transform transform = transformMapper.get(createEntity);
				reusablePosition[0] = transform.position.x;
				reusablePosition[1] = transform.position.y;
				reusableEventMessage.getDataObject().set(MessageCodes.ENTITY_CREATED_ENTITY_ID, createEntity);
				reusableEventMessage.getDataObject().set(MessageCodes.ENTITY_CREATED_TYPE, type.type);
				reusableEventMessage.getDataObject().set(MessageCodes.ENTITY_CREATED_POSITION, reusablePosition);
				reusableEventMessage.getDataObject().set(MessageCodes.ENTITY_CREATED_REASON, reason);
				reusableEventMessage.getDataObject().set(MessageCodes.ENTITY_CREATED_INTERACTABLE, interactableMapper.has(createEntity));
				peer.send(reusableEventMessage, SendOptions.ReliableSend);
				knownEntities.add(createEntity);
			}
		}
		createMapper.remove(createEntity);
	}
}
