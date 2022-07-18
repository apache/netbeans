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

package org.netbeans.modules.gradle.spi.newproject;

import org.netbeans.modules.gradle.NbGradleProjectImpl;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import org.gradle.tooling.GradleConnectionException;
import org.gradle.tooling.GradleConnector;
import org.gradle.tooling.ProjectConnection;
import org.netbeans.api.progress.ProgressHandle;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.modules.gradle.GradleProjectLoader;
import org.netbeans.modules.gradle.ProjectTrust;
import org.netbeans.modules.gradle.api.GradleProjects;
import org.netbeans.modules.gradle.api.NbGradleProject.Quality;
import org.openide.loaders.DataFolder;
import org.openide.loaders.DataObject;
import org.openide.util.NbBundle;

/**
 *
 * @author Laszlo Kishalmi
 */
public final class TemplateOperation implements Runnable {
    private static final Logger LOG = Logger.getLogger(TemplateOperation.class.getName());
    
    public interface ProjectConfigurator {
        void configure(Project project);
    }

    interface OperationStep {
        /**
         * Return the message which shall be displayed on the progress handle.
         * @return the progress message for this step.
         */
        String getMessage();

        /**
         * Execute the operation step and return the files
         * which shall be opened after the whole TemplateOperation process.
         *
         * @return the files which shall be opened in the IDE or {@code null} for none.
         */
        Set<FileObject> execute();
    }

    final List<OperationStep> steps = new LinkedList<>();
    final ProgressHandle handle;
    final Set<FileObject> importantFiles = new LinkedHashSet<>();

    public TemplateOperation(ProgressHandle handle) {
        this.handle = handle;
    }

    public TemplateOperation() {
        this(null);
    }

    @Override
    public void run() {
        if (handle != null) {
            handle.start(steps.size());
        }
        try {
            int work = 0;
            for (OperationStep step : steps) {
                if (handle != null) {
                    handle.progress(step.getMessage(), work++);
                }
                LOG.log(Level.FINE, "Executing Gradle Project Template Operation {0}", step);
                Set<FileObject> filesToOpen = step.execute();
                if (filesToOpen != null) {
                    importantFiles.addAll(filesToOpen);
                }
            }
        } finally {
            if (handle != null) {
                handle.finish();
            }
        }
    }

    public Set<FileObject> getImportantFiles() {
        return Collections.unmodifiableSet(importantFiles);
    }

    @Messages({
        "# {0} - Folder name",
        "MSG_CREATE_FOLDER=Creating foder: {0}"
    })
    public void createFolder(File target) {
        steps.add(new CreateDirStep(target, Bundle.MSG_CREATE_FOLDER(target.getName())));
    }

    @Messages({
        "# {0} - Package name",
        "MSG_CREATE_PACKAGE=Creating package: {0}"
    })
    public void createPackage(File base, String pkg) {
        String relativePath = pkg.replace('.', '/');
        steps.add(new CreateDirStep(new File(base, relativePath),Bundle.MSG_CREATE_PACKAGE(pkg)));
    }

    public void addConfigureProject(File projectDir, ProjectConfigurator configurator) {
        steps.add(new ConfigureProjectStep(projectDir, configurator));
    }

    public void addWrapperInit(File target) {
        steps.add(new InitGradleWrapper(target));
    }

    /** *  Begin creation of new project using Gradle's 
     * <a target="_blank" href="https://docs.gradle.org/current/userguide/build_init_plugin.html">gradle init</a>
     * functionality. Use the returned {@link InitOperation} object to specify 
     * additional properties and then call  
     * {@link InitOperation#add()} to finish the request.
     * 
     * 
     * @param target the directory to place the project at
     * @param type either {@code java-application}, {@code java-library}, etc.
     * @return the {@link InitOperation} builder to finish the request
     * @since 2.20
     */
    public InitOperation createGradleInit(File target, String type) {
        return new InitStep(target, type);
    }

    /** Builder to specify additional parameters for the {@link #createGradleInit(java.io.File, java.lang.String)}
     * operation. At the end call {@link #add()} to finish the operation and 
     * add it to the list of {@link OperationStep}s to perform.
     * 
     * @since 2.20
     */
    public abstract class InitOperation {
        InitOperation() {
        }

