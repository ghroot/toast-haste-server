package com.vast.data;

import com.artemis.BaseSystem;
import com.nhnent.haste.transport.QoS;

import java.util.HashMap;
import java.util.Map;

public class Metrics {
	private int timePerFrameMs;
	private Map<String, SystemMetrics> systemMetrics = new HashMap<>();
	private int numberOfCollisionChecks;
	private double meanOfRoundTripTime;
	private Map<Short, Map<QoS, Integer>> sentMessages = new HashMap<Short, Map<QoS, Integer>>();
	private long bytesSent;
	private long lastSerializeTime;
	private Map<Byte, Integer> syncedProperties = new HashMap<Byte, Integer>();

	public int getTimePerFrameMs() {
		return timePerFrameMs;
	}

	public void setTimePerFrameMs(int timePerFrameMs) {
		this.timePerFrameMs = timePerFrameMs;
	}

	public int getFps() {
		if (timePerFrameMs > 0){
			return 1000 / timePerFrameMs;
		} else {
			return 0;
		}
	}

	public void setSystemMetrics(BaseSystem system, int processingTime, int numberOfEntitiesInSystem) {
		systemMetrics.put(system.getClass().getSimpleName(), new SystemMetrics(processingTime, numberOfEntitiesInSystem));
	}

	public Map<String, SystemMetrics> getSystemMetrics() {
		return systemMetrics;
	}

	public void setNumberOfCollisionChecks(int numberOfCollisionChecks) {
		this.numberOfCollisionChecks = numberOfCollisionChecks;
	}

	public int getNumberOfCollisionChecks() {
		return numberOfCollisionChecks;
	}

	public void setRoundTripTime(double meanOfRoundTripTime) {
		this.meanOfRoundTripTime = meanOfRoundTripTime;
	}

	public double getMeanOfRoundTripTime() {
		return meanOfRoundTripTime;
	}

	public void messageSent(short messageCode, QoS qos) {
		Map<QoS, Integer> sentMessagesWithCode = sentMessages.get(messageCode);
		if (sentMessagesWithCode == null) {
			sentMessagesWithCode = new HashMap<QoS, Integer>();
			sentMessages.put(messageCode, sentMessagesWithCode);
		}
		if (sentMessagesWithCode.containsKey(qos)) {
			sentMessagesWithCode.put(qos, sentMessagesWithCode.get(qos) + 1);
		} else {
			sentMessagesWithCode.put(qos, 1);
		}
	}

	public Map<Short, Map<QoS, Integer>> getSentMessages() {
		return sentMessages;
	}

	public void bytesSent(int size) {
		bytesSent += size;
	}

	public long getBytesSent() {
		return bytesSent;
	}

	public int getTimeSinceLastSerialization() {
		return (int) (System.currentTimeMillis() - lastSerializeTime);
	}

	public void setLastSerializeTime(long lastSerializeTime) {
		this.lastSerializeTime = lastSerializeTime;
	}

	public void incrementSyncedProperty(byte property) {
		if (syncedProperties.containsKey(property)) {
			syncedProperties.put(property, syncedProperties.get(property) + 1);
		} else {
			syncedProperties.put(property, 1);
		}
	}

	public Map<Byte, Integer> getSyncedProperties() {
		return syncedProperties;
	}
}
