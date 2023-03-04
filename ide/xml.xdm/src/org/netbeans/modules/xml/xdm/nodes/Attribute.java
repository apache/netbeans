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

import java.util.List;
import java.util.Map;
import org.netbeans.modules.xml.xdm.visitor.XMLNodeVisitor;

/**
 * This class represents the XML Attributes.
 * An attribute is of the form attrName="attrValue"
 * In terms of tokens attibute can have upto 6 tokens.
 * Preceding whitespace token.
 * attribute name token (wchich comprises of namespace and seperator if any and localname.
 * whitespace token, optional.
 * assignment operator, =.
 * whitespace token, optional.
 * value token which is start quote, value and end quote.
 * @author Ajit
 */
public class Attribute extends NodeImpl implements Node, org.w3c.dom.Attr {
    
    Attribute() {
        super();
    }
    
    Attribute(String name) {
        super();
        List<Token> tokens = getTokensForWrite();
        tokens.add(Token.create(" ", TokenType.TOKEN_WHITESPACE));
        tokens.add(Token.create(name, TokenType.TOKEN_ATTR_NAME));
        tokens.add(Token.create("=", TokenType.TOKEN_ATTR_EQUAL));
        tokens.add(Token.create("\"\"", TokenType.TOKEN_ATTR_VAL));
    }
    
    Attribute(String name, String value) {
        super();
        List<Token> tokens = getTokensForWrite();
        tokens.add(Token.create(" ", TokenType.TOKEN_WHITESPACE));
        tokens.add(Token.create(name, TokenType.TOKEN_ATTR_NAME));
        tokens.add(Token.create("=", TokenType.TOKEN_ATTR_EQUAL));
        tokens.add(Token.create("\"".concat(insertEntityReference(value)).concat("\""), TokenType.TOKEN_ATTR_VAL));
    }
    
    public short getNodeType() {
        return Node.ATTRIBUTE_NODE;
    }
    
    public String getNodeName() {
        return getName();
    }
    
    public String getNodeValue() {
        return getValue();
    }
    
    public boolean getSpecified() {
        return false;
    }
    
    public boolean isId() {
        return false;
    }
    
    public org.w3c.dom.Element getOwnerElement() {
        return (org.w3c.dom.Element) super.getParentNode();
    }
    
    public org.w3c.dom.TypeInfo getSchemaTypeInfo() {
        return null;
    }
    
    private void validateTokens(List<Token> newTokens) {
        assert newTokens != null;
        assert newTokens.size() >=3 && newTokens.size() <=6;
        int currentIdx =0;
        int nameIdx = -1;
        int equalIdx = -1;
        int valIdx = -1;
        for (Token token :newTokens) {
            if(token.getType() == TokenType.TOKEN_ATTR_NAME) {
                if(nameIdx !=-1)
                    throw new IllegalArgumentException();
                nameIdx = currentIdx;
            } else if (token.getType() == TokenType.TOKEN_ATTR_EQUAL) {
                if(equalIdx !=-1 || nameIdx ==-1)
                    throw new IllegalArgumentException();
                equalIdx = currentIdx;
            } else if (token.getType() == TokenType.TOKEN_ATTR_VAL) {
                if(valIdx != -1 || equalIdx ==-1)
                    throw new IllegalArgumentException();
                valIdx = currentIdx;
            } else if (token.getType() == TokenType.TOKEN_WHITESPACE) {
            } else
                throw new IllegalArgumentException();
            currentIdx++;
        }
        if(nameIdx == -1 || equalIdx == -1 || valIdx == -1)
            throw new IllegalArgumentException();
    }
    
    void setTokens(List<Token> newTokens) {
//        validateTokens(newTokens);
        name = null;
        value = null;
        super.setTokens(newTokens);
    }
    
    public void accept(XMLNodeVisitor visitor) {
        visitor.visit(this);
    }
    
    public String getLocalName() {
        String qName = getName();
        if(qName != null){
            int idx = qName.indexOf(':')+1;
            if(idx >0) return qName.substring(idx);
        }
        return qName;
    }
    
