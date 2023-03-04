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

    abstract static class AbstractNode implements Node {

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