package com.vast.monitor;

import com.artemis.Aspect;
import com.vast.VastWorld;
import com.vast.component.*;
import net.mostlyoriginal.api.utils.QuadTree;

import javax.vecmath.Point2f;
import javax.vecmath.Point2i;
import java.awt.*;
import java.awt.geom.Point2D;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class MonitorWorld {
    private final int SCALE = 5;

    private Map<String, Boolean> debugSettings;

    private Point2i size = new Point2i();
    private Map<Integer, MonitorEntity> monitorEntities;
    private MonitorEntity selectedMonitorEntity;
    private long selectedTime;
    private MonitorEntity hoveredMonitorEntity;
    private List<Rectangle> quadRects;
    private List<MonitorEntity> monitoryEntitiesOnScreen;

    public MonitorWorld(Map<String, Boolean> debugSettings) {
        this.debugSettings = debugSettings;
        monitorEntities = new HashMap<>();
        monitoryEntitiesOnScreen = new ArrayList<>();
    }

    public int getSelectedMonitorEntity() {
        return selectedMonitorEntity != null ? selectedMonitorEntity.entity : -1;
    }

    public void sync(VastWorld vastWorld, Point2D clickPoint, Point2D movePoint) {
        size.set(vastWorld.getWorldConfiguration().width * SCALE, vastWorld.getWorldConfiguration().height * SCALE);

        Set<Integer> entities = Arrays.stream(vastWorld.getEntities(Aspect.all(Transform.class))).boxed().collect(Collectors.toSet());
        monitorEntities.entrySet().removeIf(entry -> !entities.contains(entry.getValue().entity));
        for (int entity : entities) {
            MonitorEntity monitorEntity;
            if (monitorEntities.containsKey(entity)) {
                monitorEntity = monitorEntities.get(entity);
            } else {
                monitorEntity = new MonitorEntity(entity);
                monitorEntities.put(entity, monitorEntity);
            }
            monitorEntity.type = vastWorld.getComponentMapper(com.vast.component.Type.class).get(entity).type;
            Point2f position = vastWorld.getComponentMapper(Transform.class).get(entity).position;
            monitorEntity.position = new Point2i(size.x / 2 + (int) (position.x * SCALE),
                    size.y / 2 - (int) (position.y * SCALE));

            if (vastWorld.getComponentMapper(Collision.class).has(entity)) {
                monitorEntity.collisionRadius = (int) (vastWorld.getComponentMapper(Collision.class).get(entity).radius * SCALE);
            } else {
                monitorEntity.collisionRadius = 0;
            }

            if (vastWorld.getComponentMapper(Player.class).has(entity) && vastWorld.getComponentMapper(Scan.class).has(entity)) {
                monitorEntity.scanDistance = (int) (vastWorld.getComponentMapper(Scan.class).get(entity).distance * SCALE);
            } else {
                monitorEntity.scanDistance = 0;
            }

            if (vastWorld.getComponentMapper(Path.class).has(entity)) {
                Point2f targetPosition = vastWorld.getComponentMapper(Path.class).get(entity).targetPosition;
                monitorEntity.pathPosition = new Point2i(size.x / 2 + (int) (targetPosition.x * SCALE),
                        size.y / 2 - (int) (targetPosition.y * SCALE));
            } else {
                monitorEntity.pathPosition = null;
            }

            if (vastWorld.getComponentMapper(Interact.class).has(entity)) {
                int interactEntity = vastWorld.getComponentMapper(Interact.class).get(entity).entity;
                if (interactEntity >= 0) {
                    Point2f interactPosition = vastWorld.getComponentMapper(Transform.class).get(interactEntity).position;
                    monitorEntity.interactPosition = new Point2i(size.x / 2 + (int) (interactPosition.x * SCALE),
                            size.y / 2 - (int) (interactPosition.y * SCALE));
                }
                else {
                    monitorEntity.interactPosition = null;
                }
            } else {
                monitorEntity.interactPosition = null;
            }

            if (vastWorld.getComponentMapper(Player.class).has(entity)) {
                monitorEntity.name = vastWorld.getComponentMapper(Player.class).get(entity).name;
            } else {
                monitorEntity.name = null;
            }

            if (selectedMonitorEntity != null && vastWorld.getComponentMapper(Player.class).has(selectedMonitorEntity.entity)
                && vastWorld.getComponentMapper(Scan.class).has(selectedMonitorEntity.entity)) {
                Scan scan = vastWorld.getComponentMapper(Scan.class).get(selectedMonitorEntity.entity);
                monitorEntity.colored = scan.nearbyEntities.contains(entity);
            } else {
                monitorEntity.colored = true;
            }
        }

        if (clickPoint != null) {
            MonitorEntity closestMonitorEntity = getMonitorEntityClosestTo(clickPoint);
            if (selectedMonitorEntity != null) {
                if (selectedMonitorEntity.entity == closestMonitorEntity.entity) {
                    // Clicked selected
                    selectedMonitorEntity.components = null;
                    selectedMonitorEntity = null;
                } else {
                    // Clicked other
                    selectedMonitorEntity.components = null;
                    selectedMonitorEntity = closestMonitorEntity;
                    selectedTime = System.currentTimeMillis();
                }
            } else {
                // Clicked first
                selectedMonitorEntity = closestMonitorEntity;
                selectedTime = System.currentTimeMillis();
            }
        } else if (selectedMonitorEntity != null) {
            // Selected entity was removed
            if (!entities.contains(selectedMonitorEntity.entity)) {
                selectedMonitorEntity = null;
            }
        }

        if (movePoint != null) {
            hoveredMonitorEntity = getMonitorEntityClosestTo(movePoint);
        }

        List<Rectangle> newQuadRects = new ArrayList<>();
        addQuadContainers(newQuadRects, vastWorld.getQuadTree());
        quadRects = newQuadRects;
    }

    private void addQuadContainers(List<Rectangle> quadRectsToAddTo, QuadTree quadTree) {
        int x = (int) (quadTree.getBounds().getX() * SCALE);
        int y = (int) -(quadTree.getBounds().getY() * SCALE);
        y -= quadTree.getBounds().getHeight() * SCALE;
        y += size.y;
        quadRectsToAddTo.add(new Rectangle(x, y,
                (int) (quadTree.getBounds().getWidth() * SCALE), (int) (quadTree.getBounds().getHeight() * SCALE)));
        for (QuadTree child : quadTree.getNodes()) {
            if (child != null) {
                addQuadContainers(quadRectsToAddTo, child);
            }
        }
    }

    private MonitorEntity getMonitorEntityClosestTo(Point2D point) {
        MonitorEntity monitorEntityClosestToClick = null;
        float closestDistance = Float.MAX_VALUE;
        for (MonitorEntity monitorEntity : monitorEntities.values()) {
            float dx = Math.abs((float) (monitorEntity.position.x - point.getX()));
            float dy = Math.abs((float) (monitorEntity.position.y - point.getY()));
            float distance = (float) Math.sqrt(dx * dx + dy * dy);
            if (monitorEntityClosestToClick == null || distance < closestDistance) {
                monitorEntityClosestToClick = monitorEntity;
                closestDistance = distance;
            }
        }

        return monitorEntityClosestToClick;
    }

    public Point2i getSize() {
        return size;
    }

    public void paint(Graphics g) {
        if (debugSettings.get("Quad")) {
            g.setColor(new Color(0x3f, 0x3f, 0x3f));
            for (Rectangle rect : quadRects) {
                g.drawRect(rect.x, rect.y, rect.width, rect.height);
            }
        }

        monitoryEntitiesOnScreen.clear();
        for (MonitorEntity monitorEntity : monitorEntities.values()) {
            if (g.getClipBounds().contains(monitorEntity.position.x, monitorEntity.position.y)) {
                monitoryEntitiesOnScreen.add(monitorEntity);
            }
        }

        for (MonitorEntity monitorEntity : monitoryEntitiesOnScreen) {
            if (monitorEntity == hoveredMonitorEntity && monitorEntity != selectedMonitorEntity) {
                g.setColor(Color.DARK_GRAY);
                g.drawArc(monitorEntity.position.x - 15, monitorEntity.position.y - 15, 30, 30, 0, 360);
            }
        }

        for (MonitorEntity monitorEntity : monitoryEntitiesOnScreen) {
            monitorEntity.paint(g);
        }

        if (selectedMonitorEntity != null) {
            int size;
            float secondsSinceSelected = (System.currentTimeMillis() - selectedTime) / 1000f;
            if (secondsSinceSelected < 0.2f) {
                size = 30 + (int) ((0.2f - secondsSinceSelected) * 40);
            } else {
                size = 30;
            }

            g.setColor(Color.WHITE);
            g.drawArc(selectedMonitorEntity.position.x - size / 2, selectedMonitorEntity.position.y - size / 2, size, size, 0, 360);
        }

        for (MonitorEntity monitorEntity : monitoryEntitiesOnScreen) {
            monitorEntity.paintDebug(g, debugSettings);
        }
    }
}
