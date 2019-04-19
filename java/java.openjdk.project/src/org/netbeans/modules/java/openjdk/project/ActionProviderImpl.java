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
package org.netbeans.modules.java.openjdk.project;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.Action;

import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.modules.java.openjdk.common.BuildUtils;
import org.netbeans.modules.java.openjdk.common.ShortcutUtils;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import org.netbeans.spi.project.ui.support.ProjectSensitiveActions;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.modules.InstalledFileLocator;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle.Messages;
import org.openide.util.Pair;
import org.openide.util.Task;
import org.openide.util.TaskListener;

/**
 *
 * @author lahvac
 */
public class ActionProviderImpl implements ActionProvider {

    private static final String COMMAND_BUILD_FAST = "build-fast";
    private static final String COMMAND_BUILD_GENERIC_FAST = "build-generic-fast";
    private static final String COMMAND_SELECT_TOOL = "select.tool";
    
    private static final Map<Pair<String, RootKind>, String[]> command2Targets = new HashMap<Pair<String, RootKind>, String[]>() {{
        put(Pair.of(COMMAND_BUILD, RootKind.SOURCE), new String[] {"build"});
        put(Pair.of(COMMAND_BUILD, RootKind.TEST), new String[] {"build"});
        put(Pair.of(COMMAND_BUILD_FAST, RootKind.SOURCE), new String[] {"build-fast"});
        put(Pair.of(COMMAND_BUILD_FAST, RootKind.TEST), new String[] {"build-fast"});
        put(Pair.of(COMMAND_CLEAN, RootKind.SOURCE), new String[] {"clean"});
        put(Pair.of(COMMAND_CLEAN, RootKind.TEST), new String[] {"clean"});
        put(Pair.of(COMMAND_REBUILD, RootKind.SOURCE), new String[] {"clean", "build"});
        put(Pair.of(COMMAND_REBUILD, RootKind.TEST), new String[] {"clean", "build"});
        put(Pair.of(COMMAND_COMPILE_SINGLE, RootKind.SOURCE), new String[] {"compile-single"});
        put(Pair.of(COMMAND_COMPILE_SINGLE, RootKind.TEST), new String[] {"compile-single"});
        put(Pair.of(COMMAND_RUN, RootKind.SOURCE), new String[] {"run"});
        put(Pair.of(COMMAND_RUN, RootKind.TEST), new String[] {"run"});
        put(Pair.of(COMMAND_RUN_SINGLE, RootKind.SOURCE), new String[] {"run-single"});
        put(Pair.of(COMMAND_RUN_SINGLE, RootKind.TEST), new String[] {"jtreg"});
//    COMMAND_TEST;
//    COMMAND_TEST_SINGLE;
        put(Pair.of(COMMAND_DEBUG, RootKind.SOURCE), new String[] {"debug"});
        put(Pair.of(COMMAND_DEBUG, RootKind.TEST), new String[] {"debug"});
        put(Pair.of(COMMAND_DEBUG_SINGLE, RootKind.SOURCE), new String[] {"debug-single"});
        put(Pair.of(COMMAND_DEBUG_SINGLE, RootKind.TEST), new String[] {"debug-jtreg"});
//    COMMAND_DEBUG_TEST_SINGLE;
//    COMMAND_DEBUG_STEP_INTO;
        put(Pair.of(COMMAND_SELECT_TOOL, RootKind.SOURCE), new String[] {"select-tool"});
        put(Pair.of(COMMAND_SELECT_TOOL, RootKind.TEST), new String[] {"select-tool"});
    }};

    private static final Map<Pair<String, RootKind>, RunSingleConfig> command2Properties = new HashMap<Pair<String, RootKind>, RunSingleConfig>() {{
        put(Pair.of(COMMAND_COMPILE_SINGLE, RootKind.SOURCE), new RunSingleConfig("includes", RunSingleConfig.Type.RELATIVE, ","));
        put(Pair.of(COMMAND_COMPILE_SINGLE, RootKind.TEST), new RunSingleConfig("includes", RunSingleConfig.Type.RELATIVE, ","));
        put(Pair.of(COMMAND_RUN_SINGLE, RootKind.SOURCE), new RunSingleConfig("run.classname", RunSingleConfig.Type.CLASSNAME, null));
        put(Pair.of(COMMAND_RUN_SINGLE, RootKind.TEST), new RunSingleConfig("jtreg.tests", RunSingleConfig.Type.RELATIVE, " "));
        put(Pair.of(COMMAND_DEBUG_SINGLE, RootKind.SOURCE), new RunSingleConfig("debug.classname", RunSingleConfig.Type.CLASSNAME, null));
        put(Pair.of(COMMAND_DEBUG_SINGLE, RootKind.TEST), new RunSingleConfig("jtreg.tests", RunSingleConfig.Type.RELATIVE, " "));
    }};

