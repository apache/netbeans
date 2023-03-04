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
package org.netbeans.modules.css.visual;

import org.netbeans.modules.css.visual.spi.Location;
import org.netbeans.modules.css.visual.spi.RuleHandle;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.dnd.DnDConstants;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyVetoException;
import java.util.Collections;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import javax.swing.Action;
import javax.swing.JTree;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeCellRenderer;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.css.lib.api.CssParserResult;
import org.netbeans.modules.css.model.api.Model;
import org.netbeans.modules.css.model.api.Rule;
import org.netbeans.modules.css.model.api.StyleSheet;
import org.netbeans.modules.css.visual.api.CssStylesTC;
import org.netbeans.modules.css.visual.api.RuleEditorController;
import org.netbeans.modules.css.visual.api.EditCSSRulesAction;
import org.netbeans.modules.parsing.api.ParserManager;
import org.netbeans.modules.parsing.api.ResultIterator;
import org.netbeans.modules.parsing.api.Source;
import org.netbeans.modules.parsing.api.UserTask;
import org.netbeans.modules.parsing.spi.ParseException;
import org.netbeans.modules.web.common.api.WebUtils;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.BeanTreeView;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.Lookup.Result;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.Lookups;
import org.openide.util.lookup.ProxyLookup;
import org.openide.windows.TopComponent;
import org.openide.windows.WindowManager;

/**
 *
 * @author marekfukala
 */
@NbBundle.Messages({})
public class DocumentViewPanel extends javax.swing.JPanel implements ExplorerManager.Provider {

    static RequestProcessor RP = new RequestProcessor();
    /**
     * Tree view showing the style sheet information.
     */
    private BeanTreeView treeView;
    /**
     * Explorer manager provided by this panel.
     */
    private ExplorerManager manager = new ExplorerManager();
    /**
     * Lookup of this panel.
     */
    private final PanelLookup lookup;
    private final Lookup cssStylesLookup;
    
    /**
     * A strong reference to the Lookup.Result must be kept! 
     * See {@link Result#addLookupListener(org.openide.util.LookupListener)}.
     */
    private final Result<FileObject> lookupFileObjectResult; 
    
    /**
     * Filter for the tree displayed in this panel.
     */
    private Filter filter = new Filter();
    private DocumentViewModel documentModel;
    private DocumentNode documentNode;
    private final EditCSSRulesAction createRuleAction;
    
    private final PropertyChangeListener RULE_EDITOR_CONTROLLER_LISTENER = new PropertyChangeListener() {
        @Override
        public void propertyChange(PropertyChangeEvent pce) {
            if (RuleEditorController.PropertyNames.RULE_SET.name().equals(pce.getPropertyName())) {
                final Rule rule = (Rule) pce.getNewValue();
                if (rule == null) {
                    setSelectedStyleSheet();
                    return ;
                }
                final Model model = rule.getModel();
                model.runReadTask(new Model.ModelTask() {
                    @Override
                    public void run(StyleSheet styleSheet) {
                        setSelectedRule(RuleHandle.createRuleHandle(rule));
                    }
                });
            }
        }
    };

