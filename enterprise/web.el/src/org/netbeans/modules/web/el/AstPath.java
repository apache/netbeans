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

package org.netbeans.modules.web.el;

import com.sun.el.parser.Node;
import com.sun.el.parser.NodeVisitor;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.el.ELException;
import org.openide.util.Parameters;

/**
 * AST path for Expression Language AST nodes.
 *
 * @author Erno Mononen
 */
public final class AstPath {

    private final List<Node> nodes = new ArrayList<>();
    private final Node root;

    public AstPath(Node root) {
        Parameters.notNull("root", root);
        this.root = root;
        init();
    }

    private void init() {
        root.accept(new NodeVisitor() {

            @Override
            public void visit(Node node) throws ELException {
                nodes.add(node);
            }
        });
    }

    public Node getRoot() {
        return root;
    }

    public List<Node> rootToLeaf() {
        return nodes;
    }

    public List<Node> leafToRoot() {
        List<Node> copy = new ArrayList<>(nodes);
        Collections.reverse(copy);
        return copy;
    }

    public List<Node> rootToNode(Node target) {
        return rootToNode(target, false);
    }

    public List<Node> rootToNode(Node target, boolean inclusive) {
        List<Node> result = new ArrayList<>();
        for (Node each : nodes) {
            if (equalsNodes(each, target)) {
                if (inclusive) {
                    result.add(each);
                }
                break;
            }
            result.add(each);
        }
        return result;
    }

    private static boolean equalsNodes(Node src, Node target) {
        if (src.equals(target)) {
            // #228091 SimpleNode equal method doesn't check images in some cases
            return src.getImage() == null || src.getImage().equals(target.getImage());
        }
        return false;
    }

}
