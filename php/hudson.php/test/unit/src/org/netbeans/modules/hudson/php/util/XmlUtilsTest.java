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
package org.netbeans.modules.hudson.php.util;

import java.io.File;
import org.netbeans.junit.NbTestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class XmlUtilsTest extends NbTestCase {

    public XmlUtilsTest(String name) {
        super(name);
    }

    public void testParse() throws Exception {
        Document document = XmlUtils.parse(new File(getDataDir(), "persons.xml"));
        Element documentElement = document.getDocumentElement();
        assertEquals("Document element should be found", "persons", documentElement.getNodeName());
        NodeList persons = documentElement.getElementsByTagName("person");
        assertEquals("Persons should be found", 2, persons.getLength());
    }

    public void testQuery() throws Exception {
        Document document = XmlUtils.parse(new File(getDataDir(), "persons.xml"));
        assertNotNull(XmlUtils.query(document, "//person[@id='1']"));
        assertNull(XmlUtils.query(document, "//person[@id='-1']"));
        assertNull(XmlUtils.query(document, "count(//person)"));
    }

    public void testCommentNode() throws Exception {
        final String xpath = "//person[@id='1']/name";
        final String comment = "<!--<name>John Doe</name>-->";
        Document document = XmlUtils.parse(new File(getDataDir(), "persons.xml"));
        Node node = XmlUtils.query(document, xpath);
        assertNotNull(node);
        assertFalse(XmlUtils.asString(document, true).indexOf(comment) != -1);
        XmlUtils.commentNode(document, node);
        assertNull(XmlUtils.query(document, xpath));
        assertTrue(XmlUtils.asString(document, true).indexOf(comment) != -1);
    }

    public void testAsString() throws Exception {
        final String header = "<?xml";
        Document document = XmlUtils.parse(new File(getDataDir(), "persons.xml"));
        String content = XmlUtils.asString(document, true);
        assertTrue(content.startsWith(header));
        Node node = XmlUtils.query(document, "//person[@id='1']");
        content = XmlUtils.asString(node, true);
        assertFalse(content.startsWith(header));
    }

    public void testNodeValues() throws Exception {
        Document document = XmlUtils.parse(new File(getDataDir(), "persons.xml"));
        Node disabled = XmlUtils.query(document, "/persons/disabled");
        assertNotNull(disabled);
        assertEquals("true", XmlUtils.getNodeValue(document, disabled));
        XmlUtils.setNodeValue(document, disabled, "false");
        assertEquals("false", XmlUtils.getNodeValue(document, disabled));
    }

}
