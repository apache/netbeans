/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.netbeans.modules.cnd.remote.ui.networkneighbour;

import java.awt.Image;
import java.awt.image.BufferedImage;
import javax.swing.SwingUtilities;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.NbBundle;

/**
 *
 */
final class NeighbourhoodRootNode extends AbstractNode {

    private static final NeighbourHost refreshing = new NeighbourHost("*refreshing*", false); // NOI18N
    private final NeighbourhoodChildren children = new NeighbourhoodChildren();

    public NeighbourhoodRootNode() {
        super(Children.LEAF);
        setChildren(children);
    }

    void refresh(NeighbourHost[] hosts) {
        if (hosts.length == 0) {
            children.refreshChildren(new NeighbourHost[]{refreshing});
        } else {
            children.refreshChildren(hosts);
        }
    }

    private static class NeighbourhoodChildren extends Children.Keys<NeighbourHost> {

        private final BufferedImage emptyIcon = new BufferedImage(1, 1, BufferedImage.TYPE_INT_ARGB);

        @Override
        protected Node[] createNodes(NeighbourHost host) {
            if (host == refreshing) {
                return new Node[]{new AbstractNode(LEAF) {

                        @Override
                        public String getHtmlDisplayName() {
                            return "<html><font color=\"#808080\">" + // NOI18N
                                    NbBundle.getMessage(NetworkNeighbourhoodList.class,
                                    "NetworkNeighbourhoodList.Refreshing"); // NOI18N
                        }

                        @Override
                        public Image getIcon(int type) {
                            return emptyIcon;
                        }
                    }};

            }
            NeighbourHostNode node = new NeighbourHostNode(host);

            return new Node[]{node};
        }

        private void refreshChildren(final NeighbourHost[] hosts) {
            if (SwingUtilities.isEventDispatchThread()) {
                setKeys(hosts);
            } else {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        setKeys(hosts);
                    }
                });
            }
        }
    }
}
