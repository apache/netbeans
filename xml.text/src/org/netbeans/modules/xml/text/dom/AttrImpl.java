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

package org.netbeans.modules.xml.text.dom;

import org.netbeans.modules.xml.text.api.dom.XMLSyntaxSupport;
import java.util.*;
import javax.swing.text.BadLocationException;
import org.netbeans.api.editor.document.AtomicLockDocument;
import org.netbeans.api.editor.document.LineDocument;
import org.netbeans.api.editor.document.LineDocumentUtils;

import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.modules.xml.spi.dom.AbstractNode;
import org.netbeans.modules.xml.spi.dom.NodeListImpl;
import org.netbeans.modules.xml.text.api.dom.SyntaxElement;
import org.netbeans.modules.xml.text.api.XMLTextUtils;
import org.netbeans.modules.xml.text.api.dom.XMLSyntaxSupport.SequenceCallable;
import org.openide.util.Exceptions;
import org.w3c.dom.DOMException;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

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
public class AttrImpl extends AbstractNode implements org.w3c.dom.Attr, SyntaxElement {
    private Token<XMLTokenId> first;
    private SyntaxElement parent;
    private XMLSyntaxSupport syntax;  // that produced us
    private int index;
    
    AttrImpl(XMLSyntaxSupport syntax, Token<XMLTokenId> first, SyntaxElement parent, int index) {
        this.parent = parent;
        this.first = first;
        this.syntax = syntax;
        this.index = index;
    }
    
    public Token getFirstToken() {
        return first;
    }

