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

package org.netbeans.modules.xml.xdm.visitor;

import java.util.List;
import java.util.Stack;
import org.netbeans.modules.xml.xdm.nodes.Attribute;
import org.netbeans.modules.xml.xdm.nodes.Document;
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
 * @author Ayub Khan
 */
public class NodeByPositionVisitor implements XMLNodeVisitor{
    
    private int currentPos = 0;
    private Node rootNode;
    private boolean found;
    private int position;
    private Node foundNode;
    private Stack<Element> stack = new Stack<Element>();
    
    
    public NodeByPositionVisitor(Node rootNode){
        this.rootNode = rootNode;
    }
    
    public Element getContainingElement(int position) {
        //Based on position, the containing node could be element, attr, text
        Node node=getContainingNode(position);
        if(node instanceof Attribute || node instanceof Text) {
            if(stack.isEmpty()) return null;
            return stack.peek();
        }
        return (Element)node;
    }
    
    public Node getContainingNode(int position) {
        reset();
        this.position=position;
        rootNode.accept(this);
        return this.foundNode;
    }
    
    
    public void reset(){
        currentPos = 0;
        found = false;
        foundNode = null;
    }
    
    public void visit(Document doc) {
        //xml processing instruction
        currentPos += getLengthOfTokens(doc);
        NodeList nodes = doc.getChildNodes();
        for(int i = 0; i < nodes.getLength(); i++){
            Node n = (Node)nodes.item(i);
            n.accept(this);
            if(found) return;
        }
    }
    
    public void visit(Element e) {
        stack.push(e);
        int openStartElemPos=currentPos;
        currentPos += getTokenLength(e, TokenType.TOKEN_WHITESPACE); //all whitespaces
        currentPos += getElementStartTokenLength(e, true); //open start tag
        NamedNodeMap attrs = e.getAttributes();
        for(int i = 0; i < attrs.getLength(); i++){
            Node attr = (Node)attrs.item(i);
            attr.accept(this);
            if(found) return;
        }
        currentPos++; //close of start tag
        int closeStartElemPos=currentPos;
        if((position >= openStartElemPos && position < closeStartElemPos)) {
            this.foundNode=e;
            found = true;
        }
        NodeList children = e.getChildNodes();
        for(int i = 0; i < children.getLength(); i++){
            Node n = (Node)children.item(i);
            n.accept(this);
            if(found) return;
        }
        stack.pop();
        int openEndElemPos=currentPos;
        currentPos += getElementStartTokenLength(e, false); //open end tag
        currentPos++; //close of end tag
        int closeEndElemPos=currentPos;
        if((position >= openEndElemPos && position < closeEndElemPos)) {
            this.foundNode=e;
            found = true;
        }
    }
    
    public void visit(Text txt) {
        int beginTextPos=currentPos;
        // use token length here because of automatic conversion
        // for &lt; as well as handling of comment and cdata which need
        // to include the start and end delimeter
        currentPos += getLengthOfTokens(txt);
        int endTextPos=currentPos;
        if((position >= beginTextPos && position < endTextPos)) {
            this.foundNode=txt;
            found = true;
        }
    }
    
    public void visit(Attribute attr) {
        int beginAttrPos=currentPos;
        currentPos += getLengthOfTokens(attr);
        int endAttrPos=currentPos;
        if((position >= beginAttrPos && position < endAttrPos)) {
            this.foundNode=attr;
            found = true;
        }
    }
    
    
    /**
     * Obtains the length of a start element, e.g., "<", or "<elementname",
     * "</", or "</elementname".
     * @param node The element being queried
     * @param beginTag Is this for the start tag (<) or end tag (</)?
     * @return length of start element
     */
    private int getElementStartTokenLength(Element element, boolean beginTag){
        String value = "";
        List<Token> tokens = element.getTokens();
        for(Token token : tokens){
            if(token.getType() != TokenType.TOKEN_ELEMENT_START_TAG){
                continue;
            }
            String tokenValue = token.getValue();
            if(beginTag){
                if(!tokenValue.startsWith("</")){
                    value = tokenValue;
                }
            } else{ //end tag
                if(tokenValue.startsWith("</")){
                    value = tokenValue;
                }
            }
        }
        return value.length();
    }
    
    private int getTokenLength(NodeImpl node, TokenType type){
        StringBuffer buf = new StringBuffer("");
        List<Token> tokens = node.getTokens();
        for(Token token : tokens){
            if(token.getType() == type){
                buf.append(token.getValue());
            }
        }
        return buf.toString().length();
    }
    
    private int getLeadingWhiteSpaces(Attribute attr){
        Token firstToken = attr.getTokens().get(0); //get the first token
        if(firstToken.getType() == TokenType.TOKEN_WHITESPACE){
            return firstToken.getValue().length();
        }
        return 0;
    }
    
    private int getLengthOfTokens(NodeImpl node){
        StringBuffer buf = new StringBuffer();
        List<Token> tokens = node.getTokens();
        for(Token token : tokens){
            buf.append(token.getValue());
        }
        return buf.length();
    }
}
