package com.vast.interact;

import com.artemis.Aspect;

public interface InteractionHandler {
	void initialize();
	Aspect getAspect1();
	Aspect getAspect2();
	boolean process(int entity1, int entity2);
}