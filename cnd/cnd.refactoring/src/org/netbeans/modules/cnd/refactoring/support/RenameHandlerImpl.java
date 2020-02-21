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

package org.netbeans.modules.cnd.refactoring.support;

import java.io.IOException;
import javax.swing.Action;
import org.netbeans.modules.cnd.api.model.CsmFile;
import org.netbeans.modules.cnd.modelutil.CsmUtilities;
import org.netbeans.modules.cnd.source.spi.RenameHandler;
import org.netbeans.modules.refactoring.api.ui.ExplorerContext;
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
import org.openide.ErrorManager;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 * based on RenameHandlerImpl from refactoring.java
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.modules.cnd.source.spi.RenameHandler.class)
public class RenameHandlerImpl implements RenameHandler {

    @Override
    public void handleRename(Node node, String newName) {
        DataObject dob = node.getCookie(DataObject.class);
        CsmFile[] csmFiles = CsmUtilities.getCsmFiles(dob, false, false);
        if (csmFiles != null && csmFiles.length > 0) {
            InstanceContent ic = new InstanceContent();
            // pass new name (without extension as needed by dob.rename)
            ExplorerContext explorerContext = new ExplorerContext();
            explorerContext.setNewName(newName);
            ic.add(explorerContext);
            for (CsmFile file : csmFiles) {
                ic.add(file);
            }
            Lookup l = new AbstractLookup(ic);
            Action a = RefactoringActionsFactory.renameAction().createContextAwareInstance(l);
            if (Boolean.TRUE.equals(a.getValue("applicable"))) {//NOI18N
                a.actionPerformed(RefactoringActionsFactory.DEFAULT_EVENT);
                return;
            }
        }
        try {
            dob.rename(newName);
        } catch (IOException ioe) {
            ErrorManager.getDefault().notify(ioe);
        }
    }
}
