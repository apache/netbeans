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

package org.netbeans.modules.java.api.common.project;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectInformation;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.ant.AntArtifact;
import org.netbeans.api.project.ui.OpenProjects;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import org.netbeans.modules.java.api.common.classpath.ClassPathModifier;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.spi.project.CopyOperationImplementation;
import org.netbeans.spi.project.DataFilesProviderImplementation;
import org.netbeans.spi.project.DeleteOperationImplementation;
import org.netbeans.spi.project.MoveOrRenameOperationImplementation;
import org.netbeans.spi.project.SubprojectProvider;
import org.netbeans.spi.project.ant.AntArtifactProvider;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.netbeans.spi.project.support.ant.PropertyUtils;
import org.netbeans.spi.project.support.ant.ReferenceHelper;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.util.BaseUtilities;
import org.openide.util.Exceptions;
import org.openide.util.NbBundle;
import org.openide.util.Pair;
import org.openide.util.Parameters;
import org.openide.util.Utilities;

/**
 * Support for default project operations for Ant based project.
 * @author Tomas Zezula
 * @author Jan Lahoda
 * @since 1.65
 */
public final class ProjectOperations {

    private static final Logger LOG = Logger.getLogger(ProjectOperations.class.getName());
    private static final String TARGET_CLEAN = "clean";

    private ProjectOperations() {
        throw new IllegalStateException("No instance allowed.");    //NOI18N
    }

    /**
     * Creates a builder for creating {@link DeleteOperationImplementation},
     * {@link MoveOrRenameOperationImplementation}, {@link CopyOperationImplementation}
     * implementation.
     * @param project the project to create project operations for
     * @param eval  the project's {@link PropertyEvaluator}
     * @param helper the project's {@link UpdateHelper}
     * @param refHelper the project's {@link ReferenceHelper}
     * @param sources the project's source roots
     * @param tests the project's test roots
     * @return a builder for project operations
     */
    @NonNull
    public static ProjectOperationsBuilder createBuilder(
        @NonNull final Project project,
        @NonNull final PropertyEvaluator eval,
        @NonNull final UpdateHelper helper,
        @NonNull final ReferenceHelper refHelper,
        @NonNull final SourceRoots sources,
        @NonNull final SourceRoots tests) {
        return new ProjectOperationsBuilder(
            project,
            eval,
            helper,
            refHelper,
            sources,
            tests);
    }

    /**
     * Callback for project operations.
     * The callback is called before and after each project operation.
     * The callback is registered by {@link ProjectOperationsBuilder#setCallback} method.
     */
    public static interface Callback {
        /**
         * The type of project operation.
         */
        enum Operation {
            /**
             * Project delete.
             */
            DELETE,
            /**
             * Project copy.
             */
            COPY,
            /**
             * Project move.
             */
            MOVE,
            /**
             * Project rename.
             */
            RENAME;
        }

        /**
         * Called at the beginning of the project operation.
         * @param operation the operation being performed
         */
        void beforeOperation(@NonNull final Operation operation);

        /**
         * Called at the end of the project operation.
         * @param operation the operation being performed
         * @param newName the new project's name or null when not applicable, eg. delete
         * @param oldProject the old project or null when not applicable, eg. delete, move, rename
         */
        void afterOperation(
                @NonNull final Operation operation,
                @NullAllowed String newName,
                @NullAllowed Pair<File,Project> oldProject);
    }


    /**
     * The builder for projects operations.
     */
    public static final class ProjectOperationsBuilder {

        private final Project project;
        private final PropertyEvaluator eval;
        private final UpdateHelper helper;
        private final ReferenceHelper refHelper;
        private final SourceRoots sources;
        private final SourceRoots tests;
        private final List<String> additionalMetadataFiles;
        private final List<String> additionalDataFiles;
        private final List<String> cleanTargets;
        private final Set<String> privateProps;
        private final Map<String,Pair<String,Boolean>> updatedProps;
        private Callback callback;
        private String buildScriptProperty = ProjectProperties.BUILD_SCRIPT;

