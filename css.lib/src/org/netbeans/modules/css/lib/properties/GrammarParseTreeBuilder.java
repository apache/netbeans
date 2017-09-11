/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
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
        for (GrammarElement key : peek.childrenMap.keySet()) {
            Node.AbstractNode node = peek.childrenMap.get(key);
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
