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
package org.netbeans.modules.rust.cargo.impl.nodes;

import java.awt.Image;
import java.awt.event.ActionEvent;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.SwingUtilities;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.rust.cargo.api.CargoTOML;
import org.netbeans.modules.rust.project.api.RustIconFactory;
import org.openide.nodes.AbstractNode;
import org.openide.nodes.Children;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.lookup.Lookups;

/**
 * A member of a Rust workspace. This represents a Rust project, that can be
 * opened.
 *
 * @author antonio
 */
public class RustWorkspaceMemberNode extends AbstractNode {

    @NbBundle.Messages({
        "ACTION_OPEN_PROJECT=Open workspace member",
    })
    private static final class OpenRustProjectAction extends AbstractAction {

        private final CargoTOML cargotoml;

        private OpenRustProjectAction(CargoTOML cargotoml) {
            super(NbBundle.getMessage(RustWorkspaceMemberNode.class, "ACTION_OPEN_PROJECT"));
            this.cargotoml = cargotoml;
        }

        @Override
        public void actionPerformed(ActionEvent e) {
            SwingUtilities.invokeLater(this::openProject);
        }

        private void openProject() {
            // TODO: Is this ok in the EDT?
            try {
                Project prj = ProjectManager.getDefault().findProject(cargotoml.getFileObject().getParent());
                if (prj != null) {
                    OpenProjects.getDefault().open(new Project[]{prj}, false);
                }
            } catch (Exception ex) {
                Exceptions.printStackTrace(ex);
            }
        }

    }

    private final CargoTOML cargotoml;

    public RustWorkspaceMemberNode(CargoTOML cargotoml) {
        super(Children.LEAF, Lookups.fixed(cargotoml));
        this.cargotoml = cargotoml;
    }

    @Override
    public Image getOpenedIcon(int type) {
        return RustIconFactory.getRustIcon();
    }

    @Override
    public Image getIcon(int type) {
        return RustIconFactory.getRustIcon();
    }

    @Override
    public String getDisplayName() {
        return cargotoml.getPackageName();
    }

    @Override
    public String getName() {
        return cargotoml.getPackageName();
    }

    @Override
    public Action[] getActions(boolean context) {
        return new Action[]{new OpenRustProjectAction(cargotoml)};
    }

}
