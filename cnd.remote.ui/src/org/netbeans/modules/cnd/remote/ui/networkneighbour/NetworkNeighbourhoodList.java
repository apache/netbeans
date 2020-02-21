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

import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.dnd.DnDConstants;
import java.lang.reflect.InvocationTargetException;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.swing.outline.Outline;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.OutlineView;
import org.openide.nodes.Node.Property;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;

/**
 *
 */
public final class NetworkNeighbourhoodList extends JPanel implements ExplorerManager.Provider {

    private static final NetworkRegistry registry = NetworkRegistry.getInstance();
    private final ExplorerManager mgr;
    private final NeighbourhoodRootNode rootNode;
    private final ChangeListener changeListener;

    @SuppressWarnings("deprecation")
    public NetworkNeighbourhoodList() {
        setLayout(new BorderLayout());
        mgr = new ExplorerManager();
        rootNode = new NeighbourhoodRootNode();
        mgr.setRootContext(rootNode);
        changeListener = new ChangeListener() {

            @Override
            public void stateChanged(ChangeEvent e) {
                rootNode.refresh(registry.getHosts());
            }
        };

        OutlineView view = new OutlineView(
                NbBundle.getMessage(NetworkNeighbourhoodList.class,
                "NetworkNeighbourhoodList.ColumnHost.Title")); // NOI18N

        view.setAllowedDragActions(DnDConstants.ACTION_NONE);
        view.setAllowedDropActions(DnDConstants.ACTION_NONE);
        Outline outline = view.getOutline();
        outline.setRootVisible(false);
        outline.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        outline.setFullyNonEditable(true);
        view.setPopupAllowed(false);

        view.setProperties(new Property<?>[]{
                    new PropertySupport.ReadOnly<Image>(
                    NeighbourHostNode.PROP_ACCEPTS_SSH, Image.class,
                    NbBundle.getMessage(NetworkNeighbourhoodList.class,
                    "NetworkNeighbourhoodList.ColumnSSH.Title"), // NOI18N
                    NbBundle.getMessage(NetworkNeighbourhoodList.class,
                    "NetworkNeighbourhoodList.HostCannotbeConnected")) {//NOI18N

                        @Override
                        public Image getValue() throws IllegalAccessException, InvocationTargetException {
                            return null;
                        }
                    }
                });

        outline.getColumn(NbBundle.getMessage(NetworkNeighbourhoodList.class,
                "NetworkNeighbourhoodList.ColumnSSH.Title")).setMaxWidth(40);//NOI18N

        add(view, BorderLayout.CENTER);
    }

    @Override
    public void addNotify() {
        super.addNotify();
        registry.addChangeListener(changeListener);
        registry.startScan();
    }

    @Override
    public void removeNotify() {
        registry.stopScan();
        registry.removeChangeListener(changeListener);
        super.removeNotify();
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return mgr;
    }
}
