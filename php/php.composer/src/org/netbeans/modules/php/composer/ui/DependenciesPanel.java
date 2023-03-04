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
package org.netbeans.modules.php.composer.ui;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import javax.swing.AbstractAction;
import javax.swing.AbstractListModel;
import javax.swing.ComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextField;
import javax.swing.JTextPane;
import javax.swing.KeyStroke;
import javax.swing.LayoutStyle;
import javax.swing.ListCellRenderer;
import javax.swing.ListSelectionModel;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListDataEvent;
import javax.swing.event.ListDataListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.composer.commands.Composer;
import org.netbeans.modules.php.composer.output.model.ComposerPackage;
import org.netbeans.modules.php.composer.output.model.SearchResult;
import org.netbeans.modules.php.composer.ui.options.ComposerOptionsPanelController;
import org.openide.awt.Mnemonics;
import org.openide.util.ChangeSupport;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * UI for Composer search command.
 */
public final class DependenciesPanel extends JPanel {

    private static final long serialVersionUID = -54676446789654L;

    private static final SearchResult SEARCHING_SEARCH_RESULT = new SearchResult(null, null);
    private static final SearchResult NO_RESULTS_SEARCH_RESULT = new SearchResult(null, null);

    private final PhpModule phpModule;
    private final List<SearchResult> searchResults = Collections.synchronizedList(new ArrayList<SearchResult>());
    // @GuardedBy("EDT")
    private final ResultsListModel resultsModel = new ResultsListModel(searchResults);
    private final ConcurrentMap<String, String> resultDetails = new ConcurrentHashMap<>();
    // @GuardedBy("EDT")
    private final VersionComboBoxModel versionsModel = new VersionComboBoxModel();
    // tasks
    private final RequestProcessor postSearchRequestProcessor = new RequestProcessor(DependenciesPanel.class.getName() + " (POST SEARCH)"); // NOI18N
    private final RequestProcessor postShowRequestProcessor = new RequestProcessor(DependenciesPanel.class.getName() + " (POST SHOW)"); // NOI18N
    private final List<Future<Integer>> searchTasks = new CopyOnWriteArrayList<>();
    private final List<Future<Integer>> showTasks = new CopyOnWriteArrayList<>();
    private final ChangeSupport changeSupport = new ChangeSupport(this);


    private DependenciesPanel(@NullAllowed PhpModule phpModule) {

        this.phpModule = phpModule;

        initComponents();
        init();
    }

    public static DependenciesPanel create() {
        return create(null);
    }

    public static DependenciesPanel create(PhpModule phpModule) {
        return new DependenciesPanel(phpModule);
    }

