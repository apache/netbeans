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

import java.util.Collection;

/**
 *
 * @author marekfukala
 */
public class GrammarParseTreeConvertor {

    static Node createParseTreeWithOnlyNamedNodes(Node root) {
        return convert(root);
    }

    private static Node convert(Node node) {
        Node converted;
        if(node instanceof Node.GroupNodeImpl) {
            //create a copy of the grammar node
            Node.GroupNodeImpl gen = (Node.GroupNodeImpl)node;
            Node.GroupNodeImpl copy = new Node.GroupNodeImpl(gen.getGrammarElement());
            converted = copy;
            
            //and set a modified list of child nodes
            gatherNonAnonymousNodes(gen, copy.modifiableChildren());
            
        } else if(node instanceof Node.ResolvedTokenNode) {
            //no need to convert
            converted = node;
            
        } else {
            throw new IllegalStateException();
        }
        
        return converted;
    }
    
    private static void gatherNonAnonymousNodes(Node node, Collection<Node> children) {
        for (Node child : node.children()) {
            if (isAnonymousNode(child)) {
                gatherNonAnonymousNodes(child, children);
            } else {
                children.add(convert(child));
            }
        }
    }

    private static boolean isAnonymousNode(Node child) {
        return child.name() == null;
    }
}