    private final JDKProject project;
    private final FileObject repository;
    private final FileObject script;
    private final FileObject genericScript;
    private final String[] supportedActions;

    public ActionProviderImpl(JDKProject project) {
        this.project = project;

        FileObject repo = project.currentModule != null ? project.getProjectDirectory().getParent().getParent()
                                                        : project.getProjectDirectory().getParent();

        repository = repo;

        File scriptFile = InstalledFileLocator.getDefault().locate("scripts/build-generic.xml", "org.netbeans.modules.java.openjdk.project", false);

        genericScript = FileUtil.toFileObject(scriptFile);

        if (project.currentModule != null && project.moduleRepository.isConsolidatedRepo()) {
            String repoName = ShortcutUtils.getDefault().inferLegacyRepository(project);
            File fastBuild = InstalledFileLocator.getDefault().locate("scripts/build-" + repoName + "-consol.xml", "org.netbeans.modules.java.openjdk.project", false);
            if (fastBuild != null && ShortcutUtils.getDefault().shouldUseCustomBuild(repoName, FileUtil.getRelativePath(repo, project.getProjectDirectory()))) {
                scriptFile = fastBuild;
            }
        } else {
            String repoName = repo.getNameExt();
            File fastBuild = InstalledFileLocator.getDefault().locate("scripts/build-" + repoName + ".xml", "org.netbeans.modules.java.openjdk.project", false);
            if (fastBuild != null && ShortcutUtils.getDefault().shouldUseCustomBuild(repoName, FileUtil.getRelativePath(repo, project.getProjectDirectory()))) {
                scriptFile = fastBuild;
            }
        }

        script = FileUtil.toFileObject(scriptFile);

        String[] supported = new String[0];

        try {
            for (String l : script.asLines("UTF-8")) {
                if (l.contains("SUPPORTED_ACTIONS:")) {
                    String[] actions = l.substring(l.indexOf(':') + 1).trim().split(",");
                    Set<String> filteredActions = new HashSet<>();

                    for (Pair<String, RootKind> k : command2Targets.keySet()) {
                        filteredActions.add(k.first());
                    }

                    filteredActions.retainAll(Arrays.asList(actions));
                    filteredActions.add(COMMAND_BUILD_GENERIC_FAST);
                    filteredActions.add(COMMAND_PROFILE_TEST_SINGLE);
                    supported = filteredActions.toArray(new String[0]);
                    break;
                }
            }
        } catch (IOException ex) {
            //???
            Exceptions.printStackTrace(ex);
        }
        
        supportedActions = supported;
    }

    @Override
    public String[] getSupportedActions() {
        return supportedActions;
    }

