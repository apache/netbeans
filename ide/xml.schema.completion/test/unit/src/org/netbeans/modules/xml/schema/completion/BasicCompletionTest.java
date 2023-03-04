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
package org.netbeans.modules.xml.schema.completion;

import java.util.List;
import junit.framework.*;
import org.netbeans.modules.xml.schema.completion.util.CompletionUtil;

/**
 *
 * @author Samaresh
 */
public class BasicCompletionTest extends AbstractTestCase {
    
    static final String PO_INSTANCE_DOCUMENT = "resources/PO.xml";
    static final String TEST_INSTANCE_DOCUMENT = "resources/Test.xml";
    
    public BasicCompletionTest(String testName) {
        super(testName);
    }
    
    public static Test suite() {
        TestSuite suite = new TestSuite();
        suite.addTest(new BasicCompletionTest("testAttributes1"));
        suite.addTest(new BasicCompletionTest("testNoNamespaceCompletion"));
        suite.addTest(new BasicCompletionTest("testNoNamespaceCompletion1"));
        suite.addTest(new BasicCompletionTest("testNoNamespaceCompletion2"));
//        Disabled as referenced XSD file were partly not donated by oracle to apache
//        suite.addTest(new BasicCompletionTest("testPurchaseOrder"));
//        suite.addTest(new BasicCompletionTest("testPurchaseOrder1"));
//        suite.addTest(new BasicCompletionTest("testPurchaseOrder2"));
//        suite.addTest(new BasicCompletionTest("testCompletionFilter1"));
//        suite.addTest(new BasicCompletionTest("testCompletionFilter2"));
        suite.addTest(new BasicCompletionTest("testEmptyTag1"));
        suite.addTest(new BasicCompletionTest("testEmptyTag2"));
        suite.addTest(new BasicCompletionTest("testEmptyTag3"));
        suite.addTest(new BasicCompletionTest("testEmptyTag4"));
//        Disabled as referenced XSD file were partly not donated by oracle to apache
//        suite.addTest(new BasicCompletionTest("testEndtagCompletion1"));
//        suite.addTest(new BasicCompletionTest("testEndtagCompletion2"));
//        suite.addTest(new BasicCompletionTest("testEndtagCompletion3"));
//        suite.addTest(new BasicCompletionTest("testCompletionWithAmpersand"));
  // disabled since it accesses Net:
  //      suite.addTest(new BasicCompletionTest("testSchemaFromRuntimeCatalog"));
        //suite.addTest(new BasicCompletionTest("testCompletionUsingSchemaFromCatalog"));
        suite.addTest(new BasicCompletionTest("testWildcard1"));
        suite.addTest(new BasicCompletionTest("testWildcard2"));
        suite.addTest(new BasicCompletionTest("testWildcard3"));
        suite.addTest(new BasicCompletionTest("testWildcard4"));
        //suite.addTest(new BasicCompletionTest("testWildcard5"));
        suite.addTest(new BasicCompletionTest("testChildren1"));
        suite.addTest(new BasicCompletionTest("testChildren2"));
        suite.addTest(new BasicCompletionTest("testImport1"));
        suite.addTest(new BasicCompletionTest("testInclude1"));
//        Disabled as referenced XSD file were partly not donated by oracle to apache
//        suite.addTest(new BasicCompletionTest("testElementValueCompletion1"));
//        suite.addTest(new BasicCompletionTest("testElementValueCompletion2"));
//        suite.addTest(new BasicCompletionTest("testAttributeValueCompletion1"));
//        suite.addTest(new BasicCompletionTest("testAttributeValueCompletion2"));        
        return suite;
    }
    
    /**
     * Query attributes at offset 217. See Attr1.xml.
     * Should fetch five attributes.
     */
    public void testAttributes1() throws Exception {
        setupCompletion("resources/Attr1.xml", null);
        List<CompletionResultItem> items = query(217);
        String[] expectedResult = {"attrA11", "attrA12", "attrA13", "attrA14", "attrA15"};
        assertResult(items, expectedResult);
    }
    
    /**
     * Queries elements from schema with no namespace.
     */
    public void testNoNamespaceCompletion() throws Exception {
        setupCompletion("resources/NoTNS.xml", null);
        List<CompletionResultItem> items = query(157);
        String[] expectedResult = {"NNSChild1", "NNSChild2"};
        assertResult(items, expectedResult);
    }
    
