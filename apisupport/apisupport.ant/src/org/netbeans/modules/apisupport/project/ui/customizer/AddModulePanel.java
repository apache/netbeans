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
/*
 * Contributor(s): markiewb@netbeans.org
 */

package org.netbeans.modules.apisupport.project.ui.customizer;

import org.netbeans.modules.apisupport.project.ModuleDependency;
import java.awt.Color;
import java.awt.Dialog;
import java.awt.EventQueue;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.ActionMap;
import javax.swing.InputMap;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.KeyStroke;
import javax.swing.Timer;
import javax.swing.UIManager;
import javax.swing.event.DocumentEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import org.netbeans.modules.apisupport.project.api.UIUtil;
import org.netbeans.modules.apisupport.project.api.Util;
import org.netbeans.modules.apisupport.project.universe.NbPlatform;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.awt.HtmlBrowser;
import org.openide.util.HelpCtx;
import org.openide.util.Mutex;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Represents panel for adding new dependency for a module. Shown after
 * <em>Add</em> button on the <code>CustomizerLibraries</code> panel has been
 * pushed.
 *
 * @author Martin Krauskopf, Jesse Glick
 */
public final class AddModulePanel extends JPanel {
    
    private static final String FILTER_DESCRIPTION = getMessage("LBL_FilterDescription");
    private static final String ALL_CLUSTERS = getMessage("TEXT_SelectAllClusters");
    private static final RequestProcessor RP = new RequestProcessor(AddModulePanel.class.getName(), 2, true);
    private static Rectangle lastSize;
    
    private CustomizerComponentFactory.DependencyListModel universeModules;
    private Set<ModuleDependency> selectedDeps;
    private RequestProcessor.Task filterTask;
    private AddModuleFilter filterer;
    private URL currectJavadoc;
    
    private final Object IMPL_LOCK = new Object();
    
    private final SingleModuleProperties props;
    private Timer timer;
    
    public static ModuleDependency[] selectDependencies(final SingleModuleProperties props) {
        // keep backwards compatibility
        return selectDependencies(props, null);
    }

