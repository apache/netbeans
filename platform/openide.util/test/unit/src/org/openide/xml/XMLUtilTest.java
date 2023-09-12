/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.openide.xml;

import java.io.CharConversionException;
import java.io.IOException;
import java.util.List;
import javax.xml.parsers.DocumentBuilderFactory;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Before;
import org.junit.Test;
import static org.openide.xml.XMLUtil.*;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**

 */
public class XMLUtilTest {

    private Document document;

    //--------------------------------------------------------------------------
    private static void expectNullPointer(final Runnable code) {

        try {
            code.run();
            fail("Exception not thrown.");
        } catch (final NullPointerException e) {
            // good
        }
    }

    //--------------------------------------------------------------------------
    private static void expectIllegalArgument(final Runnable code) {

        try {
            code.run();
            fail("Exception not thrown.");
        } catch (final IllegalArgumentException e) {
            // good
        }
    }

    //--------------------------------------------------------------------------
    private static void expectIndexOutOfBounds(final Runnable code) {

        try {
            code.run();
            fail("Exception not thrown.");
        } catch (final IndexOutOfBoundsException e) {
            // good
        }
    }

    //--------------------------------------------------------------------------
    private static String[] arrayOf(String... arr) {

        return arr;
    }

    //--------------------------------------------------------------------------
    @Before
    public void setUp() throws Exception {

        this.document = DocumentBuilderFactory.newInstance().
                newDocumentBuilder().newDocument();
    }

    //--------------------------------------------------------------------------
    @Test
    public void findText_throwsNullPointer_whenGivenNull() {

        expectNullPointer(() -> findText(null));
    }

    //--------------------------------------------------------------------------
    @Test
    public void findText_returnsNull_whenGivenNodeWithNoChildren() {

        assertNull(findText(this.document));
    }

    //--------------------------------------------------------------------------
    @Test
    public void findText_returnsNull_whenGivenNodeWithNoTestChildren() {

        Node child = this.document.createElement("child");
        this.document.appendChild(child);

        assertNull(findText(this.document));

        child.appendChild(this.document.createTextNode("abc"));
        // "abc" ia a GRANDCHILD, shall not be found
        assertNull(findText(this.document));
    }

    //--------------------------------------------------------------------------
    @Test
    public void findText_returnsNull_whenGivenTextNode() {

        assertNull(findText(this.document.createTextNode("abc")));
    }

    //--------------------------------------------------------------------------
    @Test
    public void findText_returnsText_whenGivenNodeWithTextChild() {

        Node parent = this.document.createElement("parent");
        this.document.appendChild(parent);
        parent.appendChild(this.document.createTextNode("abc"));

        assertEquals("abc", findText(parent));

        parent.getFirstChild().setTextContent("");

        assertEquals("", findText(parent));
    }

    //--------------------------------------------------------------------------
    @Test
    public void findText_returnsFirstText_whenGivenNodeWithMultipleTextChildren() {

        Node parent = this.document.createElement("parent");
        this.document.appendChild(parent);
        parent.appendChild(this.document.createTextNode("abc"));
        parent.appendChild(this.document.createTextNode("def"));

        assertEquals("abc", findText(parent));
    }

    //--------------------------------------------------------------------------
    @Test
    public void findElement_throwsNullPointer_whenGivenNullElement() {

        expectNullPointer(() -> findElement(null, "abc", "def"));
    }
    //--------------------------------------------------------------------------

    @Test
    public void findElement_throwsNullPointer_whenGivenNullName() {

        Element parent = this.document.createElement("parent");
        parent.appendChild(this.document.createElement("child"));

        expectNullPointer(() -> findElement(parent, null, "def"));
    }

    //--------------------------------------------------------------------------
    @Test
    public void findElement_returnsNull_whenGivenElementWithNoChildren() {

        Element parent = this.document.createElement("parent");

        assertNull(findElement(parent, null, null));
    }

