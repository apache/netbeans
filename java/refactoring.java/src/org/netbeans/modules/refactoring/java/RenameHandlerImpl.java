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
import javax.swing.Action;
import org.netbeans.modules.refactoring.api.ui.ExplorerContext;
import org.netbeans.modules.refactoring.api.ui.RefactoringActionsFactory;
import org.netbeans.spi.java.loaders.RenameHandler;
import org.openide.ErrorManager;
import org.openide.loaders.DataObject;
import org.openide.nodes.Node;
import org.openide.util.Lookup;
import org.openide.util.lookup.AbstractLookup;
import org.openide.util.lookup.InstanceContent;

/**
 *
 * @author Jan Becicka
 */
@org.openide.util.lookup.ServiceProvider(service=org.netbeans.spi.java.loaders.RenameHandler.class)
public class RenameHandlerImpl implements RenameHandler {

    @Override
    public void handleRename(Node node, String newName) {
        InstanceContent ic = new InstanceContent();
        ic.add(node);
        ExplorerContext d = new ExplorerContext();
        d.setNewName(newName);
        ic.add(d);
        Lookup l = new AbstractLookup(ic);
        DataObject dob = node.getCookie(DataObject.class);
        Action a = RefactoringActionsFactory.renameAction().createContextAwareInstance(l);
        if (Boolean.TRUE.equals(a.getValue("applicable"))) {//NOI18N
            a.actionPerformed(RefactoringActionsFactory.DEFAULT_EVENT);
        } else {
            try {
                dob.rename(newName);
            } catch (IOException ioe) {
                ErrorManager.getDefault().notify(ioe);
            }
        }
    }
}
