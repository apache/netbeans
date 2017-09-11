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

/*
 * SchemaTest.java
 * JUnit based test
 *
 * Created on October 5, 2005, 12:49 PM
 */

package org.netbeans.modules.xml.schema.model;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import javax.swing.undo.UndoManager;
import junit.framework.*;
import org.netbeans.modules.xml.schema.model.Schema.Block;
import org.netbeans.modules.xml.schema.model.Schema.Final;
import org.netbeans.modules.xml.schema.model.impl.SchemaModelImpl;
import org.netbeans.modules.xml.xam.AbstractModel;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;
import org.netbeans.modules.xml.xam.dom.NamedComponentReference;
/**
 *
 * @author rico
 */
public class SchemaTest extends TestCase {
    
    public SchemaTest(String testName) {
        super(testName);
    }

    public static final String TEST_XSD = "resources/PurchaseOrder.xsd";
    Schema schema = null;
    SchemaModel model;
    protected void setUp() throws Exception {
        model = Util.loadSchemaModel(TEST_XSD);
        schema = model.getSchema();
    }

    protected void tearDown() throws Exception {
        TestCatalogModel.getDefault().clearDocumentPool();
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(SchemaTest.class);
        return suite;
    }
    
    
    public void testReadElements() throws Exception {
        ArrayList<GlobalElement> elements = new ArrayList(schema.getElements());
        assertEquals("Schema.getElements", 2, elements.size());
        assertEquals("Schema.getElements(1)",  "comment", elements.get(1).getName());
    }

    /**
     * Test of getAttributeFormDefault method, of class org.netbeans.modules.xml.schema.model.api.Schema.
     */
    public void testGetAttributeFormDefault() {
        Form f = schema.getAttributeFormDefault();
        assertEquals("getAttributeFormDefault", Form.UNQUALIFIED, f);
    }

    /**
     * Test of setAttributeFormDefault method, of class org.netbeans.modules.xml.schema.model.api.Schema.
     */
    public void testSetAttributeFormDefault() throws Exception {
        SchemaModel model = Util.loadSchemaModel(TEST_XSD);
        Schema myschema = model.getSchema();
        model.startTransaction();
        myschema.setAttributeFormDefault(Form.QUALIFIED);
        model.endTransaction();
        
        myschema = Util.dumpAndReloadModel(model).getSchema();
        assertEquals("setAttributeFormDefault", Form.QUALIFIED, myschema.getAttributeFormDefault());
    }

    /**
     * Test of getBlockDefault method, of class org.netbeans.modules.xml.schema.model.api.Schema.
     */
    public void testGetBlockDefault() {
        Set<Block> result = schema.getBlockDefault();
        assertNull("testGetBlockDefault", result);
        assertEquals("testGetBlockDefaultDefault", "", schema.getBlockDefaultDefault().toString());
    }
    
    public void testGetBlockDefaultEffective() throws IOException{
        Set<Block> d1 = schema.getBlockDefaultDefault();
        d1.clear(); d1.add(Block.ALL);
        model.startTransaction();
        schema.setBlockDefault(d1);
        model.endTransaction();
        
        assertEquals("testGetBlockDefaultEffective.0", "#all", schema.getBlockDefaultEffective().toString());

        ArrayList<GlobalComplexType> types = new ArrayList(schema.getComplexTypes());
        GlobalComplexType typePurchaseOrder = null;
        if (types.get(0).getName().equals("PurchaseOrderType")) {
            typePurchaseOrder = types.get(0);
        }
        
        Set<GlobalComplexType.Block> d2 = typePurchaseOrder.getBlock();
        assertNull("testGetBlockEffective.2", d2);
        d2 = typePurchaseOrder.getBlockEffective();
        assertEquals("testGetBlockDefaultEffective.3", "#all", d2.toString());
    }
    
    /**
     * Test of setBlockDefault method, of class org.netbeans.modules.xml.schema.model.api.Schema.
     */
    public void testSetBlockDefault() throws IOException{
        Set<Block> d = schema.getBlockDefaultDefault();
        d.add(Block.EXTENSION);
        model.startTransaction();
        schema.setBlockDefault(d);
        model.endTransaction();
        assertEquals("testSetBlockDefault.1", "extension", schema.getBlockDefault().toString());
        assertEquals("testSetBlockDefault.2", d, schema.getBlockDefault());
    }

    /**
     * Test of getElementFormDefault method, of class org.netbeans.modules.xml.schema.model.api.Schema.
     */
    public void testGetElementFormDefault() {
        Form result = schema.getElementFormDefault();
        assertEquals("getElementFormDefault", Form.UNQUALIFIED, result);
    }

