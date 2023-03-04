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

package org.netbeans.modules.xml.text.dom;

import org.netbeans.modules.xml.text.api.dom.XMLSyntaxSupport;
import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import javax.swing.text.BadLocationException;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.modules.xml.spi.dom.NamedNodeMapImpl;
import org.netbeans.modules.xml.spi.dom.ROException;
import org.netbeans.modules.xml.spi.dom.UOException;
import org.netbeans.modules.xml.text.api.dom.TagElement;
import org.openide.util.Exceptions;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Attr;

/**
 * Represents tag syntax element. It also represent DOM <code>Element</code>.
 * This duality means that one document element is represented by
 * two DOM <code>Element</code> instances - one for start tag and one for
 * end tag. This is hidden during document traversal but never relay on
 * <code>equals</code>. The <code>equals</code> is used for syntax element
 * purposes.
 */
public abstract class Tag extends SyntaxNode implements org.w3c.dom.Element, TagElement {
    
    protected NamedNodeMap domAttributes;
    
    protected String name;
    
    Tag(XMLSyntaxSupport support, Token from, int start, int end, String name) {
        super(support, from, start, end);
        this.name = name;
    }
    
    public final short getNodeType() {
        return Node.ELEMENT_NODE;
    }
    
    public final String getNodeName() {
        return getTagName();
    }
    
    public final String getTagName() {
        return name;
    }
    
    private Map parseAttributes(TokenSequence ts) {
        HashMap map = new LinkedHashMap(3);

        int index = 0;
        SCAN_LOOP:
        while (ts.moveNext()) {
            Token<XMLTokenId> next = ts.token();
            XMLTokenId id = next.id();
            String name;
            String value;
            if (id == XMLTokenId.ARGUMENT) {
                Token attributeStart = next;
                name = next.text().toString();
                while (next.id() != XMLTokenId.VALUE) {
                    if (!ts.moveNext()) {
                        break SCAN_LOOP;
                    }
                    next = ts.token();
                    if (next.id() == XMLTokenId.ERROR) {
                        break SCAN_LOOP;
                    }
                }

                // fuzziness to relax minor tokenization changes
                String image = next.text().toString();
                char test = image.charAt(0);
                if (image.length() == 1) {
                    if (test == '"' || test == '\'') {
                        if (!ts.moveNext()) {
                            break SCAN_LOOP;
                        }
                        next = ts.token();
                    }
                }

                value = next.text().toString();

                Object key = NamedNodeMapImpl.createKey(name);
                map.put(key, new AttrImpl(support, attributeStart, this, index++));

                next = skipAttributeValue(ts, test);
                if (next == null) {
                    break SCAN_LOOP;
                }
            } else if (id == XMLTokenId.WS) {
                // just skip
            } else {
                break; // end of element markup
            }
        }
        return map;
    }
    
    private static Token skipAttributeValue(TokenSequence ts, char delim) {
        do { 
            Token t = ts.token();
            if (t.text().charAt(t.length() - 1) == delim) {
                return ts.moveNext() ? ts.token() : null;
            }
        } while (ts.moveNext());
        return null;
    }
    
