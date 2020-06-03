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
package org.netbeans.modules.cnd.makeproject.ui.actions;

import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.netbeans.api.actions.Openable;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.project.NativeFileItem;
import org.netbeans.modules.cnd.api.project.NativeFileItemSet;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.CCompilerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Configuration;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.FolderConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.LinkerConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfiguration;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.ui.MakeLogicalViewProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.loaders.TemplateWizard;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.actions.NodeAction;
import org.openide.util.actions.Presenter;
import org.openide.util.actions.SystemAction;

/**
 */
public final class NewTestActionFactory {

    private static final Logger LOGGER = Logger.getLogger("org.netbeans.modules.cnd.makeproject"); // NOI18N
    
    private NewTestActionFactory() {
    }

    public static Action[] getTestCreationActions(Project project) {
        ArrayList<Action> actions = new ArrayList<>();
        FileObject testFiles = FileUtil.getConfigFile("Templates/testFiles"); //NOI18N
        // if user deleted all test templates => folder is null
        if (testFiles != null) {
            if (testFiles.isFolder()) {
                for (FileObject test : testFiles.getChildren()) {
                    if (!"hidden".equals(test.getAttribute("templateCategory"))) { //NOI18N
                        actions.add(new NewTestAction(test, project, null, false));
                    }
                }
            }
        }
        return actions.toArray(new Action[actions.size()]);
    }

    public static Action createNewTestsSubmenu() {
        return SystemAction.get(CreateTestSubmenuAction.class);
    }

    public static Action emptyTestFolderAction() {
        return SystemAction.get(NewEmptyTestAction.class);
    }
    
    private static class NewTestAction extends AbstractAction {

        private final FileObject test;
        private final Project project;
        private final Lookup context;
        private final boolean generateCode;

        public NewTestAction(FileObject test, Project project, Lookup context, boolean generateCode) {
            super.putValue(NAME, NbBundle.getMessage(CreateTestSubmenuAction.class, "NewTestNameWrapper", getName(test)));
            super.putValue(SMALL_ICON, getIcon(test));
            this.test = test;
            this.project = project;
            this.context = context;
            this.generateCode = generateCode;
            this.setEnabled(false);
            ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
            if (pdp != null && pdp.gotDescriptor()) {
                MakeConfigurationDescriptor mcd = pdp.getConfigurationDescriptor();
                if(mcd != null) {
                    MakeConfiguration activeConfiguration = mcd.getActiveConfiguration();
                    if(activeConfiguration != null && activeConfiguration.getConfigurationType().getValue() != MakeConfiguration.TYPE_MAKEFILE) {
                        this.setEnabled(true);
                    }
                }
            }
        }

        public
        @Override
        void actionPerformed(ActionEvent e) {
            try {
                final TemplateWizard templateWizard = new TemplateWizard();
                Project aProject = project;
                templateWizard.putProperty("UnitTestContextLookup", context); // NOI18N
                templateWizard.putProperty("UnitTestCodeGeneration", generateCode); // NOI18N
                if (aProject == null) {
                    assert context != null;
                    Node node = context.lookup(Node.class);
                    if (node != null) {
                        FileObject fo = node.getLookup().lookup(FileObject.class);
                        if (fo != null) {
                            aProject = FileOwnerQuery.getOwner(fo);
                        }
                    }
                }
                templateWizard.putProperty("project", aProject); // NOI18N
                DataObject dob = DataObject.find(FileUtil.getConfigFile(test.getPath()));
                String title = templateWizard.getTitleFormat().format(new Object[] { dob.getNodeDelegate().getDisplayName() });
                templateWizard.setTitle(title);
                Set<DataObject> files = templateWizard.instantiate(dob);
                if (files != null && !files.isEmpty()) {
                    MakeConfigurationDescriptor mkd = getMakeConfigurationDescriptor(project);
                    if (mkd != null) {
                        String path = files.iterator().next().getPrimaryFile().getPath();
                        Item item = mkd.findProjectItemByPath(path);
                        if (item != null) {
                            MakeLogicalViewProvider.setVisible(project, item.getFolder());
                        } else {
                            LOGGER.log(Level.WARNING, "Can not find project item for {0}", path);
                        }
                        mkd.save();
                    } else {
                        LOGGER.warning("Can not get make configuration descriptor");
                    }
                    for (DataObject file : files) {
                        Openable open = file.getLookup().lookup(Openable.class);
                        if (open != null) {
                        open.open();
                        // org.netbeans.modules.project.ui.actions.NewFile would also select new file in Projects
                    }
                    }
                } // else wizard was canceled
            } catch (IOException x) {
                // log somehow
            }
        }

