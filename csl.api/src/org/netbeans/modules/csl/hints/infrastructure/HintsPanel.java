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
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
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

package org.netbeans.modules.csl.hints.infrastructure;

import java.awt.Component;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.prefs.Preferences;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JCheckBox;
import javax.swing.JEditorPane;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.modules.csl.api.Rule;

import org.netbeans.modules.csl.api.Rule.UserConfigurableRule;
import org.netbeans.modules.options.editor.spi.OptionsFilter;
import org.openide.filesystems.FileObject;
import org.openide.util.NbBundle;

public final class HintsPanel extends javax.swing.JPanel implements TreeCellRenderer  {
    
    private DefaultTreeCellRenderer dr = new DefaultTreeCellRenderer();
    private JCheckBox renderer = new JCheckBox();
    private HintsPanelLogic logic;
    private GsfHintsManager manager;
    private OptionsFilter filter;
    private DefaultTreeModel baseModel;
      
    public HintsPanel(OptionsFilter filter, 
            javax.swing.tree.TreeModel treeModel, GsfHintsManager manager) {        
        this.manager = manager;
        initComponents();
        
        descriptionTextArea.setContentType("text/html"); // NOI18N
        descriptionTextArea.putClientProperty(JEditorPane.HONOR_DISPLAY_PROPERTIES,Boolean.TRUE);

        if( "Windows".equals(UIManager.getLookAndFeel().getID()) ) { //NOI18N
            setOpaque(false);
        }
        
        errorTree.setCellRenderer( this );
        errorTree.setRootVisible( false );
        errorTree.setShowsRootHandles( true );
        errorTree.getSelectionModel().setSelectionMode( TreeSelectionModel.SINGLE_TREE_SELECTION );
        
        DefaultComboBoxModel model = new DefaultComboBoxModel();
        model.addElement( NbBundle.getMessage(HintsPanel.class, "CTL_AsError"));
        model.addElement( NbBundle.getMessage(HintsPanel.class, "CTL_AsWarning"));
        model.addElement( NbBundle.getMessage(HintsPanel.class, "CTL_WarningOnCurrentLine"));
        model.addElement( NbBundle.getMessage(HintsPanel.class, "CTL_Info"));
        severityComboBox.setModel(model);
        
        toProblemCheckBox.setVisible(false);
        
        update();

        //errorTree.setModel( RulesManager.getInstance().getHintsTreeModel() );
        DefaultMutableTreeNode root = (DefaultMutableTreeNode) treeModel.getRoot();
        treeModel = new DefaultTreeModel(sort(root));
        errorTree.setModel(treeModel);      
        this.baseModel = (DefaultTreeModel)treeModel;
        
        if (filter != null) {
            filter.installFilteringModel(errorTree, treeModel, new AcceptorImpl());
        }
        
        // Expand all
        for(int lastRow = errorTree.getRowCount(); lastRow >= 0; --lastRow) {
            errorTree.expandRow(lastRow);
        }        
    }

    private DefaultMutableTreeNode sort(DefaultMutableTreeNode parent) {
        List<DefaultMutableTreeNode> nodes = new ArrayList<DefaultMutableTreeNode>();

        for (int i = 0; i < parent.getChildCount(); i++) {
            DefaultMutableTreeNode node = (DefaultMutableTreeNode) parent.getChildAt(i);
            nodes.add(sort(node));
        }

        Collections.sort(nodes, new Comparator<DefaultMutableTreeNode>() {
            @Override
            public int compare(DefaultMutableTreeNode p1, DefaultMutableTreeNode p2) {
                Object o1 = p1.getUserObject();
                String s1 = "";
                if (o1 instanceof Rule) {
                    s1 = ((Rule) o1).getDisplayName();
                }
                if (o1 instanceof FileObject) {
                    s1 = getFileObjectLocalizedName((FileObject) o1);
                }

                Object o2 = p2.getUserObject();
                String s2 = "";
                if (o2 instanceof Rule) {
                    s2 = ((Rule) o2).getDisplayName();
                }
                if (o2 instanceof FileObject) {
                    s2 = getFileObjectLocalizedName((FileObject) o2);
                }
                return s1.compareTo(s2);
            }
        });
        parent.removeAllChildren();
        for (DefaultMutableTreeNode node : nodes) {
            parent.add(node);
        }

        return parent;
    }
 
    private class AcceptorImpl implements OptionsFilter.Acceptor {
        @Override
        public boolean accept(Object originalTreeNode, String filterText) {
            DefaultMutableTreeNode tn = (DefaultMutableTreeNode)originalTreeNode;
            Object o = tn.getUserObject();
            if (!(o instanceof Rule)) {
                return false;
            }
            
            Rule r = (Rule)o;
            if (filterText == null) {
                return true;
            }
            filterText = filterText.toLowerCase();
            if (r.getDisplayName().toLowerCase().contains(filterText)) {
                return true;
            }
            if (r instanceof UserConfigurableRule) {
                String htmlDesc = (((UserConfigurableRule)r)).getDescription().toLowerCase();
                // filter out opening and closing tags. Hope that > does not appear in attribute values.
                String untagged = htmlDesc.replaceAll("</?[a-z0-9]+.*?>", ""); // NOI18N
                return untagged.
                        toLowerCase().contains(filterText);
            }
            return false;
        }
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jSplitPane1 = new javax.swing.JSplitPane();
        treePanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        errorTree = new javax.swing.JTree();
        detailsPanel = new javax.swing.JPanel();
        optionsPanel = new javax.swing.JPanel();
        severityLabel = new javax.swing.JLabel();
        severityComboBox = new javax.swing.JComboBox();
        toProblemCheckBox = new javax.swing.JCheckBox();
        customizerPanel = new javax.swing.JPanel();
        descriptionPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        descriptionTextArea = new javax.swing.JEditorPane();
        descriptionLabel = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        setLayout(new java.awt.GridBagLayout());