    //--------------------------------------------------------------------------
    @Test
    public void findElement_returnsElement_whenGivenNameAndNoNamespace() {

        Element parent = this.document.createElement("parent");

        Element child1 = this.document.createElement("child1");
        parent.appendChild(child1);

        Element child2 = this.document.createElementNS("abc", "ns:child2");
        parent.appendChild(child2);

        assertEquals(child1, findElement(parent, "child1", null));
        assertEquals(child2, findElement(parent, "child2", null));
    }

    //--------------------------------------------------------------------------
    @Test
    public void findElement_returnsElement_whenGivenNameAndNamespace() {

        Element parent = this.document.createElement("parent");
        // children to be ignored while searching
        parent.appendChild(this.document.createElementNS("abc", "ns:other"));
        parent.appendChild(this.document.createElementNS("xyz", "ns:child"));
        parent.appendChild(this.document.createTextNode("text"));

        Element child = this.document.createElementNS("abc", "ns:child");
        parent.appendChild(child);

        assertEquals(child, findElement(parent, "child", "abc"));
    }

    //--------------------------------------------------------------------------
    @Test
    public void findElement_returnsNull_whenGivenNameAndNamespace_forNonNamespacedElement() {

        Element parent = this.document.createElement("parent");
        Element child = this.document.createElement("child");
        parent.appendChild(child);

        assertNull(findElement(parent, "child", "abc"));
    }

    //--------------------------------------------------------------------------
    @Test
    public void findElement_returnsNull_whenGivenNameOfAGrandchild() {

        Element parent = this.document.createElement("parent");
        Element child = this.document.createElement("child");
        parent.appendChild(child);
        child.appendChild(this.document.createElement("grandchild"));
        child.appendChild(this.document.createElementNS("abc", "ns:grandchild"));

        assertNull(findElement(parent, "grandchild", null));
        assertNull(findElement(parent, "grandchild", "abc"));
    }

    //--------------------------------------------------------------------------
    @Test
    public void findElement_throwsIllegalArgument_whenGivenNameOfRepeatedElement() {

        Element parent = this.document.createElement("parent");

        parent.appendChild(this.document.createElement("child1"));
        parent.appendChild(this.document.createElement("child1"));

        parent.appendChild(this.document.createElementNS("abc", "ns:child2"));
        parent.appendChild(this.document.createElementNS("abc", "ns:child2"));

        try {
            findElement(parent, "child1", null);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("more than one element with same name found", e.getMessage());
        }

        try {
            findElement(parent, "child2", "abc");
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("more than one element with same name found", e.getMessage());
        }
    }

    //--------------------------------------------------------------------------
    @Test
    public void findSubElements_throwsNullPointer_whenGivenNull() {

        expectNullPointer(() -> findSubElements(null));
    }

    //--------------------------------------------------------------------------
    @Test
    public void findSubElements_returnsEmptyList_whenNodeWithNoChildren() {

        Element parent = this.document.createElement("parent");

        assertTrue(findSubElements(parent).isEmpty());
    }

    //--------------------------------------------------------------------------
    @Test
    public void findSubElements_returnsEmptyList_whenNodeWithCommentAndBlankTextChidrenOnly() {

        Element parent = this.document.createElement("parent");
        parent.appendChild(this.document.createComment("comment"));
        parent.appendChild(this.document.createTextNode("  \t\r\n"));

        assertTrue(findSubElements(parent).isEmpty());
    }

    //--------------------------------------------------------------------------
    @Test
    public void findSubElements_throwsIllegalArgument_whenNodeWithNonBlankTestCildren() {

        Element parent = this.document.createElement("parent");
        parent.appendChild(this.document.createTextNode("text"));

        try {
            findSubElements(parent);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("non-ws text encountered in [parent: null]: text", e.getMessage());
        }
    }

    //--------------------------------------------------------------------------
    @Test
    public void findSubElements_throwsIllegalArgument_whenNodeWithCDataCildren() {

        Element parent = this.document.createElement("parent");
        parent.appendChild(this.document.createCDATASection("cdata"));

        try {
            findSubElements(parent);
            fail();
        } catch (IllegalArgumentException e) {
            assertEquals("unexpected non-element child of [parent: null]: [#cdata-section: cdata]",
                    e.getMessage());
        }
    }

