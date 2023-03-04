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
 * RefactorVisitorTest.java
 * JUnit based test
 *
 * Created on October 18, 2005, 3:57 PM
 */

package org.netbeans.modules.xml.schema.model.impl;

import java.io.IOException;
import java.util.Collections;
import junit.framework.*;
import org.netbeans.modules.xml.schema.model.Util;
import org.netbeans.modules.xml.schema.model.*;
import org.netbeans.modules.xml.schema.model.visitor.*;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;

/**
 *
 * @author Administrator
 */
public class RefactorVisitorTest extends TestCase {
    
    public static final String TEST_XSD     = "resources/PurchaseOrder.xsd";
    
    private Schema          schema                  = null;
    private GlobalElement   global_element          = null;
    private GlobalType      global_type             = null;
    private GlobalAttribute global_attribute        = null;
    private SchemaModel model;
    
    public RefactorVisitorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
	model = Util.loadSchemaModel(TEST_XSD);
	schema = model.getSchema();
        
        for(GlobalType type : schema.getComplexTypes()) {
            if(type.getName().endsWith("USAddress")) {
                this.global_type = type;
            }
        }
        
        for(GlobalElement e : schema.getElements()) {
            if(e.getName().endsWith("comment")) {
                this.global_element = e;
            }
        }        
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(RefactorVisitorTest.class);
        return suite;
    }
        
    public void testRenameGlobalType() throws IOException{
        String oldVal = global_type.getName();
        String newVal = "MyAddress";
        FindUsageVisitor usage = new FindUsageVisitor();
        Preview preview_before = usage.findUsages(Collections.singletonList(schema), global_type);
        System.out.println(preview_before.getUsages().size() + " occurances of " + oldVal + " found!!!");
                
        RefactorVisitor visitor = new RefactorVisitor();
        model.startTransaction();
        String oldName = global_type.getName();
        global_type.setName(newVal);
        model.endTransaction();
        visitor.setRenamedElement(global_type, oldName);
        model.startTransaction();
        visitor.rename(preview_before);
        model.endTransaction();
        
        usage = new FindUsageVisitor();
        Preview preview_after = usage.findUsages(Collections.singletonList(schema), global_type);
        System.out.println(preview_after.getUsages().size() + " occurances of " + newVal + " found!!!");
        this.assertEquals(preview_before.getUsages().size(), preview_after.getUsages().size());        
    }
    
    public void testRenameGlobalElement() throws IOException{
        System.out.println("Renaming global element comment to xcomment...");
        String oldVal = global_element.getName();
        String newVal = "xcomment";
        FindUsageVisitor usage = new FindUsageVisitor();
        Preview preview_before = usage.findUsages(Collections.singletonList(schema), global_element);
        System.out.println(preview_before.getUsages().size() + " occurances of " + oldVal + " found!!!");
                
        RefactorVisitor visitor = new RefactorVisitor();
        model.startTransaction();
        String oldName = global_element.getName();
        global_element.setName(newVal);
        model.endTransaction();
        visitor.setRenamedElement(global_element, oldName);
        model.startTransaction();
        visitor.rename(preview_before);
        model.endTransaction();
        
        usage = new FindUsageVisitor();
        Preview preview_after = usage.findUsages(Collections.singletonList(schema), global_element);
        //System.out.println(preview_after.getUsages().size() + " occurances of " + newVal + " found!!!");
        assertEquals(preview_before.getUsages().size(), preview_after.getUsages().size());
    }

    public static void renameComponent(ReferenceableSchemaComponent component, String newName) throws Exception {
        SchemaModel model = component.getModel();
        Schema schema = model.getSchema();
        FindUsageVisitor usage = new FindUsageVisitor();
        Preview preview_before = usage.findUsages(Collections.singletonList(schema), component);
        RefactorVisitor visitor = new RefactorVisitor();

        model.startTransaction();
        String oldName = component.getName();
        component.setName(newName);
        visitor.setRenamedElement(component, oldName);
        visitor.rename(preview_before);
        model.endTransaction();
    }
    
    public void testRenameSimpleTypeInUnionMemberType() throws Exception {
	SchemaModel model = Util.loadSchemaModel("resources/PurchaseOrder_union.xsd");
        GlobalSimpleType moneyType = Util.findGlobalSimpleType(model.getSchema(), "Money");
        GlobalSimpleType unionType = Util.findGlobalSimpleType(model.getSchema(), "MoneyOrPercentageType");
        Union u = (Union) unionType.getDefinition();
        String memberTypes = ((AbstractDocumentComponent)u).getAttribute(SchemaAttributes.MEMBER_TYPES);
        model.startTransaction();
        new RefactorVisitor().rename(moneyType, "USDollar");
        model.endTransaction();
        assertEquals("po:USDollar po:Percentage", ((AbstractDocumentComponent)u).getAttribute(SchemaAttributes.MEMBER_TYPES));
    }
}
