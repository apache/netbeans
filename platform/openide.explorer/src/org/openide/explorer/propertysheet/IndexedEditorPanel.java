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
package org.openide.explorer.propertysheet;

import org.openide.awt.Mnemonics;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.view.TreeTableView;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.SystemAction;

import java.awt.Component;

import java.beans.*;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.Action;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.Scrollable;
import javax.swing.UIManager;
import javax.swing.border.Border;


/**
 * Panel displaying indexed properties.
 * @author  dstrupl@netbeans.org
 */
class IndexedEditorPanel extends javax.swing.JPanel implements ExplorerManager.Provider, PropertyChangeListener,
    Lookup.Provider {
    private ExplorerManager em;

    /** lookup for move up and down actions */
    private Lookup selectedLookup;
    private Action moveUp;
    private Action moveDown;
    private Action newAction;
    private boolean showingDetails = false;
    private Node rootNode;
    private Node.Property prop;
    private JScrollPane jScrollPane1 = new JScrollPane();
    private JPanel detailsPanel = new JPanel();

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton deleteButton;
    private javax.swing.JButton detailsButton;
    private javax.swing.JButton downButton;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JPanel jPanel2;
    private javax.swing.JButton newButton;
    private javax.swing.JLabel propertiesLabel;
    private javax.swing.JButton upButton;
    // End of variables declaration//GEN-END:variables
    // XXX look into constructor
    private TreeTableView treeTableView1;
    private static final Logger LOG = Logger.getLogger(IndexedEditorPanel.class.getName());

    /** Creates new form IndexedEditorPanel */
    public IndexedEditorPanel(Node node, Node.Property[] props) {
        treeTableView1 = new TreeTableView();

        // install proper border
        setBorder((Border) UIManager.get("Nb.ScrollPane.border")); // NOI18N
        initComponents();
        propertiesLabel.setLabelFor(treeTableView1);

        jPanel2.setLayout(new java.awt.BorderLayout());
        jPanel2.add(treeTableView1);

        detailsPanel.setLayout(new java.awt.BorderLayout());
        getExplorerManager().setRootContext(node);

        rootNode = node;
        prop = props[0];
        getExplorerManager().addPropertyChangeListener(this);
        treeTableView1.setProperties(props);
        treeTableView1.setRootVisible(false);
        treeTableView1.setDefaultActionAllowed(false);
        treeTableView1.setTreePreferredWidth(200);

        node.addPropertyChangeListener(this);

        try {
            ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);
            if (l == null) {
                l = Thread.currentThread().getContextClassLoader();
            }
            if (l == null) {
                l = getClass().getClassLoader();
            }
            
            selectedLookup = org.openide.util.lookup.Lookups.proxy(this);

            NodeAction globalMoveUp = SystemAction.get(Class.forName("org.openide.actions.MoveUpAction", true, l).asSubclass(NodeAction.class)); // NOI18N
            NodeAction globalMoveDown = SystemAction.get(Class.forName("org.openide.actions.MoveDownAction", true, l).asSubclass(NodeAction.class)); // NOI18N
            NodeAction globalNewAction = SystemAction.get(Class.forName("org.openide.actions.NewAction", true, l).asSubclass(NodeAction.class)); // NOI18N

            // Get context aware instances.
            moveUp = globalMoveUp.createContextAwareInstance(selectedLookup);
            moveDown = globalMoveDown.createContextAwareInstance(selectedLookup);
            newAction = globalNewAction.createContextAwareInstance(selectedLookup);
        } catch (ClassNotFoundException cnfe) {
            LOG.log(Level.INFO, "Maybe missing openide.actions module?", cnfe);
        }

        java.util.ResourceBundle bundle = NbBundle.getBundle(IndexedEditorPanel.class);
        treeTableView1.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_Properties"));
        newButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_New"));
        deleteButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_Delete"));
        upButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_MoveUp"));
        downButton.getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_MoveDown"));
        getAccessibleContext().setAccessibleDescription(bundle.getString("ACSD_IndexedEditorPanel"));
    }

    @Override
    public void addNotify() {
        super.addNotify();
        updateButtonState();
    }

    /** Returns the lookup of currently selected node.
     */
    public Lookup getLookup() {
        Node[] arr = getExplorerManager().getSelectedNodes();

        return (arr.length == 1) ? arr[0].getLookup() : Lookup.EMPTY;
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc=" Generated Code ">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        jPanel1 = new javax.swing.JPanel();
        newButton = new javax.swing.JButton();
        deleteButton = new javax.swing.JButton();
        upButton = new javax.swing.JButton();
        downButton = new javax.swing.JButton();
        detailsButton = new javax.swing.JButton();
        propertiesLabel = new javax.swing.JLabel();
        jPanel2 = new javax.swing.JPanel();

        FormListener formListener = new FormListener();

        setLayout(new java.awt.GridBagLayout());

        setBorder(javax.swing.BorderFactory.createEmptyBorder(12, 12, 0, 12));
        jPanel1.setLayout(new java.awt.GridLayout(5, 1, 0, 5));

        org.openide.awt.Mnemonics.setLocalizedText(newButton, org.openide.util.NbBundle.getBundle(IndexedEditorPanel.class).getString("CTL_New"));
        newButton.addActionListener(formListener);

        jPanel1.add(newButton);

        org.openide.awt.Mnemonics.setLocalizedText(deleteButton, org.openide.util.NbBundle.getBundle(IndexedEditorPanel.class).getString("CTL_Delete"));
        deleteButton.addActionListener(formListener);

        jPanel1.add(deleteButton);

        org.openide.awt.Mnemonics.setLocalizedText(upButton, org.openide.util.NbBundle.getBundle(IndexedEditorPanel.class).getString("CTL_MoveUp"));
        upButton.addActionListener(formListener);

        jPanel1.add(upButton);

        org.openide.awt.Mnemonics.setLocalizedText(downButton, org.openide.util.NbBundle.getBundle(IndexedEditorPanel.class).getString("CTL_MoveDown"));
        downButton.addActionListener(formListener);

        jPanel1.add(downButton);

        org.openide.awt.Mnemonics.setLocalizedText(detailsButton, org.openide.util.NbBundle.getBundle(IndexedEditorPanel.class).getString("CTL_HideDetails"));
        detailsButton.addActionListener(formListener);

        jPanel1.add(detailsButton);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.RELATIVE;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHEAST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 0);
        add(jPanel1, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(propertiesLabel, org.openide.util.NbBundle.getBundle(IndexedEditorPanel.class).getString("CTL_Properties"));
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 2, 11);
        add(propertiesLabel, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 11, 11);
        add(jPanel2, gridBagConstraints);

    }

    // Code for dispatching events from components to event handlers.

    private class FormListener implements java.awt.event.ActionListener {
        public void actionPerformed(java.awt.event.ActionEvent evt) {
            if (evt.getSource() == newButton) {
                IndexedEditorPanel.this.newButtonActionPerformed(evt);
            }
            else if (evt.getSource() == deleteButton) {
                IndexedEditorPanel.this.deleteButtonActionPerformed(evt);
            }
            else if (evt.getSource() == upButton) {
                IndexedEditorPanel.this.upButtonActionPerformed(evt);
            }
            else if (evt.getSource() == downButton) {
                IndexedEditorPanel.this.downButtonActionPerformed(evt);
            }
            else if (evt.getSource() == detailsButton) {
                IndexedEditorPanel.this.detailsButtonActionPerformed(evt);
            }
        }
    }// </editor-fold>//GEN-END:initComponents

    private void detailsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_detailsButtonActionPerformed
        showingDetails = !showingDetails;

        if (showingDetails && !this.equals(detailsPanel.getParent())) {
            initDetails();
        }

        updateButtonState();
        updateDetailsPanel();
    }