    /**
     * 
     * @param props
     * @param initialFilterText initial filter text or null if not given
     * @return 
     */
    public static ModuleDependency[] selectDependencies(final SingleModuleProperties props, final String initialFilterText) {
        final AddModulePanel addPanel;
        if (null != initialFilterText) {
            // init dialog with filter text
            addPanel = new AddModulePanel(props, initialFilterText);
        }
        else{
            // keep backwards compatibility
            addPanel = new AddModulePanel(props);
        }
        final DialogDescriptor descriptor = new DialogDescriptor(addPanel,
                getMessage("CTL_AddModuleDependencyTitle"));
        descriptor.setHelpCtx(new HelpCtx("org.netbeans.modules.apisupport.project.ui.customizer.AddModulePanel"));
        descriptor.setClosingOptions(new Object[0]);
        final Dialog d = DialogDisplayer.getDefault().createDialog(descriptor);
        descriptor.setButtonListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (DialogDescriptor.OK_OPTION.equals(e.getSource()) && addPanel.getSelectedDependencies().length == 0) {
                    return;
                }
                d.setVisible(false);
                d.dispose();
            }
        });
        if (lastSize != null) {
            d.setBounds(lastSize);
        }
        d.setVisible(true);
        lastSize = d.getBounds();
        d.dispose();
        if (descriptor.getValue().equals(DialogDescriptor.OK_OPTION)) {
            return addPanel.getSelectedDependencies();
        } else {
            return new ModuleDependency[0]; // #114932
        }
    }
    
    public AddModulePanel(final SingleModuleProperties props) {
        this(props, FILTER_DESCRIPTION);
    }

    private AddModulePanel(final SingleModuleProperties props, String initialString) {
        this.props = props;
        this.selectedDeps = new HashSet<ModuleDependency>();
        initComponents();
        initAccessibility();
        filterValue.setText(initialString);
        fillUpUniverseModules();
        fillUpUniverseClusters();
        moduleList.setCellRenderer(CustomizerComponentFactory.getDependencyCellRenderer(false));
        moduleList.addListSelectionListener(new ListSelectionListener() {
            public void valueChanged(ListSelectionEvent e) {
                showDescription();
                currectJavadoc = null;
                final ModuleDependency[] deps = getSelectedDependencies();
                if (deps.length == 1) {
                    final NbPlatform platform = props.getActivePlatform();
                    RP.post(new Runnable() {
                        @Override
                        public void run() {
                            currectJavadoc = deps[0].getModuleEntry().getJavadoc(platform);
                            showJavadocButton.setEnabled(currectJavadoc != null);
                        }
                    });
                } else {
                    showJavadocButton.setEnabled(false);
                }
            }
        });
        filterValue.getDocument().addDocumentListener(new UIUtil.DocumentAdapter() {
            public void insertUpdate(DocumentEvent e) {
                if (!FILTER_DESCRIPTION.equals(filterValue.getText())) {
                    search();
                }
            }
        });
        clusterFilterComboBox.addItemListener(new ItemListener() {
            @Override
            public void itemStateChanged(final ItemEvent e) {
                filterValue.setEnabled(false);
                moduleList.setEnabled(false);
                showNonAPIModules.setEnabled(false);
                matchCaseValue.setEnabled(false);
                showExclModulesCheckBox.setEnabled(false);
                final String lastFilter = filterValue.getText();
                filterValue.setText(UIUtil.WAIT_VALUE);
                moduleList.setModel(UIUtil.createListWaitModel());
                final String selectedClusterStr = String.valueOf(clusterFilterComboBox.getSelectedItem());
                final Set<ModuleDependency> universeDeps = universeModules.getDependencies();
                synchronized (IMPL_LOCK) {
                    selectedDeps = new HashSet<ModuleDependency>();
                    if(!ALL_CLUSTERS.equals(selectedClusterStr)) {
                        for(ModuleDependency dependencyIter:universeDeps) {
                            if(selectedClusterStr.equals(dependencyIter.getModuleEntry().getClusterDirectory().getName())) {
                                selectedDeps.add(dependencyIter);
                            }
                        }
                    } else {
                        selectedDeps.addAll(universeDeps);
                    }
                }
                ModuleProperties.RP.post(new Runnable() {
                    public void run() {
                        EventQueue.invokeLater(new Runnable() {
                            public void run() {
                                synchronized (IMPL_LOCK) {
                                    moduleList.setModel(CustomizerComponentFactory.createSortedDependencyListModel(selectedDeps));
                                }
                                moduleList.setEnabled(true);
                                filterValue.setEnabled(true);
                                showNonAPIModules.setEnabled(true);
                                boolean enableExclModuleChckBox = props.isHasExcludedModules() && props.isSuiteComponent()?true:false;
                                showExclModulesCheckBox.setEnabled(enableExclModuleChckBox);
                                matchCaseValue.setEnabled(true);
                            }
                        });
                    }
                });
                filterValue.setText(lastFilter);
                if (!FILTER_DESCRIPTION.equals(lastFilter)) {
                    search();
                } else {
                    filterValue.selectAll();
                }
                filterValue.requestFocusInWindow();
            }
        });
        // Make basic navigation commands from the list work from the text field.
        String[][] listNavCommands = {
            { "selectPreviousRow", "selectPreviousRow" }, // NOI18N
            { "selectNextRow", "selectNextRow" }, // NOI18N
            { "selectFirstRow", "selectFirstRow" }, // NOI18N
            { "selectLastRow", "selectLastRow" }, // NOI18N
            { "scrollUp", "scrollUp" }, // NOI18N
            { "scrollDown", "scrollDown" }, // NOI18N
        };
        String[][] areaNavCommands = {
            { "selection-page-up", "page-up" },// NOI18N
            { "selection-page-down", "page-down" },// NOI18N
            { "selection-up", "caret-up" },// NOI18N
            { "selection-down", "caret-down" },// NOI18N
        };
        exchangeCommands(listNavCommands, moduleList, filterValue);
        exchangeCommands(areaNavCommands, descValue, filterValue);
    }
    
    private static void exchangeCommands(String[][] commandsToExchange,
            final JComponent target, final JComponent source) {
        InputMap targetBindings = target.getInputMap();
        KeyStroke[] targetBindingKeys = targetBindings.allKeys();
        ActionMap targetActions = target.getActionMap();
        InputMap sourceBindings = source.getInputMap();
        ActionMap sourceActions = source.getActionMap();
        for (int i = 0; i < commandsToExchange.length; i++) {
            String commandFrom = commandsToExchange[i][0];
            String commandTo = commandsToExchange[i][1];
            final Action orig = targetActions.get(commandTo);
            if (orig == null) {
                continue;
            }
            sourceActions.put(commandTo, new AbstractAction() {
                public void actionPerformed(ActionEvent e) {
                    orig.actionPerformed(new ActionEvent(target, e.getID(), e.getActionCommand(), e.getWhen(), e.getModifiers()));
                }
            });
            for (int j = 0; j < targetBindingKeys.length; j++) {
                if (targetBindings.get(targetBindingKeys[j]).equals(commandFrom)) {
                    sourceBindings.put(targetBindingKeys[j], commandTo);
                }
            }
        }
    }

    private void fillUpUniverseClusters() {
        final boolean nonApiDeps = showNonAPIModules.isSelected();
        final boolean exclModules = showExclModulesCheckBox.isSelected();
        final Object lastSelectedItem = clusterFilterComboBox.getSelectedItem();
        final List<String> allClustersList = new ArrayList<String>();
        ModuleProperties.RP.post(new Runnable() {
            public void run() {
                final Set<ModuleDependency> universeDeps = selectedDeps = props.getUniverseDependencies(!exclModules, !nonApiDeps);
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        allClustersList.add(ALL_CLUSTERS);
                        for(ModuleDependency dependencyIter:universeDeps) {
                            if(!allClustersList.contains(dependencyIter.getModuleEntry().getClusterDirectory().getName())) {
                                allClustersList.add(dependencyIter.getModuleEntry().getClusterDirectory().getName());
                            }
                        }
                        String [] allClusters = new String[allClustersList.size()];
                        allClustersList.toArray(allClusters);
                        clusterFilterComboBox.setModel(new javax.swing.DefaultComboBoxModel(allClusters));
                        for(int i=0; i<clusterFilterComboBox.getItemCount(); i++) {
                            if(clusterFilterComboBox.getItemAt(i).equals(lastSelectedItem)) {
                                clusterFilterComboBox.setSelectedIndex(i);
                            }
                        }
                    }
                });
            }
        });
    }

    private void fillUpUniverseModules() {
        filterValue.setEnabled(false);
        moduleList.setEnabled(false);
        showNonAPIModules.setEnabled(false);
        matchCaseValue.setEnabled(false);
        showExclModulesCheckBox.setEnabled(false);
        final String lastFilter = filterValue.getText();
        filterValue.setText(UIUtil.WAIT_VALUE);
        moduleList.setModel(UIUtil.createListWaitModel());
        final boolean nonApiDeps = showNonAPIModules.isSelected();
        final boolean exclModules = showExclModulesCheckBox.isSelected();
        ModuleProperties.RP.post(new Runnable() {
            public void run() {
                props.resetUniverseDependencies();  // #165300 refresh in case of added friends/public packages
                final Set<ModuleDependency> universeDeps = props.getUniverseDependencies(!exclModules, !nonApiDeps);
                EventQueue.invokeLater(new Runnable() {
                    public void run() {
                        universeModules = CustomizerComponentFactory.createSortedDependencyListModel(universeDeps);
                        filterer = null;
                        moduleList.setModel(universeModules);
                        moduleList.setEnabled(true);
                        filterValue.setEnabled(true);
                        showNonAPIModules.setEnabled(true);
                        matchCaseValue.setEnabled(true);
                        boolean enableExclModuleChckBox = props.isHasExcludedModules() && props.isSuiteComponent()?true:false;
                        showExclModulesCheckBox.setEnabled(enableExclModuleChckBox);
                        filterValue.setText(lastFilter);
                        if (!FILTER_DESCRIPTION.equals(lastFilter)) {
                            search();
                        } else {
                            filterValue.selectAll();
                        }
                        filterValue.requestFocusInWindow();
                    }
                });
            }
        });
    }
    
    private void showDescription() {
        StyledDocument doc = descValue.getStyledDocument();
        final Boolean matchCase = matchCaseValue.isSelected();
        try {
            doc.remove(0, doc.getLength());
            ModuleDependency[] deps = getSelectedDependencies();
            if (deps.length != 1) {
                return;
            }
            String longDesc = deps[0].getModuleEntry().getLongDescription();
            if (longDesc != null) {
                doc.insertString(0, longDesc, null);
            }
            String filterText = filterValue.getText().trim();
            if (filterText.length() != 0 && !FILTER_DESCRIPTION.equals(filterText)) {
                doc.insertString(doc.getLength(), "\n\n", null); // NOI18N
                Style bold = doc.addStyle(null, null);
                bold.addAttribute(StyleConstants.Bold, Boolean.TRUE);
                doc.insertString(doc.getLength(), getMessage("TEXT_matching_filter_contents"), bold);
                doc.insertString(doc.getLength(), "\n", null); // NOI18N
                if (filterText.length() > 0) {
                    String filterTextLC = matchCase?filterText:filterText.toLowerCase(Locale.US);
                    Style match = doc.addStyle(null, null);
                    match.addAttribute(StyleConstants.Background, UIManager.get("selection.highlight")!=null?
                            UIManager.get("selection.highlight"):new Color(246, 248, 139));
                    boolean isEven = false;
                    Style even = doc.addStyle(null, null);
                    even.addAttribute(StyleConstants.Background, UIManager.get("Panel.background"));
                    if (filterer == null) {
                        return; // #101776
                    }
                    for (String hit : filterer.getMatchesFor(filterText, deps[0])) {
                        int loc = doc.getLength();
                        doc.insertString(loc, hit, (isEven ? even : null));
                        int start = (matchCase?hit:hit.toLowerCase(Locale.US)).indexOf(filterTextLC);
                        while (start != -1) {
                            doc.setCharacterAttributes(loc + start, filterTextLC.length(), match, true);
                            start = hit.toLowerCase(Locale.US).indexOf(filterTextLC, start + 1);
                        }
                        doc.insertString(doc.getLength(), "\n", (isEven ? even : null)); // NOI18N
                        isEven ^= true;
                    }
                } else {
                    Style italics = doc.addStyle(null, null);
                    italics.addAttribute(StyleConstants.Italic, Boolean.TRUE);
                    doc.insertString(doc.getLength(), getMessage("TEXT_no_filter_specified"), italics);
                }
            }
            descValue.setCaretPosition(0);
        } catch (BadLocationException e) {
            Util.err.notify(ErrorManager.INFORMATIONAL, e);
        }
    }
    
    public ModuleDependency[] getSelectedDependencies() {
        ModuleDependency[] deps;
        if (UIUtil.isWaitModel(moduleList.getModel())) {
            deps = new ModuleDependency[0];
        } else {
            Object[] objects = moduleList.getSelectedValues();
            deps = new ModuleDependency[objects.length];
            System.arraycopy(objects, 0, deps, 0, objects.length);
        }
        return deps;
    }

    public @Override void removeNotify() {
        super.removeNotify();
        cancelFilterTask();
    }
    
    private synchronized void cancelFilterTask() {
        if (filterTask != null) {
            filterTask.cancel();
            filterTask = null;
            filterer = null;
        }
    }

    private void search() {
        cancelFilterTask();
        final String text = filterValue.getText();
        final Boolean matchCase = matchCaseValue.isSelected();
        if (text.length() == 0) {
            moduleList.setModel(CustomizerComponentFactory.createSortedDependencyListModel(selectedDeps));
            moduleList.setSelectedIndex(0);
            moduleList.ensureIndexIsVisible(0);
        } else {
            final Runnable compute = new Runnable() {
                public @Override void run() {
                    AddModuleFilter _filterer = filterer;
                    if (_filterer == null) {
                        return;
                    }
                    final Set<ModuleDependency> matches;
                    synchronized (IMPL_LOCK) {
                        matches = _filterer.getMatches(selectedDeps, text, matchCase);
                    }
                    synchronized (AddModulePanel.this) {
                        filterTask = null;
                    }
                    Mutex.EVENT.readAccess(new Runnable() {
                        public @Override void run() {
                            timer.stop();
                            // XXX would be better to have more fine-grained control over the thread
                            if (!text.equals(filterValue.getText())) {
                                return; // no longer valid, don't apply
                            }
                            moduleList.setModel(CustomizerComponentFactory.createDependencyListModel(matches));
                            int index = matches.isEmpty() ? -1 : 0;
                            moduleList.setSelectedIndex(index);
                            moduleList.ensureIndexIsVisible(index);
                        }
                    });
                }
            };
            restartTimer();
            synchronized (this) {
                filterTask = RP.post(new Runnable() {
                    public @Override void run() {
                        if (filterer == null) {
                            filterer = new AddModuleFilter(universeModules.getDependencies(), props.getCodeNameBase());
                        }
                        compute.run();
                    }
                });
            }
        }
    }
    
    private void restartTimer () {
        if (timer == null) {
            timer = new Timer(1000, new ActionListener() {
                @Override
                public void actionPerformed (ActionEvent e) {
                    moduleList.setModel(UIUtil.createListWaitModel());
                }
            });
            timer.setRepeats(false);
        }
        timer.restart();
    }
    
    private void initAccessibility() {
        this.getAccessibleContext().setAccessibleDescription(getMessage("ACS_AddModuleDependency"));
        filterValue.getAccessibleContext().setAccessibleDescription(getMessage("ACS_LBL_Filter"));
        moduleList.getAccessibleContext().setAccessibleDescription(getMessage("ACS_CTL_ModuleList"));
        moduleSP.getVerticalScrollBar().getAccessibleContext().setAccessibleName(getMessage("ACS_CTL_ModuleListVerticalScroll"));
        moduleSP.getVerticalScrollBar().getAccessibleContext().setAccessibleDescription(getMessage("ACSD_CTL_ModuleListVerticalScroll"));
        moduleSP.getHorizontalScrollBar().getAccessibleContext().setAccessibleName(getMessage("ACS_CTL_ModuleListHorizontalScroll"));
        moduleSP.getHorizontalScrollBar().getAccessibleContext().setAccessibleDescription(getMessage("ACSD_CTL_ModuleListHorizontalScroll"));
        showNonAPIModules.getAccessibleContext().setAccessibleDescription(getMessage("ACSD_ShowNonApiModules"));
    }
    
    private static String getMessage(String key) {
        return NbBundle.getMessage(AddModulePanel.class, key);
    }
    
    /** This method is called from within the constructor to
     * initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is
     * always regenerated by the Form Editor.
     */
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        moduleLabel = new javax.swing.JLabel();
        moduleSP = new javax.swing.JScrollPane();
        moduleList = new javax.swing.JList();
        descLabel = new javax.swing.JLabel();
        filter = new javax.swing.JLabel();
        filterValue = new javax.swing.JTextField();
        descValueSP = new javax.swing.JScrollPane();
        descValue = new javax.swing.JTextPane();
        showJavadocButton = new javax.swing.JButton();
        showNonAPIModules = new javax.swing.JCheckBox();
        matchCaseValue = new javax.swing.JCheckBox();
        showExclModulesCheckBox = new javax.swing.JCheckBox();
        clusterFilterComboBox = new javax.swing.JComboBox();
        clusterFilterLabel = new javax.swing.JLabel();

        setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 6, 6, 6));
        setPreferredSize(new java.awt.Dimension(500, 450));
        setLayout(new java.awt.GridBagLayout());

        moduleLabel.setLabelFor(moduleList);
        org.openide.awt.Mnemonics.setLocalizedText(moduleLabel, org.openide.util.NbBundle.getMessage(AddModulePanel.class, "LBL_Module")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(moduleLabel, gridBagConstraints);

        moduleSP.setPreferredSize(new java.awt.Dimension(450, 116));
        moduleSP.setViewportView(moduleList);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 4;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        add(moduleSP, gridBagConstraints);

        descLabel.setLabelFor(descValue);
        org.openide.awt.Mnemonics.setLocalizedText(descLabel, org.openide.util.NbBundle.getMessage(AddModulePanel.class, "LBL_Description")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 5;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(descLabel, gridBagConstraints);

        filter.setLabelFor(filterValue);
        org.openide.awt.Mnemonics.setLocalizedText(filter, org.openide.util.NbBundle.getMessage(AddModulePanel.class, "LBL_Filter")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 8);
        add(filter, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 4, 0);
        add(filterValue, gridBagConstraints);

        descValueSP.setPreferredSize(new java.awt.Dimension(450, 116));

        descValue.setEditable(false);
        descValue.setPreferredSize(new java.awt.Dimension(6, 100));
        descValueSP.setViewportView(descValue);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 6;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        add(descValueSP, gridBagConstraints);

        java.util.ResourceBundle bundle = java.util.ResourceBundle.getBundle("org/netbeans/modules/apisupport/project/ui/customizer/Bundle"); // NOI18N
        org.openide.awt.Mnemonics.setLocalizedText(showJavadocButton, bundle.getString("CTL_ShowJavadoc")); // NOI18N
        showJavadocButton.setEnabled(false);
        showJavadocButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showJavadoc(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 7;
        gridBagConstraints.gridwidth = java.awt.GridBagConstraints.REMAINDER;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(showJavadocButton, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(showNonAPIModules, org.openide.util.NbBundle.getMessage(AddModulePanel.class, "CTL_ShowNonAPIModules")); // NOI18N
        showNonAPIModules.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        showNonAPIModules.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showNonAPIModulesActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.gridwidth = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(5, 0, 0, 0);
        add(showNonAPIModules, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(matchCaseValue, "Match &Case");
        matchCaseValue.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 0, 0, 0));
        matchCaseValue.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                matchCaseValueActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 6, 0, 0);
        add(matchCaseValue, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(showExclModulesCheckBox, "Show &Excl. Modules");
        showExclModulesCheckBox.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                showExclModulesCheckBoxActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(4, 6, 0, 0);
        add(showExclModulesCheckBox, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.weightx = 2.0;
        gridBagConstraints.insets = new java.awt.Insets(4, 0, 4, 0);
        add(clusterFilterComboBox, gridBagConstraints);

        clusterFilterLabel.setLabelFor(clusterFilterComboBox);
        org.openide.awt.Mnemonics.setLocalizedText(clusterFilterLabel, "C&luster:");
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
        gridBagConstraints.insets = new java.awt.Insets(0, 0, 0, 8);
        add(clusterFilterLabel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents
    
    private void showNonAPIModulesActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showNonAPIModulesActionPerformed
        fillUpUniverseClusters();
        fillUpUniverseModules();
    }//GEN-LAST:event_showNonAPIModulesActionPerformed
    
    private void showJavadoc(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showJavadoc
        HtmlBrowser.URLDisplayer.getDefault().showURL(currectJavadoc);
    }//GEN-LAST:event_showJavadoc

    private void matchCaseValueActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_matchCaseValueActionPerformed
        fillUpUniverseClusters();
        fillUpUniverseModules();
    }//GEN-LAST:event_matchCaseValueActionPerformed

    private void showExclModulesCheckBoxActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_showExclModulesCheckBoxActionPerformed
        fillUpUniverseClusters();
        fillUpUniverseModules();
    }//GEN-LAST:event_showExclModulesCheckBoxActionPerformed
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JComboBox clusterFilterComboBox;
    private javax.swing.JLabel clusterFilterLabel;
    private javax.swing.JLabel descLabel;
    private javax.swing.JTextPane descValue;
    private javax.swing.JScrollPane descValueSP;
    private javax.swing.JLabel filter;
    javax.swing.JTextField filterValue;
    private javax.swing.JCheckBox matchCaseValue;
    private javax.swing.JLabel moduleLabel;
    javax.swing.JList moduleList;
    private javax.swing.JScrollPane moduleSP;
    private javax.swing.JCheckBox showExclModulesCheckBox;
    private javax.swing.JButton showJavadocButton;
    private javax.swing.JCheckBox showNonAPIModules;
    // End of variables declaration//GEN-END:variables
    
}
