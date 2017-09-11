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
