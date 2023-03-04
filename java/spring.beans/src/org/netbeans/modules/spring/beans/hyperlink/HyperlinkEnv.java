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
package org.netbeans.modules.spring.beans.hyperlink;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.xml.lexer.XMLTokenId;
import org.netbeans.editor.BaseDocument;
import org.netbeans.modules.editor.NbEditorUtilities;
import org.netbeans.modules.spring.beans.editor.ContextUtilities;
import org.netbeans.modules.spring.beans.editor.DocumentContext;
import org.netbeans.modules.spring.beans.editor.SpringXMLConfigEditorUtils;
import org.netbeans.modules.xml.text.api.dom.SyntaxElement;
import org.openide.filesystems.FileObject;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 *
 * @author Rohan Ranade (Rohan.Ranade@Sun.COM)
 */
public final class HyperlinkEnv {

    private BaseDocument baseDocument;
    private String attribName;
    private String valueString;
    private int offset;
    private FileObject fileObject;
    private Map<String, String> declaredNamespaces;
    private int tokenStartOffset;
    private int tokenEndOffset;
    private String tokenImage;
    private int beanTagOffset;
    private String tagName;
    private Map<String, String> beanAttribs;

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
        this.baseDocument = (BaseDocument) document;
        this.fileObject = NbEditorUtilities.getFileObject(baseDocument);
        this.offset = offset;
        
        baseDocument.readLock();
        try {
            initialize();
        } finally {
            baseDocument.readUnlock();
        }
    }

    private void initialize() {
        SyntaxElement currentTag = null;
        DocumentContext documentContext = DocumentContext.create(baseDocument, offset);
        if (documentContext == null) {
            return;
        }
        
        declaredNamespaces = documentContext.getDeclaredNamespacesMap();

        Token<XMLTokenId> token = documentContext.getCurrentToken();
        if (token == null) {
            return;
        }
        
        tokenStartOffset = documentContext.getCurrentTokenOffset();
        tokenEndOffset = tokenStartOffset + token.length();
        tokenImage = token.text().toString();
        
        if (ContextUtilities.isValueToken(token)
                || ContextUtilities.isAttributeToken(documentContext.getCurrentToken())) {
            SyntaxElement element = documentContext.getCurrentElement();
            if (documentContext.isTag(element)) {
                currentTag  = element;
            } else {
                return;
            }
            Node beanTag = SpringXMLConfigEditorUtils.getBean(currentTag.getNode());
            if (beanTag != null) {
                beanTagOffset = documentContext.getNodeOffset(beanTag);
                beanAttribs = collectAttributes(beanTag);
            }
            tagName = currentTag.getNode().getNodeName();
        }
        
        if (ContextUtilities.isValueToken(token)) {
            type = Type.ATTRIB_VALUE;
            attribName = ContextUtilities.getAttributeTokenImage(documentContext);
            valueString = token.text().toString();
            valueString = valueString.substring(1, valueString.length() - 1); // Strip quotes
        } else if (ContextUtilities.isAttributeToken(documentContext.getCurrentToken())) {
            type = Type.ATTRIB;
            attribName = token.text().toString();
        }
    }
    
    private Map<String, String> collectAttributes(Node currentTag) {
        Map<String, String> attribsMap = new HashMap<String, String>();
        NamedNodeMap attribsNodeMap = currentTag.getAttributes();
        for(int i = 0; i < attribsNodeMap.getLength(); i++) {
            Node n = attribsNodeMap.item(i);
            attribsMap.put(n.getNodeName(), n.getNodeValue());
        }
        
        return Collections.unmodifiableMap(attribsMap);
     }
    
    public String getAttribName() {
        return attribName;
    }

    public String getTagName() {
        return tagName;
    }

    public String getValueString() {
        return valueString;
    }

    public Type getType() {
        return type;
    }

    public int getOffset() {
        return offset;
    }
    
    public FileObject getFileObject() {
        return fileObject;
    }

    public int getTokenStartOffset() {
        return tokenStartOffset;
    }

    public int getTokenEndOffset() {
        return tokenEndOffset;
    }

    public String getTokenImage() {
        return tokenImage;
    }

    public Map<String, String> getBeanAttributes() {
        return beanAttribs;
    }

    public int getBeanTagOffset() {
        return beanTagOffset;
    }
    
    public String lookupNamespacePrefix(String prefix) {
        return declaredNamespaces.get(prefix);
    }
}
