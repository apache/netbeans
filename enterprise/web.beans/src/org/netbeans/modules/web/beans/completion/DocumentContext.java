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

package org.netbeans.modules.web.beans.completion;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.editor.TokenItem;
import org.netbeans.modules.xml.text.api.dom.SyntaxElement;
import org.netbeans.modules.xml.text.api.dom.XMLSyntaxSupport;
import org.w3c.dom.Attr;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Tracks context information for XML files
 * 
 * @author Rohan Ranade
 */
public class DocumentContext {

    //public static final String PREFIX = "ns"; //NOI18N
    //public static final String XSI_SCHEMALOCATION = "schemaLocation"; //NOI18N
    //public static final String XSI_NONS_SCHEMALOCATION = "noNamespaceSchemaLocation"; //NOI18N

    private static final Logger LOGGER = Logger.getLogger(DocumentContext.class.getName());
    private Document document;
    private XMLSyntaxSupport syntaxSupport;
    private int caretOffset = -1;
    private SyntaxElement element;
    private Token<XMLTokenId> token;
    private int tokenOffset;
    private boolean valid = false;
    private SyntaxElement docRoot;
    private String defaultNamespace;
    private HashMap<String, String> declaredNamespaces =
            new HashMap<String, String>();
    private String schemaLocation;
    private String noNamespaceSchemaLocation;

    DocumentContext(Document document) {
        this.document = document;
        try {
            this.syntaxSupport = XMLSyntaxSupport.getSyntaxSupport(document);
        } catch (ClassCastException cce) {
            LOGGER.log(Level.FINE, cce.getMessage());
            this.syntaxSupport = XMLSyntaxSupport.createSyntaxSupport(document);
        }
    }

    public void reset(int caretOffset) {
        this.caretOffset = caretOffset;
        initialize();
    }

    private void initialize() {
        valid = true;
        declaredNamespaces.clear();
        try {
            element = syntaxSupport.getElementChain(caretOffset);
            int[] bounds = new int[1];
            token = syntaxSupport.getTokenAtPosition(caretOffset, bounds);
            tokenOffset = bounds[0];
            this.docRoot = ContextUtilities.getRoot(element);
            populateNamespaces();
        } catch (BadLocationException ex) {
            // No context support available in this case
            valid = false;
        }
    }

    public boolean isValid() {
        return this.valid;
    }

    public XMLTokenId getCurrentTokenId() {
        if (isValid()) {
            return token.id();
        } else {
            return null;
        }
    }

    public Token<XMLTokenId> getCurrentToken() {
        if (isValid()) {
            return token;
        } else {
            return null;
        }
    }

    public String getCurrentTokenImage() {
        if (isValid()) {
            return token.text().toString();
        } else {
            return null;
        }
    }

    public SyntaxElement getCurrentElement() {
        return this.element;
    }
    
    public Document getDocument() {
        return this.document;
    }

    public String lookupNamespacePrefix(String prefix) {
        return declaredNamespaces.get(prefix);
    }
    
    public String getNamespacePrefix(String namespace) {
        for(Entry<String, String> entry : declaredNamespaces.entrySet()) {
            if(entry.getValue().equals(namespace)) {
                return entry.getKey();
            }
        }
        
        return null;
    }
    
    public Collection<String> getDeclaredNamespaces() {
        return declaredNamespaces.values();
    }

    public SyntaxElement getDocRoot() {
        return docRoot;
    }

    public int getCaretOffset() {
        return this.caretOffset;
    }
    
    public int getCurrentTokenOffset() {
        return tokenOffset;
    }

    private void populateNamespaces() {
        // Find the a start or empty tag just before the current syntax element.
        SyntaxElement element = this.element;
        while (element != null && !syntaxSupport.isStartTag(element) && !syntaxSupport.isEmptyTag(element)) {
            element = element.getPrevious();
        }
        if (element == null) {
            return;
        }

        // To find all namespace declarations active at the caret offset, we
        // need to look at xmlns attributes of the current element and its ancestors.
        Node node = (Node)element;
        while (node != null && element != null) {
            if (syntaxSupport.isStartTag(element) || syntaxSupport.isEmptyTag(element)) {
                NamedNodeMap attributes = node.getAttributes();
                for (int index = 0; index < attributes.getLength(); index++) {
                    Attr attr = (Attr) attributes.item(index);
                    String attrName = attr.getName();
                    String attrValue = attr.getValue();
                    if (attrName == null || attrValue == null) {
                        continue;
                    }
                    String prefix = ContextUtilities.getPrefixFromNamespaceDeclaration(attrName);
                    if (prefix == null) {
                        continue;
                    }
                    // Avoid overwriting a namespace declaration "closer" to the caret offset.
                    if (!declaredNamespaces.containsKey(prefix)) {
                        declaredNamespaces.put(prefix, attrValue);
                    }
                }
            }
            node = node.getParentNode();
        }
    }
    
    public String getNoNamespaceSchemaLocation() {
        return noNamespaceSchemaLocation;
    }

    public String getSchemaLocation() {
        return schemaLocation;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DocumentContext other = (DocumentContext) obj;
        if (this.document != other.document && (this.document == null || !this.document.equals(other.document))) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 61 * hash + (this.document != null ? this.document.hashCode() : 0);
        return hash;
    }
    
    public XMLSyntaxSupport getSyntaxSupport() {
        return syntaxSupport;
    }
}
