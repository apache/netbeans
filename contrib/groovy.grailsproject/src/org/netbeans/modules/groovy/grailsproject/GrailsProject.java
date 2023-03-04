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

package org.netbeans.modules.groovy.grailsproject;

import java.awt.Image;
import java.beans.PropertyChangeListener;
import java.util.logging.Logger;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.GlobalPathRegistry;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.modules.groovy.grails.api.GrailsConstants;
import org.netbeans.modules.groovy.grails.api.GrailsProjectConfig;
import org.netbeans.modules.groovy.grailsproject.classpath.ClassPathProviderImpl;
import org.netbeans.modules.groovy.grailsproject.classpath.SourceRoots;
import org.netbeans.modules.groovy.grailsproject.commands.GrailsCommandSupport;
import org.netbeans.modules.groovy.grailsproject.completion.ControllerCompletionProvider;
import org.netbeans.modules.groovy.grailsproject.completion.DomainCompletionProvider;
import org.netbeans.modules.groovy.grailsproject.config.BuildConfig;
import org.netbeans.modules.groovy.grailsproject.debug.GrailsDebugger;
import org.netbeans.modules.groovy.grailsproject.queries.GrailsProjectEncodingQueryImpl;
import org.netbeans.modules.groovy.grailsproject.ui.GrailsLogicalViewProvider;
import org.netbeans.modules.groovy.grailsproject.ui.TemplatesImpl;
import org.netbeans.modules.groovy.grailsproject.ui.customizer.GrailsProjectCustomizerProvider;
import org.netbeans.modules.groovy.support.spi.GroovyExtenderImplementation;
import org.netbeans.spi.project.AuxiliaryConfiguration;
import org.netbeans.spi.project.ProjectState;
import org.netbeans.spi.project.ui.LogicalViewProvider;
import org.netbeans.spi.project.ui.PrivilegedTemplates;
import org.netbeans.spi.project.ui.ProjectOpenedHook;
import org.netbeans.spi.project.ui.RecommendedTemplates;
import org.openide.filesystems.FileObject;
import org.openide.util.ImageUtilities;
import org.openide.util.Lookup;
import org.openide.util.lookup.Lookups;
import org.w3c.dom.Element;


/**
 *
 * @author Martin Adamek
 */
public final class GrailsProject implements Project {

    private static final Logger LOG = Logger.getLogger(GrailsProject.class.getName());
    private final FileObject projectDir;
    private final ProjectState projectState;
    private final LogicalViewProvider logicalView;
    private final ClassPathProviderImpl cpProvider;
    private final GrailsCommandSupport commandSupport;
    private final BuildConfig buildConfig;
    private final SourceCategoriesFactory sourceCategoriesFactory;
    private SourceRoots sourceRoots;
    private SourceRoots testRoots;
    private Lookup lookup;
    

    public GrailsProject(FileObject projectDir, ProjectState projectState) {
        this.projectDir = projectDir;
        this.projectState = projectState;
        this.logicalView = new GrailsLogicalViewProvider(this);
        this.cpProvider = new ClassPathProviderImpl(getSourceRoots(), getTestSourceRoots(), this);
        this.commandSupport = new GrailsCommandSupport(this);
        this.buildConfig = new BuildConfig(this);

        /* TODO: when projects have their own grails version we need to pass it
                 to the source categories factory.
         */
        this.sourceCategoriesFactory = new SourceCategoriesFactory();
    }

    @Override
    public FileObject getProjectDirectory() {
        return projectDir;
    }

    public ProjectState getProjectState() {
        return projectState;
    }

    public GrailsCommandSupport getCommandSupport() {
        return commandSupport;
    }

    public BuildConfig getBuildConfig() {
        return buildConfig;
    }
    
    public SourceCategoriesFactory getSourceCategoriesFactory() {
        return sourceCategoriesFactory;
    }

    @Override
    public Lookup getLookup() {
        if (lookup == null) {
            GrailsProjectConfig config = new GrailsProjectConfig(this);
            config.initListeners();

            lookup = Lookups.fixed(
                this,  //project spec requires a project be in its own lookup
                projectState, //allow outside code to mark the project as needing saving
                new Info(), //Project information implementation
                new GrailsActionProvider(this),
                GrailsSources.create(this),
                new GrailsServerState(this),
                new GrailsProjectCustomizerProvider(this),
                new GrailsProjectOperations(this),
                new GrailsProjectEncodingQueryImpl(),
                new OpenHook(),
                new AuxiliaryConfigurationImpl(),
                new RecommendedTemplatesImpl(),
                new GroovyExtenderImpl(),
                new ControllerCompletionProvider(),
                new DomainCompletionProvider(),
                logicalView, //Logical view of project implementation
                cpProvider,
                config,
                new GrailsDebugger(this)
            );
        }
        return lookup;
    }

