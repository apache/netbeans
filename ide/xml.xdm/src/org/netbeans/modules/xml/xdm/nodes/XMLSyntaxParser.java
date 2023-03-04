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

package org.netbeans.modules.xml.xdm.nodes;

import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import java.util.Stack;
import javax.swing.text.AbstractDocument;
import javax.swing.text.BadLocationException;
import org.netbeans.editor.BaseDocument;
import org.netbeans.api.lexer.TokenSequence;
import org.netbeans.api.lexer.TokenHierarchy;
import org.netbeans.api.xml.lexer.XMLTokenId;

public class XMLSyntaxParser {
            
    public Document parse(BaseDocument basedoc)
    throws IOException, BadLocationException {
        try {
            ((AbstractDocument)basedoc).readLock();
            // create the core model
            Stack<NodeImpl> stack = new Stack<NodeImpl>();
            Document doc = new Document();
            stack.push(doc);
            NodeImpl currentNode = doc;
            List<Token> currentTokens = new ArrayList<Token>();
            TokenHierarchy th = TokenHierarchy.get(basedoc);
            TokenSequence<XMLTokenId> tokenSequence = th.tokenSequence();
            org.netbeans.api.lexer.Token<XMLTokenId> token = tokenSequence.token();
            // Add the text token, if any, before xml decalration to document node
            if(token != null && token.id() == XMLTokenId.TEXT) {
                currentTokens.add(Token.create(token.text().toString(),TokenType.TOKEN_CHARACTER_DATA));
                if(tokenSequence.moveNext()) {
                    token = tokenSequence.token();
                }
                // if the xml decalration is not there assign this token to document
                if(token.id() != XMLTokenId.PI_START) {
                    currentNode.setTokens(new ArrayList<Token>(currentTokens));
                    currentTokens.clear();
                }
            }

            while (tokenSequence.moveNext()) {
                token = tokenSequence.token();
                XMLTokenId tokenId = token.id();
                String image = token.text().toString();
                TokenType tokenType = TokenType.TOKEN_WHITESPACE;
                switch(tokenId) {
                    case PI_START:
                    {
                        tokenType = TokenType.TOKEN_PI_START_TAG;
                        currentTokens.add(Token.create(image,tokenType));
                        break;
                    }
                    case PI_TARGET:
                    {
                        tokenType = TokenType.TOKEN_PI_NAME;
                        currentTokens.add(Token.create(image, tokenType));
                        break;
                    }
                    case PI_CONTENT:
                    {
                        tokenType = TokenType.TOKEN_PI_VAL;
                        currentTokens.add(Token.create(image, tokenType));
                        break;
                    }
                    case PI_END:
                    {
                        tokenType = TokenType.TOKEN_PI_END_TAG;
                        currentTokens.add(Token.create(image,tokenType));
                        if(currentNode instanceof Document) {
                            if(tokenSequence.moveNext()) {
                                org.netbeans.api.lexer.Token t = tokenSequence.token();
                                if(t.id() == XMLTokenId.TEXT) {
                                    currentTokens.add(Token.create(t.text().toString(),TokenType.TOKEN_CHARACTER_DATA));                                
                                } else {
                                    tokenSequence.movePrevious();
                                }
                            }
                            if(stack.peek() != currentNode)
                                stack.push(currentNode);
                        }
                        List<Token> list = new ArrayList<Token>(currentNode.getTokens());
                        list.addAll(currentTokens);
                        currentNode.setTokens(list);
                        currentTokens.clear();
                        break;
                    }
                    case TAG:
                    {
                        int len = image.length();
                        if (image.charAt(len-1) == '>') {
                            Token endToken =
                                    Token.create(image,TokenType.TOKEN_ELEMENT_END_TAG);
                            if(len == 2) {
                                currentNode = stack.pop();
                                endToken =
                                        Token.create(image,TokenType.TOKEN_ELEMENT_END_TAG);
                            } else if(!(currentNode instanceof Element)) {
                                currentNode = stack.peek();
                            }
                            currentTokens.add(endToken);
                            currentNode.getTokensForWrite().addAll(currentTokens);
                            currentTokens.clear();
                        } else {
                            tokenType = TokenType.TOKEN_ELEMENT_START_TAG;
                            if(image.startsWith("</")) {
                                currentNode = stack.pop();
                                if(currentNode.getTokens().isEmpty() ||
                                    !currentNode.getTokens().get(0).getValue().substring(1).
                                        equals(image.substring(2))) {
                                    throw new IOException("Invalid token '" + image +
                                            "' found in document: " +
                                            "Please use the text editor to resolve the issues...");
                                } else {//check for invalid endtag: <a></a
                                    String saveTokenImage = image;
                                    currentTokens.add(Token.create(image,tokenType));
                                    tokenSequence.moveNext();
                                    org.netbeans.api.lexer.Token<XMLTokenId> t = tokenSequence.token();
                                    while(t != null) {
                                        if(t.id() != XMLTokenId.WS) {
                                            tokenSequence.movePrevious();
                                            break;
                                        }
                                        tokenType = TokenType.TOKEN_WHITESPACE;
                                        currentTokens.add(Token.create(t.text().toString(), tokenType));
                                        if(!tokenSequence.moveNext())
                                            break;
                                        t = tokenSequence.token();
                                    }
                                    if(t == null || !t.text().toString().equals(">"))
                                        throw new IOException("Invalid token '" + saveTokenImage +
                                                "' does not end with '>': Please use the " +
                                                "text editor to resolve the issues...");
                                    continue;
                                }
                            } else {
                                currentNode = new Element();
                                Node parent = stack.peek();
                                if(parent instanceof Document &&
                                   ((Document)parent).getDocumentElement() != null)
                                    throw new IOException("Invalid XML document: " +
                                            "Cannot have multiple root elements.");
                                parent.appendChild(currentNode);
                                stack.push(currentNode);
                                currentTokens.add(Token.create(image,tokenType));
                                currentNode.setTokens(new ArrayList<Token>(currentTokens));
                                currentTokens.clear();
                            }
                        }
                        break;
                    }
                    case ARGUMENT: //attribute of an element
                    {
                        tokenType = TokenType.TOKEN_ATTR_NAME;
                        currentNode = new Attribute();
                        Element parent = (Element)stack.peek();
                        parent.appendAttribute((Attribute)currentNode);
                        currentTokens.add(Token.create(image,tokenType));
                        break;
                    }
                    case VALUE:
                    {                    
    //                    tokenSequence.moveNext();
    //                    org.netbeans.api.lexer.Token t = tokenSequence.token();
    //                    while(t.id() == XMLTokenId.VALUE || t.id() == XMLTokenId.CHARACTER) {
    //                        image = image.concat(t.text().toString());
    //                        tokenSequence.moveNext();
    //                        t = tokenSequence.token();
    //                    }
                        tokenType = TokenType.TOKEN_ATTR_VAL;
                        while(tokenSequence.moveNext()) {
                            org.netbeans.api.lexer.Token t = tokenSequence.token();
                            if(t.id() != XMLTokenId.VALUE && t.id() != XMLTokenId.CHARACTER) {
                                tokenSequence.movePrevious();
                                break;
                            }
                            image = image.concat(t.text().toString());
                        }
                        currentTokens.add(Token.create(image,tokenType));
                        currentNode.setTokens(new ArrayList<Token>(currentTokens));
                        currentTokens.clear();
                        break;
                    }
                    case BLOCK_COMMENT:
                    {
                        tokenType = TokenType.TOKEN_COMMENT;
                        Node parent = stack.peek();
                        currentTokens.add(Token.create(image, tokenType));
                        if (image.endsWith(Token.COMMENT_END.getValue())) {
                            String combinedString = combineString(currentTokens);
                            Comment comment = new Comment(combinedString);
                            if (parent instanceof Element) {
                                ((Element)parent).appendChild(comment, false);
                            } else {//parent is Document
                                if(token.id() != XMLTokenId.BLOCK_COMMENT &&
                                        token.text().toString().trim().length() > 0) {
                                    throw new IOException("Invalid token '" + token.text() +
                                            "' found in document: " +
                                            "Please use the text editor to resolve the issues...");
                                }
                                parent.appendChild(comment);
                            }
                            currentTokens.clear();
                        }
                        break;
                    }
                    case TEXT:
                    case CHARACTER:
                    {
                        tokenType = TokenType.TOKEN_CHARACTER_DATA;
                        currentNode = new Text();
                        currentTokens.add(Token.create(image,tokenType));
                        if(token.id() == XMLTokenId.TEXT) {
                            while(tokenSequence.moveNext()) {
                                org.netbeans.api.lexer.Token t = tokenSequence.token();
                                if(t.id() != XMLTokenId.TEXT && t.id() != XMLTokenId.CHARACTER) {
                                    tokenSequence.movePrevious();
                                    break;
                                }
                                currentTokens.add(Token.create(t.text().toString(), tokenType));
                            }
                        }
                        currentNode.setTokens(new ArrayList<Token>(currentTokens));
                        Node parent = stack.peek();
                        if (parent instanceof Element) {
                            ((Element)parent).appendChild(currentNode, false);
                        } else {//parent is Document
                            if(token.id() != XMLTokenId.BLOCK_COMMENT &&
                                    token.text().toString().trim().length() > 0) {
                                throw new IOException("Invalid token '" + token.text() +
                                        "' found in document: " +
                                        "Please use the text editor to resolve the issues...");
                            }
                            parent.appendChild(currentNode);
                        }
                        currentTokens.clear();
                        break;
                    }
                    case WS:
                    {
                        tokenType = TokenType.TOKEN_WHITESPACE;
                        currentTokens.add(Token.create(image, tokenType));
                        break;
                    }
                    case OPERATOR:
                    {
                        tokenType = TokenType.TOKEN_ATTR_EQUAL;
                        currentTokens.add(Token.create(image,tokenType));
                        break;
                    }
                    case DECLARATION:
                    {
                        tokenType = TokenType.TOKEN_DTD_VAL;
                        currentTokens.add(Token.create(image, tokenType));
                        while(tokenSequence.moveNext()) {
                            org.netbeans.api.lexer.Token<XMLTokenId> t = tokenSequence.token();
                            XMLTokenId id = t.id();                        
                            if(id != XMLTokenId.DECLARATION && id != XMLTokenId.VALUE) {
                                tokenSequence.movePrevious();
                                break;
                            }
                            currentTokens.add(Token.create(t.text().toString(), tokenType));
                        }
                        break;
                    }
                    case CDATA_SECTION:
                    {
                        Node parent = stack.peek();
                        CData cdata = new CData(image);
                        if (parent instanceof Element) {
                            ((Element)parent).appendChild(cdata, false);
                        } else {//parent is Document
                            throw new IOException("CDATA is not valid as direct child of document" +
                                    "Please use the text editor to resolve the issues...");
                        }
                        tokenType = TokenType.TOKEN_CDATA_VAL;
                        break;
                    }
                    case ERROR:
                    case EOL:
                    default:
                        throw new IOException("Invalid token found in document: " +
                                "Please use the text editor to resolve the issues...");
                }
            }
            Node result = stack.pop();
            if(result instanceof Document) {
                return (Document)result;
            } else {
                throw new IOException("Document not well formed/Invalid: " +
                        "Please use the text editor to resolve the issues...");
            }
        } finally {
            ((AbstractDocument)basedoc).readUnlock();
        }        
    }
    
    private String combineString(List<Token> tokens) {
        StringBuilder sb = new StringBuilder();
        for (Token t: tokens) {
            sb.append(t.getValue());
        }
        return sb.toString();
    }
}
