/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2012 Oracle and/or its affiliates. All rights reserved.
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
 * Portions Copyrighted 2012 Sun Microsystems, Inc.
 */
package org.netbeans.modules.php.project.problems;

import java.io.File;
import java.util.Collections;
import java.util.concurrent.Future;
import org.netbeans.modules.php.project.PhpProject;
import org.netbeans.modules.php.project.ProjectPropertiesSupport;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.spi.project.ui.ProjectProblemResolver;
import org.netbeans.spi.project.ui.ProjectProblemsProvider;
import org.openide.filesystems.FileChooserBuilder;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

/**
 * Problem resolver using directory chooser. The selected directory is saved
 * in project properties.
 */
public class DirectoryProblemResolver implements ProjectProblemResolver {

    private final PhpProject project;
    private final String propertyName;
    private final String dialogTitle;


    public DirectoryProblemResolver(PhpProject project, String propertyName, String dialogTitle) {
        this.project = project;
        this.propertyName = propertyName;
        this.dialogTitle = dialogTitle;
    }

    @NbBundle.Messages("DirectoryProblemResolver.dialog.choose=Choose")
    @Override
    public Future<ProjectProblemsProvider.Result> resolve() {
        File projectDir = FileUtil.toFile(project.getProjectDirectory());
        File selectedDir = new FileChooserBuilder(ProjectPropertiesProblemProvider.class)
                .setTitle(dialogTitle)
                .setDefaultWorkingDirectory(projectDir)
                .forceUseOfDefaultWorkingDirectory(true)
                .setDirectoriesOnly(true)
                .setFileHiding(true)
                .setApproveText(Bundle.DirectoryProblemResolver_dialog_choose())
                .showOpenDialog();
        if (selectedDir == null) {
            // no file selected
            return new Done(ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.UNRESOLVED));
        }
        // save metadata
        String relPath = ProjectPropertiesSupport.relativizeFile(projectDir, selectedDir);
        PhpProjectProperties.save(project, Collections.singletonMap(propertyName, relPath), Collections.<String, String>emptyMap());
        // return unresolved state; it will change automatically once the metadata are really saved (property change will be fired)
        return new Done(ProjectProblemsProvider.Result.create(ProjectProblemsProvider.Status.UNRESOLVED));
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 19 * hash + (this.project != null ? this.project.hashCode() : 0);
        hash = 19 * hash + (this.propertyName != null ? this.propertyName.hashCode() : 0);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final DirectoryProblemResolver other = (DirectoryProblemResolver) obj;
        if (this.project != other.project && (this.project == null || !this.project.equals(other.project))) {
            return false;
        }
        if ((this.propertyName == null) ? (other.propertyName != null) : !this.propertyName.equals(other.propertyName)) {
            return false;
        }
        return true;
    }

}
