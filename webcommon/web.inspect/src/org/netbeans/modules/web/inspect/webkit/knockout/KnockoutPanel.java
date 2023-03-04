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
package org.netbeans.modules.web.inspect.webkit.knockout;

import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.dnd.DnDConstants;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.List;
import javax.swing.ActionMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.border.Border;
import org.netbeans.modules.web.inspect.PageModel;
import org.netbeans.modules.web.inspect.webkit.WebKitPageModel;
import org.netbeans.modules.web.inspect.webkit.knockout.unused.UnusedBindingsPanel;
import org.netbeans.modules.web.webkit.debugging.api.WebKitDebugging;
import org.netbeans.swing.outline.Outline;
import org.openide.explorer.ExplorerManager;
import org.openide.explorer.ExplorerUtils;
import org.openide.explorer.view.OutlineView;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * A panel for Knockout-related information about the inspected page.
 *
 * @author Jan Stola
 */
public class KnockoutPanel extends JPanel implements ExplorerManager.Provider {
    /** Request processor used by this class. */
    private static final RequestProcessor RP = new RequestProcessor(KnockoutPanel.class);
    /** Explorer manager provided by this panel. */
    private final ExplorerManager manager = new ExplorerManager();
    /** Page model for this panel. */
    private final WebKitPageModel pageModel;
    /** View that displays Knockout context of the selected node. */
    private OutlineView contextView;
    /** Component show on the binding context tab. */
    private JComponent bindingContextComponent;
    /** Page model listener. */
    private Listener pageModelListener;
    /** Determines whether we found Knockout in the current page already. */
    private boolean knockoutFound;
    /** The current selected node. */
    Node selectedNode;
    /** Unused bindings panel. */
    private final UnusedBindingsPanel unusedBindingsPanel;
    /** Determines whether binding context (or unused bindings) are shown. */
    private boolean bindingContextShown = true;

    /**
     * Creates a new {@code KnockoutPanel}.
     */
    @NbBundle.Messages({
        "KnockoutPanel.messageLabel.noInspection=<No Inspected Web Page>"
    })
    public KnockoutPanel(WebKitPageModel pageModel) {
        this.pageModel = pageModel;

        initContextView();
        initComponents();
        initToolBar();
        unusedBindingsPanel = new UnusedBindingsPanel();
        unusedBindingsPanel.setPageModel(pageModel);
        if (pageModel == null) {
            messageLabel.setText(Bundle.KnockoutPanel_messageLabel_noInspection());
            add(messageLabel);
        } else {
            pageModelListener = new Listener();
            pageModel.addPropertyChangeListener(pageModelListener);
            update(true);
        }
    }

    /**
     * Initializes the context view.
     */
    @NbBundle.Messages({
        "KnockoutPanel.contextView.name=Name",
        "KnockoutPanel.contextView.value=Value"
    })
    private void initContextView() {
        contextView = new OutlineView(Bundle.KnockoutPanel_contextView_name());
        contextView.setAllowedDragActions(DnDConstants.ACTION_NONE);
        contextView.setAllowedDropActions(DnDConstants.ACTION_NONE);
        contextView.setShowNodeIcons(false);
        contextView.addPropertyColumn(
                KnockoutNode.ValueProperty.NAME,
                Bundle.KnockoutPanel_contextView_value());

        Outline outline = contextView.getOutline();
        outline.setRootVisible(false);
    }

    /**
     * Initializes the look of the tool-bar.
     */
    private void initToolBar() {
        // copied from org.netbeans.core.multiview.TabsComponent
        Border b = (Border) UIManager.get("Nb.Editor.Toolbar.border"); // NOI18N
        toolBar.setBorder(b);
        toolBar.setFocusable(true);
        String lafId = UIManager.getLookAndFeel().getID();
        if ("Windows".equals(lafId) && !isXPTheme()) { // NOI18N
            toolBar.setRollover(true);
        } else if ("Aqua".equals(lafId)) { // NOI18N
            toolBar.setBackground(UIManager.getColor("NbExplorerView.background")); // NOI18N
        }
        toolBar.setVisible(false); // Unused Bindings no longer works
    }

