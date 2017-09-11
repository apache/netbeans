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

package org.netbeans.modules.xml.multiview.ui;

import org.netbeans.modules.xml.multiview.Error;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.ChoiceView;
import org.openide.explorer.view.NodeListModel;
import org.openide.util.Lookup;

import javax.swing.*;
import java.awt.*;

/**
 * The ComponentPanel three pane editor. This is basically a container that implements the ExplorerManager
 * interface. It coordinates the selection of a node in the structure pane and the display of a panel by the a PanelView
 * in the content pane and the nodes properties in the properties pane. It will populate the tree view in the structure pane
 * from the root node of the supplied PanelView.
 *
 **/

public class ToolBarDesignEditor extends AbstractDesignEditor {
    
    protected JComponent designPanel;
    private ErrorPanel errorPanel;
    private Object lastActive;

    /**
     * Creates a new instance of ToolBarDesignEditor
     * @param panel The PanelView which will provide the node tree for the structure view
     *              and the set of panels the nodes map to.
     */
    public ToolBarDesignEditor(){
        super();
        createDesignPanel();
        add(java.awt.BorderLayout.CENTER,designPanel);
        add(java.awt.BorderLayout.SOUTH,createErrorPanel());
    }
    
    /**
     * Creates a new instance of ToolBarDesignEditor
     * @param panel The PanelView which will provide the node tree for the structure view
     *              and the set of panels the nodes map to.
     */
    public ToolBarDesignEditor(PanelView panel){
        super(panel);
        createDesignPanel();
        designPanel.add(panel,BorderLayout.CENTER);
        add(java.awt.BorderLayout.CENTER,designPanel);
        add(java.awt.BorderLayout.SOUTH,createErrorPanel());
        panel.attachErrorPanel(errorPanel);
    }
    
    public JComponent createDesignPanel(){
        designPanel = new JPanel(new BorderLayout());
        return designPanel;
    }
    
    public void setContentView(PanelView panel) {
        if (getContentView()!=null) {
            designPanel.remove(getContentView());
        }
        designPanel.add(panel,BorderLayout.CENTER);
        panel.attachErrorPanel(errorPanel);
        super.setContentView(panel);
    }
    
    public ErrorPanel getErrorPanel() {
        return errorPanel;
    }
    
    public Error getError() {
        return(errorPanel==null?null:errorPanel.getError());
    }
    
    private ErrorPanel createErrorPanel() {
        errorPanel = new ErrorPanel(this);
        return errorPanel;
    }
    
    /**
     * Used to create an instance of the JComponent used for the structure component. Usually a subclass of BeanTreeView.
     * @return the JComponent
     */
    
    public JComponent createStructureComponent() {
        JToolBar toolbar = new ToolBarView(getExplorerManager(),getContentView().getRoot(), helpAction);
        return toolbar;
    }
 
    private static class ToolBarView extends JToolBar implements ExplorerManager.Provider, Lookup.Provider {
        private ExplorerManager manager;
        private Lookup lookup;
        private javax.swing.Action helpAction;
        ToolBarView(final ExplorerManager manager, org.openide.nodes.Node root, javax.swing.Action helpAction) {
            super();
            this.manager=manager;
            this.helpAction=helpAction;
            // same as before...
            
            setLayout(new java.awt.GridBagLayout());
            ActionMap map = getActionMap();
            // ...and initialization of lookup variable
            lookup = ExplorerUtils.createLookup (manager, map);

            ChoiceView cView = new ChoiceView();
            ((NodeListModel)(cView.getModel())).setNode(root);
            setFloatable(false);
            ((NodeListModel)(cView.getModel())).setDepth(5);
            
            java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 0;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.insets = new java.awt.Insets(0, 2, 4, 0);
            add(cView,gridBagConstraints);
            
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 1;
            gridBagConstraints.gridy = 0;
            gridBagConstraints.weightx = 1.0;
            JPanel filler = new JPanel();
            add(filler,gridBagConstraints);
            
            javax.swing.JButton helpButton = new javax.swing.JButton(helpAction);
            helpButton.setText("");
            helpButton.setContentAreaFilled(false);
            helpButton.setFocusPainted(false);
            helpButton.setBorderPainted(false);
            gridBagConstraints = new java.awt.GridBagConstraints();
            gridBagConstraints.gridx = 2;
            gridBagConstraints.gridy = 0;
            //gridBagConstraints.weightx = 1.0;
            add(helpButton,gridBagConstraints);
        }
        // ...method as before and getLookup
        public ExplorerManager getExplorerManager() {
            return manager;
        }
        
        public Lookup getLookup() {
            return lookup;
        }
        // ...methods as before, but replace componentActivated and
        // componentDeactivated with e.g.:
        
        public void addNotify() {
            //System.out.println("addNotify()");
            super.addNotify();
            ExplorerUtils.activateActions(manager, true);
        }
        public void removeNotify() {
            //System.out.println("removeNotify()");
            ExplorerUtils.activateActions(manager, false);
            //super.removeNotify();
        }
    }
    
    public Object getLastActive() {
        return lastActive;
    }
    
    public void setLastActive(Object lastActive) {
        this.lastActive=lastActive;
    }
}
