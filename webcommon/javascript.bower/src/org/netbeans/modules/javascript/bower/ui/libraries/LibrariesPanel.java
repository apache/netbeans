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

package org.netbeans.modules.javascript.bower.ui.libraries;

import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.GroupLayout;
import javax.swing.JPanel;
import org.netbeans.api.options.OptionsDisplayer;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.modules.javascript.bower.exec.BowerExecutable;
import org.netbeans.modules.javascript.bower.file.BowerJson;
import org.netbeans.modules.javascript.bower.ui.options.BowerOptionsPanelController;
import org.netbeans.modules.web.common.api.UsageLogger;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;

/**
 * Panel for customization of Bower dependencies/library.
 *
 * @author Jan Stola
 */
public class LibrariesPanel extends JPanel implements HelpCtx.Provider {
    /** Request processor used by this class. */
    private static final RequestProcessor RP = new RequestProcessor(LibrariesPanel.class);
    /** Project whose Bower libraries are being customized. */
    private final Project project;
    /** Installed Bower libraries (maps the name to the installed version). */
    private Map<String,String> installedLibraries;
    /** Panels for customization of dependencies. */
    private DependenciesPanel[] dependencyPanels;

    /**
     * Creates a new {@code LibrariesPanel}.
     *
     * @param project project whose libraries should be customized.
     */
    public LibrariesPanel(Project project) {
        this.project = project;
        initComponents();
        BowerJson bowerJson = getBowerJson();
        if (bowerJson.exists()) {
            dependencyPanels = new DependenciesPanel[] {regularPanel, developmentPanel};
            regularPanel.setDependencyType(Dependency.Type.REGULAR);
            developmentPanel.setDependencyType(Dependency.Type.DEVELOPMENT);
            BowerJson.BowerDependencies bowerDependencies = bowerJson.getDependencies();
            DependenciesPanel.Dependencies dependencies = new DependenciesPanel.Dependencies(bowerDependencies);
            for (DependenciesPanel dependencyPanel : dependencyPanels) {
                dependencyPanel.setProject(project);
                dependencyPanel.setDependencies(dependencies);
            }
            loadInstalledLibraries();
        } else {
            show(bowerJsonProblemLabel);
        }
    }

    @Override
    public HelpCtx getHelpCtx() {
        return new HelpCtx("org.netbeans.modules.javascript.bower.ui.libraries.LibrariesPanel"); // NOI18N
    }

    /**
     * Shows the given component in the main area of the customizer.
     *
     * @param component component to show.
     */
    private void show(Component component) {
        assert EventQueue.isDispatchThread();
        GroupLayout layout = (GroupLayout)getLayout();
        Component currentComponent = getComponent(0);
        layout.replace(currentComponent, component);
    }

    /**
     * Creates a store listener (the listener that is invoked when
     * the changes in the project customizer are confirmed).
     *
     * @return store listener.
     */
    ActionListener createStoreListener() {
        return new StoreListener();
    }

    /**
     * Returns {@code bower.json} for the project.
     *
     * @return {@code bower.json} for the project.
     */
    private BowerJson getBowerJson() {
        return new BowerJson(project.getProjectDirectory());
    }

    /**
     * Converts the library-to-version map to the list of {@code Library.Version}s.
     *
     * @param map maps library name to library version.
     * @return list of {@code Library.Version}s that corresponds to the given map.
     */
    static List<Library.Version> toLibraries(Map<String,String> map) {
        List<Library.Version> libraries = new ArrayList<>(map.size());
        for (Map.Entry<String,String> entry : map.entrySet()) {
            Library library = new Library(entry.getKey());
            Library.Version version = new Library.Version(library, entry.getValue());
            libraries.add(version);
        }
        return libraries;
    }