    /**
     * Determines whether we are displayed using Windows XP theme.
     * 
     * @return {@code true} if we are displayed using Windows XP theme,
     * returns {@code false} otherwise.
     */
    private static boolean isXPTheme() {
        Boolean isXP = (Boolean) Toolkit.getDefaultToolkit().
                getDesktopProperty("win.xpstyle.themeActive"); // NOI18N
        return isXP == null ? false : isXP;
    }

    /**
     * Disposes this panel.
     */
    void dispose() {
        if (pageModelListener != null) {
            pageModel.removePropertyChangeListener(pageModelListener);
            pageModelListener = null;
        }
    }

    /**
     * Returns page model of this panel.
     * 
     * @return page model of this panel.
     */
    WebKitPageModel getPageModel() {
        return pageModel;
    }

    @Override
    public ExplorerManager getExplorerManager() {
        return manager;
    }

    /**
     * Updates the panel (according to the current selection).
     * 
     * @param documentUpdated {@code true} when the document was updated,
     * {@code false} otherwise.
     */
    @NbBundle.Messages({
        "KnockoutPanel.messageLabel.noKnockout=<Knockout Not Found (Yet?)>",
        "KnockoutPanel.messageLabel.noSelection=<No Element Selected>",
        "KnockoutPanel.messageLabel.noSingleSelection=<Multiple Elements Selected>"
    })
    final void update(final boolean documentUpdated) {
        if (!EventQueue.isDispatchThread()) {
            EventQueue.invokeLater(new Runnable() {
                @Override
                public void run() {
                    update(documentUpdated);
                }
            });
            return;
        }
        selectedNode = null;
        if (documentUpdated) {
            knockoutFound = false;
            unusedBindingsPanel.setKnockoutVersion(null);
        }
        JComponent componentToShow;
        if (knockoutFound) {
            List<? extends Node> selection = pageModel.getSelectedNodes();
            if (selection.isEmpty()) {
                messageLabel.setText(Bundle.KnockoutPanel_messageLabel_noSelection());
                bindingContextComponent = messageLabel;
            } else if (selection.size() > 1) {
                messageLabel.setText(Bundle.KnockoutPanel_messageLabel_noSingleSelection());
                bindingContextComponent = messageLabel;
            } else {
                selectedNode = selection.get(0);
                org.netbeans.modules.web.webkit.debugging.api.dom.Node webKitNode =
                    selectedNode.getLookup().lookup(org.netbeans.modules.web.webkit.debugging.api.dom.Node.class);
                WebKitDebugging webKit = pageModel.getWebKit();
                Node rootNode = new AbstractNode(Children.create(new KnockoutChildFactory(webKit, webKitNode), true));
                getExplorerManager().setRootContext(rootNode);
                bindingContextComponent = contextView;
                expandDataNode();
            }
            if (bindingContextButton.isSelected()) {
                showInContentPanel(bindingContextComponent);
            }
            componentToShow = mainPanel;
        } else {
            messageLabel.setText(Bundle.KnockoutPanel_messageLabel_noKnockout());
            componentToShow = messageLabel;
        }
        
        if (componentToShow.getParent() == null) {
            removeAll();
            add(componentToShow);
        }
        revalidate();
        repaint();
    }

    /**
     * Invoked when knockout is found in the page.
     * 
     * @param koVersion version of Knockout used by the page.
     */
    void knockoutUsed(String koVersion) {
        knockoutFound = true;
        unusedBindingsPanel.setKnockoutVersion(koVersion);
        update(false);
    }

    /**
     * Determines whether the inspected page uses Knockout.
     * 
     * @return {@code true} when the inspected page uses knockout,
     * returns {@code false} otherwise.
     */
    boolean isKnockoutUsed() {
        return knockoutFound;
    }

    /**
     * Selects the binding context tab.
     */
    void showKnockoutContext() {
        bindingContextButton.doClick();
    }

    /**
     * Expands the {@code $data} node of the binding context.
     */
    private void expandDataNode() {
        RP.post(new Runnable() {
            @Override
            public void run() {
                for (final Node node : manager.getRootContext().getChildren().getNodes(true)) {
                    if ("$data".equals(node.getName())) { // NOI18N
                        EventQueue.invokeLater(new Runnable() {
                            @Override
                            public void run () {
                                contextView.expandNode(node);
                            }
                        });
                    }
                }
            }
        });
    }

    /**
     * Ensures that the specified component is shown in the content panel.
     * 
     * @param component component to show in the content panel.
     */
    void showInContentPanel(JComponent component) {
        contentPanel.removeAll();
        contentPanel.add(component);
        contentPanel.revalidate();
        contentPanel.repaint();
    }

