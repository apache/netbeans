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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.openide.xml;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.CharConversionException;
import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.util.List;
import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import org.netbeans.junit.NbTestCase;
import org.openide.util.Utilities;
import org.openide.util.test.TestFileUtils;
import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.EntityResolver;
import org.xml.sax.ErrorHandler;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

public class XMLUtilTest extends NbTestCase {
    
    public XMLUtilTest(String testName) {
        super(testName);
    }
    
    public void testCreateXMLReader() throws Exception {
        XMLUtil.createXMLReader();
        // XXX now what?
    }
    
    public void testCreateDocument() throws Exception {
        XMLUtil.createDocument("root", null, null, null);
        // XXX now what?
    }
    
    public void testWrite() throws Exception {
        String data = "<foo bar=\"val\"><baz/></foo>";
        Document doc = XMLUtil.parse(new InputSource(new StringReader(data)), false, true, null, null);
        //System.err.println("XMLUtil.parse impl class: " + doc.getClass().getName());
        Element el = doc.getDocumentElement();
        assertEquals("foo", el.getNodeName());
        assertEquals("val", el.getAttribute("bar"));
        NodeList l = el.getElementsByTagName("*");
        assertEquals(1, l.getLength());
        Element el2 = (Element)l.item(0);
        assertEquals("baz", el2.getLocalName());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLUtil.write(doc, baos, "UTF-8");
        String data2 = baos.toString("UTF-8");
        //System.err.println("testWrite: data2:\n" + data2);
        assertTrue(data2, data2.indexOf("foo") != -1);
        assertTrue(data2, data2.indexOf("bar") != -1);
        assertTrue(data2, data2.indexOf("baz") != -1);
        assertTrue(data2, data2.indexOf("val") != -1);
    }
    
