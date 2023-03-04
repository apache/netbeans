/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.xml.xam;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import junit.framework.*;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.modules.xml.xam.spi.ModelAccessProvider;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;

/**
 * Implements ModelAccessProvider in order to start auto synchronization (for issue #184306).
 * @author Nam Nguyen
 */
@org.openide.util.lookup.ServiceProviders({@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.xml.xam.spi.ModelAccessProvider.class)})
public class AbstractModelTest extends TestCase implements ModelAccessProvider {
    PropertyListener plistener;
    TestComponentListener listener;
    TestModel model;
    TestComponent root;

    static class PropertyListener implements PropertyChangeListener {
        List<PropertyChangeEvent> events  = new ArrayList<PropertyChangeEvent>();
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            events.add(evt);
        }
        
        public void assertEvent(String propertyName, Object old, Object now) {
            for (PropertyChangeEvent e : events) {
                if (propertyName.equals(e.getPropertyName())) {
                    if (old != null && ! old.equals(e.getOldValue()) ||
                        old == null && e.getOldValue() != null) {
                        continue;
                    }
                    if (now != null && ! now.equals(e.getNewValue()) ||
                        now == null && e.getNewValue() != null) {
                        continue;
                    }
                    return; //matched
                }
            }
            assertTrue("Expect property change event on "+propertyName+" with "+old+" and "+now, false);
        }
    }
    
    class TestComponentListener implements ComponentListener {
        List<ComponentEvent> accu = new ArrayList<ComponentEvent>();
        @Override
        public void valueChanged(ComponentEvent evt) {
            accu.add(evt);
        }
        @Override
        public void childrenAdded(ComponentEvent evt) {
            accu.add(evt);
        }
        @Override
        public void childrenDeleted(ComponentEvent evt) {
            accu.add(evt);
        }
        public void reset() { accu.clear(); }
        public int getEventCount() { return accu.size(); }
        public List<ComponentEvent> getEvents() { return accu; }
    
        private void assertEvent(ComponentEvent.EventType type, Component source) {
            for (ComponentEvent e : accu) {
                if (e.getEventType().equals(type) &&
                    e.getSource() == source) {
                    return;
                }
            }
            assertTrue("Expect component change event " + type +" on source " + source +
                    ". Instead received: " + accu, false);
        }
    }    

    public AbstractModelTest() {}

    public AbstractModelTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        model = new TestModel();
        listener = new TestComponentListener();
        plistener = new PropertyListener();
        model.addComponentListener(listener);
        model.addPropertyChangeListener(plistener);
    }

    @Override
    protected void tearDown() throws Exception {
        model.removePropertyChangeListener(plistener);
        model.removeComponentListener(listener);
    }

    public static Test suite() {
        return new TestSuite(AbstractModelTest.class);
    }
    
    public void testWeakListenerRemoval() throws Exception {
        TestModel _model = new TestModel();
        TestComponent _root = _model.getRootComponent();
        TestComponentListener listener1 = new TestComponentListener();
        TestComponentListener listener2 = new TestComponentListener();
        TestComponentListener listener3 = new TestComponentListener();
        TestComponentListener listener4 = new TestComponentListener();
        TestComponentListener listener5 = new TestComponentListener();
        _model.addComponentListener((ComponentListener)WeakListeners.create(ComponentListener.class, listener1, _model));
        _model.addComponentListener((ComponentListener)WeakListeners.create(ComponentListener.class, listener2, _model));
        _model.addComponentListener((ComponentListener)WeakListeners.create(ComponentListener.class, listener3, _model));
        _model.addComponentListener((ComponentListener)WeakListeners.create(ComponentListener.class, listener4, _model));
        _model.addComponentListener((ComponentListener)WeakListeners.create(ComponentListener.class, listener5, _model));
        
        _model.startTransaction();
        _model.addChildComponent(_root, new TestComponent.B(_model, 1), -1);
        _model.endTransaction();
        
        listener1.assertEvent(ComponentEvent.EventType.CHILD_ADDED, _root);
        listener2.assertEvent(ComponentEvent.EventType.CHILD_ADDED, _root);
        listener3.assertEvent(ComponentEvent.EventType.CHILD_ADDED, _root);
        listener4.assertEvent(ComponentEvent.EventType.CHILD_ADDED, _root);
        listener5.assertEvent(ComponentEvent.EventType.CHILD_ADDED, _root);

        listener2 = null;
        listener3 = null;
        listener4 = null;
        listener5 = null;
        System.gc();
        Thread.sleep(50);
     
        assertEquals(1, _model.getComponentListenerList().getListenerCount());
    }
    
    public void testStateChangeEvent() throws Exception {
        model.startTransaction();
        model.setState(Model.State.NOT_WELL_FORMED);
        model.endTransaction();
        plistener.assertEvent(Model.STATE_PROPERTY, Model.State.VALID, Model.State.NOT_WELL_FORMED);
    }

    private class FlushListener implements PropertyChangeListener {
        long flushTime = 0;
        public FlushListener() {
            model.getAccess().addFlushListener(this);
        }
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            if (evt.getSource() == model.getAccess() && evt.getPropertyName().equals("flushed")) {
                flushTime = ((Long)evt.getNewValue()).longValue();
            }
        }
        public void assertFlushEvent(long since) {
            assertTrue("Expect flush event after "+since, flushTime >= since);
        }
        public void assertNoFlushEvents(long since) {
            assertTrue("Expect no flush events after "+since, flushTime < since);
        }
    }
    
    public void testReadOnlyTransactionSkipFlush() throws Exception {
        FlushListener list = new FlushListener();
        long since = System.currentTimeMillis();
        model.startTransaction();
        model.endTransaction();
        list.assertNoFlushEvents(since);
    }
    
    public void testWriteTransactionDidFlush() throws Exception {
        FlushListener list = new FlushListener();
        long since = System.currentTimeMillis();
        model.startTransaction();
        model.getRootComponent().setValue("newValue");
        model.endTransaction();
        list.assertFlushEvent(since);
    }
    
    public void testModelFactoryListener() throws Exception {
        TestModel2.factory().addPropertyChangeListener(plistener);
        TestModel2 m = TestModel2.factory().getModel(Util.createModelSource(
                "resources/test1.xml"));
        plistener.assertEvent(TestModel2.Factory.MODEL_LOADED_PROPERTY, null, m);
    }

    // Tests issue #184306
    public void testModelWithoutDocument() throws Exception {
        File file = new File("test_not_existing.xml");
        // Construct a model source without Document.
        ModelSource ms = new ModelSource(Lookups.fixed(file, (ModelAccessProvider)this), true);
        //
        TestModel2 model2 = new TestModel2(ms);
        model2.sync();
        assert model2.getState() == Model.State.NOT_SYNCED;
        //
        TestModel2.Factory modelFactory = TestModel2.factory();
        model2 = modelFactory.createFreshModel(ms);
        assert model2.getState() == Model.State.NOT_SYNCED;
        //
        model2 = modelFactory.getModel(ms);
        assert model2.getState() == Model.State.NOT_SYNCED;
        //
        model2.getAccess().setDirty();
        Thread.sleep(5000);
        //
        assert model2.getState() == Model.State.NOT_SYNCED;
    }

    @Override
    public Object getModelSourceKey(ModelSource source) {
        return source.getLookup().lookup(File.class);
    }
}
