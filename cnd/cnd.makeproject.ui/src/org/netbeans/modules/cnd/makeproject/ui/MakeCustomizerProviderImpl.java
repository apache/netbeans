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
package org.netbeans.modules.cnd.makeproject.ui;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EnumMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import javax.swing.JButton;
import javax.swing.JComponent;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.cnd.makeproject.api.MakeCustomizerProvider;
import org.netbeans.modules.cnd.makeproject.api.MakeProject;
import org.netbeans.modules.cnd.makeproject.api.MakeProjectLookupProvider;
import org.netbeans.modules.cnd.makeproject.api.MakeSharabilityQuery;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.LibraryItem.ProjectItem;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.ui.customizer.MakeContext;
import org.netbeans.modules.cnd.makeproject.ui.customizer.MakeCustomizer;
import org.netbeans.spi.project.ui.CustomizerProvider;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.lookup.ServiceProvider;

/**
 * Customization of Make project shows dialog
 */
public class MakeCustomizerProviderImpl implements MakeCustomizerProvider, CustomizerProvider {
    
    @ServiceProvider(service = MakeProjectLookupProvider.class)
    public static class MakeCustomizerProviderFactory implements MakeProjectLookupProvider {

        @Override
        public void addLookup(MakeProject owner, ArrayList<Object> ic) {
            ic.add(new MakeCustomizerProviderImpl(owner));
        }
    }

    private final MakeProject project;
    // Option indexes
    private static final int OPTION_OK = 0;
    private static final int OPTION_CANCEL = 1;
    private static final int OPTION_APPLY = 2;
    // Option command names
    public static final String COMMAND_OK = "OK";          // NOI18N
    public static final String COMMAND_CANCEL = "CANCEL";  // NOI18N
    public static final String COMMAND_APPLY = "APPLY";  // NOI18N
    private DialogDescriptor dialogDescriptor;
    private final ConfigurationDescriptorProvider projectDescriptorProvider;
    private String currentCommand;
    private final Map<MakeContext.Kind, String> lastCurrentNodeName = new EnumMap<>(MakeContext.Kind.class);
    private final Set<ActionListener> actionListenerList = new HashSet<>();
    private static final RequestProcessor RP_SAVE = new RequestProcessor("MakeCustomizerProviderSave", 1); //NOI18N

    public MakeCustomizerProviderImpl(MakeProject project) {
        this.project = project;
        this.projectDescriptorProvider = project.getConfigurationDescriptorProvider();
    }

    @Override
    public void showCustomizer() {
        showCustomizer(lastCurrentNodeName.get(MakeContext.Kind.Project), null, null);
    }

    public void showCustomizer(Item item) {
        showCustomizer(lastCurrentNodeName.get(MakeContext.Kind.Item), Arrays.asList(item), null);
    }
    
    public void showCustomizer(Folder folder) {
        showCustomizer(lastCurrentNodeName.get(MakeContext.Kind.Folder), null, Arrays.asList(folder));
    }

    @Override
    public void showCustomizer(String preselectedNodeName) {
        showCustomizer(preselectedNodeName, null, null);
    }

    public void showCustomizer(final String preselectedNodeName, final List<Item> items, final List<Folder> folders) {
        if (!projectDescriptorProvider.gotDescriptor() || projectDescriptorProvider.getConfigurationDescriptor().getConfs().size() == 0) {
            //TODO: show warning dialog
            return;
        }
        showCustomizerWorker(preselectedNodeName, items, folders);
    }

