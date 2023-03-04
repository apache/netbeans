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
package org.netbeans.modules.profiler.heapwalk.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.HierarchyEvent;
import java.awt.event.HierarchyListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.URL;
import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoundedRangeModel;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.plaf.basic.BasicSplitPaneDivider;
import javax.swing.plaf.basic.BasicSplitPaneUI;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreePath;
import org.netbeans.lib.profiler.ui.UIUtils;
import org.netbeans.lib.profiler.ui.components.HTMLTextArea;
import org.netbeans.lib.profiler.ui.components.JExtendedSplitPane;
import org.netbeans.lib.profiler.ui.components.JTitledPanel;
import org.netbeans.modules.profiler.api.icons.Icons;
import org.netbeans.modules.profiler.heapwalk.OQLController;
import org.netbeans.modules.profiler.heapwalk.OQLSupport;
import org.netbeans.modules.profiler.heapwalk.model.BrowserUtils;
import org.netbeans.modules.profiler.heapwalk.oql.ui.OQLEditor;
import org.netbeans.modules.profiler.heapwalk.ui.icons.HeapWalkerIcons;
import org.netbeans.modules.profiler.oql.engine.api.OQLEngine;
import org.netbeans.modules.profiler.oql.icons.OQLIcons;
import org.openide.awt.Mnemonics;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;


/**
 *
 * @author Tomas Hurka
 * @author Jiri Sedlacek
 * @author Jaroslav Bachorik
 */
@NbBundle.Messages({
    "OQLControllerUI_ControllerName=OQL Console",
    "OQLControllerUI_ControllerDescr=Write and run OQL scripts on the dump",
    "OQLControllerUI_QueryResultsCaption=Query Results",
    "OQLControllerUI_QueryEditorCaption=Query Editor",
    "OQLControllerUI_SavedQueriesCaption=Saved Queries",
    "OQLControllerUI_ExecutingQueryMsg=Executing query:",
    "OQLControllerUI_ExecuteButtonText=E&xecute",
    "OQLControllerUI_ExecuteButtonAccessDescr=Execute the current OQL query",
    "OQLControllerUI_CancelButtonText=Cancel",
    "OQLControllerUI_CancelButtonAccessDescr=Cancel the query currently being executed",
    "OQLControllerUI_SaveButtonText=Save",
    "OQLControllerUI_SaveButtonAccessDescr=Save the current query",
    "OQLControllerUI_PropertiesButtonText=Properties",
    "OQLControllerUI_PropertiesButtonAccessDescr=Change selected query name, description and position",
    "OQLControllerUI_DeleteButtonText=Delete",
    "OQLControllerUI_DeleteButtonAccessDescr=Delete selected query",
    "OQLControllerUI_OpenButtonText=Open",
    "OQLControllerUI_OpenButtonAccessDescr=Open selected query in Query Editor",
    "OQLControllerUI_LoadingQueriesMsg=Loading saved queries...",
    "OQLControllerUI_NoSavedQueriesMsg=<No Saved Queries>"
})
public class OQLControllerUI extends JPanel implements HelpCtx.Provider {
    
    private static Icon OQL_ICON = Icons.getIcon(OQLIcons.OQL);


    // --- Presenter -------------------------------------------------------------

    private static class Presenter extends JToggleButton implements HelpCtx.Provider {
        //~ Static fields/initializers -------------------------------------------------------------------------------------------

        private static Icon OQL_ICON = Icons.getIcon(OQLIcons.OQL);


