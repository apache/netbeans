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
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import org.netbeans.api.project.Project;
import org.netbeans.modules.cnd.makeproject.api.configurations.Folder;
import org.netbeans.modules.cnd.makeproject.api.configurations.Item;
import org.netbeans.modules.cnd.makeproject.ui.BrokenViewItemRefreshSupport.BrokenViewItemListener;
import org.netbeans.modules.cnd.utils.MIMENames;
import org.netbeans.modules.cnd.utils.MIMESupport;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;
import org.openide.util.WeakListeners;
import org.openide.util.actions.SystemAction;

/**
 *
 */
final class BrokenViewItemNode extends AbstractNode {

    private final RefreshableItemsContainer childrenKeys;
    private final Folder folder;
    private final Item item;
    private final Project project;
    private final BrokenViewItemListener brokenViewItemListener;

    public BrokenViewItemNode(RefreshableItemsContainer childrenKeys, Folder folder, Item item, Project project) {
        super(Children.LEAF);
        this.childrenKeys = childrenKeys;
        this.folder = folder;
        this.item = item;
        setName(item.getNormalizedPath());
        setDisplayName(item.getName());
        setShortDescription(NbBundle.getMessage(getClass(), "BrokenTxt", item.getPath())); // NOI18N
        this.project = project;
        brokenViewItemListener = (Project project1) -> {
            if (getParentNode() == null) {
                return;
            }
            if (project1 == BrokenViewItemNode.this.project) {
                refresh();
            }
        };
        BrokenViewItemRefreshSupport.addBrokenViewItemListener(
                WeakListeners.create(
                BrokenViewItemListener.class, brokenViewItemListener, BrokenViewItemRefreshSupport.class));
    }

    @Override
    public Image getIcon(int type) {
        // fileobject is invalid, so no need to go the long way
        // PredefinedToolKind tool = item.getDefaultTool();
        Image original;
        final String mimeType = MIMESupport.getKnownSourceFileMIMETypeByExtension(item.getName());
        if (MIMENames.CPLUSPLUS_MIME_TYPE.equals(mimeType)) {
            original = ImageUtilities.loadImage("org/netbeans/modules/cnd/source/resources/CCSrcIcon.gif"); // NOI18N;
        } else if (MIMENames.C_MIME_TYPE.equals(mimeType)) {
            original = ImageUtilities.loadImage("org/netbeans/modules/cnd/source/resources/CSrcIcon.gif"); // NOI18N
        } else if (MIMENames.HEADER_MIME_TYPE.equals(mimeType)) {
            original = ImageUtilities.loadImage("org/netbeans/modules/cnd/source/resources/HDataIcon.gif"); // NOI18N;
        } else if (MIMENames.FORTRAN_MIME_TYPE.equals(mimeType)) {
            original = ImageUtilities.loadImage("org/netbeans/modules/cnd/source/resources/FortranSrcIcon.gif"); // NOI18N
        } else {
            original = ImageUtilities.loadImage("org/netbeans/modules/cnd/loaders/unknown.gif"); // NOI18N
        }
        return ImageUtilities.mergeImages(original, MakeLogicalViewProvider.brokenProjectBadge, 11, 0);
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[]{
                    SystemAction.get(RemoveItemAction.class),
                    new RefreshItemAction(childrenKeys, null, item),
                    null,
                    SystemAction.get(PropertiesItemAction.class),};
    }

    public void refresh() {
        childrenKeys.refreshItem(item);
    }

    @Override
    public boolean canRename() {
        return false;
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
            return project;
        } else if (valstring.equals("Item")) // NOI18N
        {
            return item;
        } else if (valstring.equals("This")) // NOI18N
        {
            return this;
        }
        return super.getValue(valstring);
    }

    static final class RefreshItemAction extends AbstractAction {

        private final RefreshableItemsContainer childrenKeys;
        private final Folder folder;
        private final Item item;

        public RefreshItemAction(RefreshableItemsContainer childrenKeys, Folder folder, Item item) {
            this.childrenKeys = childrenKeys;
            this.folder = folder;
            this.item = item;
            putValue(NAME, NbBundle.getBundle(getClass()).getString("CTL_Refresh")); //NOI18N
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            if (item != null) {
                childrenKeys.refreshItem(item);
            } else {
                Item[] items = folder.getItemsAsArray();
                for (int i = 0; i < items.length; i++) {
                    childrenKeys.refreshItem(items[i]);
                }
            }
        }
    }
}
