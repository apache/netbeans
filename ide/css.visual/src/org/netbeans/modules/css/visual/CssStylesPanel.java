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

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JSplitPane;
import javax.swing.JToggleButton;
import javax.swing.JToolBar;
import javax.swing.UIManager;
import javax.swing.border.Border;
import org.netbeans.modules.css.visual.api.RuleEditorController;
import org.netbeans.modules.css.visual.spi.CssStylesPanelProvider;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;
import org.openide.util.lookup.ProxyLookup;

/**
 *
 * @author marekfukala
 */
public class CssStylesPanel extends javax.swing.JPanel {

    private static final RequestProcessor RP = new RequestProcessor(CssStylesPanel.class);

    static final boolean AQUA = "Aqua".equals(UIManager.getLookAndFeel().getID()); //NOI18N 

    private final RuleEditorController controller;
    private final Collection<CssStylesPanelProvider> providers;
    private final ActionListener toolbarListener;

    /* Lookup for CssStylesPanelProviders. The content mutates based on changed file context.*/
    private final ModifiableLookup providersLookup;

    /* Lookup for the CssStylesTC. Content got from the CssStylesPanelProvider's lookup */
    private final ModifiableLookup tcLookup;

    private final JToolBar toolBar;

    private CssStylesPanelProvider active;
    private JComponent activePanel;

    /**
     * Remember last selected tab per mimetype.
     *
     * Note: this is not exactly correct as the panel's activity is no more
     * driven purely by the file context mimetype. See {@link CssStylesPanelProvider#providesContentFor(org.openide.filesystems.FileObject)
     * }.
     */
    private Map<String, CssStylesPanelProvider> selectedTabs = new HashMap<String, CssStylesPanelProvider>();

