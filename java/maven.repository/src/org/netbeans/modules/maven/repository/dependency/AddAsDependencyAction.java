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
/*
 * Contributor(s): theanuradha@netbeans.org
 */
package org.netbeans.modules.maven.repository.dependency;

import java.awt.event.ActionEvent;
import java.util.List;
import javax.swing.AbstractAction;
import org.apache.maven.artifact.Artifact;
import org.netbeans.modules.maven.api.ModelUtils;
import org.netbeans.modules.maven.repository.dependency.ui.AddDependencyUI;
import org.netbeans.api.project.Project;
import org.netbeans.modules.maven.spi.IconResources;
import org.netbeans.modules.maven.spi.nodes.NodeUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.util.ImageUtilities;
import org.openide.util.NbBundle;

/**
 *
 * @author Anuradha G (theanuradha-at-netbeans.org)
 */
public class AddAsDependencyAction extends AbstractAction {

    private Artifact record;

    public AddAsDependencyAction(Artifact record) {
        putValue(NAME, NbBundle.getMessage(AddAsDependencyAction.class, "LBL_Add_As_Dependency"));
        putValue(SMALL_ICON, ImageUtilities.image2Icon(ImageUtilities.loadImage(IconResources.ICON_DEPENDENCY_JAR, true)));
        this.record = record;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
         StringBuffer buffer = new StringBuffer();
         buffer.append("<b>"); //NOI18N
         buffer.append(record.getArtifactId());
         buffer.append("</b>");//NOI18N
         buffer.append(":");//NOI18N
         buffer.append("<b>");//NOI18N
         buffer.append(record.getVersion().toString());
         buffer.append("</b>");//NOI18N

        AddDependencyUI adui = new AddDependencyUI(buffer.toString());
        DialogDescriptor dd = new DialogDescriptor(adui, NbBundle.getMessage(AddAsDependencyAction.class, "TIT_Add_Dependency"));
        dd.setClosingOptions(new Object[]{
            adui.getAddButton(),
            DialogDescriptor.CANCEL_OPTION
        });
        dd.setOptions(new Object[]{
            adui.getAddButton(),
            DialogDescriptor.CANCEL_OPTION
        });
        Object ret = DialogDisplayer.getDefault().notify(dd);
        if (adui.getAddButton() == ret) {
            List<Project> nmps = adui.getSelectedMavenProjects();
            for (Project project : nmps) {
                ModelUtils.addDependency(project.getProjectDirectory().getFileObject("pom.xml") /*NOI18N*/,
                        record.getGroupId(), record.getArtifactId(),
                        record.getVersion(), record.getType(), null, null,false);
            }

        }

    }


}
