/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package org.netbeans.modules.form.wizard;

import java.util.*;
import java.beans.EventSetDescriptor;
import javax.swing.tree.*;
import javax.swing.event.*;

import org.openide.util.NbBundle;
import org.netbeans.modules.form.*;


/**
 * The UI component of the ConnectionWizardPanel1.
 *
 * @author Tomas Pavek
 */

class ConnectionPanel1 extends javax.swing.JPanel {

    private ConnectionWizardPanel1 wizardPanel;

    /** Creates new form ConnectionPanel1 */
    ConnectionPanel1(ConnectionWizardPanel1 wizardPanel) {
        this.wizardPanel = wizardPanel;

        initComponents();

        RADComponent source = wizardPanel.getSourceComponent();

        java.util.ResourceBundle bundle = NbBundle.getBundle(ConnectionPanel1.class);

        setName(bundle.getString("CTL_CW_Step1_Title")); // NOI18N
        sourceComponentName.setText(source.getName());

        eventNameCombo.setEnabled(wizardPanel.getSelectedEvent() != null);

        eventNameCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                ConnectionPanel1.this.wizardPanel.fireStateChanged();
            }
        });

        eventNameCombo.getEditor().addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent e) {
                ConnectionPanel1.this.wizardPanel.fireStateChanged();
            }
        });

         // populate event tree
        final Vector eventNodes = new Vector();
        TreeNode rootNode = new TreeNode() {
            public TreeNode getChildAt(int childIndex) {
                return(TreeNode) eventNodes.elementAt(childIndex);
            }
            public int getChildCount() {
                return eventNodes.size();
            }
            public TreeNode getParent() {
                return null;
            }
            public int getIndex(TreeNode node) {
                return eventNodes.indexOf(node);
            }
            public boolean getAllowsChildren() {
                return true;
            }
            public boolean isLeaf() {
                return false;
            }
            public Enumeration children() {
                return eventNodes.elements();
            }
        };

        EventSetDescriptor lastEventSetDesc = null;
        TreeNode eventSetNode = null;
        List eventSetEvents = null;

        Event[] events = source.getAllEvents();
        for (int i=0; i < events.length; i++) {
            Event event = events[i];
            EventSetDescriptor eventSetDesc = event.getEventSetDescriptor();

            if (eventSetDesc != lastEventSetDesc) {
                eventSetEvents = new ArrayList();
                eventSetNode = new EventSetNode(rootNode,
                                                eventSetDesc.getName(),
                                                eventSetEvents);
                eventNodes.add(eventSetNode);
                lastEventSetDesc = eventSetDesc;
            }

            eventSetEvents.add(new EventNode(eventSetNode, event));
        }

        DefaultTreeSelectionModel treeSelectionModel = new DefaultTreeSelectionModel();
        treeSelectionModel.addTreeSelectionListener(new TreeSelectionListener() {
            public void valueChanged(TreeSelectionEvent evt) {
                TreePath[] paths = eventSelectTree.getSelectionPaths();
                if ((paths != null) &&(paths.length == 1)) {
                    TreeNode node =(TreeNode) paths[0].getLastPathComponent();
                    if ((node != null) &&(node instanceof EventNode)) {
                        setSelectedEvent(((EventNode)node).getEvent());
                        return;
                    }
                }
                setSelectedEvent(null);
            }
        });

        treeSelectionModel.setSelectionMode(DefaultTreeSelectionModel.SINGLE_TREE_SELECTION);
        eventSelectTree.setModel(new DefaultTreeModel(rootNode));
        eventSelectTree.setSelectionModel(treeSelectionModel);

        // localization code
        sourceNameLabel.setText(
            bundle.getString("CTL_CW_SourceComponent")); // NOI18N
        sourceNameLabel.setDisplayedMnemonic(
            bundle.getString("CTL_CW_SourceComponent_Mnemonic").charAt(0)); // NOI18N
        sourceComponentName.setToolTipText(
            bundle.getString("CTL_CW_SourceComponent_Hint")); // NOI18N
        eventSelectLabel.setText(bundle.getString("CTL_CW_Event")); // NOI18N
        eventSelectLabel.setDisplayedMnemonic(
            bundle.getString("CTL_CW_Event_Mnemonic").charAt(0)); // NOI18N
        eventSelectTree.setToolTipText(bundle.getString("CTL_CW_Event_Hint"));
        sourcePanel.setToolTipText(bundle.getString("CTL_CW_Event_Hint"));
        eventHandlerPanel.setBorder(new javax.swing.border.CompoundBorder(
                new javax.swing.border.TitledBorder(
                    new javax.swing.border.EtchedBorder(),
                    bundle.getString("CTL_CW_EventHandlerMethod")), // NOI18N
                new javax.swing.border.EmptyBorder(new java.awt.Insets(5, 5, 5, 5))));
        eventNameLabel.setText(bundle.getString("CTL_CW_MethodName")); // NOI18N
        eventNameLabel.setDisplayedMnemonic(
            bundle.getString("CTL_CW_MethodName_Mnemonic").charAt(0)); // NOI18N
        
        eventSelectTree.getAccessibleContext().setAccessibleDescription(
            bundle.getString("ACSD_CW_EventTree")); // NOI18N
        eventNameCombo.getAccessibleContext().setAccessibleDescription(
            bundle.getString("ACSD_CW_MethodName")); // NOI18N
        sourceComponentName.getAccessibleContext().setAccessibleDescription(
            bundle.getString("ACSD_CW_SourceComponent")); // NOI18N
        getAccessibleContext().setAccessibleDescription(
            bundle.getString("ACSD_CW_ConnectionPanel1")); // NOI18N
        putClientProperty(WizardDescriptor.PROP_CONTENT_SELECTED_INDEX, new Integer(0)); // NOI18N
    }

    public java.awt.Dimension getPreferredSize() {
        return new java.awt.Dimension(450, 300);
    }

    String getEventName() {
        return (String) eventNameCombo.getEditor().getItem();
    }

    private void setSelectedEvent(Event event) {
        eventNameCombo.removeAllItems();

        if (event != null) {
            eventNameCombo.setEnabled(true);

            FormEvents formEvents = wizardPanel.getSourceComponent()
                                       .getFormModel().getFormEvents();
            String defaultName = formEvents.findFreeHandlerName(
                                     event, wizardPanel.getSourceComponent());

            eventNameCombo.addItem(defaultName);

            if (event.hasEventHandlers()) {
                String[] handlers = event.getEventHandlers();
                for (int i=0; i < handlers.length; i++)
                    eventNameCombo.addItem(handlers[i]);
            }

            eventNameCombo.setSelectedIndex(0);
        }
        else eventNameCombo.setEnabled(false);

        wizardPanel.setSelectedEvent(event);
    }

    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    private void initComponents() {//GEN-BEGIN:initComponents
        sourcePanel = new javax.swing.JPanel();
        sourceInfoPanel = new javax.swing.JPanel();
        sourceNamePanel = new javax.swing.JPanel();
        sourceNameLabel = new javax.swing.JLabel();
        sourceComponentName = new javax.swing.JTextField();
        eventSelectLabelPanel = new javax.swing.JPanel();
        eventSelectLabel = new javax.swing.JLabel();
        eventSelectScroll = new javax.swing.JScrollPane();
        eventSelectTree = new javax.swing.JTree();
        eventHandlerPanel = new javax.swing.JPanel();
        eventNameLabel = new javax.swing.JLabel();
        eventNameCombo = new javax.swing.JComboBox();

        setLayout(new java.awt.BorderLayout(0, 11));

        sourcePanel.setLayout(new java.awt.BorderLayout());

        sourceInfoPanel.setLayout(new java.awt.GridLayout(2, 1));

        sourceNamePanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 0));

        sourceNameLabel.setLabelFor(sourceComponentName);
        sourceNameLabel.setText("Source Component:");
        sourceNameLabel.setBorder(new javax.swing.border.EmptyBorder(new java.awt.Insets(0, 0, 0, 6)));
        sourceNamePanel.add(sourceNameLabel);

        sourceComponentName.setEditable(false);
        sourceComponentName.setText("jTextField1");
        sourceNamePanel.add(sourceComponentName);

        sourceInfoPanel.add(sourceNamePanel);

        eventSelectLabelPanel.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT, 0, 3));

        eventSelectLabel.setLabelFor(eventSelectTree);
        eventSelectLabel.setText("Events");
        eventSelectLabelPanel.add(eventSelectLabel);

        sourceInfoPanel.add(eventSelectLabelPanel);

        sourcePanel.add(sourceInfoPanel, java.awt.BorderLayout.NORTH);

        eventSelectScroll.setMaximumSize(new java.awt.Dimension(32767, 100));
        eventSelectTree.setRootVisible(false);
        eventSelectTree.setShowsRootHandles(true);
        eventSelectScroll.setViewportView(eventSelectTree);

        sourcePanel.add(eventSelectScroll, java.awt.BorderLayout.CENTER);

        add(sourcePanel, java.awt.BorderLayout.CENTER);

        eventHandlerPanel.setLayout(new java.awt.BorderLayout(8, 0));

        eventHandlerPanel.setBorder(new javax.swing.border.TitledBorder("Event Handler Method"));
        eventNameLabel.setLabelFor(eventNameCombo);
        eventNameLabel.setText("Method Name:");
        eventHandlerPanel.add(eventNameLabel, java.awt.BorderLayout.WEST);

        eventNameCombo.setEditable(true);
        eventHandlerPanel.add(eventNameCombo, java.awt.BorderLayout.CENTER);

        add(eventHandlerPanel, java.awt.BorderLayout.SOUTH);

    }//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JTree eventSelectTree;
    private javax.swing.JLabel eventNameLabel;
    private javax.swing.JPanel sourcePanel;
    private javax.swing.JScrollPane eventSelectScroll;
    private javax.swing.JLabel eventSelectLabel;
    private javax.swing.JPanel sourceNamePanel;
    private javax.swing.JPanel sourceInfoPanel;
    private javax.swing.JPanel eventHandlerPanel;
    private javax.swing.JLabel sourceNameLabel;
    private javax.swing.JTextField sourceComponentName;
    private javax.swing.JComboBox eventNameCombo;
    private javax.swing.JPanel eventSelectLabelPanel;
    // End of variables declaration//GEN-END:variables

    // --------
    // Innerclasses

    class EventSetNode implements TreeNode {
        private TreeNode parent;
        private String eventSetName;
        private List subNodes;

        public EventSetNode(TreeNode parent, String eventSetName, List subNodes) {
            this.parent = parent;
            this.eventSetName = eventSetName;
            this.subNodes = subNodes;
        }

        public TreeNode getChildAt(int childIndex) {
            return (TreeNode) subNodes.get(childIndex);
        }
        public int getChildCount() {
            return subNodes.size();
        }
        public TreeNode getParent() {
            return null;
        }
        public int getIndex(TreeNode node) {
            return subNodes.indexOf(node);
        }
        public boolean getAllowsChildren() {
            return true;
        }
        public boolean isLeaf() {
            return false;
        }
        public Enumeration children() {
            return Collections.enumeration(subNodes);
        }
        public String toString() {
            return eventSetName;
        }
    }

    class EventNode implements TreeNode {
        private TreeNode parent;
        private Event event;
        public EventNode(TreeNode parent, Event event) {
            this.parent = parent;
            this.event = event;
        }
        public TreeNode getChildAt(int childIndex) {
            return null;
        }
        public int getChildCount() {
            return 0;
        }
        public TreeNode getParent() {
            return parent;
        }
        public int getIndex(TreeNode node) {
            return -1;
        }
        public boolean getAllowsChildren() {
            return false;
        }
        public boolean isLeaf() {
            return true;
        }
        public Enumeration children() {
            return null;
        }
        public String toString() {
            if (!event.hasEventHandlers())
                return event.getName();
            String[] handlers = event.getEventHandlers();
            if (handlers.length == 1)
                return event.getName() + " ["+ handlers[0] +"]"; // NOI18N
            return event.getName() + " [...]"; // NOI18N
        }
        Event getEvent() {
            return event;
        }
    }
}
