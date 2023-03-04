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
package org.netbeans.modules.analysis;

import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.beans.BeanInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.prefs.Preferences;
import javax.swing.DefaultComboBoxModel;
import javax.swing.DefaultListCellRenderer;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.border.EmptyBorder;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.api.fileinfo.NonRecursiveFolder;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.progress.ProgressHandleFactory;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.analysis.spi.Analyzer.AnalyzerFactory;
import org.netbeans.modules.analysis.spi.Analyzer.Context;
import org.netbeans.modules.analysis.spi.Analyzer.MissingPlugin;
import org.netbeans.modules.analysis.spi.Analyzer.WarningDescription;
import org.netbeans.modules.analysis.ui.AdjustConfigurationPanel;
import org.netbeans.modules.analysis.ui.AdjustConfigurationPanel.ErrorListener;
import org.netbeans.modules.analysis.ui.ConfigurationsComboModel;
import org.netbeans.modules.refactoring.api.Scope;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotificationLineSupport;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.loaders.DataObjectNotFoundException;
import org.openide.nodes.Node;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.LookupEvent;
import org.openide.util.LookupListener;
import org.openide.util.NbBundle.Messages;
import org.openide.util.NbPreferences;
import org.openide.util.RequestProcessor;

/**
 *
 * @author lahvac
 */
public final class RunAnalysisPanel extends javax.swing.JPanel implements LookupListener {

    private static final String COMBO_PROTOTYPE = "999999999999999999999999999999999999";
    private static final RequestProcessor WORKER = new RequestProcessor(RunAnalysisPanel.class.getName(), 1, false, false);
    private final JPanel progress;
    private final RequiredPluginsPanel requiredPlugins;
    //GuardedBy(AWT)
    private       Collection<? extends AnalyzerFactory> analyzers;
    //GuardedBy(AWT)
    private       FutureWarnings analyzer2Warnings;
    private final Lookup.Result<AnalyzerFactory> analyzersResult;
    private final Map<String, AnalyzerAndWarning> warningId2Description = new HashMap<String, AnalyzerAndWarning>();
    private final JButton runAnalysis;
    //GuardedBy(AWT)
    private       boolean inspectionsReady;
    private       boolean started;

    public RunAnalysisPanel(ProgressHandle handle, Lookup context, JButton runAnalysis) {
        this(handle, context, runAnalysis, null);
    }
    
