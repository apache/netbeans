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

import java.awt.event.ActionEvent;
import java.io.File;
import java.util.ArrayList;
import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.api.remote.ui.RemoteFileChooserUtil;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.api.configurations.ConfigurationDescriptorProvider;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.api.configurations.MakeConfigurationDescriptor;
import org.netbeans.modules.cnd.makeproject.ui.utils.PathPanel;
import org.netbeans.modules.cnd.utils.CndPathUtilities;
import org.netbeans.modules.cnd.makeproject.api.ProjectSupport;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item.ItemFactory;
import org.netbeans.modules.nativeexecution.api.ExecutionEnvironment;
import org.netbeans.modules.remote.spi.FileSystemProvider;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.util.NbBundle;


public class AddExternalItemAction extends AbstractAction {
    private final Project project;

    public AddExternalItemAction(Project project) {
	putValue(NAME, NbBundle.getBundle(getClass()).getString("CTL_AddExternalItem")); //NOI18N
	this.project = project;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
	ConfigurationDescriptorProvider pdp = project.getLookup().lookup(ConfigurationDescriptorProvider.class );
	ConfigurationDescriptor projectDescriptor = pdp.getConfigurationDescriptor();
	MakeConfigurationDescriptor makeProjectDescriptor = (MakeConfigurationDescriptor)projectDescriptor;
        if (!makeProjectDescriptor.okToChange()) {
            return;
        }
        final String chooser_key = "AddExternalItem"; //NOI18N
        ExecutionEnvironment env = FileSystemProvider.getExecutionEnvironment(makeProjectDescriptor.getBaseDirFileSystem());
        String seed = RemoteFileChooserUtil.getCurrentChooserFile(chooser_key, env);
	if (seed == null) {
	    seed = makeProjectDescriptor.getBaseDir();
	}
	JFileChooser fileChooser = RemoteFileChooserUtil.createFileChooser(
                makeProjectDescriptor.getBaseDirFileSystem(), 
                NbBundle.getBundle(getClass()).getString("LBL_FileChooserTitle"), 
                NbBundle.getBundle(getClass()).getString("LBL_SelectButton"), 
                JFileChooser.FILES_AND_DIRECTORIES, null, seed, true);
	PathPanel pathPanel = new PathPanel();
	fileChooser.setAccessory(pathPanel);
	fileChooser.setMultiSelectionEnabled(true);
	int ret = fileChooser.showOpenDialog(null); // FIXUP
	if (ret == JFileChooser.CANCEL_OPTION) {
            return;
        }

	File[] files = fileChooser.getSelectedFiles();
	ArrayList<Item> items = new ArrayList<>();        
        if (files.length > 0) {
            File selectedFolder = files[0].isFile() ? files[0].getParentFile() : files[0];
            RemoteFileChooserUtil.setCurrentChooserFile(chooser_key, selectedFolder.getPath(), env);
        }
        for (File file : files) {
            if (!file.exists()) {
                String errormsg = NbBundle.getMessage(AddExternalItemAction.class, "FILE_DOESNT_EXISTS", file.getPath()); // NOI18N
                DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(errormsg, NotifyDescriptor.ERROR_MESSAGE));
                continue;
            }
            String itemPath = ProjectSupport.toProperPath(makeProjectDescriptor.getBaseDirFileObject(), file.getPath(), project);
            itemPath = CndPathUtilities.normalizeSlashes(itemPath);
            Item item = makeProjectDescriptor.getExternalItemFolder().findItemByPath(itemPath);
            if (item != null) {
                items.add(item);
            }
            else {
                item = ItemFactory.getDefault().createInFileSystem(makeProjectDescriptor.getBaseDirFileSystem(), itemPath);
                makeProjectDescriptor.getExternalItemFolder().addItem(item);
                items.add(item);
            }
        }
        if (items.size() > 0) {
            makeProjectDescriptor.save();
            MakeLogicalViewProvider.setVisible(project, items.toArray(new Item[items.size()]));
        }
    }
}
