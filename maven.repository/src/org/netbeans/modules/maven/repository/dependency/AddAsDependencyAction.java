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
 *
 * Portions Copyrighted 2008 Sun Microsystems, Inc.
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
