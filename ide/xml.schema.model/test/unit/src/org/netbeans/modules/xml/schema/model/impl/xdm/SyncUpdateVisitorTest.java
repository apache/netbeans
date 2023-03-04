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

package org.netbeans.modules.xml.schema.model.impl.xdm;

import java.util.ArrayList;
import junit.framework.*;
import org.netbeans.modules.xml.schema.model.*;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;
import org.netbeans.modules.xml.xam.Component;

/**
 *
 * @author Administrator
 */
public class SyncUpdateVisitorTest extends TestCase {
    
    public static final String TEST_XSD     = "resources/PurchaseOrder.xsd";
    private Schema schema;
    private SchemaModel model;

    public SyncUpdateVisitorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }
    
    protected void setUp1() throws Exception {
        model = (SchemaModel)Util.loadSchemaModel(TEST_XSD);
        schema = model.getSchema();
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(SyncUpdateVisitorTest.class);
        
        return suite;
    }

    public void testVisitDelete() throws Exception {
        setUp1();
        SyncUpdateVisitor instance = new SyncUpdateVisitor();
        
        int size = schema.getComplexTypes().size();
        GlobalComplexType gct = schema.getComplexTypes().iterator().next();
        model.startTransaction();
        instance.update(schema, gct ,SyncUpdateVisitor.Operation.REMOVE);
        model.endTransaction();
        assertEquals(size-1,schema.getComplexTypes().size());

        size = schema.getSimpleTypes().size();
        GlobalSimpleType gst = schema.getSimpleTypes().iterator().next();
        model.startTransaction();
        instance.update(schema, gst ,SyncUpdateVisitor.Operation.REMOVE);
        model.endTransaction();
        assertEquals(size-1,schema.getSimpleTypes().size());

        size = schema.getElements().size();
        GlobalElement ge = schema.getElements().iterator().next();
        model.startTransaction();
        instance.update(schema, ge ,SyncUpdateVisitor.Operation.REMOVE);
        model.endTransaction();
        assertEquals(size-1,schema.getElements().size());
    }
    
    public void testVisitAdd() throws Exception{
        setUp1();
        SyncUpdateVisitor instance = new SyncUpdateVisitor();
        
        int size = schema.getComplexTypes().size();
        schema.getModel().getFactory().createGlobalComplexType();
        GlobalComplexType gct = schema.getModel().getFactory().createGlobalComplexType();
        model.startTransaction();
        instance.update(schema, gct ,SyncUpdateVisitor.Operation.ADD);
        model.endTransaction();
        assertEquals(size+1,schema.getComplexTypes().size());

        size = schema.getSchemaReferences().size();
        SchemaComponent sc = schema.getModel().getFactory().createImport();
        model.startTransaction();
        instance.update(schema, sc ,SyncUpdateVisitor.Operation.ADD);
        model.endTransaction();
        assertEquals(size+1,schema.getSchemaReferences().size());
    }
    
    public void testRemoveAllPurchaseOrder() throws Exception {
        setUp1();
        model.startTransaction();
        recursiveRemoveChildren(schema);
        assertEquals("children removed", 0, schema.getChildren().size());
        model.endTransaction();
    }
    
    //TODO: debug mysterious StackOverflowError on UnmodifiableCollection.iterator
    /*public void testRemoveAllOTA() throws Exception {
        model = TestResolver.getDefault().getModel(NamespaceLocation.OTA);
        schema = model.getSchema();
        model.startTransaction();
        recursiveRemoveChildren(schema);
        assertEquals("children removed", 0, schema.getChildren().size());
        model.endTransaction();
    }*/
    
    public void testRemoveAllLoanApp() throws Exception {
        model = TestCatalogModel.getDefault().getSchemaModel(NamespaceLocation.LOANAPP);
        schema = model.getSchema();
        model.startTransaction();
        recursiveRemoveChildren(schema);
        assertEquals("children removed", 0, schema.getChildren().size());
        model.endTransaction();
    }
    
    public static void recursiveRemoveChildren(SchemaComponent target) {
        SchemaModel model = target.getModel();
        ArrayList<SchemaComponent> children = new ArrayList<SchemaComponent>(target.getChildren());
        for (SchemaComponent child : children) {
            recursiveRemoveChildren(child);
        }
        if (target.getParent() != null) {
            model.removeChildComponent(target);
        }
    }

    public void testCanPasteAllLoanApp() throws Exception {
        model = TestCatalogModel.getDefault().getSchemaModel(NamespaceLocation.LOANAPP);
        schema = model.getSchema();
        recursiveCanPasteChildren(schema);
        recursiveCannotPasteChildren(schema);
    }
    
    public static void recursiveCanPasteChildren(SchemaComponent target) {
        SchemaModel model = target.getModel();
        ArrayList<SchemaComponent> children = new ArrayList<SchemaComponent>(target.getChildren());
        for (SchemaComponent child : children) {
            recursiveCanPasteChildren(child);
        }
        if (target.getParent() != null) {
            target.getParent().canPaste(target);  //FIXME no assertion because can return false now
        }
    }

    public static void recursiveCannotPasteChildren(SchemaComponent target) {
        SchemaModel model = target.getModel();
        ArrayList<SchemaComponent> children = new ArrayList<SchemaComponent>(target.getChildren());
        for (SchemaComponent child : children) {
            recursiveCannotPasteChildren(child);
        }
        if (target.getParent() != null) {
            if (! (target instanceof SimpleTypeRestriction && target.getParent() instanceof LocalSimpleType)) {
            String msg = target.getClass().getName() + " canPaste " + target.getParent().getClass().getName();
            assertFalse(msg, target.canPaste(target.getParent()));
            }
        }
    }
}

