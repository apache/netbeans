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

package org.netbeans.modules.ide.ergonomics.fod;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.support.CommonProjectActions;
import org.openide.filesystems.FileObject;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.nodes.Node;
import org.openide.util.ContextAwareAction;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;
import org.openide.util.lookup.Lookups;

/**
 *
 * @author Jaroslav Tulach <jtulach@netbeans.org>
 */
final class BrokenProject implements Project, ProjectInformation, LogicalViewProvider {
    private final FileObject pd;
    final String msg;

    public BrokenProject(FileObject projectDirectory, String error) {
        this.pd = projectDirectory;
        this.msg = error;
    }

    public FileObject getProjectDirectory() {
        return pd;
    }

    public Lookup getLookup() {
        return Lookups.singleton(this);
    }

    @Override
    public int hashCode() {
        return pd.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Project) {
            return pd.equals(((Project)obj).getProjectDirectory());
        }
        return false;
    }


    public String getName() {
        return pd.getName();
    }

    public String getDisplayName() {
        return NbBundle.getMessage(BrokenProject.class, "MSG_BrokenProject", pd.getName());
    }

    public Icon getIcon() {
        return ImageUtilities.loadImageIcon("org/netbeans/modules/ide/ergonomics/fod/BrokenProject.png", true); // NOI18N
    }

    public Project getProject() {
        return this;
    }

    public void addPropertyChangeListener(PropertyChangeListener listener) {
    }

    public void removePropertyChangeListener(PropertyChangeListener listener) {
    }

    public Node createLogicalView() {
        BrokenNode n = new BrokenNode(Children.LEAF, getLookup());
        n.setName(getName());
        n.setDisplayName(getDisplayName());
        n.setIconBaseWithExtension("org/netbeans/modules/ide/ergonomics/fod/BrokenProject.png"); // NOI18N
        return n;
    }

    public Node findPath(Node root, Object target) {
        return null;
    }

    private static final class BrokenNode extends AbstractNode {
        public BrokenNode(Children children, Lookup lookup) {
            super(children, lookup);
        }

        @Override
        public Action[] getActions(boolean context) {
            return new Action[] {
                CommonProjectActions.closeProjectAction(),
                null,
                BrokenActionInfo.ACTION
            };
        }
    }

    private static final class BrokenActionInfo extends AbstractAction
    implements ContextAwareAction {
        private final Lookup context;

        static final Action ACTION = new BrokenActionInfo(Utilities.actionsGlobalContext());

        private BrokenActionInfo(Lookup c) {
            context = c;
            putValue(NAME, NbBundle.getMessage(BrokenProject.class, "MSG_BrokenActionInfo"));
        }

        public void actionPerformed(ActionEvent e) {
            BrokenProject p = context.lookup(BrokenProject.class);
            if (p == null) {
                return;
            }
            BrokenProjectInfo.showInfo(p);
        }

        public Action createContextAwareInstance(Lookup actionContext) {
            return new BrokenActionInfo(actionContext);
        }

        @Override
        public int hashCode() {
            return getClass().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            return obj != null && getClass().equals(obj.getClass());
        }
    }
}
