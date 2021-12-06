/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
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
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2006 Sun
 * Microsystems, Inc. All Rights Reserved.
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
 */

/*
 * Copyright 2021 HCL America, Inc.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *    
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
*/

package org.netbeans.modules.versionvault.ui.selectors;

import org.openide.explorer.view.BeanTreeView;
import org.openide.explorer.ExplorerManager;
import org.openide.nodes.Node;
import org.openide.nodes.NodeAcceptor;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.awt.Mnemonics;
import org.openide.util.UserCancelException;
import org.openide.util.HelpCtx;

import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.*;
import java.awt.*;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;

/**
 * NodeOperations without icons and root.
 *
 * @author Petr Kuzel
 * @see issue #63249
 */
public final class NodeOperation2 extends BeanTreeView implements PropertyChangeListener {

    private final ExplorerManager manager = new ExplorerManager();

    private DialogDescriptor dd;
    private boolean hideIcons;
    private NodeAcceptor acceptor;
    private HelpCtx helpCtx;

    /**
     * Creates new selector with default node renderer.
     */
    public NodeOperation2() {
    }

    public Node[] select(String title, String subtitle, String acsd, Node root, String browserAcsn, String browserAcsd, NodeAcceptor acceptor) throws UserCancelException {
        manager.setRootContext(root);
        manager.addPropertyChangeListener(this);

        if (hideIcons) {
            DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
            renderer.setOpenIcon(null);
            renderer.setClosedIcon(null);
            renderer.setLeafIcon(null);
            tree.setCellRenderer(renderer);
        }

        setPopupAllowed (false);
        setDefaultActionAllowed (false);
        setBorder(UIManager.getBorder("Nb.ScrollPane.border")); // NOI18N

        JLabel label = new JLabel();
        Mnemonics.setLocalizedText(label, subtitle);
        label.setLabelFor(tree);
        label.setToolTipText(browserAcsd);
        getAccessibleContext().setAccessibleDescription(browserAcsd);
        getAccessibleContext().setAccessibleName(browserAcsn);
        ExplorerParent pane = new ExplorerParent(this);
        pane.add(label, BorderLayout.NORTH);
        pane.setBorder(BorderFactory.createEmptyBorder(12,12,0,12));

        dd = new DialogDescriptor(pane, title);
        dd.setModal(true);
        if (helpCtx  != null) {
            dd.setHelpCtx(helpCtx);
        }
        this.acceptor = acceptor;
        testAccept();

        Dialog dialog = DialogDisplayer.getDefault().createDialog(dd);
        dialog.getAccessibleContext().setAccessibleDescription(acsd);
        dialog.setVisible(true);

        if (dd.getValue() == DialogDescriptor.OK_OPTION) {
            return manager.getSelectedNodes();
        } else {
            throw new UserCancelException();
        }
    }

    public void setIconsVisible(boolean visible) {
        hideIcons = visible == false;
    }

    public void setHelpCtx(HelpCtx help) {
        helpCtx = help;
    }

    public void propertyChange (PropertyChangeEvent ev) {
        if (ev.getPropertyName().equals (ExplorerManager.PROP_SELECTED_NODES)) {
            testAccept();
        }
    }

    private void testAccept() {
        dd.setValid(acceptor.acceptNodes (manager.getSelectedNodes()));
    }

    /**
     * Provides explorer manager for given client.
     * Workarounds ExprorerManager.find behaviour.
     */
    private class ExplorerParent extends JPanel implements ExplorerManager.Provider {
        public ExplorerParent(Component client) {
            super(new BorderLayout(6, 6));
            add(client, BorderLayout.CENTER);
        }

        public ExplorerManager getExplorerManager() {
            return manager;
        }
    }
}
