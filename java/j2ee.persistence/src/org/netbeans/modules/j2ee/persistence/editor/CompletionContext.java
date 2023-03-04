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

package org.netbeans.modules.j2ee.persistence.editor;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.modules.xml.text.api.dom.SyntaxElement;
import org.netbeans.modules.xml.text.api.dom.XMLSyntaxSupport;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

/**
 * Tracks context information for a code completion scenario
 */
public class CompletionContext {
    private List<String> existingAttributes;

    public static enum CompletionType {
        TAG,
        VALUE,
        ATTRIBUTE,
        ATTRIBUTE_VALUE,
        NONE
    };

    private static final Logger LOGGER = Logger.getLogger(CompletionContext.class.getName());
    private CompletionType completionType = CompletionType.NONE;
    private Document doc;
    private int caretOffset;
    private DocumentContext documentContext;
    private String typedChars = "";
    private char lastTypedChar;
    private XMLSyntaxSupport support;

    public CompletionContext(Document doc, int caretOffset) {
        this.doc = doc;
        this.caretOffset = caretOffset;

        try {
            this.support = XMLSyntaxSupport.getSyntaxSupport(doc);
        } catch (ClassCastException cce) {
            LOGGER.log(Level.FINE, cce.getMessage());
            this.support = XMLSyntaxSupport.createSyntaxSupport(doc);
        }
        this.documentContext = EditorContextFactory.getDocumentContext(doc, caretOffset);
        this.lastTypedChar = support.lastTypedChar();
        try {
            initContext();
        } catch (BadLocationException ex) {
            throw new IllegalStateException(ex);
        }
    }
    
