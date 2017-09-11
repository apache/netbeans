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

/*
 * PositionFinderVisitor.java
 *
 * Created on October 26, 2005, 3:02 PM
 *
 * To change this template, choose Tools | Template Manager
 * and open the template in the editor.
 */

package org.netbeans.modules.xml.xdm.visitor;

import java.util.List;
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
 * @author rico
 */
public class PositionFinderVisitor implements XMLNodeVisitor{
    
    int position = 0;
    Node node;
    boolean found;
    
    public int findPosition(Node rootNode, Node node){
        reset();
        this.node = node;
        rootNode.accept(this);
        
        return position;
    }
    
    public void reset(){
        position = 0;
        found = false;
    }
    
    public void visit(Document doc) {
        //xml processing instruction
        position += getLengthOfTokens(doc);
        NodeList nodes = doc.getChildNodes();
        for(int i = 0; i < nodes.getLength(); i++){
            Node n = (Node)nodes.item(i);
            n.accept(this);
            if(found) return;
        }
    }
    
    public void visit(Element e) {
        if(e.getId() == node.getId()){
            found = true;
        } else{
            position += getElementStartTokenLength(e, true); //open start tag
            NamedNodeMap attrs = e.getAttributes();
            for(int i = 0; i < attrs.getLength(); i++){
                Node attr = (Node)attrs.item(i);
                attr.accept(this);
                if(found) return;
            }
            position += getStartTagWhiteSpaceTokensLength(e); //all whitespaces
            position++; //close of start tag
            NodeList children = e.getChildNodes();
            for(int i = 0; i < children.getLength(); i++){
                Node n = (Node)children.item(i);
                n.accept(this);
                if(found) return;
            }
            position += getElementStartTokenLength(e, false); //open end tag
            position += getEndTagWhiteSpaceTokensLength(e); //all whitespaces
            position++; //close of end tag
        }
    }
    
    public void visit(Text txt) {
        if(txt.getId() == node.getId()){
            found = true;
        } else{
            int txtLen = getLength(txt);
            if(txtLen > 0)
                position += txtLen;
        }
    }
    
    public void visit(Attribute attr) {
        if(attr.getId() == node.getId()){
            //add preceding white spaces
            position += getLeadingWhiteSpaces(attr);
            found = true;
        } else{
            position += getLengthOfTokens(attr);
        }
    }
    
    private int getLength(Text n) {
        int len = 0;
        for(Token token:((NodeImpl)n).getTokens())
            len += token.getValue().length();
        return len;
    }
    
    /**
     * Obtains the length of a start token of elements, e.g., "<", or "<elementname",
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
    
    private int getStartTagWhiteSpaceTokensLength(NodeImpl node){
        StringBuilder buf = new StringBuilder();
        List<Token> tokens = node.getTokens();
        for(Token token : tokens){
            if (token.getType() == TokenType.TOKEN_ELEMENT_END_TAG){
                break;  // only count whitspace before first tag end
            }
            if (token.getType() == TokenType.TOKEN_WHITESPACE) {
                buf.append(token.getValue());
            }
        }
        return buf.toString().length();
    }
    
    private int getEndTagWhiteSpaceTokensLength(NodeImpl node){
        StringBuilder buf = new StringBuilder();
        List<Token> tokens = node.getTokens();
        boolean counting = false;
        for(Token token : tokens){
            if (token.getType() == TokenType.TOKEN_ELEMENT_START_TAG){
                if (token.getValue().startsWith("</")) { 
                    counting = true;
                }
            }
            if (! counting) continue;
            if (token.getType() == TokenType.TOKEN_WHITESPACE) {
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
        StringBuilder buf = new StringBuilder();
        List<Token> tokens = node.getTokens();
        for(Token token : tokens){
            buf.append(token.getValue());
        }
        return buf.length();
    }
    
}
