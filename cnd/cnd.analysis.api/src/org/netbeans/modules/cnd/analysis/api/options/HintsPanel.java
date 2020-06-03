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

package org.netbeans.modules.cnd.analysis.api.options;

import org.netbeans.modules.cnd.analysis.api.AbstractHintsPanel;
import org.netbeans.modules.cnd.api.model.syntaxerr.AuditPreferences;
import org.netbeans.modules.cnd.api.model.syntaxerr.CodeAudit;
import org.netbeans.modules.cnd.api.model.syntaxerr.CodeAuditProvider;
import org.netbeans.modules.cnd.analysis.api.CodeAuditProviderImpl;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Observable;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.prefs.AbstractPreferences;
import java.util.prefs.BackingStoreException;
import java.util.prefs.PreferenceChangeEvent;
import java.util.prefs.PreferenceChangeListener;
import java.util.prefs.Preferences;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JTextPane;
import javax.swing.JTree;
import javax.swing.SwingUtilities;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;
import org.netbeans.modules.cnd.analysis.api.AbstractCustomizerProvider;
import org.netbeans.modules.cnd.api.model.syntaxerr.AbstractCodeAudit;
import org.netbeans.modules.cnd.api.model.syntaxerr.CsmErrorProvider;
import org.netbeans.modules.cnd.utils.CndUtils;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.NamedOption;
import org.netbeans.modules.options.editor.spi.OptionsFilter;
import org.netbeans.spi.editor.hints.Severity;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;


public class HintsPanel extends AbstractHintsPanel implements TreeCellRenderer  {
    
    private final DefaultTreeCellRenderer dr = new DefaultTreeCellRenderer();
    private final JCheckBox renderer = new JCheckBox();
    private HintsPanelLogic logic;
    private final ExtendedModel model;
    private final OptionsFilter filter;
    
    private final static RequestProcessor WORKER = new RequestProcessor(HintsPanel.class.getName(), 1, false, false);
    private final RequestProcessor.Task expandTask = WORKER.create(new Runnable() {
        @Override public void run() {
            SwingUtilities.invokeLater(new Runnable() {
                @Override public void run() {
                    JTree tree = HintsPanel.this.errorTree;
                    for (int r = 0; r < tree.getRowCount(); r++) {
                        tree.expandRow(r);
                    }
                }
            });
            
        }
    });

    public HintsPanel(Lookup masterLookup, CodeAuditProvider selection, String mimeType) {
        assert mimeType != null;
        initComponents();
        descriptionTextArea.setContentType("text/html"); // NOI18N
        descriptionTextArea.putClientProperty(JTextPane.HONOR_DISPLAY_PROPERTIES, Boolean.TRUE);
        errorTree.setCellRenderer( this );
        errorTree.setRootVisible( false );
        errorTree.setShowsRootHandles( true );
        errorTree.getSelectionModel().setSelectionMode( TreeSelectionModel.SINGLE_TREE_SELECTION );
        model = new ExtendedModel(selection, mimeType);
        OptionsFilter filter = null;
        if (masterLookup != null) {
            filter = masterLookup.lookup(OptionsFilter.class);
        }
        this.filter = filter;
        installFilter();
        logic = new HintsPanelLogic();
        logic.connect(errorTree, model, severityLabel, severityComboBox,
                customizerPanel, descriptionTextArea);
    }
    
    private void installFilter() {
        if (filter != null) {
            ((OptionsFilter) filter).installFilteringModel(errorTree, model, new AcceptorImpl());
        } else {
            errorTree.setModel(model);
        }
    }
    
