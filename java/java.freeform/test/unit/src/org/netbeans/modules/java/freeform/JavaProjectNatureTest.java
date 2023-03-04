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

package org.netbeans.modules.java.freeform;

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import org.netbeans.junit.NbTestCase;
import org.openide.xml.XMLUtil;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.InputSource;

/**
 * Test stuff in JavaProjectNature.
 * @author Jesse Glick
 */
public class JavaProjectNatureTest extends NbTestCase {

    public JavaProjectNatureTest(String name) {
        super(name);
    }

    public void testUpgradeSchema1to2() throws Exception {
        upgradeSchemaTestImpl(JavaProjectNature.NS_JAVA_1, JavaProjectNature.NS_JAVA_2);
    }

    public void testUpgradeSchema2to3() throws Exception {
        upgradeSchemaTestImpl(JavaProjectNature.NS_JAVA_2, JavaProjectNature.NS_JAVA_3);
    }

    public void testUpgradeSchema1to3() throws Exception {
        upgradeSchemaTestImpl(JavaProjectNature.NS_JAVA_1, JavaProjectNature.NS_JAVA_3);
    }
    
    public void testUpgradeSchema1to4() throws Exception {
        upgradeSchemaTestImpl(JavaProjectNature.NS_JAVA_1, JavaProjectNature.NS_JAVA_4);
    }
    
    public void testUpgradeSchema2to4() throws Exception {
        upgradeSchemaTestImpl(JavaProjectNature.NS_JAVA_2, JavaProjectNature.NS_JAVA_4);
    }
    
    public void testUpgradeSchema3to4() throws Exception {
        upgradeSchemaTestImpl(JavaProjectNature.NS_JAVA_3, JavaProjectNature.NS_JAVA_4);
    }

    private void upgradeSchemaTestImpl(String from, String to) throws Exception {
        // Formatting has to be the same as Xerces' formatter produces for this test to pass:
        String xml1 = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                      "<java-data xmlns=\""+from+"\">\n" +
                      "    <!-- Hello there. -->\n" +
                      "    <foo bar=\"baz\" quux=\"whatever\">hello</foo>\n" +
                      "    <x>OK</x>\n" +
                      "</java-data>\n";
        String xml2expected = xml1.replaceAll(from, to);
        Document doc1 = XMLUtil.parse(new InputSource(new StringReader(xml1)), false, true, null, null);
        Element el1 = doc1.getDocumentElement();
        Element el2 = LookupProviderImpl.upgradeSchema(el1,to);
        Document doc2 = XMLUtil.createDocument(JavaProjectNature.EL_JAVA, to, null, null);
        doc2.removeChild(doc2.getDocumentElement());
        doc2.appendChild(doc2.importNode(el2, true));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLUtil.write(doc2, baos, "UTF-8");
        String xml2actual = baos.toString("UTF-8").replaceAll(System.getProperty("line.separator"), "\n");
        assertEquals("Correct upgrade result", xml2expected, xml2actual);
    }
}
