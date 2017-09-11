/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * 
 * Contributor(s):
 * 
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
 */

package org.netbeans.modules.xml.wizard;

import org.netbeans.modules.xml.wizard.XMLContentAttributes;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import junit.framework.TestCase;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.BaseKit;
import org.netbeans.modules.xml.schema.model.SchemaModel;

/**
 *
 * @author Sonali
 */
public class XMLGeneratorTest extends TestCase {
    
    private static String XSD_SCHEMA = "../resources/newXmlSchema.xsd";
    private static String XML_FILE = "../resources/newXmlSchema.xml";
    
    public XMLGeneratorTest(String testName) {
        super(testName);
    }            

    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }
    
     protected BaseDocument getDocument(String path) throws Exception {
        BaseDocument doc = getResourceAsDocument(path);
        //must set the language inside unit tests
      //  doc.putProperty(Language.class, XMLTokenId.language());
        return doc;
    }
                 
    protected static BaseDocument getResourceAsDocument(String path) throws Exception {
        InputStream in = XMLGeneratorTest.class.getResourceAsStream(path);
        BaseDocument sd = new BaseDocument(true, "text/xml"); //NOI18N
        BufferedReader br = new BufferedReader(new InputStreamReader(in,"UTF-8"));
        StringBuffer sbuf = new StringBuffer();
        try {
            String line = null;
            while ((line = br.readLine()) != null) {
                sbuf.append(line);
                sbuf.append(System.getProperty("line.separator"));
            }
        } finally {
            br.close();
        }
        sd.insertString(0,sbuf.toString(),null);
        return sd;
    }

    // TODO add test methods here. The name must begin with 'test'. For example:
    // public void testHello() {}
    
    public void testSimpleXMLGeneration() throws Exception {
        StringBuffer sb = new StringBuffer();
        URL url = XMLGeneratorTest.class.getResource(XSD_SCHEMA);
        File file = new File(url.toURI());
        SchemaModel model = TestCatalogModel.getDefault().getSchemaModel(url.toURI());  
        XMLContentAttributes attr = new XMLContentAttributes("ns0");
        XMLGeneratorVisitor visitor = new XMLGeneratorVisitor(file.getPath(), attr, sb );
        visitor.generateXML("root", model);
        
        BaseDocument doc = getDocument(XML_FILE);
        String str = doc.getText(0, doc.getLength());
        
        assertEquals(sb.toString().trim(), str.trim());
    }
    
    public void testNoNamespace() throws Exception {
        StringBuffer sb = new StringBuffer();
        URL url = XMLGeneratorTest.class.getResource("../resources/NNS.xsd");
        File file = new File(url.toURI());
        SchemaModel model = TestCatalogModel.getDefault().getSchemaModel(url.toURI());  
        XMLContentAttributes attr = new XMLContentAttributes("ns0");
        XMLGeneratorVisitor visitor = new XMLGeneratorVisitor(file.getPath(), attr, sb );
        visitor.generateXML("root", model);
        
        BaseDocument doc = getDocument("../resources/NNS.xml");
        String str = doc.getText(0, doc.getLength());
        
        assertEquals(sb.toString().trim(), str.trim());
    }
    
    public void testSameNameAsRootElement() throws Exception {
         StringBuffer sb = new StringBuffer();
        URL url = XMLGeneratorTest.class.getResource("../resources/SameNamesSchema.xsd");
        File file = new File(url.toURI());
        SchemaModel model = TestCatalogModel.getDefault().getSchemaModel(url.toURI());  
        XMLContentAttributes attr = new XMLContentAttributes("ns0");
        XMLGeneratorVisitor visitor = new XMLGeneratorVisitor(file.getPath(), attr, sb );
        visitor.generateXML("newElement", model);
        
        BaseDocument doc = getDocument("../resources/SameNamesSchema.xml");
        String str = doc.getText(0, doc.getLength());
        
        assertEquals(sb.toString().trim(), str.trim());
    }
    
    public void testLocalQualified() throws Exception {
        StringBuffer sb = new StringBuffer();
        URL url = XMLGeneratorTest.class.getResource("../resources/BothQualified.xsd");
        File file = new File(url.toURI());
        SchemaModel model = TestCatalogModel.getDefault().getSchemaModel(url.toURI());  
        XMLContentAttributes attr = new XMLContentAttributes("ns0");
        XMLGeneratorVisitor visitor = new XMLGeneratorVisitor(file.getPath(), attr, sb );
        visitor.generateXML("newElement", model);
        BaseDocument doc = getDocument("../resources/BothQualified.xml");
        String str = doc.getText(0, doc.getLength());
        assertEquals(sb.toString().trim(), str.trim());
        
        sb = new StringBuffer();
        url = XMLGeneratorTest.class.getResource("../resources/BothUnqualified.xsd");
        file = new File(url.toURI());
        model = TestCatalogModel.getDefault().getSchemaModel(url.toURI()); 
        visitor = new XMLGeneratorVisitor(file.getPath(), attr, sb );
        visitor.generateXML("newElement", model);
        doc = getDocument("../resources/BothUnqualified.xml");
        str = doc.getText(0, doc.getLength());
        System.out.println("STRING BUFFER");
        System.out.println(sb.toString());
        assertEquals(sb.toString().trim(), str.trim());
        
        sb = new StringBuffer();
        url = XMLGeneratorTest.class.getResource("../resources/ElementQualified.xsd");
        file = new File(url.toURI());
        model = TestCatalogModel.getDefault().getSchemaModel(url.toURI()); 
        visitor = new XMLGeneratorVisitor(file.getPath(), attr, sb );
        visitor.generateXML("newElement", model);
        doc = getDocument("../resources/ElementQualified.xml");
        str = doc.getText(0, doc.getLength());
        assertEquals(sb.toString().trim(), str.trim());
        
         sb = new StringBuffer();
        url = XMLGeneratorTest.class.getResource("../resources/AttrQualified.xsd");
        file = new File(url.toURI());
        model = TestCatalogModel.getDefault().getSchemaModel(url.toURI()); 
        visitor = new XMLGeneratorVisitor(file.getPath(), attr, sb );
        visitor.generateXML("newElement", model);
        doc = getDocument("../resources/AttrQualified.xml");
        str = doc.getText(0, doc.getLength());
        assertEquals(sb.toString().trim(), str.trim());
    }
    
    public void testGlobalQualified() throws Exception {
        StringBuffer sb = new StringBuffer();
        URL url = XMLGeneratorTest.class.getResource("../resources/GlobalElement.xsd");
        File file = new File(url.toURI());
        SchemaModel model = TestCatalogModel.getDefault().getSchemaModel(url.toURI());  
        XMLContentAttributes attr = new XMLContentAttributes("ns0");
        XMLGeneratorVisitor visitor = new XMLGeneratorVisitor(file.getPath(), attr, sb );
        visitor.generateXML("gElem2", model);
        BaseDocument doc = getDocument("../resources/GlobalElement.xml");
        String str = doc.getText(0, doc.getLength());
        assertEquals(sb.toString().trim(), str.trim());
    }

}