        //~ Constructors ---------------------------------------------------------------------------------------------------------
        public Presenter(final QueryUI queryUI) {
            super();
            setText(Bundle.OQLControllerUI_ControllerName());
            setToolTipText(Bundle.OQLControllerUI_ControllerDescr());
            setIcon(OQL_ICON);

            addKeyListener(new KeyAdapter() {
                public void keyTyped(final KeyEvent e) {
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            queryUI.requestFocus();
                            queryUI.addToQuery(e.getKeyChar());
                        }
                    });
                }
            });
        }
        
        public Dimension getPreferredSize() {
            Dimension d = super.getPreferredSize();
            d.width += 4;
            return d;
        }
        
        public Dimension getMinimumSize() {
            return getPreferredSize();
        }

        public HelpCtx getHelpCtx() {
            return HELP_CTX;
        }
    }

    //~ Static fields/initializers -----------------------------------------------------------------------------------------------

    private static final String HELP_CTX_KEY = "OQLControllerUI.HelpCtx"; // NOI18N
    private static final HelpCtx HELP_CTX = new HelpCtx(HELP_CTX_KEY);

    //~ Instance fields ----------------------------------------------------------------------------------------------------------
    private AbstractButton presenter;
    private OQLController oqlController;


    public OQLControllerUI(OQLController controller) {
        this.oqlController = controller;
        initComponents();
    }

    // --- Public interface ------------------------------------------------------

    public HelpCtx getHelpCtx() {
        return HELP_CTX;
    }

    public AbstractButton getPresenter() {
        if (presenter == null) {
            presenter = new Presenter((QueryUI)oqlController.getQueryController().getPanel());
        }

        return presenter;
    }

    private void initComponents() {
        JSplitPane querySplitter = new JExtendedSplitPane(JSplitPane.HORIZONTAL_SPLIT,
                                        oqlController.getQueryController().getPanel(),
                                        oqlController.getSavedController().getPanel());
        tweakSplitPaneUI(querySplitter);
        querySplitter.setResizeWeight(1d);

        JSplitPane mainSplitter = new JExtendedSplitPane(JSplitPane.VERTICAL_SPLIT,
                                        oqlController.getResultsController().getPanel(),
                                        querySplitter);
        tweakSplitPaneUI(mainSplitter);
        mainSplitter.setResizeWeight(1d);

        setLayout(new BorderLayout());
        add(mainSplitter, BorderLayout.CENTER);
    }

    private static void tweakSplitPaneUI(JSplitPane splitPane) {
        splitPane.setOpaque(false);
        splitPane.setBorder(null);
        splitPane.setDividerSize(3);

        if (!(splitPane.getUI() instanceof BasicSplitPaneUI)) {
            return;
        }

        BasicSplitPaneDivider divider = ((BasicSplitPaneUI) splitPane.getUI()).getDivider();

        if (divider != null) {
            divider.setBorder(null);
        }
    }


    public static class ResultsUI extends JTitledPanel {

        private OQLController.ResultsController resultsController;
        private HTMLTextArea resultsArea;

        private static Icon ICON = Icons.getIcon(HeapWalkerIcons.PROPERTIES);

        public ResultsUI(OQLController.ResultsController resultsController) {
            super(Bundle.OQLControllerUI_QueryResultsCaption(), ICON, true);
            this.resultsController = resultsController;
            initComponents();
        }


        public void setResult(String result) {
            resultsArea.setText(result);
            try { resultsArea.setCaretPosition(0); } catch (Exception e) {}
            setVisible(true);
        }


        private void initComponents() {
            resultsArea = new HTMLTextArea() {
                protected void showURL(URL url) {
                    resultsController.showURL(url);
                }
            };

            JScrollPane resultsAreaScroll = new JScrollPane(resultsArea,
                                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            resultsAreaScroll.setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5,
                                            UIUtils.getProfilerResultsBackground()));
            resultsAreaScroll.setViewportBorder(BorderFactory.createEmptyBorder());
            resultsAreaScroll.getVerticalScrollBar().setUnitIncrement(10);
            resultsAreaScroll.getHorizontalScrollBar().setUnitIncrement(10);

            JPanel contentsPanel = new JPanel();
            contentsPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, getTitleBorderColor()));
            contentsPanel.setLayout(new BorderLayout());
            contentsPanel.setOpaque(true);
            contentsPanel.setBackground(resultsArea.getBackground());
            contentsPanel.add(resultsAreaScroll, BorderLayout.CENTER);

            setLayout(new BorderLayout());
            add(contentsPanel, BorderLayout.CENTER);
        }

    }

    public static class QueryUI extends JTitledPanel {

        private OQLController.QueryController queryController;
        private OQLEditor editor;

        private JButton runButton;
        private JButton saveButton;
        private JButton cancelButton;
        private JLabel progressLabel;
        private JProgressBar progressBar;
        private JPanel controlPanel;
        private JPanel progressPanel;
        private JPanel contentsPanel;

        private boolean queryValid = true;

        private static Icon ICON = Icons.getIcon(HeapWalkerIcons.RULES);

        public QueryUI(OQLController.QueryController queryController, OQLEngine engine) {
            super(Bundle.OQLControllerUI_QueryEditorCaption(), ICON, true);

            this.queryController = queryController;

            initComponents(engine);
            
            addHierarchyListener(new HierarchyListener() {
                public void hierarchyChanged(HierarchyEvent e) {
                    if ((e.getChangeFlags() & HierarchyEvent.SHOWING_CHANGED) != 0) {
                        if (isShowing()) {
                            updateUIState();
                            QueryUI.this.removeHierarchyListener(this);
                        }
                    }
                }
            });
        }


        public void setQuery(String query) {
            setVisible(true);
            editor.setScript(query);
            editor.requestFocus();
        }

        public void queryStarted(final BoundedRangeModel model) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    updateUIState();
                    progressLabel.setText(Bundle.OQLControllerUI_ExecutingQueryMsg());
                    progressBar.setModel(model);
                    progressBar.setMaximumSize(new Dimension(progressBar.getMaximumSize().width,
                                                             progressBar.getPreferredSize().height));
                    contentsPanel.remove(controlPanel);
                    contentsPanel.add(progressPanel, BorderLayout.SOUTH);
                    progressPanel.invalidate();
                    contentsPanel.revalidate();
                    contentsPanel.repaint();
                }
            });
        }

        public void queryFinished() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    updateUIState();
                    contentsPanel.remove(progressPanel);
                    contentsPanel.add(controlPanel, BorderLayout.SOUTH);
                    controlPanel.invalidate();
                    contentsPanel.revalidate();
                    contentsPanel.repaint();
                }
            });
        }

        private void addToQuery(char ch) {
            setVisible(true);
            String chs = new String(new char[] { ch });
            editor.setScript(editor.getScript() + chs);
        }

        @Override
        public void requestFocus() {
            editor.requestFocus();
        }

        private void updateUIState() {
            if (queryController.getOQLController().isQueryRunning()) {
                runButton.setEnabled(false);
                editor.setEditable(false);
                editor.setEnabled(false);
            } else {
                runButton.setEnabled(editor.getScript().length() > 0 && queryValid);
                saveButton.setEnabled(editor.getScript().length() > 0 && queryValid);
                editor.setEditable(true);
                editor.setEnabled(true);
            }
        }

        private void executeQuery() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    requestFocus();
                    queryController.getOQLController().executeQuery(editor.getScript());
                }
            });
        }

        private void saveQuery() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    queryController.getOQLController().getSavedController().
                                                    saveQuery(editor.getScript());
                    editor.requestFocus();
                }
            });
        }

        private void cancelQuery() {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    queryController.getOQLController().cancelQuery();
                }
            });
        }


        private void initComponents(OQLEngine engine) {
            editor = new OQLEditor(engine);
            editor.addPropertyChangeListener(OQLEditor.VALIDITY_PROPERTY, new PropertyChangeListener() {

                public void propertyChange(PropertyChangeEvent evt) {
                    queryValid = (Boolean)evt.getNewValue();
                    updateUIState();
                }
            });
            editor.setBackground(UIUtils.getProfilerResultsBackground());

            JScrollPane editorScroll = new JScrollPane(editor,
                                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            editorScroll.setBorder(BorderFactory.createMatteBorder(5, 5, 0, 5,
                                        UIUtils.getProfilerResultsBackground()));
            editorScroll.setViewportBorder(BorderFactory.createEmptyBorder());
            editorScroll.getVerticalScrollBar().setUnitIncrement(10);
            editorScroll.getHorizontalScrollBar().setUnitIncrement(10);

            runButton = new JButton() {
                protected void fireActionPerformed(ActionEvent e) { executeQuery(); }
            };
            Mnemonics.setLocalizedText(runButton, Bundle.OQLControllerUI_ExecuteButtonText());
            runButton.getAccessibleContext().setAccessibleDescription(Bundle.OQLControllerUI_ExecuteButtonAccessDescr());
            saveButton = new JButton() {
                 protected void fireActionPerformed(ActionEvent e) { saveQuery(); }
            };
            Mnemonics.setLocalizedText(saveButton, Bundle.OQLControllerUI_SaveButtonText());
            saveButton.getAccessibleContext().setAccessibleDescription(Bundle.OQLControllerUI_SaveButtonAccessDescr());

            controlPanel = new JPanel(new BorderLayout(5, 5));
            controlPanel.setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5,
                                        UIUtils.getProfilerResultsBackground()));
            controlPanel.setOpaque(false);
            controlPanel.add(saveButton, BorderLayout.WEST);
            controlPanel.add(runButton, BorderLayout.EAST);

            progressBar = new JProgressBar(JProgressBar.HORIZONTAL);
            progressLabel = new JLabel();
            progressLabel.setLabelFor(progressBar);
            cancelButton = new JButton() {
                 protected void fireActionPerformed(ActionEvent e) { cancelQuery(); }
            };
            Mnemonics.setLocalizedText(cancelButton, Bundle.OQLControllerUI_CancelButtonText());
            cancelButton.getAccessibleContext().setAccessibleDescription(Bundle.OQLControllerUI_CancelButtonAccessDescr());

            progressPanel = new JPanel(new GridBagLayout());
            progressPanel.setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5,
                                        UIUtils.getProfilerResultsBackground()));
            progressPanel.setOpaque(false);
            GridBagConstraints c;

            c = new GridBagConstraints();
            c.anchor = GridBagConstraints.WEST;
            c.insets = new Insets(0, 0, 0, 10);
            progressPanel.add(progressLabel, c);

            c = new GridBagConstraints();
            c.weightx = 1;
            c.anchor = GridBagConstraints.CENTER;
            c.fill = GridBagConstraints.HORIZONTAL;
            c.insets = new Insets(0, 0, 0, 10);
            progressPanel.add(progressBar, c);

            c = new GridBagConstraints();
            c.weighty = 1;
            c.anchor = GridBagConstraints.EAST;
            c.insets = new Insets(0, 0, 0, 0);
            progressPanel.add(cancelButton, c);

            contentsPanel = new JPanel(new BorderLayout());
            contentsPanel.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, getTitleBorderColor()));
            contentsPanel.setBackground(UIUtils.getProfilerResultsBackground());
            contentsPanel.setOpaque(true);
            contentsPanel.add(editorScroll, BorderLayout.CENTER);
            contentsPanel.add(controlPanel, BorderLayout.SOUTH);

            setLayout(new BorderLayout());
            add(contentsPanel, BorderLayout.CENTER);

            getInputMap(QueryUI.WHEN_IN_FOCUSED_WINDOW).put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0), "CANCEL_ACTION"); // NOI18N
            getActionMap().put("CANCEL_ACTION", new AbstractAction() {// NOI18N
                public void actionPerformed(ActionEvent e) {
                    cancelQuery();
                }
            });

            editorScroll.setPreferredSize(new Dimension(1, 220));
        }

    }

    public static class SavedUI extends JTitledPanel {

        private OQLController.SavedController savedController;

        private JTree savedTree;
        private JScrollPane savedTreeScroll;
        private JButton openButton;
        private JButton editButton;
        private JButton deleteButton;
        private JTextArea descriptionArea;
        private JPanel contentsPanel;
        private JPanel loadingMsgPanel;
        private JPanel noQueriesMsgPanel;

        private OQLSupport.OQLTreeModel treeModel;

        private boolean queriesLoaded = false;


        private static Icon ICON = Icons.getIcon(HeapWalkerIcons.SAVED_OQL_QUERIES);

        public SavedUI(OQLController.SavedController savedController) {
            super(Bundle.OQLControllerUI_SavedQueriesCaption(), ICON, true);

            this.savedController = savedController;

            treeModel = OQLSupport.createModel();

            initComponents();
            refreshQueries();

            BrowserUtils.performTask(new Runnable() {
                public void run() {
                    OQLController.SavedController.loadData(treeModel);
                    SwingUtilities.invokeLater(new Runnable() {
                        public void run() {
                            queriesLoaded = true;
                            initializeQueries();
                            refreshQueries();
                        }
                    });
                }
            });
        }


        public void saveQuery(final String query) {
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    if (OQLQueryCustomizer.saveQuery(query, treeModel, savedTree)) {
                        setVisible(true);
                        refreshQueries();
                        BrowserUtils.performTask(new Runnable() {
                            public void run() {
                                OQLController.SavedController.saveData(treeModel);
                            }
                        });
                    }
                }
            });
        }


        private void openQuery() {
            TreePath[] selection = savedTree.getSelectionPaths();
            if (supportsOpen(selection)) {
                OQLSupport.OQLQueryNode node =
                        (OQLSupport.OQLQueryNode)node(selection[0]);
                savedController.getOQLController().getQueryController().
                        setQuery(node.getUserObject().getScript());
            }
        }

        private void editQuery() {
            TreePath[] selection = savedTree.getSelectionPaths();
            if (supportsProperties(selection)) {
                OQLSupport.OQLNode node = node(selection[0]);
                if (OQLQueryCustomizer.editNode(node, treeModel, savedTree)) {
                    refreshDescription();
                    BrowserUtils.performTask(new Runnable() {
                        public void run() {
                            OQLController.SavedController.saveData(treeModel);
                        }
                    });
                }
                editButton.requestFocus();
            }
        }

        private void deleteQueries() {
            TreePath[] selection = savedTree.getSelectionPaths();
            if (supportsDelete(selection)) {
                DefaultMutableTreeNode otherNode =
                        node(savedTree.getLeadSelectionPath()).getPreviousSibling();
                if (otherNode == null) otherNode =
                        node(savedTree.getAnchorSelectionPath()).getNextSibling();
                if (otherNode == null) otherNode = treeModel.customCategory();
                for (TreePath path : selection)
                    treeModel.removeNodeFromParent(node(path));
                if (!treeModel.hasCustomQueries())
                    treeModel.nodeStructureChanged(treeModel.customCategory());
                savedTree.setSelectionPath(new TreePath(treeModel.getPathToRoot(otherNode)));
                refreshQueries();
                savedTree.requestFocus();
                BrowserUtils.performTask(new Runnable() {
                    public void run() {
                        OQLController.SavedController.saveData(treeModel);
                    }
                });
            }
        }


        public void initializeQueries() {
            savedTree.expandPath(new TreePath(treeModel.getPathToRoot(
                    treeModel.customCategory())));
            if (!treeModel.hasCustomQueries() && treeModel.hasDefinedCategories())
                savedTree.expandRow(2);
        }

        private void refreshQueries() {
            Component currentContents =
                    ((BorderLayout)contentsPanel.getLayout()).
                    getLayoutComponent(BorderLayout.CENTER);

            if (queriesLoaded) {
                OQLSupport.OQLNode node = (OQLSupport.OQLNode)treeModel.getRoot();
                if (node.getChildCount() == 0) {
                    if (currentContents != noQueriesMsgPanel) {
                        if (currentContents != null) contentsPanel.remove(currentContents);
                        contentsPanel.add(noQueriesMsgPanel, BorderLayout.CENTER);
                        noQueriesMsgPanel.invalidate();
                        contentsPanel.revalidate();
                        contentsPanel.repaint();
                    }
                } else {
                    if (currentContents != savedTreeScroll) {
                        if (currentContents != null) contentsPanel.remove(currentContents);
                        contentsPanel.add(savedTreeScroll, BorderLayout.CENTER);
                        savedTreeScroll.invalidate();
                        contentsPanel.revalidate();
                        contentsPanel.repaint();
                    }
                }
            } else {
                contentsPanel.add(loadingMsgPanel, BorderLayout.CENTER);
                loadingMsgPanel.invalidate();
                contentsPanel.revalidate();
                contentsPanel.repaint();
            }
        }

        private void refreshButtons() {
            TreePath[] selection = savedTree.getSelectionPaths();
            openButton.setEnabled(supportsOpen(selection));
            editButton.setEnabled(supportsProperties(selection));
            deleteButton.setEnabled(supportsDelete(selection));
        }

        private static boolean supportsOpen(TreePath[] selection) {
            if (selection == null || selection.length != 1) return false;
            return node(selection[0]).supportsOpen();
        }

        private static boolean supportsProperties(TreePath[] selection) {
            if (selection == null || selection.length != 1) return false;
            return node(selection[0]).supportsProperties();
        }

        private static boolean supportsDelete(TreePath[] selection) {
            if (selection == null || selection.length == 0) return false;
            for (TreePath path : selection)
                if (!node(path).supportsDelete())
                    return false;
            return true;
        }

        private static OQLSupport.OQLNode node(TreePath selection) {
            return (OQLSupport.OQLNode)selection.getLastPathComponent();
        }

        private void refreshDescription() {
            TreePath[] selection = savedTree.getSelectionPaths();
            String description = null;
            boolean showDescr = selection != null && selection.length == 1;
            if (showDescr) description = node(selection[0]).getDescription();
            if (description != null) {
                descriptionArea.setText(description);
                descriptionArea.setVisible(true);
            } else {
                descriptionArea.setVisible(false);
            }
            
        }


        private void initComponents() {
            setLayout(new BorderLayout());

            savedTree = new JTree(treeModel);
            savedTree.setRowHeight(UIUtils.getDefaultRowHeight() + 2);
            savedTree.setRootVisible(false);
            savedTree.setShowsRootHandles(true);
            DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer();
            renderer.setLeafIcon(null);
            savedTree.setCellRenderer(renderer);
            savedTree.addTreeSelectionListener(new TreeSelectionListener() {
                public void valueChanged(TreeSelectionEvent e) {
                    refreshButtons();
                    refreshDescription();
                }
            });
            


            savedTreeScroll = new JScrollPane(savedTree,
                                    JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                    JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
            savedTreeScroll.setBorder(BorderFactory.createMatteBorder(5, 5, 5, 5,
                                            savedTree.getBackground()));
            savedTreeScroll.setViewportBorder(BorderFactory.createEmptyBorder());

            loadingMsgPanel = new JPanel(new BorderLayout());
            loadingMsgPanel.setOpaque(false);
            JLabel loadingMsgLabel = new JLabel(Bundle.OQLControllerUI_LoadingQueriesMsg(), JLabel.CENTER);
            loadingMsgLabel.setEnabled(false);
            loadingMsgPanel.add(loadingMsgLabel, BorderLayout.CENTER);

            noQueriesMsgPanel = new JPanel(new BorderLayout());
            noQueriesMsgPanel.setOpaque(false);
            JLabel noQueriesMsgLabel = new JLabel(Bundle.OQLControllerUI_NoSavedQueriesMsg(), JLabel.CENTER);
            noQueriesMsgLabel.setEnabled(false);
            noQueriesMsgPanel.add(noQueriesMsgLabel, BorderLayout.CENTER);

            openButton = new JButton() {
                 protected void fireActionPerformed(ActionEvent e) { openQuery(); }
            };
            Mnemonics.setLocalizedText(openButton, Bundle.OQLControllerUI_OpenButtonText());
            openButton.getAccessibleContext().
                            setAccessibleDescription(Bundle.OQLControllerUI_OpenButtonAccessDescr());
            editButton = new JButton() {
                 protected void fireActionPerformed(ActionEvent e) { editQuery(); }
            };
            Mnemonics.setLocalizedText(editButton, Bundle.OQLControllerUI_PropertiesButtonText());
            editButton.getAccessibleContext().
                            setAccessibleDescription(Bundle.OQLControllerUI_PropertiesButtonAccessDescr());
            deleteButton = new JButton() {
                 protected void fireActionPerformed(ActionEvent e) { deleteQueries(); }
            };
            Mnemonics.setLocalizedText(deleteButton, Bundle.OQLControllerUI_DeleteButtonText());
            deleteButton.getAccessibleContext().
                            setAccessibleDescription(Bundle.OQLControllerUI_DeleteButtonAccessDescr());

            JPanel editContainer = new JPanel(new FlowLayout(FlowLayout.LEADING, 5, 0));
            editContainer.setOpaque(false);
            editContainer.add(editButton);
            editContainer.add(deleteButton);

            JPanel controlPanel = new JPanel(new BorderLayout(5, 5));
            controlPanel.setBorder(BorderFactory.createMatteBorder(5, 0, 5, 5,
                                        savedTree.getBackground()));
            controlPanel.setOpaque(false);
            controlPanel.add(editContainer, BorderLayout.WEST);
            controlPanel.add(openButton, BorderLayout.EAST);

            descriptionArea = new JTextArea();
            descriptionArea.setOpaque(true);
            descriptionArea.setEnabled(false);
            descriptionArea.setLineWrap(true);
            descriptionArea.setWrapStyleWord(true);
            descriptionArea.setDisabledTextColor(UIManager.getColor("ToolTip.foreground")); // NOI18N
            descriptionArea.setBackground(UIManager.getColor("ToolTip.background")); // NOI18N
            descriptionArea.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 5, 0, 5,
                                        savedTree.getBackground()),
                    BorderFactory.createMatteBorder(5, 5, 5, 5,
                                        UIManager.getColor("ToolTip.background")))); // NOI18N

            JPanel bottomPanel = new JPanel(new BorderLayout());
            bottomPanel.setOpaque(false);
            bottomPanel.add(descriptionArea, BorderLayout.CENTER);
            bottomPanel.add(controlPanel, BorderLayout.SOUTH);

            contentsPanel = new JPanel();
            contentsPanel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, getTitleBorderColor()));
            contentsPanel.setLayout(new BorderLayout());
            contentsPanel.setOpaque(true);
            contentsPanel.setBackground(UIUtils.getProfilerResultsBackground());
            contentsPanel.add(bottomPanel, BorderLayout.SOUTH);

            setLayout(new BorderLayout());
            add(contentsPanel, BorderLayout.CENTER);

            refreshButtons();
            refreshDescription();

            savedTree.getInputMap().put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "OPEN_QUERY_ACTION"); // NOI18N
            savedTree.getActionMap().put("OPEN_QUERY_ACTION", new AbstractAction() {// NOI18N
                public void actionPerformed(ActionEvent e) {
                    openQuery();
                }
            });
            savedTree.getInputMap().put(
                    KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, 0), "DELETE_QUERY_ACTION"); // NOI18N
            savedTree.getActionMap().put("DELETE_QUERY_ACTION", new AbstractAction() {// NOI18N
                public void actionPerformed(ActionEvent e) {
                    deleteQueries();
                }
            });
            savedTree.addMouseListener(new MouseAdapter() {
                public void mouseClicked(MouseEvent e) {
                    if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2)
                        openQuery();
                }
            });

            savedTreeScroll.setPreferredSize(new Dimension(
                    openButton.getPreferredSize().width +
                    editButton.getPreferredSize().width +
                    deleteButton.getPreferredSize().width + 90, 220));
        }

    }

}