    private void initContext() throws BadLocationException {
        Token<XMLTokenId> token = documentContext.getCurrentToken();
        if(token == null) {
            return;
        }
        
        boolean tokenBoundary = (documentContext.getCurrentTokenOffset() == caretOffset) 
                || ((documentContext.getCurrentTokenOffset() + token.length()) == caretOffset);
        
        XMLTokenId id = token.id();
        SyntaxElement element = documentContext.getCurrentElement();
        int tOffset = documentContext.getCurrentTokenOffset();
        
        switch (id) {
            //
            case TEXT:
                String chars = token.text().toString().trim();
                Token<XMLTokenId> previousTokenItem = support.getPreviousToken(tOffset);
                if (previousTokenItem == null) {
                    completionType = CompletionType.NONE;
                    break;
                }
                String previousTokenText = previousTokenItem.text().toString().trim();
                if (chars != null && chars.isEmpty() &&
                        previousTokenText.equals("/>")) { // NOI18N
                    completionType = CompletionType.NONE;
                    break;
                }
                if (chars != null && chars.isEmpty() &&
                        previousTokenText.equals(">")) { // NOI18N
                    completionType = CompletionType.VALUE;
                    break;
                }
                if (chars != null && !chars.startsWith("<") &&
                        previousTokenText.equals(">")) { // NOI18N

                    completionType = CompletionType.VALUE;
                    typedChars = token.text().subSequence(0, caretOffset - tOffset).toString();
                    break;
                }
                if (chars != null && !chars.equals("<") &&
                        previousTokenText.equals(">")) { // NOI18N
                    completionType = CompletionType.NONE;
                    break;
                }
                if (chars != null && chars.startsWith("<")) { // NOI18N
                    typedChars = chars.substring(1);
                }
                completionType = CompletionType.TAG;
                break;

            //start tag of an element
            case TAG:
                if (support.isEndTag(element)) {
                    completionType = CompletionType.NONE;
                    break;
                }
                if (support.isEmptyTag(element)) {
                    if (token != null &&
                            token.text().toString().trim().equals("/>")) {
                        completionType = CompletionType.NONE;
                        break;
                    }
                    if (element.getElementOffset() + 1 == this.caretOffset) {
                        completionType = CompletionType.TAG;
                        break;
                    }
                    if (caretOffset > element.getElementOffset() + 1 &&
                            caretOffset <= element.getElementOffset() + 1 +element.getNode().getNodeName().length()) {
                        completionType = CompletionType.TAG;
                        typedChars = element.getNode().getNodeName();
                        break;
                    }
                    completionType = CompletionType.ATTRIBUTE;
                    break;
                }

                if (support.isStartTag(element)) {
                    if (token != null &&
                            token.text().toString().trim().equals(">")) {
                        completionType = CompletionType.NONE;
                        break;
                    }
                    if (token != null &&
                            token.text().toString().trim().startsWith("</")) {
                        typedChars = "";
                        completionType = CompletionType.VALUE;
                        break;
                    }
                    if (element.getElementOffset() + 1 != this.caretOffset) {
                        typedChars = element.getNode().getNodeName();
                    }
                }
                
                if (element instanceof Text) {
                    if (token != null &&
                            token.text().toString().trim().startsWith("</")) {
                        Token<XMLTokenId> prevToken = support.getPreviousToken(tOffset);
                        if (prevToken == null) {
                            completionType = CompletionType.NONE;
                            break;
                        }
                        typedChars = prevToken.text().toString().trim();
                        completionType = CompletionType.VALUE;
                        break;
                    }
                }
                
                if (lastTypedChar == '>') {
                    completionType = CompletionType.VALUE;
                    break;
                }
                completionType = CompletionType.TAG;
                break;

            //user enters an attribute name
            case ARGUMENT:
                completionType = CompletionType.ATTRIBUTE;
                typedChars = token.text().toString().substring(0, caretOffset - tOffset);
                break;

            //some random character
            case CHARACTER:
            //user enters = character, we should ignore all other operators
            case OPERATOR:
                completionType = CompletionType.NONE;
                break;
            //user enters either ' or "
            case VALUE:
                if(!tokenBoundary) {
                    completionType = CompletionType.ATTRIBUTE_VALUE;
                    typedChars = token.text().subSequence(1, caretOffset - tOffset).toString();
                } else {
                    completionType = CompletionType.NONE;
                }
                break;

            //user enters white-space character
            case WS:
                completionType = CompletionType.NONE;
                int[] offset = new int[1];
                Token<XMLTokenId> prev = support.runWithSequence(tOffset,
                    (TokenSequence ts) -> {
                        Token<XMLTokenId> t  = null;
                        boolean ok;
                        while ((ok = ts.movePrevious())) {
                            t = ts.token();
                            if (t.id() != XMLTokenId.WS) {
                                break;
                            }
                        }
                        if (ok) {
                            offset[0] = ts.offset();
                            return t;
                        } else {
                            return null;
                        }
                    }
                );
                if (prev == null) {
                    completionType = CompletionType.NONE;
                    break;
                }
                int prevOffset = offset[0];

                if(prev.id() == XMLTokenId.ARGUMENT) {
                    typedChars = prev.text().toString();
                    completionType = CompletionType.ATTRIBUTE;
                } else if ((prev.id() == XMLTokenId.VALUE) ||
                        (prev.id() == XMLTokenId.TAG)) {
                    completionType = CompletionType.ATTRIBUTE;
                }
                break;

            default:
                completionType = CompletionType.NONE;
                break;
        }
    }

    public CompletionType getCompletionType() {
        return completionType;
    }
    
    public String getTypedPrefix() {
        return typedChars;
    }
    
    public Document getDocument() {
        return this.doc;
    }
    
    public DocumentContext getDocumentContext() {
        return this.documentContext;
    }
    
    public int getCaretOffset() {
        return caretOffset;
    }
    
    public Node getTag() {
        SyntaxElement element = documentContext.getCurrentElement();
        return element.getType() == Node.ELEMENT_NODE ? (Node) element : null;
    }
    
    public Token<XMLTokenId> getCurrentToken() {
        return documentContext.getCurrentToken();
    }
    
    public int getCurrentTokenOffset() {
        return documentContext.getCurrentTokenOffset();
    }
    
    private List<String> getExistingAttributesLocked(TokenSequence ts) {
        List<String> existingAttributes = new ArrayList<>();
        while (ts.movePrevious()) {
            Token<XMLTokenId> item = ts.token();
            XMLTokenId tokenId = item.id();
            if (tokenId == XMLTokenId.TAG) {
                break;
            }
            if (tokenId == XMLTokenId.ARGUMENT) {
                existingAttributes.add(item.text().toString());
            }
        }
        return existingAttributes;
    }

    public List<String> getExistingAttributes() {
        if (existingAttributes == null) {
            try {
                existingAttributes = support.runWithSequence(
                        documentContext.getCurrentTokenOffset(),
                        this::getExistingAttributesLocked
                );
            } catch (BadLocationException ex) {
            }
        }
        return existingAttributes;
    }
}