    public RunAnalysisPanel(ProgressHandle handle, Lookup context, JButton runAnalysis, DialogState state) {
        this.runAnalysis = runAnalysis;
        this.analyzersResult = Lookup.getDefault().lookupResult(AnalyzerFactory.class);
        this.analyzersResult.addLookupListener(this);
        
        initComponents();

        List<ScopeDescription> scopes = new ArrayList<ScopeDescription>();
        Icon currentProjectIcon = null;
        NonRecursiveFolder pack = context.lookup(NonRecursiveFolder.class);
        FileObject currentFile = context.lookup(FileObject.class);
        DataFolder folder = context.lookup(DataFolder.class);

        if (currentFile != null && currentFile.isData()) {
            scopes.add(new FileScopeDescription(currentFile));
        }
        
        if (folder != null) {
            scopes.add(new FolderScopeDescription(folder));
        }
        
        if (pack != null && currentFile == null) {
            currentFile = pack.getFolder();
        }

        if (currentFile != null) {
            Project p = FileOwnerQuery.getOwner(currentFile);

            if (p != null) {
                ProjectInformation pi = ProjectUtils.getInformation(p);

                scopes.add(0, new CurrentProjectScopeDescription(p));
                currentProjectIcon = pi.getIcon();

                if (pack == null) {
                    ClassPath bootCP = ClassPath.getClassPath(currentFile, ClassPath.BOOT);

                    if (bootCP != null) {
                        final FileObject packFO = !currentFile.isFolder() ? currentFile.getParent() : currentFile;
                        pack = new NonRecursiveFolder() {
                            @Override public FileObject getFolder() {
                                return packFO;
                            }
                        };
                    }
                }
            }
        } else {
            Project selected = context.lookup(Project.class);

            if (selected == null) {
                SourceGroup sg = context.lookup(SourceGroup.class);

                if (sg != null) {
                    selected = FileOwnerQuery.getOwner(sg.getRootFolder());
                }
            }

            if (selected == null) {
                DataFolder df = context.lookup(DataFolder.class);

                if (df != null) {
                    selected = FileOwnerQuery.getOwner(df.getPrimaryFile());
                }
            }

            if (selected != null) {
                ProjectInformation pi = ProjectUtils.getInformation(selected);

                scopes.add(0, new CurrentProjectScopeDescription(selected));
                currentProjectIcon = pi.getIcon();
            }
        }

        if (pack != null) {
            ClassPath source = ClassPath.getClassPath(pack.getFolder(), ClassPath.SOURCE);

            if (source != null) {
                String packName = source.getResourceName(pack.getFolder(), '.', false);

                scopes.add(1, new PackageScopeDescription(pack, packName));
            }
        }

        scopes.add(0, new AllProjectsScopeDescription(currentProjectIcon));

        scopeCombo.setModel(new DefaultComboBoxModel(scopes.toArray(new ScopeDescription[0])));
        scopeCombo.setRenderer(new ScopeRenderer());
        scopeCombo.setSelectedIndex(scopes.size() - 1);

        GridBagConstraints gridBagConstraints = new java.awt.GridBagConstraints();

        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.gridheight = 1;
        gridBagConstraints.weightx = 1;
        gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.NORTHWEST;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        progress = new JPanel(new CardLayout());
        progress.add(new JPanel(), "empty");
        progress.add(ProgressHandleFactory.createProgressComponent(handle), "progress");
        progress.add(requiredPlugins = new RequiredPluginsPanel(), "plugins");
        add(progress, gridBagConstraints);
        ((CardLayout) progress.getLayout()).show(progress, "empty");

        if (state == null) state = DialogState.load();
        updateConfigurations(state);
        updateEnableDisable();

        setBorder(new EmptyBorder(12, 12, 12, 12));
        
        ConfigurationsManager.getDefault().addChangeListener(new ChangeListener() {
            @Override public void stateChanged(ChangeEvent e) {
                resultChanged(null);
            }
        });
        
        configurationCombo.addActionListener(new ActionListener() {
            Object currentItem = configurationCombo.getSelectedItem();
            @Override
            public void actionPerformed(ActionEvent e) {
                Object tempItem = configurationCombo.getSelectedItem();
                if (tempItem instanceof String) {
                    configurationCombo.setSelectedItem(currentItem);
                } else {
                    currentItem = tempItem;
                    updateEnableDisable();
                }
            }
        });
        
        inspectionCombo.addActionListener(new ActionListener() {
            Object currentItem = inspectionCombo.getSelectedItem();
            @Override
            public void actionPerformed(ActionEvent e) {
                Object tempItem = inspectionCombo.getSelectedItem();
                if (!(tempItem instanceof AnalyzerAndWarning)) {
                    inspectionCombo.setSelectedItem(currentItem);
                    updatePlugins();
                } else {
                    currentItem = tempItem;
                }
            }
        });

        inspectionCombo.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int direction = 1;
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_DOWN:
                        direction = 1;
                        break;
                    case KeyEvent.VK_UP:
                        direction = -1;
                        break;
                }
                int currentIndex = inspectionCombo.getSelectedIndex() + direction;
                Object tempItem = inspectionCombo.getItemAt(currentIndex);
                if (tempItem instanceof AnalyzerAndWarning) {
                    return;
                } else {
                    currentIndex += direction;
                }
                Object nextItem = inspectionCombo.getItemAt(currentIndex);
                while (!(nextItem instanceof AnalyzerAndWarning)) {
                    currentIndex += direction;
                    nextItem = inspectionCombo.getItemAt(currentIndex);
                    if (nextItem == null) {
                        return;
                    }
                }
                inspectionCombo.setSelectedItem((AnalyzerAndWarning) nextItem);
                e.consume();
            }
        });

        if (state != null && state.scope != null) {
            for (int i = 0; i < scopeCombo.getItemCount(); i++) {
                ScopeDescription sd = (ScopeDescription)scopeCombo.getItemAt(i);
                if (sd != null && sd.getId().equals(state.scope)) {
                    scopeCombo.setSelectedIndex(i);
                    break;
                }
            }
        }
    }

    void started() {
        started = true;
        
        ((CardLayout) progress.getLayout()).show(progress, "progress");
        progress.invalidate();

        //disable all elements in the dialog:
        List<JComponent> todo = new LinkedList<JComponent>();

        todo.add(this);

        while (!todo.isEmpty()) {
            JComponent c = todo.remove(0);

            if (c == progress) continue;

            c.setEnabled(false);

            for (Component child : c.getComponents()) {
                if (child instanceof JComponent) todo.add((JComponent) child);
            }
        }
    }

    @Messages({"LBL_Predefined=Predefined", "LBL_Custom=Custom", "LBL_PleaseWait=Computing..."})
    public void updateConfigurations(final DialogState state) {
        analyzers = analyzersResult.allInstances();
        
        Object selectedConfiguration = null;
        DefaultComboBoxModel configurationModel = new DefaultComboBoxModel();
        configurationModel.addElement(Bundle.LBL_Predefined());
        configurationModel.addElement(null);
        
        for (AnalyzerFactory analyzer : analyzers) {
            if (SPIAccessor.ACCESSOR.getAnalyzerId(analyzer).equals(state.selectedAnalyzer)) {
                selectedConfiguration = analyzer;
            }
            configurationModel.addElement(analyzer);
        }

        configurationModel.addElement(Bundle.LBL_Custom());

        for (Configuration c : ConfigurationsManager.getDefault().getConfigurations()) {
            if (c.id().equals(state.selectedConfiguration)) {
                selectedConfiguration = c;
            }
            configurationModel.addElement(c);
        }

        configurationCombo.setModel(configurationModel);
        configurationCombo.setSelectedItem(selectedConfiguration);

        configurationRadio.setSelected(state.configurationsSelected);
        configurationCombo.setRenderer(new ConfigurationRenderer(true));

        DefaultComboBoxModel inspectionModel = new DefaultComboBoxModel();
        
        inspectionModel.addElement(Bundle.LBL_PleaseWait());
        
        inspectionCombo.setModel(inspectionModel);
        inspectionCombo.setRenderer(new InspectionRenderer());
        inspectionCombo.setSelectedItem(0);
        singleInspectionRadio.setSelected(!state.configurationsSelected);

        updatePlugins();
        
        final Collection<? extends AnalyzerFactory> analyzersCopy = new ArrayList<AnalyzerFactory>(analyzers);
        
        inspectionsReady = false;
        analyzer2Warnings = new FutureWarnings();
        
        WORKER.post(new Runnable() {
            @Override public void run() {
                final Map<AnalyzerFactory, Iterable<? extends WarningDescription>> analyzer2Warnings = new HashMap<AnalyzerFactory, Iterable<? extends WarningDescription>>();
                
                for (AnalyzerFactory a : analyzersCopy) {
                    List<WarningDescription> warnings = new ArrayList<WarningDescription>();
                    
                    for (WarningDescription wd : a.getWarnings()) {
                        warnings.add(wd);
                    }
                    
                    analyzer2Warnings.put(a, warnings);
                }
                
                final Map<AnalyzerFactory, Map<String, WarningDescription>> analyzer2Id2DescriptionVal = computeAnalyzerId2Description(analyzer2Warnings);
                
                SwingUtilities.invokeLater(new Runnable() {
                    @Override public void run() {
                        RunAnalysisPanel.this.analyzer2Warnings.set(analyzer2Id2DescriptionVal);
                        fillInspectionCombo(state, analyzer2Warnings);
                        inspectionsReady = true;
                        updatePlugins();
                    }
                });
            }
        });
    }
    
    private void fillInspectionCombo(DialogState state, Map<? extends AnalyzerFactory, Iterable<? extends WarningDescription>> analyzer2Warnings) {
        DefaultComboBoxModel inspectionModel = new DefaultComboBoxModel();
        AnalyzerAndWarning firstInspection = null;
        AnalyzerAndWarning preselectInspection = null;

        for (Entry<? extends AnalyzerFactory, Iterable<? extends WarningDescription>> e : analyzer2Warnings.entrySet()) {
            inspectionModel.addElement(SPIAccessor.ACCESSOR.getAnalyzerDisplayName(e.getKey()));

            Map<String, Collection<WarningDescription>> cat2Warnings = new TreeMap<String, Collection<WarningDescription>>();

            for (WarningDescription wd : e.getValue()) {
                String cat = SPIAccessor.ACCESSOR.getWarningCategoryDisplayName(wd); //TODO: should be based on the id rather than on the display name
                Collection<WarningDescription> warnings = cat2Warnings.get(cat);

                if (warnings == null) {
                    cat2Warnings.put(cat, warnings = new TreeSet<WarningDescription>(new Comparator<WarningDescription>() {
                        @Override public int compare(WarningDescription o1, WarningDescription o2) {
                            return SPIAccessor.ACCESSOR.getWarningDisplayName(o1).compareToIgnoreCase(SPIAccessor.ACCESSOR.getWarningDisplayName(o2));
                        }
                    }));
                }

                warnings.add(wd);
            }

            for (Entry<String, Collection<WarningDescription>> catE : cat2Warnings.entrySet()) {
                inspectionModel.addElement("  " + catE.getKey());

                for (WarningDescription wd : catE.getValue()) {
                    AnalyzerAndWarning aaw = new AnalyzerAndWarning(e.getKey(), wd);
                    inspectionModel.addElement(aaw);
                    warningId2Description.put(SPIAccessor.ACCESSOR.getWarningId(wd), aaw);
                    
                    if (firstInspection == null) firstInspection = aaw;
                    if (SPIAccessor.ACCESSOR.getWarningId(wd).equals(state.selectedInspection)) {
                        preselectInspection = aaw;
                    }
                }
            }
        }

        inspectionCombo.setModel(inspectionModel);
        inspectionCombo.setSelectedItem(preselectInspection != null ? preselectInspection : firstInspection);
        
    }

    private void updatePlugins() {
        if (started) return ;
        
        Collection<? extends AnalyzerFactory> toRun;

        if (singleInspectionRadio.isSelected()) {
            if (!inspectionsReady) {
                runAnalysis.setEnabled(false);
                return ;
            }
            
            Object selectedInspection = inspectionCombo.getSelectedItem();
            
            if (selectedInspection instanceof AnalyzerAndWarning) {
                toRun = Collections.singleton(((AnalyzerAndWarning) selectedInspection).analyzer);
            } else {
                toRun = Collections.emptyList();
            }
        } else {
            if (!(configurationCombo.getSelectedItem() instanceof AnalyzerFactory)) {
                toRun = analyzers;
            } else {
                toRun = Collections.singleton((AnalyzerFactory) configurationCombo.getSelectedItem());
            }
        }
        Set<MissingPlugin> plugins = new HashSet<MissingPlugin>();
        boolean someOk = false;

        for (AnalyzerFactory a : toRun) {
            Configuration configuration = getConfiguration();
            Preferences settings = configuration != null ? configuration.getPreferences().node(SPIAccessor.ACCESSOR.getAnalyzerId(a)) : null;
            Context ctx = SPIAccessor.ACCESSOR.createContext(getSelectedScope(new AtomicBoolean(false)), settings, null, null, -1, -1);
            Collection<? extends MissingPlugin> req = a.requiredPlugins(ctx);
            plugins.addAll(req);
            someOk |= req.isEmpty();
        }

        if (plugins.isEmpty()) {
            ((CardLayout) progress.getLayout()).show(progress, "empty");
        } else {
            requiredPlugins.setRequiredPlugins(plugins, !someOk);
            ((CardLayout) progress.getLayout()).show(progress, "plugins");
        }
        
        runAnalysis.setEnabled(someOk);
    }

    public Scope getSelectedScope(AtomicBoolean cancel) {
        return ((ScopeDescription) scopeCombo.getSelectedItem()).getScope(cancel);
    }
    
    public AnalyzerFactory getSelectedAnalyzer() {
        if (singleInspectionRadio.isSelected()) {
            Object singleInspection = inspectionCombo.getSelectedItem();
            
            if (singleInspection instanceof AnalyzerAndWarning) {
                return ((AnalyzerAndWarning) singleInspection).analyzer;
            }
            
            return null;
        }
        if (!(configurationCombo.getSelectedItem() instanceof AnalyzerFactory)) return null;
        return (AnalyzerFactory) configurationCombo.getSelectedItem();
    }

    public Configuration getConfiguration() {
        if (inspectionCombo.isEnabled()) return ConfigurationsManager.getDefault().getTemporaryConfiguration();
        Object selected = configurationCombo.getSelectedItem();

        if (selected instanceof Configuration) return (Configuration) selected;
        else return null;
    }

    public Collection<? extends AnalyzerFactory> getAnalyzers() {
        return analyzers;
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {
        java.awt.GridBagConstraints gridBagConstraints;

        radioButtons = new javax.swing.ButtonGroup();
        javax.swing.JLabel jLabel1 = new javax.swing.JLabel();
        scopeCombo = new javax.swing.JComboBox();
        javax.swing.JLabel jLabel2 = new javax.swing.JLabel();
        configurationCombo = new javax.swing.JComboBox();
        manage = new javax.swing.JButton();
        configurationRadio = new javax.swing.JRadioButton();
        singleInspectionRadio = new javax.swing.JRadioButton();
        inspectionCombo = new javax.swing.JComboBox();
        browse = new javax.swing.JButton();
        javax.swing.JPanel emptyPanel = new javax.swing.JPanel();

        setLayout(new java.awt.GridBagLayout());

        org.openide.awt.Mnemonics.setLocalizedText(jLabel1, org.openide.util.NbBundle.getMessage(RunAnalysisPanel.class, "RunAnalysisPanel.jLabel1.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.ABOVE_BASELINE_LEADING;
        add(jLabel1, gridBagConstraints);

        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 0;
        gridBagConstraints.gridwidth = 3;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.ABOVE_BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(0, 12, 0, 0);
        add(scopeCombo, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(jLabel2, org.openide.util.NbBundle.getMessage(RunAnalysisPanel.class, "RunAnalysisPanel.jLabel2.text")); // NOI18N
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 0;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.ABOVE_BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(12, 0, 0, 0);
        add(jLabel2, gridBagConstraints);

        configurationCombo.setPrototypeDisplayValue(COMBO_PROTOTYPE);
        configurationCombo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configurationComboActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.ABOVE_BASELINE_LEADING;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(configurationCombo, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(manage, org.openide.util.NbBundle.getMessage(RunAnalysisPanel.class, "RunAnalysisPanel.manage.text")); // NOI18N
        manage.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                manageActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(manage, gridBagConstraints);

        radioButtons.add(configurationRadio);
        org.openide.awt.Mnemonics.setLocalizedText(configurationRadio, org.openide.util.NbBundle.getMessage(RunAnalysisPanel.class, "RunAnalysisPanel.configurationRadio.text")); // NOI18N
        configurationRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configurationRadioActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 1;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.LINE_START;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(configurationRadio, gridBagConstraints);

        radioButtons.add(singleInspectionRadio);
        org.openide.awt.Mnemonics.setLocalizedText(singleInspectionRadio, org.openide.util.NbBundle.getMessage(RunAnalysisPanel.class, "RunAnalysisPanel.singleInspectionRadio.text")); // NOI18N
        singleInspectionRadio.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                singleInspectionRadioActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 1;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE_LEADING;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(singleInspectionRadio, gridBagConstraints);

        inspectionCombo.setPrototypeDisplayValue(COMBO_PROTOTYPE);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 2;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.weightx = 1.0;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(inspectionCombo, gridBagConstraints);

        org.openide.awt.Mnemonics.setLocalizedText(browse, org.openide.util.NbBundle.getMessage(RunAnalysisPanel.class, "RunAnalysisPanel.browse.text")); // NOI18N
        browse.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                browseActionPerformed(evt);
            }
        });
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridx = 3;
        gridBagConstraints.gridy = 2;
        gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
        gridBagConstraints.anchor = java.awt.GridBagConstraints.BASELINE;
        gridBagConstraints.insets = new java.awt.Insets(12, 12, 0, 0);
        add(browse, gridBagConstraints);
        gridBagConstraints = new java.awt.GridBagConstraints();
        gridBagConstraints.gridy = 3;
        gridBagConstraints.gridwidth = 4;
        gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
        gridBagConstraints.weighty = 1.0;
        add(emptyPanel, gridBagConstraints);
    }// </editor-fold>//GEN-END:initComponents

    private void configurationComboActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configurationComboActionPerformed
        updatePlugins();
    }//GEN-LAST:event_configurationComboActionPerformed

    private AdjustConfigurationPanel showConfigurationPanel(AnalyzerFactory preselectedAnalyzer, String preselected, Configuration configurationToSelect) {
        final NotificationLineSupport[] nls = new NotificationLineSupport[1];
        final DialogDescriptor[] dd = new DialogDescriptor[1];
        ErrorListener errorListener = new ErrorListener() {
            @Override public void setError(String error) {
                nls[0].setErrorMessage(error);
                dd[0].setValid(error == null);
            }
        };
        AdjustConfigurationPanel panel = new AdjustConfigurationPanel(analyzers, preselectedAnalyzer, preselected, configurationToSelect, errorListener);
        panel.setPreferredSize(new Dimension(700, 300));
        DialogDescriptor nd = new DialogDescriptor(panel, Bundle.LBL_Configurations(), true, NotifyDescriptor.OK_CANCEL_OPTION, NotifyDescriptor.OK_OPTION, null);
        
        nls[0] = nd.createNotificationLineSupport();
        dd[0] = nd;

        if (DialogDisplayer.getDefault().notify(nd) == NotifyDescriptor.OK_OPTION) {
            return panel;
        }
        
        return null;
    }

    @Messages("LBL_Configurations=Configurations")
    private void manageActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_manageActionPerformed
        AdjustConfigurationPanel panel = showConfigurationPanel(null, null, (Configuration) configurationCombo.getSelectedItem());
        if (panel != null) {
            panel.save();
            configurationCombo.setSelectedItem(panel.getSelectedConfiguration());
        }
    }//GEN-LAST:event_manageActionPerformed

    private void configurationRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configurationRadioActionPerformed
        updateEnableDisable();
    }//GEN-LAST:event_configurationRadioActionPerformed

    private void singleInspectionRadioActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_singleInspectionRadioActionPerformed
        updateEnableDisable();
    }//GEN-LAST:event_singleInspectionRadioActionPerformed

    @Messages("LBL_Browse=Browse Inspections")
    private void browseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_browseActionPerformed
        Object selectedInspection = inspectionCombo.getSelectedItem();
        AnalyzerFactory analyzerToSelect;
        String warningToSelect;
        
        if (selectedInspection instanceof AnalyzerAndWarning) {
            analyzerToSelect = ((AnalyzerAndWarning) selectedInspection).analyzer;
            warningToSelect = SPIAccessor.ACCESSOR.getWarningId(((AnalyzerAndWarning) selectedInspection).wd);
        } else {
            analyzerToSelect = null;
            warningToSelect = "";
        }

        AdjustConfigurationPanel panel = showConfigurationPanel(analyzerToSelect, warningToSelect, null);

        if (panel != null) {
            inspectionCombo.setSelectedItem(warningId2Description.get(panel.getIdToRun()));
        }
    }//GEN-LAST:event_browseActionPerformed

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton browse;
    private javax.swing.JComboBox configurationCombo;
    private javax.swing.JRadioButton configurationRadio;
    private javax.swing.JComboBox inspectionCombo;
    private javax.swing.JButton manage;
    private javax.swing.ButtonGroup radioButtons;
    private javax.swing.JComboBox scopeCombo;
    private javax.swing.JRadioButton singleInspectionRadio;
    // End of variables declaration//GEN-END:variables

    private void updateEnableDisable() {
        if (started) return ;
        
        boolean configuration = configurationRadio.isSelected();

        configurationCombo.setEnabled(configuration);
        manage.setEnabled(configuration && configurationCombo.getSelectedItem() instanceof Configuration);
        inspectionCombo.setEnabled(!configuration);
        browse.setEnabled(!configuration);
        
        updatePlugins();
    }

    String getSingleWarningId() {
        return inspectionCombo.isEnabled() ? SPIAccessor.ACCESSOR.getWarningId(((AnalyzerAndWarning) inspectionCombo.getSelectedItem()).wd) : null;
    }

    @Override
    public void resultChanged(LookupEvent ev) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override public void run() {
                updateConfigurations(getDialogState());
            }
        });
    }
    
    public DialogState getDialogState() {
        Object selectedConfiguration = configurationCombo.getSelectedItem();
        Object selectedInspection = inspectionCombo.getSelectedItem();
        return new DialogState(configurationRadio.isSelected(),
                               selectedConfiguration instanceof AnalyzerFactory ? SPIAccessor.ACCESSOR.getAnalyzerId((AnalyzerFactory) selectedConfiguration) : null,
                               selectedConfiguration instanceof Configuration ? ((Configuration) selectedConfiguration).id() : null,
                               selectedInspection instanceof AnalyzerAndWarning ? SPIAccessor.ACCESSOR.getWarningId(((AnalyzerAndWarning) selectedInspection).wd) : null,
                               ((ScopeDescription) scopeCombo.getSelectedItem()).getId());
    }

    FutureWarnings getAnalyzerId2Description() {
        return analyzer2Warnings;
    }
    
    private static Map<AnalyzerFactory, Map<String, WarningDescription>> computeAnalyzerId2Description(Map<? extends AnalyzerFactory, Iterable<? extends WarningDescription>> analyzer2Warnings) {
        Map<AnalyzerFactory, Map<String, WarningDescription>> result = new HashMap<AnalyzerFactory, Map<String, WarningDescription>>();
        
        for (Entry<? extends AnalyzerFactory, Iterable<? extends WarningDescription>> e : analyzer2Warnings.entrySet()) {
            Map<String, WarningDescription> perAnalyzer = new HashMap<String, WarningDescription>();
            
            result.put(e.getKey(), perAnalyzer);
            
            for (WarningDescription wd : e.getValue()) {
                perAnalyzer.put(SPIAccessor.ACCESSOR.getWarningId(wd), wd);
            }
        }
        
        return result;
    }

    public static final class ConfigurationRenderer extends DefaultListCellRenderer {

        private final boolean indent;

        public ConfigurationRenderer(boolean indent) {
            this.indent = indent;
        }

        @Messages({"LBL_RunAllAnalyzers=All Analyzers", "# {0} - the analyzer that should be run", "LBL_RunAnalyzer={0}"})
        @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value == null) {
                value = Bundle.LBL_RunAllAnalyzers();
            } else if (value instanceof AnalyzerFactory) {
                value = Bundle.LBL_RunAnalyzer(SPIAccessor.ACCESSOR.getAnalyzerDisplayName((AnalyzerFactory) value));
            } else if (value instanceof Configuration) {
                value = ((Configuration) value).getDisplayName();
            } else if (value instanceof String) {
                setFont(getFont().deriveFont(Font.ITALIC));
                setText((String) value);
                setEnabled(false);
                setBackground(list.getBackground());
                setForeground(UIManager.getColor("Label.disabledForeground"));

                return this;
            }

            if (index == list.getModel().getSize()-5 && list.getModel() instanceof ConfigurationsComboModel && ((ConfigurationsComboModel) list.getModel()).canModify()) {
                setBorder(new Separator(list.getForeground()));
            } else {
                setBorder(null);
            }

            return super.getListCellRendererComponent(list, (indent ? "  " : "") + value, index, isSelected, cellHasFocus);
        }
    }

    private static final class InspectionRenderer extends DefaultListCellRenderer {

        @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof AnalyzerAndWarning) {
                value = "    " + SPIAccessor.ACCESSOR.getWarningDisplayName(((AnalyzerAndWarning) value).wd);
            } else if (value instanceof String) {
                setFont(getFont().deriveFont(Font.ITALIC));
                setText((String) value);
                setEnabled(false);
                setBackground(list.getBackground());
                setForeground(UIManager.getColor("Label.disabledForeground"));

                return this;
            }

            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }

    private interface ScopeDescription {
        public String getDisplayName();
        public Icon   getIcon();
        public Scope  getScope(AtomicBoolean cancel);
        public String getId();
    }

    private static final class AllProjectsScopeDescription implements ScopeDescription {
        private final Icon icon;
        public AllProjectsScopeDescription(Icon icon) {
            this.icon = icon;
        }
        @Override
        @Messages("DN_OpenProjects=Open Projects")
        public String getDisplayName() {
            return Bundle.DN_OpenProjects();
        }
        @Override
        public Icon getIcon() {
            return icon;
        }

        @Override
        public Scope getScope(AtomicBoolean cancel) {
            Map<Project, Map<FileObject, ClassPath>> projects2RegisteredContent = RunAnalysis.projects2RegisteredContent(cancel);

            if (cancel.get()) return null;

            Scope target = Scope.create(null, null, null);

            for (Project p : OpenProjects.getDefault().getOpenProjects()) {
                if (cancel.get()) return null;
                target = RunAnalysis.addProjectToScope(p, target, cancel, projects2RegisteredContent);
            }

            return target;
        }
        
        public String getId() {
            return "*allProjects"; // NOI18N
        }
    }

    private static final class CurrentProjectScopeDescription implements ScopeDescription {
        private final Project project;
        public CurrentProjectScopeDescription(Project project) {
            this.project = project;
        }
        @Override
        @Messages({"# {0} - project display name", "DN_CurrentProject=Current Project ({0})"})
        public String getDisplayName() {
            return Bundle.DN_CurrentProject(ProjectUtils.getInformation(project).getDisplayName());
        }
        @Override
        public Icon getIcon() {
            return ProjectUtils.getInformation(project).getIcon();
        }

        @Override
        public Scope getScope(AtomicBoolean cancel) {
            Map<Project, Map<FileObject, ClassPath>> projects2RegisteredContent = RunAnalysis.projects2RegisteredContent(cancel);

            if (cancel.get()) return null;
            
            return RunAnalysis.addProjectToScope(project, Scope.create(null, null, null), cancel, projects2RegisteredContent);
        }
        
        public String getId() {
            return "*currentProject"; // NOI18N
        }
    }

    private static final class PackageScopeDescription implements ScopeDescription {
        private final NonRecursiveFolder pack;
        private final String packName;
        public PackageScopeDescription(NonRecursiveFolder pack, String packName) {
            this.pack = pack;
            this.packName = packName;
        }
        @Override
        @Messages({"# {0} - package display name", "DN_CurrentPackage=Current Package ({0})"})
        public String getDisplayName() {
            return Bundle.DN_CurrentPackage(packName);
        }
        @Override
        public Icon getIcon() {
            return ImageUtilities.loadImageIcon("org/netbeans/modules/analysis/ui/resources/package.gif", false);
        }

        @Override
        public Scope getScope(AtomicBoolean cancel) {
            return Scope.create(null, Collections.singletonList(pack), null);
        }
        
        public String getId() {
            return "*currentPackage"; // NOI18N
        }
    }

    private static final class FolderScopeDescription implements ScopeDescription {
        private final DataFolder folder;
        public FolderScopeDescription(DataFolder folder) {
            this.folder = folder;
        }
        @Override
        @Messages({"# {0} - folder display name", "DN_CurrentFolder=Current Folder ({0})"})
        public String getDisplayName() {
            return Bundle.DN_CurrentFolder(folder.getName());
        }
        @Override
        public Icon getIcon() {
            return ImageUtilities.image2Icon(folder.getNodeDelegate().getIcon(1));
        }

        @Override
        public Scope getScope(AtomicBoolean cancel) {
            return Scope.create(null, null, folder.files());
        }
        
        public String getId() {
            return "*currentFolder"; // NOI18N
        }
    }

    private static final class FileScopeDescription implements ScopeDescription {
        private static final Logger LOG = Logger.getLogger(FileScopeDescription.class.getName());
        private final FileObject file;
        public FileScopeDescription(FileObject file) {
            this.file = file;
        }
        @Override
        @Messages({"# {0} - file display name", "DN_CurrentFile=Current File ({0})"})
        public String getDisplayName() {
            return Bundle.DN_CurrentFile(file.getNameExt());
        }
        @Override
        public Icon getIcon() {
            try {
                DataObject d = DataObject.find(file);
                Node n = d.getNodeDelegate();
                return ImageUtilities.image2Icon(n.getIcon(BeanInfo.ICON_COLOR_16x16));
            } catch (DataObjectNotFoundException ex) {
                LOG.log(Level.FINE, null, ex);
                return null;
            }
        }

        @Override
        public Scope getScope(AtomicBoolean cancel) {
            return Scope.create(null, null, Collections.singletonList(file));
        }
        
        public String getId() {
            return "*currentFile";
        }
    }

    private static final class ScopeRenderer extends DefaultListCellRenderer {

        @Override public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected, boolean cellHasFocus) {
            if (value instanceof ScopeDescription) {
                ScopeDescription sd = (ScopeDescription) value;

                try {
                    return super.getListCellRendererComponent(list, sd.getDisplayName(), index, isSelected, cellHasFocus);
                } finally {
                    setIcon(sd.getIcon());
                }
            }

            return super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
        }
    }

    private static class Separator implements Border {

        private Color fgColor;

        Separator(Color color) {
            fgColor = color;
        }

        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics gr = g.create();
            if (gr != null) {
                try {
                    gr.translate(x, y);
                    gr.setColor(fgColor);
                    gr.drawLine(0, height - 1, width - 1, height - 1);
                } finally {
                    gr.dispose();
                }
            }
        }

        public boolean isBorderOpaque() {
            return true;
        }

        public Insets getBorderInsets(Component c) {
            return new Insets(0, 0, 1, 0);
        }
    }

    private static final class AnalyzerAndWarning {
        private final AnalyzerFactory analyzer;
        private final WarningDescription wd;
        public AnalyzerAndWarning(AnalyzerFactory analyzer, WarningDescription wd) {
            this.analyzer = analyzer;
            this.wd = wd;
        }
    }
    
    public static final class DialogState {
        private final boolean configurationsSelected;
        private final String  selectedAnalyzer;
        private final String  selectedConfiguration;
        private final String  selectedInspection;
        private final String  scope;

        private DialogState(boolean configurationsSelected, String selectedAnalyzer, String selectedConfiguration, String selectedInspection, String scope) {
            this.configurationsSelected = configurationsSelected;
            this.selectedAnalyzer = selectedAnalyzer;
            this.selectedConfiguration = selectedConfiguration;
            this.selectedInspection = selectedInspection;
            this.scope = scope;
        }
        
        public void save() {
            Preferences prefs = NbPreferences.forModule(RunAnalysisPanel.class).node("RunAnalysisPanel");
            
            prefs.putBoolean("configurationsSelected", configurationsSelected);
            if (selectedAnalyzer != null)
                prefs.put("selectedAnalyzer", selectedAnalyzer);
            else
                prefs.remove("selectedAnalyzer");
            if (selectedConfiguration != null)
                prefs.put("selectedConfiguration", selectedConfiguration);
            else
                prefs.remove("selectedConfiguration");
            if (selectedInspection != null)
                prefs.put("selectedInspection", selectedInspection);
            else
                prefs.remove("selectedInspection");
        }
        
        private static DialogState load() {
            Preferences prefs = NbPreferences.forModule(RunAnalysisPanel.class).node("RunAnalysisPanel");
            return new DialogState(prefs.getBoolean("configurationsSelected", true),
                                   prefs.get("selectedAnalyzer", null),
                                   prefs.get("selectedConfiguration", null),
                                   prefs.get("selectedInspection", null),
                                   null);
        }
        
        public static DialogState from(WarningDescription wd) {
            return new DialogState(false, null, null, SPIAccessor.ACCESSOR.getWarningId(wd), null);
        }
    }
    
    public static class FutureWarnings {
        private Map<AnalyzerFactory, Map<String, WarningDescription>> result;
        public synchronized boolean isDone() {
            return result != null;
        }
        public synchronized Map<AnalyzerFactory, Map<String, WarningDescription>> get() {
            while (result == null) {
                try {
                    wait();
                } catch (InterruptedException ex) {
                    //Should not be happening, hopefully:
                    Logger.getLogger(RunAnalysisPanel.class.getName()).log(Level.FINE, null, ex);
                }
            }
            return result;
        }
        synchronized void set(Map<AnalyzerFactory, Map<String, WarningDescription>> value) {
            result = value;
            notifyAll();
        }
    }
}
