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
            packageName = packageName.replace("/", "."); // NOI18N
        }
        return packageName;
    }

    protected List<SourceGroup> retrieveGroups() {
        return GroovySources.getGroovySourceGroups(ProjectUtils.getSources(project));
    }
}
