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
import java.util.ArrayList;
import java.util.List;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.spi.project.ui.templates.support.Templates;
import org.openide.WizardDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.util.Exceptions;

/**
 * Base project type strategy implementation used by GroovyJUnitTestWizardIterator.
 * This abstract class encapsulate some general method independent on project type
 * and provide several template methods to be implemented by concrete strategy
 * (there are only two project types at the moment: Ant based and Maven based)
 *
 * @see AntProjectTypeStrategy
 * @see MavenProjectTypeStrategy
 *
 * @author Martin Janicek
 */
public abstract class ProjectTypeStrategy {

    protected final Project project;
    protected JUnit jUnitVersion;


    protected ProjectTypeStrategy(Project project) {
        this.project = project;
    }


    public abstract boolean existsGroovyTestFolder(List<SourceGroup> groups);

    public abstract boolean existsGroovySourceFolder(List<SourceGroup> groups);

    public abstract void createGroovyTestFolder();

    public abstract void createGroovySourceFolder();

    /**
     * Enables to change the order of the given source groups.
     *
     * @param groups source groups
     * @return regrouped source groups
     */
    public abstract List<SourceGroup> moveTestFolderAsFirst(List<SourceGroup> groups);

    public abstract List<SourceGroup> moveSourceFolderAsFirst(List<SourceGroup> groups);

    public final List<SourceGroup> getOnlyTestSourceGroups(List<SourceGroup> groups) {
        List<SourceGroup> reorderedGroup = new ArrayList<SourceGroup>();
        for (SourceGroup group : groups) {
            final String groupPath = group.getRootFolder().getPath();

            // Two check because of issue #221727
            if (groupPath.endsWith("/test") || groupPath.contains("/test/")) { // NOI18N
                reorderedGroup.add(group);
            }
        }
        return reorderedGroup;
    }

    public final List<SourceGroup> moveAsFirst(List<SourceGroup> groups, String folderName) {
        List<SourceGroup> reorderedGroup = new ArrayList<SourceGroup>();
        for (SourceGroup group : groups) {
            if (group.getRootFolder().getPath().contains(folderName)) { // NOI18N
                reorderedGroup.add(0, group);
            } else {
                reorderedGroup.add(group);
            }
        }
        return reorderedGroup;
    }

    public abstract JUnit findJUnitVersion();

    public abstract void addJUnitLibrary(JUnit jUnit);


    public final void setjUnitVersion(JUnit jUnitVersion) {
        this.jUnitVersion = jUnitVersion;
    }


    /**
     * Finds and return template based on the current set JUnit version. If the
     * version of the JUnit is lower than 4, this method returns template based
     * on JUnit 3 (<code>ClassName extends TestCase</code>, without JUnit
     * annotations, etc.). Otherwise it returns JUnit 4 template.
     *
     * @param wizardDescriptor current wizard descriptor
     * @return Groovy JUnit Test template based on JUnit version declared in pom.xml
     */
    public FileObject findTemplate(WizardDescriptor wizardDescriptor) {
        // Let's find out parent for declared templates --> This allows
        // to find templates marked with category = "invisible"
        final FileObject templatesParent = Templates.getTemplate(wizardDescriptor).getParent();
        if (jUnitVersion == JUnit.JUNIT4) {
            return lookForJUnitTemplate(templatesParent, 4);
        } else if (jUnitVersion == JUnit.JUNIT3) {
            return lookForJUnitTemplate(templatesParent, 3);
        } else {
            return null;
        }
    }

    private FileObject lookForJUnitTemplate(FileObject templatesParent, int majorVersion) {
        final String templateName = "GroovyJUnit" + majorVersion + "Test.groovy"; //NOI18N
        for (FileObject child : templatesParent.getChildren()) {
            if (templateName.equals(child.getNameExt())) {
                return child;
            }
        }
        return null;
    }
    
    protected final FileObject createFolder(FileObject parentFolder, String folderName) {
        FileObject childFolder = parentFolder.getFileObject(folderName, null);
        if (childFolder == null || !childFolder.isValid()) {
            try {
                return parentFolder.createFolder(folderName);
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        }
        return childFolder;
    }
}
