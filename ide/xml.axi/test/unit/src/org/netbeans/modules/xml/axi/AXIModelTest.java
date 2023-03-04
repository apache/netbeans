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

import java.util.List;
import junit.framework.*;
import org.netbeans.modules.xml.axi.util.FileUtil;
import org.netbeans.modules.xml.axi.util.ModelValidator;


/**
 * The unit test covers the integrity of the AXI model.
 * In reverseEngineer(), it reads a schema file and creates the model.
 * In forwardEngineer(), it reads a xml file creates the model and then
 * code generates a schema. Generated code may not be available.
 *
 * @author Samaresh (Samaresh.Panda@Sun.Com)
 */
public class AXIModelTest extends AbstractTestCase {
    
    public static final String TEST_XSD         = "resources/po.xsd";
    public static final String GLOBAL_ELEMENT   = "purchaseOrder";
    public static final String META_XSD         = "resources/XMLSchema.xsd";
    //public static final String META_XSD         = "resources/metaSchema.xsd";
    
    
    /**
     * AXIModelTest
     */
    public AXIModelTest(String testName) {
        super(testName, TEST_XSD, GLOBAL_ELEMENT);
    }
    
    /**
     * AXIModelTest
     */
    public AXIModelTest(String testName, String schemaFile, String elementName) {
        super(testName, schemaFile, elementName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite();
//        Disabled as referenced XSD file were partly not donated by oracle to apache
//        suite.addTest(new AXIModelTest("testAXIModel"));
//        suite.addTest(new AXIModelTest("testAXIModelForMetaSchema"));
//        suite.addTest(new AXIModelTest("testRecursiveResolve1"));
//        suite.addTest(new AXIModelTest("testRecursiveResolve2"));
//        suite.addTest(new AXIModelTest("testSubstitutionGroup"));
        return suite;
    }        
    
    public void testAXIModel() throws Exception {
        reverseEngineer();
        axiModel.setSchemaDesignPattern(SchemaGenerator.Pattern.GARDEN_OF_EDEN);
        forwardEngineer();
    }
    
    public void testAXIModelForMetaSchema() throws Exception {
        loadModel(META_XSD);
        AXIDocument document = axiModel.getRoot();
        ContentModel schemaTop = findContentModel("schemaTop");
        assert(schemaTop.getChildElements().size() == 7);
        
        Element schema = findAXIGlobalElement("schema");        
        assert(schema.getAttributes().size() == 8);
        assert(schema.getChildElements().size() == 12);
        assert(schema.getCompositor() != null);
    }    
    
    /**
     * See http://www.netbeans.org/issues/show_bug.cgi?id=134861.
     */
    public void testRecursiveResolve1() throws Exception {
        loadModel("resources/A.xsd");
        AXIDocument document = axiModel.getRoot();
        Element e = findAXIGlobalElement("A");
        assert(e.getChildElements().size() == 2);
        assert(e.getChildElements().get(0).getName().equals("C11"));
        assert(e.getChildElements().get(1).getName().equals("C12"));
    }
    
    /**
     * Handle substitutionGroup.
     */
    public void testSubstitutionGroup() throws Exception {
        loadModel("resources/misc.xsd");
        Element e = findAXIGlobalElement("umbrella");
        List<AbstractElement> children = e.getChildElements();        
        assert(children.size() == 2);
        assert("number".equals(children.get(0).getName()));
        assert("name".equals(children.get(1).getName()));
    }
    
    /**
     * See http://www.netbeans.org/issues/show_bug.cgi?id=134861.
     */
    public void testRecursiveResolve2() throws Exception {
        loadModel("resources/A_1.xsd");
        AXIDocument document = axiModel.getRoot();
        Element e = findAXIGlobalElement("A");
        assert(e.getChildElements().size() == 2);
        assert(e.getChildElements().get(0).getName().equals("C11"));
        assert(e.getChildElements().get(1).getName().equals("C12"));
    }
    
    /**
     * Tests forward engineering of AXI model.
     * Creates an AXI tree by parsing an XML input file
     * and then compares it against the DOM tree for the
     * same XML.
     */
    private void forwardEngineer() {
        if(referenceXML == null) return;
        FileUtil.parseXMLAndPopulateAXIModel(
                referenceXML, getAXIModel());
        ModelValidator visitor = new ModelValidator(referenceXML);
        Element po = getAXIModel().getRoot().getElements().get(0);
        Element first = (Element)po.getChildElements().get(0);
        assert(first.getParentElement() == po);
        boolean result = visitor.visitAndCompareAgainstDOMElement(po);
        this.assertEquals(visitor.getErrorMessage(), true, result);
    }
    
    /**
     * Tests reverse engineering of AXI model.
     * Creates an AXI tree for a schema global element and
     * compares it against the DOM tree.
     */
    public void reverseEngineer() {
        assertNotNull(globalElement);
        assertNotNull(getAXIModel().getRoot());
        //visit each node in the AXI tree and compare against
        //corresponding DOM node.
        ModelValidator visitor = new ModelValidator(referenceXML);
        boolean result = visitor.visitAndCompareAgainstDOMElement(globalElement);
        this.assertEquals(visitor.getErrorMessage(),
                true, result);
    }    
}
