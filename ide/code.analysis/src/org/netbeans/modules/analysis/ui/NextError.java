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

package org.netbeans.modules.analysis.ui;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import org.openide.nodes.Node;

/**
 *
 * @author lahvac
 */
public class NextError extends AbstractErrorAction {

    public NextError(AnalysisResultTopComponent comp) {
        super(comp);
    }

    @Override
    protected Node findSubsequentNode(Node from) {
        if (!isUseful(from)) {
            Node result = findFirstUsableChild(from);

            if (result != null) return result;
        }

        Node parent = from.getParentNode();

        while (parent != null) {
            List<Node> children = Arrays.asList(parent.getChildren().getNodes(true));
            int index = children.indexOf(from);

            for (int i = index + 1; i < children.size(); i++) {
                Node c = children.get(i);

                if (isUseful(c)) return c;

                Node result = findFirstUsableChild(c);

                if (result != null) return result;
            }

            from = parent;
            parent = parent.getParentNode();
        }

        return null;
    }

    private Node findFirstUsableChild(Node parent) {
        List<Node> deeper = new LinkedList<>();

        deeper.add(parent);

        while (!deeper.isEmpty()) {
            Node top = deeper.remove(0);

            if (isUseful(top)) return top;

            deeper.addAll(0, Arrays.asList(top.getChildren().getNodes(true)));
        }

        return null;
    }
}