    /**
     * Queries elements from schema with no namespace.
     */
    public void testNoNamespaceCompletion1() throws Exception {
        setupCompletion("resources/NoTNS1.xml", null);
        List<CompletionResultItem> items = query(175);
        String[] expectedResult = {"NNSChild11", "NNSChild12"};
        assertResult(items, expectedResult);
    }
    
    /**
     * Queries elements from schema with no namespace.
     */
    public void testNoNamespaceCompletion2() throws Exception {
        setupCompletion("resources/NoTNS2.xml", null);
        List<CompletionResultItem> items = query(167);
        String[] expectedResult = {"Attr11", "Attr12"};
        assertResult(items, expectedResult);
    }
    
    /**
     * Queries elements from a PO schema.
     */
    public void testPurchaseOrder() throws Exception {
        setupCompletion("resources/PO.xml", null);
        List<CompletionResultItem> items = query(227);
        String[] expectedResult = {"po:shipTo", "po:billTo", "po:comment", "po:items"};
        assertResult(items, expectedResult);
    }
    
    /**
     * Queries elements from a PO schema.
     */
    public void testPurchaseOrder1() throws Exception {
        setupCompletion("resources/PO1.xml", null);
        List<CompletionResultItem> items = query(237);
        String[] expectedResult = {"po:name", "po:street", "po:city", "po:state", "po:zip"};
        assertResult(items, expectedResult);
    }
    
    /**
     * Queries elements from a PO schema.
     * Issue: http://www.netbeans.org/issues/show_bug.cgi?id=117841
     */
    public void testPurchaseOrder2() throws Exception {
        setupCompletion("resources/PO2.xml", null);
        List<CompletionResultItem> items = query(237);
        String[] expectedResult = null;
        assertResult(items, expectedResult);
    }
    
    /**
     * Tests completion filtering.
     */
    public void testCompletionFilter1() throws Exception {
        setupCompletion("resources/PO3.xml", null);
        List<CompletionResultItem> items = query(228);
        String[] expectedResult = {};
        assertResult(items, expectedResult);
    }
    
    
    /**
     * Tests completion filtering.
     */
    public void testCompletionFilter2() throws Exception {
        setupCompletion("resources/PO4.xml", null);
        List<CompletionResultItem> items = query(230);
        String[] expectedResult = {"po:shipTo"};
        assertResult(items, expectedResult);
    }
    
    /**
     * Tests end tag completion.
     */
    public void testEndtagCompletion1() throws Exception {
        setupCompletion("resources/PO5.xml", null);
        List<CompletionResultItem> items = query(240);
        String[] expectedResult = null;
        assertResult(items, expectedResult);
    }
    
    /**
     * Tests end tag completion.
     */
    public void testEndtagCompletion2() throws Exception {
        setupCompletion("resources/PO6.xml", null);
        List<CompletionResultItem> items = query(274);
        String[] expectedResult = {};
        assertResult(items, expectedResult);
    }
    
    /**
     * Tests end tag completion.
     */
    public void testEndtagCompletion3() throws Exception {
        setupCompletion("resources/PO7.xml", null);
        List<CompletionResultItem> items = query(261);
        String[] expectedResult = {};
        assertResult(items, expectedResult);
    }
    
    /**
     * Tests completion with ampersand.
     * See http://www.netbeans.org/issues/show_bug.cgi?id=135379.
     */
    public void testCompletionWithAmpersand() throws Exception {
        setupCompletion("resources/PO8.xml", null);
        List<CompletionResultItem> items = query(260);
        String[] expectedResult = {"po:name","po:street","po:city","po:state","po:zip"};
        assertResult(items, expectedResult);
    }
    
    /**
     * Queries an empty tag. If cursor is between < and A:A31 in <A:A31 />
     * we should show all qualifying elements that are children of the parent.
     */
    public void testEmptyTag1() throws Exception {
        // see issue #196598
        setupCompletion("resources/EmptyTag.xml", null);
        List<CompletionResultItem> items = query(220);
        String[] expectedResult = {"A:A31", "A:A32"};
        assertResult(items, expectedResult);
    }
    
    /**
     * Queries an empty tag. If cursor is after second 'A' in <A:A31 />
     * we should show all elements beginning with this prefix.
     */
    public void testEmptyTag2() throws Exception {
        // see issue #196598
        setupCompletion("resources/EmptyTag.xml", null);
        List<CompletionResultItem> items = query(223);
        String[] expectedResult = {"A:A31", "A:A32"};
        assertResult(items, expectedResult);
    }
    