        /** Add the operation to the list of {@link OperationStep}s to perform.
         * @since 2.20
         */
        public final void add() {
            steps.add((OperationStep) this);
        }

        /** Specify the type of DSL to use.
         * @param dsl either {@code groovy} or {@code kotlin}
         * @return this builder to chain the calls.
         * @since 2.20
         */
        public abstract InitOperation dsl(String dsl);

        /** Specify the type of test framework.
         * @param testFramework {@code junit-jupiter}, {@code spock}, {@code testng}
         * @return this builder to chain the calls.
         * @since 2.20
         */
        public abstract InitOperation testFramework(String testFramework);

        /** Specify base package of the project
         * @param pkg base package for the sources
         * @return this builder to chain the calls.
         * @since 2.20
         */
        public abstract InitOperation basePackage(String pkg);

        /** Specify project name.
         * @param name the (logical) name of the project
         * @return this builder to chain the calls.
         * @since 2.20
         */
        public abstract InitOperation projectName(String name);
    }

    private final class InitStep extends InitOperation implements OperationStep {
        private final File target;
        private final String type;
        private String dsl;
        private String testFramework;
        private String basePackage;
        private String projectName;

        InitStep(File target, String type) {
            this.target = target;
            this.type = type;
        }

        @Override
        public InitStep dsl(String dsl) {
            this.dsl = dsl;
            return this;
        }

        @Override
        public InitStep testFramework(String testFramework) {
            this.testFramework = testFramework;
            return this;
        }

        @Override
        public InitStep basePackage(String pkg) {
            this.basePackage = pkg;
            return this;
        }

        @Override
        public InitStep projectName(String name) {
            this.projectName = name;
            return this;
        }

        @NbBundle.Messages({
            "MSG_INIT_GRADLE=Initializing {0} in {1}"
        })
        @Override
        public String getMessage() {
            return Bundle.MSG_INIT_GRADLE(type, target);
        }

        @Override
        public Set<FileObject> execute() {
            GradleConnector gconn = GradleConnector.newConnector();
            target.mkdirs();
            try (ProjectConnection pconn = gconn.forProjectDirectory(target).connect()) {
                List<String> args = new ArrayList<>();
                args.add("init");
                // gradle init --type java-application --test-framework junit-jupiter --dsl groovy --package com.example --project-name example
                args.add("--type");
                args.add(type);
                // --test-framework junit-jupiter
                if (testFramework != null) {
                    args.add("--test-framework");
                    args.add(testFramework);
                }
                // --dsl groovy
                if (dsl != null) {
                    args.add("--dsl");
                    args.add(dsl);
                }
                // --package com.example
                if (basePackage != null) {
                    args.add("--package");
                    args.add(basePackage);
                }

                // --project-name example
                if (projectName != null) {
                    args.add("--project-name");
                    args.add(projectName);
                }

                pconn.newBuild().withArguments("--offline").forTasks(args.toArray(new String[0])).run(); //NOI18N
            } catch (GradleConnectionException | IllegalStateException ex) {
                // Well for some reason we were  not able to load Gradle.
                // Ignoring that for now
            }
            return Collections.singleton(FileUtil.toFileObject(target));
        }
    }

    public void copyFromFile(String templateName, File target, Map<String, ? extends Object> tokens) {
        steps.add(new CopyFromFileTemplate(templateName, target, tokens, false));
    }

    public void openFromFile(String templateName, File target, Map<String, ? extends Object> tokens) {
        steps.add(new CopyFromFileTemplate(templateName, target, tokens, true));
    }

    public void copyFromTemplate(String template, File target, Map<String, ? extends Object> tokens) {
        steps.add(new CopyFromTemplate(template, target, tokens, false));
    }

    public void openFromTemplate(String template, File target, Map<String, ? extends Object> tokens) {
        steps.add(new CopyFromTemplate(template, target, tokens, true));
    }

