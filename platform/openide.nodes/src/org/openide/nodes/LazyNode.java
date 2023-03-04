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

package org.openide.nodes;

import java.util.Map;
import javax.swing.Action;

/** Lazy delegating node.
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class LazyNode extends FilterNode {
    private Map<String,?> map;

    LazyNode(Map<String,?> map) {
        this(new AbstractNode(new Children.Array()), map);
    }
    private LazyNode(AbstractNode an, Map<String,?> map) {
        super(an, new SwitchChildren(an));
        ((SwitchChildren)getChildren()).node = this;
        this.map = map;
        
        an.setName((String) map.get("name")); // NOI18N
        an.setDisplayName((String) map.get("displayName")); // NOI18N
        an.setShortDescription((String) map.get("shortDescription")); // NOI18N
        String iconBase = (String) map.get("iconResource"); // NOI18N
        if (iconBase != null) {
            an.setIconBaseWithExtension(iconBase);
        }
    }

    @Override
    public Action[] getActions(boolean context) {
        return switchToOriginal().getActions(context);
    }

    final Node switchToOriginal() {
        final Node[] n = new Node[]{null};
        synchronized (this) {
            if (map == null) {
                return getOriginal();
            }
            n[0] = (Node)map.get("original"); // NOI18N
            if (n[0] == null) {
                throw new IllegalArgumentException("Original Node from map " + map + " is null");
            }
            map = null;
        }
        Children.MUTEX.postWriteRequest(new Runnable() {

            public void run() {
                changeOriginal(n[0], true);
            }
        });
        return n[0];
    }
    
    private static final class SwitchChildren extends FilterNode.Children {
        LazyNode node;

        public SwitchChildren(Node or) {
            super(or);
        }

        @Override
        protected void addNotify() {
            node.switchToOriginal();
            super.addNotify();
        }

        @Override
        public Node[] getNodes(boolean optimalResult) {
            node.switchToOriginal();
            return super.getNodes(optimalResult);
        }

        @Override
        public int getNodesCount(boolean optimalResult) {
            node.switchToOriginal();
            return super.getNodesCount(optimalResult);
        }


        @Override
        public Node findChild(String name) {
            node.switchToOriginal();
            return super.findChild(name);
        }


    }
}