    /**
     * Creates new form DocumentViewPanel
     */
    public DocumentViewPanel(Lookup cssStylesLookup) {
        this.cssStylesLookup = cssStylesLookup;

        createRuleAction = EditCSSRulesAction.getDefault();

        lookupFileObjectResult = cssStylesLookup.lookupResult(FileObject.class);
        lookupFileObjectResult.addLookupListener(new LookupListener() {
            @Override
            public void resultChanged(LookupEvent ev) {
                //current stylesheet changed
                try {
                    contextChanged();
                } catch(Throwable t) {
                    Exceptions.printStackTrace(t);
                }
            }
        });

        //listen on selected rule in the rule editor and set selected rule in the 
        //document view accordingly
        RuleEditorController controller = cssStylesLookup.lookup(RuleEditorController.class);
        controller.addRuleEditorListener(RULE_EDITOR_CONTROLLER_LISTENER);

        Lookup explorerLookup = ExplorerUtils.createLookup(getExplorerManager(), getActionMap());
        lookup = new PanelLookup(explorerLookup);        
        Result<Node> lookupResult = explorerLookup.lookupResult(Node.class);
        lookupResult.addLookupListener(new LookupListener() {
            @Override
            public void resultChanged(LookupEvent ev) {
                //selected node changed
                Node[] selectedNodes = manager.getSelectedNodes();
                Node selected = selectedNodes.length > 0 ? selectedNodes[0] : null;
                boolean empty = (selected == null);
                lookup.emptySelection(empty);
                if (!empty) {
                    RuleHandle ruleHandle = selected.getLookup().lookup(RuleHandle.class);
                    if (ruleHandle != null) {
                        if (!settingRule.get()) {
                            selectRuleInRuleEditor(ruleHandle);
                        }
                        CssStylesListenerSupport.fireRuleSelected(ruleHandle);
                    }
                    Location location = selected.getLookup().lookup(Location.class);
                    if (location != null) {
                        createRuleAction.setContext(location.getFile());
                    }

                }
            }
        });

        initComponents();

        initTreeView();

        //create toolbar
        CustomToolbar toolbar = new CustomToolbar();
        toolbar.addButton(filterToggleButton);
        toolbar.addLineSeparator();
        toolbar.addButton(createRuleToggleButton);

        northPanel.add(toolbar, BorderLayout.EAST);

        //add document listener to the filter text field 
        filterTextField.getDocument().addDocumentListener(new DocumentListener() {
            private void contentChanged() {
                filter.setPattern(filterTextField.getText());
            }

            @Override
            public void insertUpdate(DocumentEvent e) {
                contentChanged();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                contentChanged();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
            }
        });

        setFilterVisible(true, false);
        filterToggleButton.setSelected(true);

        initializeNodes();

        contextChanged();
    }

    public Lookup getLookup() {
        return lookup;
    }
    
    /**
     * Select corresponding node in the document view tree upon change of the
     * rule editor's content.
     *
     * A. The RuleNode holds instances of Rule-s from the model instance which
     * was created as setContext(file) was called on the view panel. B. The
     * 'rule' argument here is from an up-to-date model.
     *
     */
    private void setSelectedRule(RuleHandle handle) {
        try {
            Node foundRuleNode = findLocation(manager.getRootContext(), handle);
            Node[] toSelect = foundRuleNode != null ? new Node[]{foundRuleNode} : new Node[0];
            manager.setSelectedNodes(toSelect);
        } catch (PropertyVetoException ex) {
            //no-op
        }
    }