        private ProjectOperationsBuilder(
            @NonNull final Project project,
            @NonNull final PropertyEvaluator eval,
            @NonNull final UpdateHelper helper,
            @NonNull final ReferenceHelper refHelper,
            @NonNull final SourceRoots sources,
            @NonNull final SourceRoots tests) {
            Parameters.notNull("project", project); //NOI18N
            Parameters.notNull("eval", eval);   //NOI18N
            Parameters.notNull("helper", helper);   //NOI18N
            Parameters.notNull("refHelper", refHelper); //NOI18N
            Parameters.notNull("sources", sources); //NOI18N
            Parameters.notNull("tests", tests); //NOI18N
            this.project = project;
            this.eval = eval;
            this.helper = helper;
            this.refHelper = refHelper;
            this.sources = sources;
            this.tests = tests;
            this.additionalMetadataFiles = new ArrayList<>();
            this.additionalDataFiles = new ArrayList<>();
            this.cleanTargets = new ArrayList<>();            
            this.privateProps = new HashSet<>();
            this.updatedProps = new HashMap<>();
        }

        /**
         * Sets the name of property referencing the project build script.
         * If not set the {@link ProjectProperties#BUILD_SCRIPT} is used.
         * @param propertyName the name of property holding the name of project's build script.
         * @return the {@link ProjectOperationsBuilder}
         */
        @NonNull
        public ProjectOperationsBuilder setBuildScriptProperty(@NonNull final String propertyName) {
            Parameters.notNull("propertyName", propertyName);   //NOI18N
            this.buildScriptProperty = propertyName;
            return this;
        }

        /**
         * Adds build script targets to the list of clean targets.
         * All the added targets are executed before project operation, when no
         * clean target is set the default one "clean" is used.
         * @param cleanTargets the clean targets
         * @return the {@link ProjectOperationsBuilder}
         */
        @NonNull
        public ProjectOperationsBuilder addCleanTargets(@NonNull String... cleanTargets) {
            Parameters.notNull("cleanTargets", cleanTargets);   //NOI18N
            Collections.addAll(this.cleanTargets, cleanTargets);
            return this;
        }

        /**
         * Adds additional files to the list of project metadata.
         * The "nbproject" and project build script are already included.
         * @param paths the metadata file paths, relative to project directory
         * @return the {@link ProjectOperationsBuilder}
         */
        @NonNull
        public ProjectOperationsBuilder addMetadataFiles(@NonNull final String... paths) {
            Parameters.notNull("paths", paths);   //NOI18N
            Collections.addAll(additionalMetadataFiles, paths);
            return this;
        }

        /**
         * Adds additional files to the list of project data files.
         * The source roots, test roots and library definitions are already included.
         * @param paths the data file paths, relative to project directory
         * @return the {@link ProjectOperationsBuilder}
         */
        @NonNull
        public ProjectOperationsBuilder addDataFiles(@NonNull final String... paths) {
            Parameters.notNull("paths", paths);   //NOI18N
            Collections.addAll(additionalDataFiles, paths);
            return this;
        }

        /**
         * Adds private properties which should be retained during copy, move or rename of project.
         * @param properties the private properties to be retained
         * @return the {@link ProjectOperationsBuilder}
         */
        @NonNull
        public ProjectOperationsBuilder addPreservedPrivateProperties(@NonNull final String... properties) {
            Parameters.notNull("properties", properties);   //NOI18N
            Collections.addAll(privateProps, properties);
            return this;
        }

