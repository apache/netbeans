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
 * AnnotationImplTest.java
 * JUnit based test
 *
 * Created on October 31, 2005, 11:17 AM
 */

package org.netbeans.modules.xml.schema.model.impl;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import junit.framework.*;
import org.netbeans.modules.xml.schema.model.Annotation;
import org.netbeans.modules.xml.schema.model.AppInfo;
import org.netbeans.modules.xml.schema.model.Documentation;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.Util;
import org.netbeans.modules.xml.schema.model.visitor.FindSchemaComponentFromDOM;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.dom.DocumentComponent;
import org.netbeans.modules.xml.xam.ComponentEvent;
import org.netbeans.modules.xml.xam.ComponentListener;
import org.netbeans.modules.xml.xdm.nodes.Text;
import org.w3c.dom.Element;

/**
 * @author Nam Nguyen
 * @author Vidhya Narayanan
 */
public class AnnotationImplTest extends TestCase {

    public static final String TEST_XSD = "resources/loanApplication.xsd";
    Schema schema = null;
    TestPropertyListener plistener;
    TestComponentListener clistener;
    
    public AnnotationImplTest(String testName) {
	super(testName);
    }
    
    static class TestPropertyListener implements PropertyChangeListener {
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
        List<ComponentEvent> accu = new ArrayList<ComponentEvent>();
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
        public List<ComponentEvent> getEvents() { return accu; }
    
        private void assertEvent(ComponentEvent.EventType type, DocumentComponent source) {
            for (ComponentEvent e : accu) {
                if (e.getEventType().equals(type) &&
                    e.getSource() == source) {
                    return;
                }
            }
            assertTrue("Expect component change event" + type +" on source " + source, false);
        }
    }    
    
    protected void setUp() throws Exception {
	SchemaModel model = Util.loadSchemaModel(TEST_XSD);
        clistener = new TestComponentListener();
        plistener = new TestPropertyListener();
        model.addComponentListener(clistener);
        model.addPropertyChangeListener(plistener);
	schema = model.getSchema();
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(AnnotationImplTest.class);
        
        return suite;
    }

    /**
     * Test of getDocumentations method, of class org.netbeans.modules.xml.schema.model.impl.AnnotationImpl.
     */
    public void testDocumentationInternalDetail() throws Exception {
        String xpath = "/schema/element[1]/annotation";
        Annotation ann = FindSchemaComponentFromDOM.find(Annotation.class, schema, xpath);
        assertEquals("getDocumentations().size()", 2, ann.getDocumentationElements().size());
        Documentation doc = ann.getDocumentationElements().iterator().next();
        assertEquals("en", doc.getLanguage());
        String txt = "A loan application";
        assertEquals("documentation read text", txt, doc.getContent());
        
        String txt2 = "testDocumentation.write";
        schema.getModel().startTransaction();
        doc.setContent(txt2);
        schema.getModel().endTransaction();
        assertEquals("testDocumentation.write", "testDocumentation.write", doc.getContent());
        
        schema.getModel().startTransaction();
        Element parentPeer = ((AbstractDocumentComponent)ann).getPeer();
        Element docElement = doc.getDocumentationElement();
        assertTrue("documentation element cloned", docElement != ((AbstractDocumentComponent)doc).getPeer());
        assertFalse("cloned element not in tree", ((org.netbeans.modules.xml.xdm.nodes.Node)docElement).isInTree());
        Element docMyTag = schema.getModel().getDocument().createElement("mytag");
        docMyTag.setAttribute("attribute1", "value1");
        docElement.appendChild(docMyTag);
        doc.setDocumentationElement(docElement);
        schema.getModel().endTransaction();
        assertTrue("parent component not changed", ann == doc.getParent());
        assertTrue("parent peer node updated", parentPeer != ((AbstractDocumentComponent)ann).getPeer());
        assertTrue("peer == documentation element used", docElement == ((AbstractDocumentComponent)doc).getPeer());
        assertTrue("documentation element child", docElement.getLastChild() == docMyTag);
        Text txtNode = (Text)docElement.getChildNodes().item(0);
        assertEquals("documentation element text child", txt2, txtNode.getText());
        assertTrue("updated element now in tree", ((org.netbeans.modules.xml.xdm.nodes.Node)docElement).isInTree());
        
        //Util.dumpToFile(schema.getModel().getBaseDocument(), new File("C:\\temp\\test.xsd"));
    }
    
    public void testDocumentationReadElement() throws IOException {
        String xpath = "/schema/element[1]/annotation/documentation[2]";
        Documentation doc = FindSchemaComponentFromDOM.find(Documentation.class, schema, xpath);
        assertEquals("documentation content when no-text", "", doc.getContent().trim());
        Element doce = doc.getDocumentationElement();
        assertEquals("documentation element count", 3, doce.getChildNodes().getLength());
        Element html = (Element) doce.getChildNodes().item(1);
        assertEquals("documentation element tag", "html", html.getNodeName());
        Element a = (Element) html.getChildNodes().item(1);
        String aText = "Testing documenation elemnent";
        assertEquals("doc element children", aText, ((Text)a.getFirstChild()).getText());
    }
    
    public void testAppInfo() throws IOException {
        String xpath = "/schema/element[1]/annotation";
        Annotation ann = FindSchemaComponentFromDOM.find(Annotation.class, schema, xpath);
        AppInfo info = ann.getAppInfos().iterator().next();
        assertEquals("appinfo source", "http://www.aloan.com/loanApp", info.getURI());
        Element infoE = info.getAppInfoElement();
        Element handlingE = (Element)infoE.getChildNodes().item(1);
        Text textnode = (Text)handlingE.getFirstChild();
        assertEquals("appinfo element child", "checkForPrimes", textnode.getText());

        ann.getModel().startTransaction();
        ann.removeAppInfo(info);
        AppInfo info2 = ann.getModel().getFactory().createAppInfo();
        textnode.setText("checkIfUpdated");
	ann.getModel().endTransaction();
    }

    public void testDocumentationFragment() throws Exception {
        String xpath = "/schema/element[1]/annotation";
        Annotation ann = FindSchemaComponentFromDOM.find(Annotation.class, schema, xpath);
        Documentation docu = ann.getDocumentationElements().iterator().next();
        String oldContent = docu.getContent();
        
        schema.getModel().startTransaction();
        try {
            docu.setContentFragment("<bad/xml>");
            assertFalse("should have thrown IOException", true);
        } catch(IOException ioe) {
            assertTrue("gotit", true);
        }
        String fragment = "<good>This is a good &lt;doc></good>";
        docu.setContentFragment(fragment);
        schema.getModel().endTransaction();
        
        clistener.assertEvent(ComponentEvent.EventType.VALUE_CHANGED, docu);
        plistener.assertEvent(Documentation.CONTENT_PROPERTY, oldContent, fragment);
        assertEquals(fragment, docu.getContentFragment());
        Element good = (Element) docu.getDocumentationElement().getChildNodes().item(0);
        assertEquals("good", good.getTagName());
        Text text = (Text) good.getChildNodes().item(0);
        assertEquals("This is a good <doc>", text.getText());
    }
}
 
