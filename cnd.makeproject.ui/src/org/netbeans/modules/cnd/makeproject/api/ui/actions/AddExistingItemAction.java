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

package org.netbeans.modules.cnd.makeproject.api.ui.actions;

import java.io.File;
import java.util.ArrayList;
import java.util.ResourceBundle;
import javax.swing.JFileChooser;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.remote.ui.RemoteFileChooserUtil;
import org.netbeans.modules.cnd.utils.FileFilterFactory;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.ui.utils.PathPanel;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.makeproject.api.ProjectSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item.ItemFactory;
import org.netbeans.modules.cnd.makeproject.ui.MakeLogicalViewProvider;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.nodes.Node;
import org.openide.util.HelpCtx;
import org.openide.util.NbBundle;
import org.openide.util.RequestProcessor;
import org.openide.util.actions.NodeAction;

public class AddExistingItemAction extends NodeAction {
    
    private static final RequestProcessor RP = new RequestProcessor("AddExistingItemAction", 1); // NOI18N
    
    public AddExistingItemAction(){
        //TODO: uncomment when problem iwth MakeProjectLogicalViewRootNode folder will be fixed, now "Folder" can be null when it should not be null
        //putValue(DynamicMenuContent.HIDE_WHEN_DISABLED, true);
    }

    @Override
    protected boolean enable(Node[] activatedNodes)  {
        if (activatedNodes.length != 1) {
            return false;
        }
        Object project = activatedNodes[0].getValue("Project"); // NOI18N
        if (project == null || (!(project instanceof Project))) {
            return false;
        }
        
        Object o = activatedNodes[0].getValue("Folder"); // NOI18N
        if (!(o instanceof Folder)) {
            return false;
        }
        Folder folder = (Folder) o;
        if (!folder.isProjectFiles()) {
            return false;
        }
        return true;
    }

    @Override
    public String getName() {
	return getString("CTL_AddExistingItemAction"); // NOI18N
    }

    @Override
    public void performAction(Node[] activatedNodes) {
	Node n = activatedNodes[0];
	Project project = (Project)n.getValue("Project"); // NOI18N
	assert project != null;
	Folder folder = (Folder)n.getValue("Folder"); // NOI18N
	assert folder != null;

	ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class );
	MakeConfigurationDescriptor projectDescriptor = pdp.getConfigurationDescriptor();

        if (!projectDescriptor.okToChange()) {
            return;
        }
        final String chooser_key = "AddExistingItem"; //NOI18N
        ExecutionEnvironment env = FileSystemProvider.getExecutionEnvironment(projectDescriptor.getBaseDirFileSystem());
        String seed = RemoteFileChooserUtil.getCurrentChooserFile(chooser_key, env);
        if (seed == null) {
            seed = projectDescriptor.getBaseDir();
        }
        JFileChooser fileChooser = RemoteFileChooserUtil.createFileChooser(projectDescriptor.getBaseDirFileSystem(), getString("SelectItem"), getString("Select"),
                                   JFileChooser.FILES_ONLY, null, seed, false);                
	PathPanel pathPanel = new PathPanel();
	fileChooser.setAccessory(pathPanel);
	fileChooser.setMultiSelectionEnabled(true);
        fileChooser.addChoosableFileFilter(FileFilterFactory.getCSourceFileFilter());
        fileChooser.addChoosableFileFilter(FileFilterFactory.getCCSourceFileFilter());
        fileChooser.addChoosableFileFilter(FileFilterFactory.getHeaderSourceFileFilter());
        fileChooser.addChoosableFileFilter(FileFilterFactory.getFortranSourceFileFilter());
        fileChooser.addChoosableFileFilter(FileFilterFactory.getResourceFileFilter());
        fileChooser.addChoosableFileFilter(FileFilterFactory.getQtFileFilter());
        fileChooser.addChoosableFileFilter(FileFilterFactory.getAllSourceFileFilter());
        fileChooser.addChoosableFileFilter(FileFilterFactory.getAllFileFilter());
        fileChooser.setFileFilter(fileChooser.getAcceptAllFileFilter());
	int ret = fileChooser.showOpenDialog(null); // FIXUP
	if (ret == JFileChooser.CANCEL_OPTION) {
            return;
        }

