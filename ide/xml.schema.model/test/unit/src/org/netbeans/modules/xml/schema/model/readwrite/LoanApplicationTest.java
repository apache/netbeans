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
 * LoanApplicationTest.java
 *
 * Created on October 26, 2005, 10:31 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.schema.model.readwrite;

import java.io.File;
import junit.framework.TestCase;
import org.netbeans.modules.xml.schema.model.*;
import org.netbeans.modules.xml.schema.model.Util;
import org.netbeans.modules.xml.schema.model.impl.SchemaModelImpl;
import org.netbeans.modules.xml.schema.model.visitor.FindSchemaComponentFromDOM;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentComponent;

/**
 *
 * @author rico
 */
public class LoanApplicationTest extends TestCase implements TestSchemaReadWrite {
    
    /** Creates a new instance of LoanApplicationTest */
    public LoanApplicationTest(String testName) {
        super(testName);
    }
    
    private static final String TEST_XSD = "resources/loanApplication.xsd";
    
    public String getSchemaResourcePath() {
        return TEST_XSD;
    }
    
    protected void setUp() throws Exception {
    }
    
    protected void tearDown() throws Exception {
        TestCatalogModel.getDefault().clearDocumentPool();
    }
    
    public void testRead() throws Exception {
        SchemaModel model = Util.loadSchemaModel(TEST_XSD);
        Schema schema =  model.getSchema();
        checkRead(schema);
    }
    
    private void checkRead(Schema schema) throws Exception {
        String xpath = "/xs:schema/xs:element[@name='auto-loan-application']/xs:complexType/xs:sequence/xs:element[@name='loan-type']";
        SchemaComponent sc = new FindSchemaComponentFromDOM().findComponent(schema, xpath);
        assertTrue("loan-type " + sc, sc instanceof LocalElement);
        LocalElement le = (LocalElement) sc;
        GlobalSimpleType gst = (GlobalSimpleType) le.getType().get();
        assertEquals("loan-type type name", "LoanType", gst.getName());
    }
    
    public void testWrite() throws Exception {
        SchemaModel model = Util.loadSchemaModel("resources/Empty_loanApp.xsd");
        Schema s = model.getSchema();
        SchemaComponentFactory factory = model.getFactory();
        
        model.startTransaction();
        //set attributes
        s.setAttributeFormDefault(Form.QUALIFIED);
        s.setElementFormDefault(Form.UNQUALIFIED);
	//<xs:element name="auto-loan-application">
        GlobalElement ge1 = Util.createGlobalElement(model, "auto-loan-application");   
        assertEquals("xs prefix", "xs", ((AbstractDocumentComponent)ge1).getPeer().getPrefix());
        Util.createAnnotation(model, ge1, "A loan application");
        LocalComplexType t2 = Util.createLocalComplexType(model, ge1);
        Sequence seq = Util.createSequence(model, t2);
        LocalElement le = Util.createLocalElement(model, seq, "loan-type", 1);
        LocalElement loanType = le;
        le = Util.createLocalElement(model, seq, "term", 2);
        le.setType(factory.createGlobalReference(
                Util.getPrimitiveType("integer"), GlobalSimpleType.class, le));
        le = Util.createLocalElement(model, seq, "amount", 3);
        LocalSimpleType lst = Util.createLocalSimpleType(model, le);
        SimpleTypeRestriction restriction = Util.createSimpleRestriction(model, lst);
        restriction.setBase(factory.createGlobalReference(
                Util.getPrimitiveType("decimal"), GlobalSimpleType.class, restriction));
        assertEquals("xs:decimal", restriction.getBase().getRefString());
        
        GlobalSimpleType gst1 = Util.createGlobalSimpleType(model, "LoanType");
        loanType.setType(factory.createGlobalReference(gst1, GlobalSimpleType.class, le));
	//<xs:simpleType name="State">
        GlobalSimpleType gst2 = Util.createGlobalSimpleType(model, "State");
	//<xs:complexType name="Applicant">
        GlobalComplexType gct1 = Util.createGlobalComplexType(model, "Applicant");
        
	//<xs:complexType name="Address">
        GlobalComplexType gct2 = Util.createGlobalComplexType(model, "Address");
        seq = Util.createSequence(model, gct2);
        Util.createLocalElement(model, seq, "address1", 1);
        Util.createLocalElement(model, seq, "address2", 2);
        Util.createLocalElement(model, seq, "city", 3);
        le = Util.createLocalElement(model, seq, "state", 4);
        le.setType(factory.createGlobalReference(gst2, GlobalSimpleType.class, le));
        le = Util.createLocalElement(model, seq, "zip", 5);
        lst = Util.createLocalSimpleType(model, le);
        restriction = Util.createSimpleRestriction(model, lst);
        restriction.setBase(factory.createGlobalReference(
                Util.getPrimitiveType("string"), GlobalSimpleType.class, restriction));
        MinLength min = factory.createMinLength(); min.setValue(5);
        restriction.addMinLength(min);
        MaxLength max = factory.createMaxLength(); max.setValue(5);
        restriction.addMaxLength(max);
        Pattern pat = factory.createPattern(); pat.setValue("\\d{5}");
        
	//<xs:complexType name="PhoneNumber">
        GlobalComplexType gct3 = Util.createGlobalComplexType(model, "PhoneNumber");
	//<xs:complexType name="Occupancy">
        GlobalComplexType gct4 = Util.createGlobalComplexType(model, "Occupancy");
	//<xs:complexType name="Residence">
        GlobalComplexType gct5 = Util.createGlobalComplexType(model, "Residence");
	//<xs:complexType name="Car">
        GlobalComplexType gct6 = Util.createGlobalComplexType(model, "Car");
	//<xs:complexType name="Duration">
        GlobalComplexType gct7 = Util.createGlobalComplexType(model, "Duration");
        model.endTransaction();
        //Util.dumpToFile(model.getBaseDocument(), new File("C:\\temp\\test.xml"));
        model = Util.dumpAndReloadModel(model);
        checkRead(model.getSchema());
    }
    
    public void testPrimitiveTypePrefix() throws Exception {
        SchemaModelImpl model = (SchemaModelImpl) Util.loadSchemaModel("resources/Empty_loanApp_prefix.xsd");
        Schema s = model.getSchema();
        SchemaComponentFactory factory = model.getFactory();

        GlobalElement ge1 = model.getFactory().createGlobalElement();
        ge1.setName("auto-loan-application");
        ge1.setType(factory.createGlobalReference(
                Util.getPrimitiveType("string"), GlobalSimpleType.class, ge1));
        model.startTransaction();
        model.getSchema().addElement(ge1);
        model.endTransaction();

        //Util.dumpToFile(model.getBaseDocument(), new File("c:/temp/test.xsd"));
        assertEquals("xs:string", ge1.getType().getRefString());
    }
}