    /**
     * Creates a lookup provider (that provides the lookup that corresponds
     * to the selected tab).
     * 
     * @param actionMap action map to use in the creation of returned lookups.
     * @return lookup provider.
     */
    Lookup.Provider createLookupProvider(ActionMap actionMap) {
        final Lookup bindingContextLookup = ExplorerUtils.createLookup(manager, actionMap);
        final Lookup unusedBindingLookup = ExplorerUtils.createLookup(unusedBindingsPanel.getExplorerManager(), actionMap);
        return new Lookup.Provider() {
            @Override
            public Lookup getLookup() {
                synchronized (KnockoutPanel.this) {
                    return bindingContextShown ? bindingContextLookup : unusedBindingLookup;
                }
            }
        };
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        messageLabel = new javax.swing.JLabel();
        toolBarButtonGroup = new javax.swing.ButtonGroup();
        mainPanel = new javax.swing.JPanel();
        toolBar = new javax.swing.JToolBar();
        bindingContextButton = new javax.swing.JToggleButton();
        unusedBindingsButton = new javax.swing.JToggleButton();
        contentPanel = new javax.swing.JPanel();

        messageLabel.setBackground(contextView.getViewport().getView().getBackground());
        messageLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        messageLabel.setEnabled(false);
        messageLabel.setOpaque(true);

        mainPanel.setLayout(new java.awt.BorderLayout());

        toolBar.setFloatable(false);
        toolBar.setRollover(true);

        toolBarButtonGroup.add(bindingContextButton);
        bindingContextButton.setSelected(true);
        org.openide.awt.Mnemonics.setLocalizedText(bindingContextButton, org.openide.util.NbBundle.getMessage(KnockoutPanel.class, "KnockoutPanel.bindingContextButton.text")); // NOI18N
        bindingContextButton.setFocusable(false);
        bindingContextButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        bindingContextButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        bindingContextButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                bindingContextButtonActionPerformed(evt);
            }
        });
        toolBar.add(bindingContextButton);

        toolBarButtonGroup.add(unusedBindingsButton);
        org.openide.awt.Mnemonics.setLocalizedText(unusedBindingsButton, org.openide.util.NbBundle.getMessage(KnockoutPanel.class, "KnockoutPanel.unusedBindingsButton.text")); // NOI18N
        unusedBindingsButton.setFocusable(false);
        unusedBindingsButton.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
        unusedBindingsButton.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
        unusedBindingsButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                unusedBindingsButtonActionPerformed(evt);
            }
        });
        toolBar.add(unusedBindingsButton);

        mainPanel.add(toolBar, java.awt.BorderLayout.PAGE_START);

        contentPanel.setLayout(new java.awt.BorderLayout());
        mainPanel.add(contentPanel, java.awt.BorderLayout.CENTER);

        setLayout(new java.awt.BorderLayout());
    }// </editor-fold>//GEN-END:initComponents

    private void bindingContextButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_bindingContextButtonActionPerformed
        synchronized (this) {
            bindingContextShown = true;
        }
        showInContentPanel(bindingContextComponent);
    }//GEN-LAST:event_bindingContextButtonActionPerformed

    private void unusedBindingsButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_unusedBindingsButtonActionPerformed
        synchronized (this) {
            bindingContextShown = false;
        }
        showInContentPanel(unusedBindingsPanel);
    }//GEN-LAST:event_unusedBindingsButtonActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JToggleButton bindingContextButton;
    private javax.swing.JPanel contentPanel;
    private javax.swing.JPanel mainPanel;
    private javax.swing.JLabel messageLabel;
    private javax.swing.JToolBar toolBar;
    private javax.swing.ButtonGroup toolBarButtonGroup;
    private javax.swing.JToggleButton unusedBindingsButton;
    // End of variables declaration//GEN-END:variables

    /**
     * Listener for the changes of the page model.
     */
    final class Listener implements PropertyChangeListener {

        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            String propName = evt.getPropertyName();
            if (PageModel.PROP_SELECTED_NODES.equals(propName)) {
                update(false);
            } else if (PageModel.PROP_DOCUMENT.equals(propName)) {
                update(true);
            }
        }
        
    }

}