    /**
     * Queries attributes inside an empty tag.
     */
    public void testEmptyTag3() throws Exception {
        setupCompletion("resources/EmptyTag.xml", null);
        List<CompletionResultItem> items = query(226);
        String[] expectedResult = {"attrA31", "attrA32"};
        assertResult(items, expectedResult);
    }
    
    /**
     * Issue: 108634, shouldn't show attributes after empty tag.
     */
    public void testEmptyTag4() throws Exception {
        setupCompletion("resources/EmptyTag.xml", null);
        List<CompletionResultItem> items = query(229);
        String[] expectedResult = {"A:A31", "A:A32"};
        assertResult(items, expectedResult);
    }
    
    public void testSchemaFromRuntimeCatalog() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //offset=39
        buffer.append("<persistence xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"); //offset=67
        buffer.append("  xmlns=\"http://java.sun.com/xml/ns/persistence\"\n"); //offset=49
        buffer.append("  xsi:schemaLocation=\"http://java.sun.com/xml/ns/persistence http://java.sun.com/xml/ns/persistence/persistence_1_0.xsd\">\n"); //offset=122
        buffer.append("  <persistence-unit>\n"); //offset=21
        buffer.append("  <\n"); //offset=06
        buffer.append("  <persistence-unit>\n"); //offset=21
        buffer.append("</persistence>");
        setupCompletion(TEST_INSTANCE_DOCUMENT, buffer);
        List<CompletionResultItem> items = query(304);
//        String[] expectedResult = {"C:rootC1", "C:rootC1","B:rootB1", "B:rootB2", "A:rootA1",
//            "A:rootA2", "A:rootA3", "A:rootA3", "A:A21", "A:A22"};
//        assertResult(items, expectedResult);
    }
    
    public void testCompletionUsingSchemaFromCatalog() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //offset=39
        buffer.append("<web-app version=\"2.5\"\n"); //offset=23
        buffer.append("    xmlns=\"http://java.sun.com/xml/ns/javaee\"\n"); //offset=46
        buffer.append("    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"); //offset=58
        buffer.append("    xsi:schemaLocation=\"http://java.sun.com/xml/ns/javaee http://java.sun.com/xml/ns/javaee/web-app_2_5.xsd\">\n"); //offset=110
        buffer.append("    <\n"); //offset=6
        buffer.append("</web-app>");
        setupCompletion(TEST_INSTANCE_DOCUMENT, buffer);
        List<CompletionResultItem> items = query(280);
//        String[] expectedResult = {"A:rootA1", "A:rootA2", "A:rootA3", "A:A11", "A:A12"};
//        assertResult(items, expectedResult);
    }
    
    public void testWildcard1() throws Exception {
        setupCompletion("resources/WildCard1.xml", null);
        List<CompletionResultItem> items = query(221);
        String[] expectedResult = {"A:rootA1", "A:rootA2", "A:rootA3", "A:A11", "A:A12"};
        assertResult(items, expectedResult);
    }
    
    public void testWildcard2() throws Exception {
        setupCompletion("resources/WildCard2.xml", null);
        List<CompletionResultItem> items = query(265);
        String[] expectedResult = {"ns1:rootB1", "ns1:rootB2", "A:rootA1", "A:rootA2",
        "A:rootA3", "A:A11", "A:A12"};
        assertResult(items, expectedResult);
    }
    
    public void testWildcard3() throws Exception {
        setupCompletion("resources/WildCard3.xml", null);
        //query at 405
        List<CompletionResultItem> items = query(405);
        String[] expectedResult = {"C:rootC1", "C:rootC1","B:rootB1", "B:rootB2",
        "A:rootA1", "A:rootA2", "A:rootA3", "A:A11", "A:A12"};
        assertResult(items, expectedResult);
        
        //query at 447
        items = query(447);
        String[] expectedResult1 = {"C:rootC1", "C:rootC2","B:rootB1", "B:rootB2", "A:rootA1",
            "A:rootA2", "A:rootA3", "A:A11", "A:A12"};        
        assertResult(items, expectedResult1);
        
        //query at 494
        items = query(494);
        String[] expectedResult2 = {"B:B11", "B:B12"};
        assertResult(items, expectedResult2);
    }
    
    public void testWildcard4() throws Exception {
        setupCompletion("resources/WildCard4.xml", null);
        List<CompletionResultItem> items = query(405);
        String[] expectedResult = {"C:rootC1", "C:rootC1","B:rootB1", "B:rootB2", "A:rootA1",
            "A:rootA2", "A:rootA3", "A:rootA3", "A:A21", "A:A22"};
        assertResult(items, expectedResult);
    }
    
