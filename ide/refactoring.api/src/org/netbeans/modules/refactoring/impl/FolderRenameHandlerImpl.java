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

package org.netbeans.modules.refactoring.impl;

import java.io.IOException;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.modules.refactoring.api.impl.ActionsImplementationFactory;
import org.netbeans.modules.refactoring.api.ui.ExplorerContext;
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
import org.openide.ErrorManager;
import org.openide.filesystems.FileObject;
import org.openide.loaders.DataFolder;
import org.openide.loaders.FolderRenameHandler;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Jan Becicka
 */
@org.openide.util.lookup.ServiceProvider(service=org.openide.loaders.FolderRenameHandler.class)
public class FolderRenameHandlerImpl implements FolderRenameHandler {

    @Override
    public void handleRename(DataFolder folder, String newName) {
        InstanceContent ic = new InstanceContent();
        ic.add(folder.getNodeDelegate());
        ExplorerContext d = new ExplorerContext();
        d.setNewName(newName);
        ic.add(d);
        final Lookup l = new AbstractLookup(ic);
        if (ActionsImplementationFactory.canRename(l)) {
            SwingUtilities.invokeLater(new Runnable() {
                @Override
                public void run() {
                    Action a = RefactoringActionsFactory.renameAction().createContextAwareInstance(l);
                    a.actionPerformed(RefactoringActionsFactory.DEFAULT_EVENT);
                }
            });
        } else {
            FileObject fo = folder.getPrimaryFile();
            try {
                folder.rename(newName);
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ioe);
            }
        }
    }
}
