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
package org.netbeans.modules.hibernate.hyperlink;

import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.hibernate.editor.ContextUtilities;
import org.netbeans.modules.hibernate.editor.DocumentContext;
import org.netbeans.modules.hibernate.editor.EditorContextFactory;
import org.netbeans.modules.xml.text.api.dom.SyntaxElement;
import org.openide.filesystems.FileObject;
import org.w3c.dom.Node;

/**
 *
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM), Dongmei Cao
 */
public final class HyperlinkEnv {

    private Document document;
    private SyntaxElement currentTag;
    private String attribName;
    private String valueString;
    private int offset;
    private Token<XMLTokenId> token;
    private DocumentContext documentContext;

    public static enum Type {

        ATTRIB_VALUE,
        ATTRIB,
        TEXT,
        NONE;

        public boolean isValueHyperlink() {
            return this == Type.ATTRIB_VALUE;
        }

        public boolean isAttributeHyperlink() {
            return this == Type.ATTRIB;
        }
    };

    private Type type = Type.NONE;
                  
    public HyperlinkEnv(Document document, int offset) {
        this.document = document;
        this.offset = offset;
        this.documentContext = EditorContextFactory.getDocumentContext(document, offset);
        if(documentContext.isValid()) {
            currentTag = documentContext.getCurrentElement();
            attribName = ContextUtilities.getAttributeTokenImage(documentContext);
            token = documentContext.getCurrentToken();
            
            if (ContextUtilities.isValueToken(documentContext.getCurrentToken())) {
                type = Type.ATTRIB_VALUE;
                currentTag = documentContext.getCurrentElement();
                attribName = ContextUtilities.getAttributeTokenImage(documentContext);
                token = documentContext.getCurrentToken();
                valueString = token.text().toString();
                valueString = valueString.substring(1, valueString.length() - 1); // Strip quotes
            } else if (ContextUtilities.isAttributeToken(documentContext.getCurrentToken())) {
                type = Type.ATTRIB;
                currentTag = documentContext.getCurrentElement();
                token = documentContext.getCurrentToken();
                attribName = token.text().toString();
            }
        }
    }

    public String getAttribName() {
        return attribName;
    }

    public SyntaxElement getCurrentTag() {
        if (currentTag.getType() == Node.ELEMENT_NODE) {
            return currentTag;
        } else {
            return null;
        }
    }

    public Document getDocument() {
        return document;
    }

    public String getTagName() {
        return getCurrentTag() != null ? getCurrentTag().getNode().getNodeName(): null;
    }

    public String getValueString() {
        return valueString;
    }

    public Type getType() {
        return type;
    }

    public Token<XMLTokenId> getToken() {
        return token;
    }
    
    public int getTokenOffset() {
        return documentContext.getCurrentTokenOffset();
    }

    public int getOffset() {
        return offset;
    }
    
    public DocumentContext getDocumentContext() {
        return this.documentContext;
    }
    
    public FileObject getFile() {
        return NbEditorUtilities.getFileObject(document);
    }
}