    void selectPath(String path) {
        TreePath treePath = null;
        for(DefaultMutableTreeNode node : model.audits) {
            Object provider = node.getUserObject();
            if (provider instanceof CodeAuditProviderProxy) {
                String providerID = ((CodeAuditProviderProxy)provider).getName();
                if (path.startsWith(providerID)) {
                    treePath = new TreePath(new Object[]{model.getRoot(),node});
                    path = path.substring(providerID.length());
                    if (path.length() > 1) {
                        path = path.substring(1);
                        Enumeration children = node.children();
                        while(children.hasMoreElements()) {
                            Object sub = children.nextElement();
                            if (sub instanceof DefaultMutableTreeNode) {
                                Object audit = ((DefaultMutableTreeNode)sub).getUserObject();
                                if (audit instanceof CodeAuditProxy) {
                                    String name = ((CodeAuditProxy)audit).getID();
                                    if (path.startsWith(name)) {
                                        treePath = new TreePath(new Object[]{model.getRoot(),node, sub});
                                        break;
                                    }
                                }
                            }
                        }
                    }
                    break;
                }
            }
        }
        if (treePath != null) {
            errorTree.setSelectionPath(treePath);
            errorTree.scrollPathToVisible(treePath);
            errorTree.requestFocusInWindow();
        }
    }
    
    @Override
    public void setSettings(Preferences settings) {
        //TODO set settings
        CndUtils.assertTrueInConsole(false, "Method is not implemented");
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jSplitPane1 = new javax.swing.JSplitPane();
        treePanel = new javax.swing.JPanel();
        jScrollPane1 = new javax.swing.JScrollPane();
        errorTree = new javax.swing.JTree();
        detailsPanel = new javax.swing.JPanel();
        optionsPanel = new javax.swing.JPanel();
        severityLabel = new javax.swing.JLabel();
        severityComboBox = new javax.swing.JComboBox();
        customizerPanel = new javax.swing.JPanel();
        descriptionPanel = new javax.swing.JPanel();
        jScrollPane2 = new javax.swing.JScrollPane();
        descriptionTextArea = new javax.swing.JEditorPane();
        descriptionLabel = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(8, 8, 8, 8));
        setPreferredSize(new java.awt.Dimension(400, 400));
        setLayout(new java.awt.BorderLayout());

        jSplitPane1.setBorder(null);
        jSplitPane1.setDividerLocation(200);
        jSplitPane1.setPreferredSize(new java.awt.Dimension(400, 400));

        treePanel.setOpaque(false);
        treePanel.setPreferredSize(new java.awt.Dimension(200, 400));
        treePanel.setLayout(new java.awt.BorderLayout());