    public synchronized SourceRoots getSourceRoots() {
        if (this.sourceRoots == null) { //Local caching, no project metadata access
            this.sourceRoots = new SourceRoots(this, projectDir); //NOI18N
        }
        return this.sourceRoots;
    }

    public synchronized SourceRoots getTestSourceRoots() {
        if (this.testRoots == null) { //Local caching, no project metadata access
            this.testRoots = new SourceRoots(this, projectDir); //NOI18N
        }
        return this.testRoots;
    }

    private final class Info implements ProjectInformation {

        @Override
        public Icon getIcon() {
            Image image = ImageUtilities.loadImage(GrailsConstants.GRAILS_ICON_16x16);
            return image == null ? null : new ImageIcon(image);
        }

        @Override
        public String getName() {
            return getProjectDirectory().getName();
        }

        @Override
        public String getDisplayName() {
            return getName();
        }

        @Override
        public void addPropertyChangeListener(PropertyChangeListener pcl) {
            //do nothing, won't change
        }

        @Override
        public void removePropertyChangeListener(PropertyChangeListener pcl) {
            //do nothing, won't change
        }

        @Override
        public Project getProject() {
            return GrailsProject.this;
        }
    }

    private class OpenHook extends ProjectOpenedHook {

        @Override
        protected void projectOpened() {
            ClassPath[] sourceClasspaths = cpProvider.getProjectClassPaths(ClassPath.SOURCE);

            GlobalPathRegistry.getDefault().register(ClassPath.BOOT, cpProvider.getProjectClassPaths(ClassPath.BOOT));
            GlobalPathRegistry.getDefault().register(ClassPath.COMPILE, cpProvider.getProjectClassPaths(ClassPath.COMPILE));
            GlobalPathRegistry.getDefault().register(ClassPath.SOURCE, sourceClasspaths);
        }

        @Override
        protected void projectClosed() {
            GlobalPathRegistry.getDefault().unregister(ClassPath.BOOT, cpProvider.getProjectClassPaths(ClassPath.BOOT));
            GlobalPathRegistry.getDefault().unregister(ClassPath.COMPILE, cpProvider.getProjectClassPaths(ClassPath.COMPILE));
            GlobalPathRegistry.getDefault().unregister(ClassPath.SOURCE, cpProvider.getProjectClassPaths(ClassPath.SOURCE));
        }
    }

    private static class AuxiliaryConfigurationImpl implements AuxiliaryConfiguration {

        @Override
        public Element getConfigurationFragment(String elementName, String namespace, boolean shared) {
            return null;
        }

        @Override
        public void putConfigurationFragment(Element fragment, boolean shared) throws IllegalArgumentException {
        }

        @Override
        public boolean removeConfigurationFragment(String elementName, String namespace, boolean shared) throws IllegalArgumentException {
            return false;
        }
    }

    private static final class RecommendedTemplatesImpl implements RecommendedTemplates, PrivilegedTemplates {

        // List of primarily supported templates

        private static final String[] RECOMMENDED_TYPES = new String[] {
            "groovy",               // NOI18N
            "java-classes",         // NOI18N
            "XML",                  // NOI18N
            "simple-files"          // NOI18N
        };

        private static final String[] PRIVILEGED_NAMES = new String[] {
            TemplatesImpl.DOMAIN_CLASS,
            TemplatesImpl.CONTROLLER,
            TemplatesImpl.INTEGRATION_TEST,
            TemplatesImpl.GANT_SCRIPT,
            TemplatesImpl.SERVICE,
            TemplatesImpl.TAG_LIB,
            TemplatesImpl.UNIT_TEST,
            "Templates/Other/Folder",
            "Templates/Other/properties.properties",
            "simple-files"
        };

        @Override
        public String[] getRecommendedTypes() {
            return RECOMMENDED_TYPES;
        }

        @Override
        public String[] getPrivilegedTemplates() {
            return PRIVILEGED_NAMES;
        }
    }

    private static final class GroovyExtenderImpl implements GroovyExtenderImplementation {

        @Override
        public boolean isActive() {
            return true;
        }

        @Override
        public boolean activate() {
            return true;
        }

        @Override
        public boolean deactivate() {
            return true;
        }
    }
}
