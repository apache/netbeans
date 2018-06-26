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

package org.netbeans.modules.web.beans.completion;

import javax.swing.text.BadLocationException;
import javax.xml.XMLConstants;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import static org.netbeans.api.xml.lexer.XMLTokenId.ARGUMENT;
import static org.netbeans.api.xml.lexer.XMLTokenId.OPERATOR;
import org.netbeans.editor.TokenItem;
import org.netbeans.modules.xml.text.api.dom.SyntaxElement;
import org.netbeans.modules.xml.text.api.dom.TagElement;
import org.w3c.dom.Node;

/**
 *
 * @author Rohan Ranade
 */
public final class ContextUtilities {

    private ContextUtilities() {
    }
    
    public static boolean isValueToken(Token<XMLTokenId> currentToken) {
        if(currentToken != null) {
            if (currentToken.id() == XMLTokenId.VALUE) {
                return true;
            }
        }
        
        return false;
    }
    
    public static boolean isTagToken(Token<XMLTokenId> currentToken) {
        if(currentToken != null) {
            if (currentToken.id() == XMLTokenId.TAG) {
                return true;
            }
        }
        
        return false;
    }
    
    public static boolean isAttributeToken(Token<XMLTokenId> currentToken) {
        if(currentToken != null) {
            if (currentToken.id() == XMLTokenId.ARGUMENT) {
                return true;
            }
        }
        
        return false;
    }
    
    public static Token<XMLTokenId> getAttributeToken(DocumentContext context) {
        if(context.getCurrentToken() == null ) {
            return null;
        }
        return context.getSyntaxSupport().getAttributeToken(context.getCurrentTokenOffset());
    }

    public static String getAttributeTokenImage(DocumentContext context) {
        Token<XMLTokenId> tok = getAttributeToken(context);
        if(tok != null) {
            return tok.text().toString();
        }
        
        return null;
    }
    
    /**
     * Returns the prefix from the element's tag.
     */
    public static String getPrefixFromTag(String tagName) {
        if(tagName == null) return null;
        return (tagName.indexOf(":") == -1) ? null : // NOI18N
            tagName.substring(0, tagName.indexOf(":")); // NOI18N
    }
    
    /**
     * Returns the local name from the element's tag.
     */
    public static String getLocalNameFromTag(String tagName) {
        if(tagName == null) return null;
        return (tagName.indexOf(":") == -1) ? tagName : // NOI18N
            tagName.substring(tagName.indexOf(":")+1, tagName.length()); // NOI18N
    }
    
    /**
     * Returns any prefix declared with this namespace. For example, if
     * the namespace was declared as xmlns:po, the prefix 'po' will be returned.
     * Returns null for declaration that contains no prefix.
     */
    public static String getPrefixFromNamespaceDeclaration(String namespace) {
        if (!namespace.startsWith(XMLConstants.XMLNS_ATTRIBUTE)) return null;
        int xmlnsLength = XMLConstants.XMLNS_ATTRIBUTE.length();
        if (namespace.length() == xmlnsLength) {
            return ""; // NOI18N
        }
        if (namespace.charAt(xmlnsLength) == ':') {
            return namespace.substring(xmlnsLength + 1);
        }
        return null;
    }
    
    public static String getPrefixFromNodeName(String nodeName) {
        int colonIndex = nodeName.indexOf(':');
        if (colonIndex <= 0) {
            return null;
        }
        return nodeName.substring(0, colonIndex);
    }
    
    public static SyntaxElement getRoot(SyntaxElement se) {
        SyntaxElement root = null;
        while( se != null) {
            if(se.getType() == Node.ELEMENT_NODE &&
               ((TagElement)se).isStart()) {
                root = se;
            }
            se = se.getPrevious();
        }
        
        return root;
    }
    
}

