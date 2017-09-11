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
        suite.addTest(new AXIModelTest("testAXIModel"));
        suite.addTest(new AXIModelTest("testAXIModelForMetaSchema"));
        suite.addTest(new AXIModelTest("testRecursiveResolve1"));
        suite.addTest(new AXIModelTest("testRecursiveResolve2"));
        suite.addTest(new AXIModelTest("testSubstitutionGroup"));
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