    /**
     * Loads the libraries installed in the project. Updates
     * the view once the installed libraries are determined.
     */
    private void loadInstalledLibraries() {
        show(loadingLabel);
        RP.post(new Runnable() {
            @Override
            public void run() {
                LibraryProvider provider = LibraryProvider.forProject(project);
                installedLibraries = provider.installedLibraries();

                EventQueue.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        if (installedLibraries == null) {
                            show(bowerProblemPanel);
                        } else {
                            regularPanel.setInstalledLibraries(installedLibraries);
                            developmentPanel.setInstalledLibraries(installedLibraries);
                            show(tabbedPane);
                        }
                    }
                });
            }
        });
    }

    /** Progress handle used when storing changes. */
    private ProgressHandle progressHandle;
    /** Determines whether library usage should be logged. */
    private boolean logLibraryUsage;

    /**
     * Performs/stores the changes requested by the user in the customizer.
     */
    @NbBundle.Messages({
        "LibrariesPanel.updatingPackages=Updating Bower packages...",
        "LibrariesPanel.updatingBowerJson=Updating bower.json."
    })
    void storeChanges() {
        if (installedLibraries == null) {
            return; // 255276
        }
        RP.post(new Runnable() {
            @Override
            public void run() {
                progressHandle = ProgressHandle.createHandle(Bundle.LibrariesPanel_updatingPackages());
                progressHandle.start();
                logLibraryUsage = false;
                try {
                    BowerJson bowerJson = getBowerJson();
                    if (bowerJson.exists()) {
                        BowerJson.BowerDependencies dependencies = bowerJson.getDependencies();
                        List<String> errors = new ArrayList<>();

                        // Uninstall packages that are no longer needed
                        for (DependenciesPanel dependencyPanel : dependencyPanels) {
                            Dependency.Type dependencyType = dependencyPanel.getDependencyType();
                            uninstallDependencies(getBowerJsonDependencies(dependencies, dependencyType),
                                    dependencyPanel.getSelectedDependencies(),
                                    dependencyType, errors);
                        }

                        // Install missing packages
                        for (DependenciesPanel dependencyPanel : dependencyPanels) {
                            Dependency.Type dependencyType = dependencyPanel.getDependencyType();
                            installDependencies(dependencyPanel.getSelectedDependencies(),
                                    dependencyType, errors);
                        }

                        // Update (required versions in) bower.json
                        progressHandle.progress(Bundle.LibrariesPanel_updatingBowerJson());
                        // Both unistallDependencies and installDependencies modify bower.json externally => refresh
                        bowerJson.refresh();
                        for (DependenciesPanel dependencyPanel : dependencyPanels) {
                            Dependency.Type dependencyType = dependencyPanel.getDependencyType();
                            updateBowerJson(dependencyPanel.getSelectedDependencies(),
                                    dependencyType, errors);
                        }

                        reportErrors(errors);

                        if (logLibraryUsage) {
                            logLibraryUsage();
                        }
                    }
                } finally {
                    progressHandle.finish();
                    progressHandle = null;
                }
            }
        });
    }

    /**
     * Returns dependencies from {@code bower.json} of the given type.
     *
     * @param dependencies dependencies from {@code bower.json}.
     * @param dependencyType requested type of dependencies.
     * @return dependencies of the given type.
     */
    private Map<String,String> getBowerJsonDependencies(
            BowerJson.BowerDependencies dependencies,
            Dependency.Type dependencyType) {
        Map<String,String> map;
        switch (dependencyType) {
            case REGULAR: map = dependencies.dependencies; break;
            case DEVELOPMENT: map = dependencies.devDependencies; break;
            default: throw new IllegalArgumentException();
        }
        return map;
    }

    /**
     * Returns save parameter ({@code --save(-dev)}) for the given dependency type.
     *
     * @param dependencyType requested type of save parameter.
     * @return save parameter for the given dependency type.
     */
    private String getSaveParameter(Dependency.Type dependencyType) {
        String saveParameter;
        switch (dependencyType) {
            case REGULAR: saveParameter = BowerExecutable.SAVE_PARAM; break;
            case DEVELOPMENT: saveParameter = BowerExecutable.SAVE_DEV_PARAM; break;
            default: throw new IllegalArgumentException();
        }
        return saveParameter;
    }

    /**
     * Returns the name of the {@code bower.json} section where the dependencies
     * of the given type are stored.
     *
     * @param dependencyType requested type of the section.
     * @return name of the section where the dependencies of the given type are stored.
     */
    private String getBowerJsonSection(Dependency.Type dependencyType) {
        String section;
        switch (dependencyType) {
            case REGULAR: section = BowerJson.FIELD_DEPENDENCIES; break;
            case DEVELOPMENT: section = BowerJson.FIELD_DEV_DEPENDENCIES; break;
            default: throw new IllegalArgumentException();
        }
        return section;
    }

    /**
     * Notifies the user about errors that occurred while storing changes.
     *
     * @param errors list of error messages (possibly empty).
     */
    private void reportErrors(List<String> errors) {
        if (!errors.isEmpty()) {
            StringBuilder message = new StringBuilder();
            for (String error : errors) {
                if (message.length() != 0) {
                    message.append('\n');
                }
                message.append(error);
            }
            NotifyDescriptor descriptor = new NotifyDescriptor.Message(
                    message.toString(), NotifyDescriptor.ERROR_MESSAGE);
            DialogDisplayer.getDefault().notifyLater(descriptor);
        }
    }

    /**
     * Un-installs the dependencies that are no longer needed.
     *
     * @param originalDependencies original dependencies.
     * @param selectedDependencies requested list of dependencies.
     * @param dependencyType dependency type.
     * @param errors collection that should be populated with errors that occurred.
     */
    @NbBundle.Messages({
        "# {0} - library name",
        "LibrariesPanel.uninstallationFailed=Un-installation of package {0} failed!",
        "# {0} - library name",
        "LibrariesPanel.uninstallingPackage=Un-installing package {0}."
    })
    private void uninstallDependencies(Map<String,String> originalDependencies,
            List<Dependency> selectedDependencies,
            Dependency.Type dependencyType, List<String> errors) {
        BowerExecutable executable = BowerExecutable.getDefault(project, false);
        if (executable != null) {
            Map<String, String> selectedMap = new HashMap<>();
            for (Dependency dependency : selectedDependencies) {
                selectedMap.put(dependency.getName(), dependency.getInstalledVersion());
            }
            String saveParameter = getSaveParameter(dependencyType);
            for (String name : originalDependencies.keySet()) {
                if (!selectedMap.containsKey(name) ||
                        (installedLibraries.containsKey(name) && !selectedMap.get(name).equals(installedLibraries.get(name)))) {
                    progressHandle.progress(Bundle.LibrariesPanel_uninstallingPackage(name));
                    installedLibraries.remove(name);
                    Integer result = null;
                    try {
                        // bower uninstall --save(-dev) name
                        Future<Integer> future = executable.uninstall(saveParameter, name);
                        if (future != null) {
                            result = future.get();
                        }
                    } catch (InterruptedException | ExecutionException ex) {
                        Logger.getLogger(LibrariesPanel.class.getName()).log(Level.INFO, null, ex);
                    }
                    if (result == null || result != 0) {
                        errors.add(Bundle.LibrariesPanel_uninstallationFailed(name));
                    }
                    logLibraryUsage = true;
                }
            }
        }
    }

    /**
     * Installs the missing dependencies.
     *
     * @param selectedDependencies requested list of dependencies.
     * @param dependencyType dependency type.
     * @param errors collection that should be populated with errors that occurred.
     */
    @NbBundle.Messages({
        "# {0} - library name",
        "# {1} - library version",
        "LibrariesPanel.dependencyNotSet=Unable to set {0}@{1} dependency in bower.json!",
        "# {0} - library name",
        "# {1} - library version",
        "LibrariesPanel.installationFailed=Installation of version {1} of package {0} failed!",
        "# {0} - library name",
        "# {1} - library version",
        "LibrariesPanel.installingPackage=Installing version {1} of package {0}."
    })
    private void installDependencies(List<Dependency> selectedDependencies,
            Dependency.Type dependencyType, List<String> errors) {
        BowerExecutable executable = BowerExecutable.getDefault(project, false);
        if (executable != null) {
            for (Dependency dependency : selectedDependencies) {
                String name = dependency.getName();
                String versionToInstall = dependency.getInstalledVersion();
                String installedVersion = installedLibraries.get(name);
                if (versionToInstall != null && !versionToInstall.equals(installedVersion)) {
                    progressHandle.progress(Bundle.LibrariesPanel_installingPackage(name, versionToInstall));
                    Integer result = null;
                    try {
                        // bower install name#versionToInstall
                        String saveParameter = getSaveParameter(dependencyType);
                        String versionParameter;
                        if (Library.Version.LATEST_VERSION_PLACEHOLDER.equals(versionToInstall)) {
                            versionParameter = name;
                        } else {
                            versionParameter = name + "#" + versionToInstall; // NOI18N
                        }
                        Future<Integer> future = executable.install(true, saveParameter, versionParameter);
                        if (future != null) {
                            result = future.get();
                        }
                    } catch (InterruptedException | ExecutionException ex) {
                        Logger.getLogger(LibrariesPanel.class.getName()).log(Level.INFO, null, ex);
                    }
                    if (result == null || result != 0) {
                        errors.add(Bundle.LibrariesPanel_installationFailed(name, versionToInstall));
                    }
                    logLibraryUsage = true;
                }
            }
        }
    }

    /**
     * Updates required versions in {@code bower.json} (to match the ones
     * required by the user).
     *
     * @param selectedDependencies requested list of dependencies.
     * @param dependencyType dependency type.
     * @param errors collection that should be populated with errors that occurred.
     */
    private void updateBowerJson(List<Dependency> selectedDependencies,
            Dependency.Type dependencyType, List<String> errors) {
        BowerJson bowerJson = getBowerJson();
        if (bowerJson.exists()) {
            String section = getBowerJsonSection(dependencyType);
            Map<String,String> currentDependencies = getBowerJsonDependencies(bowerJson.getDependencies(), dependencyType);
            for (Dependency dependency : selectedDependencies) {
                String name = dependency.getName();
                String currentRequiredVersion = currentDependencies.get(name);
                String newRequiredVersion = dependency.getRequiredVersion();
                if (!newRequiredVersion.equals(currentRequiredVersion)) {
                    try {
                        bowerJson.setContent(Arrays.asList(section, name), newRequiredVersion);
                    } catch (IOException ioex) {
                        Logger.getLogger(LibrariesPanel.class.getName()).log(Level.INFO, null, ioex);
                        errors.add(Bundle.LibrariesPanel_dependencyNotSet(name, newRequiredVersion));
                    }
                    logLibraryUsage = true;
                }
            }
        }
    }

    /** Logger of Bower library UI usage. */
    private static final UsageLogger USAGE_LOGGER = new UsageLogger.Builder("org.netbeans.ui.metrics.javascript.bower")  // NOI18N
            .message(LibrariesPanel.class, "USG_BOWER_LIBRARY_EDIT") // NOI18N
            .create();

    /**
     * Logs the UI usage.
     */
    private void logLibraryUsage() {
        USAGE_LOGGER.log();
    }

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        bowerJsonProblemLabel = new javax.swing.JLabel();
        bowerProblemPanel = new javax.swing.JPanel();
        buttonPanel = new javax.swing.JPanel();
        configureButton = new javax.swing.JButton();
        retryButton = new javax.swing.JButton();
        bowerProblemLabel = new javax.swing.JLabel();
        loadingLabel = new javax.swing.JLabel();
        tabbedPane = new javax.swing.JTabbedPane();
        regularPanel = new org.netbeans.modules.javascript.bower.ui.libraries.DependenciesPanel();
        developmentPanel = new org.netbeans.modules.javascript.bower.ui.libraries.DependenciesPanel();

        bowerJsonProblemLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(bowerJsonProblemLabel, org.openide.util.NbBundle.getMessage(LibrariesPanel.class, "LibrariesPanel.bowerJsonProblemLabel.text")); // NOI18N
        bowerJsonProblemLabel.setEnabled(false);
        bowerJsonProblemLabel.setMaximumSize(new java.awt.Dimension(Short.MAX_VALUE, Short.MAX_VALUE));

        org.openide.awt.Mnemonics.setLocalizedText(configureButton, org.openide.util.NbBundle.getMessage(LibrariesPanel.class, "LibrariesPanel.configureButton.text")); // NOI18N
        configureButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                configureButtonActionPerformed(evt);
            }
        });

        org.openide.awt.Mnemonics.setLocalizedText(retryButton, org.openide.util.NbBundle.getMessage(LibrariesPanel.class, "LibrariesPanel.retryButton.text")); // NOI18N
        retryButton.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                retryButtonActionPerformed(evt);
            }
        });

        javax.swing.GroupLayout buttonPanelLayout = new javax.swing.GroupLayout(buttonPanel);
        buttonPanel.setLayout(buttonPanelLayout);
        buttonPanelLayout.setHorizontalGroup(
            buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonPanelLayout.createSequentialGroup()
                .addComponent(configureButton)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(retryButton))
        );

        buttonPanelLayout.linkSize(javax.swing.SwingConstants.HORIZONTAL, new java.awt.Component[] {configureButton, retryButton});

        buttonPanelLayout.setVerticalGroup(
            buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(buttonPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                .addComponent(configureButton)
                .addComponent(retryButton))
        );

        org.openide.awt.Mnemonics.setLocalizedText(bowerProblemLabel, org.openide.util.NbBundle.getMessage(LibrariesPanel.class, "LibrariesPanel.bowerProblemLabel.text")); // NOI18N

        javax.swing.GroupLayout bowerProblemPanelLayout = new javax.swing.GroupLayout(bowerProblemPanel);
        bowerProblemPanel.setLayout(bowerProblemPanelLayout);
        bowerProblemPanelLayout.setHorizontalGroup(
            bowerProblemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bowerProblemPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addGroup(bowerProblemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.CENTER)
                    .addComponent(bowerProblemLabel)
                    .addComponent(buttonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        bowerProblemPanelLayout.setVerticalGroup(
            bowerProblemPanelLayout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(bowerProblemPanelLayout.createSequentialGroup()
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                .addComponent(bowerProblemLabel)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(buttonPanel, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        loadingLabel.setHorizontalAlignment(javax.swing.SwingConstants.CENTER);
        org.openide.awt.Mnemonics.setLocalizedText(loadingLabel, org.openide.util.NbBundle.getMessage(LibrariesPanel.class, "LibrariesPanel.loadingLabel.text")); // NOI18N
        loadingLabel.setEnabled(false);
        loadingLabel.setMaximumSize(new java.awt.Dimension(Short.MAX_VALUE, Short.MAX_VALUE));

        tabbedPane.addTab(org.openide.util.NbBundle.getMessage(LibrariesPanel.class, "LibrariesPanel.regularPanel.TabConstraints.tabTitle"), regularPanel); // NOI18N
        tabbedPane.addTab(org.openide.util.NbBundle.getMessage(LibrariesPanel.class, "LibrariesPanel.developmentPanel.TabConstraints.tabTitle"), developmentPanel); // NOI18N

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(this);
        this.setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabbedPane)
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addComponent(tabbedPane, javax.swing.GroupLayout.Alignment.TRAILING)
        );
    }// </editor-fold>//GEN-END:initComponents

    private void configureButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_configureButtonActionPerformed
        OptionsDisplayer.getDefault().open(BowerOptionsPanelController.OPTIONS_PATH);
    }//GEN-LAST:event_configureButtonActionPerformed

    private void retryButtonActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_retryButtonActionPerformed
        loadInstalledLibraries();
    }//GEN-LAST:event_retryButtonActionPerformed


    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JLabel bowerJsonProblemLabel;
    private javax.swing.JLabel bowerProblemLabel;
    private javax.swing.JPanel bowerProblemPanel;
    private javax.swing.JPanel buttonPanel;
    private javax.swing.JButton configureButton;
    private org.netbeans.modules.javascript.bower.ui.libraries.DependenciesPanel developmentPanel;
    private javax.swing.JLabel loadingLabel;
    private org.netbeans.modules.javascript.bower.ui.libraries.DependenciesPanel regularPanel;
    private javax.swing.JButton retryButton;
    private javax.swing.JTabbedPane tabbedPane;
    // End of variables declaration//GEN-END:variables

    /**
     * Listener invoked when the changes in the project customizer are confirmed.
     */
    private class StoreListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            storeChanges();
        }

    }

}
