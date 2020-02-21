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
