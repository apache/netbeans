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

package org.netbeans.modules.xml.xdm.visitor;
import java.util.List;
import org.netbeans.modules.xml.xdm.nodes.Attribute;
import org.netbeans.modules.xml.xdm.nodes.Element;
import org.netbeans.modules.xml.xdm.nodes.Node;
import org.netbeans.modules.xml.xdm.nodes.NodeImpl;
import org.netbeans.modules.xml.xdm.nodes.Text;
import org.netbeans.modules.xml.xdm.nodes.Token;
import org.netbeans.modules.xml.xdm.nodes.TokenType;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.NodeList;

/**
 *
 * @author Srividhya Narayanan
 */
public class FlushVisitor extends ChildVisitor {
    
    public String flushModel(org.netbeans.modules.xml.xdm.nodes.Document root) {
        buffer = new StringBuilder();
        root.accept(this);
        String result = buffer.substring(0, buffer.length() - 1);
        return result;
        // return buffer.toString();
    }

    public String flush(NodeList children) {
        buffer = new StringBuilder();
        for (int i=0; i<children.getLength(); i++) {
            Node child = (Node) children.item(i);
            child.accept(this);
        }
        return buffer.toString();
    }
    
    public void visit(Element e) {
        java.util.ListIterator<Token> tokensIter = e.getTokens().listIterator();
        while(tokensIter.hasNext()) {
            Token token = tokensIter.next();
            buffer.append(token.getValue());
            if(token.getType()==TokenType.TOKEN_ELEMENT_START_TAG) break;
        }

        
        if(e.hasAttributes()) {
            NamedNodeMap attributes = e.getAttributes();
            for (int i =0; i<attributes.getLength(); i++) {
                Node l = (Node)attributes.item(i);
                l.accept(this);
            }
        }

        while (tokensIter.hasNext()) {
            Token token = tokensIter.next();
            buffer.append(token.getValue());
            if(token.getType()==TokenType.TOKEN_ELEMENT_END_TAG) break;
        }
        
        if(e.hasChildNodes()) {
            NodeList children = e.getChildNodes();
            for (int i =0; i<children.getLength(); i++) {
                Node l = (Node)children.item(i);
                if (l instanceof Attribute) {
                    //
                } else {
                    l.accept(this);
                }
            }
        }

        while(tokensIter.hasNext()) {
            buffer.append(tokensIter.next().getValue());
        }
    }
    
    protected void visitNode(Node node) {
        List<Token> tokens = ((NodeImpl)node).getTokens();
        for (Token token :tokens)
            buffer.append(token.getValue());
        super.visitNode(node);
    }
    
    private StringBuilder buffer;
}