    /**
     * Test of setElementFormDefault method, of class org.netbeans.modules.xml.schema.model.api.Schema.
     */
    public void testSetElementFormDefault() throws Exception {
        SchemaModel model = Util.loadSchemaModel(TEST_XSD);
        Schema myschema = model.getSchema();
        Form old = myschema.getElementFormDefault();
        assertEquals("getElementFormDefault", Form.UNQUALIFIED, old);
        model.startTransaction();
        myschema.setElementFormDefault(Form.QUALIFIED);
        model.endTransaction();
        Form now = myschema.getElementFormDefault();
        assertEquals("setElementFormDefault: notchanged", Form.QUALIFIED, now);
        
        myschema = Util.dumpAndReloadModel(model).getSchema();
        assertEquals("setElementFormDefault", Form.QUALIFIED, myschema.getElementFormDefault());
    }

    /**
     * Test of getFinalDefault method, of class org.netbeans.modules.xml.schema.model.api.Schema.
     */
    public void testGetFinalDefault() {
        Set<Final> result = schema.getFinalDefault();
        assertNull("getFinalDefault", result);
        Set<Final> def = schema.getFinalDefaultDefault();
        assertEquals("getFinalDefaultDefault", def.toString(), "");
        result = schema.getFinalDefaultEffective();
        assertEquals("getFinalDefaultEffective", def.toString(), result.toString());
    }

    /**
     * Test of setFinalDefault method, of class org.netbeans.modules.xml.schema.model.api.Schema.
     */
     public void testSetFinalDefault() throws Exception {
        SchemaModel model = Util.loadSchemaModel(TEST_XSD);
        Schema myschema = model.getSchema();
        Set<Final> f = myschema.getFinalDefaultDefault();
        f.add(Final.ALL);
        model.startTransaction();
        myschema.setFinalDefault(f);
        model.endTransaction();
        
        myschema = Util.dumpAndReloadModel(model).getSchema();
        assertTrue("setFinalDefault flush failed", f.equals(myschema.getFinalDefault()));
     }

    /**
     * Test of getTargetNamespace method, of class org.netbeans.modules.xml.schema.model.api.Schema.
     */
    public void testGetTargetNamespace() throws Exception {
        String url = schema.getTargetNamespace();
        assertEquals("getTargetNamespace", "http://www.example.com/PO1", url);
    }

    public void testGetComplexTypes() {
        ArrayList<GlobalComplexType> types = new ArrayList(schema.getComplexTypes());
        GlobalComplexType typePurchaseOrder = null;
        if (types.get(0).getName().equals("PurchaseOrderType")) {
            typePurchaseOrder = types.get(0);
        }
        assertNotNull("getComplexTypes", typePurchaseOrder);
        
        ComplexTypeDefinition ctd = typePurchaseOrder.getDefinition();
        assertTrue("getComplexTypes:sequence", ctd instanceof Sequence);
        
        Sequence seq = (Sequence) ctd;
        assertEquals("getComplexTypes:PurchaseOrder:sequence count", 3, seq.getChildren().size());
        ArrayList<SchemaComponent> elements = new ArrayList(seq.getChildren());
        assertTrue("getComplexTypes:PurchaseOrder:sequence.element(0).name", elements.get(0) instanceof LocalElement);
        LocalElement e = (LocalElement) elements.get(1);
        assertNotNull("getComplexTypes:PurchaseOrder:billTo type null", e.getType());
        assertTrue("getComplexTypes:PurchaseOrder:billTo type", e.getType() instanceof NamedComponentReference);
        NamedComponentReference<? extends GlobalType> ref = e.getType();
        GlobalComplexType gct = (GlobalComplexType) ref.get();
        assertEquals("getComplexTypes:PurchaseOrder:billTo type", "USAddress", gct.getName()); 
    }
    
    public void testAddSchemaReferences() throws Exception {
        Import extref = schema.getModel().getFactory().createImport();
        extref.setNamespace("foor");  
        extref.setSchemaLocation("foor");
        model.startTransaction();
        schema.addExternalReference(extref);
        model.endTransaction();
        int index = ((AbstractDocumentModel)model).getAccess().getElementIndexOf(schema.getPeer(), extref.getPeer());
        assertEquals(0, index);
        
        // now add new element it should be added after import
        GlobalElement ge = schema.getModel().getFactory().createGlobalElement();
        ge.setName("newElement");
        model.startTransaction();
        schema.addElement(ge);
        model.endTransaction();
        index = ((AbstractDocumentModel)model).getAccess().getElementIndexOf(schema.getPeer(), extref.getPeer());
        assertEquals("import should still be the first child component", 0, index);
        index = ((AbstractDocumentModel)model).getAccess().getElementIndexOf(schema.getPeer(), ge.getPeer());
        assertEquals("globalelement should be appended as last component", schema.getChildren().size()-1, index);
    }
    
