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

package org.netbeans.modules.xml.xdm.visitor;
import java.util.List;
import junit.framework.*;
import org.netbeans.modules.xml.xdm.Util;
import org.netbeans.modules.xml.xdm.XDMModel;
import org.netbeans.modules.xml.xdm.nodes.Attribute;
import org.netbeans.modules.xml.xdm.nodes.Document;
import org.netbeans.modules.xml.xdm.nodes.Element;
import org.netbeans.modules.xml.xdm.nodes.Node;

/**
 *
 * @author nn136682
 */
public class XPathFinderTest extends TestCase {
    
    public XPathFinderTest(String testName) {
        super(testName);
    }

    private Document doc;
    protected void setUp() throws Exception {
        doc = Util.loadXdmDocument("visitor/address.xsd");
    }

    protected void tearDown() throws Exception {
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(XPathFinderTest.class);
        
        return suite;
    }

    public void testFindAndGetXpath() throws Exception {
        String expr = "/company/employee[1]";
        Document root = Util.loadXdmDocument("test.xml");
        Node n = new XPathFinder().findNode(root, expr);
        assertNotNull(n);
        
        String xpathString = XPathFinder.getXpath(root, n);
        assertEquals("getXpath check round-trip", expr, xpathString);
    }
    
    public void testFindNodeWithIndex() throws Exception {
        String expr = "/schema/complexType[2]";
        Node n = new XPathFinder().findNode(doc, expr);
        assertNotNull(n);
        assertEquals("attribute name=", "US-Address", n.getAttributes().getNamedItem("name").getNodeValue());
        
        String xpathString = XPathFinder.getXpath(doc, n);
        assertEquals("checking round-trip", expr, xpathString);
    }
    
    public void testSelectionByAttribute() throws Exception {
        String expr = "/schema/simpleType[@name='US-State']/restriction/enumeration";
        List<Node> nodes = new XPathFinder().findNodes(doc, expr);
        assertEquals("testSelectionByAttribute", 54, nodes.size());
        
        String xpathString = XPathFinder.getXpath(doc, nodes.get(50));
        assertEquals("testSelectionByAttribute.2", "/schema/simpleType[1]/restriction[1]/enumeration[51]", xpathString);
    }

    public void testFindAttributeNode() throws Exception {
        String expr = "/schema/complexType[2]/complexContent[1]/extension[1]/@base";
        Node n = new XPathFinder().findNode(doc, expr);
        assertTrue("Attribute node", n instanceof Attribute);

        Attribute attr = (Attribute) n;
        assertEquals("Attribute value", "ipo:Address", attr.getNodeValue());
        
        String xpathString = XPathFinder.getXpath(doc, n);
        assertEquals("checking round-trip", expr, xpathString);
    }
    
    public void testWithDefaultNamespaceAndRootElementPrefix() throws Exception {
        Document root = Util.loadXdmDocument("visitor/ipo.xml");
        String expr = "/ipo:purchaseOrder/shipTo[1]/street[1]";
        Node n = new XPathFinder().findNode(root, expr);
        assertEquals("no default namespace", "street", n.getNodeName());
        
        String xpathString = XPathFinder.getXpath(root, n);
        assertEquals("checking round-trip", expr, xpathString);
    }
     
    public void testNonEmptyDefaultNamspacePrefix() throws Exception {
        Document root = Util.loadXdmDocument("visitor/OrgChart.xsd");
        String expr = "/xsd:schema/xsd:complexType[1]/xsd:sequence[1]/xsd:element[1]";
        Node n = new XPathFinder().findNode(root, expr);

        String xpathString = XPathFinder.getXpath(root, n);
        assertEquals("checking round-trip", expr, xpathString);
    }

    public void testMixedPrefixesForSameNamespace() throws Exception {
        Document root = Util.loadXdmDocument("visitor/Invoke1parent.bpel");
        String expr = "/bpel_20:process[1]/bpel_20:sequence[1]/bpel_20:receive[1]";
        Element n = (Element) new XPathFinder().findNode(root, expr);
        assertEquals("MyRole.Invoke1parentOperation.Receive", n.getAttribute("name"));
        String xpathString = XPathFinder.getXpath(root, n);
        String expected = "/bpel_20:process/sequence[1]/bpel_20:receive[1]";
        assertEquals("checking round-trip", expected, xpathString);
    }

    public void testMixedPrefixesForSameNamespace2() throws Exception {
        Document root = Util.loadXdmDocument("visitor/Invoke1parent.bpel");
        String expr = "/bpel_20:process[1]/sequence/receive[2]";
        Element n = (Element) new XPathFinder().findNode(root, expr);
        assertEquals("Invoke1parentOperation1", n.getAttribute("name"));
        String xpathString = XPathFinder.getXpath(root, n);
        String expected = "/bpel_20:process/sequence[1]/receive[2]";
        assertEquals("checking round-trip", expected, xpathString);
    }

    private XDMModel xmlModel = null;
}
