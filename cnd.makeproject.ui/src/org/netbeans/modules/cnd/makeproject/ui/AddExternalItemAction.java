/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 1997-2010 Oracle and/or its affiliates. All rights reserved.
 *
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates.
 * Other names may be trademarks of their respective owners.
 *
 * The contents of this file are subject to the terms of either the GNU
 * General Public License Version 2 only ("GPL") or the Common
 * Development and Distribution License("CDDL") (collectively, the
 * "License"). You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.netbeans.org/cddl-gplv2.html
 * or nbbuild/licenses/CDDL-GPL-2-CP. See the License for the
 * specific language governing permissions and limitations under the
 * License.  When distributing the software, include this License Header
 * Notice in each file and include the License file at
 * nbbuild/licenses/CDDL-GPL-2-CP.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the GPL Version 2 section of the License file that
 * accompanied this code. If applicable, add the following below the
 * License Header, with the fields enclosed by brackets [] replaced by
 * your own identifying information:
 * "Portions Copyrighted [year] [name of copyright owner]"
 *
 * Contributor(s):
 *
 * The Original Software is NetBeans. The Initial Developer of the Original
 * Software is Sun Microsystems, Inc. Portions Copyright 1997-2007 Sun
 * Microsystems, Inc. All Rights Reserved.
 *
 * If you wish your version of this file to be governed by only the CDDL
 * or only the GPL Version 2, indicate your decision by adding
 * "[Contributor] elects to include this software in this distribution
 * under the [CDDL or GPL Version 2] license." If you do not indicate a
 * single choice of license, a recipient has the option to distribute
 * your version of this file under either the CDDL, the GPL Version 2 or
 * to extend the choice of license to its licensees as provided above.
 * However, if you add GPL Version 2 code and therefore, elected the GPL
 * Version 2 license, then the option applies only if the new code is
 * made subject to such option by the copyright holder.
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
