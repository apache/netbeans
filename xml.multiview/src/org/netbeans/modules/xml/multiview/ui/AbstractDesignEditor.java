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
        
        SaveAction act = (SaveAction) org.openide.util.actions.SystemAction.get(SaveAction.class);
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
            ((PanelView)contentView).open();
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
