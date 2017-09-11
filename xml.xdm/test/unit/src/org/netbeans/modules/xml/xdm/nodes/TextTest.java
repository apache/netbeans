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
 * TextTest.java
 * JUnit based test
 *
 * Created on October 21, 2005, 2:21 PM
 */

package org.netbeans.modules.xml.xdm.nodes;

import java.io.File;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import junit.framework.*;
import org.netbeans.modules.xml.xdm.Util;
import org.netbeans.modules.xml.xdm.XDMModel;

/**
 *
 * @author ajit
 */
public class TextTest extends TestCase {
    
    public TextTest(String testName) {
        super(testName);
    }

    protected void setUp() throws Exception {
        baseDocument = Util.getResourceAsDocument("nodes/xdm.xml");
        xmlModel = Util.loadXDMModel(baseDocument);
        text = (Text)xmlModel.getDocument().getChildNodes().item(0).
                getChildNodes().item(1).getChildNodes().item(0);
    }

    public static Test suite() {
        TestSuite suite = new TestSuite(TextTest.class);
        
        return suite;
    }

    /**
     * Test of getNodeValue method, of class org.netbeans.modules.xml.xdm.nodes.Text.
     */
    public void testGetNodeValue() {
        String expResult = "Vidhya Narayanan\n    ";
        String result = text.getNodeValue();
        assertEquals(expResult, result);
    }

    /**
     * Test of getNodeType method, of class org.netbeans.modules.xml.xdm.nodes.Text.
     */
    public void testGetNodeType() {
        short expResult = org.w3c.dom.Node.TEXT_NODE;
        short result = text.getNodeType();
        assertEquals("getNodeType must return TEXT_NODE",expResult, result);
    }

    /**
     * Test of getNodeName method, of class org.netbeans.modules.xml.xdm.nodes.Text.
     */
    public void testGetNodeName() {
        String expResult = "#text";
        String result = text.getNodeName();
        assertEquals("getNodeName must return #text",expResult, result);
    }

    /**
     * Test of getNamespaceURI method, of class org.netbeans.modules.xml.xdm.nodes.Text.
     */
    public void testGetNamespaceURI() {
        
        String result = text.getNamespaceURI();
        assertNull(result);
    }

    /**
     * Test of getText method, of class org.netbeans.modules.xml.xdm.nodes.Text.
     */
    public void testGetText() {
        
        String expResult = "Vidhya Narayanan\n    ";
        String result = text.getText();
        assertEquals(expResult, result);
    }

    /**
     * Test of setText method, of class org.netbeans.modules.xml.xdm.nodes.Text.
     */
    public void testSetText() {
        String newText = "Another Person";
        try {
            text.setText(newText);
            assertTrue("setText must throw exception for text node in tree",false);
        } catch (Exception e) {
            assertTrue(true);
        }
        Text newTextNode = (Text)text.clone(true,false,false);
        try {
            newTextNode.setText(newText);
            assertTrue(true);
        } catch (Exception e) {
            assertTrue("setText must not throw exception for text node not in tree",false);
        }
        xmlModel.modify(text,newTextNode);
        text = (Text)xmlModel.getDocument().getChildNodes().item(0).
                getChildNodes().item(1).getChildNodes().item(0);
        assertEquals(newText, text.getText());
    }
    
    public void testXmlEscapeText() throws Exception {
        Element root = (Element) xmlModel.getDocument().getDocumentElement();
        String textValue = "<mytext>Am I a \"text\" node or part of a 'text' node&?</mytext";
        Text textNode = (Text) xmlModel.getCurrentDocument().createTextNode(textValue);
        xmlModel.append(root, textNode);
        xmlModel.flush();
        File tempFile = Util.dumpToTempFile(baseDocument);
        Util.setDocumentContentTo(baseDocument, tempFile);
        xmlModel.sync();
        root = (Element) xmlModel.getCurrentDocument().getDocumentElement();
        Text last = (Text) root.getLastChild();
        assertEquals("last should be text with same string value", textValue, last.getText().trim());
    }

    public void testXmlFragmentText() throws Exception {
        Element root = (Element) xmlModel.getDocument().getDocumentElement();
        String textValue = "I am a \"text\" node or part &lt;doc> of a 'text' node&?<test attr1=\"foo\"></test>";
        root = (Element) xmlModel.setXmlFragmentText(root, textValue).get(0);
        Text first = (Text) root.getChildNodes().item(0);
        assertEquals("I am a \"text\" node or part <doc> of a 'text' node&?", first.getText());
        Element second = (Element) root.getChildNodes().item(1);
        assertEquals("test", second.getTagName());
        assertEquals("foo", second.getAttribute("attr1"));
        assertEquals(textValue, root.getXmlFragmentText());
    }
    
    /*public void testDOMTextBehaviour() throws Exception {
        DocumentBuilderFactory fact = DocumentBuilderFactory.newInstance();
        DocumentBuilder builder = fact.newDocumentBuilder();
        org.w3c.dom.Document doc = builder.parse(Util.class.getResourceAsStream("nodes/xdm.xml"));
        org.w3c.dom.Element root = doc.getDocumentElement();
        String textValue = "<mytext>Am I a text node or part of a text node?</mytext>";
        org.w3c.dom.Text textNode = doc.createTextNode(textValue);
        root.appendChild(textNode);
        File tempFile = File.createTempFile("xdm-tester-", null);
        System.out.println("TEMP FILE is " + tempFile);
        DOMSource s = new DOMSource();
        s.setNode(doc);
        Transformer t = TransformerFactory.newInstance().newTransformer();
        StreamResult r = new StreamResult(tempFile);
        t.transform(s, r);
    }*/

    private XDMModel xmlModel;
    private Text text;
    private javax.swing.text.Document baseDocument;
}
