/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

/*
 * FindUsageVisitorTest.java
 * JUnit based test
 *
 * Created on November 3, 2005, 2:34 PM
 */

package org.netbeans.modules.xml.schema.model.visitor;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import junit.framework.*;
import org.netbeans.modules.xml.schema.model.GlobalAttributeGroup;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.GlobalType;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.TestCatalogModel;
import org.netbeans.modules.xml.schema.model.Util;
import org.netbeans.modules.xml.xam.NamedReferenceable;
/**
 *
 * @author Samaresh
 */
public class FindUsageVisitorTest extends TestCase {
    
    public static final String TEST_XSD                     = "resources/J1_TravelItinerary.xsd";    
    public static final String FIND_USAGE_FOR_ATTR_GROUP    = "OTA_PayloadStdAttributes";
    public static final String FIND_USAGE_FOR_ELEMENT       = "TPA_Extensions";
    public static final String FIND_USAGE_FOR_TYPE          = "TransactionActionType";
    public static final String NO_TARGET_NAMESPACE = "resources/CTDerivations.xsd";
    private Schema          schema                  = null;
    private GlobalElement   global_element          = null;
    private GlobalType      global_type             = null;
    private GlobalAttributeGroup global_attribute_group        = null;
    
    public FindUsageVisitorTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
    }
    protected void setUp1() throws Exception {
	SchemaModel model = Util.loadSchemaModel(TEST_XSD);
	schema = model.getSchema();
        
        Collection<GlobalType> types = new ArrayList<>(schema.getComplexTypes());
        types.addAll(schema.getSimpleTypes());
        for(GlobalType type : types) {
            if(type.getName().equals(FIND_USAGE_FOR_TYPE)) {
                this.global_type = type;
            }
        }
        
        for(GlobalElement e : schema.getElements()) {
            if(e.getName().equals(FIND_USAGE_FOR_ELEMENT)) {
                this.global_element = e;
            }
        }
        
        for(GlobalAttributeGroup gag : schema.getAttributeGroups()) {
            if(gag.getName().equals(FIND_USAGE_FOR_ATTR_GROUP)) {
                this.global_attribute_group = gag;
            }
        }        
    }

    protected void tearDown() throws Exception {
        TestCatalogModel.getDefault().clearDocumentPool();
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(FindUsageVisitorTest.class);
        return suite;
    }
    
//    Disabled as referenced files were partly not donated by oracle to apache    
//    public void testFindPath() throws Exception {
//        setUp1();
//        this.assertEquals(49, findUsageCountForItem(global_element));
//        this.assertEquals(3, findUsageCountForItem(global_type));
//        this.assertEquals(4, findUsageCountForItem(global_attribute_group));        
//    }

    public int findUsageCountForItem(NamedReferenceable<SchemaComponent> ref) {
        long startTime = System.currentTimeMillis();
        System.out.println("Finding Usage for " + ref.getName() == null? ref : ref.getName());
        FindUsageVisitor usage = new FindUsageVisitor();
        Preview preview = usage.findUsages(Collections.singletonList(schema), ref);
        System.out.println(preview.getUsages().size() + " occurances found!!!");
                
        Map<SchemaComponent, List<SchemaComponent>> usageMap = preview.getUsages();
        for(SchemaComponent c : usageMap.keySet()) {
            System.out.println("Path for component: " + c);
            List<SchemaComponent> path = usageMap.get(c);
            for(SchemaComponent e : path) {
                System.out.println(getComponentDetail(e));
            }
            System.out.println("\n");            
            //if u want to compare with the paths as it were obtained using PathFromRootVisitor
            //getPathUsingPathFromRootVisitor(schema, c);            
        }
        long endTime = System.currentTimeMillis();
        System.out.println("Time taken: " + (endTime - startTime));
        return usageMap.keySet().size();
        //this.assertEquals(49, usageMap.keySet().size());
    }
    
    private String getComponentDetail(SchemaComponent component) {
        String details = component.toString();
        if(component instanceof GlobalComplexType) {
            return details + ":" + ((GlobalComplexType)component).getName();
        }
        if(component instanceof GlobalSimpleType) {
            return details + ":" + ((GlobalSimpleType)component).getName();
        }
        if(component instanceof LocalElement) {
            return details + ":" + ((LocalElement)component).getName();
        }        
        if(component instanceof GlobalElement) {
            return details + ":" + ((GlobalElement)component).getName();
        }
        return details;
    }
    
    public void testNoTargetNamespace() throws Exception {
        SchemaModel model = Util.loadSchemaModel(NO_TARGET_NAMESPACE);
        Schema schema = model.getSchema();
        GlobalComplexType gct = schema.getComplexTypes().iterator().next();
        assertEquals("Base-For-Restriction", gct.getName());
        FindUsageVisitor fuv = new FindUsageVisitor();
        Preview pv = fuv.findUsages(Collections.singleton(schema), gct);
        assertEquals("notargetnamespace.usages.count", 2, pv.getUsages().size());
    }
    
    public void testUnion() throws Exception {
	SchemaModel model = Util.loadSchemaModel("resources/PurchaseOrder_union.xsd");
        FindUsageVisitor fuv = new FindUsageVisitor();
        GlobalSimpleType type = Util.findGlobalSimpleType(model.getSchema(), "Money");
        assertEquals("Money", type.getName());
        model.getSchema().getSimpleTypes();
        Preview pv = fuv.findUsages(Collections.singleton(model.getSchema()), type);
        assertEquals("findusage on Money count", 1, pv.getUsages().size());
    }
}
