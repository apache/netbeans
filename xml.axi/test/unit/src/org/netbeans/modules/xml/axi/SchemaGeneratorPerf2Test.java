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
import org.netbeans.modules.xml.schema.model.LocalElement;
import org.netbeans.modules.xml.schema.model.SchemaModel;
import org.netbeans.modules.xml.xam.dom.AbstractDocumentModel;


/**
 *
 * @author Ayub Khan
 */
public class SchemaGeneratorPerf2Test extends AbstractTestCase {
    
    public static final String TEST_XSD         = "resources/LoanApplication.xsd";
    public static final String GLOBAL_ELEMENT   = "autoLoanApplication";
    
    private Document doc = null;
    
    public SchemaGeneratorPerf2Test(String testName) {
        super(testName, TEST_XSD, GLOBAL_ELEMENT);
    }
    
    protected void setUp() throws Exception {
        super.setUp();
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new SchemaGeneratorPerf2Test("testGenerateSchema")); // NOI18N
        suite.addTest(new SchemaGeneratorPerf2Test("testGenerateSchema2")); // NOI18N
        suite.addTest(new SchemaGeneratorPerf2Test("testDeleteExistingGlobalElement")); // NOI18N
        
        return suite;
    }
    
    public void testGenerateSchema() throws Exception {
        Element element = globalElement;
        assertNotNull(element);
        SchemaModel sm = null;
        sm = getSchemaModel();
        doc = ((AbstractDocumentModel)sm).getBaseDocument();
        //global element name change
        axiModel.startTransaction();
        for(Element e:axiModel.getRoot().getElements())
            if(e.getName().equals(GLOBAL_ELEMENT))
                e.setName(e.getName()+"_");
        long startTime = System.currentTimeMillis();
        axiModel.endTransaction();
        long endTime = System.currentTimeMillis();
        print("Time taken to flush: "+(endTime-startTime)+" ms");
        validateSchema(axiModel.getSchemaModel());
    }
    
    public void testGenerateSchema2() {
        assertEquals("global elements",1,getSchemaModel().getSchema().getElements().size());
        Element element = axiModel.getComponentFactory().createElement();
        element.setName("MyElement");
        
        axiModel.startTransaction();
        try {
            for(Element e:axiModel.getRoot().getElements())
                if(e.getName().equals(GLOBAL_ELEMENT+"_"))
                    e.getCompositor().addElement(element);
        } finally {
            axiModel.endTransaction();
        }
        assertEquals("global elements",1,getSchemaModel().getSchema().getElements().size());
        assertEquals("global elements","MyElement",((LocalElement)getSchemaModel().getSchema().getElements().iterator().next().//autoLoanApp
                getChildren().get(1).//complexType
                getChildren().get(0).//sequence
                getChildren().get(7)).getName());//NewElement
        validateSchema(axiModel.getSchemaModel());
//		try {
//			SchemaModel sm = getSchemaModel();
//			doc = ((AbstractDocumentModel)sm).getBaseDocument();
//			print("doc: "+doc.getText(0, doc.getLength()));
//		} catch (BadLocationException ex) {
//			ex.printStackTrace();
//		}
        
        axiModel.startTransaction();
        try {
            for(Element e:axiModel.getRoot().getElements())
                if(e.getName().equals(GLOBAL_ELEMENT+"_"))
                    e.getCompositor().removeElement(element);
        } finally {
            axiModel.endTransaction();
        }
        assertEquals("global elements",1,getSchemaModel().getSchema().getElements().size());
        validateSchema(axiModel.getSchemaModel());
//      try {
//          SchemaModel sm = getSchemaModel();
//          doc = ((AbstractDocumentModel)sm).getBaseDocument();
//          print("doc: "+doc.getText(0, doc.getLength()));
//      } catch (BadLocationException ex) {
//          ex.printStackTrace();
//      }
    }
    
    public void testDeleteExistingGlobalElement() {
        assertEquals("global elements",1,getSchemaModel().getSchema().getElements().size());
        Element element = axiModel.getComponentFactory().createElement();
        element.setName("NewElement"+axiModel.getRoot().getElements().size());
        //global element name change
        axiModel.startTransaction();
        try {
            for(Element e:axiModel.getRoot().getElements())
                if(e.getName().equals(GLOBAL_ELEMENT+"_"))
                    axiModel.getRoot().removeElement(e);
        } finally {
            axiModel.endTransaction();
        }
        assertEquals("global elements",0,getSchemaModel().getSchema().getElements().size());
        validateSchema(axiModel.getSchemaModel());
//      try {
//          SchemaModel sm = getSchemaModel();
//          doc = ((AbstractDocumentModel)sm).getBaseDocument();
//          print("doc: "+doc.getText(0, doc.getLength()));
//      } catch (BadLocationException ex) {
//          ex.printStackTrace();
//      }
    }
}
