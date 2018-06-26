/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
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
