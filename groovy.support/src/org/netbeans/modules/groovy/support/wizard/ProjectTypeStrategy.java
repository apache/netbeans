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
