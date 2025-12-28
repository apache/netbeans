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
package org.netbeans.modules.refactoring.spi.impl;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringBufferInputStream;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.prefs.Preferences;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeWillExpandListener;
import javax.swing.event.ChangeEvent;
import javax.swing.text.html.HTMLEditorKit;
import javax.swing.text.html.parser.ParserDelegator;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.ExpandVetoException;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.*;
import org.netbeans.modules.parsing.api.indexing.IndexingManager;
import org.netbeans.modules.refactoring.api.*;
import org.netbeans.modules.refactoring.api.impl.APIAccessor;
import org.netbeans.modules.refactoring.spi.ui.ExpandableTreeElement;
import org.netbeans.modules.refactoring.spi.ui.FiltersDescription;
import org.netbeans.modules.refactoring.spi.ui.RefactoringCustomUI;
import org.netbeans.modules.refactoring.spi.ui.RefactoringUI;
import org.netbeans.modules.refactoring.spi.ui.TreeElement;
import org.netbeans.modules.refactoring.spi.ui.TreeElementFactory;
import org.netbeans.modules.refactoring.spi.ui.TreeElementFactoryImplementation;
import org.netbeans.modules.refactoring.spi.ui.UI;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.awt.Mnemonics;
import org.openide.awt.ToolbarWithOverflow;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.util.*;
import org.openide.windows.TopComponent;

/**
 * Panel for showing proposed changes (refactoring elements) of any refactoring.
 *
 * @author  Pavel Flaska, Martin Matula
 */
public class RefactoringPanel extends JPanel implements FiltersManagerImpl.FilterChangeListener {
    private static final RequestProcessor RP = new RequestProcessor(RefactoringPanel.class.getName(), 1, false, false);

    private static final String PREF_KEY_SHOW_PREVIEW = "showPreview";
    private static final String PREF_KEY_DIVIDER_LOCATION = "dividerLocation";

    // PRIVATE FIELDS
    /* tree contains elements which will be changed by refactoring action */
    private transient JTree tree = null;
    /* toolbar button causing refresh of the data */
    private transient JButton refreshButton = null;
    /* button lying in the toolbar allows expansion of all nodes in a tree */
    private transient JToggleButton expandButton = null;

    private transient JButton refactorButton = null;
    private transient JButton cancelButton = null;
    private transient ButtonL buttonListener = null;
    private transient JButton rerunButton = null;

    private final RefactoringUI refactoringUI;
    private final boolean isQuery;

    private transient boolean isVisible = false;
    private transient RefactoringSession session = null;
    private transient ParametersPanel parametersPanel = null;
    private transient JScrollPane scrollPane = null;
    private transient JPanel southPanel;
    private JSplitPane splitPane;
    private JPanel left;
    private Component right;
    private Action callback = null;

    private static final int MAX_ROWS = 50;
    private static final int MIN_DIVIDER_LOCATION = 250;

    private transient JToggleButton logicalViewButton = null;
    private transient JToggleButton physicalViewButton = null;
    private transient JToggleButton customViewButton = null;
    private transient JToggleButton previewButton = null;
    private JButton stopButton;

    private transient ProgressListener progressListener;
    private transient ProgressListener fuListener;

    private transient JButton prevMatch = null;
    private transient JButton nextMatch = null;
    private boolean inited = false;
    private Component customComponent;
    private final AtomicBoolean cancelRequest = new AtomicBoolean();
    private FiltersManagerImpl filtersManager;
    private JComponent filterBar;
    private JPanel toolbars;

    public RefactoringPanel(RefactoringUI ui) {
        this(ui,null);
    }

    public RefactoringPanel(RefactoringUI ui, TopComponent caller) {
        this.refactoringUI = ui;
        this.isQuery = ui.isQuery();
        if (isQuery) {
            fuListener = new FUListener();
            ui.getRefactoring().addProgressListener(fuListener);
        }
        refresh(true);
    }

    public RefactoringPanel(RefactoringUI ui, RefactoringSession session, Action callback) {
        this.session = session;
        this.refactoringUI = ui;
        this.isQuery = ui.isQuery();
        this.callback = callback;
        if (isQuery) {
            fuListener = new FUListener();
            ui.getRefactoring().addProgressListener(fuListener);
        }
        initialize();
        updateFilters(false);
        refresh(false);
    }