    public void setLocalName(String localName) {
        String prefix = getPrefix();
        if(prefix == null) {
            setName(localName);
        } else if(localName == null || localName.equals("")) {
            setName(prefix);
        } else {
            setName(prefix.concat(":").concat(localName));
        }
    }
    
    public String getPrefix() {
        String qName = getName();
        if(qName != null){
            int idx = qName.indexOf(':');
            if(idx >0) return qName.substring(0,idx);
        }
        return null;
    }
    
    public void setPrefix(String prefix) {
        String localName = getLocalName();
        if(prefix == null || prefix.equals("")) {
            setName(localName);
        } else {
            setName(prefix.concat(":").concat(localName));
        }
    }
    
    public String getName() {
        if(name == null) {
            for(Token token : getTokens()) {
                if(token.getType() == TokenType.TOKEN_ATTR_NAME) {
                    name = token.getValue();
                    break;
                }
            }
        }
        return name;
    }
    
    public void setName(String name) {
        assert name!= null && !"".equals(name);
        checkNotInTree();
        this.name = name;
        int tokenIndex = -1;
        for(Token token : getTokens()) {
            tokenIndex++;
            if(token.getType() == TokenType.TOKEN_ATTR_NAME) {
                Token newToken = Token.create(name,TokenType.TOKEN_ATTR_NAME);
                getTokensForWrite().set(tokenIndex,newToken);
                return;
            }
        }
    }
    
    public String getValue() {
        if(value==null) {
            for(Token token : getTokens()) {
                if(token.getType() == TokenType.TOKEN_ATTR_VAL) {
                    String tokenValue = token.getValue();
                    int len = tokenValue.length();
                    if (len<=2) {
                        value = "";
                    } else {
                        value = removeEntityReference(tokenValue.substring(1, len-1));
                    }
                }
            }
        }
        return value;
    }
    
    public void setValue(String value) {
        checkNotInTree();
        this.value = value;
        int tokenIndex = -1;
        for(Token token : getTokens()) {
            tokenIndex++;
            if(token.getType() == TokenType.TOKEN_ATTR_VAL) {
                String oldVal = token.getValue();
                String newVal = oldVal.charAt(0)+insertEntityReference(value)+oldVal.charAt(oldVal.length()-1);
                Token newToken = Token.create(newVal,TokenType.TOKEN_ATTR_VAL);
                getTokensForWrite().set(tokenIndex,newToken);
                return;
            }
        }
    }
    
    public boolean isXmlnsAttribute() {
        return XMLNS.equals(getPrefix()) || XMLNS.equals(getName());
    }
    
    protected void cloneNamespacePrefixes(Map<Integer,String> allNS, Map<String,String> prefixes) {
        if (allNS == null) return;
        
        String[] parts = this.getValue().split(":");
        String prefix = parts.length > 1 ? parts[0] : null;
        if (prefix != null) {
            String namespace = lookupNamespaceURI(prefix);
            if (namespace != null) {
                prefixes.put(prefix, namespace);
            }
        }
        super.cloneNamespacePrefixes(allNS, prefixes);
    }
    
    private String insertEntityReference(String text) {
        // just make sure we replace & with &amp; and not &amp; with &&amp;amp; and so on
        return removeEntityReference(text)
                    .replace("&", "&amp;")   //replace &
                    .replace("<", "&lt;")    //replace <
//                    .replace(">", "&gt;")    //replace >
                    .replace("'", "&apos;")  //replace '
                    .replace("\"", "&quot;"); //replace "
    }

    private String removeEntityReference(String text) {
        return text.replace("&amp;", "&")   //replace with &
                   .replace("&lt;", "<")    //replace with <
//                   .replace("&gt;", ">")    //replace with >
                   .replace("&apos;", "'")  //replace with '
                   .replace("&quot;", "\""); //replace with "
    }
    
    private String name = null;
    private String value = null;
}