    public void addProjectPreload(File projectDir) {
        steps.add(new PreloadProject(projectDir));
    }

    private abstract static class BaseOperationStep implements OperationStep {
        @Override
        public final String toString() {
            return "Step: " + getMessage();
        }
    }
    
    private static final class CreateDirStep extends BaseOperationStep {

        final String message;
        final File dir;

        public CreateDirStep(File dir, String message) {
            this.dir = dir;
            this.message = message;
        }

        @Override
        public String getMessage() {
            return message;
        }

        @Override
        public Set<FileObject> execute() {
            try {
                FileUtil.createFolder(dir);
                Thread.sleep(200);
            } catch (InterruptedException | IOException ex) {
            }
            return null;
        }
        
    }

    private static final class ConfigureProjectStep extends BaseOperationStep {
        final File dir;
        final ProjectConfigurator configurator;

        public ConfigureProjectStep(File dir, ProjectConfigurator configurator) {
            this.dir = dir;
            this.configurator = configurator;
        }

        @Override
        @Messages("MSG_CONFIGURING_PROJECT=Configuring Project...")
        public String getMessage() {
            return Bundle.MSG_CONFIGURING_PROJECT();
        }

        @Override
        public final Set<FileObject> execute() {
            if (GradleProjects.testForProject(dir)) {
                try {
                    FileObject projectDir = FileUtil.toFileObject(dir);
                    Project project = ProjectManager.getDefault().findProject(projectDir);
                    ProjectTrust.getDefault().trustProject(project);
                    NbGradleProjectImpl impl = project != null ? project.getLookup().lookup(NbGradleProjectImpl.class): null;
                    if (impl != null) {
                        impl.projectWithQuality(null, Quality.FULL, false, false);
                        configurator.configure(project);
                    }

                } catch (IOException ex) {
                }
            }
            return Collections.<FileObject>emptySet();
        }

    }
    private static final class PreloadProject extends BaseOperationStep {

        final File dir;

        public PreloadProject(File dir) {
            this.dir = dir;
        }

        @Override
        @Messages({
            "# {0} - Folder Name",
            "MSM_CHECKING_FOLDER=Checking folder: {0}",
            "# {0} - Project Name",
            "MSG_PRELOAD_PROJECT=Load: {0}"
        })
        public String getMessage() {
            return GradleProjects.testForProject(dir) ? Bundle.MSG_PRELOAD_PROJECT(dir.getName()) : Bundle.MSM_CHECKING_FOLDER(dir.getName());
        }

        @Override
        public Set<FileObject> execute() {
            if (GradleProjects.testForProject(dir)) {
                try {
                    FileObject projectDir = FileUtil.toFileObject(dir);
                    Project project = ProjectManager.getDefault().findProject(projectDir);
                    if (project == null) {
                        ProjectManager.getDefault().clearNonProjectCache();
                    }
                    project = ProjectManager.getDefault().findProject(projectDir);
                    if (project != null) {
                        //Let's trust the generated project
                        ProjectTrust.getDefault().trustProject(project);
                        NbGradleProjectImpl nbProject = project.getLookup().lookup(NbGradleProjectImpl.class);
                        if (nbProject != null) {
                            //Just load the project into the cache.
                            GradleProjectLoader loader = nbProject.getLookup().lookup(GradleProjectLoader.class);
                            if (loader != null) {
                                loader.loadProject(Quality.FULL_ONLINE, null, true, false);
                            }
                        }
                        return Collections.singleton(projectDir);
                    }
                } catch (IOException | IllegalArgumentException ex) {
                }
            }
            return null;
        }

    }

    private static final class InitGradleWrapper extends BaseOperationStep {

        final File projectDir;

        public InitGradleWrapper(File projectDir) {
            this.projectDir = projectDir;
        }

        @Override
        @Messages("MSG_INIT_WRAPPER=Initializing Gradle Wrapper")
        public String getMessage() {
            return Bundle.MSG_INIT_WRAPPER();
        }