    private void showCustomizerWorker(String preselectedNodeName, List<Item> items, List<Folder> folders) {
        if (folders != null) {
            for (Folder folder : folders) {
                if (folder != null) {
                    // Make sure all FolderConfigurations are created (they are lazyly created)
                    Configuration[] configurations = projectDescriptorProvider.getConfigurationDescriptor().getConfs().toArray();
                    for (int i = 0; i < configurations.length; i++) {
                        folder.getFolderConfiguration(configurations[i]);
                    }
                }
            }
        }

        // Make sure all languages are update
        projectDescriptorProvider.getConfigurationDescriptor().refreshRequiredLanguages();

        // Create options
        JButton options[] = new JButton[]{
            new JButton(NbBundle.getMessage(MakeCustomizerProviderImpl.class, "LBL_Customizer_Ok_Option")), // NOI18N
            new JButton(NbBundle.getMessage(MakeCustomizerProviderImpl.class, "LBL_Customizer_Cancel_Option")), // NOI18N
            new JButton(NbBundle.getMessage(MakeCustomizerProviderImpl.class, "LBL_Customizer_Apply_Option")), // NOI18N
        };

        // Set commands
        options[OPTION_OK].setActionCommand(COMMAND_OK);
        options[OPTION_OK].getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MakeCustomizerProviderImpl.class, "ACSD_Customizer_Ok_Option")); // NOI18N
        options[OPTION_CANCEL].setActionCommand(COMMAND_CANCEL);
        options[OPTION_CANCEL].getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MakeCustomizerProviderImpl.class, "ACSD_Customizer_Cancel_Option")); // NOI18N
        options[OPTION_APPLY].setActionCommand(COMMAND_APPLY);
        options[OPTION_APPLY].getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MakeCustomizerProviderImpl.class, "ACSD_Customizer_Apply_Option")); // NOI18N

        //A11Y
        options[OPTION_OK].getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MakeCustomizerProviderImpl.class, "AD_MakeCustomizerProviderOk")); // NOI18N
        options[OPTION_CANCEL].getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MakeCustomizerProviderImpl.class, "AD_MakeCustomizerProviderCancel")); // NOI18N
        options[OPTION_APPLY].getAccessibleContext().setAccessibleDescription(NbBundle.getMessage(MakeCustomizerProviderImpl.class, "AD_MakeCustomizerProviderApply")); // NOI18N

        // Mnemonics
        options[OPTION_APPLY].setMnemonic(NbBundle.getMessage(MakeCustomizerProviderImpl.class, "MNE_Customizer_Apply_Option").charAt(0)); // NOI18N

        // RegisterListener
        ConfigurationDescriptor clonedProjectdescriptor = projectDescriptorProvider.getConfigurationDescriptor().cloneProjectDescriptor();
        ArrayList<JComponent> controls = new ArrayList<>();
        controls.add(options[OPTION_OK]);
        MakeCustomizer innerPane = new MakeCustomizer(project, preselectedNodeName, clonedProjectdescriptor, items, folders, Collections.unmodifiableCollection(controls));
        ActionListener optionsListener = new OptionListener(project, projectDescriptorProvider.getConfigurationDescriptor(), clonedProjectdescriptor, innerPane, folders, items);
        options[OPTION_OK].addActionListener(optionsListener);
        options[OPTION_CANCEL].addActionListener(optionsListener);
        options[OPTION_APPLY].addActionListener(optionsListener);

        String dialogTitle;
        if (items != null && !items.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            items.forEach((i) -> {
                sb.append(i.getName()).append(", "); //NOI18N
            });
            String name = sb.toString().substring(0, sb.length() - 2);
            dialogTitle = NbBundle.getMessage(MakeCustomizerProviderImpl.class, "LBL_File_Customizer_Title", name); // NOI18N 
        } else if (folders != null && !folders.isEmpty()) {
            StringBuilder sb = new StringBuilder();
            folders.forEach((f) -> {
                sb.append(f.getName()).append(", "); //NOI18N
            });
            String name = sb.toString().substring(0, sb.length() - 2);
            dialogTitle = NbBundle.getMessage(MakeCustomizerProviderImpl.class, "LBL_Folder_Customizer_Title", name); // NOI18N 
        } else {
            dialogTitle = NbBundle.getMessage(MakeCustomizerProviderImpl.class, "LBL_Project_Customizer_Title", ProjectUtils.getInformation(project).getDisplayName()); // NOI18N 
        }

        dialogDescriptor = new DialogDescriptor(
                innerPane,                      // innerPane
                dialogTitle,
                true,                           // modal
                options,                        // options
                options[OPTION_OK],             // initial value
                DialogDescriptor.BOTTOM_ALIGN,  // options align
                null,                           // helpCtx
                null);                          // listener

        dialogDescriptor.setClosingOptions(new Object[]{options[OPTION_OK], options[OPTION_CANCEL]});
        innerPane.setDialogDescriptor(dialogDescriptor);
        Dialog dialog = DialogDisplayer.getDefault().createDialog(dialogDescriptor);
        
        currentCommand = COMMAND_CANCEL;

        try {
            dialog.setVisible(true);
        } catch (Throwable th) {
            if (!(th.getCause() instanceof InterruptedException)) {
                throw new RuntimeException(th);
            }
            dialogDescriptor.setValue(DialogDescriptor.CANCEL_OPTION);
        } finally {
            dialog.dispose();
        }

        MakeContext lastContext = innerPane.getLastContext();
        String nodeName = innerPane.getCurrentNodeName();
        if (lastContext != null) {
            lastCurrentNodeName.put(lastContext.getKind(), nodeName);
        }
        if (currentCommand.equals(COMMAND_CANCEL)) {
            fireActionEvent(new ActionEvent(project, 0, currentCommand));
        }
    }

    /**
     * Listens to the actions on the Customizer's option buttons
     */
    private final class OptionListener implements ActionListener {

        private final Project project;
        private final ConfigurationDescriptor projectDescriptor;
        private final ConfigurationDescriptor clonedProjectdescriptor;
        private final MakeCustomizer makeCustomizer;
        private final List<Folder> folders;
        private final List<Item> items;

        OptionListener(Project project, ConfigurationDescriptor projectDescriptor, ConfigurationDescriptor clonedProjectdescriptor, MakeCustomizer makeCustomizer, List<Folder> folders, List<Item> items) {
            this.project = project;
            this.projectDescriptor = projectDescriptor;
            this.clonedProjectdescriptor = clonedProjectdescriptor;
            this.makeCustomizer = makeCustomizer;
            this.folders = folders;
            this.items = items;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            currentCommand = e.getActionCommand();

            if (currentCommand.equals(COMMAND_OK) || currentCommand.equals(COMMAND_APPLY)) {
                makeCustomizer.save();
                //non UI actions such as as update of MakeConfiguration accessing filesystem should be invoked from non EDT
                RP_SAVE.post(() -> {
                    int previousVersion = projectDescriptor.getVersion();
                    int currentVersion = ConfigurationDescriptor.CURRENT_VERSION;
                    if (previousVersion < currentVersion) {
                        // Check
                        boolean issueRequiredProjectBuildWarning = false;
                        if (previousVersion < 76) {
                            for (Configuration configuration : projectDescriptor.getConfs().getConfigurations()) {
                                MakeConfiguration makeConfiguration = (MakeConfiguration) configuration;
                                if (!makeConfiguration.isMakefileConfiguration()) {
                                    continue;
                                }
                                List<ProjectItem> projectLinkItems = makeConfiguration.getRequiredProjectsConfiguration().getValue();
                                for (ProjectItem projectItem : projectLinkItems) {
                                    if (projectItem.getMakeArtifact().getBuild()) {
                                        issueRequiredProjectBuildWarning = true;
                                        break;
                                    }
                                }
                            }
                        }
                        
                        String txt;
                        
                        if (issueRequiredProjectBuildWarning) {
                            txt = getString("UPGRADE_RQ_TXT");
                        } else {
                            txt = getString("UPGRADE_TXT");
                        }
                        NotifyDescriptor d = new NotifyDescriptor.Confirmation(txt, getString("UPGRADE_DIALOG_TITLE"), NotifyDescriptor.YES_NO_OPTION); // NOI18N
                        if (DialogDisplayer.getDefault().notify(d) != NotifyDescriptor.YES_OPTION) {
                            return;
                        }
                        projectDescriptor.setVersion(currentVersion);
                    }
                    ConfigurationDescriptorProvider.SnapShot delta = null;
                    if (folders == null && items == null) { // project
                        ConfigurationDescriptorProvider cdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
                        delta = cdp.startModifications();
                    }
                    List<String> oldSourceRoots = ((MakeConfigurationDescriptor) projectDescriptor).getSourceRoots();
                    List<String> newSourceRoots = ((MakeConfigurationDescriptor) clonedProjectdescriptor).getSourceRoots();
                    List<String> oldTestRoots = ((MakeConfigurationDescriptor) projectDescriptor).getTestRoots();
                    List<String> newTestRoots = ((MakeConfigurationDescriptor) clonedProjectdescriptor).getTestRoots();
                    Configuration oldActive = projectDescriptor.getConfs().getActive();
                    if (oldActive != null) {
                        oldActive = oldActive.cloneConf();
                    }
                    Configuration[] oldConf = projectDescriptor.getConfs().toArray();
                    Configuration newActive = clonedProjectdescriptor.getConfs().getActive();
                    Configuration[] newConf = clonedProjectdescriptor.getConfs().toArray();
                    
                    projectDescriptor.assign(clonedProjectdescriptor);
                    projectDescriptor.getConfs().fireChangedConfigurations(oldConf, newConf);
                    projectDescriptor.setModified();
                    projectDescriptor.save(); // IZ 133606
                    
                    // IZ#179995
                    MakeSharabilityQuery query = project.getLookup().lookup(MakeSharabilityQuery.class);
                    if (query != null) {
                        query.update();
                    }
                    if (folders == null && items == null) { // project
                        ConfigurationDescriptorProvider cdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
                        cdp.endModifications(delta, true, null);
                    } else {
                        if (folders != null) {
                            folders.forEach((folder) -> {
                                ((MakeConfigurationDescriptor) projectDescriptor).checkForChangedItems(project, folder, null);
                            });
                        }
                        if (items != null) {
                            items.forEach((item) -> {
                                ((MakeConfigurationDescriptor) projectDescriptor).checkForChangedItems(project, null, item);
                            });
                        }
                    }
                    ((MakeConfigurationDescriptor) projectDescriptor).checkForChangedSourceRoots(oldSourceRoots, newSourceRoots);
                    ((MakeConfigurationDescriptor) projectDescriptor).checkForChangedTestRoots(oldTestRoots, newTestRoots);
                    ((MakeConfigurationDescriptor) projectDescriptor).checkConfigurations(oldActive, newActive);
                });

            }
            if (!currentCommand.equals(COMMAND_CANCEL)) {
                fireActionEvent(new ActionEvent(project, 0, currentCommand));
            }
            if (currentCommand.equals(COMMAND_APPLY)) {
                
                makeCustomizer.refresh();
            }

        }
    }

    @Override
    public void addActionListener(ActionListener cl) {
        synchronized (actionListenerList) {
            actionListenerList.add(cl);
        }
    }

    @Override
    public void removeActionListener(ActionListener cl) {
        synchronized (actionListenerList) {
            actionListenerList.remove(cl);
        }
    }

    private void fireActionEvent(ActionEvent e) {
        Iterator<ActionListener> it;

        synchronized (actionListenerList) {
            it = new HashSet<>(actionListenerList).iterator();
        }
        while (it.hasNext()) {
            it.next().actionPerformed(e);
        }
    }
    
    public String getLastCurrentNodeName(MakeContext.Kind kind) {
        return lastCurrentNodeName.get(kind);
    }
 
   /**
     * Look up i18n strings here
     */
    private static String getString(String s) {
        return NbBundle.getMessage(MakeCustomizerProviderImpl.class, s);
    }
}