    /**
     * Create properly bound attributes and cache results.
     * Parse attributes from first token.
     */
    public synchronized NamedNodeMap getAttributes() {
        try {
            return new NamedNodeMapImpl(
                    support.runWithSequence(offset, (TokenSequence ts) -> {
                        return parseAttributes(ts);
                    })
            );
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }
    
    public String getAttribute(String name) {
        Attr attribute = getAttributeNode(name);
        return attribute != null ? attribute.getValue() : "";
    }
    
    public final void setAttribute(String name, String value) {
//        NamedNodeMap attributes = getAttributes();
//        Node attr = attributes.getNamedItem(name);
//        if (attr != null) {
//            attr.setNodeValue(value);
//        } else {
//            String stringToInsert = " " + name + "=" + '"' + value + '"';
//
//            // Get the document and lock it
//            BaseDocument doc = (BaseDocument)support.getDocument();
//            doc.atomicLock();
//            try {
//                // An attribute with the name was not found for the element
//                // Let's add it to the end
//                int insertStart = offset + length - 1;
//
//                SCAN_LOOP:
//                    for (TokenItem next = first().getNext(); next != null; next = next.getNext()) {
//                        TokenID id = next.getTokenID();
//                        if (id == ARGUMENT) {
//                            while (next.getTokenID() != VALUE) {
//                                next = next.getNext();
//                                if (next == null) break SCAN_LOOP;
//                            }
//
//                            if (next == null) break SCAN_LOOP;
//
//                            String image = next.getImage();
//                            char test = image.charAt(0);
//
//                            while (next.getTokenID() == VALUE || next.getTokenID() == CHARACTER) {
//                                String actualValue = Util.actualAttributeValue(image);
//                                if (!actualValue.equals(image)) {
//                                    insertStart = next.getOffset() + actualValue.length();
//                                    break SCAN_LOOP;
//                                }
//                                next = next.getNext();
//                                if (next == null) break SCAN_LOOP;
//
//                                // Check if this is the last token in the element and set the
//                                // insertStart if it is
//                                image = next.getImage();
//                                insertStart = next.getOffset();
//                                if (image.length() > 0 && image.charAt(image.length() - 1) == '>') {
//                                    // The element is closing
//                                    insertStart += image.length() - 1;
//                                    if (image.length() > 1 && image.charAt(image.length() - 2) == '/') {
//                                        // We have a closed element at the form <blu/>
//                                        insertStart--;
//                                    }
//                                }
//                            }
//
//                            if (next == null) break SCAN_LOOP;
//                        } else if (id == WS) {
//                            // just skip
//                        } else {
//                            break; // end of element markup
//                        }
//                    }
//
//                    doc.insertString(insertStart, stringToInsert, null);
//                    doc.invalidateSyntaxMarks();
//                } catch( BadLocationException e ) {
//                    throw new DOMException(DOMException.INVALID_STATE_ERR , e.getMessage());
//                } finally {
//                    doc.atomicUnlock();
//                }
//        }
        
        // Update this object's member variables
//        retokenizeObject();
    }
    
    public final void removeAttribute(String name) {
        throw new ROException();
    }
    
    public Attr getAttributeNode(String name) {
        NamedNodeMap map = getAttributes();
        Node node = map.getNamedItem(name);
        return (Attr) node;
    }
    
    public final Attr setAttributeNode(Attr attribute) {
        throw new ROException();
    }
    
    public final Attr removeAttributeNode(Attr attribute) {
        throw new ROException();
    }
    
    public NodeList getElementsByTagName(String name) {
        throw new ROException();
    }
    
    /**
     * Returns previous sibling by locating pairing start tag
     * and asking it for previous non-start tag SyntaxNode.
     */
    public Node getPreviousSibling() {
        SyntaxNode prev = getStartTag();
        if (prev == null) return null;
        prev = findPrevious(prev);
        if (prev instanceof StartTag) {
            return null;
        } else {
            return prev;
        }
    }
    
    /**
     * Returns next sibling by locating pairing end tag
     * and asking it for next non-end tag SyntaxNode.
     */
    public Node getNextSibling() {
        SyntaxNode next = getEndTag();
        if (next == null) return null;
        next = findNext(next);
        if (next instanceof EndTag) {
            return null;
        } else {
            return next;
        }
    }
    
    public Node getFirstChild() {
        NodeList list = getChildNodes();
        if (list.getLength() == 0) return null;
        return getChildNodes().item(0);
    }
    
    public Node getLastChild() {
        NodeList list = getChildNodes();
        if (list.getLength() == 0) return null;
        return list.item(list.getLength());
    }
    
    public abstract Tag getStartTag();
    
    public abstract Tag getEndTag();
        
    // unsupported DOM level 2 ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~    
    public String getAttributeNS(String namespaceURI, String localName) {
        throw new UOException();
    }
    
    public void setAttributeNS(String namespaceURI, String qualifiedName, String value) {
        throw new UOException();
    }
    
    public void removeAttributeNS(String namespaceURI, String localName) {
        throw new UOException();
    }
    
    public Attr getAttributeNodeNS(String namespaceURI, String localName) {
        throw new UOException();
    }
    
    public Attr setAttributeNodeNS(Attr newAttr) {
        throw new UOException();
    }
    
    public NodeList getElementsByTagNameNS(String namespaceURI, String localName) {
        throw new UOException();
    }
    
    public boolean hasAttribute(String name) {
        throw new UOException();
    }
    
    public boolean hasAttributeNS(String namespaceURI, String localName) {
        throw new UOException();
    }
    
    /**
     * We guarantee DOM Node equality by using Java Object's equals.
     * It's potentionally dangerous as it mixes StartTags and EndTags.
     * Never put this object into vector of so until you assumes DOM Node
     * equality.
     * <p>
     * I would appreciate a methos at DOM that would define
     * Node equals not Objevt's equals.
     */
    public final boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj instanceof Tag) {
            Tag tag = (Tag) obj;
            Tag t1 = tag.getStartTag();
            Tag t2 = getStartTag();
            if (t1 == null || t2 == null) return false;
            return t1.superEquals(t2);
        }
        return false;
    }

    private boolean superEquals(Tag tag) {
        return super.equals(tag);
    }

    /**
     * The same as for equals it's DOM node hashcode.
     */
    public final int hashCode() {
        Tag tag = getStartTag();
        if (tag == null || tag == this) {
            return super.hashCode();
        } else {
            return tag.hashCode();
        }
    }
}

