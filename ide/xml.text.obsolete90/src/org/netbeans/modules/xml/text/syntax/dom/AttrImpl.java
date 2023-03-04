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

package org.netbeans.modules.xml.text.syntax.dom;

import org.netbeans.modules.xml.text.syntax.XMLSyntaxSupport;
import org.netbeans.modules.xml.text.syntax.XMLTokenIDs;
import java.util.*;
import javax.swing.text.BadLocationException;

import org.w3c.dom.*;
import org.netbeans.modules.xml.text.syntax.*;
import org.netbeans.modules.xml.spi.dom.*;
import org.netbeans.editor.*;
import org.netbeans.modules.xml.*;

/**
 * Holds attribute: name-value pairs. It is returned by <code>Tag</code>
 * syntax nodes. Note that attributes are not a part part of DOM Node
 * hiearchy, but they are rather properties of <code>Element</code>.
 * It matches well with fact taht attributes are not represented by
 * <code>SyntaxNode</code>s.
 *
 * @author Petr Kuzel
 * @author asgeir@dimonsoftware.com
 */
public class AttrImpl extends AbstractNode implements Attr, XMLTokenIDs {
    
    private TokenItem first;
    
    private Element parent;
    
    private XMLSyntaxSupport syntax;  // that produced us
    
    AttrImpl(XMLSyntaxSupport syntax, TokenItem first, Element parent) {
        this.parent = parent;
        this.first = first;
        this.syntax = syntax;
    }
    
    public TokenItem getFirstToken() {
        return first;
    }
    
    /**
     * @return list of child nodes (Text or EntityReference), never <code>null</code>
     */
    public NodeList getChildNodes() {
        List list = new ArrayList(3);
        
        Node node = getFirstChild();
        while (node != null) {
            list.add(node);
            node = node.getNextSibling();
        }
        
        return new NodeListImpl(list);
    }
    
    /**
     * Get next sibling for non syntax element text.
     */
    Node getPreviousSibling(Text text) {
        return null;  //!!! todo
    }
    
    Node getPreviousSibling(EntityReferenceImpl ref) {
        return null;
    }
    
    // NOTE:  This method has to be implemented, unless the getChildNodes() method
    // will only return the first child node.
    Node getNextSibling(Text text) {
        return null;
    }
    
    Node getNextSibling(EntityReferenceImpl ref) {
        return null;
    }
    
    public Node getNextSibling() {
        return null;  //according to DOM-1spec
    }
    
    public Node getPreviousSibling() {
        return null;  //according to DOM-1 spec
    }
    
    public Node getFirstChild() {
        TokenItem next = first;
        for (; next != null; next = next.getNext()) {
            if (next.getTokenID() == VALUE) {
                // fuzziness to relax minor tokenization changes
                String image = next.getImage();
                if (image.length() == 1) {
                    char test = image.charAt(0);
                    if (test == '"' || test == '\'') {
                        next = next.getNext();
                    }
                }
                break;  // we are after opening "'"
            }
        }
        if (next == null) return null;
        if (next.getTokenID() == VALUE) {
            return new TextImpl(syntax, next, this);  //!!! strip out ending "'", return standalone "'" token
        } else {
            throw new RuntimeException("Not recognized yet: " + next.getTokenID());
        }
    }
    
    public Node getLastChild() {
        throw new RuntimeException("Not implemented yet");
    }
    
    public String getNodeName() {
        return getName();
    }
    
    public String getName() {
        return first.getImage();
    }
    
    public boolean getSpecified() {
        return true;
    }
    
