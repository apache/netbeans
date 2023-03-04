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
package org.netbeans.modules.xml.wsdl.model.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.ArrayList;
import java.util.List;
import org.netbeans.junit.NbTestCase;
import org.netbeans.modules.xml.wsdl.model.Definitions;
import org.netbeans.modules.xml.wsdl.model.Message;
import org.netbeans.modules.xml.wsdl.model.NotificationOperation;
import org.netbeans.modules.xml.wsdl.model.OneWayOperation;
import org.netbeans.modules.xml.wsdl.model.Operation;
import org.netbeans.modules.xml.wsdl.model.Part;
import org.netbeans.modules.xml.wsdl.model.ReferenceableWSDLComponent;
import org.netbeans.modules.xml.wsdl.model.RequestResponseOperation;
import org.netbeans.modules.xml.wsdl.model.SolicitResponseOperation;
import org.netbeans.modules.xml.wsdl.model.TestCatalogModel;
import org.netbeans.modules.xml.wsdl.model.WSDLModel;
import org.netbeans.modules.xml.wsdl.model.Util;
import org.netbeans.modules.xml.wsdl.model.visitor.FindReferencedVisitor;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;

/**
 *
 * @author Nam Nguyen
 */
public class UndoRedoTest extends NbTestCase {
    private TestComponentListener listener;
    private TestPropertyListener plistener;
    
    public UndoRedoTest(String testName) {
        super(testName);
    }
    
    static class TestPropertyListener implements PropertyChangeListener {
        ArrayList<PropertyChangeEvent> events  = new ArrayList<PropertyChangeEvent>();
        public void propertyChange(PropertyChangeEvent evt) {
            events.add(evt);
        }
        
        public void assertNoEvents(String propertyName) {
            for (PropertyChangeEvent e : events) {
                if (propertyName.equals(e.getPropertyName())) {
                    assertTrue("Expect no property change events "+propertyName, false);
                }
            }
            return; //matched
        }
        
        public void assertEvent(String propertyName, Object source) {
            for (PropertyChangeEvent e : events) {
                if (propertyName.equals(e.getPropertyName()) && e.getSource() == source) {
                    return; //matched
                }
            }
            assertTrue("Expect property change event "+propertyName, false);
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

        public void reset() { events.clear(); }
    }
    
    public static class TestComponentListener implements ComponentListener {
        ArrayList<ComponentEvent> events = new ArrayList<ComponentEvent>();
        
        public void valueChanged(ComponentEvent evt) {
            events.add(evt);
        }
        public void childrenDeleted(ComponentEvent evt) {
            events.add(evt);
        }
        public void childrenAdded(ComponentEvent evt) {
            events.add(evt);
        }
        
        public void assertChangeEvent(Object source) {
            for (ComponentEvent e : events) {
                if (e.getEventType() == ComponentEvent.EventType.VALUE_CHANGED &&
                    e.getSource() == source) {
                    return;
                }
            }
            assertFalse("Failed to receive ATTRIBUTE event on " + source, true);
        }
        
        public void assertChildAddedEvent(Object source) {
            for (ComponentEvent e : events) {
                if (e.getEventType() == ComponentEvent.EventType.CHILD_ADDED &&
                        e.getSource() == source) {
                    return;
                }
            }
            assertFalse("Failed to receive CHILD_ADDED event on " + source, true);
        }
        
        public void assertChildRemovedEvent(Object source) {
            for (ComponentEvent e : events) {
                if (e.getEventType() == ComponentEvent.EventType.CHILD_REMOVED &&
                        e.getSource() == source) {
                    return;
                }
            }
            assertFalse("Failed to receive CHILD_REMOVED event on " + source, true);
        }
        
        public void assertEventCount(int count) {
            assertEquals("Event count", count, events.size());
        }
        
        public void reset() { events.clear(); }
        
    }
    
    protected void setUp() throws Exception {
    }
    
    private void setup(WSDLModel m) {
        listener = new TestComponentListener();
        plistener = new TestPropertyListener();
        m.addComponentListener(listener);
        m.addPropertyChangeListener(plistener);
    }
    
    protected void tearDown() throws Exception {
        if (listener != null) listener.reset();
        if (plistener != null) plistener.reset();
        TestCatalogModel.getDefault().clearDocumentPool();
    }
    
    public <T extends ReferenceableWSDLComponent> T find(WSDLModel model, String name, Class<T> type) {
        return new FindReferencedVisitor<T>(model.getDefinitions()).find(name, type);
    }

    public void testRenameMessage() throws Exception {
        WSDLModel model = Util.loadWSDLModel("resources/testOrderingUndoRedo.wsdl");
        setup(model);
     
        Definitions d = model.getDefinitions();
        List<Message> messages = new ArrayList<Message>(d.getMessages());
        assertEquals("newWSDLOperationRequest", messages.get(0).getName());
        assertEquals("newWSDLOperationReply", messages.get(1).getName());
        assertEquals("ItineraryFault", messages.get(2).getName());
     
        Util.setDocumentContentTo(model, "resources/testOrderingUndoRedo_2.wsdl");
        model.sync();
     
        messages = new ArrayList<Message>(d.getMessages());
        assertEquals("newWSDLOperationRequest", messages.get(0).getName());
        assertEquals("reply1", messages.get(1).getName());
        assertEquals("ItineraryFault", messages.get(2).getName());

        Util.setDocumentContentTo(model, "resources/testOrderingUndoRedo.wsdl");
        model.sync();
     
        messages = new ArrayList<Message>(d.getMessages());
        assertEquals("newWSDLOperationRequest", messages.get(0).getName());
        assertEquals("newWSDLOperationReply", messages.get(1).getName());
        assertEquals("ItineraryFault", messages.get(2).getName());
    }

    public void testDeleteMessagePart() throws Exception {
        WSDLModel model = Util.loadWSDLModel("resources/testOrderingUndoRedo.wsdl");
        setup(model);
     
        Definitions d = model.getDefinitions();
        Message m = d.getModel().findComponentByName("newWSDLOperationRequest", Message.class);
        assertEquals("part1", new ArrayList<Part>(m.getParts()).get(0).getName());
        assertEquals("part2", new ArrayList<Part>(m.getParts()).get(1).getName());
        assertEquals("part3", new ArrayList<Part>(m.getParts()).get(2).getName());
     
        Util.setDocumentContentTo(model, "resources/testOrderingUndoRedo_1.wsdl");
        model.sync();
     
        assertEquals("part1", new ArrayList<Part>(m.getParts()).get(0).getName());
        assertEquals("part3", new ArrayList<Part>(m.getParts()).get(1).getName());

        Util.setDocumentContentTo(model, "resources/testOrderingUndoRedo.wsdl");
        model.sync();
     
        assertEquals("part1", new ArrayList<Part>(m.getParts()).get(0).getName());
        assertEquals("part2", new ArrayList<Part>(m.getParts()).get(1).getName());
        assertEquals("part3", new ArrayList<Part>(m.getParts()).get(2).getName());
    }

}
