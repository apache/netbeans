/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright (c) 2006, 2016 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
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