    public void testRollback() throws Exception {
        UndoManager um = new UndoManager();
        schema.getModel().addUndoableEditListener(um);
        
       GlobalElement stick = schema.getModel().getFactory().createGlobalElement();
       stick.setName("stickAfterRollbackElement");
       model.startTransaction();
       schema.addElement(stick);
       model.endTransaction();
       
       GlobalElement ge = schema.getModel().getFactory().createGlobalElement();
       ge.setName("newElement");
       int initialCount = schema.getElements().size();
       model.startTransaction();
       schema.addElement(ge);
       assertEquals(initialCount+1, schema.getElements().size());
       String text = (( AbstractDocumentModel)model).getAccess().getCurrentDocumentText();
       assertTrue(text.indexOf("newElement") > 0);
       ( (AbstractModel)model).rollbackTransaction();
       text = (( AbstractDocumentModel)model).getAccess().getCurrentDocumentText();
       assertTrue(text.indexOf("newElement") == -1);
       assertEquals(initialCount, schema.getElements().size());
       assertTrue(text.indexOf("stickAfterRollbackElement") > 0);
       
       um.undo();
       text = (( AbstractDocumentModel)model).getAccess().getCurrentDocumentText();
       assertTrue(text.indexOf("stickAfterRollbackElement") == -1);

       um.redo();
       text = (( AbstractDocumentModel)model).getAccess().getCurrentDocumentText();
       assertTrue(text.indexOf("stickAfterRollbackElement") > 0);
    }
    
    public void testDeleteRollback() throws Exception {
        UndoManager um = new UndoManager();
        schema.getModel().addUndoableEditListener(um);
        
       GlobalElement stick = schema.getModel().getFactory().createGlobalElement();
       stick.setName("stickAfterRollbackElement");
       model.startTransaction();
       schema.addElement(stick);
       model.endTransaction();
       
       model.startTransaction();
       ArrayList<GlobalComplexType> types = new ArrayList(schema.getComplexTypes());
       ArrayList<GlobalElement> elements = new ArrayList(schema.getElements());
       GlobalElement element = elements.get(0);
       
         if(element.getName().equals("purchaseOrder")) {
            schema.removeElement(element);
                          
            String text = (( AbstractDocumentModel)model).getAccess().getCurrentDocumentText();
            assertTrue(text.indexOf("purchaseOrder")== -1);
            ( (AbstractModel)model).rollbackTransaction();
            text = (( AbstractDocumentModel)model).getAccess().getCurrentDocumentText();
            assertTrue(text.indexOf("purchaseOrder") > 0);
            assertTrue(text.indexOf("stickAfterRollbackElement") > 0);
       
            um.undo();
            text = (( AbstractDocumentModel)model).getAccess().getCurrentDocumentText();
            assertTrue(text.indexOf("stickAfterRollbackElement") == -1);

            um.redo();
            text = (( AbstractDocumentModel)model).getAccess().getCurrentDocumentText();
            assertTrue(text.indexOf("stickAfterRollbackElement") > 0);
           
         }
       
    }
    
    public void testRenameRollback() throws Exception {
        UndoManager um = new UndoManager();
        schema.getModel().addUndoableEditListener(um);
        
       GlobalElement stick = schema.getModel().getFactory().createGlobalElement();
       stick.setName("stickAfterRollbackElement");
       model.startTransaction();
       schema.addElement(stick);
       model.endTransaction();
       
       int initialCount = schema.getElements().size();
       model.startTransaction();
       ArrayList<GlobalComplexType> types = new ArrayList(schema.getComplexTypes());
       ArrayList<GlobalElement> elements = new ArrayList(schema.getElements());
       GlobalElement element = elements.get(0);
       
         if(element.getName().equals("purchaseOrder")) {
            element.setName("TestPurchaseOrder");
                          
            String text = (( AbstractDocumentModel)model).getAccess().getCurrentDocumentText();
            assertTrue(text.indexOf("purchaseOrder")== -1);
            assertTrue(text.indexOf("TestPurchaseOrder") > 0);
            assertEquals(initialCount, schema.getElements().size());
            ( (AbstractModel)model).rollbackTransaction();
            text = (( AbstractDocumentModel)model).getAccess().getCurrentDocumentText();
            assertTrue(text.indexOf("purchaseOrder") > 0);
            assertTrue(text.indexOf("TestPurchaseOrder") == -1);
            assertTrue(text.indexOf("stickAfterRollbackElement") > 0);
            assertEquals(initialCount, schema.getElements().size());
            assertEquals("purchaseOrder", element.getName());
            
            um.undo();
            text = (( AbstractDocumentModel)model).getAccess().getCurrentDocumentText();
            assertTrue(text.indexOf("stickAfterRollbackElement") == -1);

            um.redo();
            text = (( AbstractDocumentModel)model).getAccess().getCurrentDocumentText();
            assertTrue(text.indexOf("stickAfterRollbackElement") > 0);
           
         }
      
    }
}
