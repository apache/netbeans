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
package org.netbeans.modules.css.lib;

import java.util.Collections;
import java.util.List;
import org.antlr.runtime.tree.ParseTree;
import org.antlr.runtime.tree.Tree;
import org.netbeans.modules.css.lib.api.Node;

/**
 *
 * @author marekfukala
 */
public abstract class AbstractParseTreeNode extends ParseTree implements Node {

    private Tree parent;
    
    private final CharSequence source;
    
    public AbstractParseTreeNode(CharSequence source) {
        super(null);
        this.source = source;
    }

    protected CharSequence getSource() {
        return source;
    }

    @Override
    public CharSequence image() {
        return getSource().subSequence(from(), to());
    }
    
    public boolean deleteChild(AbstractParseTreeNode node) {
        if (children == null) {
            return false;
        }
        int childIndex = children.indexOf(node);
        if (childIndex == -1) {
            return false; //no such node
        }

        return super.deleteChild(childIndex) != null;
    }

    /** BaseTree doesn't track parent pointers. */
    @Override
    public Tree getParent() {
        return parent;
    }

    @Override
    public void setParent(Tree t) {
        this.parent = t;
    }

    @Override
    public List<Node> children() {
        @SuppressWarnings("unchecked")
        List<Node> ch = (List<Node>) (List<?>) getChildren();
        return ch == null ? Collections.<Node>emptyList() : ch;
    }

    @Override
    public Node parent() {
        return (Node)getParent();
    }
    
    @Override
    public String toString() {
        return new StringBuilder()
                .append(type())
                .append('(')
                .append(from())
                .append('-')
                .append(to())
                .append(')')
                .toString();
    }

}