        @Override
        public Set<FileObject> execute() {
            GradleConnector gconn = GradleConnector.newConnector();
            try (ProjectConnection pconn = gconn.forProjectDirectory(projectDir).connect()) {
                pconn.newBuild().withArguments("--offline").forTasks("wrapper").run(); //NOI18N
            } catch (GradleConnectionException | IllegalStateException ex) {
                // Well for some reason we were  not able to load Gradle.
                // Ignoring that for now
            }
            return null;
        }

    }

    private static final class CopyFromFileTemplate extends BaseOperationStep {
        final File target;
        final Map<String, ? extends Object> tokens;
        final boolean important;
        final String templateName;

        public CopyFromFileTemplate(String templateName, File target, Map<String, ? extends Object> tokens, boolean important) {
            this.templateName = templateName;
            this.target = target;
            this.tokens = tokens;
            this.important = important;
        }

        @Override
        @Messages({
            "# {0} - File name",
            "MSG_COPY_TEMPLATE=Generating {0}..."
        })
        public String getMessage() {
            return Bundle.MSG_COPY_TEMPLATE(target.getName());
        }

        @Override
        public Set<FileObject> execute() {
            ClassLoader l = Lookup.getDefault().lookup(ClassLoader.class);
            if (l == null) {
                l = Thread.currentThread().getContextClassLoader();
            }
            ScriptEngineManager scriptEngineManager = new ScriptEngineManager(l);
            ScriptEngine engine = scriptEngineManager.getEngineByName("freemarker"); //NOI18N
            assert engine != null;
            Map<String, Object> bindings = engine.getContext().getBindings(ScriptContext.ENGINE_SCOPE);
            String basename = target.getName();
            bindings.put("name", basename.replaceFirst("\\.[^./]+$", "")); // NOI18N
            bindings.put("user", System.getProperty("user.name")); // NOI18N
            Date d = new Date();
            bindings.put("date", DateFormat.getDateInstance().format(d)); // NOI18N
            bindings.put("time", DateFormat.getTimeInstance().format(d)); // NOI18N
            bindings.put("nameAndExt", target.getName()); // NOI18N
            bindings.putAll(tokens);

            try {
                FileObject fo = FileUtil.createData(target);
                // Requesting Charset information at this stahe would try to
                // load the project from the dir.
                Charset targetEnc = Charset.defaultCharset();
                bindings.put("encoding", targetEnc.name()); //NOI18N
                try (Writer w = new OutputStreamWriter(fo.getOutputStream(), targetEnc)) {
                    engine.getContext().setWriter(w);
                    //engine.getContext().setAttribute(ScriptEngine.FILENAME, "/" + templateName, ScriptContext.ENGINE_SCOPE);
                    try (Reader is = new InputStreamReader(TemplateOperation.class.getResourceAsStream("/" + templateName))) {
                        engine.eval(is);
                    } catch (IOException | ScriptException ex) {
                        throw new IOException(ex.getMessage(), ex);
                    }
                    return important ? Collections.singleton(fo) : null;
                } catch (IOException ex) {}
            } catch (IOException ex) {}
            return null;
        }

    }

    private static final class CopyFromTemplate extends BaseOperationStep {
        final File target;
        final Map<String, ? extends Object> tokens;
        final boolean important;
        final String templateName;

        public CopyFromTemplate(String templateName, File target, Map<String, ? extends Object> tokens, boolean important) {
            this.templateName = templateName;
            this.target = target;
            this.tokens = tokens;
            this.important = important;
        }


        @Override
        public String getMessage() {
            return Bundle.MSG_COPY_TEMPLATE(target.getName());
        }

        @Override
        public Set<FileObject> execute() {
            FileObject template = FileUtil.getConfigFile(templateName);
            if (template != null) {
                String targetName = target.getName();
                try {
                    FileObject targetParent = FileUtil.createFolder(target.getParentFile());
                    DataFolder targetFolder = DataFolder.findFolder(targetParent);
                    DataObject o = DataObject.find(template);
                    DataObject newData = o.createFromTemplate(targetFolder, targetName, tokens);
                    return important ? Collections.singleton(newData.getPrimaryFile()) : null;
                } catch (IOException ex) {

                }
            }
            return null;
        }

    }
}
