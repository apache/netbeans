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

package org.netbeans.modules.xml.multiview.ui;

import java.awt.BorderLayout;
import java.beans.*;
import javax.swing.JComponent;
import javax.swing.ActionMap;
import javax.swing.KeyStroke;

import org.openide.nodes.*;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.windows.TopComponent;
import org.openide.util.HelpCtx;
import org.openide.actions.SaveAction;

/**
 * The ComponentPanel three pane editor. This is basically a container that implements the ExplorerManager
 * interface. It coordinates the selection of a node in the structure pane and the display of a panel by the a PanelView
 * in the content pane and the nodes properties in the properties pane. It will populate the tree view in the structure pane
 * from the root node of the supplied PanelView.
 *
 **/

public abstract class AbstractDesignEditor extends TopComponent implements ExplorerManager.Provider {
    public static final String PROPERTY_FLUSH_DATA = "Flush Data"; // NOI18N
    
    private static final String ACTION_INVOKE_HELP = "invokeHelp"; //NOI18N
    protected JComponent structureView;
    protected PanelView contentView;
    protected javax.swing.Action helpAction;
    private ExplorerManager manager;
    
    /** The icon for ComponentInspector */
    protected static String iconURL = "/org/netbeans/modules/form/resources/inspector.gif"; // NOI18N
    
    protected static final long serialVersionUID =1L;
    
    public AbstractDesignEditor() {
        init();
    }
    
    private void init() {
        manager = new ExplorerManager();
        helpAction = new HelpAction();
        final ActionMap map = AbstractDesignEditor.this.getActionMap();
        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(
            KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_F1, 0), ACTION_INVOKE_HELP);
        map.put(ACTION_INVOKE_HELP, helpAction);
        
        SaveAction act = org.openide.util.actions.SystemAction.get(SaveAction.class);
        KeyStroke stroke = KeyStroke.getKeyStroke(java.awt.event.KeyEvent.VK_S,
                java.awt.Toolkit.getDefaultToolkit().getMenuShortcutKeyMask());

        getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(stroke, "save"); //NOI18N
        map.put("save", act); //NOI18N  
       
        associateLookup(ExplorerUtils.createLookup(manager, map));
        
        manager.addPropertyChangeListener(new NodeSelectedListener());
        setLayout(new BorderLayout());
    }

    /**
     * Creates a new instance of ComponentPanel
     * @param contentView The PanelView which will provide the node tree for the structure view
     *              and the set of panels the nodes map to.
     */
    public AbstractDesignEditor(PanelView contentView){
        init();
        this.contentView = contentView;
        setRootContext(contentView.getRoot());
    }
    
    public void setContentView(PanelView panelView) {
        contentView = panelView;
        setRootContext(panelView.getRoot());
    }
    
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
    public void componentActivated() {
        ExplorerUtils.activateActions(manager, true);
    }
    
    public void componentDeactivated() {
        ExplorerUtils.activateActions(manager, false);
    }
    
    public void componentClosed() {
        super.componentClosed();
    }
    public void componentOpened() {
        super.componentOpened();
    }
    public void componentShowing() {
        super.componentShowing();
    }
    public void componentHidden() {
        super.componentShowing();
    }  
    
    /**
     * Sets the root context for the ExplorerManager
     * @param node The new root context.
     */
    public void setRootContext(Node node) {
        getExplorerManager().setRootContext(node);
    }
 
    /**
     * Used to get the JComponent used for the content pane. Usually a subclass of PanelView.
     * @return the JComponent
     */
    public PanelView getContentView(){
        return contentView;
    }
    
    /**
     * Used to get the JComponent used for the structure pane. Usually a container for the structure component or the structure component itself.
     * @return the JComponent
     */
    public JComponent getStructureView(){
        if (structureView ==null){
            structureView = createStructureComponent();
            structureView.addPropertyChangeListener(new NodeSelectedListener());
        }
        return structureView;
    }
    /**
     * Used to create an instance of the JComponent used for the structure component. Usually a subclass of BeanTreeView.
     * @return the JComponent
     */
    abstract public JComponent createStructureComponent() ;

    abstract public ErrorPanel getErrorPanel();
    
    /**
     * A parent TopComponent can use this method to notify the ComponentPanel and it PanelView children that it was opened
     * and lets them do any needed initialization as a result. Default implementation just delegates to the PanelView.
     */
    public void open(){
        if (contentView!=null)
            contentView.open();
    }
    
    /**
     * returns the HelpCtx for this component.
     * @return the HelpCtx
     */
    public HelpCtx getHelpCtx() {
        return new HelpCtx("ComponentPanel"); // NOI18N
    }
    
    class NodeSelectedListener implements PropertyChangeListener {
        public void propertyChange(PropertyChangeEvent evt) {
            if (contentView.isSectionHeaderClicked()) {
                contentView.setSectionHeaderClicked(false);
                return;
            }
            if (!ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName()))
                return;

            Node[] selectedNodes = getExplorerManager().getSelectedNodes();
            if (selectedNodes!=null && selectedNodes.length>0)
                contentView.showSelection(selectedNodes);
        }
    }
    
    final class HelpAction extends javax.swing.AbstractAction {
        HelpCtx.Provider provider = null;
        public HelpAction() {
            super(org.openide.util.NbBundle.getMessage(AbstractDesignEditor.class,"CTL_Help"),
                  new javax.swing.ImageIcon (
                      AbstractDesignEditor.this.getClass().getResource("/org/netbeans/modules/xml/multiview/resources/help.gif"))); //NOI18N
        }
        
        public boolean isEnabled() {
            return getContext() != null;
        }
        
        public void actionPerformed(java.awt.event.ActionEvent e) {
            HelpCtx ctx = getContext();
            if (ctx == null || !ctx.display()) {
                java.awt.Toolkit.getDefaultToolkit().beep();
            }
        }
        
        private HelpCtx getContext() {
            Node[] selectedNodes = getExplorerManager().getSelectedNodes();
            if (selectedNodes!=null && selectedNodes.length>0)
                return selectedNodes[0].getHelpCtx();
            else 
                return null;
        }
    }

    public void fireVetoableChange(String propertyName, Object oldValue, Object newValue)
            throws PropertyVetoException {
        super.fireVetoableChange(propertyName, oldValue, newValue);
    }
}
