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
 * MergeTest.java
 * JUnit based test
 *
 * Created on October 28, 2005, 3:40 PM
 */

package org.netbeans.modules.xml.schema.model.impl.xdm;

import java.util.ArrayList;
import javax.swing.event.UndoableEditEvent;
import javax.swing.event.UndoableEditListener;
import javax.swing.undo.CompoundEdit;
import javax.swing.undo.UndoManager;
import junit.framework.*;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.xml.schema.model.*;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;

/**
 *
 * @author Ayub Khan
 */
public class UndoRedoTest extends TestCase {
    
    public UndoRedoTest(String testName) {
        super(testName);
    }
    @Override
            protected void setUp() throws Exception {
        
    }
    
    @Override
            protected void tearDown() {
        TestCatalogModel.getDefault().clearDocumentPool();
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
    
    public void testIssue83963() throws Exception {
        SchemaModel model = Util.loadSchemaModel("resources/undoredo.xsd");
        BaseDocument doc = (BaseDocument) model.getModelSource().
                getLookup().lookup(BaseDocument.class);
        Schema s = model.getSchema();
        TestComponentListener listener = new TestComponentListener();
        model.addComponentListener(listener);
        UndoManager ur = new UndoManager();
        model.addUndoableEditListener(ur);
        
        String original = doc.getText(0, doc.getLength());
        //System.out.println("doc before add ComplexType"+doc.getText(0, doc.getLength()));
        GlobalComplexType gct = model.getFactory().createGlobalComplexType();
        model.startTransaction();
        s.addComplexType(gct);
        model.endTransaction();
        model.removeUndoableEditListener(ur);
        doc.addUndoableEditListener(ur);
        
        //System.out.println("doc after add ComplexType"+doc.getText(0, doc.getLength()));
        
        String stStr = "   <xsd:simpleType name=\"lend\">\n     <xsd:list>\n       <xsd:simpleType>\n         <xsd:restriction base=\"xsd:string\"/>\n       </xsd:simpleType>\n     </xsd:list>\n   </xsd:simpleType>";
        
        String afterInsert = doc.getText(0, doc.getLength());
        //System.out.println("doc after insert simpleType"+doc.getText(290, 10));
        // position was changing which is weird but doesn't matter for undo-redo testing
        int schemaTagPosition = afterInsert.length() - 10;
        doc.insertString(schemaTagPosition, "\n", null);
        model.sync();
        doc.insertString(schemaTagPosition + 1, stStr, null);
        model.sync();
        
        //System.out.println("doc after insert simpleType"+doc.getText(0, doc.getLength()));
        ur.undo();
        //System.out.println("doc after first undo"+doc.getText(0, doc.getLength()));
        ur.undo();
        assertEquals(afterInsert,doc.getText(0, doc.getLength()));
        //System.out.println("doc after second undo"+doc.getText(0, doc.getLength()));
        ur.undo();
        //System.out.println("doc after third undo"+doc.getText(0, doc.getLength()));
        assertEquals(original, doc.getText(0, doc.getLength()));
        
        ur.redo();
        assertEquals(afterInsert,doc.getText(0, doc.getLength()));
        //System.out.println("doc after first redo"+doc.getText(0, doc.getLength()));
        ur.redo();
        //System.out.println("doc after second redo"+doc.getText(0, doc.getLength()));
        ur.redo();
        //System.out.println("doc after third redo"+doc.getText(0, doc.getLength()));
    }
    
    public void testIssue83963_1() throws Exception {
        SchemaModel model = Util.loadSchemaModel("resources/undoredo.xsd");
        BaseDocument doc = (BaseDocument) model.getModelSource().
                getLookup().lookup(BaseDocument.class);
        Schema s = model.getSchema();
        TestComponentListener listener = new TestComponentListener();
        model.addComponentListener(listener);
        UndoManager ur = new UndoManager();
        model.addUndoableEditListener(ur);
        doc.removeUndoableEditListener(ur);
        
        //System.out.println("doc before add ComplexType"+doc.getText(0, doc.getLength()));
        GlobalComplexType gct = model.getFactory().createGlobalComplexType();
        doc.insertString(271, "<complexType/>",null);
        
        //System.out.println("doc after add ComplexType"+doc.getText(0, doc.getLength()));
        
        String stStr = "   <xsd:simpleType name=\"lend\">\n     <xsd:list>\n       <xsd:simpleType>\n         <xsd:restriction base=\"xsd:string\"/>\n       </xsd:simpleType>\n     </xsd:list>\n   </xsd:simpleType>";
        model.sync();

        doc.insertString(285, "\n", null);
        model.sync();
        doc.insertString(286, stStr, null);
        model.sync();

        //System.out.println("doc after insert simpleType"+doc.getText(0, doc.getLength()));
        ur.undo();
        //System.out.println("doc after first undo"+doc.getText(0, doc.getLength()));
        ur.undo();
        //System.out.println("doc after second undo"+doc.getText(0, doc.getLength()));
        ur.undo();
        //System.out.println("doc after third undo"+doc.getText(0, doc.getLength()));
        
        ur.redo();
        //System.out.println("doc after first redo"+doc.getText(0, doc.getLength()));
        ur.redo();
        //System.out.println("doc after second redo"+doc.getText(0, doc.getLength()));
        ur.redo();
        //System.out.println("doc after third redo"+doc.getText(0, doc.getLength()));
    }
    private SchemaModel model;
    
}