    /**
     * Creates new form CssStylesPanel
     */
    public CssStylesPanel() {
        initComponents();

        tcLookup = new ModifiableLookup();
        providersLookup = new ModifiableLookup();
        //assumption: should not change in time, otherwise we need to listen
        providers = new ArrayList<CssStylesPanelProvider>();
        for (CssStylesPanelProvider provider : Lookup.getDefault().lookupAll(CssStylesPanelProvider.class)) {
            providers.add(new ProxyCssStylesPanelProvider(provider));
        }

        //the bottom component
        controller = RuleEditorController.createInstance();

        //put of the rule editor component initialization so the whole css styles panel
        //initialization is not done in one chunk
        EventQueue.invokeLater(new Runnable() {
            @Override
            public void run() {
                splitPane.setBottomComponent(controller.getRuleEditorComponent());
            }
        });

        //toolbar
        toolbarListener = new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent ae) {
                String command = ae.getActionCommand();
                //linear search, but should be at most 2 or 3 items
                for (CssStylesPanelProvider provider : providers) {
                    if (provider.getPanelID().equals(command)) {
                        FileObject file = providersLookup.lookup(FileObject.class);
                        selectedTabs.put(file.getMIMEType(), provider);
                        setActiveProvider(provider);
                    }
                }
            }
        };

        toolBar = new JToolBar();
        toolBar.setFloatable(false);

        //copied from org.netbeans.core.multiview.TabsComponent to make the look 
        //similar to the editor tabs
        Border b = (Border) UIManager.get("Nb.Editor.Toolbar.border"); //NOI18N
        toolBar.setBorder(b);
        toolBar.setFocusable(true);
        if ("Windows".equals(UIManager.getLookAndFeel().getID())
                && !isXPTheme()) {
            toolBar.setRollover(true);
        } else if (AQUA) {
            toolBar.setBackground(UIManager.getColor("NbExplorerView.background"));
        }

        splitPane.setResizeWeight(0.66);
    }

    private Border buttonBorder = null;

    private Border getButtonBorder() {
        if (buttonBorder == null) {
            //For some lf's, core will supply one
            buttonBorder = UIManager.getBorder("nb.tabbutton.border"); //NOI18N
        }

        return buttonBorder;
    }

    private static boolean isXPTheme() {
        Boolean isXP = (Boolean) Toolkit.getDefaultToolkit().
                getDesktopProperty("win.xpstyle.themeActive"); //NOI18N
        return isXP == null ? false : isXP.booleanValue();
    }

    private JSplitPane createSplitPane() {
        return new JSplitPane() {

            @Override
            public String getUIClassID() {
                if (AQUA && UIManager.get("Nb.SplitPaneUI.clean") != null) //NOI18N
                {
                    return "Nb.SplitPaneUI.clean"; //NOI18N
                }
                return super.getUIClassID();
            }
        };
    }

    /**
     * Returns lookup which content changes based on the lookups of the active
     * CssStylesPanelProvider.
     */
    public Lookup getLookup() {
        return tcLookup;
    }

    private Collection<CssStylesPanelProvider> getActiveProviders(FileObject file) {
        Collection<CssStylesPanelProvider> active = new ArrayList<CssStylesPanelProvider>();
        for (CssStylesPanelProvider provider : providers) {
            if (provider.providesContentFor(file)) {
                active.add(provider);
            }
        }
        return active;
    }

    private void addToolbar() {
        if (toolBar.getParent() == null) {
            //not added in the hierarchy, add it
            topPanel.add(toolBar, BorderLayout.PAGE_START);
        }
    }

    private void removeToolbar() {
        if (toolBar.getParent() != null) {
            //preset in the hierarchy, remove it
            topPanel.remove(toolBar);
        }
    }

    private void updateToolbar(final FileObject file) {
        RP.post(new Runnable() {

            @Override
            public void run() {
                //getActiveProviders() must not be called in EDT as it might do some I/Os
                final Collection<CssStylesPanelProvider> activeProviders = getActiveProviders(file);
                EventQueue.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        updateToolbar(file, activeProviders);
                    }

                });
            }

        });
    }

    private void updateToolbar(FileObject file, Collection<CssStylesPanelProvider> activeProviders) {

        toolBar.removeAll();
        if (activeProviders.size() <= 1) {
            //remove the whole toolbar, if there's one or zero providers
            removeToolbar();
        } else {
            addToolbar();
        }

        // Button group for document and source buttons
        ButtonGroup buttonGroup = new ButtonGroup();

        boolean first = true;

        CssStylesPanelProvider selected = (file == null) ? null : selectedTabs.get(file.getMIMEType());

        //do the active providers contain the pre-selected provider for this mimetype?
        boolean containsPreselected = selected == null ? false : activeProviders.contains(selected);

        for (CssStylesPanelProvider provider : activeProviders) {
            JToggleButton button = new JToggleButton();
            button.setText(provider.getPanelDisplayName());
            button.setActionCommand(provider.getPanelID());
            button.addActionListener(toolbarListener);

            button.setFocusable(true);
            button.setFocusPainted(false);
            button.setRolloverEnabled(true);

            //copied from org.netbeans.core.multiview.TabsComponent.createButton to make the look 
            //similar to the editor tabs
            Border b = (getButtonBorder());
            if (b != null) {
                button.setBorder(b);
            }
            if (AQUA) {
                button.putClientProperty("JButton.buttonType", "square"); //NOI18N
                button.putClientProperty("JComponent.sizeVariant", "small"); //NOI18N
            }

            buttonGroup.add(button);
            toolBar.add(button);

            if (containsPreselected) {
                //one of the active providers is already pre-selected by user
                if (provider == selected) {
                    //the selected one - activate it
                    button.setSelected(true);
                    setActiveProvider(provider);
                } else {
                    button.setSelected(false);
                }
            } else {
                //no provider has been explicitly selected by the user yet
                button.setSelected(first);
                if (first) {
                    setActiveProvider(provider);
                    first = false;
                }
            }
        }

        revalidate();
        repaint();
    }

    public void setContext(FileObject file) {
        InstanceContent ic = new InstanceContent();
        if (file != null) {
            ic.add(file);
        }
        ic.add(getRuleEditorController());
        providersLookup.updateLookup(new AbstractLookup(ic));

        updateToolbar(file);
    }

    private void setActiveProvider(CssStylesPanelProvider provider) {
        if (active == provider) {
            return; //no change
        }

        if (active != null) {
            topPanel.remove(activePanel);
            active.deactivated();
        }

        active = provider;
        activePanel = provider.getContent(providersLookup);

        topPanel.add(activePanel, BorderLayout.CENTER);
        active.activated();

        //propagate the provider's lookup to the lookup of the CssStylesTC.
        tcLookup.updateLookup(active.getLookup());

        revalidate();
        repaint();
    }

    /**
     * Returns the default {@link RuleEditorController} associated with this
     * rule editor top component.
     */
    public RuleEditorController getRuleEditorController() {
        return controller;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        splitPane = createSplitPane();
        topPanel = new javax.swing.JPanel();

        setLayout(new java.awt.BorderLayout());

        splitPane.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        splitPane.setDividerSize(4);
        splitPane.setOrientation(javax.swing.JSplitPane.VERTICAL_SPLIT);

        topPanel.setLayout(new java.awt.BorderLayout());
        splitPane.setTopComponent(topPanel);

        add(splitPane, java.awt.BorderLayout.CENTER);
    }// </editor-fold>//GEN-END:initComponents
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JSplitPane splitPane;
    private javax.swing.JPanel topPanel;
    // End of variables declaration//GEN-END:variables

    private static class ModifiableLookup extends ProxyLookup {

        protected final void updateLookup(Lookup lookup) {
            if (lookup == null) {
                setLookups();
            } else {
                setLookups(lookup);
            }
        }
    }

    /**
     * Caches the content panel so the real provider is asked for it just once.
     */
    private static class ProxyCssStylesPanelProvider implements CssStylesPanelProvider {

        private final CssStylesPanelProvider delegate;
        private JComponent content;

        public ProxyCssStylesPanelProvider(CssStylesPanelProvider delegate) {
            this.delegate = delegate;
        }

        @Override
        public String getPanelID() {
            return delegate.getPanelID();
        }

        @Override
        public String getPanelDisplayName() {
            return delegate.getPanelDisplayName();
        }

        @Override
        public JComponent getContent(Lookup lookup) {
            if (content == null) {
                content = delegate.getContent(lookup);
            }
            return content;
        }

        @Override
        public Lookup getLookup() {
            return delegate.getLookup();
        }

        @Override
        public void activated() {
            delegate.activated();
        }

        @Override
        public void deactivated() {
            delegate.deactivated();
        }

        @Override
        public boolean providesContentFor(FileObject file) {
            return delegate.providesContentFor(file);
        }
    }
}