    //--------------------------------------------------------------------------
    @Test
    public void findSubElements_returnSubElements_whenGiventNodewithElementChildren() {

        Element parent = this.document.createElement("parent");
        parent.appendChild(this.document.createComment("comment"));
        parent.appendChild(this.document.createTextNode("  \t\r\n"));
        Element child1 = this.document.createElement("child1");
        parent.appendChild(child1);
        Element child2 = this.document.createElement("child2");
        parent.appendChild(child2);
        // grandhild will not be returned
        Element grandchild = this.document.createElement("grandchild");
        child2.appendChild(grandchild);

        List<Element> subElements = findSubElements(parent);
        assertEquals(2, subElements.size());
        assertEquals(child1, subElements.get(0));
        assertEquals(child2, subElements.get(1));
    }

    //--------------------------------------------------------------------------
    @Test
    public void toHex_returnsEmptyString_whenGivenNullAndZeros() {

        assertEquals("", toHex(null, 0, 0));
    }

    //--------------------------------------------------------------------------
    @Test
    public void toHex_returnsEmptyString_whenGivenEmptyArray() {

        assertEquals("", toHex(new byte[0], 0, 0));
    }

    //--------------------------------------------------------------------------
    @Test
    public void toHex_throwsNullPointer_whenGivenNullAndNonzeroLenght() {

        expectNullPointer(() -> toHex(null, 0, 1));
    }

    //--------------------------------------------------------------------------
    @Test
    public void toHex_returnsEmptyString_whenGivenNonPositiveLength() {

        byte[] data = {0, 2, 15};

        assertEquals("", toHex(data, 0, 0));
        assertEquals("", toHex(data, 0, -1));
    }

    //--------------------------------------------------------------------------
    @Test
    public void toHex_throwsIndexOutOfBounds_whenGivenStartOffsetBeyondBounds() {

        byte[] data = {0, 2, 15};

        expectIndexOutOfBounds(() -> toHex(data, -1, 1));
        expectIndexOutOfBounds(() -> toHex(data, 20, 1));
    }

    //--------------------------------------------------------------------------
    @Test
    public void toHex_throwsIndexOutOfBounds_whenGivenTooLargeLength() {

        byte[] data = {0, 2, 15};

        expectIndexOutOfBounds(() -> toHex(data, 0, 20));
    }

    //--------------------------------------------------------------------------
    @Test
    public void toHex_returnsProperResult_forProperInvocation() {

        byte[] data = {0, 2, 15, -1, 127};

        assertEquals("00020fff7f", toHex(data, 0, 5));
        assertEquals("02", toHex(data, 1, 1));
        assertEquals("0f", toHex(data, 2, 1));
        assertEquals("ff", toHex(data, 3, 1));
        assertEquals("7f", toHex(data, 4, 1));
    }

    //--------------------------------------------------------------------------
    @Test
    public void fromHex_throwsIO_whenGivenNullAndZeros() {

        try {
            fromHex(null, 0, 0);
            fail();
        } catch (IOException e) {
            assertEquals("null", e.getMessage());
        }
    }

    //--------------------------------------------------------------------------
    @Test
    public void fromHex_throwsIO_whenGivenArrayOfOddLength() {

        try {
            fromHex(new char[1], 0, 1);
            fail();
        } catch (IOException e) {
            assertEquals("odd length", e.getMessage());
        }
    }

    //--------------------------------------------------------------------------
    @Test
    public void fromHex_returnsResult_forProperInvocation()
            throws IOException {

        char[] data = {'0', '0', 'a', '2', 'f', 'f'};
        byte[] result;

        //Since 'start' and 'len' arguments are not used, I've put -1s.
        //Is they ever get used, this test will fail indicating the need of new tests. 
        result = fromHex(data, -1, -1);

        assertEquals(3, result.length);
        assertEquals(0, result[0]);
        assertEquals(-94, result[1]);
        assertEquals(-1, result[2]);
    }

    //--------------------------------------------------------------------------
    @Test
    public void fromHex_throwsIO_whenGivenImproperHexChars()
            throws IOException {

        try {
            fromHex(new char[]{'0', 'z'}, -1, -1);
            fail();
        } catch (IOException e) {
            //good
        }

        try {
            fromHex(new char[]{'z', '0'}, -1, -1);
            fail();
        } catch (IOException e) {
            //good
        }
    }

