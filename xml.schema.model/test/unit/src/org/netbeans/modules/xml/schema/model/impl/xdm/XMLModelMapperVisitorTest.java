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

/*
 * XMLModelMapperVisitorTest.java
 * JUnit based test
 *
 * Created on October 31, 2005, 11:06 AM
 */

package org.netbeans.modules.xml.schema.model.impl.xdm;

import junit.framework.*;
import org.netbeans.modules.xml.schema.model.Schema;
import org.netbeans.modules.xml.schema.model.SchemaComponent;
import org.netbeans.modules.xml.schema.model.Util;
import org.netbeans.modules.xml.schema.model.impl.SchemaModelImpl;
import org.netbeans.modules.xml.schema.model.visitor.FindSchemaComponentFromDOM;
import org.netbeans.modules.xml.xdm.nodes.Document;
import org.netbeans.modules.xml.xdm.nodes.Element;
//import org.w3c.dom.Document;
//import org.w3c.dom.Element;
/**
 *
 * @author Administrator
 */
public class XMLModelMapperVisitorTest extends TestCase {
    
    public static final String TEST_XSD     = "resources/PurchaseOrder.xsd";
    
    private SchemaModelImpl model;
    private Schema schema;
    private Document doc;
    private FindSchemaComponentFromDOM instance;
    
    public XMLModelMapperVisitorTest(String testName) {
        super(testName);
    }
    
    protected void setUp() throws Exception {
        model = (SchemaModelImpl)Util.loadSchemaModel(TEST_XSD);
        schema = model.getSchema();
        doc = (org.netbeans.modules.xml.xdm.nodes.Document)model.getDocument();
        instance = new FindSchemaComponentFromDOM();
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite(XMLModelMapperVisitorTest.class);
        
        return suite;
    }
    
    /**
     * Test of findComponent method, of class org.netbeans.modules.xml.schema.model.visitor.XMLModelMapperVisitor.
     */
    public void testFindComponent() {
        System.out.println("findComponent");
        
        Element poElement = (Element)doc.getDocumentElement().getChildNodes().item(1);
        SchemaComponent poComponent = schema.getChildren().get(0);
        SchemaComponent result = instance.findComponent(schema, poElement);
        assertEquals(poComponent, result);
        
        Element poTypeElement = (Element)doc.getDocumentElement().getChildNodes().item(5);
        SchemaComponent poGlobalType = schema.getChildren().get(2);
        result = instance.findComponent(schema, poTypeElement);
        assertEquals(poGlobalType, result);

        Element shiptoElement = (Element)doc.getDocumentElement().getChildNodes().item(5).
                getChildNodes().item(1).getChildNodes().item(1);
        SchemaComponent shiptoComponent = poGlobalType.getChildren().get(0).getChildren().get(0);
        result = instance.findComponent(schema, shiptoElement);
        assertEquals(shiptoComponent, result);
    }
    
}