        jSplitPane1.setBorder(null);
        jSplitPane1.setDividerLocation(320);

        treePanel.setOpaque(false);
        treePanel.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setViewportView(errorTree);

        treePanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jSplitPane1.setLeftComponent(treePanel);

        detailsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 6, 0, 0));
        detailsPanel.setOpaque(false);
        detailsPanel.setLayout(new java.awt.GridBagLayout());

        optionsPanel.setOpaque(false);
        optionsPanel.setLayout(new java.awt.GridBagLayout());

        severityLabel.setLabelFor(severityComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(severityLabel, org.openide.util.NbBundle.getMessage(HintsPanel.class, "CTL_ShowAs_Label")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 4);
        optionsPanel.add(severityLabel, gridBagConstraints);

        severityComboBox.setModel(new javax.swing.DefaultComboBoxModel(new String[] { "Item 1", "Item 2", "Item 3", "Item 4" }));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        optionsPanel.add(severityComboBox, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(toProblemCheckBox, org.openide.util.NbBundle.getMessage(HintsPanel.class, "CTL_InTasklist_CheckBox")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
        optionsPanel.add(toProblemCheckBox, gridBagConstraints);

        customizerPanel.setOpaque(false);
        customizerPanel.setLayout(new java.awt.BorderLayout());
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(8, 0, 0, 0);
        optionsPanel.add(customizerPanel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.7;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 12, 0);
        detailsPanel.add(optionsPanel, gridBagConstraints);

        descriptionPanel.setOpaque(false);
        descriptionPanel.setLayout(new java.awt.GridBagLayout());

        descriptionTextArea.setEditable(false);
        descriptionTextArea.setPreferredSize(new java.awt.Dimension(100, 50));
        jScrollPane2.setViewportView(descriptionTextArea);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 0, 0);
        descriptionPanel.add(jScrollPane2, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(descriptionLabel, org.openide.util.NbBundle.getMessage(HintsPanel.class, "CTL_Description_Border")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        descriptionPanel.add(descriptionLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 0.3;
        detailsPanel.add(descriptionPanel, gridBagConstraints);

        jSplitPane1.setRightComponent(detailsPanel);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.weighty = 1.0;
        add(jSplitPane1, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
        
    public synchronized void update() {
        if ( logic != null ) {
            logic.disconnect();
        }
        logic = new HintsPanelLogic(manager);
        logic.connect(errorTree, baseModel, severityComboBox, toProblemCheckBox, customizerPanel, descriptionTextArea);
    }
    
    public void cancel() {
        if (logic != null) {
            logic.disconnect();
            logic = null;
        }
    }
    
    public boolean isChanged() {
        return logic != null ? logic.isChanged() : false;
    }
    
    public void applyChanges() {
        if (logic != null) {
            logic.applyChanges();
            logic.disconnect();
            logic = null;
        }
    }
           
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        
        renderer.setBackground( selected ? dr.getBackgroundSelectionColor() : dr.getBackgroundNonSelectionColor() );
        renderer.setForeground( selected ? dr.getTextSelectionColor() : dr.getTextNonSelectionColor() );
        renderer.setEnabled( true );

        Object data = ((DefaultMutableTreeNode)value).getUserObject();
        if ( data instanceof FileObject ) {
            FileObject fo = ((FileObject)data);            
            renderer.setText( getFileObjectLocalizedName(fo) );
            if (logic != null) {
                renderer.setSelected( logic.isSelected((DefaultMutableTreeNode)value));
            }
        }
        else if ( data instanceof UserConfigurableRule ) {
            UserConfigurableRule treeRule = (UserConfigurableRule)data;
            renderer.setText( treeRule.getDisplayName() );            

            if (logic != null) {
                Preferences node = logic.getCurrentPrefernces(treeRule);
                renderer.setSelected( HintsSettings.isEnabled(manager, treeRule, node));
            }
        }
        else {
            renderer.setText( value.toString() );
        }

        return renderer;
    }
    
    private String getFileObjectLocalizedName( FileObject fo ) {
        Object o = fo.getAttribute("SystemFileSystem.localizingBundle"); // NOI18N
        if ( o instanceof String ) {
            String bundleName = (String)o;
            try {
                ResourceBundle rb = NbBundle.getBundle(bundleName);            
                String localizedName = rb.getString(fo.getPath());                
                return localizedName;
            }
            catch(MissingResourceException ex ) {
                // Do nothing return file path;
            }
        }
        return fo.getPath();
    } 
        
    // Variables declaration - do not modify                     
        
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JPanel customizerPanel;
    private javax.swing.JLabel descriptionLabel;
    private javax.swing.JPanel descriptionPanel;
    private javax.swing.JEditorPane descriptionTextArea;
    private javax.swing.JPanel detailsPanel;
    private javax.swing.JTree errorTree;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JSplitPane jSplitPane1;
    private javax.swing.JPanel optionsPanel;
    private javax.swing.JComboBox severityComboBox;
    private javax.swing.JLabel severityLabel;
    private javax.swing.JCheckBox toProblemCheckBox;
    private javax.swing.JPanel treePanel;
    // End of variables declaration//GEN-END:variables



}