    //--------------------------------------------------------------------------
    @Test
    public void toElementContent_throwsCarConversion_whenGivenNull()
            throws Exception {

        try {
            toElementContent(null);
            fail();
        } catch (CharConversionException e) {
            //good
        }
    }

    //--------------------------------------------------------------------------
    @Test
    public void toElementContent_returnsArgumentValue_whenNoEscapeCharsArePresent()
            throws Exception {

        assertEquals("", toElementContent(""));
        assertEquals(" \r\t\n ", toElementContent(" \r\t\n "));
        assertEquals("John Doe", toElementContent("John Doe"));
    }

    //--------------------------------------------------------------------------
    @Test
    public void toElementContent_throwsCarConversion_whenGivenInvalidCharacters()
            throws Exception {

        try {
            toElementContent("\b\f");
            fail();
        } catch (CharConversionException e) {
            //good
        }
    }

    //--------------------------------------------------------------------------
    @Test
    public void toElementContent_returnsEscapedValue_whenEscapeCharsArePresent()
            throws Exception {

        assertEquals("&lt;&lt;a>&amp;text]]&gt;", toElementContent("<<a>&text]]>"));

        assertEquals("text>", toElementContent("text>"));
        assertEquals("text]>", toElementContent("text]>"));
        assertEquals("text]a>", toElementContent("text]a>"));

        assertEquals(">text", toElementContent(">text"));
        assertEquals("]>text", toElementContent("]>text"));
    }

    //--------------------------------------------------------------------------
    @Test
    public void toAttributeValue_throwsCarConversion_whenGivenNull()
            throws Exception {

        try {
            toAttributeValue(null);
            fail();
        } catch (CharConversionException e) {
            //good
        }
    }

    //--------------------------------------------------------------------------
    @Test
    public void toAttributeValue_returnsArgumentValue_whenNoEscapeCharsArePresent()
            throws Exception {

        assertEquals("", toAttributeValue(""));
        assertEquals(" \r\t\n ", toAttributeValue(" \r\t\n "));
        assertEquals("John Doe", toAttributeValue("John Doe"));
    }

    //--------------------------------------------------------------------------
    @Test
    public void toAttributeValue_throwsCarConversion_whenGivenInvalidCharacters()
            throws Exception {

        try {
            toAttributeValue("\b\f");
            fail();
        } catch (CharConversionException e) {
            //good
        }
    }

    //--------------------------------------------------------------------------
    @Test
    public void toAttributeValue_returnsEscapedValue_whenEscapeCharsArePresent()
            throws Exception {

        assertEquals("&lt;&lt;a>&amp;text&apos;&quot;", toAttributeValue("<<a>&text'\""));
    }

    //--------------------------------------------------------------------------
    @Test
    public void copyDocument_throwsNullPointer_whenGivenNullElements()
            throws Exception {

        Element parent = this.document.createElement("parent");

        expectNullPointer(() -> copyDocument(null, parent, "abc"));
        expectNullPointer(() -> copyDocument(parent, null, "abc"));
    }

    //--------------------------------------------------------------------------
    @Test
    public void copyDocument_doesNothing_whenGivenEmementWithNoChildren()
            throws Exception {

        Element sourceParent = this.document.createElement("sourceParent");
        Element targetParent = this.document.createElement("targetParent");

        copyDocument(sourceParent, targetParent, "abc");

        assertEquals(0, targetParent.getChildNodes().getLength());
    }

