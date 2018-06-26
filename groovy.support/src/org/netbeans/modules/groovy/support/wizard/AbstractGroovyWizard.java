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

package org.netbeans.modules.groovy.support.wizard;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import org.netbeans.api.progress.ProgressHandle;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.modules.groovy.support.api.GroovyExtender;
import org.netbeans.modules.groovy.support.api.GroovySources;
import org.netbeans.modules.groovy.support.wizard.impl.AntProjectTypeStrategy;
import org.netbeans.modules.groovy.support.wizard.impl.MavenProjectTypeStrategy;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/**
 * Abstract wizard iterator for groovy files.
 *
 * @author Martin Janicek
 */
public abstract class AbstractGroovyWizard extends AbstractFileWizard {

    protected ProjectTypeStrategy strategy;


    protected AbstractGroovyWizard() {
    }


    @Override
    public void initialize(WizardDescriptor wiz) {
        project = Templates.getProject(wiz);
        strategy = findCorrectStrategy();

        super.initialize(wiz);
    }

    private ProjectTypeStrategy findCorrectStrategy() {
        FileObject pom = project.getProjectDirectory().getFileObject("pom.xml"); // NOI18N

        if (pom != null && pom.isValid()) {
            // Looks like Maven based project
            return new MavenProjectTypeStrategy(project);
        } else {
            return new AntProjectTypeStrategy(project);
        }
    }

    @Override
    public Set instantiate(ProgressHandle handle) throws IOException {
        handle.start();
        handle.progress(NbBundle.getMessage(AbstractGroovyWizard.class, "LBL_NewGroovyFileWizardIterator_WizardProgress_CreatingFile"));

        FileObject template = findCorrectTemplate();
        FileObject targetFolder = Templates.getTargetFolder(wiz);
        String targetName = Templates.getTargetName(wiz);

        DataFolder dFolder = DataFolder.findFolder(targetFolder);
        DataObject dTemplate = DataObject.find(template);

        String pkgName = getPackageName(targetFolder);
        DataObject dobj;
        if (pkgName == null) {
            dobj = dTemplate.createFromTemplate(dFolder, targetName);
        } else {
            dobj = dTemplate.createFromTemplate(dFolder, targetName, Collections.singletonMap("package", pkgName)); // NOI18N
        }

        FileObject createdFile = dobj.getPrimaryFile();
        
        Project proj = Templates.getProject(wiz);
        if (!GroovyExtender.isActive(proj)) {
            GroovyExtender.activate(proj);
        }

        handle.finish();
        return Collections.singleton(createdFile);
    }

    /**
     * Return template that should be used for file creation. By default it uses
     * {@link Templates#getTemplate(org.openide.WizardDescriptor)} method, but it
     * is possible to override this behavior in subclasses.
     *
     * @return file template
     */
    protected FileObject findCorrectTemplate() {
        return Templates.getTemplate(wiz);
    }

    private String getPackageName(FileObject targetFolder) {
        List<SourceGroup> groups = retrieveGroups();
        String packageName = null;
        for (int i = 0; i < groups.size() && packageName == null; i++) {
            packageName = FileUtil.getRelativePath(groups.get(i).getRootFolder(), targetFolder);
        }
        if (packageName != null) {
            packageName = packageName.replaceAll("/", "."); // NOI18N
        }
        return packageName;
    }

    protected List<SourceGroup> retrieveGroups() {
        return GroovySources.getGroovySourceGroups(ProjectUtils.getSources(project));
    }
}