    private void setSelectedStyleSheet() {
        try {
            Node styleSheetNode = findLocation(manager.getRootContext(), new Location(getContext()));
            //assert styleSheetNode != null;
            Node[] toSelect = styleSheetNode != null ? new Node[]{styleSheetNode} : new Node[0];
            manager.setSelectedNodes(toSelect);
        } catch (PropertyVetoException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    private final AtomicReference<RuleHandle> ruleHandleToSelect = new AtomicReference<>();
    private final AtomicBoolean settingRule = new AtomicBoolean();

    /**
     * Select rule in rule editor upon user action in the document view.
     */
    private void selectRuleInRuleEditor(final RuleHandle handle) {
        ruleHandleToSelect.set(handle);
        RP.post(new Runnable() { 
            @Override
            public void run() {
                final RuleEditorController rec = cssStylesLookup.lookup(RuleEditorController.class);
                final AtomicReference<Rule> matched_rule_ref = new AtomicReference<>();

                FileObject file = handle.getFile();
                Source source = Source.create(file);
                try {
                    ParserManager.parse(Collections.singleton(source), new UserTask() {
                        @Override
                        public void run(ResultIterator resultIterator) throws Exception {
                            ResultIterator ri = WebUtils.getResultIterator(resultIterator, "text/css"); //NOI18N
                            if (ri != null) {
                                final CssParserResult result = (CssParserResult) ri.getParserResult();
                                final Model model = Model.getModel(result);
                                Rule rule = handle.getRule(model);
                                matched_rule_ref.set(rule);
                            }
                        }
                    });
                } catch (ParseException ex) {
                    Exceptions.printStackTrace(ex);
                }

                final Rule match = matched_rule_ref.get();
                if (match != null) {
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            if (ruleHandleToSelect.get() == handle) {
                                settingRule.set(true);
                                rec.setModel(match.getModel());
                                rec.setRule(match);
                                settingRule.set(false);
                            }
                        }
                    });
                }
            }
        });

    }

    @Override
    public final ExplorerManager getExplorerManager() {
        return manager;
    }

    private FileObject getContext() {
        return cssStylesLookup.lookup(FileObject.class);
    }

    /**
     * Called when the CssStylesPanel is activated for different file.
     */
    private void contextChanged() {
        final FileObject context = getContext();
        lookup.update(context);

        //update the action context
        createRuleAction.setContext(context);

        //dispose old model
        if (documentModel != null) {
            documentModel.dispose();
        }

        if (context == null) {
            documentModel = null;
        } else {
            documentModel = new DocumentViewModel(context);
        }

        updateTitle();

        RP.post(new Runnable() {
            @Override
            public void run() {
                documentNode.setModel(documentModel);
                setSelectedStyleSheet();
            }
        });

    }

    /** Determines whether the panel is active. */
    private boolean active;

    /**
     * Invoked when the panel is activated.
     */
    void activated() {
        this.active = true;
        updateTitle();
    }

    /**
     * Invoked when the panel is deactivated.
     */
    void deactivated() {
        this.active = false;
    }

    /**
     * Updates the title of the enclosing view.
     */
    void updateTitle() {
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                if (active) {
                    TopComponent tc = WindowManager.getDefault().findTopComponent("CssStylesTC"); // NOI18N
                    if(tc != null) {
                        FileObject fob = getContext();
                        if(fob != null) {
                            ((CssStylesTC)tc).setTitle(fob.getNameExt());
                        }
                    }
                }
            }
        });
    }
    

    /**
     * Initializes the tree view.
     */
    private void initTreeView() {
        treeView = new BeanTreeView() {
            {
                tree.setCellRenderer(createTreeCellRenderer(tree.getCellRenderer()));
            }

            @Override
            public void expandAll() {
                // The original expandAll() doesn't work for us as it doesn't
                // seem to wait for the calculation of sub-nodes.
                Node root = manager.getRootContext();
                expandAll(root);
                // The view attempts to scroll to the expanded node
                // and it does it with a delay. Hence, simple calls like
                // tree.scrollRowToVisible(0) have no effect (are overriden
                // later) => the dummy collapse and expansion attempts
                // to work around that and keep the root node visible.
                collapseNode(root);
                expandNode(root);
            }

            /**
             * Expands the whole sub-tree under the specified node.
             *
             * @param node root node of the sub-tree that should be expanded.
             */
            private void expandAll(Node node) {
                treeView.expandNode(node);
                for (Node subNode : node.getChildren().getNodes(true)) {
                    if (!subNode.isLeaf()) {
                        expandAll(subNode);
                    }
                }
            }
        };
        treeView.setAllowedDragActions(DnDConstants.ACTION_NONE);
        treeView.setAllowedDropActions(DnDConstants.ACTION_NONE);
        treeView.setRootVisible(false);
        add(treeView, BorderLayout.CENTER);
    }

    private void initializeNodes() {
        documentNode = new DocumentNode(documentModel, filter);
        Node root = new FakeRootNode<>(documentNode,
                new Action[]{});
        manager.setRootContext(root);
        treeView.expandAll();
    }

    /**
     * Finds a node that represents the specified location in a tree represented
     * by the given root node.
     *
     * @param root root of a tree to search.
     * @param rule rule to find.
     * @return node that represents the rule or {@code null}.
     */
    public static Node findLocation(Node root, Location location) {
        Location candidate = root.getLookup().lookup(Location.class);
        if (candidate != null && location.equals(candidate)) {
            return root;
        }
        for (Node node : root.getChildren().getNodes()) {
            Node result = findLocation(node, location);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /**
     * Creates a cell renderer for the tree view.
     *
     * @param delegate delegating/original tree renderer.
     * @return call renderer for the tree view.
     */
    private TreeCellRenderer createTreeCellRenderer(final TreeCellRenderer delegate) {
        return new DefaultTreeCellRenderer() {
            @Override
            public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                return delegate.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
            }
        };
    }

    private void setFilterVisible(boolean visible, boolean requestFocus) {
        northPanel.remove(filterTextField);
        if (visible) {
            //update the UI
            northPanel.add(filterTextField, BorderLayout.CENTER);
            //set the filter text to the node
            filter.setPattern(filterTextField.getText());

            if(requestFocus) {
                filterTextField.requestFocus();
            }
        } else {
            //just remove the filter text from the node, but keep it in the field
            //so next time it is opened it will contain the old value
            filter.setPattern(null);
        }
        northPanel.revalidate();
        northPanel.repaint();
    }

    static class PanelLookup extends ProxyLookup {
        private final Lookup base;
        private Project project;
        private boolean emptySelection = true;

        PanelLookup(Lookup base) {
            this.base = base;
        }
        
        void update(FileObject file) {
            project = (file == null) ? null : FileOwnerQuery.getOwner(file);
            update();
        }

        void emptySelection(boolean emptySelection) {
            if (this.emptySelection != emptySelection) {
                this.emptySelection = emptySelection;
                update();
            }
        }

        private void update() {
            if (emptySelection && (project != null)) {
                setLookups(Lookups.fixed(new AbstractNode(Children.LEAF, Lookups.fixed(project))));
            } else {
                setLookups(base);
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        createRuleToggleButton = new javax.swing.JToggleButton();
        filterToggleButton = new javax.swing.JToggleButton();
        filterTextField = new javax.swing.JTextField();
        northPanel = new javax.swing.JPanel();

        createRuleToggleButton.setAction(createRuleAction);
        createRuleToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/css/visual/resources/newRule.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(createRuleToggleButton, null);
        createRuleToggleButton.setToolTipText(org.openide.util.NbBundle.getMessage(DocumentViewPanel.class, "CreateRuleDialog.title")); // NOI18N
        createRuleToggleButton.setFocusable(false);
        createRuleToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                createRuleToggleButtonActionPerformed(evt);
            }
        });

        filterToggleButton.setIcon(new javax.swing.ImageIcon(getClass().getResource("/org/netbeans/modules/css/visual/resources/find.png"))); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(filterToggleButton, null);
        filterToggleButton.setToolTipText(org.openide.util.NbBundle.getMessage(DocumentViewPanel.class, "DocumentViewPanel.filterToggleButton.toolTipText")); // NOI18N
        filterToggleButton.setFocusable(false);
        filterToggleButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                filterToggleButtonActionPerformed(evt);
            }
        });

        filterTextField.setText(org.openide.util.NbBundle.getMessage(DocumentViewPanel.class, "DocumentViewPanel.filterTextField.text")); // NOI18N
        filterTextField.setToolTipText(org.openide.util.NbBundle.getMessage(DocumentViewPanel.class, "DocumentViewPanel.filterToggleButton.toolTipText")); // NOI18N

        setLayout(new java.awt.BorderLayout());

        northPanel.setLayout(new java.awt.BorderLayout());
        add(northPanel, java.awt.BorderLayout.NORTH);
    }// </editor-fold>//GEN-END:initComponents

    private void filterToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_filterToggleButtonActionPerformed
        setFilterVisible(filterToggleButton.isSelected(), true);
    }//GEN-LAST:event_filterToggleButtonActionPerformed

    private void createRuleToggleButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_createRuleToggleButtonActionPerformed
        createRuleToggleButton.setSelected(false); //disable selected as it's a toggle button
    }//GEN-LAST:event_createRuleToggleButtonActionPerformed
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton createRuleToggleButton;
    private javax.swing.JTextField filterTextField;
    private javax.swing.JToggleButton filterToggleButton;
    private javax.swing.JPanel northPanel;
    // End of variables declaration//GEN-END:variables
}