        /**
         * Adds a project property which should be updated by a new project name after rename or copy of project.
         * @param propertyName  the project property name
         * @param propertyPattern the {@link MessageFormat} pattern of the property value, the "{0}"
         * is replaced by the new project name
         * @param antName when true the project name is converted into Ant friendly name before substitution
         * @return the {@link ProjectOperationsBuilder}
         */
        @NonNull
        public ProjectOperationsBuilder addUpdatedNameProperty(
                @NonNull final String propertyName,
                @NullAllowed String propertyPattern,
                final boolean antName) {
            Parameters.notNull("propertyName", propertyName);  //NOI18N
            if (propertyPattern == null) {
                propertyPattern = "{0}";    //NOI18N
            }
            updatedProps.put(propertyName, Pair.<String,Boolean>of(propertyPattern, antName));
            return this;
        }

        /**
         * Sets the project operation callback.
         * @param callback the callback
         * @return the {@link ProjectOperationsBuilder}
         */
        @NonNull
        public ProjectOperationsBuilder setCallback(@NonNull final Callback callback) {
            Parameters.notNull("callback", callback);   //NOI18N
            this.callback = callback;
            return this;
        }

        /**
         * Creates a new configured {@link CopyOperationImplementation},
         * {@link DeleteOperationImplementation} and {@link MoveOrRenameOperationImplementation}
         * instance.
         * @return the project operations implementation
         */
        @NonNull
        public DataFilesProviderImplementation build() {
            if (cleanTargets.isEmpty()) {
                cleanTargets.add(TARGET_CLEAN);
            }
            return new Operations(
                project,
                eval,
                helper,
                refHelper,
                sources,
                tests,
                buildScriptProperty,
                additionalMetadataFiles,
                additionalDataFiles,
                cleanTargets,
                privateProps,
                updatedProps,
                callback);
        }
    }


