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
/*
 * DefinitionsTest.java
 * JUnit based test
 *
 * Created on May 2, 2006, 6:01 PM
 */

package org.netbeans.modules.xml.wsdl.model;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import javax.swing.undo.UndoManager;
import junit.framework.*;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.wsdl.model.impl.WSDLModelImpl;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;

/**
 *
 * @author nn136682
 */
public class DefinitionsTest extends TestCase {

    public DefinitionsTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }

    protected void tearDown() throws Exception {
        TestCatalogModel.getDefault().clearDocumentPool();
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(DefinitionsTest.class);
        
        return suite;
    }

    static class PropertyListener implements PropertyChangeListener {
        List<PropertyChangeEvent> events  = new ArrayList<PropertyChangeEvent>();
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
        ArrayList<ComponentEvent> accu = new ArrayList<ComponentEvent>();
        public void valueChanged(ComponentEvent evt) {
            accu.add(evt);
        }
        public void childrenAdded(ComponentEvent evt) {
            accu.add(evt);
        }
        public void childrenDeleted(ComponentEvent evt) {
            accu.add(evt);
        }
        public void reset() { accu.clear(); }
        public int getEventCount() { return accu.size(); }
        public java.util.List<ComponentEvent> getEvents() { return accu; }
    
        private void assertEvent(ComponentEvent.EventType type, DocumentComponent source) {
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
    
    public void testSetTypes() throws Exception {
        UndoManager um = new UndoManager();
        WSDLModel model = TestCatalogModel.getDefault().getWSDLModel(NamespaceLocation.ECHOCONCAT);
        model.addUndoableEditListener(um);
        TestComponentListener cl = new TestComponentListener();
        PropertyListener pl = new PropertyListener();
        model.addComponentListener(cl);
        model.addPropertyChangeListener(pl);
        
        Definitions d = model.getDefinitions();
        int childCount = d.getChildren().size();
        Types types = d.getTypes();
        assertNotNull(types);
        model.startTransaction();
        d.setTypes(null);
        model.endTransaction();
        
        cl.assertEvent(ComponentEvent.EventType.CHILD_REMOVED, d);
        pl.assertEvent(Definitions.TYPES_PROPERTY, types, null);

        
        um.undo();
        assertEquals(childCount, d.getChildren().size());
        um.redo();
        assertEquals(childCount-1, d.getChildren().size());
    }

    public void testEmbeddedSchemaUsingNamspacesFromDefinitions() throws Exception {
        WSDLModelImpl model = (WSDLModelImpl)Util.loadWSDLModel("resources/schemaUsingNamespaceFromWsdlRoot.wsdl");
        Schema schema = model.getDefinitions().getTypes().getSchemas().iterator().next();
        List<GlobalElement> elements = new ArrayList<GlobalElement>(schema.getElements());
        assertEquals(2, elements.size());
        assertEquals("someType", elements.get(0).getType().get().getName());
        assertEquals("PurchaseOrderType", elements.get(1).getType().get().getName());
    }
}
