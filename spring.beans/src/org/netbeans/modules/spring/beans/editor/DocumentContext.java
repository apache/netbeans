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

package org.netbeans.modules.spring.beans.editor;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
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

    private Document document;
    private XMLSyntaxSupport syntaxSupport;
    private int caretOffset = -1;
    private SyntaxElement element;
    private Token<XMLTokenId> token;
    private int tokenOffset;
    private HashMap<String, String> declaredNamespaces =
            new HashMap<String, String>();

    public static DocumentContext create(Document document, int caretOffset) {
        try {
            return new DocumentContext(document, caretOffset);
        } catch (BadLocationException ble) {
            return null;
        }
    }
    
    private DocumentContext(Document document, int caretOffset) throws BadLocationException {
        this.document = document;
        this.syntaxSupport = XMLSyntaxSupport.getSyntaxSupport(document);
        this.caretOffset = caretOffset;
        initialize();
    }

    private void initialize() throws BadLocationException {
        element = syntaxSupport.getElementChain(caretOffset);
        syntaxSupport.runWithSequence(caretOffset, 
            (TokenSequence ts) -> {
                token = syntaxSupport.getNextToken(caretOffset);
                tokenOffset = ts.offset();
                return null;
            }
        );
        populateNamespaces();
    }
    
    public XMLSyntaxSupport getSyntaxSupport() {
        return syntaxSupport;
    }
    
    public <T> T runWithTokenSequence(int offset, XMLSyntaxSupport.SequenceCallable<T> callable) throws BadLocationException {
        return syntaxSupport.runWithSequence(offset, callable);
    }

    public int getCurrentTokenOffset() {
        return tokenOffset;
    }
    
    public Token<XMLTokenId> getCurrentToken() {
        return token;
    }

    public String getCurrentTokenImage() {
        return token.text().toString();
    }

    public SyntaxElement getCurrentElement() {
        return this.element;
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
    
    public Map<String, String> getDeclaredNamespacesMap() {
        return declaredNamespaces;
    }

    public int getCaretOffset() {
        return this.caretOffset;
    }

    private void populateNamespaces() {
        // Find the a start or empty tag just before the current syntax element.
        SyntaxElement element = this.element;
        while (element != null && !(syntaxSupport.isStartTag(element)) && !(syntaxSupport.isEmptyTag(element))) {
            element = element.getPrevious();
        }
        if (element == null) {
            return;
        }

        // To find all namespace declarations active at the caret offset, we
        // need to look at xmlns attributes of the current element and its ancestors.
        while (element != null) {
            Node node = element.getNode();
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
            element = element.getParentElement();
        }
    }
    
    public int getNodeOffset(Node n) {
        return syntaxSupport.getNodeOffset(n);
    }
    
    public boolean isTag(SyntaxElement e) {
        return syntaxSupport.isStartTag(e) || syntaxSupport.isEmptyTag(e) || syntaxSupport.isEndTag(e);
    }
}