    private static class Operations implements DataFilesProviderImplementation,
            DeleteOperationImplementation,
            CopyOperationImplementation, MoveOrRenameOperationImplementation {

        private final Project project;
        private final PropertyEvaluator eval;
        private final UpdateHelper helper;
        private final ReferenceHelper refHelper;
        private final SourceRoots sources;
        private final SourceRoots tests;
        private final String buildScriptProperty;
        private final List<? extends String> additionalMetadataFiles;
        private final List<? extends String> additionalDataFiles;
        private final List<? extends String> cleanTargets;
        private final Set<? extends String> privateProps;
        private final Map<String,Pair<String,Boolean>> updatedProps;
        private final Callback callback;


        //RELY: Valid only on original project after the notifyMoving or notifyCopying was called
        private final Map<String,String> privatePropsToRestore = new HashMap<>();
        //RELY: Valid only on original project after the notifyMoving or notifyCopying was called
        private String absolutesRelPath;
        //RELY: Valid only on original project after the notifyMoving or notifyCopying was called
        private String libraryPath;
        //RELY: Valid only on original project after the notifyMoving or notifyCopying was called
        private File libraryFile;
        //RELY: Valid only on original project after the notifyMoving or notifyCopying was called
        private boolean libraryWithinProject;
        //RELY: Valid only on original project after the notifyMoving or notifyCopying was called
        private FileSystem configs;
        //RELY: Valid only on original project after the notifyMoving was called
        private Collection<Dependency> dependenciesToFix;

        Operations(
            @NonNull final Project project,
            @NonNull final PropertyEvaluator eval,
            @NonNull final UpdateHelper helper,
            @NonNull final ReferenceHelper refHelper,
            @NonNull final SourceRoots sources,
            @NonNull final SourceRoots tests,
            @NonNull final String buildScriptProperty,
            @NonNull final List<? extends String> additionalMetadataFiles,
            @NonNull final List<? extends String> additionalDataFiles,
            @NonNull final List<? extends String> cleanTargets,
            @NonNull final Set<? extends String> privateProps,
            @NonNull final Map<String,Pair<String,Boolean>> updatedProps,
            @NullAllowed final Callback callback) {
            Parameters.notNull("project", project); //NOI18N
            Parameters.notNull("eval", eval);   //NOI18N
            Parameters.notNull("helper", helper);   //NOI18N
            Parameters.notNull("refHelper", refHelper); //NOI18N
            Parameters.notNull("sources", sources); //NOI18N
            Parameters.notNull("tests", tests); //NOI18N
            Parameters.notNull("buildScriptProperty", buildScriptProperty); //NOI18N
            Parameters.notNull("additionalMetadataFiles", additionalMetadataFiles); //NOI18N
            Parameters.notNull("additionalDataFiles", additionalDataFiles); //NOI18N
            Parameters.notNull("cleanTargets", cleanTargets);   //NOI18N
            Parameters.notNull("privateProps", privateProps);   //NOI18N
            Parameters.notNull("updatedProps", updatedProps);   //NOI18N
            this.project = project;
            this.eval = eval;
            this.helper = helper;
            this.refHelper = refHelper;
            this.sources = sources;
            this.tests = tests;
            this.buildScriptProperty = buildScriptProperty;
            this.additionalMetadataFiles = additionalMetadataFiles;
            this.additionalDataFiles = additionalDataFiles;
            this.cleanTargets = cleanTargets;
            this.privateProps = privateProps;
            this.updatedProps = updatedProps;
            this.callback = callback;
        }

        @Override
        public void notifyDeleting() throws IOException {
            before(Callback.Operation.DELETE);
            clean();
        }

        @Override
        public void notifyDeleted() throws IOException {
            helper.getAntProjectHelper().notifyDeleted();
            after(Callback.Operation.DELETE, null, null);
        }

        @Override
        public List<FileObject> getMetadataFiles() {
            final FileObject projectDirectory = project.getProjectDirectory();
            final List<FileObject> files = new ArrayList<>();
            addFile(projectDirectory, "nbproject", files); // NOI18N
            addFile(projectDirectory, CommonProjectUtils.getBuildXmlName(eval, buildScriptProperty), files);
            for (String amf : additionalMetadataFiles) {
                addFile(projectDirectory, amf, files);
            }
            return files;
        }

        @Override
        public List<FileObject> getDataFiles() {
            final FileObject projectDirectory = project.getProjectDirectory();
            final List<FileObject> files = new ArrayList<>();
            //add source & test roots
            Collections.addAll(files, sources.getRoots());
            Collections.addAll(files, tests.getRoots());
            // add libraries folder if it is within project:
            final AntProjectHelper aph = helper.getAntProjectHelper();
            if (aph.getLibrariesLocation() != null) {
                File f = aph.resolveFile(aph.getLibrariesLocation());
                if (f != null && f.exists()) {
                    FileObject libFolder = FileUtil.toFileObject(f).getParent();
                    if (FileUtil.isParentOf(projectDirectory, libFolder)) {
                        files.add(libFolder);
                    }
                }
            }
            //add additional files
            for (String adf : additionalDataFiles) {
                addFile(projectDirectory, adf, files);
            }
            return files;
        }

        @Override
        public void notifyCopying() throws IOException {
            before(Callback.Operation.COPY);
            rememberLibraryLocation();
            readPrivateProperties();
            rememberConfigurations();
        }

        @Override
        public void notifyCopied(Project original, File originalPath, String nueName) throws IOException {
            if (original == null) {
                //do nothing for the original project.
                return ;
            }
            final Operations origOperations = original.getLookup().lookup(Operations.class);
            fixLibraryLocation(origOperations);
            fixPrivateProperties(origOperations);
            updateProjectProperties(nueName);
            refHelper.fixReferences(originalPath);
            restoreConfigurations(origOperations);
            after(Callback.Operation.COPY, nueName, null);
        }

        @Override
        public void notifyRenaming() throws IOException {
            if (!this.helper.requestUpdate()) {
                throw new IOException(NbBundle.getMessage(
                    ProjectOperations.class,
                    "MSG_OldProjectMetadata"));
            }
            before(Callback.Operation.RENAME);
            clean();
        }

        @Override
        public void notifyRenamed(String nueName) throws IOException {
            updateProjectProperties(nueName);
            after(Callback.Operation.RENAME, nueName, null);
        }

        @Override
        public void notifyMoving() throws IOException {
            if (!this.helper.requestUpdate()) {
            throw new IOException (NbBundle.getMessage(
                ProjectOperations.class,
                "MSG_OldProjectMetadata"));
            }
            before(Callback.Operation.MOVE);
            rememberLibraryLocation();
            readPrivateProperties ();
            rememberConfigurations();
            rememberDependencies();
            clean();
        }

        @Override
        public void notifyMoved(Project original, File originalPath, String nueName) throws IOException {
            if (original == null) {
                helper.getAntProjectHelper().notifyDeleted();
                return ;
            }
            final Operations origOperations = original.getLookup().lookup(Operations.class);
            fixLibraryLocation(origOperations);
            fixPrivateProperties (origOperations);
            updateProjectProperties(nueName);
            refHelper.fixReferences(originalPath);
            restoreConfigurations(origOperations);
            fixDependencies(origOperations);
            after(Callback.Operation.MOVE, nueName, Pair.<File, Project>of(originalPath,original));
        }

        private void clean() throws IOException {
            final Properties p = new Properties();
            final String buildXmlName = CommonProjectUtils.getBuildXmlName(eval, buildScriptProperty);
            final FileObject buildXML = project.getProjectDirectory().getFileObject(buildXmlName);
            if (buildXML != null) {
                ActionUtils.runTarget(buildXML, cleanTargets.toArray(new String[0]), p)
                           .waitFinished();
            } else {
                LOG.log(
                    Level.INFO,
                    "Not cleaning the project: {0}, the build file: {1} does not exist.", //NOI18N
                    new Object[] {
                        ProjectUtils.getInformation(project).getDisplayName(),
                        buildXmlName
                    });
            }
        }

        private void rememberLibraryLocation() {
            libraryWithinProject = false;
            absolutesRelPath = null;
            libraryPath = helper.getAntProjectHelper().getLibrariesLocation();
            if (libraryPath != null) {
                File prjRoot = FileUtil.toFile(project.getProjectDirectory());
                libraryFile = PropertyUtils.resolveFile(prjRoot, libraryPath);
                if (FileOwnerQuery.getOwner(Utilities.toURI(libraryFile)) == project &&
                        libraryFile.getAbsolutePath().startsWith(prjRoot.getAbsolutePath())) {
                    //do not update the relative path if within the project..
                    libraryWithinProject = true;
                    FileObject fo = FileUtil.toFileObject(libraryFile);
                    if (new File(libraryPath).isAbsolute() && fo != null) {
                        // if absolte path within project, it will get moved/copied..
                        absolutesRelPath = FileUtil.getRelativePath(project.getProjectDirectory(), fo);
                    }
                }
            }
        }

        private void readPrivateProperties () {
            ProjectManager.mutex().readAccess(new Runnable() {
                public void run () {
                    privatePropsToRestore.clear();
                    for (String privateProp : privateProps) {
                        backUpPrivateProp(privateProp);
                    }
                }
            });
        }
        //where
        /**
         * Threading: Has to be called under project mutex
         */
        private void backUpPrivateProp(String propName) {
            assert ProjectManager.mutex().isReadAccess() || ProjectManager.mutex().isWriteAccess();
            final String tmp = helper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH).getProperty(propName);
            if (tmp != null) {
                privatePropsToRestore.put(propName, tmp);
            }
        }