    public static void checkEventThread() {
        if (!SwingUtilities.isEventDispatchThread()) {
            ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, new IllegalStateException("This must happen in event thread!")); //NOI18N
        }
    }

    /* initializes all the ui */
    private void initialize() {
        if (inited) {
            return ;
        }
        checkEventThread();
        setFocusCycleRoot(true);
        splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        left = new JPanel();
        splitPane.setLeftComponent(left);
        left.setLayout(new BorderLayout());
        setLayout(new BorderLayout());
        add(splitPane, BorderLayout.CENTER);
        right = new JLabel(org.openide.util.NbBundle.getMessage(RefactoringPanel.class, "LBL_Preview_not_Available"), SwingConstants.CENTER);
        splitPane.setRightComponent(right);
        splitPane.setBorder(null);
        splitPane.addPropertyChangeListener(JSplitPane.DIVIDER_LOCATION_PROPERTY, evt -> {
            if (previewButton.isSelected() && splitPane.getRightComponent() != null) {
                if (evt.getNewValue() instanceof Integer pos && pos > MIN_DIVIDER_LOCATION) {
                    getPreferences().putInt(preferencesKeyForUI(PREF_KEY_DIVIDER_LOCATION), pos);
                }
            }
        });
        // add panel with buttons
        JButton[] buttons = getButtons();
        // there will be at least one button on panel
        southPanel = new JPanel(new GridBagLayout());
        for (int i = 0; i < buttons.length; i++) {
            GridBagConstraints c = new GridBagConstraints();
            c.gridy = 0;
            c.insets = new Insets(5, 5, 5, 0);
            southPanel.add(buttons[i], c);
        }
        JPanel pp = new JPanel(new BorderLayout());
        GridBagConstraints c = new GridBagConstraints();
        c.gridy = 0;
        c.insets = new Insets(5, 5, 5, 5);
        c.weightx = 1;
        c.fill = GridBagConstraints.HORIZONTAL;
        southPanel.add(pp, c);

        if (!isQuery|| callback != null) {
            left.add(southPanel, BorderLayout.SOUTH);
        }
        if ("Aqua".equals(UIManager.getLookAndFeel().getID())) { //NOI18N
            southPanel.setBackground(UIManager.getColor("NbExplorerView.background"));
        }

        JToolBar toolbar = getToolBar();
        if ("Aqua".equals(UIManager.getLookAndFeel().getID())) { //NOI18N
            toolbar.setBackground(UIManager.getColor("NbExplorerView.background"));
        }
        toolbars = new JPanel(new BorderLayout());
        toolbars.add(toolbar, BorderLayout.WEST);
        left.add(toolbars, BorderLayout.WEST);
        updatePreviewVisibility();
        validate();
        inited=true;
    }

    @Override
    public boolean requestFocusInWindow() {
        boolean value = super.requestFocusInWindow();
        if(this.tree != null) {
            return this.tree.requestFocusInWindow();
        } else {
            return value;
        }
    }

    //#41258: In the SDI, requestFocus is called rather than requestFocusInWindow:
    @Override
    public void requestFocus() {
        super.requestFocus();
        if(this.tree != null) {
            this.tree.requestFocus();
        }
    }

    /**
     * Returns the toolbar. In this default implementation, toolbar is
     * oriented vertically in the west and contains 'expand tree' toggle
     * button and refresh button.
     *
     * @return  toolBar with actions for refactoring panel
     */
    private JToolBar getToolBar() {
        checkEventThread();
        refreshButton = new JButton(
            ImageUtilities.loadImageIcon("org/netbeans/modules/refactoring/api/resources/refresh.png", false));
        Dimension dim = new Dimension(24, 24);
        refreshButton.setMaximumSize(dim);
        refreshButton.setMinimumSize(dim);
        refreshButton.setPreferredSize(dim);
        refreshButton.setToolTipText(
            NbBundle.getMessage(RefactoringPanel.class, "HINT_refresh") // NOI18N
        );
        refreshButton.setBorderPainted(false);
        refreshButton.addActionListener(getButtonListener());
        // expand button settings
        expandButton = new JToggleButton(
            ImageUtilities.loadImageIcon("org/netbeans/modules/refactoring/api/resources/expandTree.png", false));
        expandButton.setSelectedIcon(
            ImageUtilities.loadImageIcon("org/netbeans/modules/refactoring/api/resources/colapseTree.png", false));
        expandButton.setMaximumSize(dim);
        expandButton.setMinimumSize(dim);
        expandButton.setPreferredSize(dim);
        expandButton.setSelected(true);
        expandButton.setToolTipText(
            NbBundle.getMessage(RefactoringPanel.class, "HINT_expandAll") // NOI18N
        );
        expandButton.setBorderPainted(false);
        expandButton.addActionListener(getButtonListener());

        logicalViewButton = new JToggleButton(
            ImageUtilities.loadImageIcon("org/netbeans/modules/refactoring/api/resources/logical_view.png", false));

        logicalViewButton.setMaximumSize(dim);
        logicalViewButton.setMinimumSize(dim);
        logicalViewButton.setPreferredSize(dim);
        logicalViewButton.setSelected(currentView==LOGICAL);
        logicalViewButton.setToolTipText(
            NbBundle.getMessage(RefactoringPanel.class, "HINT_logicalView") // NOI18N
        );
        logicalViewButton.setBorderPainted(false);
        logicalViewButton.addActionListener(getButtonListener());

        physicalViewButton = new JToggleButton(
            ImageUtilities.loadImageIcon("org/netbeans/modules/refactoring/api/resources/file_view.png", false));

        physicalViewButton.setMaximumSize(dim);
        physicalViewButton.setMinimumSize(dim);
        physicalViewButton.setPreferredSize(dim);
        physicalViewButton.setSelected(currentView==PHYSICAL);
        physicalViewButton.setToolTipText(
            NbBundle.getMessage(RefactoringPanel.class, "HINT_physicalView") // NOI18N
        );
        physicalViewButton.setBorderPainted(false);
        physicalViewButton.addActionListener(getButtonListener());

        if (!Utilities.isMac()) {
            refreshButton.setMnemonic(
                    NbBundle.getMessage(RefactoringPanel.class, "MNEM_refresh").charAt(0)); // NOI18N
            expandButton.setMnemonic(
                    NbBundle.getMessage(RefactoringPanel.class, "MNEM_expandAll").charAt(0)); // NOI18N
            logicalViewButton.setMnemonic(
                    NbBundle.getMessage(RefactoringPanel.class, "MNEM_logicalView").charAt(0)); // NOI18N
            physicalViewButton.setMnemonic(
                    NbBundle.getMessage(RefactoringPanel.class, "MNEM_physicalView").charAt(0)); // NOI18N
        }

        if (refactoringUI instanceof RefactoringCustomUI refactoringCustomUI) {
            customViewButton = new JToggleButton(refactoringCustomUI.getCustomIcon());
            customViewButton.setMaximumSize(dim);
            customViewButton.setMinimumSize(dim);
            customViewButton.setPreferredSize(dim);
            customViewButton.setSelected(currentView==GRAPHICAL);
            customViewButton.setToolTipText(refactoringCustomUI.getCustomToolTip());
            customViewButton.setBorderPainted(false);
            customViewButton.addActionListener(getButtonListener());
        }

        nextMatch = new JButton(
            ImageUtilities.loadImageIcon("org/netbeans/modules/refactoring/api/resources/nextmatch.png", false));

        nextMatch.setMaximumSize(dim);
        nextMatch.setMinimumSize(dim);
        nextMatch.setPreferredSize(dim);
        nextMatch.setToolTipText(
            NbBundle.getMessage(RefactoringPanel.class, "HINT_nextMatch") // NOI18N
        );
        nextMatch.setBorderPainted(false);
        nextMatch.addActionListener(getButtonListener());

        prevMatch = new JButton(
            ImageUtilities.loadImageIcon("org/netbeans/modules/refactoring/api/resources/prevmatch.png", false));

        prevMatch.setMaximumSize(dim);
        prevMatch.setMinimumSize(dim);
        prevMatch.setPreferredSize(dim);
        prevMatch.setToolTipText(
            NbBundle.getMessage(RefactoringPanel.class, "HINT_prevMatch") // NOI18N
        );
        prevMatch.setBorderPainted(false);
        prevMatch.addActionListener(getButtonListener());

        stopButton = new JButton(
                ImageUtilities.loadImageIcon("org/netbeans/modules/refactoring/api/resources/stop.png", false));

        stopButton.setMaximumSize(dim);
        stopButton.setMinimumSize(dim);
        stopButton.setPreferredSize(dim);
        stopButton.setToolTipText(
            NbBundle.getMessage(RefactoringPanel.class, "HINT_stop") // NOI18N
        );
        stopButton.setBorderPainted(false);
        stopButton.addActionListener(getButtonListener());

        previewButton = new JToggleButton(
                ImageUtilities.loadImageIcon("org/netbeans/modules/refactoring/api/resources/preview.png", false));
        previewButton.setMaximumSize(dim);
        previewButton.setMinimumSize(dim);
        previewButton.setPreferredSize(dim);
        previewButton.setToolTipText(
                NbBundle.getMessage(RefactoringPanel.class, "HINT_showPreview") // NOI18N
        );
        previewButton.setBorderPainted(false);
        previewButton.addActionListener(getButtonListener());
        previewButton.setSelected(getPreferences().getBoolean(preferencesKeyForUI(PREF_KEY_SHOW_PREVIEW), true));

        // create toolbar
        JToolBar toolbar = new ToolbarWithOverflow(JToolBar.VERTICAL);
        toolbar.setFloatable(false);

        toolbar.add(refreshButton);
        toolbar.add(stopButton);
        toolbar.add(prevMatch);
        toolbar.add(nextMatch);
        toolbar.add(expandButton);
        toolbar.add(logicalViewButton);
        toolbar.add(physicalViewButton);
        toolbar.add(previewButton);
        if (refactoringUI instanceof RefactoringCustomUI) {
            toolbar.add(customViewButton);
        }
        return toolbar;
    }

    private Preferences getPreferences() {
        return NbPreferences.forModule(RefactoringPanel.class);
    }

    private String preferencesKeyForUI(String uiPreference) {
        return RefactoringPanel.class.getName() + "_" + (isQuery ? "query" : "refactoring") + "." + uiPreference;
    }

    /**
     * Returns array of available buttons. Initially, it returns only
     * basic "do refactoring/cancel refactoring" button. Override this method,
     * if you want to provide any other buttons with different action to be
     * performed.
     *
     * @return  array of available buttons.
     */
    private JButton[] getButtons() {
        checkEventThread();
        if (isQuery) {
            refactorButton = null;
            if (callback==null) {
                return new JButton[] {};
            } else {
                rerunButton = new JButton((String) callback.getValue(Action.NAME)); // NOI18N
                rerunButton.addActionListener(getButtonListener());
                return new JButton[] {rerunButton};
            }
        } else {
            refactorButton = new JButton(); // NOI18N
            Mnemonics.setLocalizedText(refactorButton, NbBundle.getMessage(RefactoringPanel.class, "LBL_DoRefactor"));
            refactorButton.setToolTipText(NbBundle.getMessage(RefactoringPanel.class, "HINT_DoRefactor")); // NOI18N
            refactorButton.addActionListener(getButtonListener());
            cancelButton = new JButton(NbBundle.getMessage(RefactoringPanel.class, "LBL_CancelRefactor")); // NOI18N
            Mnemonics.setLocalizedText(cancelButton, NbBundle.getMessage(RefactoringPanel.class, "LBL_CancelRefactor"));
            cancelButton.setToolTipText(NbBundle.getMessage(RefactoringPanel.class, "HINT_CancelRefactor")); // NOI18N
            cancelButton.addActionListener(getButtonListener());
            return new JButton[] {refactorButton, cancelButton};
        }
    }

    private static final byte LOGICAL = 0;
    private static final byte PHYSICAL = 1;
    private static final byte GRAPHICAL = 2;

    private static final String PREF_VIEW_TYPE = "PREF_VIEW_TYPE";
    private byte currentView = getPrefViewType();

    void switchToLogicalView() {
        logicalViewButton.setSelected(true);
        if (currentView == LOGICAL) {
            return ;
        }
        currentView = LOGICAL;
        physicalViewButton.setSelected(false);
        if (customViewButton!=null) {
            customViewButton.setSelected(false);
            prevMatch.setEnabled(true);
            nextMatch.setEnabled(true);
            expandButton.setEnabled(true);
        }
        storePrefViewType();
        refresh(false);
    }

    void switchToPhysicalView() {
        physicalViewButton.setSelected(true);
        if (currentView == PHYSICAL) {
            return ;
        }
        currentView = PHYSICAL;
        logicalViewButton.setSelected(false);
        if (customViewButton!=null) {
            customViewButton.setSelected(false);
            prevMatch.setEnabled(true);
            nextMatch.setEnabled(true);
            expandButton.setEnabled(true);
        }
        storePrefViewType();
        refresh(false);
    }

    void switchToCustomView() {
        customViewButton.setSelected(true);
        if (currentView == GRAPHICAL) {
            return ;
        }
        currentView = GRAPHICAL;
        logicalViewButton.setSelected(false);
        physicalViewButton.setSelected(false);
        prevMatch.setEnabled(false);
        nextMatch.setEnabled(false);
        expandButton.setEnabled(false);
        refresh(false);
    }

    private void updatePreviewVisibility() {
        getPreferences().putBoolean(preferencesKeyForUI(PREF_KEY_SHOW_PREVIEW), previewButton.isSelected());
        if (previewButton.isSelected()) {
            boolean initDivider = splitPane.getRightComponent() == null;
            if (initDivider) {
                splitPane.setRightComponent(right);
                initDivider();
            } else {
                int oldLocation = splitPane.getDividerLocation();
                splitPane.setRightComponent(right);
                splitPane.setDividerLocation(oldLocation);
            }
        } else {
            splitPane.setRightComponent(null);
        }
    }

    private CheckNode createNode(TreeElement representedObject, Map<Object, CheckNode> nodes, CheckNode root) {
        //checkEventThread();
        boolean isLogical = currentView == LOGICAL;

        CheckNode node;
        if (representedObject instanceof SourceGroup sourceGroup) {
            //workaround for issue 52541
            node = nodes.get(sourceGroup.getRootFolder());
        } else {
            node = nodes.get(representedObject);
        }
        if (node != null) {
            return node;
        }

        TreeElement parent = representedObject.getParent(isLogical);
        String displayName = representedObject.getText(isLogical);
        Icon icon = representedObject.getIcon();

        node = new CheckNode(representedObject, displayName, icon, isQuery);
        final CheckNode parentNode = parent == null ? root : createNode(parent, nodes, root);

        parentNode.add(node);

        if (isQuery) {
            final int childCount = parentNode.getChildCount();
            try {
                SwingUtilities.invokeAndWait(() -> {
                    if (tree!=null) {
                        ((DefaultTreeModel) tree.getModel()).nodesWereInserted(parentNode, new int[]{childCount-1});
                        tree.expandPath(new TreePath(parentNode.getPath()));
                    }
                });
            } catch (InterruptedException | InvocationTargetException ex) {
                Exceptions.printStackTrace(ex);
            }
        }

        if (representedObject instanceof SourceGroup sourceGroup) {
            //workaround for issue 52541
            nodes.put(sourceGroup.getRootFolder(), node);
        } else {
            nodes.put(representedObject, node);
        }
        return node;
    }

    /**
     * Method is responsible for making changes in sources.
     */
    private void refactor() {
        checkEventThread();
        if (!checkTimeStamps()) {
            if (JOptionPane.showConfirmDialog(
                    this,
                    NbBundle.getMessage(RefactoringPanel.class, "MSG_ConfirmRefresh"),
                    NbBundle.getMessage(RefactoringPanel.class, "MSG_FileModified"),
                    JOptionPane.OK_CANCEL_OPTION) == JOptionPane.OK_OPTION) {
                refresh(true);
                return;
            } else {
                return;
            }
        }
        disableComponents();
        progressListener = new ProgressL();
        RP.post(() -> {
            try {
                session.addProgressListener(progressListener);
                session.doRefactoring(true);
            } finally {
                session.removeProgressListener(progressListener);
                progressListener.stop(null);
                progressListener = null;
                SwingUtilities.invokeLater(RefactoringPanel.this::close);
            }
        });
    }

    /**
     * Cancel refactor action. This default implementation is closing window
     * only. It can return result state. In this implementation it returns
     * everytime 0.
     *
     * @return  result of cancel operation. Zero represent successful cancel.
     */
    private int cancel() {
        checkEventThread();
        this.close();
        return 0;
    }

    void close() {
        if (isQuery) {
            RefactoringPanelContainer.getUsagesComponent().removePanel(this);
        } else {
            RefactoringPanelContainer.getRefactoringComponent().removePanel(this);
        }
        if(isVisible) {
            Action action = FileUtil.getConfigObject("Actions/Window/org-netbeans-core-windows-actions-SwitchToRecentDocumentAction.instance", Action.class); //NOI18N
            if(action != null) {
                action.actionPerformed(new ActionEvent(this, ActionEvent.ACTION_PERFORMED, null));
            }
        }
        closeNotify();
    }


    /*
     * Initializes button listener. The subclasses must not need this listener.
     * This is the reason of lazy initialization.
     */
    private ButtonL getButtonListener() {
        if (buttonListener == null) {
            buttonListener = new ButtonL();
        }

        return buttonListener;
    }

    /* expandAll nodes in the tree */
    public void expandAll() {
        checkEventThread();
        final Cursor old = getCursor();
        expandButton.setEnabled(false);
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        int row = 0;
        while (row < tree.getRowCount()) {
            tree.expandRow(row);
            row++;
        }

        setCursor(old);
        expandButton.setEnabled(true);
        expandButton.setToolTipText(
            NbBundle.getMessage(RefactoringPanel.class, "HINT_collapseAll") // NOI18N
        );
    }

    /* collapseAll nodes in the tree */
    public void collapseAll() {
        checkEventThread();
        expandButton.setEnabled(false);
        final Cursor old = getCursor();
        setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));

        int row = tree.getRowCount() - 1;
        while (row > 0) {
            tree.collapseRow(row);
            row--;
        }

        setCursor(old);
        expandButton.setEnabled(true);
        expandButton.setToolTipText(
            NbBundle.getMessage(RefactoringPanel.class, "HINT_expandAll") // NOI18N
        );
    }

    private void refresh(final boolean showParametersPanel) {
        checkEventThread();
        boolean scanning = IndexingManager.getDefault().isIndexing();
        boolean resetFilters = showParametersPanel;
        if (showParametersPanel) {
            // create parameters panel for refactoring
            if (parametersPanel == null) {
                parametersPanel = new ParametersPanel(refactoringUI);
                resetFilters = false;
            }
            // show parameters dialog
            RefactoringSession tempSession = parametersPanel.showDialog();
            // if no elements were returned, action was either cancelled or preview
            // was skipped -> finish
            if (tempSession == null) {
                if (!parametersPanel.isCanceledDialog()) {
                    // close tab in case the refactoring is bypassed but it has been open before
                    close();
                }
                return;
            } else if (tempSession.getRefactoringElements().isEmpty() && !scanning && !isQuery) {
                DialogDescriptor nd = new DialogDescriptor(NbBundle.getMessage(ParametersPanel.class, "MSG_NoPatternsFound"),
                                        refactoringUI.getName(),
                                        true,
                                        new Object[] {DialogDescriptor.OK_OPTION},
                                        DialogDescriptor.OK_OPTION,
                                        DialogDescriptor.DEFAULT_ALIGN,
                                        refactoringUI.getHelpCtx(),
                                        null);
                DialogDisplayer.getDefault().notifyLater(nd);
                return;
            }

            session = tempSession;
        }

        final RefactoringPanelContainer cont = isQuery ? RefactoringPanelContainer.getUsagesComponent() : RefactoringPanelContainer.getRefactoringComponent();
        cont.makeBusy(true);
        final AtomicInteger size = new AtomicInteger();
        final AtomicBoolean sizeIsApproximate = new AtomicBoolean();
        initialize();
        if(showParametersPanel) {
            updateFilters(resetFilters);
        }

        cancelRequest.set(false);
        stopButton.setVisible(isQuery && showParametersPanel);
        refreshButton.setVisible(!isQuery || !showParametersPanel);
        stopButton.setEnabled(showParametersPanel);
        final String description = refactoringUI.getDescription();
        setToolTipText("<html>" + description + "</html>"); // NOI18N
        final Collection<RefactoringElement> elements = session.getRefactoringElements();
        setName(refactoringUI.getName());
        if (refactoringUI instanceof RefactoringCustomUI refactoringCustomUI) {
            if (customComponent==null) {
                customComponent = refactoringCustomUI.getCustomComponent(elements);
            }
            this.left.remove(customComponent);
        }
        final ProgressHandle progressHandle = ProgressHandle.createHandle(NbBundle.getMessage(RefactoringPanel.class, isQuery ? "LBL_PreparingUsagesTree":"LBL_PreparingRefactoringTree"));
        if (currentView == GRAPHICAL) {
            assert refactoringUI instanceof RefactoringCustomUI;
            assert customComponent != null;
            this.left.remove(scrollPane);
            this.left.add(customComponent, BorderLayout.CENTER);
            UI.setComponentForRefactoringPreview(null);
            this.splitPane.validate();
            this.repaint();
            tree=null;
        } else {
            RP.post(new Runnable() {
                @Override
                public void run() {
                    SwingUtilities.invokeLater(() -> {
                        setTreeControlsEnabled(false);
                    });
                    Set<FileObject> fileObjects = new HashSet<>();
                    int errorsNum = 0;
                    if (!isQuery) {
                        for (Iterator iter = elements.iterator(); iter.hasNext(); ) {
                            RefactoringElement elem = (RefactoringElement) iter.next();
                            if (elem.getStatus() == RefactoringElement.GUARDED || elem.getStatus() == RefactoringElement.READ_ONLY) {
                                errorsNum++;
                            }
                        }
                    }
                    StringBuffer errorsDesc = getErrorDesc(errorsNum, isQuery?size.get():elements.size(), 0, isQuery && sizeIsApproximate.get());
                    final CheckNode root = new CheckNode(refactoringUI, description + errorsDesc.toString() + "&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;",ImageUtilities.loadImageIcon("org/netbeans/modules/refactoring/api/resources/" + (isQuery ? "findusages.png" : "refactoring.gif"), false), isQuery);
                    final Map<Object, CheckNode> nodes = new HashMap<>();

                    if (isQuery && showParametersPanel) {
                        setupInstantTree(root);
                    }

                    if (!isQuery) {
                        progressHandle.start(elements.size()/10);
                    } else {
                        progressHandle.start();
                    }

                    int i=0;
                    int hidden = 0;
                    try {
                        //[retouche]                    JavaModel.getJavaRepository().beginTrans(false);
                        try {
                            // ui.getRefactoring().setClassPath();
                            for (Iterator it = elements.iterator(); it.hasNext();i++) {
                                final RefactoringElement e = (RefactoringElement) it.next();
                                TreeElement treeElement = null;
                                if(callback != null || // #217986, not really nice
                                        filtersManager == null || e.include(filtersManager)) {
                                    treeElement = TreeElementFactory.getTreeElement(e);
                                    createNode(treeElement, nodes, root);
                                } else {
                                    hidden++;
                                }
                                final int occurrences = i + (treeElement instanceof ExpandableTreeElement ? ((ExpandableTreeElement) treeElement).estimateChildCount() : 1);
                                final int hiddenOccurrences = hidden;
                                size.set(occurrences);
                                sizeIsApproximate.compareAndSet(false, treeElement instanceof ExpandableTreeElement);
                                if (isQuery && showParametersPanel) {
                                    if (cancelRequest.get()) {
                                        break;
                                    }
                                    final boolean finished = session!= null? APIAccessor.DEFAULT.isFinished(session) : true;
                                    final boolean last = !it.hasNext();
                                    if ((occurrences % 10 == 0 && !finished) || last) {
                                        SwingUtilities.invokeLater(() -> {
                                            if (tree!=null) {
                                                root.setNodeLabel(description + getErrorDesc(0, occurrences, hiddenOccurrences, isQuery && sizeIsApproximate.get()));
                                                if (last) {
                                                    tree.repaint();
                                                }
                                            }
                                        });
                                    }
                                }
//                                PositionBounds pb = e.getPosition();
                                fileObjects.add(e.getParentFile());

                                if (!isQuery) {
                                    if (i % 10 == 0) {
                                        progressHandle.progress(i / 10);
                                    }
                                }
                            }
                        } finally {
                            //[retouche]                        JavaModel.getJavaRepository().endTrans();
                        }

                        //UndoManager.getDefault().watch(editorSupports, RefactoringPanel.this);
                        storeTimeStamps(fileObjects);
                    } catch (RuntimeException | Error t) {
                        cleanupTreeElements();
                        throw t;
                    } finally {
                        progressHandle.finish();
                        cont.makeBusy(false);
                        SwingUtilities.invokeLater(() -> {
                            setTreeControlsEnabled(true);
                            stopButton.setEnabled(false);
                            stopButton.setVisible(false);
                            refreshButton.setVisible(true);
                            if(showParametersPanel) {
                                updateFilters(false);
                            }
                        });
                    }

                    if (!(isQuery && showParametersPanel)) {
                        root.setNodeLabel(description + getErrorDesc(errorsNum, elements.size(), hidden, false).toString());
                        setupTree(root, showParametersPanel, elements.size());
                    } else if (isQuery && showParametersPanel) {
                        SwingUtilities.invokeLater(() -> expandTreeIfNeeded(showParametersPanel, size.get()));
                    }

                }

                private StringBuffer getErrorDesc(int errorsNum, int occurencesNum, int hiddenNum, boolean occurencesNumApproximate) throws MissingResourceException {
                    StringBuffer errorsDesc = new StringBuffer();
                    errorsDesc.append(" ["); // NOI18N
                    errorsDesc.append(occurencesNumApproximate ?
                        NbBundle.getMessage(RefactoringPanel.class, "LBL_OccurencesApproximate", occurencesNum) :
                        NbBundle.getMessage(RefactoringPanel.class, "LBL_Occurences", occurencesNum)
                        );
                    if (errorsNum > 0) {
                        errorsDesc.append(',');
                        errorsDesc.append(' ');
                        errorsDesc.append("<font color=#CC0000>").append(errorsNum); // NOI18N
                        errorsDesc.append(' ');
                        errorsDesc.append(errorsNum == 1 ?
                            NbBundle.getMessage(RefactoringPanel.class, "LBL_Error") :
                            NbBundle.getMessage(RefactoringPanel.class, "LBL_Errors")
                            );
                        errorsDesc.append("</font>"); // NOI18N
                    }
                    if (hiddenNum > 0) {
                        errorsDesc.append(',');
                        errorsDesc.append(' ');
                        errorsDesc.append("<font color=#CC0000>").append(hiddenNum); // NOI18N
                        errorsDesc.append(' ');
                        errorsDesc.append(NbBundle.getMessage(RefactoringPanel.class, "LBL_Hidden"));
                        errorsDesc.append("</font>"); // NOI18N
                    }
                    errorsDesc.append(']');
                    return errorsDesc;
                }

            });
        }
        if (!isVisible) {
            // dock it into output window area and display
            cont.open();
            cont.requestActive();
            if (isQuery && parametersPanel!=null && !parametersPanel.isCreateNewTab()) {
                cont.removePanel(null);
            }
            cont.addPanel(this);
            isVisible = true;
        }
        if (!isQuery) {
            setRefactoringEnabled(false, true);
        }
    }

    private void setTreeControlsEnabled(final boolean b) {
        expandButton.setEnabled(b);
        logicalViewButton.setEnabled(b);
        physicalViewButton.setEnabled(b);
        if (customViewButton != null) {
            customViewButton.setEnabled(b);
        }
    }


    private void setupTree(final CheckNode root, final boolean showParametersPanel, final int size) {
        SwingUtilities.invokeLater(() -> {
            createTree(root);
            initDivider();
            expandTreeIfNeeded(showParametersPanel, size);

            tree.setSelectionRow(0);
            setRefactoringEnabled(true, true);
            if (parametersPanel != null && (Boolean) parametersPanel.getClientProperty(ParametersPanel.JUMP_TO_FIRST_OCCURENCE)) {
                selectNextUsage(false);
            }
        });
    }

    private void initDivider() {
        int dividerLocation = getPreferences().getInt(preferencesKeyForUI(PREF_KEY_DIVIDER_LOCATION), MIN_DIVIDER_LOCATION);
        if (dividerLocation > MIN_DIVIDER_LOCATION) {
            splitPane.setDividerLocation(dividerLocation);
        } else {
            splitPane.setDividerLocation(0.3);
        }
    }

    private void expandTreeIfNeeded(boolean showParametersPanel, int size) {
        if (showParametersPanel) {
            if (size < MAX_ROWS) {
                expandAll();
                selectNextUsage(false);
            } else {
                expandButton.setSelected(false);
            }
        } else {
            if (expandButton.isSelected()) {
                expandAll();
                selectNextUsage(false);
            } else {
                expandButton.setSelected(false);
            }
        }
    }

     private final Map<FileObject, Long> timeStamps = new HashMap<>();

     private void storeTimeStamps(Set<FileObject> fileObjects) {
         timeStamps.clear();
         for (FileObject fo:fileObjects) {
             timeStamps.put(fo, fo.lastModified().getTime());
         }
     }

    /**
     * @return true if timestamps are OK
     */
    private boolean checkTimeStamps() {
        Set<FileObject> modified = getModifiedFileObjects();
         for (Entry<FileObject, Long> entry: timeStamps.entrySet()) {
             if (modified.contains(entry.getKey()))
                return false;
             if (!entry.getKey().isValid())
                 return false;
             if (entry.getKey().lastModified().getTime() != entry.getValue())
                 return false;
            }
        return true;
    }

     private Set<FileObject> getModifiedFileObjects() {
         Set<FileObject> result = new HashSet<>();
         for (DataObject dob: DataObject.getRegistry().getModified()) {
             result.add(dob.getPrimaryFile());
         }
         return result;
     }

    private void createTree(TreeNode root) throws MissingResourceException {
        if (tree == null) {
            // add panel with appropriate content
            tree = new JTree(root);
            if ("Aqua".equals(UIManager.getLookAndFeel().getID())) { //NOI18N
                tree.setBackground(UIManager.getColor("NbExplorerView.background")); //NOI18N
            }
            ToolTipManager.sharedInstance().registerComponent(tree);
            tree.setCellRenderer(new CheckRenderer(isQuery, tree.getBackground()));
            String s = NbBundle.getMessage(RefactoringPanel.class, "ACSD_usagesTree"); // NOI18N
            tree.getAccessibleContext().setAccessibleDescription(s);
            tree.getAccessibleContext().setAccessibleName(s);
            CheckNodeListener l = new CheckNodeListener(isQuery);
            tree.addMouseListener(l);
            tree.addKeyListener(l);
            tree.setToggleClickCount(0);
            tree.setTransferHandler(new TransferHandlerImpl());
            scrollPane = new JScrollPane(tree);
            scrollPane.setBorder(javax.swing.BorderFactory.createCompoundBorder(
                javax.swing.BorderFactory.createMatteBorder(0, 1, 1, 1,
                javax.swing.UIManager.getDefaults().getColor("Separator.background")),
                javax.swing.BorderFactory.createMatteBorder(0, 1, 1, 1,
                javax.swing.UIManager.getDefaults().getColor("Separator.foreground"))));

            RefactoringPanel.this.left.add(scrollPane, BorderLayout.CENTER);
            RefactoringPanel.this.validate();
        } else {
            tree.setModel(new DefaultTreeModel(root));
        }
        tree.setRowHeight((int) ((CheckRenderer) tree.getCellRenderer()).getPreferredSize().getHeight());

        this.tree.addTreeWillExpandListener(new TreeWillExpandListener() {
            @Override public void treeWillExpand(TreeExpansionEvent event) throws ExpandVetoException {
                Object last = event.getPath().getLastPathComponent();

                if (last instanceof CheckNode checkNode) {
                    checkNode.ensureChildrenFilled((DefaultTreeModel) RefactoringPanel.this.tree.getModel());
                }
            }

            @Override public void treeWillCollapse(TreeExpansionEvent event) throws ExpandVetoException { }
        });
    }

    private void setupInstantTree(final CheckNode root) {
        SwingUtilities.invokeLater(() -> {
            createTree(root);
            tree.setSelectionRow(0);
            initDivider();
            if (refactorButton != null) {
                refactorButton.requestFocusInWindow();
            } else if (tree != null) {
                tree.requestFocusInWindow();
            }
        });
    }

    void setRefactoringEnabled(boolean enabled, boolean isRefreshing) {
        checkEventThread();
        if (tree != null) {
            if (!enabled) {
                CheckNode c = (CheckNode) tree.getModel().getRoot();
                if (!isRefreshing) {
                    c.setNeedsRefresh();
                } else {
                    c.setDisabled();
                }
                tree.setModel(new DefaultTreeModel(c, false));
            }
//            tree.validate();
            tree.setEnabled(enabled);
            if (refactorButton != null) {
                refactorButton.setEnabled(enabled);
            }
        }
        if (refactorButton != null) {
            refactorButton.requestFocusInWindow();
        } else if (tree != null) {
            tree.requestFocusInWindow();
        }
    }

    // disables all components in a given container
    private void disableComponent(JComponent jc) {
        if(jc != null) {
            jc.setEnabled(false);
        }
    }

    private void disableComponents() {
        disableComponent(cancelButton);
        disableComponent(expandButton);
        disableComponent(filterBar);
        disableComponent(logicalViewButton);
        disableComponent(nextMatch);
        disableComponent(physicalViewButton);
        disableComponent(prevMatch);
        disableComponent(refactorButton);
        disableComponent(refreshButton);
        disableComponent(rerunButton);
        disableComponent(stopButton);
        disableComponent(previewButton);
        disableComponent(tree);
    }

    /**
     * @param enableSourceJump if true and preview is disabled, the
     * next/previous actions shall jump to the corresponding code location,
     * this should not happen when the initial tree is opened.
     */
    void selectNextUsage(boolean enableSourceJump) {
        CheckNodeListener.selectNextPrev(true, enableSourceJump && !previewButton.isSelected(), tree);
    }

    /**
     * @param enableSourceJump if true and preview is disabled, the
     * next/previous actions shall jump to the corresponding code location,
     * this should not happen when the initial tree is opened.
     */
    void selectPrevUsage(boolean enableSourceJump) {
        CheckNodeListener.selectNextPrev(false, enableSourceJump && !previewButton.isSelected(), tree);
    }

    public boolean setPreviewComponent(Component component) {
        if (component == null) {
            if (right == null) {
                return false;
            }
        }
        if (component == null) {
            right = new JLabel(org.openide.util.NbBundle.getMessage(RefactoringPanel.class, "LBL_Preview_not_Available"), SwingConstants.CENTER);
        } else {
            right = component;
        }
        updatePreviewVisibility();
        return true;
    }

    public boolean isQuery() {
        return isQuery;
    }

    private byte getPrefViewType() {
        Preferences prefs = NbPreferences.forModule(RefactoringPanel.class);
        return (byte) prefs.getInt(PREF_VIEW_TYPE, PHYSICAL);
    }

    private void storePrefViewType() {
        assert currentView!=GRAPHICAL;
        Preferences prefs = NbPreferences.forModule(RefactoringPanel.class);
        prefs.putInt(PREF_VIEW_TYPE, currentView);
    }

    private void updateFilters(boolean reset) {
        if(!refactoringUI.isQuery() || callback != null) {
            if(filterBar != null) {
                toolbars.remove(filterBar);
                filterBar = null;
                filtersManager = null;
            }
            return;
        }

        if(filtersManager != null) {
            toolbars.remove(filterBar);
            filterBar = null;
            filtersManager = null;
        }
        AbstractRefactoring refactoring = refactoringUI.getRefactoring();
        if(reset) {
            APIAccessor.DEFAULT.resetFiltersDescription(refactoring);
        }
        final FiltersDescription desc = APIAccessor.DEFAULT.getFiltersDescription(refactoring);
        filtersManager = FiltersManagerImpl.create(desc == null? new FiltersDescription() : desc);
        filterBar = filtersManager.getComponent();
        toolbars.add(filterBar, BorderLayout.EAST);
        filtersManager.hookChangeListener(this);
        toolbars.validate();
    }

    @Override
    public void filterStateChanged(ChangeEvent e) {
        refresh(false);
    }

    @CheckForNull
    public static RefactoringPanel getCurrentRefactoringPanel() {
        TopComponent activated = TopComponent.getRegistry().getActivated();
        RefactoringPanel refactoringPanel = null;
        if (activated instanceof RefactoringPanelContainer panel) {
            refactoringPanel = panel.getCurrentPanel();
        }
        if (refactoringPanel == null) {
            refactoringPanel = RefactoringPanelContainer.getRefactoringComponent().getCurrentPanel();
        }
        if (refactoringPanel == null) {
            refactoringPanel = RefactoringPanelContainer.getUsagesComponent().getCurrentPanel();
        }

        return refactoringPanel;
    }

    // INNER CLASSES
    private class ButtonL implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent event) {
            Object o = event.getSource();
            // Cancel button pressed, remove refactoring panel
            if (o == cancelButton) {
                cancel();
            } else if (o == refactorButton) {
                refactor();
            } else if (o == rerunButton) {
                close();
                callback.actionPerformed(event);
            }
            // expandAll button selected/deselected
            else if (o == expandButton && tree != null) {
                if (expandButton.isSelected()) {
                    expandAll();
                } else {
                    collapseAll();
                }
            } else if (o == refreshButton) {
                if (callback!=null) {
                    close();
                    callback.actionPerformed(event);
                } else {
                    refresh(true);
                }
            } else if (o == physicalViewButton) {
                switchToPhysicalView();
            } else if (o == logicalViewButton) {
                switchToLogicalView();
            } else if (o == customViewButton) {
                switchToCustomView();
            } else if (o == nextMatch) {
                selectNextUsage(true);
            } else if (o == prevMatch) {
                selectPrevUsage(true);
            } else if (o == stopButton) {
                stopSearch();
            } else if (o == previewButton) {
                updatePreviewVisibility();
            }
        }

    } // end ButtonL
    private void stopSearch() {
        if(isVisible) {
            stopButton.setEnabled(false);
            stopButton.setVisible(false);
            refreshButton.setVisible(true);
        }
        cancelRequest.set(true);
        refactoringUI.getRefactoring().cancelRequest();
    }

    /** Processes returned problems from refactoring operations and notifies
     * user (in case of non-fatal problems gives user a chance to continue or cancel).
     * @param problem Problems returned from a refactoring operation.
     * @return <code>true</code> if no fatal problems were found and user decided
     * to continue in case of non-fatal problems; <code>false</code> if there was at
     * least one fatal problem or at least one non-fatal problem in response to which
     * user decided to cancel the operation.
     */
    /* public static boolean confirmProblems(Problem problem) {
        while (problem != null) {
            int result;
            if (problem.isFatal()) {
                JOptionPane.showMessageDialog(null, problem.getMessage(), NbBundle.getMessage(ParametersPanel.class, "LBL_Error"), JOptionPane.ERROR_MESSAGE);
                return false;
            } else {
                if (JOptionPane.showConfirmDialog(
                    null,
                    problem.getMessage() + ' ' + NbBundle.getMessage(ParametersPanel.class, "QST_Continue"),
                    NbBundle.getMessage(ParametersPanel.class, "LBL_Warning"),
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE
                ) != JOptionPane.YES_OPTION) {
                    return false;
                }
            }
            problem = problem.getNext();
        }
        return true;
    } */

    protected void closeNotify() {
        if (fuListener!=null) {
            stopSearch();
            refactoringUI.getRefactoring().removeProgressListener(fuListener);
            fuListener.stop(null);
            fuListener = null;
        }
        timeStamps.clear();
        //UndoWatcher.stopWatching(this);
        if (tree!=null) {
            ToolTipManager.sharedInstance().unregisterComponent(tree);
            scrollPane.getViewport().remove(tree);
        }
        if (scrollPane!=null)
            scrollPane.setViewport(null);
//        if (refCallerTC != null) {
//            TopComponent tc = refCallerTC.get();
//            if (tc != null && tc.isShowing()) {
//                tc.requestActive();
//            }
//        }
        cleanupTreeElements();
        PreviewManager.getDefault().clean(this);
        tree = null;
        session =null;
        parametersPanel = null;
        //super.closeNotify();
    }

    private void cleanupTreeElements() {
        for (TreeElementFactoryImplementation tefi: Lookup.getDefault().lookupAll(TreeElementFactoryImplementation.class)) {
            tefi.cleanUp();
        }
    }

    private static class ProgressL implements ProgressListener {

        private final ProgressHandle handle;
        private final Dialog d;

        public ProgressL() {
            final String lab = NbBundle.getMessage(RefactoringPanel.class, "LBL_RefactorProgressLabel");
            handle = ProgressHandle.createHandle(lab);
            JComponent progress = ProgressHandleFactory.createProgressComponent(handle);
            JPanel component = new JPanel();
            component.setLayout(new BorderLayout());
            component.setBorder(new EmptyBorder(12,12,11,11));
            JLabel label = new JLabel(lab);
            label.setBorder(new EmptyBorder(0, 0, 6, 0));
            component.add(label, BorderLayout.NORTH);
            component.add(progress, BorderLayout.CENTER);
            DialogDescriptor desc = new DialogDescriptor(component, NbBundle.getMessage(RefactoringPanel.class, "LBL_RefactoringInProgress"), true, new Object[]{}, null, 0, null, null);
            desc.setLeaf(true);
            d = DialogDisplayer.getDefault().createDialog(desc);
            ((JDialog) d).setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        }

        @Override
        public void start(final ProgressEvent event) {
            SwingUtilities.invokeLater(() -> {
                handle.start(event.getCount());
                d.setVisible(true);
            });
        }

        @Override
        public void step(final ProgressEvent event) {
            SwingUtilities.invokeLater(() -> {
                try {
                    handle.progress(event.getCount());
                } catch (Throwable e) {
                    ErrorManager.getDefault().notify(ErrorManager.INFORMATIONAL, e);
                }
            });
        }

        @Override
        public void stop(final ProgressEvent event) {
            SwingUtilities.invokeLater(() -> {
                if (event!=null) {
                    handle.finish();
                }
                d.setVisible(false);
                d.dispose();
            });
        }
    }

    private class FUListener implements ProgressListener, Cancellable {

        private ProgressHandle handle;
        private boolean isIndeterminate;

        @Override
        public void start(final ProgressEvent event) {
            handle = ProgressHandle.createHandle(getMessage(event), this);
            if (event.getCount() == -1) {
                handle.start();
                handle.switchToIndeterminate();
                isIndeterminate = true;
            } else {
                handle.start(event.getCount());
                isIndeterminate = false;
            }
        }

        @Override
        public void step(ProgressEvent event) {
            if (handle == null) {
                return;
            }

            if (isIndeterminate && event.getCount() > 0) {
                handle.switchToDeterminate(event.getCount());
                handle.setDisplayName(getMessage(event));
                isIndeterminate = false;
            } else {
                handle.progress(isIndeterminate ? -2 : event.getCount());
            }
        }

        @Override
        public void stop(final ProgressEvent event) {
            if (handle != null) {
                handle.finish();
            }
        }

        private String getMessage(ProgressEvent event) {
            switch (event.getOperationType()) {
                case AbstractRefactoring.PARAMETERS_CHECK:
                    return NbBundle.getMessage(ParametersPanel.class, "LBL_ParametersCheck");
                case AbstractRefactoring.PREPARE:
                    return NbBundle.getMessage(ParametersPanel.class, "LBL_Prepare");
                case AbstractRefactoring.PRE_CHECK:
                    return NbBundle.getMessage(ParametersPanel.class, "LBL_PreCheck");
                default:
                    return NbBundle.getMessage(ParametersPanel.class, "LBL_Usages");
            }
        }

        @Override
        public boolean cancel() {
            stopSearch();
            return true;
        }
    }

    private static class TransferHandlerImpl extends TransferHandler {
        @Override
        protected Transferable createTransferable(JComponent c) {
            if (c instanceof JTree tree) {
                TreePath[] paths = tree.getSelectionPaths();

                if (paths == null || paths.length == 0) {
                    return null;
                }

                Html2Text html2Text = new Html2Text();
                StringBuilder plain = new StringBuilder();
                StringBuilder html = new StringBuilder("<html><ul>"); // NOI18N
                int depth = 1;
                for(TreePath path: paths) {
                    for(; depth < path.getPathCount(); depth++) {
                        html.append("<ul>"); // NOI18N
                    }
                    for(; depth > path.getPathCount(); depth--) {
                        html.append("</ul>"); // NOI18N
                    }
                    Object o = path.getLastPathComponent();
                    if(o instanceof CheckNode node) {
                        String label = node.getLabel();
                        try {
                            html2Text.parse(new StringReader(label));
                        } catch (IOException ex) {
                            assert false : ex;
                        }

                        plain.append(html2Text.getText());
                        plain.append("\n"); // NOI18N
                        html.append("<li>"); // NOI18N
                        html.append(label);
                        html.append("</li>"); // NOI18N
                    }
                }
                for(; depth > 1; depth--) {
                        html.append("</ul>"); // NOI18N
                }
                html.append("</ul></html>"); // NOI18N

                return new ResultTransferable(plain.toString(), html.toString());
            }
            return null;
        }

        @Override
        public int getSourceActions(JComponent c) {
            return COPY;
        }
    }
    /**
     * Transferable implementation for ResultPanel.
     */
    private static class ResultTransferable implements Transferable {
        private static DataFlavor[] stringFlavors;
        private static DataFlavor[] plainFlavors;
        private static DataFlavor[] htmlFlavors;

        static {
            try {
                htmlFlavors = new DataFlavor[3];
                htmlFlavors[0] = new DataFlavor("text/html;class=java.lang.String"); // NOI18N
                htmlFlavors[1] = new DataFlavor("text/html;class=java.io.Reader"); // NOI18N
                htmlFlavors[2] = new DataFlavor("text/html;charset=unicode;class=java.io.InputStream"); // NOI18N

                plainFlavors = new DataFlavor[3];
                plainFlavors[0] = new DataFlavor("text/plain;class=java.lang.String"); // NOI18N
                plainFlavors[1] = new DataFlavor("text/plain;class=java.io.Reader"); // NOI18N
                // XXX isn't this just DataFlavor.plainTextFlavor?
                plainFlavors[2] = new DataFlavor("text/plain;charset=unicode;class=java.io.InputStream"); // NOI18N

                stringFlavors = new DataFlavor[2];
                stringFlavors[0] = new DataFlavor(DataFlavor.javaJVMLocalObjectMimeType + ";class=java.lang.String"); // NOI18N
                stringFlavors[1] = DataFlavor.stringFlavor;
            } catch (ClassNotFoundException cle) {
                assert false : cle;
            }
        }

        protected String plainData;
        protected String htmlData;

        public ResultTransferable(String plainData, String htmlData) {
            this.plainData = plainData;
            this.htmlData = htmlData;
        }

        @Override
        public DataFlavor[] getTransferDataFlavors() {
            int nHtml = (isHtmlSupported()) ? htmlFlavors.length : 0;
            int nPlain = (isPlainSupported()) ? plainFlavors.length : 0;
            int nString = (isPlainSupported()) ? stringFlavors.length : 0;
            int nFlavors = nHtml + nPlain + nString;
            DataFlavor[] flavors = new DataFlavor[nFlavors];

            // fill in the array
            int nDone = 0;
            if (nHtml > 0) {
                System.arraycopy(htmlFlavors, 0, flavors, nDone, nHtml);
                nDone += nHtml;
            }
            if (nPlain > 0) {
                System.arraycopy(plainFlavors, 0, flavors, nDone, nPlain);
                nDone += nPlain;
            }
            if (nString > 0) {
                System.arraycopy(stringFlavors, 0, flavors, nDone, nString);
                nDone += nString;
            }
            return flavors;
        }

        @Override
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            DataFlavor[] flavors = getTransferDataFlavors();
            for (int i = 0; i < flavors.length; i++) {
                if (flavors[i].equals(flavor)) {
                    return true;
                }
            }
            return false;
        }

        @Override
        public Object getTransferData(DataFlavor flavor)
        throws UnsupportedFlavorException, IOException {
            if (isHtmlFlavor(flavor)) {
                String html = getHtmlData();
                html = (html == null) ? "" : html;
                if (String.class.equals(flavor.getRepresentationClass())) {
                    return html;
                } else if (Reader.class.equals(flavor.getRepresentationClass())) {
                    return new StringReader(html);
                } else if (InputStream.class.equals(flavor.getRepresentationClass())) {
                    // XXX should this enforce UTF-8 encoding?
                    return new StringBufferInputStream(html);
                }
                // fall through to unsupported
            } else if (isPlainFlavor(flavor)) {
                String data = getPlainData();
                data = (data == null) ? "" : data;
                if (String.class.equals(flavor.getRepresentationClass())) {
                    return data;
                } else if (Reader.class.equals(flavor.getRepresentationClass())) {
                    return new StringReader(data);
                } else if (InputStream.class.equals(flavor.getRepresentationClass())) {
                    // XXX should this enforce UTF-8 encoding?
                    return new StringBufferInputStream(data);
                }
                // fall through to unsupported
            } else if (isStringFlavor(flavor)) {
                String data = getPlainData();
                data = (data == null) ? "" : data;

                return data;
            }

            throw new UnsupportedFlavorException(flavor);
        }

        // --- plain text flavors ----------------------------------------------

        /**
         * Returns whether or not the specified data flavor is an plain flavor
         * that is supported.
         *
         * @param flavor the requested flavor for the data
         * @return boolean indicating whether or not the data flavor is supported
         */
        protected boolean isPlainFlavor(DataFlavor flavor) {
            DataFlavor[] flavors = plainFlavors;

            for (int i = 0; i < flavors.length; i++) {
                if (flavors[i].equals(flavor)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Should the plain text flavors be offered?  If so, the method
         * getPlainData should be implemented to provide something reasonable.
         */
        protected boolean isPlainSupported() {
            return plainData != null;
        }

        /**
         * Fetch the data in a text/plain format.
         */
        protected String getPlainData() {
            return plainData;
        }

        // --- string flavors --------------------------------------------------

        /**
         * Returns whether or not the specified data flavor is a String flavor
         * that is supported.
         *
         * @param flavor the requested flavor for the data
         * @return boolean indicating whether or not the data flavor is supported
         */
        protected boolean isStringFlavor(DataFlavor flavor) {
            DataFlavor[] flavors = stringFlavors;

            for (int i = 0; i < flavors.length; i++) {
                if (flavors[i].equals(flavor)) {
                    return true;
                }
            }
            return false;
        }

        // --- html flavors ----------------------------------------------------

        /**
         * Returns whether or not the specified data flavor is a html flavor
         * that is supported.
         *
         * @param flavor the requested flavor for the data
         * @return boolean indicating whether or not the data flavor is supported
         */
        protected boolean isHtmlFlavor(DataFlavor flavor) {
            DataFlavor[] flavors = htmlFlavors;

            for (int i = 0; i < flavors.length; i++) {
                if (flavors[i].equals(flavor)) {
                    return true;
                }
            }
            return false;
        }

        /**
         * Should the html text flavors be offered? If so, the method
         * getHtmlData should be implemented to provide something reasonable.
         */
        protected boolean isHtmlSupported() {
            return htmlData != null;
        }

        /**
         * Fetch the data in text/html format.
         */
        protected String getHtmlData() {
            return htmlData;
        }
    }

    private static class Html2Text extends HTMLEditorKit.ParserCallback {
        StringBuffer s;

        public Html2Text() {
        }

        public void parse(Reader in) throws IOException {
            s = new StringBuffer();
            ParserDelegator delegator = new ParserDelegator();
            // the third parameter is TRUE to ignore charset directive
            delegator.parse(in, this, Boolean.TRUE);
        }

        @Override
        public void handleText(char[] text, int pos) {
            s.append(text);
        }

        public String getText() {
            return s.toString();
        }
    }
} // end Refactor Panel
