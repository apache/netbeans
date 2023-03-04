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
package org.netbeans.modules.apisupport.project.layers;

import javax.swing.Action;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.actions.SystemAction;

/**
 *
 * @author mkozeny
 */
public class LayerFilterNode extends FilterNode {

    LayerFilterNode(Node original) {
        super(original, org.openide.nodes.Children.LEAF == original.getChildren()
                ? org.openide.nodes.Children.LEAF : new LayerFilterNodeChildren(original));
    }

    @Override
    public Action getPreferredAction() {
        return SystemAction.get(OpenLayerFilesAction.class);
    }

    private static class LayerFilterNodeChildren extends FilterNode.Children {

        LayerFilterNodeChildren(Node orig) {
            super(orig);
        }

        @Override
        protected Node copyNode(Node node) {
            return new LayerFilterNode(node);
        }

    }

}