        private void rememberConfigurations () {
            FileObject fo = project.getProjectDirectory().getFileObject(ProjectConfigurations.CONFIG_PROPS_PATH);
            if (fo != null) {
                //Has configurations
                try {
                    FileSystem fs = FileUtil.createMemoryFileSystem();
                    FileUtil.copyFile(fo, fs.getRoot(),fo.getName());
                    fo = project.getProjectDirectory().getFileObject("nbproject/private/configs");      //NOI18N
                    if (fo != null && fo.isFolder()) {
                        FileObject cfgs = fs.getRoot().createFolder("configs");                         //NOI18N
                        for (FileObject child : fo.getChildren()) {
                            FileUtil.copyFile(child, cfgs, child.getName());
                        }
                    }
                    configs = fs;
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }
        }

        private void rememberDependencies() {
            final AntArtifactProvider aap = project.getLookup().lookup(AntArtifactProvider.class);
            if (aap == null) {
                return;
            }
            final Map<URI,Pair<AntArtifact,URI>> artifacts = createArtifactsMap(aap);
            final Set<Project> dependencies = new HashSet<>();
            for (Project prj : OpenProjects.getDefault().getOpenProjects()) {
                final SubprojectProvider spp = prj.getLookup().lookup(SubprojectProvider.class);
                if (spp != null && spp.getSubprojects().contains(project)) {
                    dependencies.add(prj);
                }
            }
            Collection<Dependency> toFix = new ArrayList<>();
            for (Project depProject : dependencies) {
                for (SourceGroup sg : ProjectUtils.getSources(depProject).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA)) {
                    Set<URI> roots = classPathURIs(ClassPath.getClassPath(sg.getRootFolder(), ClassPath.COMPILE));
                    for (Map.Entry<URI,Pair<AntArtifact,URI>> e : artifacts.entrySet()) {
                        if (roots.contains(e.getKey())) {
                            final Dependency dep = new Dependency(
                                    depProject,
                                    sg,
                                    e.getValue().first(),
                                    e.getValue().second());
                            if (dep.remove()) {
                                toFix.add(dep);
                            }
                        }
                    }
                }
            }
            dependenciesToFix = toFix;
        }

