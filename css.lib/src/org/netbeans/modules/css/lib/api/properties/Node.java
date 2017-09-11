/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2011 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2011 Sun Microsystems, Inc.
 */
package org.netbeans.modules.css.lib.api.properties;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

/**
 * Node of the css property value parse tree.
 *
 * @author mfukala@netbeans.org
 */
public interface Node {

    public String name();

    public Collection<Node> children();

    public void accept(NodeVisitor visitor);
    
    public void accept(NodeVisitor2 visitor);

    public CharSequence image();

    static abstract class AbstractNode implements Node {

        @Override
        public void accept(NodeVisitor visitor) {
            if (visitor.visit(this)) {
                for (Node child : children()) {
                    child.accept(visitor);
                }
                visitor.unvisit(this);
            }
        }
    }

    public static class ResolvedTokenNode extends AbstractNode implements TokenNode {

        private ResolvedToken resolvedToken = null;

        public ResolvedTokenNode() {
        }

        @Override
        public void accept(NodeVisitor2 visitor) {
            visitor.visitTokenNode(this);
        }

        public void setResolvedToken(ResolvedToken resolvedToken) {
            this.resolvedToken = resolvedToken;
        }
        
        @Override
        public ResolvedToken getResolvedToken() {
            return resolvedToken;
        }

        @Override
        public Collection<Node> children() {
            return Collections.emptyList();
        }

        public Token getToken() {
            return resolvedToken.token();
        }

        @Override
        public CharSequence image() {
            return resolvedToken.token().image();
        }

        @Override
        public String toString() {
            return resolvedToken.token().toString();
        }

        @Override
        public String name() {
            //XXX should be ... .getName()
            return resolvedToken.getGrammarElement().getValue();
        }
        }

        public static class GroupNodeImpl extends AbstractNode implements GroupNode  {

        private GrammarElement element;
        private Collection<Node> children = new ArrayList<>();

        public GroupNodeImpl(GrammarElement group) {
            this.element = group;
        }

        @Override
        public void accept(NodeVisitor2 visitor) {
            if(visitor.visitGroupNode(this)) {
                for (Node child : children()) {
                    child.accept(visitor);
                }
            }
        }
        
        @Override
        public GrammarElement getGrammarElement() {
            return element;
        }
        
        public <T extends AbstractNode> T addChild(T node) {
            children.add(node);
            return node;
        }

        public <T extends AbstractNode> boolean removeChild(T node) {
            return children.remove(node);
        }

        @Override
        public String name() {
            return element.getName();
        }

        @Override
        public Collection<Node> children() {
            return children;
        }

        public Collection<Node> modifiableChildren() {
            return children;
        }
        
        @Override
        public String toString() {
            return element.toString();
        }

        @Override
        public CharSequence image() {
            StringBuilder sb = new StringBuilder();
            for (Node child : children()) {
                sb.append(child.image());
            }
            return sb.toString();
        }
    }


}