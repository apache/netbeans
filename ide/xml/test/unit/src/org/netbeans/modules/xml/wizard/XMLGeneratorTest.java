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

package org.netbeans.modules.xml.wizard;

import org.netbeans.modules.xml.wizard.XMLContentAttributes;
import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
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
        BufferedReader br = new BufferedReader(new InputStreamReader(in, StandardCharsets.UTF_8));
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