        private void fixLibraryLocation(Operations original) throws IllegalArgumentException {
            String libPath = original.libraryPath;
            if (libPath != null) {
                if (!new File(libPath).isAbsolute()) {
                    //relative path to libraries
                    if (!original.libraryWithinProject) {
                        File file = original.libraryFile;
                        if (file == null) {
                            // could happen in some rare cases, but in that case the original project was already broken, don't fix.
                            return;
                        }
                        String relativized = PropertyUtils.relativizeFile(FileUtil.toFile(project.getProjectDirectory()), file);
                        if (relativized != null) {
                            helper.getAntProjectHelper().setLibrariesLocation(relativized);
                        } else {
                            //cannot relativize, use absolute path
                            helper.getAntProjectHelper().setLibrariesLocation(file.getAbsolutePath());
                        }
                    } else {
                        //got copied over to new location.. the relative path is the same..
                    }
                } else {

                    //absolute path to libraries..
                    if (original.libraryWithinProject) {
                        if (original.absolutesRelPath != null) {
                            helper.getAntProjectHelper().setLibrariesLocation(PropertyUtils.resolveFile(FileUtil.toFile(project.getProjectDirectory()), original.absolutesRelPath).getAbsolutePath());
                        }
                    } else {
                        // absolute path to an external folder stays the same.
                    }
                }
            }
        }