        private MakeConfigurationDescriptor getMakeConfigurationDescriptor(Project p) {
            ConfigurationDescriptorProvider pdp = p.getLookup().lookup(ConfigurationDescriptorProvider.class);
            if (pdp == null) {
                return null;
            }
            return pdp.getConfigurationDescriptor();
        }
        
        private String getName(FileObject test) {
            String bundleName = (String)test.getAttribute("SystemFileSystem.localizingBundle"); //NOI18N
            // User templates do not have bundles
            if (bundleName != null) {
                return NbBundle.getBundle(bundleName).getString(test.getPath());
            } else {
                return test.getName();
            }
        }

        private Icon getIcon(FileObject test) {
            URL url = (URL) test.getAttribute("SystemFileSystem.icon"); // NOI18N
            // User templates do not have icons
            if (url != null) {
                return ImageUtilities.loadImageIcon(url.getPath().substring(1), true);
            } else {
                return null;
            }
        }
    }

    public static class NewEmptyTestAction extends NodeAction {

        @Override
        public String getName() {
            return getString("NewEmptyTestActionName"); // NOI18N
        }

        @Override
        public void performAction(Node[] activatedNodes) {
            if (activatedNodes.length == 0) {
                return;
            }
            Node n = activatedNodes[0];
            Folder folder = (Folder) n.getValue("Folder"); // NOI18N
            assert folder != null;
            Node thisNode = (Node) n.getValue("This"); // NOI18N
            assert thisNode != null;
            Project project = (Project) n.getValue("Project"); // NOI18N
            assert project != null;

            ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
            MakeConfigurationDescriptor makeConfigurationDescriptor = pdp.getConfigurationDescriptor();
            if (!makeConfigurationDescriptor.okToChange()) {
                return;
            }

            NotifyDescriptor.InputLine dlg = new NotifyDescriptor.InputLine(getString("TestName"), getString("NewTest"));
            dlg.setInputText(folder.suggestedNewTestFolderName());
            String newname = null;

            if (NotifyDescriptor.OK_OPTION.equals(DialogDisplayer.getDefault().notify(dlg))) {
                newname = dlg.getInputText();
            } else {
                return;
            }

            Folder newFolder = folder.addNewFolder(true, Folder.Kind.TEST);
            newFolder.setDisplayName(newname);
            setOptions(project, newFolder);
            makeConfigurationDescriptor.save();
            MakeLogicalViewProvider.setVisible(project, newFolder);
        }
        
        private void setOptions(Project project, Folder testFolder) {
            ConfigurationDescriptorProvider cdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class);
            MakeConfigurationDescriptor projectDescriptor = cdp.getConfigurationDescriptor();
            for (Configuration conf : projectDescriptor.getConfs().getConfigurations()) {
                FolderConfiguration folderConfiguration = testFolder.getFolderConfiguration(conf);
                LinkerConfiguration linkerConfiguration = folderConfiguration.getLinkerConfiguration();
                linkerConfiguration.getOutput().setValue("${TESTDIR}/" + testFolder.getPath()); // NOI18N
                CCompilerConfiguration cCompilerConfiguration = folderConfiguration.getCCompilerConfiguration();
                CCCompilerConfiguration ccCompilerConfiguration = folderConfiguration.getCCCompilerConfiguration();
                cCompilerConfiguration.getIncludeDirectories().add("."); // NOI18N
                ccCompilerConfiguration.getIncludeDirectories().add("."); // NOI18N
            }
        }

        @Override
        public boolean enable(Node[] activatedNodes) {
            return true;
        }

        @Override
        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        @Override
        protected boolean asynchronous() {
            return false;
        }