//    public void testWildcard5() throws Exception {
//        setupCompletion("resources/Camera1.xml", null);
//        //query at 376
//        List<CompletionResultItem> items = query(376);
//        String[] expectedResult1 = {"n:body", "n:lens", "n:manulaAdapter"};
//        assertResult(items, expectedResult1);        
//        
//        //query at 404
//        items = query(404);
//        String[] expectedResult2 = {"c:body", "c:lens", "c:manulaAdapter"};
//        assertResult(items, expectedResult2);
//    }
    
    
    public void testChildren1() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //offset=39
        buffer.append("<ns0:component xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"); //offset=69
        buffer.append("  xmlns:ns0=\"http://xml.netbeans.org/schema/newXMLSchema\"\n"); //offset=58
        buffer.append("  xsi:schemaLocation=\"http://xml.netbeans.org/schema/newXMLSchema Test.xsd\">\n"); //offset=77
        buffer.append("  <ns0:uninstallList>\n"); //offset 22
        buffer.append("  <\n"); //offset 4
        buffer.append("  </ns0:uninstallList>\n");
        buffer.append("</ns0:component>\n");
        setupCompletion(TEST_INSTANCE_DOCUMENT, buffer);
        List<CompletionResultItem> items = query(269);
        String[] expectedResult = {"ns0:uninstallSteps"};
        assertResult(items, expectedResult);
    }

    public void testChildren2() throws Exception {
        StringBuffer buffer = new StringBuffer();
        buffer.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"); //offset=39
        buffer.append("<ns0:component xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n"); //offset=69
        buffer.append("  xmlns:ns0=\"http://xml.netbeans.org/schema/newXMLSchema\"\n"); //offset=58
        buffer.append("  xsi:schemaLocation=\"http://xml.netbeans.org/schema/newXMLSchema Test.xsd\">\n"); //offset=77
        buffer.append("  <ns0:installList/>\n"); //offset 20
        buffer.append("  <\n"); //offset 4
        buffer.append("</ns0:component>\n");
        setupCompletion(TEST_INSTANCE_DOCUMENT, buffer);
        List<CompletionResultItem> items = query(267);
        String[] expectedResult = {"ns0:installList","ns0:uninstallList"};
        assertResult(items, expectedResult);
    }
    
    public void testImport1() throws Exception {
        setupCompletion("resources/Import.xml", null);
        List<CompletionResultItem> items = query(246);
        String[] expectedResult = {"ns1:A1","ns1:A2"};
        assertResult(items, expectedResult);
    }
    
    public void testInclude1() throws Exception {
        setupCompletion("resources/Include.xml", null);
        List<CompletionResultItem> items = query(233);
        String[] expectedResult = {"ns:M1","ns:M2"};
        assertResult(items, expectedResult);
    }
    
    /**
     * Queries elements values for element "city".
     * Result should be 0.
     */
    public void testElementValueCompletion1() throws Exception {
        setupCompletion("resources/PO9.xml", null);
        List<CompletionResultItem> items = query(309);
        assert(items.size() == 0); //50 states
    }    

    /**
     * Queries elements values for element "state".
     * Result should be 50.
     */
    public void testElementValueCompletion2() throws Exception {
        setupCompletion("resources/PO9.xml", null);
        List<CompletionResultItem> items = query(332);
        assert(items.size() == 50); //50 states
    }    
    
    /**
     * Queries attribute values for attribute "partNum".
     * Result should be 0.
     */
    public void testAttributeValueCompletion1() throws Exception {
        setupCompletion("resources/PO9.xml", null);
        List<CompletionResultItem> items = query(588);
        assert(items.size() == 0);
    }    
    
    /**
     * Queries attribute values for attribute "brand".
     * Result should be 6.
     */
    public void testAttributeValueCompletion2() throws Exception {
        setupCompletion("resources/PO9.xml", null);
        String[] expectedResult = {"ACER", "HP", "NOKIA", "SAMSUNG", "SONY","SUN"};
        List<CompletionResultItem> items = query(597);
        assert(items.size() == 6);
        assertResult(items, expectedResult);
    }
}
