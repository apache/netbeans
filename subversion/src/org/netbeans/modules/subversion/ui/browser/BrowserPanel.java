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
package org.netbeans.modules.subversion.ui.browser;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.lang.reflect.InvocationTargetException;
import java.util.ResourceBundle;
import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.TreePath;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.OutlineView;
import org.openide.nodes.PropertySupport;
import org.openide.util.NbBundle;

/**
 *
 * @author Tomas Stupka
 */
public class BrowserPanel extends JPanel implements ExplorerManager.Provider {

    private final BrowserOutlineView outlineView;
    private final ExplorerManager manager;

    private ControlPanel controlPanel;
    
    /** Creates new form BrowserPanel */
    public BrowserPanel(String labelText, String browserAcsn, String browserAcsd, boolean singleSelection) {      
        setName(java.util.ResourceBundle.getBundle("org/netbeans/modules/subversion/ui/browser/Bundle").getString("CTL_Browser_Prompt"));                   // NOI18N
        
        manager = new ExplorerManager();
        
        setLayout(new GridBagLayout());
        
        outlineView = new BrowserOutlineView();
        outlineView.setDragSource(true);
        outlineView.setDropTarget(true);
              
        outlineView.setBorder(UIManager.getBorder("Nb.ScrollPane.border")); // NOI18N
        outlineView.getAccessibleContext().setAccessibleDescription(browserAcsd);
        outlineView.getAccessibleContext().setAccessibleName(browserAcsn);
        if(singleSelection) {
            outlineView.getOutline().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        }
        outlineView.setPopupAllowed(true);
        
        GridBagConstraints c = new GridBagConstraints();
        int gridY = 0;
                
        // title label        
        JLabel label = new JLabel();        
        label.setLabelFor(outlineView);
        label.setToolTipText(browserAcsd);
        if(labelText != null && !labelText.trim().equals("")) {
            org.openide.awt.Mnemonics.setLocalizedText(label, labelText);
        } else {
            org.openide.awt.Mnemonics.setLocalizedText(label, org.openide.util.NbBundle.getMessage(BrowserPanel.class, "BK2003"));                          // NOI18N
        }        
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = gridY++;
        c.insets = new Insets(4,0,4,4);
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.WEST;        
        
        add(label, c);        
        
        // treetable               
        c.gridx = 0;
        c.gridy = gridY++;
        c.fill = GridBagConstraints.BOTH;
        c.weighty = 1;
        c.weightx = 1;
        c.anchor = GridBagConstraints.WEST;
        c.gridwidth = 1;
        add(outlineView, c);
                
        // buttons
        controlPanel = new ControlPanel();        
        controlPanel.warningLabel.setVisible(false);
        c = new GridBagConstraints();
        c.gridx = 0;
        c.gridy = gridY++;
        c.fill = GridBagConstraints.HORIZONTAL;
        c.anchor = GridBagConstraints.EAST;                
        add(controlPanel, c);                
        
        setBorder(BorderFactory.createEmptyBorder(12,12,0,12));
        
        setPreferredSize(new Dimension(800, 400));
    }

    void expandNode(RepositoryPathNode repositoryPathNode) {
        outlineView.expandNode(repositoryPathNode);
    }
    
    void warning(String warningText) {
        if(warningText != null) {
            controlPanel.warningLabel.setText(warningText);
            controlPanel.warningLabel.setVisible(true);      
        } else {
            controlPanel.warningLabel.setText("");   
            controlPanel.warningLabel.setVisible(false);                  
        }     
    }
    
    public void setActions(AbstractAction[] actions) {
        if(actions != null) {
            controlPanel.buttonPanel.removeAll();
            for (int i = 0; i < actions.length; i++) {
                JButton button = new JButton(); 
                button.setAction(actions[i]);      
                button.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(BrowserPanel.class, "CTL_Action_MakeDir"));     // NOI18N
                org.openide.awt.Mnemonics.setLocalizedText(button, org.openide.util.NbBundle.getMessage(BrowserPanel.class, "CTL_Action_MakeDir"));         // NOI18N
                controlPanel.buttonPanel.add(button);                    
            }            
            revalidate();
        }                
    }
    
    @Override
    public ExplorerManager getExplorerManager() {
        return manager;
    }
    
    void addTreeExpansionListener(TreeExpansionListener l) {
        outlineView.addTreeExpansionListener(l);
    }
    
    void removeTreeExpansionListener(TreeExpansionListener l) {
        outlineView.removeTreeExpansionListener(l);
    }
    
    private class BrowserOutlineView extends OutlineView {
        BrowserOutlineView() {
            setupColumns();
        }
        
        public void startEditingAtPath(TreePath path) {            
            startEditingAtPath(path);
        }         
        
        @Override
        public void addNotify() {
            super.addNotify();
            setDefaultColumnSizes();
        }
        
        private void setupColumns() {
            ResourceBundle loc = NbBundle.getBundle(BrowserPanel.class);            
            setPropertyColumns(RepositoryPathNode.PROPERTY_NAME_REVISION, loc.getString("LBL_BrowserTree_Column_Revision"), //NOI18N
                RepositoryPathNode.PROPERTY_NAME_DATE, loc.getString("LBL_BrowserTree_Column_Date"), //NOI18N
                RepositoryPathNode.PROPERTY_NAME_AUTHOR, loc.getString("LBL_BrowserTree_Column_Author"), //NOI18N
                RepositoryPathNode.PROPERTY_NAME_HISTORY, loc.getString("LBL_BrowserTree_Column_History")); //NOI18N
            setPropertyColumnDescription(RepositoryPathNode.PROPERTY_NAME_REVISION, loc.getString("LBL_BrowserTree_Column_Revision_Desc")); //NOI18N
            setPropertyColumnDescription(RepositoryPathNode.PROPERTY_NAME_DATE, loc.getString("LBL_BrowserTree_Column_Date_Desc")); //NOI18N
            setPropertyColumnDescription(RepositoryPathNode.PROPERTY_NAME_AUTHOR, loc.getString("LBL_BrowserTree_Column_Author_Desc")); //NOI18N
            setPropertyColumnDescription(RepositoryPathNode.PROPERTY_NAME_HISTORY, loc.getString("LBL_BrowserTree_Column_History_Desc")); //NOI18N
        }    
    
        private void setDefaultColumnSizes() {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    int width = getWidth();                    
                    getOutline().getColumnModel().getColumn(0).setPreferredWidth(width * 50 / 100);
                    getOutline().getColumnModel().getColumn(1).setPreferredWidth(width * 10 / 100);
                    getOutline().getColumnModel().getColumn(2).setPreferredWidth(width * 20 / 100);
                    getOutline().getColumnModel().getColumn(3).setPreferredWidth(width * 10 / 100);
                    getOutline().getColumnModel().getColumn(4).setPreferredWidth(width * 10 / 100);
                }
            });
        }            
    }     

    private static class ColumnDescriptor<T> extends PropertySupport.ReadOnly<T> {        
        public ColumnDescriptor(String name, Class<T> type, String displayName, String shortDescription) {
            super(name, type, displayName, shortDescription);
        }

        @Override
        public T getValue() throws IllegalAccessException, InvocationTargetException {
            return null;
        }
    }    
}
