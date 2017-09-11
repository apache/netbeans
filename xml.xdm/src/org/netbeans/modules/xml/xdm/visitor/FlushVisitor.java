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
