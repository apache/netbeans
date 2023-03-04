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

package org.netbeans.modules.xml.axi;

import java.util.Iterator;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import junit.framework.*;
import org.netbeans.modules.xml.axi.impl.ElementRef;
import org.netbeans.modules.xml.schema.model.ElementReference;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;


/**
 *
 * @author Ayub Khan
 */
public class RefactorRenameTest extends AbstractTestCase {
    
    public static final String RENAME_ELEMENT_XSD   = "resources/refactorRenameElement.xsd";
    public static final String RENAME_TYPE_XSD   = "resources/refactorRenameType.xsd";
    public static final String GLOBAL_ELEMENT   = "a";
    
    private Document doc = null;
    
    public RefactorRenameTest(String testName) {
        super(testName, RENAME_ELEMENT_XSD, GLOBAL_ELEMENT);
    }
    
    public static Test suite() {
//        TestSuite suite = new TestSuite(DesignPatternTest.class);
        TestSuite suite = new TestSuite();
        suite.addTest(new RefactorRenameTest("testRenameElement"));
        suite.addTest(new RefactorRenameTest("testRenameType"));
        return suite;
    }
    
    public void testRenameElement() {
        print("testRenameElement");
        try {
            loadModel(RENAME_ELEMENT_XSD);
        } catch (Exception ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        ContentModel aType = axiModel.getRoot().getContentModels().get(0);
        assertElementNames(axiModel, "b");
        
        renameElementRef((ElementRef) aType.getChildElements().get(0), "newName");
        
        assertElementNames(axiModel, "newName");
        
        Iterator it3 = axiModel.getRoot().getElements().iterator();
        Element e1 = (Element) it3.next();
        if(!e1.getName().equals("b"))
            e1 = (Element) it3.next();
        renameElement(e1, "newName2");
        
        assertElementNames(axiModel, "newName2");
    }
    
    private void assertElementNames(final AXIModel axiModel, String name) {
        ContentModel aType = axiModel.getRoot().getContentModels().get(0);
        ContentModel aType1 = axiModel.getRoot().getContentModels().get(1);
        Iterator it = getSchemaModel().getSchema().getComplexTypes().iterator();
        GlobalComplexType gct = (GlobalComplexType) it.next();
        GlobalComplexType gct1 = (GlobalComplexType) it.next();
        Iterator it1 = getSchemaModel().getSchema().getElements().iterator();
        GlobalElement ge = (GlobalElement) it1.next();
        GlobalElement ge1 = (GlobalElement) it1.next();
        assertEquals("global complex types",2,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",2,getSchemaModel().getSchema().getElements().size());
        assertEquals("AXI global element b",name,ge1.getName());
        assertEquals("AXI global element b",name,((ElementReference)gct.getDefinition().getChildren().get(0)).getRef().get().getName());
        assertEquals("AXI global element b",name,((ElementReference)gct1.getDefinition().getChildren().get(0)).getRef().get().getName());
        
        assertEquals("AXI content models",2,axiModel.getRoot().getContentModels().size());
        assertEquals("AXI global elements",2,axiModel.getRoot().getElements().size());
//		assertEquals("AXI global element a","a",axiModel.getRoot().getElements().get(0).getName());
        assertEquals("AXI global element b",name,axiModel.getRoot().getElements().get(1).getName());
        assertEquals("AXI global element b",name,((ElementRef) aType.getChildElements().get(0)).getName());
        assertEquals("AXI global element b",name,((ElementRef) aType1.getChildElements().get(0)).getName());
    }
    
    public void testRenameType() {
        print("testRenameType");
        try {
            loadModel(RENAME_TYPE_XSD);
        } catch (Exception ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
        ContentModel aType = axiModel.getRoot().getContentModels().get(0);
        Iterator it = getSchemaModel().getSchema().getComplexTypes().iterator();
        GlobalComplexType gct = (GlobalComplexType) it.next();
        Iterator it1 = getSchemaModel().getSchema().getElements().iterator();
        GlobalElement ge = (GlobalElement) it1.next();
        GlobalElement ge1 = (GlobalElement) it1.next();
        GlobalElement ge2 = (GlobalElement) it1.next();
        assertEquals("global complex types",1,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",3,getSchemaModel().getSchema().getElements().size());
        assertEquals("AXI global element b","a1",ge.getName());
        assertEquals("AXI global element b","aType",ge.getType().get().getName());
        assertEquals("AXI global element b","a2",ge1.getName());
        assertEquals("AXI global element b","aType",ge1.getType().get().getName());
        assertEquals("AXI global element b","aType",gct.getName());
        assertEquals("AXI global element b","b",((ElementReference)gct.getDefinition().getChildren().get(0)).getRef().get().getName());
        
        assertEquals("AXI content models",1,axiModel.getRoot().getContentModels().size());
        assertEquals("AXI global elements",3,axiModel.getRoot().getElements().size());
        assertEquals("AXI global element a","a1",axiModel.getRoot().getElements().get(0).getName());
        assertEquals("AXI global element b","a2",axiModel.getRoot().getElements().get(1).getName());
        assertEquals("AXI global element b","b",axiModel.getRoot().getElements().get(2).getName());
        assertEquals("AXI global element b","b",((ElementRef) aType.getChildElements().get(0)).getName());
        
        renameType(aType, "newName");
        
//		printDocument();
        assertEquals("global complex types",1,getSchemaModel().getSchema().getComplexTypes().size());
        assertEquals("global simple types",0,getSchemaModel().getSchema().getSimpleTypes().size());
        assertEquals("global elements",3,getSchemaModel().getSchema().getElements().size());
        assertEquals("AXI global element b","a1",ge.getName());
        //FIXME - is null
//		assertEquals("AXI global element b","aType",ge.getType().get().getName());
        assertEquals("AXI global element b","a2",ge1.getName());
        //FIXME - is null
//		assertEquals("AXI global element b","aType",ge1.getType().get().getName());
        assertEquals("AXI global element b","newName",gct.getName());
        assertEquals("AXI global element b","b",((ElementReference)gct.getDefinition().getChildren().get(0)).getRef().get().getName());
        
        assertEquals("AXI content models",1,axiModel.getRoot().getContentModels().size());
        assertEquals("AXI global elements",3,axiModel.getRoot().getElements().size());
        assertEquals("AXI global element a","a1",axiModel.getRoot().getElements().get(0).getName());
        assertEquals("AXI global element b","a2",axiModel.getRoot().getElements().get(1).getName());
        assertEquals("AXI global element b","b",axiModel.getRoot().getElements().get(2).getName());
        assertEquals("AXI global element b","b",((ElementRef) aType.getChildElements().get(0)).getName());
    }
    
    private void printDocument() {
        try {
            SchemaModel sm = getSchemaModel();
            doc = ((AbstractDocumentModel)sm).getBaseDocument();
            print("doc: "+doc.getText(0, doc.getLength()));
        } catch (BadLocationException ex) {
            //ex.printStackTrace();
            assertTrue("Should not be here", false);
        }
    }
    
    private void renameElement(Element e, String name) {
        axiModel.startTransaction();
        e.setName(name);
        axiModel.endTransaction();
    }
    
    private void renameElementRef(ElementRef eref, String name) {
        axiModel.startTransaction();
        eref.setName(name);
        axiModel.endTransaction();
    }
    
    private void renameType(ContentModel aType, String name) {
        axiModel.startTransaction();
        aType.setName(name);
        axiModel.endTransaction();
    }
}
