package com.vast.data;

import org.json.JSONObject;

import java.util.Map;
import java.util.Set;

public class Building {
	private int id;
	private String name;
	private Set<Cost> costs;
	private Map<String, JSONObject> aspects;

	public Building(int id, String name, Set<Cost> costs, Map<String, JSONObject> aspects) {
		this.id = id;
		this.name = name;
		this.costs = costs;
		this.aspects = aspects;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public Set<Cost> getCosts() {
		return costs;
	}

	public boolean hasAspect(String name) {
		return aspects.containsKey(name);
	}

	public JSONObject getAspect(String name) {
		return aspects.get(name);
	}
}
