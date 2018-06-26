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
package org.netbeans.modules.websvc.core.dev.wizard;

import java.awt.BorderLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import javax.swing.JPanel;
import javax.swing.tree.TreeSelectionModel;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.BeanTreeView;
import org.openide.nodes.Node;


/**
 *
 * @author ChrisWebster
 */
public class NodeDisplayPanel extends JPanel implements ExplorerManager.Provider {
    private PropertyChangeSupport pcs;
    private ExplorerManager manager = new ExplorerManager();
    
    /** Creates a new instance of NodeDisplayPanel */
    public NodeDisplayPanel(Node rootNode) {
        BeanTreeView btv = new BeanTreeView();
        btv.setRootVisible(false);
        btv.setDefaultActionAllowed(false);
        btv.setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
        manager.setRootContext(rootNode);
        Node[] rootChildren = rootNode.getChildren().getNodes();
        for (int i = 0; i < rootChildren.length; i++) {
            btv.expandNode(rootChildren[i]);
        }
        manager.addPropertyChangeListener(
        new PropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent pce) {
                firePropertyChange();
            }
        });
        setLayout(new BorderLayout());
        add(btv, BorderLayout.CENTER);
        btv.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(NodeDisplayPanel.class, "ACSD_PortNodeTreeView"));
        btv.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(NodeDisplayPanel.class, "ACSD_PortNodeTreeView"));
    }
    
    /** Ubuntu workaround
     */
    private PropertyChangeSupport getProperChangeSupport() {
        if(pcs == null) {
            pcs =  new PropertyChangeSupport(this);
        }
        return pcs;
    }
    
    public void addPropertyChangeListener(PropertyChangeListener l) {
        getProperChangeSupport().addPropertyChangeListener(l);
    }
    
    private void firePropertyChange() {
        getProperChangeSupport().firePropertyChange(ExplorerManager.PROP_NODE_CHANGE, null, null);
    }
    
    public Node[] getSelectedNodes() {
        return manager.getSelectedNodes();
    }

    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
}