    public void setDefaultAction(Dialog dialog, final Runnable defaultResultsListTask) {
        if (!(dialog instanceof JDialog)) {
            return;
        }
        JRootPane rootPane = ((JDialog) dialog).getRootPane();
        rootPane.getInputMap(JComponent.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT).put(KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0), "enter"); // NOI18N
        rootPane.getActionMap().put("enter", new AbstractAction() { // NOI18N
            private static final long serialVersionUID = -687845132165467L;
            @Override
            public void actionPerformed(ActionEvent e) {
                assert EventQueue.isDispatchThread();
                if (tokenTextField.hasFocus()) {
                    if (searchButton.isEnabled()) {
                        searchButton.doClick();
                    }
                } else if (resultsList.hasFocus()) {
                    defaultResultsListTask.run();
                }
            }
        });
    }

    public void addChangeListener(ChangeListener listener) {
        changeSupport.addChangeListener(listener);
    }

    public void removeChangeListener(ChangeListener listener) {
        changeSupport.removeChangeListener(listener);
    }

    @CheckForNull
    public ComposerPackage getComposerPackage() {
        SearchResult selectedSearchResult = getSelectedSearchResult();
        String selectedVersion = getSelectedResultVersion();
        if (selectedSearchResult == null
                || selectedVersion == null) {
            return null;
        }
        return new ComposerPackage(selectedSearchResult.getName(), selectedVersion);
    }

    @Override
    public void removeNotify() {
        super.removeNotify();
        cleanUp();
    }

    private void init() {
        initSearch();
        initResults();
        initVersions();
    }

    private void initSearch() {
        enableSearchButton();
        // listeners
        tokenTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                processChange();
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                processChange();
            }
            @Override
            public void changedUpdate(DocumentEvent e) {
                processChange();
            }
            private void processChange() {
                enableSearchButton();
            }
        });
    }

    private void initResults() {
        // split pane
        outputSplitPane.setDividerLocation(0.5);
        // results
        resultsList.setModel(resultsModel);
        resultsList.setCellRenderer(new ResultListCellRenderer());
        // details
        updateResultDetailsAndVersions(false);
        // listeners
        resultsList.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
            @Override
            public void valueChanged(ListSelectionEvent e) {
                resultsChanged();
            }
        });
        resultsModel.addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent e) {
                processChange();
            }
            @Override
            public void intervalRemoved(ListDataEvent e) {
                processChange();
            }
            @Override
            public void contentsChanged(ListDataEvent e) {
                processChange();
            }
            private void processChange() {
                resultsChanged();
            }
        });
    }

    private void initVersions() {
        versionComboBox.setModel(versionsModel);
        // listeners
        versionComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(ItemEvent e) {
                versionChanged();
            }
        });
        versionsModel.addListDataListener(new ListDataListener() {
            @Override
            public void intervalAdded(ListDataEvent e) {
                processChange();
            }
            @Override
            public void intervalRemoved(ListDataEvent e) {
                processChange();
            }
            @Override
            public void contentsChanged(ListDataEvent e) {
                processChange();
            }
            private void processChange() {
                versionChanged();
            }
        });
    }

    void resultsChanged() {
        updateResultDetailsAndVersions(false);
        fireChange();
    }

    void versionChanged() {
        fireChange();
    }

    void enableSearchButton() {
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                assert EventQueue.isDispatchThread();
                searchButton.setEnabled(StringUtils.hasText(tokenTextField.getText()));
            }
        });
    }

    void clearSearchResults() {
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                assert EventQueue.isDispatchThread();
                searchResults.clear();
                resultsModel.fireContentsChanged();
            }
        });
    }

    void addSearchResult(final SearchResult searchResult) {
        addSearchResult(searchResult, false);
    }

    void addSearchResult(final SearchResult searchResult, final boolean select) {
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                assert EventQueue.isDispatchThread();
                searchResults.add(searchResult);
                resultsModel.fireContentsChanged();
                if (select) {
                    resultsList.setSelectedValue(searchResult, true);
                }
            }
        });
    }

    void removeSearchResult(final SearchResult searchResult) {
        Mutex.EVENT.readAccess(new Runnable() {
            @Override
            public void run() {
                assert EventQueue.isDispatchThread();
                if (searchResults.remove(searchResult)) {
                    resultsModel.fireContentsChanged();
                }
            }
        });
    }

    void updateResultDetailsAndVersions(boolean fetchDetails) {
        assert EventQueue.isDispatchThread();
        String msg = ""; // NOI18N
        List<String> versions = null;
        SearchResult selectedSearchResult = getSelectedSearchResult();
        if (selectedSearchResult != null) {
            String name = selectedSearchResult.getName();
            String details = getResultsDetails(name, fetchDetails);
            if (details != null) {
                msg = details;
            }
            versions = getResultVersions(name);
        }
        detailsTextPane.setText(msg);
        detailsTextPane.setCaretPosition(0);
        if (versions == null) {
            versionsModel.setNoVersions();
        } else {
            versionsModel.setVersions(versions);
        }
    }

    @NbBundle.Messages("DependenciesPanel.details.loading=Loading package details...")
    private String getResultsDetails(final String resultName, boolean fetchDetails) {
        if (resultName == null) {
            return null;
        }
        String details = resultDetails.get(resultName);
        if (details != null) {
            return details;
        }
        if (!fetchDetails) {
            return null;
        }
        final Composer composer = getComposer();
        if (composer == null) {
            return null;
        }
        String loading = Bundle.DependenciesPanel_details_loading();
        String prev = resultDetails.putIfAbsent(resultName, loading);
        assert prev == null : "Previous message found?!: " + prev;
        postShowRequestProcessor.post(new Runnable() {
            @Override
            public void run() {
                final StringBuffer buffer = new StringBuffer(200);
                Future<Integer> task = composer.show(phpModule, resultName, new Composer.OutputProcessor<String>() {
                    @Override
                    public void process(String chunk) {
                        buffer.append(chunk);
                    }
                });
                if (task == null) {
                    // cleanup => remove loading message
                    resultDetails.remove(resultName);
                } else {
                    showTasks.add(task);
                    runWhenTaskFinish(task, null, new Runnable() {
                        @Override
                        public void run() {
                            if (buffer.length() == 0) {
                                // no output => remove loading message
                                resultDetails.remove(resultName);
                            } else {
                                resultDetails.put(resultName, buffer.toString());
                                EventQueue.invokeLater(new Runnable() {
                                    @Override
                                    public void run() {
                                        updateResultDetailsAndVersions(false);
                                    }
                                });
                            }
                        }
                    });
                }
            }
        });
        return loading;
    }

    @CheckForNull
    private List<String> getResultVersions(String resultName) {
        if (resultName == null) {
            return null;
        }
        String details = resultDetails.get(resultName);
        if (details == null) {
            // not fetched yet
            return null;
        }
        return VersionsParser.parse(details);
    }

    @CheckForNull
    SearchResult getSelectedSearchResult() {
        assert EventQueue.isDispatchThread();
        Object selectedValue = resultsList.getSelectedValue();
        if (selectedValue == null
                || selectedValue == SEARCHING_SEARCH_RESULT
                || selectedValue == NO_RESULTS_SEARCH_RESULT) {
            return null;
        }
        return (SearchResult) selectedValue;
    }

    @CheckForNull
    private String getSelectedResultVersion() {
        assert EventQueue.isDispatchThread();
        Object selectedVersion = versionsModel.getSelectedItem();
        if (selectedVersion == VersionComboBoxModel.NO_VERSIONS_AVAILABLE) {
            return null;
        }
        return (String) selectedVersion;
    }

    private void fireChange() {
        changeSupport.fireChange();
    }

    @NbBundle.Messages("DependenciesPanel.error.composer.notValid=Composer is not valid.")
    @CheckForNull
    Composer getComposer() {
        try {
            return Composer.getDefault();
        } catch (InvalidPhpExecutableException ex) {
            UiUtils.invalidScriptProvided(Bundle.DependenciesPanel_error_composer_notValid(), ComposerOptionsPanelController.OPTIONS_SUBPATH);
        }
        return null;
    }

    private void cleanUp() {
        postSearchRequestProcessor.shutdownNow();
        postShowRequestProcessor.shutdownNow();
        cancelTasks(searchTasks);
        cancelTasks(showTasks);
    }

    private void cancelTasks(List<Future<Integer>> tasks) {
        ArrayList<Future<Integer>> tasksCopy = new ArrayList<>(tasks);
        for (Future<Integer> task : tasksCopy) {
            assert task != null;
            task.cancel(true);
        }
        tasks.removeAll(tasksCopy);
    }

    void runWhenTaskFinish(Future<Integer> task, @NullAllowed Runnable postTask, @NullAllowed Runnable finalTask) {
        try {
            task.get(3, TimeUnit.MINUTES);
            if (postTask != null) {
                postTask.run();
            }
        } catch (TimeoutException ex) {
            task.cancel(true);
        } catch (CancellationException ex) {
            // noop, dialog is being closed
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
        } catch (ExecutionException ex) {
            UiUtils.processExecutionException(ex, ComposerOptionsPanelController.OPTIONS_SUBPATH);
        } finally {
            if (finalTask != null) {
                finalTask.run();
            }
        }
    }

    /**
     * This method is called from within the constructor to initialize the form. WARNING: Do NOT modify this code. The content of this method is always regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        tokenLabel = new JLabel();
        tokenTextField = new JTextField();
        onlyNameCheckBox = new JCheckBox();
        searchButton = new JButton();
        packagesLabel = new JLabel();
        outputSplitPane = new JSplitPane();
        resultsScrollPane = new JScrollPane();
        resultsList = new JList<SearchResult>();
        detailsScrollPane = new JScrollPane();
        detailsTextPane = new JTextPane();
        versionLabel = new JLabel();
        versionComboBox = new JComboBox<String>();

        Mnemonics.setLocalizedText(tokenLabel, NbBundle.getMessage(DependenciesPanel.class, "DependenciesPanel.tokenLabel.text")); // NOI18N

        Mnemonics.setLocalizedText(onlyNameCheckBox, NbBundle.getMessage(DependenciesPanel.class, "DependenciesPanel.onlyNameCheckBox.text")); // NOI18N

        Mnemonics.setLocalizedText(searchButton, NbBundle.getMessage(DependenciesPanel.class, "DependenciesPanel.searchButton.text")); // NOI18N
        searchButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                searchButtonActionPerformed(evt);
            }
        });

        Mnemonics.setLocalizedText(packagesLabel, NbBundle.getMessage(DependenciesPanel.class, "DependenciesPanel.packagesLabel.text")); // NOI18N

        outputSplitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
        outputSplitPane.setResizeWeight(0.5);

        resultsList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        resultsList.setMinimumSize(new Dimension(100, 50));
        resultsList.addMouseListener(new MouseAdapter() {
            public void mouseClicked(MouseEvent evt) {
                resultsListMouseClicked(evt);
            }
        });
        resultsScrollPane.setViewportView(resultsList);

        outputSplitPane.setLeftComponent(resultsScrollPane);

        detailsTextPane.setEditable(false);
        detailsTextPane.setFont(new Font("Monospaced", 0, 12)); // NOI18N
        detailsTextPane.setMinimumSize(new Dimension(100, 50));
        detailsScrollPane.setViewportView(detailsTextPane);

        outputSplitPane.setBottomComponent(detailsScrollPane);

        versionLabel.setLabelFor(versionComboBox);
        Mnemonics.setLocalizedText(versionLabel, NbBundle.getMessage(DependenciesPanel.class, "DependenciesPanel.versionLabel.text")); // NOI18N

        GroupLayout layout = new GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                    .addComponent(outputSplitPane)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(tokenLabel)
                        .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(onlyNameCheckBox)
                                .addGap(0, 0, Short.MAX_VALUE))
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(tokenTextField)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(searchButton))))
                    .addGroup(layout.createSequentialGroup()
                        .addGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
                            .addComponent(packagesLabel)
                            .addGroup(layout.createSequentialGroup()
                                .addComponent(versionLabel)
                                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                                .addComponent(versionComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)))
                        .addGap(0, 0, Short.MAX_VALUE)))
                .addGap(0, 0, 0))
        );
        layout.setVerticalGroup(layout.createParallelGroup(GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGap(0, 0, 0)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(tokenLabel)
                    .addComponent(tokenTextField, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
                    .addComponent(searchButton))
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(onlyNameCheckBox)
                .addPreferredGap(LayoutStyle.ComponentPlacement.UNRELATED)
                .addComponent(packagesLabel)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(outputSplitPane)
                .addPreferredGap(LayoutStyle.ComponentPlacement.RELATED)
                .addGroup(layout.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(versionLabel)
                    .addComponent(versionComboBox, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
                .addGap(0, 0, 0))
        );
    }// </editor-fold>//GEN-END:initComponents

    private void searchButtonActionPerformed(ActionEvent evt) {//GEN-FIRST:event_searchButtonActionPerformed
        final Composer composer = getComposer();
        if (composer == null) {
            return;
        }
        cancelTasks(searchTasks);
        cancelTasks(showTasks);
        searchButton.setEnabled(false);
        clearSearchResults();
        addSearchResult(SEARCHING_SEARCH_RESULT);
        String token = tokenTextField.getText();
        boolean onlyName = onlyNameCheckBox.isSelected();
        final Future<Integer> task = composer.search(phpModule, token, onlyName, new Composer.OutputProcessor<SearchResult>() {
            private boolean first = true;

            @Override
            public void process(SearchResult item) {
                if (first) {
                    first = false;
                    clearSearchResults();
                    addSearchResult(item, true);
                    EventQueue.invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            updateResultDetailsAndVersions(true);
                        }
                    });
                } else {
                    addSearchResult(item);
                }
            }
        });
        if (task == null) {
            enableSearchButton();
        } else {
            searchTasks.add(task);
            postSearchRequestProcessor.post(new Runnable() {
                @Override
                public void run() {
                    runWhenTaskFinish(task, new Runnable() {
                        @Override
                        public void run() {
                            EventQueue.invokeLater(new Runnable() {
                                @Override
                                public void run() {
                                    removeSearchResult(SEARCHING_SEARCH_RESULT);
                                    if (searchResults.isEmpty()) {
                                        addSearchResult(NO_RESULTS_SEARCH_RESULT);
                                    }
                                }
                            });
                        }
                    }, new Runnable() {
                        @Override
                        public void run() {
                            enableSearchButton();
                        }
                    });
                }
            });
        }
    }//GEN-LAST:event_searchButtonActionPerformed

    private void resultsListMouseClicked(MouseEvent evt) {//GEN-FIRST:event_resultsListMouseClicked
        updateResultDetailsAndVersions(true);
    }//GEN-LAST:event_resultsListMouseClicked

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private JScrollPane detailsScrollPane;
    private JTextPane detailsTextPane;
    private JCheckBox onlyNameCheckBox;
    private JSplitPane outputSplitPane;
    private JLabel packagesLabel;
    private JList<SearchResult> resultsList;
    private JScrollPane resultsScrollPane;
    private JButton searchButton;
    private JLabel tokenLabel;
    private JTextField tokenTextField;
    private JComboBox<String> versionComboBox;
    private JLabel versionLabel;
    // End of variables declaration//GEN-END:variables

    //~ Inner classes

    private static final class ResultsListModel extends AbstractListModel<SearchResult> {

        private static final long serialVersionUID = -897454654321324564L;

        // @GuardedBy("EDT")
        private final List<SearchResult> searchResults;


        public ResultsListModel(List<SearchResult> searchResults) {
            assert EventQueue.isDispatchThread();
            this.searchResults = searchResults;
        }

        @Override
        public int getSize() {
            assert EventQueue.isDispatchThread();
            return searchResults.size();
        }

        @Override
        public SearchResult getElementAt(int index) {
            assert EventQueue.isDispatchThread();
            try {
                return searchResults.get(index);
            } catch (IndexOutOfBoundsException ex) {
                // can happen while clearing results
                return null;
            }
        }

        public void fireContentsChanged() {
            assert EventQueue.isDispatchThread();
            fireContentsChanged(this, 0, searchResults.size());
        }

    }

    private static final class ResultListCellRenderer implements ListCellRenderer<SearchResult> {

        private final ListCellRenderer<Object> defaultRenderer = new DefaultListCellRenderer();

        @NbBundle.Messages({
            "# {0} - name",
            "# {1} - description",
            "DependenciesPanel.results.result=<html><b>{0}</b>: {1}",
            "DependenciesPanel.results.searching=<html><i>Searching...</i>",
            "DependenciesPanel.results.noResults=<html><i>No results found.</i>"
        })
        @Override
        public Component getListCellRendererComponent(JList<? extends SearchResult> list, SearchResult value, int index, boolean isSelected, boolean cellHasFocus) {
            String label;
            SearchResult result = value;
            if (result == SEARCHING_SEARCH_RESULT) {
                label = Bundle.DependenciesPanel_results_searching();
            } else if (result == NO_RESULTS_SEARCH_RESULT) {
                label = Bundle.DependenciesPanel_results_noResults();
            } else {
                label = Bundle.DependenciesPanel_results_result(result.getName(), result.getDescription());
            }
            return defaultRenderer.getListCellRendererComponent(list, label, index, isSelected, cellHasFocus);
        }

    }

    private static final class VersionComboBoxModel extends AbstractListModel<String> implements ComboBoxModel<String> {

        private static final long serialVersionUID = -7546812657897987L;

        @NbBundle.Messages("VersionComboBoxModel.noVersions=<no versions available>")
        static final String NO_VERSIONS_AVAILABLE = Bundle.VersionComboBoxModel_noVersions();


        // @GuardedBy("EDT")
        private final List<String> versions = new ArrayList<>();

        private volatile String selectedVersion = null;


        public VersionComboBoxModel() {
            setNoVersions();
        }

        @Override
        public int getSize() {
            assert EventQueue.isDispatchThread();
            return versions.size();
        }

        @Override
        public String getElementAt(int index) {
            assert EventQueue.isDispatchThread();
            return versions.get(index);
        }

        @Override
        public void setSelectedItem(Object anItem) {
            selectedVersion = (String) anItem;
            fireContentsChanged();
        }

        @CheckForNull
        @Override
        public Object getSelectedItem() {
            return selectedVersion;
        }

        public void setNoVersions() {
            assert EventQueue.isDispatchThread();
            this.versions.clear();
            versions.add(NO_VERSIONS_AVAILABLE);
            selectedVersion = NO_VERSIONS_AVAILABLE;
            fireContentsChanged();
        }

        public void setVersions(List<String> versions) {
            assert EventQueue.isDispatchThread();
            this.versions.clear();
            this.versions.addAll(versions);
            if (!versions.isEmpty()) {
                selectedVersion = versions.get(0);
            }
            fireContentsChanged();
        }

        private void fireContentsChanged() {
            fireContentsChanged(this, 0, Integer.MAX_VALUE);
        }

    }

    private static final class VersionsParser  {

        private static final String VERSIONS_PREFIX = "versions : "; // NOI18N
        private static final String VERSIONS_DELIMITER = ", "; // NOI18N


        private VersionsParser() {
        }

        @CheckForNull
        public static List<String> parse(String details) {
            String versionsLine = getVersionLine(details);
            if (versionsLine == null) {
                return null;
            }
            return getVersions(versionsLine);
        }

        @CheckForNull
        private static String getVersionLine(String details) {
            for (String line : details.split("\n")) { // NOI18N
                line = line.trim();
                if (line.startsWith(VERSIONS_PREFIX)) {
                    return line.substring(VERSIONS_PREFIX.length());
                }
            }
            return null;
        }

        private static List<String> getVersions(String versionsLine) {
            List<String> versions = new ArrayList<>(StringUtils.explode(versionsLine, VERSIONS_DELIMITER));
            versions.add("*"); // NOI18N
            return versions;
        }

    }

}
