/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2013 Oracle and/or its affiliates. All rights reserved.
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
 *
 * Contributor(s):
 *
 * Portions Copyrighted 2013 Sun Microsystems, Inc.
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
        return actions.toArray(new Action[actions.size()]);
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
