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

package org.netbeans.modules.php.project;

import java.beans.PropertyChangeEvent;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.prefs.Preferences;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.phpmodule.PhpModuleProperties;
import org.netbeans.modules.php.project.ui.customizer.PhpProjectProperties;
import org.netbeans.modules.php.spi.testing.PhpTestingProviders;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.ui.CustomizerProvider2;
import org.openide.filesystems.FileObject;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;

/**
 * @author Tomas Mysik
 */
public class PhpModuleImpl implements PhpModule {

    private final PhpProject phpProject;

    // @GuardedBy("this")
    private Lookup lookup;


    public PhpModuleImpl(PhpProject phpProject) {
        assert phpProject != null;
        this.phpProject = phpProject;
    }

    public PhpProject getPhpProject() {
        return phpProject;
    }

    @Override
    public String getName() {
        return ProjectUtils.getInformation(phpProject).getName();
    }

    @Override
    public String getDisplayName() {
        return ProjectUtils.getInformation(phpProject).getDisplayName();
    }

    @Override
    public FileObject getProjectDirectory() {
        return ProjectPropertiesSupport.getProjectDirectory(phpProject);
    }

    @Override
    public FileObject getSourceDirectory() {
        return ProjectPropertiesSupport.getSourcesDirectory(phpProject);
    }

    @Override
    public List<FileObject> getTestDirectories() {
        return ProjectPropertiesSupport.getTestDirectories(phpProject, false);
    }

    @Override
    public FileObject getTestDirectory(FileObject file) {
        return ProjectPropertiesSupport.getTestDirectory(phpProject, file, false);
    }

    @Override
    public boolean isBroken() {
        return PhpProjectValidator.isFatallyBroken(phpProject);
    }

    @Override
    public synchronized Lookup getLookup() {
        if (lookup == null) {
            Lookup projectLookup = phpProject.getLookup();
            lookup = Lookups.fixed(
                    (Project) phpProject,
                    projectLookup.lookup(CustomizerProvider2.class),
                    projectLookup.lookup(org.netbeans.modules.php.api.queries.PhpVisibilityQuery.class),
                    projectLookup.lookup(PhpTestingProviders.class),
                    new PhpModulePropertiesFactory(phpProject)
            );
        }
        return lookup;
    }

    @Override
    public String toString() {
        return "PhpModuleImpl{" + "directory=" + phpProject.getProjectDirectory() + '}'; // NOI18N
    }

    @Override
    public Preferences getPreferences(Class<?> clazz, boolean shared) {
        return ProjectUtils.getPreferences(phpProject, clazz, shared);
    }

    @Override
    public void notifyPropertyChanged(PropertyChangeEvent propertyChangeEvent) {
        if (PROPERTY_FRAMEWORKS.equals(propertyChangeEvent.getPropertyName())) {
            phpProject.resetFrameworks();
        }
    }

    //~ Inner classes

    private static final class PhpModulePropertiesFactory implements PhpModuleProperties.Factory {

        private final PhpProject phpProject;


        PhpModulePropertiesFactory(PhpProject phpProject) {
            assert phpProject != null;
            this.phpProject = phpProject;
        }

        @Override
        public PhpModuleProperties getProperties() {
            PhpModuleProperties properties = new PhpModuleProperties();
            properties = setEncoding(properties);
            properties = setWebRoot(properties);
            properties = setTests(properties);
            properties = setUrl(properties);
            properties = setIndexFile(properties);
            properties = setIncludePath(properties);
            return properties;
        }

        private PhpModuleProperties setEncoding(PhpModuleProperties properties) {
            return properties.setEncoding(ProjectPropertiesSupport.getEncoding(phpProject));
        }

        private PhpModuleProperties setWebRoot(PhpModuleProperties properties) {
            return properties.setWebRoot(ProjectPropertiesSupport.getWebRootDirectory(phpProject));
        }

        private PhpModuleProperties setTests(PhpModuleProperties properties) {
            // XXX
            FileObject tests = ProjectPropertiesSupport.getTestDirectory(phpProject, null, false);
            if (tests != null) {
                properties = properties.setTests(tests);
            }
            return properties;
        }

        private PhpModuleProperties setUrl(PhpModuleProperties properties) {
            String url = ProjectPropertiesSupport.getUrl(phpProject);
            if (url != null) {
                properties = properties.setUrl(url);
            }
            return properties;
        }

        private PhpModuleProperties setIndexFile(PhpModuleProperties properties) {
            String indexFile = ProjectPropertiesSupport.getIndexFile(phpProject);
            FileObject sourceDirectory = phpProject.getSourcesDirectory();
            if (indexFile != null && sourceDirectory != null) {
                FileObject index = sourceDirectory.getFileObject(indexFile);
                if (index != null
                        && index.isData()
                        && index.isValid()) {
                    properties = properties.setIndexFile(index);
                }
            }
            return properties;
        }

        private PhpModuleProperties setIncludePath(PhpModuleProperties properties) {
            String includePath = ProjectPropertiesSupport.getPropertyEvaluator(phpProject).getProperty(PhpProjectProperties.INCLUDE_PATH);
            List<String> paths;
            if (includePath == null) {
                paths = Collections.emptyList();
            } else {
                paths = Arrays.asList(PropertyUtils.tokenizePath(includePath));
            }
            properties = properties.setIncludePath(paths);
            return properties;
        }

    }

}