        private void fixPrivateProperties (final Operations original) {
            if (original != null && !original.privatePropsToRestore.isEmpty()) {
                ProjectManager.mutex().writeAccess(new Runnable () {
                    public void run () {
                        final EditableProperties ep = helper.getProperties (AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                        for (Map.Entry<String,String> entry : original.privatePropsToRestore.entrySet()) {
                            ep.put(entry.getKey(), entry.getValue());
                        }
                        helper.putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
                    }
                });
            }
        }

        private void restoreConfigurations (final Operations original) {
            final FileSystem fs = original.configs;
            original.configs = null;
            if (fs != null) {
                try {
                    FileObject fo = fs.getRoot().getFileObject("config.properties");        //NOI18N
                    if (fo != null) {
                        FileObject privateFolder = FileUtil.createFolder(project.getProjectDirectory(), "nbproject/private");  //NOI18N
                        if (privateFolder != null) {
                            // #131857: SyncFailedException : check for file existence before FileUtil.copyFile
                            FileObject oldFile = privateFolder.getFileObject(fo.getName(), fo.getExt());
                            if (oldFile != null) {
                                //Probably delete outside of IDE + move. First try to repair FS cache
                                privateFolder.refresh();
                                oldFile = privateFolder.getFileObject(fo.getName(), fo.getExt());
                                if (oldFile != null) {
                                    //The file still exists, delete it.
                                    oldFile.delete();
                                }
                            }

                            FileUtil.copyFile(fo, privateFolder, fo.getName());
                        }
                    }
                    fo = fs.getRoot().getFileObject("configs");                             //NOI18N
                    if (fo != null) {
                        FileObject configsFolder = FileUtil.createFolder(project.getProjectDirectory(), "nbproject/private/configs");  //NOI18N
                        if (configsFolder != null) {
                            for (FileObject child : fo.getChildren()) {
                                FileUtil.copyFile(child, configsFolder, child.getName());
                            }
                        }
                    }
                } catch (IOException ioe) {
                    Exceptions.printStackTrace(ioe);
                }
            }
        }

        private void fixDependencies(final Operations original) {
            final Collection<Dependency> toFix = original.dependenciesToFix;
            if (toFix != null) {
                for (Dependency dep : toFix) {
                    dep.add(project);
                }
            }
        }

        private void updateProjectProperties(@NonNull final String newName) {
            if (!updatedProps.isEmpty()) {
                final ProjectInformation pi = ProjectUtils.getInformation(project);
                final String oldName = pi.getDisplayName();
                final EditableProperties ep = helper.getProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH);
                for (Map.Entry<String,Pair<String,Boolean>> e : updatedProps.entrySet()) {
                    final String propName = e.getKey();
                    final String format = e.getValue().first();
                    final boolean antName = e.getValue().second();
                    final String oldProp = MessageFormat.format(
                        format,
                        antName ?
                            PropertyUtils.getUsablePropertyName(oldName) :
                            oldName);
                    final String propValue = ep.getProperty(propName);
                    if (oldProp.equals (propValue)) {
                        final String newProp = MessageFormat.format(
                            format,
                            antName ?
                                PropertyUtils.getUsablePropertyName(newName) :
                                newName);
                        ep.put (propName, newProp);
                    }
                }
                helper.putProperties (AntProjectHelper.PROJECT_PROPERTIES_PATH,ep);
            }
        }

        private void before(@NonNull final Callback.Operation operation) {
            if (callback != null) {
                callback.beforeOperation(operation);
            }
        }

        private void after(
            @NonNull final Callback.Operation operation,
            @NullAllowed final String newName,
            @NullAllowed final Pair<File,Project> oldProject) {
            if (callback != null) {
                callback.afterOperation(operation, newName, oldProject);
            }
        }

        private static void addFile(FileObject projectDirectory, String fileName, List<FileObject> result) {
            final FileObject file = projectDirectory.getFileObject(fileName);
            if (file != null) {
                result.add(file);
            }
        }

        @NonNull
        private static Map<URI,Pair<AntArtifact,URI>> createArtifactsMap(@NonNull final AntArtifactProvider aap) {
            final Map<URI,Pair<AntArtifact,URI>> res = new HashMap<>();
            for (AntArtifact aa : aap.getBuildArtifacts()) {
                for (URI uri : aa.getArtifactLocations()) {
                    final URI absoluteURI = uri.isAbsolute() ?
                            uri :
                            resolve(aa.getProject().getProjectDirectory(), uri);
                    res.put(absoluteURI, Pair.of(aa,uri));
                }
            }
            return res;
        }

        @NonNull
        private static URI resolve (
                @NonNull final FileObject prjDir,
                @NonNull final URI relative) {
            return BaseUtilities.normalizeURI(prjDir.toURI().resolve(relative));
        }