        private String getString(String s) {
            return NbBundle.getBundle(NewTestActionFactory.class).getString(s);
        }
    }

    private final static class CreateTestSubmenuAction extends NodeAction {

        private LazyPopupMenu popupMenu;
        private final Collection<Action> items = new ArrayList<>(5);

        @Override
        public JMenuItem getPopupPresenter() {
            createSubMenu();
            return popupMenu;
        }

        @Override
        public JMenuItem getMenuPresenter() {
            createSubMenu();
            return popupMenu;
        }

        private void createSubMenu() {
            if (popupMenu == null) {
                popupMenu = new LazyPopupMenu(NbBundle.getMessage(CreateTestSubmenuAction.class, "CTL_TestAction"), items);
            }
            items.clear();
            Node[] nodes = getActivatedNodes();
            if (nodes != null && nodes.length == 1) {
                FileObject fo = nodes[0].getLookup().lookup(FileObject.class);
                if (fo != null) {
                    Project project = FileOwnerQuery.getOwner(fo);
                    if (project != null) {
                        items.addAll(createActions(project, fo));
                    }
                }
            }
            popupMenu.setEnabled(!items.isEmpty());
        }

        @Override
        protected void performAction(Node[] activatedNodes) {
        }

        @Override
        protected boolean enable(Node[] activatedNodes) {
            if (activatedNodes.length == 1) {
                NativeFileItemSet set = activatedNodes[0].getLookup().lookup(NativeFileItemSet.class);
                if (set != null && !set.isEmpty()) {
                    for (NativeFileItem nativeFileItem : set.getItems()) {
                        if (nativeFileItem instanceof Item) {
                            Item item = (Item) nativeFileItem;
                            Folder folder = item.getFolder();
                            if (folder != null && folder.isTest()) {
                                return false;
                            }
                        }
                    }
                    return true;
                }
            }
            return false;
        }

        @Override
        public String getName() {
            return NbBundle.getMessage(CreateTestSubmenuAction.class, "CTL_TestAction");
        }

        @Override
        public HelpCtx getHelpCtx() {
            return HelpCtx.DEFAULT_HELP;
        }

        private Collection<Action> createActions(Project project, FileObject fo) {
            ArrayList<Action> actions = new ArrayList<>();
            FileObject testFiles = FileUtil.getConfigFile("Templates/testFiles"); //NOI18N
            // Bug 195897
            // Templates/testFiles could be deleted
            if (testFiles != null) {
                if (testFiles.isFolder()) {
                    for (FileObject test : testFiles.getChildren()) {
                        if (Boolean.TRUE.equals(test.getAttribute("templateGenerator"))) { //NOI18N
                            String mimeTypes = (String) test.getAttribute("supportedMimeTypes"); //NOI18N
                            if (checkMimeType(mimeTypes, fo.getMIMEType())) {
                                actions.add(new NewTestAction(test, project, org.openide.util.Utilities.actionsGlobalContext(), true));
                            }
                        }
                    }
                }
            }
            return actions;
        }

        private boolean checkMimeType(String mimeTypes, String mimeType) {
            if (mimeTypes == null) {
                return true;
            }
            String[] split = mimeTypes.split(";"); // NOI18N
            for (String string : split) {
                if (mimeType.contentEquals(string)) {
                    return true;
                }
            }
            return false;
        }
    }

    private final static class LazyPopupMenu extends JMenu {

        private final Collection<Action> items;

        public LazyPopupMenu(String name, Collection<Action> items) {
            super(name);
            assert items != null : "array must be inited";
            this.items = items;
        }

        @Override
        public synchronized JPopupMenu getPopupMenu() {
            super.removeAll();
            // Some L&F call this method in constructor.
            // Work around bug #247145
            if (items != null) {
                items.forEach((action) -> {
                    if (action instanceof Presenter.Popup) {
                        JMenuItem item = ((Presenter.Popup) action).getPopupPresenter();
                        add(item);
                    } else if (action instanceof Presenter.Menu) {
                        JMenuItem item = ((Presenter.Menu) action).getMenuPresenter();
                        add(item);
                    } else {
                        add(action);
                    }
                });
            }
            return super.getPopupMenu();
        }
    }
}