    //--------------------------------------------------------------------------
    @Test
    public void copyDocument_duplicatesChildren_whenGivenTheSameNodeAsSourceAndTarget()
            throws Exception {

        Element parent = this.document.createElement("parent");
        Element child = this.document.createElement("child");
        Attr attr = this.document.createAttributeNS("xyz", "attr1");
        attr.setNodeValue("value");
        child.setAttributeNode(attr);
        parent.appendChild(child);

        copyDocument(parent, parent, "abc");

        NodeList children = parent.getChildNodes();
        assertEquals(2, children.getLength());

        assertEquals("child", children.item(0).getNodeName());
        assertNull(children.item(0).getNamespaceURI());
        NamedNodeMap attrs = children.item(0).getAttributes();
        assertEquals(1, attrs.getLength());
        assertEquals("value", attrs.getNamedItem("attr1").getNodeValue());
        assertEquals("xyz", attrs.getNamedItem("attr1").getNamespaceURI());

        assertEquals("child", children.item(1).getNodeName());
        assertEquals("abc", children.item(1).getNamespaceURI());
        attrs = children.item(1).getAttributes();
        assertEquals(1, attrs.getLength());
        assertEquals("value", attrs.getNamedItem("attr1").getNodeValue());
        assertNull(attrs.getNamedItem("attr1").getNamespaceURI());
    }

    //--------------------------------------------------------------------------
    @Test
    public void copyDocument_copiesSubtree_forProperInvocation()
            throws Exception {

        Element sourceParent = this.document.createElement("sourceParent");
        sourceParent.appendChild(this.document.createComment("comment"));
        Element child = this.document.createElementNS("abc", "ns:child");
        Attr attr = this.document.createAttributeNS("xyz", "attr1");
        attr.setNodeValue("value");
        child.setAttributeNode(attr);
        sourceParent.appendChild(child);
        Element grandChild = this.document.createElement("grandChild");
        grandChild.appendChild(this.document.createCDATASection("cdata"));
        child.appendChild(grandChild);
        child.appendChild(this.document.createTextNode("text"));

        Element targetParent = this.document.createElement("targetParent");

        copyDocument(sourceParent, targetParent, "abc");

        assertEquals(2, targetParent.getChildNodes().getLength());

        NodeList children = targetParent.getChildNodes();
        assertEquals(2, children.getLength());

        assertEquals("#comment", children.item(0).getNodeName());
        assertEquals("comment", children.item(0).getNodeValue());
        assertNull(children.item(0).getNamespaceURI());

        assertEquals("ns:child", children.item(1).getNodeName());
        assertNull(children.item(1).getNodeValue());
        assertEquals("abc", children.item(1).getNamespaceURI());
        NamedNodeMap attrs = children.item(1).getAttributes();
        assertEquals(1, attrs.getLength());
        assertEquals("value", attrs.getNamedItem("attr1").getNodeValue());
        assertNull(attrs.getNamedItem("attr1").getNamespaceURI());

        NodeList grandchildren = children.item(1).getChildNodes();
        assertEquals(2, grandchildren.getLength());

        assertEquals("grandChild", grandchildren.item(0).getNodeName());
        assertNull(grandchildren.item(0).getNodeValue());
        assertEquals("abc", grandchildren.item(0).getNamespaceURI());
        assertEquals(0, grandchildren.item(0).getAttributes().getLength());

        assertEquals("#text", grandchildren.item(1).getNodeName());
        assertEquals("text", grandchildren.item(1).getNodeValue());

        assertEquals(1, grandchildren.item(0).getChildNodes().getLength());
        Node grandGrandChild = grandchildren.item(0).getChildNodes().item(0);
        assertEquals("#cdata-section", grandGrandChild.getNodeName());
        assertEquals("cdata", grandGrandChild.getNodeValue());
    }

    //--------------------------------------------------------------------------
    @Test
    public void appendChildElement_throwsNullPointer_whenGivenNulls() {

        Element parent = this.document.createElement("parent");
        Element child = this.document.createElementNS("abc", "child1");
        String[] order = {"child1"};

        expectNullPointer(() -> appendChildElement(null, child, order));
        expectNullPointer(() -> appendChildElement(parent, null, order));
        expectNullPointer(() -> appendChildElement(parent, child, null));
    }

    //--------------------------------------------------------------------------
    @Test
    public void appendChildElement_throwsIlleglArgument_whenOrderTableIsEmpty() {

        Element parent = this.document.createElement("parent");
        Element child = this.document.createElementNS("abc", "child1");
        String[] order = {};

        expectIllegalArgument(() -> appendChildElement(parent, child, order));
    }