    @Override
    public void invokeAction(String command, Lookup context) throws IllegalArgumentException {
        if (COMMAND_PROFILE_TEST_SINGLE.equals(command)) {
            for (ActionProvider ap : Lookup.getDefault().lookupAll(ActionProvider.class)) {
                if (new HashSet<>(Arrays.asList(ap.getSupportedActions())).contains(COMMAND_PROFILE_TEST_SINGLE) && ap.isActionEnabled(COMMAND_PROFILE_TEST_SINGLE, context)) {
                    ap.invokeAction(COMMAND_PROFILE_TEST_SINGLE, context);
                    return ;
                }
            }
        }
        FileObject scriptFO = script;
        if (COMMAND_BUILD_GENERIC_FAST.equals(command)) {
            Settings settings = project.getLookup().lookup(Settings.class);
            switch (settings.getRunBuildSetting()) {
                case NEVER:
                    ActionProgress.start(context).finished(true);
                    return;
                case ALWAYS:
                default:
                    break;
            }
            scriptFO = genericScript;
            command = COMMAND_BUILD_FAST; //XXX: should only do this if genericScript supports it
        }
        Properties props = new Properties();
        FileObject basedirFO = project.currentModule != null ? scriptFO == genericScript ? project.moduleRepository.getJDKRoot()
                                                                                         : repository
                                                             : repository.getParent();
        props.put("basedir", FileUtil.toFile(basedirFO).getAbsolutePath());
        props.put("CONF", project.configurations.getActiveConfiguration().getLocation().getName());
        props.put("nb.jdk.project.target.java.home", BuildUtils.findTargetJavaHome(project.getProjectDirectory()).getAbsolutePath());
        RootKind kind = getKind(context);
        RunSingleConfig singleFileProperty = command2Properties.get(Pair.of(command, kind));
        if (singleFileProperty != null) {
            String srcdir = "";
            String moduleName = "";
            StringBuilder value = new StringBuilder();
            String sep = "";
            for (FileObject file : context.lookupAll(FileObject.class)) {
                value.append(sep);
                ClassPath sourceCP;
                switch (kind) {
                    case SOURCE:
                        sourceCP = ClassPath.getClassPath(file, ClassPath.SOURCE);
                        break;
                    case TEST:
                        sourceCP = ClassPathSupport.createClassPath(BuildUtils.getFileObject(project.getProjectDirectory(), "../../test"));
                        break;
                    default:
                        throw new IllegalStateException(kind.name());
                }
                value.append(singleFileProperty.valueType.convert(sourceCP, file));
                sep = singleFileProperty.separator;
                FileObject ownerRoot = sourceCP.findOwnerRoot(file);
                srcdir = FileUtil.getRelativePath(BuildUtils.getFileObject(project.getProjectDirectory(), "../.."), ownerRoot);
                moduleName = ownerRoot.getParent().getParent().getNameExt();
            }
            props.put(singleFileProperty.propertyName, value.toString());
            props.put("srcdir", srcdir);
            props.put("module.name", moduleName);
        }
        final ActionProgress progress = ActionProgress.start(context);
        try {
            ActionUtils.runTarget(scriptFO, command2Targets.get(Pair.of(command, kind)), props)
                       .addTaskListener(new TaskListener() {
                @Override
                public void taskFinished(Task task) {
                    progress.finished(((ExecutorTask) task).result() == 0);
                }
            });
        } catch (IOException ex) {
            //???
            Exceptions.printStackTrace(ex);
            progress.finished(false);
        }
    }

    @Override
    public boolean isActionEnabled(String command, Lookup context) throws IllegalArgumentException {
        RootKind kind = getKind(context);
        RunSingleConfig singleFileProperty = command2Properties.get(Pair.of(command, kind));
        if (singleFileProperty != null) {
            int fileCount = context.lookupAll(FileObject.class).size();

            if (fileCount == 0 || (fileCount > 1 && singleFileProperty.separator == null))
                return false;
        }

        return true;
    }

    private RootKind getKind(Lookup context) {
        FileObject aFile = context.lookup(FileObject.class);
        FileObject testDir = BuildUtils.getFileObject(project.getProjectDirectory(), "../../test");
        return aFile != null && testDir != null && FileUtil.isParentOf(testDir, aFile) ? RootKind.TEST : RootKind.SOURCE;
    }

    private static final class RunSingleConfig {
        public final String propertyName;
        public final Type valueType;
        public final String separator;

        public RunSingleConfig(String propertyName, Type valueType, String separator) {
            this.propertyName = propertyName;
            this.valueType = valueType;
            this.separator = separator;
        }

        enum Type {
            RELATIVE {
                @Override
                public String convert(ClassPath sourceCP, FileObject file) {
                    return sourceCP.getResourceName(file);
                }
            },
            CLASSNAME {
                @Override
                public String convert(ClassPath sourceCP, FileObject file) {
                    return sourceCP.getResourceName(file, '.', false);
                }
            };
            public abstract String convert(ClassPath sourceCP, FileObject file);
        }

    }

    private enum RootKind {
        SOURCE,
        TEST;
    }

    //for layer:
    @Messages("DN_SelectTool=Select Tool (langtools only)")
    public static Action selectToolAction() {
        return ProjectSensitiveActions.projectCommandAction(COMMAND_SELECT_TOOL, Bundle.DN_SelectTool(), null);
    }
}