    @Override
    public SyntaxElement getParentElement() {
        return parent;
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

    @Override
    public SyntaxElement getNext() {
        if (parent == null) {
            return null;
        }
        NamedNodeMap attrs = ((Element)parent.getNode()).getAttributes();
        if (index < attrs.getLength() - 1) {
            return (SyntaxElement)attrs.item(index + 1);
        }  else {
            return parent.getNext();
        }
    }

    @Override
    public SyntaxElement getPrevious() {
        if (parent == null) {
            return null;
        }
        NamedNodeMap attrs = ((Element)parent.getNode()).getAttributes();
        if (index > 0) {
            return (SyntaxElement)attrs.item(index - 1);
        }  else {
            return parent.getPrevious();
        }
    }

    @Override
    public int getElementLength() {
        return getLength();
    }

    @Override
    public int getElementOffset() {
        return first.offset(null);
    }

    @Override
    public int getType() {
        return Node.ATTRIBUTE_NODE;
    }

    @Override
    public Node getNode() {
        return this;
    }
    
    /**
     * Get next sibling for non syntax element text.
     */
    Node getPreviousSibling(Text text) {
        return null;  //!!! todo
    }
    
    Node getPreviousSibling(EntityReference ref) {
        return null;
    }
    
    // NOTE:  This method has to be implemented, unless the getChildNodes() method
    // will only return the first child node.
    Node getNextSibling(Text text) {
        return null;
    }
    
    Node getNextSibling(EntityReference ref) {
        return null;
    }
    
    public Node getNextSibling() {
        if (parent == null) {
            return null;
        }
        NamedNodeMap attrs = ((Element)parent.getNode()).getAttributes();
        if (index < attrs.getLength() - 1) {
            return attrs.item(index + 1);
        }  else {
            return null;
        }
    }
    
    public Node getPreviousSibling() {
        if (parent == null) {
            return null;
        }
        NamedNodeMap attrs = ((Element)parent.getNode()).getAttributes();
        if (index > 0) {
            return attrs.item(index - 1);
        }  else {
            return null;
        }
    }
    
    private Node getFirstChildLocked(TokenSequence ts) {
        while (ts.moveNext()) {
            Token<XMLTokenId> t = ts.token();
            if (t.id() == XMLTokenId.VALUE) {
                // fuzziness to relax minor tokenization changes
                CharSequence image = t.text();
                if (image.length() == 1) {
                    char test = image.charAt(0);
                    if (test == '"' || test == '\'') {
                        if (ts.moveNext()) {
                            t = ts.token();
                        } else {
                            return null;
                        }
                    }
                }
                
                if (t.id() == XMLTokenId.VALUE) {
                    return new TextImpl(syntax, t, ts.offset(), ts.offset() + t.length(), this);
                } else {
                    return null;
                }
            }
        }
        return null;
    }
    
    public Node getFirstChild() {
        try {
            return syntax.runWithSequence(first, this::getFirstChildLocked);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }
    
    public Node getLastChild() {
        return getFirstChild();
    }
    
    public String getNodeName() {
        return getName();
    }
    
    public String getName() {
        return first.text().toString();
    }
    
    public boolean getSpecified() {
        return true;
    }
    
    public void setValue(String value) {
        class H implements SequenceCallable {
            int oldValueStartPos = -1;
            int oldValueLength = 0;
            boolean notClosed = false;
            char firstChar = '"';
            char lastChar = '\0';
            
            @Override
            public Object call(TokenSequence ts) throws BadLocationException {
                Token<XMLTokenId> next = first;
                while (ts.moveNext()) {
                    next = ts.token();
                    XMLTokenId nextId = next.id();

                    if (oldValueStartPos != -1 && nextId != XMLTokenId.VALUE  && nextId != XMLTokenId.CHARACTER) {
                        break;
                    }

                    String nextImage = next.text().toString();

                    String actualImage = XMLTextUtils.actualAttributeValue(nextImage);
                    if (!nextImage.equals(actualImage)) {
                        notClosed = true;
                        nextImage = actualImage;
                    }

                    if (nextId == XMLTokenId.VALUE && oldValueStartPos == -1 && nextImage.length() > 0) {
                        oldValueStartPos = ts.offset();
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

                return null;
            }
        }
        H h = new H();
        try {
            syntax.runWithSequence(first.offset(null), h);
        } catch (BadLocationException ex) {
            throw new DOMException(DOMException.INVALID_STATE_ERR , ex.getMessage());
        }

        // Replace known entities
        value = XMLTextUtils.replaceCharsWithEntityStrings(value);

        // Close the attribute if it was non-closed
        if (h.notClosed) {
            value += h.firstChar;
        }

        // Replace the text in the document
        final LineDocument doc = syntax.getDocument();
        AtomicLockDocument ald = LineDocumentUtils.asRequired(doc, AtomicLockDocument.class);
        final BadLocationException[] ex = new BadLocationException[1];
        final int fOldValStartPos = h.oldValueStartPos;
        final int fOldValLen = h.oldValueLength;
        final String fValue = value;
        ald.runAtomic(() -> {;
            try {
                doc.remove(fOldValStartPos, fOldValLen);
                doc.insertString(fOldValStartPos, fValue, null);
                //doc.invalidateSyntaxMarks();
            } catch( BadLocationException e ) {
                ex[0] = e;
            }
        });
        if (ex[0] != null) {
            throw new DOMException(DOMException.INVALID_STATE_ERR , ex[0].getMessage());
        }

        // Update the status of this object
        try {
            int endOffset = fOldValStartPos + fOldValLen;
            if (endOffset > doc.getLength()) {
                endOffset = doc.getLength();
            }
            syntax.runWithSequence(first.offset(null), 
                (TokenSequence ts) -> {
                first = ts.token();
                return null;
            });
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
    
    private String getValueLocked(TokenSequence ts) {
        StringBuilder sb = new StringBuilder();
        boolean valStarted = false;
        V: while (ts.moveNext()) {
            Token<XMLTokenId> t = ts.token();
            switch (t.id()) {
                case CHARACTER:
                    if (!valStarted) {
                        return null;
                    }
                    // fall through
                case VALUE:  {
                    String image = t.text().toString();
                    String actual = XMLTextUtils.actualAttributeValue(image);
                    valStarted = true;
                    if (!image.equals(actual)) {
                        sb.append(actual);
                        break;
                    } else {
                        sb.append(image);
                    }
                    break;
                }
                case WS:
                case OPERATOR:
                    break;
                default:
                    break V;
            }
        }
        // Remove " and ' around the attribute value
        if (sb.length() > 0) {
            char firstChar = sb.charAt(0);
            if (firstChar == '"' ||  firstChar == '\'') {
                sb.deleteCharAt(0);
                if (sb.length() > 0 && sb.charAt(sb.length()-1) == firstChar) {
                    sb.deleteCharAt(sb.length()-1);
                }
            }
        }
        return XMLTextUtils.replaceEntityStringsWithChars(sb.toString());
    }
    
    public String getValue() {
        try {
            return syntax.runWithSequence(first, this::getValueLocked);
        } catch (BadLocationException ex) {
            Exceptions.printStackTrace(ex);
            return null;
        }
    }
    
    public short getNodeType() {
        return Node.ATTRIBUTE_NODE;
    }
    
    public Node getParentNode() {
        return null;  //accordnig to DOM-1 specs
    }
    
    public Element getOwnerElement() {
//        ((Tag)parent).retokenizeObject();
        if (parent == null) {
            return null;
        }
        return (Element)parent.getNode();
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
