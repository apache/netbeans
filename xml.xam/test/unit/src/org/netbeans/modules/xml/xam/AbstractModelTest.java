/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
