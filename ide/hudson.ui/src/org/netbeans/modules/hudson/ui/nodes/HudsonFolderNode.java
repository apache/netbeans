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

package org.netbeans.modules.hudson.ui.nodes;

import java.awt.Image;
import java.util.ArrayList;
import java.util.List;
import javax.swing.Action;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.netbeans.modules.hudson.api.HudsonFolder;
import org.netbeans.modules.hudson.api.HudsonJob;
import org.netbeans.modules.hudson.ui.actions.OpenUrlAction;
import org.netbeans.modules.hudson.api.ui.OpenableInBrowser;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.ChildFactory;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.Union2;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;

class HudsonFolderNode extends AbstractNode {

    private static final Node iconDelegate = DataFolder.findFolder(FileUtil.getConfigRoot()).getNodeDelegate();

    private final HudsonFolder folder;

    HudsonFolderNode(HudsonFolder folder) {
        super(Children.create(new HudsonFolderChildren(folder), true), Lookups.singleton(folder));
        this.folder = folder;
        String name = folder.getName();
        setName(name);
        setDisplayName(!name.contains("/") ? name //NOI18N
                : name.substring(name.lastIndexOf("/") + 1, //NOI18N
                name.length()));
    }

    public @Override Image getIcon(int type) {
        return iconDelegate.getIcon(type);
    }

    public @Override Image getOpenedIcon(int type) {
        return iconDelegate.getOpenedIcon(type);
    }

    @Override public Action[] getActions(boolean context) {
        List<Action> actions = new ArrayList<Action>();
        if (folder instanceof OpenableInBrowser) {
            actions.add(OpenUrlAction.forOpenable((OpenableInBrowser) folder));
        }
        return actions.toArray(new Action[0]);
    }

    private static final class HudsonFolderChildren extends ChildFactory.Detachable<Union2<HudsonJob,HudsonFolder>> implements ChangeListener {

        private final HudsonFolder folder;

        HudsonFolderChildren(HudsonFolder folder) {
            this.folder = folder;
        }

        @Override protected boolean createKeys(List<Union2<HudsonJob,HudsonFolder>> toPopulate) {
            for (HudsonFolder subfolder : folder.getFolders()) {
                toPopulate.add(Union2.<HudsonJob,HudsonFolder>createSecond(subfolder));
            }
            for (HudsonJob job : folder.getJobs()) {
                toPopulate.add(Union2.<HudsonJob,HudsonFolder>createFirst(job));
            }
            return true;
        }

        @Override protected Node createNodeForKey(Union2<HudsonJob,HudsonFolder> key) {
            return key.hasFirst() ? new HudsonJobNode(key.first()) : new HudsonFolderNode(key.second());
        }

        @Override protected void addNotify() {
            folder.addChangeListener(WeakListeners.change(this, folder));
        }

        @Override public void stateChanged(ChangeEvent e) {
            refresh(false);
        }

    }

}
