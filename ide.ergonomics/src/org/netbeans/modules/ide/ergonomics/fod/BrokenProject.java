/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2010 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2009 Sun Microsystems, Inc.
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