        jScrollPane1.setPreferredSize(new java.awt.Dimension(100, 200));
        jScrollPane1.setViewportView(errorTree);
        errorTree.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(HintsPanel.class, "HintsPanel.errorTree.AccessibleContext.accessibleName")); // NOI18N
        errorTree.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HintsPanel.class, "HintsPanel.errorTree.AccessibleContext.accessibleDescription")); // NOI18N

        treePanel.add(jScrollPane1, java.awt.BorderLayout.CENTER);

        jSplitPane1.setLeftComponent(treePanel);

        detailsPanel.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 6, 0, 0));
        detailsPanel.setOpaque(false);
        detailsPanel.setPreferredSize(new java.awt.Dimension(200, 400));
        detailsPanel.setLayout(new java.awt.BorderLayout());

        optionsPanel.setOpaque(false);
        optionsPanel.setPreferredSize(new java.awt.Dimension(200, 200));

        severityLabel.setLabelFor(severityComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(severityLabel, org.openide.util.NbBundle.getMessage(HintsPanel.class, "CTL_ShowAs_Label")); // NOI18N

        customizerPanel.setOpaque(false);
        customizerPanel.setLayout(new java.awt.BorderLayout());

        javax.swing.GroupLayout optionsPanelLayout = new javax.swing.GroupLayout(optionsPanel);
        optionsPanel.setLayout(optionsPanelLayout);
        optionsPanelLayout.setHorizontalGroup(
            optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(optionsPanelLayout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(customizerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                    .addGroup(optionsPanelLayout.createSequentialGroup()
                        .addComponent(severityLabel)
                        .addGap(18, 18, 18)
                        .addComponent(severityComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, 249, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(0, 91, Short.MAX_VALUE)))
                .addContainerGap())
        );
        optionsPanelLayout.setVerticalGroup(
            optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(optionsPanelLayout.createSequentialGroup()
                .addGap(4, 4, 4)
                .addGroup(optionsPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(severityLabel)
                    .addComponent(severityComboBox, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addGap(8, 8, 8)
                .addComponent(customizerPanel, javax.swing.GroupLayout.DEFAULT_SIZE, 152, Short.MAX_VALUE)
                .addContainerGap())
        );

        severityLabel.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HintsPanel.class, "HintsPanel.severityLabel.AccessibleContext.accessibleDescription")); // NOI18N
        severityComboBox.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(HintsPanel.class, "AN_Show_As_Combo")); // NOI18N
        severityComboBox.getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HintsPanel.class, "AD_Show_As_Combo")); // NOI18N

        detailsPanel.add(optionsPanel, java.awt.BorderLayout.CENTER);

        descriptionPanel.setOpaque(false);
        descriptionPanel.setPreferredSize(new java.awt.Dimension(200, 200));

        jScrollPane2.setHorizontalScrollBarPolicy(javax.swing.ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);

        descriptionTextArea.setEditable(false);
        descriptionTextArea.setPreferredSize(new java.awt.Dimension(100, 50));
        jScrollPane2.setViewportView(descriptionTextArea);

        descriptionLabel.setLabelFor(descriptionTextArea);
        org.openide.awt.Mnemonics.setLocalizedText(descriptionLabel, org.openide.util.NbBundle.getMessage(HintsPanel.class, "CTL_Description_Border")); // NOI18N

        javax.swing.GroupLayout descriptionPanelLayout = new javax.swing.GroupLayout(descriptionPanel);
        descriptionPanel.setLayout(descriptionPanelLayout);
        descriptionPanelLayout.setHorizontalGroup(
            descriptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(descriptionPanelLayout.createSequentialGroup()
                .addComponent(descriptionLabel)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 428, Short.MAX_VALUE)
        );
        descriptionPanelLayout.setVerticalGroup(
            descriptionPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(descriptionPanelLayout.createSequentialGroup()
                .addComponent(descriptionLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 177, Short.MAX_VALUE))
        );

        jScrollPane2.getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(HintsPanel.class, "HintsPanel.jScrollPane2.AccessibleContext.accessibleName")); // NOI18N

        detailsPanel.add(descriptionPanel, java.awt.BorderLayout.SOUTH);

        jSplitPane1.setRightComponent(detailsPanel);

        add(jSplitPane1, java.awt.BorderLayout.CENTER);

        getAccessibleContext().setAccessibleName(org.openide.util.NbBundle.getMessage(HintsPanel.class, "HintsPanel.AccessibleContext.accessibleName")); // NOI18N
        getAccessibleContext().setAccessibleDescription(org.openide.util.NbBundle.getMessage(HintsPanel.class, "HintsPanel.AccessibleContext.accessibleDescription")); // NOI18N
    }// </editor-fold>//GEN-END:initComponents
       
    synchronized final void update() {       
        if ( logic != null ) {
            logic.disconnect();
        }
        logic = new HintsPanelLogic();
        logic.connect(errorTree, model, severityLabel, severityComboBox,
                customizerPanel, descriptionTextArea);
    }
    
    void cancel() {
        logic.cancel();
        logic.disconnect();
        logic = null;
    }
    
    boolean isChanged() {
        return logic != null ? logic.isChanged() : false;
    }
    
    void applyChanges() {
        logic.applyChanges();
        logic.disconnect();
        logic = null;
    }
           
    @Override
    public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
        
        renderer.setBackground( selected ? dr.getBackgroundSelectionColor() : dr.getBackgroundNonSelectionColor() );
        renderer.setForeground( selected ? dr.getTextSelectionColor() : dr.getTextNonSelectionColor() );
        renderer.setEnabled( true );
        if (value instanceof DefaultMutableTreeNode) {
            Object data = ((DefaultMutableTreeNode)value).getUserObject();
            if ( data instanceof CodeAuditProxy ) {
                CodeAuditProxy audit = (CodeAuditProxy)data;
                renderer.setText(audit.getName());
                renderer.setSelected(audit.isEnabled());
            } else if (data instanceof CodeAuditProviderProxy) {
                CodeAuditProviderProxy provider = (CodeAuditProviderProxy)data;
                renderer.setText( provider.getDisplayName());
                boolean hasEnabled = false;
                boolean hasDisabled = false;
                for(int i = 0; i < ((DefaultMutableTreeNode)value).getChildCount(); i++) {
                    DefaultMutableTreeNode childAt = (DefaultMutableTreeNode) ((DefaultMutableTreeNode)value).getChildAt(i);
                    CodeAuditProxy audit = (CodeAuditProxy) childAt.getUserObject();
                    if (audit.isEnabled()) {
                        hasEnabled = true;
                    } else {
                        hasDisabled = true;
                    }
                }
                if (hasEnabled) {
                    if (hasDisabled) {
                        //TODO make partly selected state
                        renderer.setSelected(true);
                    } else {
                        renderer.setSelected(true);
                    }
                } else {
                    renderer.setSelected(false);
                }
            } else if (data instanceof NamedOptionProxy) {
                NamedOptionProxy option = (NamedOptionProxy)data;
                renderer.setText( option.getDisplayName());
                renderer.setSelected(option.getBoolean());
            }
        }

        return renderer;
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
    private javax.swing.JPanel treePanel;
    // End of variables declaration//GEN-END:variables
    
    private static class TreeStorage {

        private final Map<HintModelController, List<HintModelController>> storage;

        public TreeStorage() {
            this.storage = new ConcurrentHashMap<HintModelController, List<HintModelController>>();
        }

        public void putRoot(HintModelController root) {
            storage.put(root, new CopyOnWriteArrayList<HintModelController>());
        }

        public void removeRoot(HintModelController root) {
            storage.remove(root);
        }
        
        public void clearRoot(HintModelController root) {
            storage.get(root).clear();
        }

        public void addNode(HintModelController parent, HintModelController node) {
            List<HintModelController> children = storage.get(parent);
            if (children != null) {
                children.add(node);
            }
        }

        public void removeNode(HintModelController parent, HintModelController node) {
            List<HintModelController> children = storage.get(parent);
            if (children != null) {
                children.remove(node);
            }
        }

        public boolean isChanged() {
            for (Map.Entry<HintModelController, List<HintModelController>> entry : storage.entrySet()) {
                if (entry.getKey().isChanged()) {
                    return true;
                }
                for (HintModelController child : entry.getValue()) {
                    if (child.isChanged()) {
                        return true;
                    }
                }
            }
            return false;
        }

        public void store() {
            for (Map.Entry<HintModelController, List<HintModelController>> entry : storage.entrySet()) {
                if (entry.getKey().isChanged()) {
                    entry.getKey().store();
                }
                for (HintModelController child : entry.getValue()) {
                    if (child.isChanged()) {
                        child.store();
                    }
                }
            }
        }

        public void cancel() {
            for (Map.Entry<HintModelController, List<HintModelController>> entry : storage.entrySet()) {
                if (entry.getKey().isChanged()) {
                    entry.getKey().cancel();
                }
                for (HintModelController child : entry.getValue()) {
                    if (child.isChanged()) {
                        child.cancel();
                    }
                }
            }
        }
    }

    final class ExtendedModel implements TreeModel {
        private final List<DefaultMutableTreeNode> audits = new ArrayList<DefaultMutableTreeNode>();
        private final TreeStorage storage =  new TreeStorage();
        private final String mimeType;
        private final CodeAuditProvider selection;
        private final List<TreeModelListener> listeners;
        
        private ExtendedModel(CodeAuditProvider selection, String mimeType) {
            this.mimeType = mimeType;
            this.selection = selection;
            this.listeners = new ArrayList<TreeModelListener>();
            if (selection == null) {
                for (CodeAuditProvider provider : CodeAuditProviderImpl.getDefault()) {
                    if (mimeType.equals(provider.getMimeType())) {
                        final CodeAuditProviderProxy proxy = new CodeAuditProviderProxy(provider, true);
                        final DefaultMutableTreeNode providerRoot = new DefaultMutableTreeNode(proxy);
                        audits.add(providerRoot);
                        storage.putRoot(proxy);
                        for (CodeAudit audit : proxy.getAudits()) {
                            providerRoot.add(new DefaultMutableTreeNode(audit));
                            storage.addNode(proxy, (HintModelController) audit);
                        }
                        proxy.addAuditChangedListener(new ActionListenerImpl(providerRoot, proxy));
                    }
                }
                if (MIMENames.SOURCES_MIME_TYPE.equals(mimeType)) {
                    for (NamedOption option : Lookups.forPath(NamedOption.HINTS_CATEGORY).lookupAll(NamedOption.class)) {
                        if (option.isVisible()) {
                            NamedOptionProxy proxy = new NamedOptionProxy(option, true);
                            audits.add(new DefaultMutableTreeNode(proxy));
                            storage.putRoot(proxy);
                        }
                    }
                }
            } else {
                final CodeAuditProviderProxy proxy = new CodeAuditProviderProxy(selection, false);
                final DefaultMutableTreeNode providerRoot = new DefaultMutableTreeNode(proxy);
                audits.add(providerRoot);
                storage.putRoot(proxy);
                for (CodeAudit audit : proxy.getAudits()) {
                    providerRoot.add(new DefaultMutableTreeNode(audit));
                    storage.addNode(proxy, (HintModelController) audit);
                }
                proxy.addAuditChangedListener(new ActionListenerImpl(providerRoot, proxy));
            }
        }

        boolean isChanged() {
            return storage.isChanged();
        }

        void store() {
            storage.store();
        }

        void cancel() {
            storage.cancel();
        }

        @Override
        public Object getRoot() {
            return "Root"; //NOI18N
        }

        @Override
        public Object getChild(Object parent, int index) {
            if (parent == getRoot()) {
                return audits.get(index);
            } else if (parent instanceof DefaultMutableTreeNode) {
                return ((DefaultMutableTreeNode)parent).getChildAt(index);
            }
            return null;
        }

        @Override
        public int getChildCount(Object parent) {
            if (parent == getRoot()) {
                return audits.size();
            } else if (parent instanceof DefaultMutableTreeNode) {
                return ((DefaultMutableTreeNode)parent).getChildCount();
            }
            return 0;
        }

        @Override
        public boolean isLeaf(Object node) {
            if (node == getRoot()) {
                return false;
            } else if (node instanceof DefaultMutableTreeNode) {
                return ((DefaultMutableTreeNode)node).isLeaf();
            }
            return true;
        }

        @Override
        public void valueForPathChanged(TreePath path, Object newValue) {
        }

        @Override
        public int getIndexOfChild(Object parent, Object child) {
            if (parent == getRoot()) {
                return audits.indexOf(child);
            } else if (parent instanceof DefaultMutableTreeNode) {
                return ((DefaultMutableTreeNode)parent).getIndex((DefaultMutableTreeNode)child);
            }
            return 0;
        }

        @Override
        public void addTreeModelListener(TreeModelListener l) {
            listeners.add(l);
        }

        @Override
        public void removeTreeModelListener(TreeModelListener l) {
            listeners.remove(l);
        }

        private void fireTreeNodesChanged(final TreeNode root) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    installFilter();
                    for (TreeModelListener listener : listeners) {
                        listener.treeStructureChanged(new TreeModelEvent(this, new TreePath(root)));
                    }
                }
            });
        }

        public void nodeChanged(TreeNode node) {
//            if (node instanceof DefaultMutableTreeNode) {
//                Object data = ((DefaultMutableTreeNode)node).getUserObject();
//                if ( data instanceof CodeAuditProxy ) {
//                    CodeAuditProxy rule = (CodeAuditProxy)data;
//                } else if (data instanceof CodeAuditProvider) {
//                    CodeAuditProvider provider = (CodeAuditProvider)data;
//                } else if (data instanceof NamedOption) {
//                    NamedOption option = (NamedOption)data;
//                }
//            }
        }

        private class ActionListenerImpl implements ActionListener {

            private final DefaultMutableTreeNode providerRoot;
            private final CodeAuditProviderProxy proxy;

            public ActionListenerImpl(DefaultMutableTreeNode providerRoot, CodeAuditProviderProxy proxy) {
                this.providerRoot = providerRoot;
                this.proxy = proxy;
            }

            @Override
            public void actionPerformed(ActionEvent e) {
                providerRoot.removeAllChildren();
                storage.clearRoot(proxy);
                for (CodeAudit audit : proxy.getAudits()) {
                    providerRoot.add(new DefaultMutableTreeNode(audit));
                    storage.addNode(proxy, (HintModelController) audit);
                }
                fireTreeNodesChanged(providerRoot);
            }
        }
    }

    private final class AcceptorImpl implements OptionsFilter.Acceptor {

        @Override
        public boolean accept(Object originalTreeNode, String filterText) {
            if (filterText.isEmpty()) {
                return true;
            }
            expandTask.schedule(100);
            if (!(originalTreeNode instanceof DefaultMutableTreeNode)) {
                return true;
            }
            DefaultMutableTreeNode n = (DefaultMutableTreeNode) originalTreeNode;
            Object uo = n.getUserObject();

            if (!(uo instanceof CodeAuditProxy)) {
                return false;
            }
            CodeAuditProxy audit = (CodeAuditProxy) uo;
            return audit.getName().toLowerCase().contains(filterText.toLowerCase());
        }
    }
    
    interface HintModelController {
        boolean isChanged();
        void store();
        void cancel();
        JComponent createComponent();
    }
    
    static final class CodeAuditProviderProxy implements CodeAuditProvider, HintModelController {

        private final CodeAuditProvider proxy;
        private final List<CodeAudit> audits = new ArrayList<CodeAudit>();
        private final AuditPreferences modifiedAuditPref;
        private final ModifiedPreferences modifiedPref;
        private List<ActionListener> listeners;

        private CodeAuditProviderProxy(final CodeAuditProvider proxy, final boolean supportChanges) {
            this.proxy = proxy;
            this.listeners = new ArrayList<ActionListener>();

            modifiedPref = new ModifiedPreferences(proxy.getPreferences().getPreferences(), supportChanges);
            modifiedAuditPref = new AuditPreferences(modifiedPref);

            for (CodeAudit a : proxy.getAudits()) {
                audits.add(CodeAuditProxy.create(a, supportChanges));
            }

            proxy.getPreferences().getPreferences().addPreferenceChangeListener(new PreferenceChangeListener() {
                @Override
                public void preferenceChange(PreferenceChangeEvent evt) {
                    Collection<CodeAudit> current = proxy.getAudits();
                    if (audits.size() == current.size()) {
                        return;
                    }
                    audits.clear();
                    for (CodeAudit a : current) {
                        audits.add(CodeAuditProxy.create(a, supportChanges));
                    }
                    Collections.sort(audits, new AuditIdComparator());
                    for (ActionListener listener : listeners) {
                        listener.actionPerformed(null);
                    }
                }

                class AuditIdComparator implements Comparator<CodeAudit> {

                    @Override
                    public int compare(CodeAudit o1, CodeAudit o2) {
                        return o1.getID().compareTo(o2.getID());
                    }
                }
            });
        }

        public void addAuditChangedListener(ActionListener listener) {
            listeners.add(listener);
        }

        public void removeAuditChangedListener(ActionListener listener) {
            listeners.remove(listener);
        }

        @Override
        public JComponent createComponent() {
            if (proxy instanceof AbstractCustomizerProvider) {
                return ((AbstractCustomizerProvider) proxy).createComponent(modifiedPref);
            }
            return null;
        }

        @Override
        public Collection<CodeAudit> getAudits() {
            return audits;
        }

        @Override
        public String getName() {
            return proxy.getName();
        }

        @Override
        public String getDisplayName() {
            return proxy.getDisplayName();
        }

        @Override
        public String getDescription() {
            return proxy.getDescription();
        }

        @Override
        public AuditPreferences getPreferences() {
            return modifiedAuditPref;
        }

        @Override
        public boolean isChanged() {
            return modifiedPref.isModified();
        }

        @Override
        public void store() {
            modifiedPref.store();
        }

        @Override
        public void cancel() {
            modifiedPref.cancel();
        }

        @Override
        public String getMimeType() {
            return proxy.getMimeType();
        }
    }
    
   static final class CodeAuditProxy extends AbstractCodeAudit implements HintModelController {
        private final CodeAudit proxy;
        private final AuditPreferences modifiedAuditPref;
        private final ModifiedPreferences modifiedPref;
        
        private static CodeAuditProxy create(CodeAudit proxy, boolean supportChanges) {
            ModifiedPreferences modifiedPref = new ModifiedPreferences(proxy.getPreferences().getPreferences(), supportChanges);
            AuditPreferences modifiedAuditPref = new AuditPreferences(modifiedPref);            
            return new CodeAuditProxy(proxy, supportChanges, modifiedPref, modifiedAuditPref);
        }
        
        private CodeAuditProxy(CodeAudit proxy, boolean supportChanges, ModifiedPreferences modifiedPref, AuditPreferences modifiedAuditPref) {
            super(proxy.getID(), proxy.getName(), proxy.getDescription(), proxy.getDefaultSeverity(), proxy.getDefaultEnabled(), modifiedAuditPref);
            this.proxy = proxy;
            this.modifiedPref = modifiedPref;
            this.modifiedAuditPref = modifiedAuditPref;
        }

        @Override
        public JComponent createComponent() {
            if (proxy instanceof AbstractCustomizerProvider) {
                return ((AbstractCustomizerProvider)proxy).createComponent(modifiedPref);
            }
            return null;
        }

        @Override
        public String getKind() {
            return proxy.getKind();
        }

        @Override
        public AuditPreferences getPreferences() {
            return modifiedAuditPref;
        }
        
        void setSeverity(Severity value) {
            String old = minimalSeverity();
            String defValue = getDefaultSeverity();
            switch (value) {
                case ERROR:
                    if (!"error".equals(old)) { //NOI18N
                        putString("severity", "error", defValue); //NOI18N
                    }
                    break;
                case HINT:
                    if (!"hint".equals(old)) { //NOI18N
                        putString("severity", "hint", defValue); //NOI18N
                    }
                    break;
                case WARNING:
                    if (!"warning".equals(old)) { //NOI18N
                        putString("severity", "warning", defValue); //NOI18N
                    }
                    break;
            }
        }

        void setEnabled(boolean enabled) {
            boolean old = isEnabled();
            final String defValue = getDefaultEnabled() ? "true" : "false"; //NOI18N
            if (enabled) {
                if (!old) {
                    putString("enabled", "true", defValue); //NOI18N
                }
            } else {
                if (old) {
                    putString("enabled", "false", defValue); //NOI18N
                }
            }
        }
        
        private void putString(String key, String value, String defValue) {
            getPreferences().put(getID(), key, value, defValue);
        }

        @Override
        public boolean isChanged() {
            return modifiedPref.isModified();
        }

        @Override
        public void store() {
            modifiedPref.store();
        }

        @Override
        public void cancel() {
            modifiedPref.cancel();
        }

        @Override
        public boolean isSupportedEvent(CsmErrorProvider.EditorEvent kind) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void doGetErrors(CsmErrorProvider.Request request, CsmErrorProvider.Response response) {
            throw new UnsupportedOperationException();
        }
    }
    
    static final class NamedOptionProxy extends NamedOption implements HintModelController {
        private final NamedOption proxy;
        private final ModifiedPreferences modifiedPref;
        
        private NamedOptionProxy(NamedOption proxy, boolean supportChanges) {
            this.proxy = proxy;
            // this is hack:
            modifiedPref = new ModifiedPreferences(NbPreferences.forModule(NamedOption.class), supportChanges);
        }

        @Override
        public JComponent createComponent() {
            if (proxy instanceof AbstractCustomizerProvider) {
                return ((AbstractCustomizerProvider)proxy).createComponent(modifiedPref);
            }
            return null;
        }

        @Override
        public String getName() {
            return proxy.getName();
        }

        @Override
        public String getDisplayName() {
            return proxy.getDisplayName();
        }

        @Override
        public String getDescription() {
            return proxy.getDescription();
        }

        @Override
        public OptionKind getKind() {
            return proxy.getKind();
        }

        @Override
        public Object getDefaultValue() {
            return proxy.getDefaultValue();
        }

        public boolean getBoolean() {
            return modifiedPref.getBoolean(getName(), (Boolean)getDefaultValue());
        }
        
        public void setBoolean(boolean value) {
            boolean old = getBoolean();
            if (old != value) {
                modifiedPref.putBoolean(getName(), value);
            }
        }

        @Override
        public boolean isChanged() {
            return modifiedPref.isModified();
        }

        @Override
        public void store() {
            modifiedPref.store();
        }

        @Override
        public void cancel() {
            modifiedPref.cancel();
        }
    }
    
    static final class ModifiedPreferences extends AbstractPreferences {
        private Map<String,String> map = new HashMap<String, String>();
        private final Map<String, String> modifiedKeys = new HashMap<String, String>();
        private final boolean supportChanges;
        private final Preferences proxyNode;
        private volatile boolean modified;
        public ModifiedPreferences( Preferences node, boolean supportChanges) {
            super(null, ""); // NOI18N
            this.supportChanges = supportChanges;
            proxyNode = node;
            init(node);                
        }

        private void init(Preferences node) {
            map.clear();
            try {
                for (String key : node.keys()) {
                    map.put(key, node.get(key, null));    
                }
            }
            catch (BackingStoreException ex) {
                Exceptions.printStackTrace(ex);
            }
            modifiedKeys.clear();
            modified = false;
        }

        @Override
        public String absolutePath() {
            return proxyNode.absolutePath();
        }

        public boolean isModified() {
            return modified;
        }
        
        void store() {
            for (Map.Entry<String, String> entry : modifiedKeys.entrySet()) {
                String key = entry.getKey();
                String origVal = entry.getValue();
                String curVal = map.get(key);
                if (curVal == null) {
                    proxyNode.remove(key);
                } else if (!curVal.equals(origVal)) {                    
                    proxyNode.put(key, curVal);
                }
            }
            modifiedKeys.clear();
            modified = false;
        }
        
        void cancel() {
            init(proxyNode);
        }

        @Override
        protected void putSpi(String key, String value) {
            if (!modifiedKeys.containsKey(key)) {
                modifiedKeys.put(key, map.get(key));
            }
            map.put(key, value);            
            if (!supportChanges) {
                store();
                init(proxyNode);
            } else {
                calculateModified();
            }
        }

        @Override
        protected String getSpi(String key) {
            return (String)map.get(key);                    
        }

        @Override
        protected void removeSpi(String key) {
            if (!modifiedKeys.containsKey(key)) {
                modifiedKeys.put(key, map.get(key));
            }
            map.remove(key);
            if (!supportChanges) {
                store();
                init(proxyNode);
            } else {
                calculateModified();
            }
        }

        private void calculateModified() {
            modified = false;
            for (Map.Entry<String, String> entry : modifiedKeys.entrySet()) {
                final String key = entry.getKey();
                final String origVal = entry.getValue();
                final String curVal = map.get(key);
                if (origVal != null) {
                    modified = !origVal.equals(curVal);
                } else {
                    modified = curVal != null;
                }
                if (modified) {
                    return;
                }
            }
        }
        
        @Override
        protected void removeNodeSpi() throws BackingStoreException {
            throw new UnsupportedOperationException();
        }

        @Override
        protected String[] keysSpi() throws BackingStoreException {
            String array[] = new String[map.keySet().size()];
            return map.keySet().toArray( array );
        }

        @Override
        protected String[] childrenNamesSpi() throws BackingStoreException {
            throw new UnsupportedOperationException();
        }

        @Override
        protected AbstractPreferences childSpi(String name) {
            throw new UnsupportedOperationException();
        }

        @Override
        protected void syncSpi() throws BackingStoreException {
            throw new UnsupportedOperationException();
        }

        @Override
        protected void flushSpi() throws BackingStoreException {
            throw new UnsupportedOperationException();
        }
    }
}

