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

package org.netbeans.modules.xml.axi.datatype;

import java.util.ArrayList;
import java.util.List;
import junit.framework.*;
import org.netbeans.modules.xml.axi.*;
import org.netbeans.modules.xml.axi.visitor.DefaultVisitor;


/**
 *
 * @author Ayub Khan
 */
public class DatatypePerfTest extends AbstractTestCase {
    
    public static final String TEST_XSD         = "resources/OTA_TravelItinerary.xsd";
    public static final String GLOBAL_ELEMENT   = "OTA_TravelItineraryRS";
    
    private List<AbstractAttribute> attList;
    
    public DatatypePerfTest(String testName) {
        super(testName, TEST_XSD, GLOBAL_ELEMENT);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
        attList = new ArrayList<AbstractAttribute>();
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite();
//        Disabled as referenced XSD file were partly not donated by oracle to apache
//        suite.addTestSuite(DatatypePerfTest.class);
        return suite;
    }
    
    /**
     * Test of createElement method, of class org.netbeans.modules.xml.axi.XAMFactory.
     */
    public void testCreateDatatype() {
        validateSchema(axiModel.getSchemaModel());
        Element element = globalElement;
        assertNotNull(element);
        
        attList.clear();
        TestVisitor visitor = new TestVisitor();
        visitor.visit(element);
        
        String contentType = "";
        if(element.getType() instanceof Datatype)
            contentType = element.getType().getName();
        print("\n\n=================\nGE: "+element.getName()+"["+contentType+"]");
    }
    
    private void printAttributes(List<Attribute> attList) {
        for(Attribute attr:attList) {
            String attName = "";
            if(attr.getName() != null)
                attName = attr.getName();
            Datatype type = (Datatype) attr.getType();
            if(type != null) {
                print("\n=================\n"+attName+"["+type.getName()+"]");
                printFacets(type);
            }
        }
    }
    
    private void printFacets(final Datatype type) {
        print("\nlength: "+type.getLengths());
        print("\nminLength: "+type.getMinLengths());
        print("\nmaxLength: "+type.getMaxLengths());
        print("\npattern: "+type.getPatterns());
        print("\nenum: "+type.getEnumerations());
        print("\nwhitespace: "+type.getWhiteSpaces());
    }
    
    private class TestVisitor extends DefaultVisitor {
        
        private int depth = 0;
        
        /**
         * Creates a new instance of TestVisitor
         */
        public TestVisitor() {
            try {
            } catch(Exception ex) {
                //ex.printStackTrace();
                assertTrue("Should not be here", false);
            }
        }
        
        public void visit(Element element) {
            for(AbstractAttribute attr : element.getAttributes()) {
                visit(attr);
            }
            visitChildren(element);
        }
        
        public void visit(AbstractAttribute attribute) {
            attList.add(attribute);
        }
        
        protected void visitChildren(AXIComponent component) {
            for(AXIComponent child: component.getChildren()) {
                child.accept(this);
            }
        }
    }
    
}
