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

package org.netbeans.modules.xml.axi.datatype;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import junit.framework.*;
import org.netbeans.modules.xml.axi.*;
import org.netbeans.modules.xml.schema.model.GlobalSimpleType;
import org.netbeans.modules.xml.schema.model.Length;
import org.netbeans.modules.xml.schema.model.Pattern;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.TotalDigits;


/**
 *
 * @author Ayub Khan
 */
public class DatatypeFactoryTest extends AbstractTestCase {
    
    public static final String TEST_XSD         = "resources/types.xsd";
    public static final String GLOBAL_ELEMENT   = "purchaseOrder";
    
    private List<Attribute> attList;
    
    public DatatypeFactoryTest(String testName) {
        super(testName, TEST_XSD, GLOBAL_ELEMENT);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        attList = new ArrayList<Attribute>();
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite();
//        Disabled as referenced XSD file were partly not donated by oracle to apache
//        suite.addTestSuite(DatatypeFactoryTest.class);
        return suite;
    }
    
    /**
     * Test of createElement method, of class org.netbeans.modules.xml.axi.XAMFactory.
     */
    public void testFindApplicableFacets() {
        validateSchema(axiModel.getSchemaModel());
        Collection<GlobalSimpleType> types = getSchemaModel().getSchema().getSimpleTypes();
        assertEquals("primitiveTypes", 7, types.size());
        
        for(GlobalSimpleType type:types) {
            if(type.getName().equals("myDate")) {
                long start = System.currentTimeMillis();
                List<Class<? extends SchemaComponent>> facets =
                        DatatypeFactory.getDefault().getApplicableSchemaFacets(type);
                long end = System.currentTimeMillis();
                print("time taken to find facets from GlobalSimpleType: "+(end-start)+"ms");
                assertEquals("Facets", 6, facets.size());
                start = System.currentTimeMillis();
                facets =
                        DatatypeFactory.getDefault().getApplicableSchemaFacets(type);
                end = System.currentTimeMillis();
                print("time taken to find same facets (second time) from GlobalSimpleType: "+(end-start)+"ms");
            } else if(type.getName().equals("myDate1")) {
                assertEquals("Facets", 7, DatatypeFactory.getDefault().getApplicableSchemaFacets(type).size());
                assertEquals("Facets", Pattern.class, DatatypeFactory.getDefault().getApplicableSchemaFacets(type).get(0));
            } else if(type.getName().equals("SKU")) {
                assertEquals("Facets", 6, DatatypeFactory.getDefault().getApplicableSchemaFacets(type).size());
                assertEquals("Facets", Length.class, DatatypeFactory.getDefault().getApplicableSchemaFacets(type).get(0));
            } else if(type.getName().equals("ListOfMyDate")) {
                assertEquals("Facets", 6, DatatypeFactory.getDefault().getApplicableSchemaFacets(type).size());
                //test same instance is returned
                assertEquals("Facets", DatatypeFactory.getDefault().getApplicableSchemaFacets(type), DatatypeFactory.getDefault().getApplicableSchemaFacets(type));
            } else if(type.getName().equals("Cost")) {
                assertEquals("Facets", 9, DatatypeFactory.getDefault().getApplicableSchemaFacets(type).size());
                assertEquals("Facets", TotalDigits.class, DatatypeFactory.getDefault().getApplicableSchemaFacets(type).get(0));
            }
        }
    }
    
}