    //--------------------------------------------------------------------------
    @Test
    public void appendChildElement_throwsIlleglArgument_whenChildInNotInOrder() {

        Element parent = this.document.createElement("parent");
        Element child = this.document.createElementNS("abc", "child1");
        String[] order = {"abc", "def"};

        expectIllegalArgument(() -> appendChildElement(parent, child, order));
    }

    //--------------------------------------------------------------------------
    @Test()
    public void appendChildElement_throwsIllegalArgument_whenChildHasNoNamespace() {

        Element parent = this.document.createElement("parent");
        Element child = this.document.createElement("child1");

        expectIllegalArgument(() -> appendChildElement(parent, child, arrayOf("child1")));
    }

    //--------------------------------------------------------------------------
    @Test()
    public void appendChildElement_throwsIllegalArgument_ifParentHasNonElementChildren() {

        Element child = this.document.createElementNS("abc", "child1");

        Element parent1 = this.document.createElement("parent");
        parent1.appendChild(this.document.createCDATASection("cdata"));

        expectIllegalArgument(() -> appendChildElement(parent1, child, arrayOf("child1")));

        Element parent2 = this.document.createElement("parent");
        parent2.appendChild(this.document.createTextNode("text"));

        expectIllegalArgument(() -> appendChildElement(parent2, child, arrayOf("child1")));
    }
    
    //--------------------------------------------------------------------------
    @Test()
    public void appendChildElement_throwsIllegalArgument_ifExistingElementsAreNotInOrder() {

        Element parent = this.document.createElement("parent");
        parent.appendChild(this.document.createElementNS("abc", "child1"));
        parent.appendChild(this.document.createElementNS("abc", "child2"));
        Element child = this.document.createElementNS("abc", "child3");

        expectIllegalArgument(() -> appendChildElement(parent, child, 
                arrayOf("child1", "child3"))); // "child2" omitter from 'order' array
    }
    
    //--------------------------------------------------------------------------
    @Test()
    public void appendChildElement_throwsIllegalArgument_ifNewChildIsNotInOrder() {

        Element parent = this.document.createElement("parent");
        parent.appendChild(this.document.createElementNS("abc", "child1"));
        parent.appendChild(this.document.createElementNS("abc", "child2"));
        Element child = this.document.createElementNS("abc", "child3");

        expectIllegalArgument(() -> appendChildElement(parent, child, 
                arrayOf("child1", "child2"))); // "child3" omitted from 'order' array
    }

    //--------------------------------------------------------------------------
    @Test
    public void appendChildElement_appendChild_forProperInvocation()
            throws Exception {

        Element parent = this.document.createElement("parent");

        // append first
        appendChildElement(parent, this.document.createElementNS("abc", "child1"),
                arrayOf("child1"));

        assertEquals(1, parent.getChildNodes().getLength());
        assertEquals("child1", parent.getChildNodes().item(0).getNodeName());

        //append before first
        appendChildElement(parent, this.document.createElementNS("abc", "child0"),
                arrayOf("child0", "child1"));

        assertEquals(2, parent.getChildNodes().getLength());
        assertEquals("child0", parent.getChildNodes().item(0).getNodeName());
        assertEquals("child1", parent.getChildNodes().item(1).getNodeName());

        //append after last
        appendChildElement(parent, this.document.createElementNS("abc", "child3"),
                arrayOf("child0", "child1", "child3"));

        assertEquals(3, parent.getChildNodes().getLength());
        assertEquals("child0", parent.getChildNodes().item(0).getNodeName());
        assertEquals("child1", parent.getChildNodes().item(1).getNodeName());
        assertEquals("child3", parent.getChildNodes().item(2).getNodeName());

        //insert in between
        appendChildElement(parent, this.document.createElementNS("abc", "child2"),
                arrayOf("child0", "child1", "child2", "child3"));

        assertEquals(4, parent.getChildNodes().getLength());
        assertEquals("child0", parent.getChildNodes().item(0).getNodeName());
        assertEquals("child1", parent.getChildNodes().item(1).getNodeName());
        assertEquals("child2", parent.getChildNodes().item(2).getNodeName());
        assertEquals("child3", parent.getChildNodes().item(3).getNodeName());
    }
}
