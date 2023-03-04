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
package org.netbeans.modules.db.explorer.node;

import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;
import org.netbeans.modules.db.explorer.action.RefreshAction;
import org.openide.nodes.FilterNode;
import org.openide.nodes.Node;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.SystemAction;

/**
 * Extension of FilterNode. It can refresh its parent node (or some other
 * ancestor) after it is destroyed, if the delegate wishes to do it (the
 * delegate sets attribute {@link #REFRESH_ANCESTOR_DISTANCE}). See bug #222113.
 *
 * @author jhavlin
 */
public class BaseFilterNode extends FilterNode {

    /**
     * Distance of ancestor to refresh. If the original node (delegate) needs to
     * refresh its parent (ancestor at distance 1) or some other ancestor after
     * it is destroyed, it has to set attribute with this key to distance of
     * ancestor to refresh. (1 for parent, 2 for grandparent, ...). See
     * {@link Node#setValue(java.lang.String, java.lang.Object)}.
     */
    public static final String REFRESH_ANCESTOR_DISTANCE =
            "BaseFilterNode.refreshAncestorDistance";                   //NOI18N
    /** */
    private static RequestProcessor RP =
            new RequestProcessor(BaseFilterNode.class);

    private static final Map<Node, Object> NODES_TO_REFRESH =
            new WeakHashMap<Node, Object>();

    public BaseFilterNode(Node original) {
        super(original);
    }

    /**
     * Destroy the original node and check if it wishes to refresh some of its
     * ancestors.
     */
    @Override
    public void destroy() throws IOException {
        super.destroy();
        Object ancestorDist = getOriginal().getValue(REFRESH_ANCESTOR_DISTANCE);
        int ancestorDistInt = (ancestorDist instanceof Integer)
                ? (Integer) ancestorDist : 0;
        if (ancestorDistInt > 0) {
            int currentDist = 0;
            Node ancestor = this;
            while (currentDist < ancestorDistInt) {
                if (ancestor.getParentNode() != null) {
                    ancestor = ancestor.getParentNode();
                    currentDist++;
                } else {
                    break;
                }
            }
            if (currentDist > 0) {
                scheduleRefresh(ancestor);
            }
        }
    }

    /**
     * Schedule refreshing of a node. Do not schedule refreshing if it is
     * already scheduled. See bug #216907
     */
    private static void scheduleRefresh(final Node node) {
        synchronized (NODES_TO_REFRESH) {
            if (NODES_TO_REFRESH.containsKey(node)) {
                return;
            } else {
                NODES_TO_REFRESH.put(node, new Object());
            }
        }
        RP.post(new Runnable() {
            @Override
            public void run() {
                SystemAction.get(RefreshAction.class).performAction(
                        new Node[]{node});
                synchronized (NODES_TO_REFRESH) {
                    NODES_TO_REFRESH.remove(node);
                }
            }
        }, 250);
    }
}
