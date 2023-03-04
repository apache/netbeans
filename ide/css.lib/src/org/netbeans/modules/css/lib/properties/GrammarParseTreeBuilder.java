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
package org.netbeans.modules.css.lib.properties;

import java.util.*;
import org.netbeans.modules.css.lib.api.properties.*;
import org.netbeans.modules.css.lib.api.properties.Node.AbstractNode;

/**
 * Css property value parse tree builder based on {@link GrammarResolverListener}.
 *
 * Creates a parse tree for css property values.
 * 
 * @author marekfukala
 */
public class GrammarParseTreeBuilder implements GrammarResolverListener {

    private Node root;
    private Stack<Entry> stack = new Stack<Entry>();
    private boolean parsingFinished;
    
    public static boolean DEBUG = false;
    private int indent = 0;

    public Node getParseTree() {
        if (!parsingFinished) {
            throw new IllegalStateException("Parsing not finished!");
        }
        return root;
    }

    private void out(GrammarElement element, boolean accepted) {
        if (DEBUG) {
            indent--;
            System.out.println(String.format("%s%s %s", getIndent(), (accepted ? "*" : "-"), element));
        }

        Entry pop = stack.pop();

        Node.AbstractNode node = pop.node;
        if (stack.isEmpty()) {
            return; //root
        }
        if (accepted) {
            Entry peek = stack.peek();
            Node.GroupNodeImpl gnode = (Node.GroupNodeImpl) peek.node;

            peek.childrenMap.put(pop.grammarElement, node);
            gnode.addChild(node);
        }

    }

    @Override
    public void entering(GroupGrammarElement group) {
        if (DEBUG) {
            System.out.println(String.format("%s%s", getIndent(), group));
            indent++;
        }

        Node.GroupNodeImpl node = new Node.GroupNodeImpl(group);
        if (root == null) {
            if(DEBUG) {
                System.out.println(String.format("Root node set to element %s", group));
            }
            root = node;
        }

        Entry e = new Entry(group, node);
        stack.push(e);
    }

    @Override
    public void accepted(GroupGrammarElement group) {
        out(group, true);
    }

    @Override
    public void rejected(GroupGrammarElement group) {
        out(group, false);
    }

    @Override
    public  void entering(ValueGrammarElement element) {
        Node.AbstractNode node = new Node.ResolvedTokenNode();
        if (root == null) {
            root = node;
        }
        Entry e = new Entry(element, node);
        stack.push(e);
    }
    
    @Override
    public void accepted(ValueGrammarElement value, ResolvedToken resolvedToken) {
        if (DEBUG) {
            System.out.println(String.format("%s * '%s' value token accepted", getIndent(), value));
        }
        Entry pop = stack.pop();

        Node.ResolvedTokenNode node = (Node.ResolvedTokenNode) pop.node;
        node.setResolvedToken(resolvedToken);

        Entry peek = stack.peek();
        Node.GroupNodeImpl gnode = (Node.GroupNodeImpl) peek.node;
        peek.childrenMap.put(pop.grammarElement, node);
        gnode.addChild(node);
        
    }
    
    @Override
    public void rejected(ValueGrammarElement value) {
        stack.pop();
    }
    
    @Override
    public void ruleChoosen(GroupGrammarElement base, GrammarElement element) {
        Entry peek = stack.peek();
        
        if (DEBUG) {
            System.out.println(String.format("%s group %s: choosen branch %s", getIndent(), base, element));
            System.out.println(String.format("(in group %s)", peek.grammarElement));
        }

        Node.GroupNodeImpl group = (Node.GroupNodeImpl) peek.node;

        peek.choosenBranches.add(element);
        
        //remove all children except the choosen one
        for (Map.Entry<GrammarElement, Node.AbstractNode> entry : peek.childrenMap.entrySet()) {
            GrammarElement key = entry.getKey();
            Node.AbstractNode node = entry.getValue();
            if(!peek.choosenBranches.contains(key)) {
                group.removeChild(node);
            }
        }
        
        peek.childrenMap.clear();
    }

    @Override
    public void starting() {
        if (DEBUG) {
            System.out.println("Parsing is about to start");
        }
    }

    @Override
    public void finished() {
        parsingFinished = true;

        if (DEBUG) {
            System.out.println("Parsing finished");
        }
    }

    private CharSequence getIndent() {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < indent * 4; i++) {
            b.append(' ');
        }
        return b;
    }

    private static class Entry {

        public final GrammarElement grammarElement;
        public final Node.AbstractNode node;
        public final Map<GrammarElement, Node.AbstractNode> childrenMap;
        public final Set<GrammarElement> choosenBranches;

        public Entry(GrammarElement grammarElement, 
                AbstractNode node) {
            this.grammarElement = grammarElement;
            this.node = node;
            this.childrenMap = new HashMap<GrammarElement, AbstractNode>();
            this.choosenBranches = new HashSet<GrammarElement>();
        }
        
    }
}