        File[] files = fileChooser.getSelectedFiles();
        if (files.length > 0) {
            RemoteFileChooserUtil.setCurrentChooserFile(chooser_key, files[0].getParent(), env);
        }
        addFilesWorker(project, projectDescriptor, folder, files);
//	boolean notifySources = false;
//      ArrayList<Item> items = new ArrayList<Item>();
//	for (int i = 0; i < files.length; i++) {
//	    String itemPath;
//	    if (PathPanel.getMode() == PathPanel.REL_OR_ABS)
//		itemPath = CndPathUtilities.toAbsoluteOrRelativePath(projectDescriptor.getBaseDir(), files[i].getPath());
//	    else if (PathPanel.getMode() == PathPanel.REL)
//		itemPath = CndPathUtilities.toRelativePath(projectDescriptor.getBaseDir(), files[i].getPath());
//	    else
//		itemPath = files[i].getPath();
//	    itemPath = FilePathAdaptor.normalize(itemPath);
//	    if (((MakeConfigurationDescriptor)projectDescriptor).findProjectItemByPath(itemPath) != null) {
//		String errormsg = getString("AlreadyInProjectError", itemPath); // NOI18N
//		DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(errormsg, NotifyDescriptor.ERROR_MESSAGE));
//                //return;
//	    }
//	    else {
//                Item item = new Item(itemPath);
//		folder.addItemAction(item);
//                items.add(item);
//		if (CndPathUtilities.isPathAbsolute(itemPath))
//		    notifySources = true;
//	    }
//	}
//	MakeLogicalViewProvider.setVisible(project, items.toArray(new Item[items.size()]));
//
//	if (notifySources)
//	    ((MakeSources)ProjectUtils.getSources(project)).descriptorChanged();
    }

    private void addFilesWorker(final Project project, final MakeConfigurationDescriptor projectDescriptor, final Folder folder, final File[] files) {
        RP.post(() -> {
            boolean notifySources = false;
            ArrayList<Item> items = new ArrayList<>();
            ArrayList<String> sourceRoots = new ArrayList<>();
            for (int i = 0; i < files.length; i++) {
                String itemPath = ProjectSupport.toProperPath(projectDescriptor.getBaseDirFileObject(), files[i].getPath(), project);
                itemPath = CndPathUtilities.normalizeSlashes(itemPath);
                if (((MakeConfigurationDescriptor) projectDescriptor).findProjectItemByPath(itemPath) != null) {
                    String errormsg = getString("AlreadyInProjectError", itemPath); // NOI18N
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(errormsg, NotifyDescriptor.ERROR_MESSAGE));
                    //return;
                } else {
                    Item item = ItemFactory.getDefault().createInFileSystem(((MakeConfigurationDescriptor) projectDescriptor).getBaseDirFileSystem(), itemPath);
                    folder.addItemAction(item);
                    items.add(item);
                    if (CndPathUtilities.isPathAbsolute(itemPath)) {
                        notifySources = true;
                        String dirName = CndPathUtilities.getDirName(itemPath);
                        if (!sourceRoots.contains(dirName)) {
                            sourceRoots.add(dirName);
                        }
                    }
                }
            }
            if (notifySources && !sourceRoots.isEmpty()) {
                sourceRoots.forEach((root) -> {
                    projectDescriptor.addSourceRoot(root);
                });
            }
            MakeLogicalViewProvider.setVisible(project, items.toArray(new Item[items.size()]));
            projectDescriptor.save();
            if (notifySources) {
                //((MakeSources) ProjectUtils.getSources(project)).descriptorChanged();
            }
        });

    }

    @Override
    public HelpCtx getHelpCtx() {
	return null;
    }

    @Override
    protected boolean asynchronous() {
	return false;
    }
    
    /** Look up i18n strings here */
    private static ResourceBundle bundle;
    private static String getString(String s) {
	if (bundle == null) {
	    bundle = NbBundle.getBundle(AddExistingItemAction.class);
	}
	return bundle.getString(s);
    }
    private static String getString(String s, String arg) {
        return NbBundle.getMessage(AddExistingItemAction.class, s, arg);
    }
}
