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

package org.netbeans.modules.refactoring.java;

import java.io.IOException;
import java.text.MessageFormat;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.refactoring.api.ui.ExplorerContext;
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
import org.netbeans.modules.refactoring.java.plugins.RenameRefactoringPlugin;
import org.netbeans.spi.java.project.support.ui.PackageRenameHandler;
import org.openide.DialogDisplayer;
import org.openide.ErrorManager;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Jan Becicka
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.java.project.support.ui.PackageRenameHandler.class)
public class PackageRenameHandlerImpl implements PackageRenameHandler {

    @Override
    public void handleRename(Node node, String newName) {
        DataFolder dob = (DataFolder) node.getCookie(DataObject.class);
        FileObject fo = dob.getPrimaryFile();
        if (node.isLeaf()) {
            //rename empty package and don't try to do any refactoring
            try {
                if (!RefactoringUtils.isValidPackageName(newName)) {
                    String msg = new MessageFormat(NbBundle.getMessage(RenameRefactoringPlugin.class,"ERR_InvalidPackage")).format(
                            new Object[] {newName}
                    );
                    
                    DialogDisplayer.getDefault().notify(new NotifyDescriptor.Message(
                        msg, NotifyDescriptor.INFORMATION_MESSAGE));
                    return;
                }
                ClassPath sourcepath = ClassPath.getClassPath(fo, ClassPath.SOURCE);
                if (sourcepath == null) {
                    throw new IOException("no sourcepath for " + fo);
                }
                FileObject root = sourcepath.findOwnerRoot(fo);
                if (root == null) {
                    throw new IOException(fo + " not in its own sourcepath " + sourcepath);
                }
                FileObject newFolder = FileUtil.createFolder(root, newName.replace('.','/'));
                while (dob.getChildren().length == 0 && dob.isDeleteAllowed() && !dob.getPrimaryFile().equals(newFolder)) {
                    DataFolder parent = dob.getFolder();
                    dob.delete();
                    dob = parent;
                }
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ioe);
            }
            return;
        }

        InstanceContent ic = new InstanceContent();
        ic.add(node);
        ExplorerContext d = new ExplorerContext();
        d.setNewName(newName);
        ic.add(d);
        final Lookup l = new AbstractLookup(ic);
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                Action a = RefactoringActionsFactory.renameAction().createContextAwareInstance(l);
                if (Boolean.TRUE.equals(a.getValue("applicable"))) { //NOI18N
                    a.actionPerformed(RefactoringActionsFactory.DEFAULT_EVENT);
                }
            }
        });
    }
}
