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