    /** Test that read/write DOCTYPE works too. */
    public void testDocType() throws Exception {
        String data = "<!DOCTYPE foo PUBLIC \"The foo DTD\" \"http://nowhere.net/foo.dtd\"><foo><x/><x/></foo>";
        Document doc = XMLUtil.parse(new InputSource(new StringReader(data)), true, true, new Handler(), new Resolver());
        DocumentType t = doc.getDoctype();
        assertNotNull(t);
        assertEquals("foo", t.getName());
        assertEquals("The foo DTD", t.getPublicId());
        assertEquals("http://nowhere.net/foo.dtd", t.getSystemId());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLUtil.write(doc, baos, "UTF-8");
        String data2 = baos.toString("UTF-8");
        //System.err.println("data2:\n" + data2);
        assertTrue(data2, data2.indexOf("foo") != -1);
        assertTrue(data2, data2.indexOf('x') != -1);
        assertTrue(data2, data2.indexOf("DOCTYPE") != -1);
        assertTrue(data2, data2.indexOf("The foo DTD") != -1);
        assertTrue(data2, data2.indexOf("http://nowhere.net/foo.dtd") != -1);
    }
    private static final class Handler implements ErrorHandler {
        public @Override void error(SAXParseException exception) throws SAXException {
            throw exception;
        }
        public @Override void fatalError(SAXParseException exception) throws SAXException {
            throw exception;
        }
        public @Override void warning(SAXParseException exception) throws SAXException {
            throw exception;
        }
    }
    private static final class Resolver implements EntityResolver {
        public @Override InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
            assertEquals("The foo DTD", publicId);
            assertEquals("http://nowhere.net/foo.dtd", systemId);
            String data = "<!ELEMENT foo (x+)><!ELEMENT x EMPTY>";
            return new InputSource(new StringReader(data));
        }
    }

    public void testValidate() throws Exception {
        Element r = XMLUtil.createDocument("root", "some://where", null, null).getDocumentElement();
        r.setAttribute("hello", "there");
        SchemaFactory f = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        String xsd =
                "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema' targetNamespace='some://where' xmlns='some://where' elementFormDefault='qualified'>\n" +
                " <xsd:element name='root'>\n" +
                "  <xsd:complexType>\n" +
                "   <xsd:attribute name='hello' type='xsd:NMTOKEN' use='required'/>\n" +
                "  </xsd:complexType>\n" +
                " </xsd:element>\n" +
                "</xsd:schema>\n";
        Schema s = f.newSchema(new StreamSource(new StringReader(xsd)));
        XMLUtil.validate(r, s);
        r.setAttribute("goodbye", "now");
        try {
            XMLUtil.validate(r, s);
            fail();
        } catch (SAXException x) {/*OK*/}
        // Make sure Java #6529766 is fixed (no longer any need for fixupNoNamespaceAttrs):
        String xml = "<root xmlns='some://where'/>";
        r = XMLUtil.parse(new InputSource(new StringReader(xml)), false, true, null, null).getDocumentElement();
        r.setAttribute("hello", "there");
        XMLUtil.validate(r, s);
        r.setAttribute("goodbye", "now");
        try {
            XMLUtil.validate(r, s);
            fail();
        } catch (SAXException x) {/*OK*/}
    }
    
    public void testToAttributeValue() throws IOException {
        String result = null;
        try {
            result = XMLUtil.toAttributeValue("\t\r\n &'<\"");
        } catch (CharConversionException ex) {            
        }
        
        assertEquals("Basic escape test failed", "\t\r\n &amp;&apos;&lt;&quot;", result);
        
        try {
            XMLUtil.toAttributeValue(new String(new byte[] { 0 }));
            fail("Forbidden character accepted.");
        } catch (CharConversionException ex) {            
        }

        try {
            XMLUtil.toAttributeValue(new String(new byte[] { 31 }));
            fail("Forbidden character accepted.");
        } catch (CharConversionException ex) {            
        }        
    }
    
    public void testElementToContent() {
        String result = null;
        
        try {
            result = XMLUtil.toElementContent("]]>\t\r\n &<>");
        } catch (CharConversionException ex) {
        }
        
        assertEquals("Basic escape test failed", "]]&gt;\t\r\n &amp;&lt;>", result);
        
        try {
            XMLUtil.toElementContent(new String(new byte[] { 0 }));
            fail("Forbidden character accepted.");
        } catch (CharConversionException ex) {            
        }

        try {
            XMLUtil.toElementContent(new String(new byte[] { 31 }));
            fail("Forbidden character accepted.");
        } catch (CharConversionException ex) {            
        }        
                
    }
    
    public void testToHex() {
        
        byte[] data = new byte[] {0, 1, 15, 16, (byte)255};
        String s = XMLUtil.toHex(data, 0, data.length);
        
        // Add your test code below by replacing the default call to fail.
        if (s.equalsIgnoreCase("00010f10ff") == false) {
            fail("toHex() =" + s);
        }
    }
    
    public void testFromHex() {
        
        char[] hex = "00010f10ff".toCharArray();
        try {
            byte[] ret = XMLUtil.fromHex(hex, 0, hex.length);
            if (ret[0] != 0 || ret[1] != 1 || ret[2] != 15 || ret[3] != 16 || ret[4] != (byte)255) {
                fail("fromHex()");
            }
        } catch (IOException ex) {
            fail(ex.getMessage());
        }
                
    }
    
    /**
     * Check that reading and writing namespaces works.
     * @see "#36294"
     */
    public void testNamespaces() throws Exception {
        String data = "<foo xmlns='bar'><baz/></foo>";
        Document doc = XMLUtil.parse(new InputSource(new StringReader(data)), false, true, null, null);
        //System.err.println("XMLUtil.parse impl class: " + doc.getClass().getName());
        Element el = doc.getDocumentElement();
        assertEquals("foo", el.getNodeName());
        assertEquals("foo", el.getTagName());
        assertEquals("foo", el.getLocalName());
        assertEquals("bar", el.getNamespaceURI());
        NodeList l = el.getElementsByTagName("*");
        assertEquals(1, l.getLength());
        Element el2 = (Element)l.item(0);
        assertEquals("baz", el2.getLocalName());
        assertEquals("bar", el2.getNamespaceURI());
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLUtil.write(doc, baos, "UTF-8");
        String data2 = baos.toString("UTF-8");
        //System.err.println("testNamespaces: data2:\n" + data2);
        assertTrue(data2, data2.indexOf("foo") != -1);
        assertTrue(data2, data2.indexOf("bar") != -1);
        doc = XMLUtil.parse(new InputSource(new ByteArrayInputStream(baos.toByteArray())), false, true, null, null);
        el = doc.getDocumentElement();
        assertEquals("foo", el.getLocalName());
        assertEquals("bar", el.getNamespaceURI());
        l = el.getElementsByTagName("*");
        assertEquals(1, l.getLength());
        el2 = (Element)l.item(0);
        assertEquals("baz", el2.getLocalName());
        assertEquals("bar", el2.getNamespaceURI());
        doc = XMLUtil.createDocument("foo2", "bar2", null, null);
        //System.err.println("XMLUtil.createDocument impl class: " + doc.getClass().getName());
        doc.getDocumentElement().appendChild(doc.createElementNS("bar2", "baz2"));
        baos = new ByteArrayOutputStream();
        XMLUtil.write(doc, baos, "UTF-8");
        data2 = baos.toString("UTF-8");
        assertTrue(data2, data2.indexOf("foo2") != -1);
        assertTrue("namespace 'bar2' of root element mentioned in output: " + data2, data2.indexOf("bar2") != -1);
        doc = XMLUtil.parse(new InputSource(new ByteArrayInputStream(baos.toByteArray())), false, true, null, null);
        el = doc.getDocumentElement();
        assertEquals("foo2", el.getLocalName());
        assertEquals("bar2", el.getNamespaceURI());
        l = el.getElementsByTagName("*");
        assertEquals(1, l.getLength());
        el2 = (Element)l.item(0);
        assertEquals("baz2", el2.getLocalName());
        assertEquals("bar2", el2.getNamespaceURI());
    }
    
    /**
     * Check more namespace stuff, since JAXP has a lot of bugs...
     * @see "#6308026"
     */
    public void testNamespaces2() throws Exception {
        String data = "<root xmlns='root'/>";
        Document doc = XMLUtil.parse(new InputSource(new StringReader(data)), false, true, null, null);
        doc.getDocumentElement().appendChild(doc.createElementNS("child", "child"));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLUtil.write(doc, baos, "UTF-8");
        //System.err.println("testNamespaces2:\n" + baos);
        doc = XMLUtil.parse(new InputSource(new ByteArrayInputStream(baos.toByteArray())), false, true, null, null);
        Element el = doc.getDocumentElement();
        assertEquals("root", el.getLocalName());
        assertEquals("root", el.getNamespaceURI());
        NodeList l = el.getElementsByTagName("*");
        assertEquals(1, l.getLength());
        el = (Element) l.item(0);
        assertEquals("child", el.getLocalName());
        assertEquals("Correct namespaces in " + baos.toString(), "child", el.getNamespaceURI());
    }
    
    public void testIndentation() throws Exception {
        Document doc = XMLUtil.createDocument("root", null, null, null);
        doc.getDocumentElement().appendChild(doc.createElement("child"));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLUtil.write(doc, baos, "UTF-8");
        String data = baos.toString()/*#62680*/.replaceAll("\r\n", "\n");
        assertTrue("had reasonable indentation in\n" + data, data.indexOf("<root>\n    <child/>\n</root>\n") != -1);
    }
    
    /** cf. #62006 */
    public void testIndentation2() throws Exception {
        // XXX currently it seems that the JDK 5/6 serializer adds an extra \n after DOCTYPE, for no apparent reason!
        // While the Mantis serializer inserts a useless line break in the middle...
        // so we don't check formatting on that part.
        // Also serializers may arbitrarily reorder the doctype, so don't even look at it (just make sure it is there).
        String doctype = "<!DOCTYPE p PUBLIC \"random DTD\" \"" + XMLUtilTest.class.getResource("random.dtd") + "\">\n";
        String data =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                doctype +
                "<!--\n" +
                "Some license or whatever.\n" +
                "-->\n" +
                "<?stylesheet location=\"here\"?>\n" +
                "<p>\n" +
                "    <t/>\n" +
                "    <c>\n" +
                "        <d>\n" +
                "            <s/>\n" +
                "            <e>\n" +
                "                <!-- oh? -->\n" +
                "            </e>\n" +
                "        </d>\n" +
                "    </c>\n" +
                "</p>\n";
        Document doc = XMLUtil.parse(new InputSource(new StringReader(data)), false, false, null, null);
        Element d = (Element) doc.getElementsByTagName("d").item(0);
        Element c = (Element) d.getParentNode();
        Element d2 = (Element) DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument().importNode(d, true);
        c.removeChild(d);
        c.appendChild(doc.importNode(d2, true));
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLUtil.write(doc, baos, "UTF-8");
        String data2 = baos.toString().replaceAll("\r\n", "\n");
        //System.err.println("normalized data:\n" + ignoreSpaceChanges(data, doctype) + "\nnormalized data2:\n" + ignoreSpaceChanges(data2, doctype));
        assertEquals("identity replacement should not mess up indentation in \n" + data2, ignoreSpaceChanges(data, doctype), ignoreSpaceChanges(data2, doctype));
    }
    private static String ignoreSpaceChanges(String text, String fuzzy) {
        // Yes this is confusing!
        // Inner regexp:
        // Input: <!DOCTYPE p PUBLIC ...>\n
        // Output: \Q<!DOCTYPE\E\s+\Qp\E\s+\QPUBLIC...>\E\s+\Q\E
        // Outer regexp:
        // Input: stuff\n<!DOCTYPE p\nPUBLIC ...>\n\nmore stuff
        // Output: stuff\n<!DOCTYPE p PUBLIC ...>\nmore stuff
        String regexp = "\\Q" + fuzzy.replaceAll("\\s+", "\\\\E\\\\s+\\\\Q") + "\\E";
        //System.err.println("regexp='" + regexp + "' text='" + text + "' fuzzy='" + fuzzy + "' result='" + text.replaceFirst(regexp, "") + "'");
        return text.replaceFirst(regexp, "");
    }
    
    public void testSignificantWhitespace() throws Exception {
        String data =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<r>\n" +
                "    <p>This is <em>not</em> a test!</p>\n" +
                "</r>\n";
        Document doc = XMLUtil.parse(new InputSource(new StringReader(data)), false, false, null, null);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLUtil.write(doc, baos, "UTF-8");
        String data2 = baos.toString().replaceAll("\r\n", "\n");
        assertEquals("identity replacement should not mess up significant whitespace", data, data2);
    }
    
    public void testDocumentLeak() throws Exception {
        String data = "<foo xmlns='bar'><baz/></foo>";
        Document doc = XMLUtil.parse(new InputSource(new StringReader(data)), false, true, null, null);

        WeakReference<Document> wr = new WeakReference<Document>(doc);
        doc = null;
        
        assertGC("Document should be freed", wr);
    }
    
    public void testCDATA() throws Exception {
        Document doc = XMLUtil.createDocument("root", null, null, null);
        Element e = doc.createElement("sometag");
        doc.getDocumentElement().appendChild(e);

        String cdataContent = "!&<>*\n[[]]";
        CDATASection cdata = doc.createCDATASection(cdataContent);
        e.appendChild(cdata);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLUtil.write(doc, baos, "UTF-8");

        String data = baos.toString("UTF-8").replace("\r\n", "\n");
        assertTrue("Can't find CDATA section", data.indexOf("<![CDATA[" + cdataContent + "]]>") != -1);
        
        // parse the data back to DOM
        Document doc2 = XMLUtil.parse(new InputSource(new ByteArrayInputStream(baos.toByteArray())), false, false, null, null);
        NodeList nl = doc2.getElementsByTagName("sometag");
        assertEquals("Wrong number of <sometag/> elements", 1, nl.getLength());
        nl = nl.item(0).getChildNodes();
        assertEquals("Wrong number of <sometag/> child elements", 1, nl.getLength());
        Node child = nl.item(0);
        assertTrue("Expecting CDATASection node", child instanceof CDATASection);
        assertEquals("Wrong CDATASection content", cdataContent, ((CDATASection) child).getNodeValue());
    }

    public void testEntityIncludes() throws Exception {
        clearWorkDir();
        // #146081: xml:base attributes get inserted sometimes and can mess up validation.
        // #160806: problems writing: no system ID, xml:base inserted.
        SchemaFactory f = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
        String xsd =
                "<xsd:schema xmlns:xsd='http://www.w3.org/2001/XMLSchema' targetNamespace='some://where' xmlns='some://where' elementFormDefault='qualified'>\n" +
                " <xsd:element name='root'>\n" +
                "  <xsd:complexType>\n" +
                "   <xsd:sequence>\n" +
                "    <xsd:element name='hello' type='xsd:NMTOKEN'/>\n" +
                "   </xsd:sequence>\n" +
                "  </xsd:complexType>\n" +
                " </xsd:element>\n" +
                "</xsd:schema>\n";
        Schema s = f.newSchema(new StreamSource(new StringReader(xsd)));
        File d = getWorkDir();
        File main = new File(d, "main.xml");
        File ent = new File(d, "ent.xml");
        TestFileUtils.writeFile(main, "<!DOCTYPE root [<!ENTITY ent SYSTEM 'ent.xml'>]> <root xmlns='some://where'>&ent;</root>");
        TestFileUtils.writeFile(ent, "<hello xmlns='some://where'>there</hello>");
        Document doc = XMLUtil.parse(new InputSource(Utilities.toURI(main).toString()), false, true, null, null);
        XMLUtil.validate(doc.getDocumentElement(), s);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLUtil.write(doc, baos, "UTF-8");
        String expanded =
                "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                "<root xmlns=\"some://where\">\n" +
                "    <hello>there</hello>\n" +
                "</root>\n";
        assertEquals(expanded, baos.toString("UTF-8").replace("\r\n", "\n"));
        // XXX #160806 reported a problem with "xml:base" being consider a no-NS attr; not yet caught by test
        // Try again with no xmlns specified in entity; should inherit from main.xml:
        TestFileUtils.writeFile(ent, "<hello>there</hello>");
        doc = XMLUtil.parse(new InputSource(Utilities.toURI(main).toString()), false, true, null, null);
        XMLUtil.validate(doc.getDocumentElement(), s);
        baos = new ByteArrayOutputStream();
        XMLUtil.write(doc, baos, "UTF-8");
        assertEquals(expanded, baos.toString("UTF-8").replace("\r\n", "\n"));
    }

    public void testHandlerLeak() throws Exception {
        ErrorHandler handler = new DefaultHandler();
        EntityResolver resolver = new DefaultHandler();
        Reference<?> handlerRef = new WeakReference<Object>(handler);
        Reference<?> resolverRef = new WeakReference<Object>(resolver);
        XMLUtil.parse(new InputSource(new StringReader("<hello/>")), false, false, handler, resolver);
        handler = null;
        resolver = null;
        assertGC("can collect handler", handlerRef);
        assertGC("can collect resolver", resolverRef);
    }

    public void testAppendChildElement() throws Exception {
        Document doc = XMLUtil.parse(new InputSource(new StringReader("<root></root>")), false, true, null, null);
        Element parent = doc.createElementNS("unittest", "parent");
        Element newElement1 = doc.createElementNS("unittest", "new_element1");
        Element newElement2 = doc.createElementNS("unittest", "new_element2");
        Element newElement3 = doc.createElementNS("unittest", "new_element3");

        // append to the default root node (no match in order)
        XMLUtil.appendChildElement(parent, newElement1, new String[] { "new_element2", "new_element1" });
        NodeList children = parent.getChildNodes();
        assertEquals(1, children.getLength());

        // append after the child we just appended
        XMLUtil.appendChildElement(parent, newElement2, new String[] { "new_element2", "new_element1" });

        children = parent.getChildNodes();
        assertEquals(2, children.getLength());
        Node firstChild = parent.getChildNodes().item(0);
        Node secondChild = parent.getChildNodes().item(1);
        assertEquals("new_element2", firstChild.getNodeName());
        assertEquals("new_element1", secondChild.getNodeName());

        // failures

        try {
            // new child is not in the order list
            XMLUtil.appendChildElement(parent, newElement3, new String[] { "new_element2", "new_element1"});
            fail("Expecting IAE");
        } catch (IllegalArgumentException e) {
            assertEquals("new child element 'new_element3' not specified in order [new_element2, new_element1]", e.getMessage());
        }
        try {
            // existing child not in the order list
            XMLUtil.appendChildElement(parent, newElement3, new String[] { "new_element3"});
            fail("Expecting IAE");
        } catch (IllegalArgumentException e) {
            assertEquals("Existing child element 'new_element2' not specified in order [new_element3]", e.getMessage());
        }
    }

    public void testFindSubElements() throws Exception {
        Document doc = XMLUtil.parse(new InputSource(new StringReader("<root> <child1></child1><child2/><!-- comment --><child3/></root>")),
                false, true, null, null);

        Element parent = doc.getDocumentElement();
        assertEquals(5, parent.getChildNodes().getLength());
        List<Element> subElements = XMLUtil.findSubElements(parent);
        assertEquals(3, subElements.size());
        Element firstChild = subElements.get(0);
        Element secondChild = subElements.get(1);
        Element thirdChild = subElements.get(2);

        assertEquals("child1", firstChild.getNodeName());
        assertEquals("child2", secondChild.getNodeName());
        assertEquals("child3", thirdChild.getNodeName());

        Document failureDoc = XMLUtil.parse(new InputSource(new StringReader("<root>Non whitespace<!-- comment --></root>")),
            false, true, null, null);
        Element failedParent = failureDoc.getDocumentElement();
        try {
            XMLUtil.findSubElements(failedParent);
            fail("expected IAE");
        } catch (IllegalArgumentException e) { }
    }

    public void testFindElement() throws Exception {
        String xmlDoc = "<root> " +
                        " <h:table xmlns:h=\"http://www.w3.org/TR/html4/\">" +
                        " </h:table>" +
                        " <f:table xmlns:f=\"http://www.w3schools.com/furniture\">" +
                        " </f:table>" +
                        " <dup/><dup/>" +
                        " <h:form xmlns:h=\"http://www.w3.org/TR/html4/\">" +
                        " </h:form>" +
                        "</root>";
        
        Document doc = XMLUtil.parse(new InputSource(new StringReader(xmlDoc)), false, true, null, null);
        Element parent = doc.getDocumentElement();

        Element doesNotExist1 = XMLUtil.findElement(parent, "doesNotExist", "h");
        assertNull(doesNotExist1);

        Element doesNotExist2 = XMLUtil.findElement(parent, "doesNotExist", null);
        assertNull(doesNotExist2);

        Element noTable2 = XMLUtil.findElement(parent, "table", "h");
        assertNull(noTable2);

        Element noTable3 = XMLUtil.findElement(parent, "table", "f");
        assertNull(noTable3);

        Element table1 = XMLUtil.findElement(parent, "table", "http://www.w3.org/TR/html4/");
        assertNotNull(table1);

        Element table2 = XMLUtil.findElement(parent, "table", "http://www.w3schools.com/furniture");
        assertNotNull(table2);

        Element form1 = XMLUtil.findElement(parent, "form", "http://www.w3.org/TR/html4/");
        assertNotNull(form1);

        Element form2 = XMLUtil.findElement(parent, "form", null);
        assertNotNull(form2);

        assertEquals(form1, form2);

        try {
            XMLUtil.findElement(parent, "dup", null);
            fail("Expected IAE");
        } catch (IllegalArgumentException e) { }

        try {
            XMLUtil.findElement(parent, "table", null);
            fail("Expected IAE");
        } catch (IllegalArgumentException e) { }
    }

    public void testFindText() throws Exception {
        Document doc = XMLUtil.parse(new InputSource(new StringReader("<root>Text To Find<child></child></root>")),
                false, true, null, null);

        Element parent = doc.getDocumentElement();
        String foundText = XMLUtil.findText(parent);

        assertEquals("Text To Find", foundText);

        String notFoundText = XMLUtil.findText(parent.getFirstChild());

        assertNull(notFoundText);
    }

    public void testTranslateXML() throws Exception {
        // don't add any whitespace to the first 3 nodes
        String xmlDoc = "<root><table bgcolor='red'><tr>" +
                        "   <td>Apples</td>" +
                        "   <td>Bananas</td>" +
                        "  </tr>" +
                        " </table>" +
                        " <!-- comment -->" +
                        "<![CDATA[free form data]]>" +
                        "</root>";

        Document doc = XMLUtil.parse(new InputSource(new StringReader(xmlDoc)), false, true, null, null);
        Element parent = doc.getDocumentElement();

        Element translated = XMLUtil.translateXML((Element) parent.getFirstChild(), "http://www.w3.org/TR/html4/");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLUtil.write(doc, baos, "UTF-8");

        Document destDoc = XMLUtil.parse(new InputSource(new StringReader("<root/>")), false, true, null, null);

        destDoc.importNode(translated, true);
        baos = new ByteArrayOutputStream();
        XMLUtil.write(destDoc, baos, "UTF-8");

        assertEquals("http://www.w3.org/TR/html4/", translated.getNamespaceURI());
        assertEquals(1, translated.getAttributes().getLength());

        assertEquals("http://www.w3.org/TR/html4/", translated.getFirstChild().getNamespaceURI());
        assertEquals(0, translated.getFirstChild().getAttributes().getLength());
    }

    public void testCopyDocument() throws Exception {
        String srcXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
                        "<root>" +
                        "   <table bgcolor='red'>" +
                        "    <tr>" +
                        "           <td>Apples</td>" +
                        "           <td>Bananas</td>" +
                        "       </tr>" +
                        "   </table>" +
                        " <!-- comment -->" +
                        "</root>";

        String resultXml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n" +
                        "<root>\n" +
                        "    <table xmlns=\"http://www.w3.org/TR/html4/\" bgcolor=\"red\">\n" +
                        "        <tr>\n" +
                        "            <td>Apples</td>\n" +
                        "            <td>Bananas</td>\n" +
                        "        </tr>\n" +
                        "    </table>\n" +
                        "    <!-- comment -->\n" +
                        "</root>\n";

        Document srcDoc = XMLUtil.parse(new InputSource(new StringReader(srcXml)), false, true, null, null);
        Element srcRoot = srcDoc.getDocumentElement();

        Document dstDoc = XMLUtil.parse(new InputSource(new StringReader("<root/>")), false, true, null, null);

        Element dstRoot = dstDoc.getDocumentElement();
        XMLUtil.copyDocument(srcRoot, dstRoot, "http://www.w3.org/TR/html4/");

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        XMLUtil.write(dstDoc, baos, "UTF-8");
        assertEquals(resultXml, baos.toString().replace("\r\n", "\n"));

        assertEquals(1, dstDoc.getChildNodes().getLength());
        Element root = dstDoc.getDocumentElement();
        Element tableNode = (Element) root.getFirstChild().getNextSibling();

        assertEquals("table", tableNode.getNodeName());
        assertEquals("http://www.w3.org/TR/html4/", tableNode.getNamespaceURI());
        assertEquals(1, tableNode.getAttributes().getLength());
    }

}