//GEN-LAST:event_detailsButtonActionPerformed

    private void newButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_newButtonActionPerformed

        try {
            getExplorerManager().setSelectedNodes(new Node[] { rootNode });
        } catch (PropertyVetoException pve) {
            // this should be always possible --> if not, notify problem
            PropertyDialogManager.notify(pve);
        }

        if ((newAction != null) && (newAction.isEnabled())) {
            newAction.actionPerformed(evt);
        }
    }
//GEN-LAST:event_newButtonActionPerformed

    private void deleteButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_deleteButtonActionPerformed

        Node[] sn = getExplorerManager().getSelectedNodes();

        if ((sn == null) || (sn.length != 1) || (sn[0] == rootNode)) {
            return;
        }

        try {
            sn[0].destroy();
        } catch (java.io.IOException ioe) {
            PropertyDialogManager.notify(ioe);
        }

        rootNode = getExplorerManager().getRootContext();
    }
//GEN-LAST:event_deleteButtonActionPerformed

    private void downButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_downButtonActionPerformed

        Node[] sn = getExplorerManager().getSelectedNodes();

        if ((moveDown != null) && (moveDown.isEnabled())) {
            moveDown.actionPerformed(evt);
        }

        if ((sn == null) || (sn.length != 1) || (sn[0] == rootNode)) {
            return;
        }

        try {
            getExplorerManager().setSelectedNodes(sn);
        } catch (PropertyVetoException pve) {
        }
    }
//GEN-LAST:event_downButtonActionPerformed

    private void upButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_upButtonActionPerformed

        Node[] sn = getExplorerManager().getSelectedNodes();

        if ((moveUp != null) && (moveUp.isEnabled())) {
            moveUp.actionPerformed(evt);
        }

        if ((sn == null) || (sn.length != 1) || (sn[0] == rootNode)) {
            return;
        }

        try {
            getExplorerManager().setSelectedNodes(sn);
        } catch (PropertyVetoException pve) {
        }
    }
