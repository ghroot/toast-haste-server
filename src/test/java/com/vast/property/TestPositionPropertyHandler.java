package com.vast.property;

import com.artemis.World;
import com.artemis.WorldConfigurationBuilder;
import com.nhnent.haste.protocol.data.DataObject;
import com.vast.component.SyncHistory;
import com.vast.component.Transform;
import com.vast.network.Properties;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import javax.vecmath.Point2f;

public class TestPositionPropertyHandler {
    private PositionPropertyHandler positionPropertyHandler;
    private World world;
    private int entity;

    @Before
    public void setUp() {
        positionPropertyHandler = new PositionPropertyHandler(1f);

        world = new World(new WorldConfigurationBuilder().build());
        world.inject(positionPropertyHandler);

        entity = world.create();
    }

    @Test
    public void handlesCorrectProperty() {
        Assert.assertEquals(Properties.POSITION, positionPropertyHandler.getProperty());
    }

    @Test
    public void givenEmptyEntity_doesNotDecoratesDataObject() {
        DataObject dataObject = new DataObject();
        boolean decorated = positionPropertyHandler.decorateDataObject(entity, dataObject, true);

        Assert.assertFalse(decorated);
        Assert.assertNull(dataObject.get(Properties.POSITION));
    }

    @Test
    public void whenForced_decoratesDataObject() {
        world.getMapper(Transform.class).create(entity);

        DataObject dataObject = new DataObject();
        boolean decorated = positionPropertyHandler.decorateDataObject(entity, dataObject, true);

        Assert.assertTrue(decorated);
        Assert.assertArrayEquals(new double[] {0f, 0f}, (double[]) dataObject.get(Properties.POSITION).value, 0.001);
    }

    @Test
    public void givenNoPositionChange_doesNotDecorateDataObject() {
        Transform transform = world.getMapper(Transform.class).create(entity);
        SyncHistory syncHistory = world.getMapper(SyncHistory.class).create(entity);

        syncHistory.syncedValues.put(Properties.POSITION, new Point2f(transform.position));

        DataObject dataObject = new DataObject();
        boolean decorated = positionPropertyHandler.decorateDataObject(entity, dataObject, false);

        Assert.assertFalse(decorated);
        Assert.assertNull(dataObject.get(Properties.POSITION));
    }

    @Test
    public void givenPositionChange_decoratesDataObject() {
        Transform transform = world.getMapper(Transform.class).create(entity);
        SyncHistory syncHistory = world.getMapper(SyncHistory.class).create(entity);

        // Moved from 0,0 -> 2,0
        transform.position.set(2f, 0f);
        syncHistory.syncedValues.put(Properties.POSITION, new Point2f());

        DataObject dataObject = new DataObject();
        boolean decorated = positionPropertyHandler.decorateDataObject(entity, dataObject, false);

        Assert.assertTrue(decorated);
        Assert.assertArrayEquals(new double[] {2, 0}, (double[]) dataObject.get(Properties.POSITION).value, 0.001);
        Assert.assertEquals(transform.position, syncHistory.syncedValues.get(Properties.POSITION));
    }

    @Test
    public void givenNoSyncHistory_decoratesDataObject() {
        world.getMapper(Transform.class).create(entity);

        DataObject dataObject = new DataObject();
        boolean decorated = positionPropertyHandler.decorateDataObject(entity, dataObject, false);

        Assert.assertTrue(decorated);
        Assert.assertArrayEquals(new double[] {0, 0}, (double[]) dataObject.get(Properties.POSITION).value, 0.001);
    }

    @Test
    public void givenEmptySyncHistory_whenForced_populatesSyncHistory() {
        world.getMapper(Transform.class).create(entity);
        SyncHistory syncHistory = world.getMapper(SyncHistory.class).create(entity);

        positionPropertyHandler.decorateDataObject(entity, new DataObject(), true);

        Assert.assertNotNull(syncHistory.syncedValues.get(Properties.POSITION));
    }
}