        @NonNull
        private static Set<URI> classPathURIs(@NullAllowed final ClassPath cp) {
            final Set<URI> res = new HashSet<>();
            if (cp != null) {
                for (ClassPath.Entry e : cp.entries()) {
                    try {
                        final URL rootUrl = e.getURL();
                        URL fileURL = FileUtil.getArchiveFile(rootUrl);
                        if (fileURL == null) {
                            fileURL = rootUrl;
                        }
                        res.add(fileURL.toURI());
                    } catch (URISyntaxException ex) {
                        LOG.log(
                                Level.WARNING,
                                "Cannot convert to URI: {0}, reason: {1}",  //NOI18N
                                new Object[]{
                                    e.getURL(),
                                    ex.getMessage()
                                });
                    }
                }
            }
            return res;
        }

        private static class Dependency {
            private final Project project;
            private final SourceGroup root;
            private final AntArtifact onArt;
            private final URI onLoc;

            Dependency(
                @NonNull final Project project,
                @NonNull final SourceGroup root,
                @NonNull final AntArtifact onArt,
                @NonNull final URI onLoc) {
                this.project = project;
                this.root = root;
                this.onArt = onArt;
                this.onLoc = onLoc;
            }

            boolean remove() {
                final ClassPathModifier cpm = project.getLookup().lookup(ClassPathModifier.class);
                boolean success = false;
                if (cpm != null) {
                    try {
                        cpm.removeAntArtifacts(
                                new AntArtifact[]{onArt},
                                new URI[] {onLoc},
                                root,
                                ClassPath.COMPILE);
                        success = true;
                    } catch (IOException | UnsupportedOperationException ex) {
                        LOG.log(
                                Level.INFO,
                                "Cannot fix dependencies in project: {0}",  //NOI18N
                                ProjectUtils.getInformation(project).getDisplayName());
                    }
                }
                return success;
            }

            boolean add(@NonNull final Project newProject) {
                boolean success = false;
                final AntArtifactProvider aap = newProject.getLookup().lookup(AntArtifactProvider.class);
                final ClassPathModifier cpm = project.getLookup().lookup(ClassPathModifier.class);
                if (aap != null && cpm != null) {
                    AntArtifact newOn = null;
                    URI newOnLoc = null;
                    for (AntArtifact a : aap.getBuildArtifacts()) {
                        if (Objects.equals(a.getType(), onArt.getType()) &&
                            Objects.equals(a.getTargetName(), onArt.getTargetName()) &&
                            Objects.equals(a.getCleanTargetName(), onArt.getCleanTargetName())) {
                            newOn = a;
                            int index = 0;
                            final URI[] oal = onArt.getArtifactLocations();
                            for (int i = 0; i < oal.length; i++) {
                                if (oal[i].equals(onLoc)) {
                                    index = i;
                                    break;
                                }
                            }
                            newOnLoc = a.getArtifactLocations()[index];
                            break;
                        }
                    }
                    if (newOn != null) {
                        try {
                            cpm.addAntArtifacts(
                                    new AntArtifact[]{newOn},
                                    new URI[] {newOnLoc},
                                    root,
                                    ClassPath.COMPILE);
                            success = true;
                        } catch (IOException | UnsupportedOperationException ex) {
                            LOG.log(
                                    Level.INFO,
                                    "Cannot fix dependencies in project: {0}",  //NOI18N
                                    ProjectUtils.getInformation(project).getDisplayName());
                        }
                    }
                }
                return success;
            }

            @Override
            public String toString() {
                return String.format(
                    "%s in %s depends on %s in %s",   //NOI18N
                    root.getName(),
                    ProjectUtils.getInformation(project).getDisplayName(),
                    onLoc,
                    ProjectUtils.getInformation(onArt.getProject()).getDisplayName());
            }
        }
    }

}