    public void setValue(String value) {
        // Initialize oldValueStartPos and oldValueLength parameters
        int oldValueStartPos = -1;
        int oldValueLength = 0;
        boolean notClosed = false;
        char firstChar = '"';
        char lastChar = '\0';
        TokenItem next = first;
        for (; next != null; next = next.getNext()) {
            TokenID nextId = next.getTokenID();
            
            if (oldValueStartPos != -1 && nextId != VALUE  && nextId != CHARACTER) {
                break;
            }
            
            String nextImage = next.getImage();
            
            String actualImage = Util.actualAttributeValue(nextImage);
            if (!nextImage.equals(actualImage)) {
                notClosed = true;
                nextImage = actualImage;
            }
            
            if (nextId == VALUE && oldValueStartPos == -1 && nextImage.length() > 0) {
                oldValueStartPos = next.getOffset();
                if (nextImage.charAt(0) == '"' || nextImage.charAt(0) == '\'') {
                    firstChar = nextImage.charAt(0);
                    oldValueStartPos++;
                    oldValueLength--;
                }
            }
            
            if (oldValueStartPos != -1 && nextImage.length() > 0) {
                oldValueLength += nextImage.length();
                lastChar = nextImage.charAt(nextImage.length()-1);
            }
            
            if (notClosed) {
                break;
            }
        }
        
        if (lastChar == firstChar) {
            oldValueLength--;
        }
        
        // Replace known entities
        value = Util.replaceCharsWithEntityStrings(value);
        
        // Close the attribute if it was non-closed
        if (notClosed) {
            value += firstChar;
        }
        
        // Replace the text in the document
        BaseDocument doc = (BaseDocument)syntax.getDocument();
        doc.atomicLock();
        try {
            doc.remove(oldValueStartPos, oldValueLength);
            doc.insertString(oldValueStartPos, value, null);
            doc.invalidateSyntaxMarks();
        } catch( BadLocationException e ) {
            throw new DOMException(DOMException.INVALID_STATE_ERR , e.getMessage());
        } finally {
            doc.atomicUnlock();
        }
        
        // Update the status of this object
        try {
            int endOffset = oldValueStartPos + oldValueLength;
            if (endOffset > doc.getLength()) {
                endOffset = doc.getLength();
            }
            first = syntax.getTokenChain(first.getOffset(), endOffset);
        } catch (BadLocationException e) {
            throw new DOMException(DOMException.INVALID_STATE_ERR , e.getMessage());
        }
    }
    
    public void setNodeValue(String value) {
        setValue(value);
    }
    
    /**
     * Iterate over children to get value.
     * @return a String never <code>null</code>
     */
    public String getNodeValue() {
        return getValue();
    }
    
    public String getValue() {
        // Find the first value token.  Should be after "name="
        TokenItem next = first;
        for (; next != null; next = next.getNext()) {
            if (next.getTokenID() == VALUE) {
                break;
            }
        }
        
        // Add values of all value and character entity
        StringBuffer buf = new StringBuffer();
        while (next != null && (next.getTokenID() == VALUE || next.getTokenID() == CHARACTER)) {
            String image = next.getImage();
            String actual = Util.actualAttributeValue(image);
            if (!image.equals(actual)) {
                buf.append(actual);
                break;
            } else {
                buf.append(image);
            }
            next = next.getNext();
        }
        
        // Remove " and ' around the attribute value
        if (buf.length() > 0) {
            char firstChar = buf.charAt(0);
            if (firstChar == '"' ||  firstChar == '\'') {
                buf.deleteCharAt(0);
                if (buf.length() > 0 && buf.charAt(buf.length()-1) == firstChar) {
                    buf.deleteCharAt(buf.length()-1);
                }
            }
        }
        
        return Util.replaceEntityStringsWithChars(buf.toString());
    }
    
    public short getNodeType() {
        return Node.ATTRIBUTE_NODE;
    }
    
    public Node getParentNode() {
        return null;  //accordnig to DOM-1 specs
    }
    
    public Element getOwnerElement() {
//        ((Tag)parent).retokenizeObject();
        return parent;
    }
    
    /**
     * Get owner document or <code>null</code>
     */
    public org.w3c.dom.Document getOwnerDocument() {
        Node parent = getOwnerElement();
        if (parent == null) return null;
        return parent.getOwnerDocument();
    }
    
    /**
     * Return string representation of the object for debug purposes.
     */
    public String toString() {
        return "Attr(" + getName() + "='" + getValue() + "')";
    }
    
}