//GEN-LAST:event_upButtonActionPerformed

    public synchronized ExplorerManager getExplorerManager() {
        if (em == null) {
            em = new ExplorerManager();
        }

        return em;
    }

    private void updateButtonState() {
        // refresh the lookup
        selectedLookup.lookup(Object.class);

        if (showingDetails) {
            detailsButton.setText(NbBundle.getMessage(IndexedEditorPanel.class, "CTL_HideDetails"));
        } else {
            detailsButton.setText(NbBundle.getMessage(IndexedEditorPanel.class, "CTL_ShowDetails"));
        }

        upButton.setEnabled((moveUp != null) && (moveUp.isEnabled()));
        downButton.setEnabled((moveDown != null) && (moveDown.isEnabled()));

        Node[] sn = getExplorerManager().getSelectedNodes();
        deleteButton.setEnabled((sn != null) && (sn.length == 1) && (sn[0] != rootNode));
        detailsButton.setVisible(
            (prop != null) && (prop.getPropertyEditor() != null) && (prop.getPropertyEditor().supportsCustomEditor())
        );

        if (detailsButton.isVisible()) {
            if (showingDetails) {
                Mnemonics.setLocalizedText(detailsButton, NbBundle.getMessage(IndexedEditorPanel.class, "CTL_HideDetails"));
                detailsButton.getAccessibleContext().setAccessibleDescription(
                    NbBundle.getMessage(IndexedEditorPanel.class, "ACSD_HideDetails")
                );
            } else {
                Mnemonics.setLocalizedText(detailsButton, NbBundle.getMessage(IndexedEditorPanel.class, "CTL_ShowDetails"));
                detailsButton.getAccessibleContext().setAccessibleDescription(
                    NbBundle.getMessage(IndexedEditorPanel.class, "ACSD_ShowDetails")
                );
            }

            detailsButton.setEnabled((sn != null) && (sn.length == 1) && (sn[0] != rootNode));
        }
    }

    private void updateDetailsPanel() {
        detailsPanel.removeAll();

        if (!showingDetails) {
            remove(detailsPanel);
            revalidateDetailsPanel();

            return;
        }

        Node[] selN = getExplorerManager().getSelectedNodes();

        if ((selN == null) || (selN.length == 0)) {
            revalidateDetailsPanel();

            return;
        }

        Node n = selN[0];

        if (n == rootNode) {
            revalidateDetailsPanel();

            return;
        }

        if (selN.length > 1) {
            n = new ProxyNode(selN);
        }

        // beware - this will function only if the DisplayIndexedNode has
        // one property on the first sheet and the property is of type
        // ValueProp
        Node.Property prop = n.getPropertySets()[0].getProperties()[0];
        PropertyPanel p = new PropertyPanel(prop);
        p.setPreferences(PropertyPanel.PREF_CUSTOM_EDITOR);

        if (isEditorScrollable(p)) {
            detailsPanel.add(p, java.awt.BorderLayout.CENTER);
        } else {
            jScrollPane1.setViewportView(p);
            detailsPanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);
        }

        revalidateDetailsPanel();
    }

    private void revalidateDetailsPanel() {
        detailsPanel.invalidate();
        repaint();

        if (detailsPanel.getParent() != null) {
            detailsPanel.getParent().validate();
        } else {
            detailsPanel.validate();
        }
    }

    /**
     * This method gets called when a bound property is changed.
     * @param evt A PropertyChangeEvent object describing the event source
     *          and the property that has changed.
     */
    public void propertyChange(PropertyChangeEvent evt) {
        if (ExplorerManager.PROP_SELECTED_NODES.equals(evt.getPropertyName())) {
            updateButtonState();
            updateDetailsPanel();
        }
    }

    private void initDetails() {
        java.awt.GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.gridheight = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTH;
        gridBagConstraints.weighty = 1.0;
        add(detailsPanel, gridBagConstraints);
    }

    private boolean isEditorScrollable(PropertyPanel p) {
        Component[] comps = p.getComponents();

        for (int i = 0; i < comps.length; i++) {
            if (comps[i] instanceof Scrollable || isInstanceOfTopComponent(comps[i])) {
                return true;
            }
        }

        return false;
    }

    /** Checks whether an object is instanceof TopComponent
     * @param obj the object
     * @return true or false
     */
    private static boolean isInstanceOfTopComponent(Object obj) {
        ClassLoader l = org.openide.util.Lookup.getDefault().lookup(ClassLoader.class);

        if (l == null) {
            l = IndexedEditorPanel.class.getClassLoader();
        }

        try {
            Class c = Class.forName("org.openide.windows.TopComponent", true, l); // NOI18N

            return c.isInstance(obj);
        } catch (Exception ex) {
            return false;
        }
    }
}
