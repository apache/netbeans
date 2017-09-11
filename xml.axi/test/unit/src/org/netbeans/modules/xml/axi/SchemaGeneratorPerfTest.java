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

import javax.swing.text.Document;
import junit.framework.*;
import org.netbeans.modules.xml.schema.model.AttributeGroupReference;
import org.netbeans.modules.xml.schema.model.Choice;
import org.netbeans.modules.xml.schema.model.GlobalAttributeGroup;
import org.netbeans.modules.xml.schema.model.GlobalComplexType;
import org.netbeans.modules.xml.schema.model.GlobalElement;
import org.netbeans.modules.xml.schema.model.LocalAttribute;
import org.netbeans.modules.xml.schema.model.LocalComplexType;
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.schema.model.SimpleExtension;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;


/**
 *
 * @author Ayub Khan
 */
public class SchemaGeneratorPerfTest extends AbstractTestCase {
    
    public static final String TEST_XSD         = "resources/OTA_TI_simple.xsd";//"resources/OTA_TravelItinerary.xsd";
    public static final String GLOBAL_ELEMENT   = "OTA_TravelItineraryRS";
    
    private Document doc = null;
    
    public SchemaGeneratorPerfTest(String testName) {
        super(testName, TEST_XSD, GLOBAL_ELEMENT);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(SchemaGeneratorPerfTest.class);
        
        return suite;
    }
    
    public void testGenerateSchema() throws Exception {
        generateSchema();
    }
    
    /**
     * Test of createElement method, of class org.netbeans.modules.xml.axi.XAMFactory.
     */
    public void generateSchema() throws Exception {
        Element element = globalElement;
        assertNotNull(element);
        SchemaModel sm = null;
        sm = getSchemaModel();
        doc = ((AbstractDocumentModel)sm).getBaseDocument();
        //DefaultSchemaGenerator schemaGenerator = new DefaultSchemaGenerator(getAXIModel());
        
        //global element name change
        axiModel.startTransaction();
        for(Element e:axiModel.getRoot().getElements())
            if(e.getName().equals("CancellationStatus"))
                e.setName(e.getName()+"_");
        long startTime = System.currentTimeMillis();
        axiModel.endTransaction();
        long endTime = System.currentTimeMillis();
        print("Time taken to flush: "+(endTime-startTime)+" ms");
//			for(GlobalElement ge:sm.getSchema().getElements()) {
//				if(ge.getName().startsWith("CancellationStatus")) {
//					assertEquals("updated schemamodel", ge.getName(), "CancellationStatus_");
//					assertEquals("updated schemamodel type", ge.getType().getQName().getLocalPart(), "boolean");
//				}
//			}
        //print("doc: "+doc.getText(0, doc.getLength()));
        
        //Local element name change
                    /*axiModel.startTransaction();
                    for(Element e:axiModel.getElements()) {
                            if(e.getName().equals("OTA_TravelItineraryRS")) {
                                    for(AXIComponent e2: e.getCompositor().getChildren()) {
                                            if(e2 instanceof Element) {
                                                    if(((Element)e2).getName().equals("Errors"))
                                                            ((Element)e2).setName(((Element)e2).getName()+"_");
                                            }
                                    }
                            }
                    }
                    axiModel.endTransaction();
                     
                    boolean found = false;
                    for(GlobalElement ge:sm.getSchema().getElements()) {
                            if(ge.getName().startsWith("OTA_TravelItineraryRS")) {
                                    assertEquals("updated schemamodel",
                                            ((LocalElement)ge.getChildren().get(0).getChildren().get(0).
                                                    getChildren().get(1)).getName(), "Errors_");
                                    found = true;
                            }
                    }
                    assertTrue("Should have verified updated element", found);*/
        
        //check an attribute change is flushed to schema
        axiModel.startTransaction();
        for(Element e:axiModel.getRoot().getElements()) {
            if(e.getName().equals(GLOBAL_ELEMENT)) {
                for(AXIComponent e2: e.getCompositor().getChildren()) {
                    if(e2 instanceof Element) {
                        if(((Element)e2).getName().equals("Errors")) {
                            AbstractAttribute attr = ((Element)((Element)e2).getChildren().get(0).getChildren().get(0)).getAttributes().get(0);
                            if(attr instanceof Attribute) {
                                ((Attribute)attr).setName("XYZ");
                            }
                        }
                    }
                }
            }
        }
        startTime = System.currentTimeMillis();
        axiModel.endTransaction();
        endTime = System.currentTimeMillis();
        print("Time taken to flush: "+(endTime-startTime)+" ms");
        
        boolean found = false;
        for(GlobalElement ge:sm.getSchema().getElements()) {
            if(ge.getName().startsWith(GLOBAL_ELEMENT)) {
                LocalComplexType lct = (LocalComplexType) ge.getChildren().get(1);
                Choice choice = (Choice) lct.getChildren().get(0);
                LocalElement le = (LocalElement) choice.getChildren().get(1);
                GlobalComplexType gct = (GlobalComplexType) le.getType().get();
                le = (LocalElement) gct.getChildren().get(1).getChildren().get(0);
                gct = (GlobalComplexType) le.getType().get();
                SimpleExtension se = (SimpleExtension) gct.getChildren().get(1).getChildren().get(0);
                gct = (GlobalComplexType)se.getBase().get();
                AttributeGroupReference agr = (AttributeGroupReference)gct.getChildren().get(1).getChildren().get(0).getChildren().get(0);
                GlobalAttributeGroup gag = agr.getGroup().get();
                LocalAttribute la = (LocalAttribute)gag.getChildren().get(1);
                assertEquals("updated schemamodel", "XYZ", la.getName());
                found = true;
            }
        }
        assertTrue("Should have verified updated element", found);
        validateSchema(axiModel.getSchemaModel());
    }
    
    public void testGenerateSchema2() {
        assertEquals("global elements",SchemaGeneratorTest.GE_SIZE,getSchemaModel().getSchema().getElements().size());
        Element element = axiModel.getComponentFactory().createElement();
        element.setName("NewElement"+axiModel.getRoot().getElements().size());
        
        axiModel.startTransaction();
        try {
            axiModel.getRoot().addElement(element);
        } finally {
            axiModel.endTransaction();
        }
        assertEquals("global elements",SchemaGeneratorTest.GE_SIZE+1,getSchemaModel().getSchema().getElements().size());
        
//		try {
//			SchemaModel sm = getSchemaModel();
//			doc = ((AbstractDocumentModel)sm).getBaseDocument();
//			print("doc: "+doc.getText(0, doc.getLength()));
//		} catch (BadLocationException ex) {
//			ex.printStackTrace();
//		}
        
        axiModel.startTransaction();
        try {
            axiModel.getRoot().removeElement(element);
        } finally {
            axiModel.endTransaction();
        }
        assertEquals("global elements",SchemaGeneratorTest.GE_SIZE,getSchemaModel().getSchema().getElements().size());
        validateSchema(axiModel.getSchemaModel());
//		try {
//			SchemaModel sm = getSchemaModel();
//			doc = ((AbstractDocumentModel)sm).getBaseDocument();
//			print("doc: "+doc.getText(0, doc.getLength()));
//		} catch (BadLocationException ex) {
//			ex.printStackTrace();
//		}
    }
    
}
