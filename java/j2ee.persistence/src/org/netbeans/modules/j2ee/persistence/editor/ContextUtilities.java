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

package org.netbeans.modules.j2ee.persistence.editor;

import javax.swing.text.BadLocationException;
import javax.xml.XMLConstants;
import org.netbeans.api.lexer.Token;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.xml.lexer.XMLTokenId;
import static org.netbeans.api.xml.lexer.XMLTokenId.*;
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
            if (currentToken.id() == VALUE) {
                return true;
            }
        }
        
        return false;
    }
    
    public static boolean isTagToken(Token<XMLTokenId> currentToken) {
        if(currentToken != null) {
            if (currentToken.id() == TAG) {
                return true;
            }
        }
        
        return false;
    }
    
    public static boolean isAttributeToken(Token<XMLTokenId> currentToken) {
        if(currentToken != null) {
            if (currentToken.id() == ARGUMENT) {
                return true;
            }
        }
        
        return false;
    }
    
    public static Token<XMLTokenId> getAttributeToken(DocumentContext context) {
        if(context.getCurrentToken() == null ) {
            return null;
        }
        try {
            return context.<Token<XMLTokenId>>runWithSequence((TokenSequence ts) -> {
                if(!isValueToken(context.getCurrentToken())) {
                    return null;
                }
                Token<XMLTokenId> equalsToken = null;
                while (ts.movePrevious()) {
                    Token<XMLTokenId> t = ts.token();
                    if (t.id() == OPERATOR) {
                        equalsToken = t;
                        break;
                    }
                }
                if(equalsToken == null) {
                    return null;
                }
                Token<XMLTokenId> argumentToken = null;
                while (ts.movePrevious()) {
                    Token<XMLTokenId> t = ts.token();
                    if (t.id() == ARGUMENT) {
                        argumentToken = t;
                        break;
                    }
                }
                return argumentToken;
            });
        } catch (BadLocationException ex) {
        }
        return null;
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
        if(tagName == null) {
            return null;
        }
        if(tagName.indexOf(':') == -1) {
            return null;
        } else {
            return tagName.substring(0, tagName.indexOf(':'));
        }
    }
    
    /**
     * Returns the local name from the element's tag.
     */
    public static String getLocalNameFromTag(String tagName) {
        if(tagName == null) {
            return null;
        }
        if(tagName.indexOf(':') == -1) {
            return tagName;
        } else {
            return tagName.substring(tagName.indexOf(':') + 1);
        }
    }
    
    /**
     * Returns any prefix declared with this namespace. For example, if
     * the namespace was declared as xmlns:po, the prefix 'po' will be returned.
     * Returns null for declaration that contains no prefix.
     */
    public static String getPrefixFromNamespaceDeclaration(String namespace) {
        if (!namespace.startsWith(XMLConstants.XMLNS_ATTRIBUTE)) {
            return null;
        }
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

