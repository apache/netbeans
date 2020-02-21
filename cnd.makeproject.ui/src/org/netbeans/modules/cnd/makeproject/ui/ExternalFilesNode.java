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

import java.awt.Image;
import java.util.List;
import javax.swing.Action;
import org.netbeans.modules.cnd.makeproject.api.MakeProjectType;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.openide.nodes.AbstractNode;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.actions.SystemAction;
import org.openide.util.lookup.Lookups;

/**
 *
 */
final class ExternalFilesNode extends AbstractNode {

    private final Folder folder;
    private final MakeLogicalViewProvider provider;

    public ExternalFilesNode(Folder folder, MakeLogicalViewProvider provider) {
        super(new ExternalFilesChildren(folder, provider), Lookups.fixed(new Object[]{provider.getProject(), new FolderSearchInfo(folder)}));
        setName(folder.getName());
        setDisplayName(folder.getDisplayName());
        setShortDescription(NbBundle.getBundle(getClass()).getString("ONLY_REFERENCE_TXT"));
        this.folder = folder;
        this.provider = provider;
    }

    @Override
    public Object getValue(String valstring) {
        if (valstring == null) {
            return super.getValue(null);
        }
        if (valstring.equals("Folder")) // NOI18N
        {
            return folder;
        } else if (valstring.equals("Project")) // NOI18N
        {
            return provider.getProject();
        } else if (valstring.equals("This")) // NOI18N
        {
            return this;
        }
        return super.getValue(valstring);
    }

    @Override
    public Image getIcon(int type) {
        return ImageUtilities.loadImage("org/netbeans/modules/cnd/makeproject/ui/resources/importantFolder.gif"); // NOI18N
    }

    @Override
    public Image getOpenedIcon(int type) {
        return ImageUtilities.loadImage("org/netbeans/modules/cnd/makeproject/ui/resources/importantFolderOpened.gif"); // NOI18N
    }

    @Override
    public Action[] getActions(boolean context) {
        Action[] result = new Action[]{
            new AddExternalItemAction(provider.getProject()),
            null,
            SystemAction.get(org.openide.actions.FindAction.class),};
        // makeproject sensitive actions
        final MakeProjectType projectKind = provider.getProject().getLookup().lookup(MakeProjectType.class);
        final List<? extends Action> actionsForMakeProject = Utilities.actionsForPath(projectKind.extFolderActionsPath());
        result = NodeActionFactory.insertAfter(result, actionsForMakeProject.toArray(new Action[actionsForMakeProject.size()]), AddExternalItemAction.class);
        result = NodeActionFactory.insertSyncActions(result, AddExternalItemAction.class);
        return result;
    }

    @Override
    public boolean canRename() {
        return false;
    }
}
