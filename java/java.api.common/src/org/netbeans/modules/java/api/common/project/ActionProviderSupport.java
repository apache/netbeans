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

import java.awt.Dialog;
import java.awt.event.MouseEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.charset.IllegalCharsetNameException;
import java.nio.charset.UnsupportedCharsetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Properties;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.TreeSet;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.function.Supplier;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.swing.JButton;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import org.apache.tools.ant.module.api.support.ActionUtils;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NonNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.extexecution.startup.StartupExtender;
import org.netbeans.api.java.classpath.ClassPath;
import org.netbeans.api.java.classpath.JavaClassPathConstants;
import org.netbeans.api.java.platform.JavaPlatform;
import org.netbeans.api.java.platform.JavaPlatformManager;
import org.netbeans.api.java.project.JavaProjectConstants;
import org.netbeans.api.java.project.runner.JavaRunner;
import org.netbeans.api.java.queries.SourceLevelQuery;
import org.netbeans.api.java.queries.UnitTestForSourceQuery;
import org.netbeans.api.java.source.ClasspathInfo;
import org.netbeans.api.java.source.CompilationController;
import org.netbeans.api.java.source.JavaSource;
import org.netbeans.api.java.source.ui.ScanDialog;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.api.project.ProjectManager;
import org.netbeans.api.project.ProjectUtils;
import org.netbeans.api.project.SourceGroup;
import org.netbeans.api.project.Sources;
import org.netbeans.modules.java.api.common.SourceRoots;
import org.netbeans.modules.java.api.common.ant.UpdateHelper;
import static org.netbeans.modules.java.api.common.project.Bundle.*;
import org.netbeans.modules.java.api.common.project.JavaActionProvider.Context;
import org.netbeans.modules.java.api.common.project.JavaActionProvider.CompileOnSaveOperation;
import org.netbeans.modules.java.api.common.project.JavaActionProvider.ScriptAction.Result;
import org.netbeans.modules.java.api.common.project.ui.customizer.MainClassChooser;
import org.netbeans.modules.java.api.common.project.ui.customizer.MainClassWarning;
import org.netbeans.modules.java.api.common.util.CommonModuleUtils;
import org.netbeans.modules.java.api.common.util.CommonProjectUtils;
import org.netbeans.spi.java.classpath.ClassPathProvider;
import org.netbeans.spi.java.classpath.support.ClassPathSupport;
import org.netbeans.spi.java.project.support.ProjectPlatform;
import org.netbeans.spi.project.ActionProgress;
import org.netbeans.spi.project.ActionProvider;
import static org.netbeans.spi.project.ActionProvider.*;
import org.netbeans.spi.project.support.ant.AntProjectHelper;
import org.netbeans.spi.project.support.ant.EditableProperties;
import org.netbeans.spi.project.support.ant.PropertyEvaluator;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.awt.MouseUtils;
import org.openide.execution.ExecutorTask;
import org.openide.filesystems.FileChangeAdapter;
import org.openide.filesystems.FileChangeListener;
import org.openide.filesystems.FileEvent;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileStateInvalidException;
import org.openide.filesystems.FileSystem;
import org.openide.filesystems.FileUtil;
import org.openide.loaders.DataObject;
import org.openide.modules.SpecificationVersion;
import org.openide.util.Exceptions;
import org.openide.util.Lookup;
import org.openide.util.NbBundle;
import org.openide.util.Parameters;
import org.openide.util.Task;
import org.openide.util.WeakListeners;
import org.openide.util.lookup.Lookups;

/**
 * Support methods for {@link ActionProvider} implementation.
 * @author Tomas Zezula
 */
final class ActionProviderSupport {
    private static final Logger LOG = Logger.getLogger(ActionProviderSupport.class.getName());
    private static final String PROP_JAVA_MAIN_ACTION = "java.main.action"; //NOI18N
    private static final Pattern SRCDIRJAVA = Pattern.compile("\\.java$"); // NOI18N
    private static final String SUBST = "Test.java"; // NOI18N
    private static final String SUBSTNG = "NGTest.java"; // NOI18N
    private static final Set<String> NO_SYNC_COMMANDS = Collections.unmodifiableSet(new HashSet<String>(
            Arrays.asList(new String[]{
                COMMAND_BUILD,
                COMMAND_CLEAN,
                COMMAND_REBUILD,
                COMMAND_COMPILE_SINGLE,
                JavaProjectConstants.COMMAND_JAVADOC
            })));

    private ActionProviderSupport() {
        throw new IllegalStateException("No instance allowed.");    //NOI18N
    }

    static enum ActionFlag {
        PLATFORM_SENSITIVE,
        JAVA_MODEL_SENSITIVE,
        SCAN_SENSITIVE,
        COS_ENABLED
    }

    static enum UserPropertiesPolicy {
        RUN_ANYWAY(NbBundle.getMessage(ActionProviderSupport.class, "OPTION_Run_Anyway")),
        RUN_WITH(NbBundle.getMessage(ActionProviderSupport.class, "OPTION_Run_With")),
        RUN_UPDATE(NbBundle.getMessage(ActionProviderSupport.class, "OPTION_Run_Update"));

        private final String displayName;

        private UserPropertiesPolicy(@NonNull final String displayName) {
            this.displayName = displayName;
        }

        @NonNull
        public String getDisplayName() {
            return this.displayName;
        }

        @Override
        public String toString() {
            return getDisplayName();
        }
    }

    static enum JavaMainAction {
        RUN("run"),     //NOI18N
        TEST("test");   //NOI18N

        private final String name;
        JavaMainAction(@NonNull final String name) {
            assert name != null;
            this.name = name;
        }
        @CheckForNull
        static JavaMainAction forName(@NullAllowed final String name) {
            if (RUN.name.equals(name)) {
                return RUN;
            } else if (TEST.name.equals(name)) {
                return TEST;
            }
            return null;
        }
    }

    static void invokeTarget(
            @NonNull final JavaActionProvider.ScriptAction scriptAction,
            @NonNull final Context ctx) {
        final String userPropertiesFile = verifyUserPropertiesFile(ctx);
        final JavaModelWork action = new JavaModelWork(scriptAction, ctx, userPropertiesFile);
        final Set<ActionFlag> flags = scriptAction.getActionFlags();
        if (flags.contains(ActionFlag.JAVA_MODEL_SENSITIVE) ||
                (ctx.getCompileOnSaveOperations().contains(CompileOnSaveOperation.UPDATE) && flags.contains(ActionFlag.SCAN_SENSITIVE))) {
            //Always have to run with java model
            ScanDialog.runWhenScanFinished(action, scriptAction.getDisplayName());
        } else if (flags.contains(ActionFlag.SCAN_SENSITIVE)) {
            //Run without model if not yet ready
            try {
                action.needsJavaModel = false;
                invokeByJavaSource(action);
                if (!action.isCalled()) {
                    action.doJavaChecks = false;
                    action.run();
                }
            } catch (IOException ex) {
                Exceptions.printStackTrace(ex);
            }
        } else {
            //Does not need java model
            action.run();
        }
    }

    static boolean allowAntBuild(
            @NonNull final PropertyEvaluator evaluator,
            @NonNull final UpdateHelper updateHelper) {
        String buildClasses = evaluator.getProperty(ProjectProperties.BUILD_CLASSES_DIR);
        if (buildClasses == null) return false;
        File buildClassesFile = updateHelper.getAntProjectHelper().resolveFile(buildClasses);
        return !new File(buildClassesFile, BaseActionProvider.AUTOMATIC_BUILD_TAG).exists();
    }

    @CheckForNull
    static JavaPlatform getActivePlatform(
            @NonNull final Project prj,
            @NonNull final PropertyEvaluator eval,
            @NonNull final String activePlatformProperty) {
        JavaPlatform plat = CommonProjectUtils.getActivePlatform(eval.getProperty(activePlatformProperty));
        if (plat == null) {
            plat = ProjectPlatform.forProject(prj, eval, CommonProjectUtils.J2SE_PLATFORM_TYPE);
        }
        return plat;
    }


    static @NonNull final Supplier<? extends String[]> createConditionalTarget(
            @NonNull final PropertyEvaluator eval,
            @NonNull final Predicate<PropertyEvaluator> predicate,
            @NonNull final String[] ifTargets,
            @NonNull final String[] elseTargets) {
        return () -> {
            return predicate.test(eval) ?
                    ifTargets:
                    elseTargets;
        };
    }

    @NonNull
    static Predicate<PropertyEvaluator> createJarEnabledPredicate() {
        return (evaluator) -> !"false".equalsIgnoreCase(evaluator.getProperty(ProjectProperties.DO_JAR));    //NOI18N
    }

    /**
     * Loads targets for specific commands from shared config property file.
     * @returns map; key=command name; value=array of targets for given command
     */
    @NonNull
    static Map<String,String[]> loadTargetsFromConfig(
            @NonNull final Project project,
            @NonNull final PropertyEvaluator evaluator) {
        final Map<String,String[]> targets = new HashMap<>(6);
        final String config = evaluator.getProperty(ProjectProperties.PROP_PROJECT_CONFIGURATION_CONFIG);
        // load targets from shared config
        FileObject propFO = project.getProjectDirectory().getFileObject("nbproject/configs/" + config + ".properties");
        if (propFO == null) {
            return targets;
        }
        final Properties props = new Properties();
        try (InputStream is = propFO.getInputStream()) {
                props.load(is);
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
            return targets;
        }
        for (Map.Entry<Object,Object> e : props.entrySet()) {
            final String propName = (String) e.getKey();
            if (propName.startsWith("$target.")) {
                final String tNameVal = (String) e.getValue();
                if (tNameVal != null && !tNameVal.isEmpty()) {
                    final String cmdNameKey = propName.substring("$target.".length());
                    final StringTokenizer stok = new StringTokenizer(tNameVal.trim(), " ");
                    List<String> targetNames = new ArrayList<>(3);
                    while (stok.hasMoreTokens()) {
                        targetNames.add(stok.nextToken());
                    }
                    targets.put(cmdNameKey, targetNames.toArray(new String[0]));
                }
            }
        }
        return targets;
    }

    @CheckForNull
    static JavaMainAction getJavaMainAction(@NonNull final PropertyEvaluator evaluator) {
        return JavaMainAction.forName(evaluator.getProperty(PROP_JAVA_MAIN_ACTION));
    }

    @CheckForNull
    static String getProjectMainClass(
            @NonNull final Project project,
            @NonNull final PropertyEvaluator evaluator,
            @NonNull final SourceRoots projectSourceRoots,
            @NonNull final Function<String,ClassPath> classpaths,
            final boolean verify) {
        final String mainClass = evaluator.getProperty(ProjectProperties.MAIN_CLASS);
        // support for unit testing
        if (MainClassChooser.unitTestingSupport_hasMainMethodResult != null) {
            return MainClassChooser.unitTestingSupport_hasMainMethodResult ?
                mainClass :
                null;
        }
        if (mainClass == null || mainClass.length () == 0) {
            LOG.fine("Main class is not set");    //NOI18N
            return null;
        }
        if (!verify) {
            return mainClass;
        }
        final FileObject[] sourcesRoots = projectSourceRoots.getRoots();
        if (sourcesRoots.length > 0) {
            LOG.log(Level.FINE, "Searching main class {0} for root: {1}",   //NOI18N
                    new Object[] {
                        mainClass,
                        FileUtil.getFileDisplayName(sourcesRoots[0])
            });
            ClassPath bootPath = null, compilePath = null;
            try {
                bootPath = ClassPath.getClassPath (sourcesRoots[0], ClassPath.BOOT);        //Single compilation unit
                assert bootPath != null : assertPath (
                        sourcesRoots[0],
                        sourcesRoots,
                        project,
                        projectSourceRoots,
                        ClassPath.BOOT);
            } catch (AssertionError e) {
                //Log the assertion when -ea
                Exceptions.printStackTrace(e);
            }
            try {
                compilePath = ClassPath.getClassPath (sourcesRoots[0], ClassPath.EXECUTE);
                assert compilePath != null : assertPath (
                        sourcesRoots[0],
                        sourcesRoots,
                        project,
                        projectSourceRoots,
                        ClassPath.EXECUTE);
            } catch (AssertionError e) {
                //Log the assertion when -ea
                Exceptions.printStackTrace(e);
            }
            //todo: The J2SEActionProvider does not require the sourceRoots, it can take the classpath
            //from ClassPathProvider everytime. But the assertions above are important, it seems that
            //the SimpleFileOwnerQueryImplementation is broken in some cases. When assertions are enabled
            //log the data.
            if (bootPath == null) {
                LOG.fine("Source root has no boot classpath, using project boot classpath.");   //NOI18N
                bootPath = classpaths.apply(ClassPath.BOOT);
            }
            if (compilePath == null) {
                LOG.fine("Source root has no execute classpath, using project execute classpath.");   //NOI18N
                compilePath = classpaths.apply(ClassPath.EXECUTE);
            }

            ClassPath sourcePath = ClassPath.getClassPath(sourcesRoots[0], ClassPath.SOURCE);
            LOG.log(Level.FINE, "Classpaths used to resolve main boot: {0}, exec: {1}, src: {2}",   //NOI18N
                    new Object[]{
                        bootPath,
                        compilePath,
                        sourcePath
            });
            if (CommonProjectUtils.isMainClass (mainClass, bootPath, compilePath, sourcePath)) {
                return mainClass;
            }
        } else {
            LOG.log(Level.FINE, "Searching main class {0} without source root", mainClass);  //NOI18N
            ClassPath bootPath = classpaths.apply(ClassPath.BOOT);
            ClassPath compilePath = classpaths.apply(ClassPath.EXECUTE);
            ClassPath sourcePath = classpaths.apply(ClassPath.SOURCE);   //Empty ClassPath
            LOG.log(Level.FINE, "Classpaths used to resolve main boot: {0}, exec: {1}, src: {2}",   //NOI18N
                    new Object[]{
                        bootPath,
                        compilePath,
                        sourcePath
            });
            if (CommonProjectUtils.isMainClass (mainClass, bootPath, compilePath, sourcePath)) {
                return mainClass;
            }
        }
        LOG.log(Level.FINE, "Main class {0} is invalid.", mainClass);   //NOI18N
        return null;
    }

    @NbBundle.Messages({
        "LBL_MainClassWarning_ChooseMainClass_OK=OK",
        "AD_MainClassWarning_ChooseMainClass_OK=N/A",
        "# {0} - project name", "LBL_MainClassNotFound=Project {0} does not have a main class set.",
        "# {0} - name of class", "# {1} - project name", "LBL_MainClassWrong={0} class wasn''t found in {1} project.",
        "CTL_MainClassWarning_Title=Run Project"
        })
    static boolean showCustomizer(
            @NonNull final Project project,
            @NonNull final UpdateHelper updateHelper,
            @NonNull final PropertyEvaluator evaluator,
            @NonNull final SourceRoots projectSourceRoots,
            @NonNull final Function<String,ClassPath> classpaths) {
        boolean result = false;
        final JButton okButton = new JButton(LBL_MainClassWarning_ChooseMainClass_OK());
        okButton.getAccessibleContext().setAccessibleDescription(AD_MainClassWarning_ChooseMainClass_OK());
        // main class goes wrong => warning
        String mainClass = getProjectMainClass(project, evaluator, projectSourceRoots, classpaths, false);
        String message;
        if (mainClass == null) {
            message = LBL_MainClassNotFound(ProjectUtils.getInformation(project).getDisplayName());
        } else {
            message = LBL_MainClassWrong(
                mainClass,
                ProjectUtils.getInformation(project).getDisplayName());
        }
        final MainClassWarning panel = new MainClassWarning (message, projectSourceRoots.getRoots());
        Object[] options = new Object[] {
            okButton,
            DialogDescriptor.CANCEL_OPTION
        };
        panel.addChangeListener (new ChangeListener () {
            @Override
           public void stateChanged (ChangeEvent e) {
               if (e.getSource () instanceof MouseEvent && MouseUtils.isDoubleClick (((MouseEvent)e.getSource ()))) {
                   // click button and the finish dialog with selected class
                   okButton.doClick ();
               } else {
                   okButton.setEnabled (panel.getSelectedMainClass () != null);
               }
           }
        });
        okButton.setEnabled (false);
        DialogDescriptor desc = new DialogDescriptor (panel,
            CTL_MainClassWarning_Title(),
            true, options, options[0], DialogDescriptor.BOTTOM_ALIGN, null, null);
        desc.setMessageType (DialogDescriptor.INFORMATION_MESSAGE);
        Dialog dlg = DialogDisplayer.getDefault ().createDialog (desc);
        dlg.setVisible (true);
        if (desc.getValue() == options[0]) {
            mainClass = panel.getSelectedMainClass ();
            String config = evaluator.getProperty(ProjectProperties.PROP_PROJECT_CONFIGURATION_CONFIG);
            String path;
            if (config == null || config.length() == 0) {
                path = AntProjectHelper.PROJECT_PROPERTIES_PATH;
            } else {
                // Set main class for a particular config only.
                path = "nbproject/configs/" + config + ".properties"; // NOI18N
            }
            final EditableProperties ep = updateHelper.getProperties(path);
            ep.put(ProjectProperties.MAIN_CLASS, mainClass == null ? "" : mainClass);
            try {
                if (updateHelper.requestUpdate()) {
                    updateHelper.putProperties(path, ep);
                    ProjectManager.getDefault().saveProject(project);
                    result = true;
                }
            } catch (IOException ioe) {
                Exceptions.printStackTrace(ioe);
            }
        }
        dlg.dispose();
        return result;
    }

    @NonNull
    static Map<String,Object> createBaseCoSProperties(
            @NonNull final JavaActionProvider.Context ctx) {
        final String command = ctx.getCommand();
        final Project project = ctx.getProject();
        final UpdateHelper updateHelper = ctx.getUpdateHelper();
        final PropertyEvaluator evaluator = ctx.getPropertyEvaluator();
        final JavaPlatform jp = ctx.getActiveJavaPlatform();
        final Map<String, Object> execProperties = new HashMap<>();
        execProperties.put("nb.internal.action.name", command);
        copyMultiValue(evaluator, execProperties, ProjectProperties.RUN_JVM_ARGS);
        prepareWorkDir(updateHelper, evaluator, execProperties);
        execProperties.put(JavaRunner.PROP_PLATFORM, jp);
        execProperties.put(JavaRunner.PROP_PROJECT_NAME, ProjectUtils.getInformation(project).getDisplayName());
        String runtimeEnc = evaluator.getProperty(ProjectProperties.RUNTIME_ENCODING);
        if (runtimeEnc != null) {
            try {
                Charset runtimeChs = Charset.forName(runtimeEnc);
                execProperties.put(JavaRunner.PROP_RUNTIME_ENCODING, runtimeChs); //NOI18N
            } catch (IllegalCharsetNameException ichsn) {
                LOG.log(Level.WARNING, "Illegal charset name: {0}", runtimeEnc); //NOI18N
            } catch (UnsupportedCharsetException uchs) {
                LOG.log(Level.WARNING, "Unsupported charset : {0}", runtimeEnc); //NOI18N
            }
        }
        Optional.ofNullable(evaluator.getProperty("java.failonerror"))  //NOI18N
                .map((val) -> Boolean.valueOf(val))
                .ifPresent((b) -> execProperties.put("java.failonerror", b));    //NOI18N
        return execProperties;
    }

    @CheckForNull
    static Set<String> prepareSystemProperties(
            @NonNull final Context context,
            @NonNull final Map<String, Object> properties,
            final boolean test) {
        String prefix = test ? ProjectProperties.SYSTEM_PROPERTIES_TEST_PREFIX : ProjectProperties.SYSTEM_PROPERTIES_RUN_PREFIX;
        Map<String, String> evaluated = context.getPropertyEvaluator().getProperties();
        if (evaluated == null) {
            return null;
        }
        for (Map.Entry<String, String> e : evaluated.entrySet()) {
            if (e.getKey().startsWith(prefix) && e.getValue() != null) {
                putMultiValue(properties, JavaRunner.PROP_RUN_JVMARGS, "-D" + e.getKey().substring(prefix.length()) + "=" + e.getValue());
            }
        }
        collectStartupExtenderArgs(context, (k,v) -> {
            properties.put(k, v);
            return null;
        });
        return context.copyAdditionalProperties(properties);
    }

    static void bypassAntBuildScript(
            @NonNull final JavaActionProvider.Context ctx,
            @NonNull final Map<String, Object> p,
            @NonNull final FileObject[] sourceRoots,
            @NonNull final FileObject[] testRoots,
            @NonNull final AtomicReference<ExecutorTask> task,
            @NullAllowed final BiFunction <Context,Map<String,Object>,Boolean> cosExecuteInterceptor) throws IllegalArgumentException {
        final Project project = ctx.getProject();
        final PropertyEvaluator evaluator = ctx.getPropertyEvaluator();
        final String command = ctx.getCommand();
        final Lookup context = ctx.getActiveLookup();
        final ActionProviderSupport.JavaMainAction javaMainAction = ActionProviderSupport.getJavaMainAction(evaluator);
        boolean run = javaMainAction != ActionProviderSupport.JavaMainAction.TEST;
        boolean hasMainMethod = run;

        if (COMMAND_RUN.equals(command) || COMMAND_DEBUG.equals(command) || COMMAND_DEBUG_STEP_INTO.equals(command) || COMMAND_PROFILE.equals(command)) {
            final String mainClass = evaluator.getProperty(ProjectProperties.MAIN_CLASS);

            p.put(JavaRunner.PROP_CLASSNAME, mainClass);
            if (modulesSupported(project)) {
                p.put(JavaRunner.PROP_EXECUTE_CLASSPATH, ctx.getProjectClassPath(JavaClassPathConstants.MODULE_EXECUTE_CLASS_PATH));
                p.put(JavaRunner.PROP_EXECUTE_MODULEPATH, ctx.getProjectClassPath(JavaClassPathConstants.MODULE_EXECUTE_PATH));
            } else {
                p.put(JavaRunner.PROP_EXECUTE_CLASSPATH, ctx.getProjectClassPath(ClassPath.EXECUTE));
            }
            if (COMMAND_DEBUG_STEP_INTO.equals(command)) {
                p.put("stopclassname", mainClass);
            }
        } else {
            //run single:
            FileObject[] files = findSources(sourceRoots, context);
            if (files == null || files.length != 1) {
                files = findTestSources(sourceRoots, testRoots, context, false);
                if (files != null && files.length == 1) {
                    hasMainMethod = CommonProjectUtils.hasMainMethod(files[0]);
                    run = false;
                }
            } else if (!hasMainMethod) {
                hasMainMethod = CommonProjectUtils.hasMainMethod(files[0]);
            }
            if (files == null || files.length != 1) {
                return ;//warn the user
            }
            p.put(JavaRunner.PROP_EXECUTE_FILE, files[0]);
        }
        boolean debug = COMMAND_DEBUG.equals(command) || COMMAND_DEBUG_SINGLE.equals(command) || COMMAND_DEBUG_STEP_INTO.equals(command);
        boolean profile = COMMAND_PROFILE.equals(command) || COMMAND_PROFILE_SINGLE.equals(command);
        try {
            boolean vote = true;
            if (cosExecuteInterceptor != null) {
                vote = cosExecuteInterceptor.apply(ctx, p);
            }
            if (vote) {
                if (run) {
                    ActionProviderSupport.copyMultiValue(evaluator, p, ProjectProperties.APPLICATION_ARGS);
                    task.set(JavaRunner.execute(debug ? JavaRunner.QUICK_DEBUG : (profile ? JavaRunner.QUICK_PROFILE : JavaRunner.QUICK_RUN), p));
                } else {
                    if (hasMainMethod) {
                        task.set(JavaRunner.execute(debug ? JavaRunner.QUICK_DEBUG : (profile ? JavaRunner.QUICK_PROFILE : JavaRunner.QUICK_RUN), p));
                    } else {
                        task.set(JavaRunner.execute(debug ? JavaRunner.QUICK_TEST_DEBUG : (profile ? JavaRunner.QUICK_TEST_PROFILE : JavaRunner.QUICK_TEST), p));
                    }
                }
            }
        } catch (IOException ex) {
            Exceptions.printStackTrace(ex);
        }
    }

    @NbBundle.Messages({
        "CTL_BrokenPlatform_Close=Close",
        "AD_BrokenPlatform_Close=N/A",
        "# {0} - project name", "TEXT_BrokenPlatform=<html><p><strong>The project {0} has a broken platform reference.</strong></p><br><p> You have to fix the broken reference and invoke the action again.</p>",
        "MSG_BrokenPlatform_Title=Broken Platform Reference"
    })
    static void showPlatformWarning (@NonNull final Project project) {
        final JButton closeOption = new JButton(CTL_BrokenPlatform_Close());
        closeOption.getAccessibleContext().setAccessibleDescription(AD_BrokenPlatform_Close());
        final String projectDisplayName = ProjectUtils.getInformation(project).getDisplayName();
        final DialogDescriptor dd = new DialogDescriptor(
            TEXT_BrokenPlatform(projectDisplayName),
            MSG_BrokenPlatform_Title(),
            true,
            new Object[] {closeOption},
            closeOption,
            DialogDescriptor.DEFAULT_ALIGN,
            null,
            null);
        dd.setMessageType(DialogDescriptor.WARNING_MESSAGE);
        final Dialog dlg = DialogDisplayer.getDefault().createDialog(dd);
        dlg.setVisible(true);
    }

    @CheckForNull
    static FileObject getRoot (@NonNull final FileObject[] roots, @NonNull final FileObject file) {
        assert file != null : "File can't be null";   //NOI18N
        FileObject srcDir = null;
        for (int i=0; i< roots.length; i++) {
            assert roots[i] != null : "Source Path Root can't be null"; //NOI18N
            if (FileUtil.isParentOf(roots[i],file) || roots[i].equals(file)) {
                srcDir = roots[i];
                break;
            }
        }
        return srcDir;
    }

    @CheckForNull
    @org.netbeans.api.annotations.common.SuppressWarnings("PZLA_PREFER_ZERO_LENGTH_ARRAYS")
    static FileObject[] findSourcesAndPackages (Lookup context, FileObject[] srcRoots) {
        for (int i=0; i<srcRoots.length; i++) {
            FileObject[] result = findSourcesAndPackages(context, srcRoots[i]);
            if (result != null) {
                return result;
            }
        }
        return null;
    }

    /** Find selected sources, the sources has to be under single source root,
     *  @param context the lookup in which files should be found
     */
    @CheckForNull
    @org.netbeans.api.annotations.common.SuppressWarnings("PZLA_PREFER_ZERO_LENGTH_ARRAYS")
    static FileObject[] findSources(@NonNull final FileObject[] sourceRoots, @NonNull final Lookup context) {
        return findSources(sourceRoots, context, true, false);
    }

    /** Find either selected tests or tests which belong to selected source files
     */
    @CheckForNull
    @org.netbeans.api.annotations.common.SuppressWarnings("PZLA_PREFER_ZERO_LENGTH_ARRAYS")
    static FileObject[] findTestSources(
            @NonNull final FileObject[] sourceRoots,
            @NonNull final FileObject[] testRoots,
            @NonNull final Lookup context,
            final boolean checkInSrcDir) {
        return findTestSources(sourceRoots, testRoots, context, checkInSrcDir, true, false);
    }

    /**
     * Find selected tests and tests which belong to selected source files
     * when package(s) or multiple files are selected.
     *
     * @param context the lookup in which files should be found
     */
    @org.netbeans.api.annotations.common.SuppressWarnings("PZLA_PREFER_ZERO_LENGTH_ARRAYS")
    @CheckForNull
    static FileObject[] findTestSourcesForFiles(
            @NonNull final FileObject[] sourceRoots,
            @NonNull final FileObject[] testRoots,
            @NonNull final Lookup context) {
        FileObject[] sourcesFOs = findSources(sourceRoots, context, false, true);
        FileObject[] testSourcesFOs = findTestSources(sourceRoots, testRoots, context, false, false, true);
        HashSet<FileObject> testFiles = new HashSet<>();
        if(testSourcesFOs == null) { // no test files were selected
            return findTestSources(sourceRoots, testRoots, context, true, false, true); // return tests which belong to selected source files, if any
        } else {
            if(sourcesFOs == null) { // only test files were selected
                return testSourcesFOs;
            } else { // both test and source files were selected, do not return any dublicates
                testFiles.addAll(Arrays.asList(testSourcesFOs));
                //Try to find the test under the test roots
                FileObject srcRoot = getRoot(sourceRoots,sourcesFOs[0]);
                for (FileObject testRoot : testRoots) {
                    FileObject[] files2 = ActionUtils.regexpMapFiles(sourcesFOs, srcRoot, SRCDIRJAVA, testRoot, SUBST, true);
                    if (files2 != null) {
                        for (FileObject fo : files2) {
                            if(!testFiles.contains(fo)) {
                                testFiles.add(fo);
                            }
                        }
                    }
                    FileObject[] files2NG = ActionUtils.regexpMapFiles(sourcesFOs, srcRoot, SRCDIRJAVA, testRoot, SUBSTNG, true);
                    if (files2NG != null) {
                        for (FileObject fo : files2NG) {
                            if(!testFiles.contains(fo)) {
                                testFiles.add(fo);
                            }
                        }
                    }
                }
            }
        }
        return testFiles.isEmpty() ? null : testFiles.toArray(new FileObject[0]);
    }

    static String[] setupTestSingle(
            @NonNull final Context context,
            @NonNull final FileObject[] files,
            @NonNull final FileObject[] srcPath) {
        final FileObject root = getRoot(srcPath, files[0]);
        // Convert foo/FooTest.java -> foo.FooTest
        final String path = FileUtil.getRelativePath(root, files[0]);
        context.setProperty("test.class", path.substring(0, path.length() - 5).replace('/', '.')); // NOI18N
        context.setProperty("test.includes", ActionUtils.antIncludesList(files, root)); // NOI18N
        context.setProperty("javac.includes", ActionUtils.antIncludesList(files, root)); // NOI18N
        switch (context.getCommand()) {
            case COMMAND_RUN_SINGLE:
            case COMMAND_TEST_SINGLE:
                return new String[] {"test-single"}; // NOI18N
            case COMMAND_DEBUG_SINGLE:
            case COMMAND_DEBUG_TEST_SINGLE:
                return new String[] {"debug-test"}; // NOI18N
            case COMMAND_PROFILE_SINGLE:
            case COMMAND_PROFILE_TEST_SINGLE:
                return new String[] {"profile-test"}; // NOI18N
            default:
                throw new IllegalArgumentException(context.getCommand());
        }
    }

    @CheckForNull
    static String[] pathAndFqn (
            @NonNull final FileObject file,
            @NonNull final FileObject[] roots) {
        String path = FileUtil.getRelativePath(getRoot(roots, file), file);
        if (path == null) {
            return null;
        }
        String fqn = path.endsWith(".java") ? //NOI18N
            path.substring(0, path.length() - 5) :
            path;
        return new String[] {
            path,
            fqn.replace('/','.')    //NOI18N
        };
    }

    @CheckForNull
    @org.netbeans.api.annotations.common.SuppressWarnings("PZLA_PREFER_ZERO_LENGTH_ARRAYS")
    private static  FileObject[] findSourcesAndPackages (Lookup context, FileObject srcDir) {
        if (srcDir != null) {
            FileObject[] files = ActionUtils.findSelectedFiles(context, srcDir, null, true); // NOI18N
            //Check if files are either packages of java files
            if (files != null) {
                for (int i = 0; i < files.length; i++) {
                    if (!files[i].isFolder() && !"java".equals(files[i].getExt())) {
                        return null;
                    }
                }
            }
            return files;
        } else {
            return null;
        }
    }

    /**
     * Find selected tests and/or tests which belong to selected source files
     *
     * @param context the lookup in which files should be found
     * @param checkInSrcDir if true, tests which belong to selected source files will be searched for
     * @param strict if true, all files in the selection have to be accepted
     * @param findInPackages if true, all files under a selected package in the selection will also be checked
     */
    @CheckForNull
    @org.netbeans.api.annotations.common.SuppressWarnings("PZLA_PREFER_ZERO_LENGTH_ARRAYS")
    private static FileObject[] findTestSources(
            @NonNull final FileObject[] sourceRoots,
            @NonNull final FileObject[] testRoots,
            @NonNull final Lookup context,
            final boolean checkInSrcDir,
            final boolean strict,
            final boolean findInPackages) {
        //XXX: Ugly, should be rewritten
        for (FileObject testSrcPath : testRoots) {
            FileObject[] files = ActionUtils.findSelectedFiles(context, testSrcPath, findInPackages ? null : ".java", strict); // NOI18N
            ArrayList<FileObject> testFOs = new ArrayList<>();
            if (files != null) {
                for (FileObject file : files) {
                    if ((file.hasExt("java") || findInPackages && file.isFolder())) {
                        testFOs.add(file);
                    }
                }
                return testFOs.isEmpty() ?
                    null:
                    testFOs.toArray(new FileObject[0]);
            }
        }
        if (checkInSrcDir && testRoots.length > 0) {
            FileObject[] files = findSources(sourceRoots, context, strict, findInPackages);
            if (files != null) {
                //Try to find the test under the test roots
                FileObject srcRoot = getRoot(sourceRoots, files[0]);
                for (FileObject testSrcPath : testRoots) {
                    FileObject[] files2 = ActionUtils.regexpMapFiles(files, srcRoot, SRCDIRJAVA, testSrcPath, SUBST, strict);
                    if (files2 != null && files2.length != 0) {
                        return files2;
                    }
                    FileObject[] files2NG = ActionUtils.regexpMapFiles(files, srcRoot, SRCDIRJAVA, testSrcPath, SUBSTNG, strict);
                    if (files2NG != null && files2NG.length != 0) {
                        return files2NG;
                    }
                }
                // no test files found. The selected FOs might be folders under source packages
                files = ActionUtils.findSelectedFiles(context, srcRoot, findInPackages ? null : ".java", strict); // NOI18N
                ArrayList<FileObject> testFOs = new ArrayList<>();
                if (files != null) {
                    for (FileObject file : files) {
                        if (findInPackages && file.isFolder()) {
                            String relativePath = FileUtil.getRelativePath(srcRoot, file);
                            if (relativePath != null) {
                                for (FileObject testSrcPath : testRoots) {
                                    FileObject testFO = FileUtil.toFileObject(new File(FileUtil.toFile(testSrcPath).getPath().concat(File.separator).concat(relativePath)));
                                    if (testFO != null) {
                                        testFOs.add(testFO);
                                    }
                                }
                            }
                        }
                    }
                    return testFOs.isEmpty() ?
                        null :
                        testFOs.toArray(new FileObject[0]);
                }
            }
        }
        return null;
    }

    /**
     * Find selected source files
     *
     * @param context the lookup in which files should be found
     * @param strict if true, all files in the selection have to be accepted
     * @param findInPackages if true, all files under a selected package in the selection will also be checked
     */
    @CheckForNull
    @org.netbeans.api.annotations.common.SuppressWarnings("PZLA_PREFER_ZERO_LENGTH_ARRAYS")
    private static FileObject[] findSources(
            @NonNull final FileObject[] sourceRoots,
            @NonNull final Lookup context,
            final boolean strict,
            final boolean findInPackages) {
        for (int i=0; i< sourceRoots.length; i++) {
            FileObject[] files = ActionUtils.findSelectedFiles(context, sourceRoots[i], findInPackages ? null : ".java", strict); // NOI18N
            if (files != null) {
                return files;
            }
        }
        return null;
    }

    @CheckForNull
    private static FileObject getBuildScript(@NonNull final Context context) {
        final String path =  CommonProjectUtils.getBuildXmlName(context.getPropertyEvaluator(), null);
        return context.getProject().getProjectDirectory().getFileObject(path);
    }

    private static void copyMultiValue(
            @NonNull final PropertyEvaluator evaluator,
            @NonNull final Map<String, Object> properties,
            @NonNull final String propertyName) {
        String val = evaluator.getProperty(propertyName);
        if (val != null) {

            putMultiValue(properties,propertyName, val);
        }
    }

    private static boolean modulesSupported(@NonNull final Project project) {
        return Optional.ofNullable(SourceLevelQuery.getSourceLevel2(project.getProjectDirectory()).getSourceLevel())
                .map((s) -> new SpecificationVersion(s))
                .map((sv) -> CommonModuleUtils.JDK9.compareTo(sv) <= 0)
                .orElse(Boolean.FALSE);
    }

    private static void putMultiValue(
            @NonNull final Map<String, Object> properties,
            @NonNull final String propertyName,
            @NonNull final String val) {
        @SuppressWarnings(value = "unchecked")
        Collection<String> it = (Collection<String>) properties.get(propertyName);
        if (it == null) {
            properties.put(propertyName, it = new LinkedList<>());
        }
        it.add(val);
    }

    private static void invokeByJavaSource (
            @NonNull final Runnable runnable) throws IOException {
        Parameters.notNull("runnable", runnable);   //NOI18N
        final ClasspathInfo info = ClasspathInfo.create(JavaPlatform.getDefault().getBootstrapLibraries(),
            ClassPathSupport.createClassPath(new URL[0]),
            ClassPathSupport.createClassPath(new URL[0]));
        final JavaSource js = JavaSource.create(info);
        if (js != null) {
            js.runWhenScanFinished((final CompilationController controller) -> {
                runnable.run();
            }, true);
        }
    }

    @CheckForNull
    private static String verifyUserPropertiesFile(
            @NonNull final Context ctx) {
        final String currentPath = ctx.getPropertyEvaluator().getProperty("user.properties.file");      //NOI18N
        final File current = currentPath == null ? null : FileUtil.normalizeFile(ctx.getUpdateHelper().getAntProjectHelper().resolveFile(currentPath));
        final File expected = FileUtil.normalizeFile(new File(System.getProperty("netbeans.user"), "build.properties")); // NOI18N
        if (!expected.equals(current)) {
            if (ctx.getUserPropertiesPolicy() == null) {
                final Object option = DialogDisplayer.getDefault().notify(new NotifyDescriptor(
                        NbBundle.getMessage(ActionProviderSupport.class, "MSG_InvalidBuildPropertiesPath", ProjectUtils.getInformation(ctx.getProject()).getDisplayName()),
                        NbBundle.getMessage(ActionProviderSupport.class, "TITLE_InvalidBuildPropertiesPath"),
                        0,
                        NotifyDescriptor.QUESTION_MESSAGE,
                        UserPropertiesPolicy.values(),
                        UserPropertiesPolicy.RUN_ANYWAY));
                ctx.setUserPropertiesPolicy(option instanceof UserPropertiesPolicy ?
                        (UserPropertiesPolicy) option :
                        null);
            }
            if (null != ctx.getUserPropertiesPolicy()) {
                switch (ctx.getUserPropertiesPolicy()) {
                    case RUN_ANYWAY:
                        return null;
                    case RUN_WITH:
                        return expected.getAbsolutePath();
                    case RUN_UPDATE:
                        ProjectManager.mutex().writeAccess(() -> {
                            final EditableProperties ep = ctx.getUpdateHelper().getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH);
                            ep.setProperty("user.properties.file", expected.getAbsolutePath()); //NOI18N
                            ctx.getUpdateHelper().putProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH, ep);
                            try {
                                ProjectManager.getDefault().saveProject(ctx.getProject());
                            } catch (IOException ioe) {
                                Exceptions.printStackTrace(ioe);
                            }
                        });
                        return null;
                    default:
                }
            }
        }
        return null;
    }

    private static void collectStartupExtenderArgs(
            @NonNull final Context ctx,
            @NonNull final BiFunction<String,String,Void> consummer) {
        StringBuilder b = new StringBuilder();
        for (String arg : runJvmargsIde(ctx)) {
            b.append(' ').append(arg);
        }
        if (b.length() > 0) {
            consummer.apply(ProjectProperties.RUN_JVM_ARGS_IDE, b.toString());
        }
    }

    private static List<String> runJvmargsIde(@NonNull final Context ctx) {
        StartupExtender.StartMode mode;
        switch (ctx.getCommand()) {
            case COMMAND_RUN:
            case COMMAND_RUN_SINGLE:
                mode = StartupExtender.StartMode.NORMAL;
                break;
            case COMMAND_DEBUG:
            case COMMAND_DEBUG_SINGLE:
            case COMMAND_DEBUG_STEP_INTO:
                mode = StartupExtender.StartMode.DEBUG;
                break;
            case COMMAND_PROFILE:
            case COMMAND_PROFILE_SINGLE:
                mode = StartupExtender.StartMode.PROFILE;
                break;
            case COMMAND_TEST:
            case COMMAND_TEST_SINGLE:
                mode = StartupExtender.StartMode.TEST_NORMAL;
                break;
            case COMMAND_DEBUG_TEST_SINGLE:
                mode = StartupExtender.StartMode.TEST_DEBUG;
                break;
            case COMMAND_PROFILE_TEST_SINGLE:
                mode = StartupExtender.StartMode.TEST_PROFILE;
                break;
            default:
                return Collections.emptyList();
        }
        final List<String> args = new ArrayList<>();
        final JavaPlatform p = ctx.getActiveJavaPlatform();
        for (StartupExtender group : StartupExtender.getExtenders(Lookups.fixed(
                ctx.getProject(),
                p != null ? p : JavaPlatformManager.getDefault().getDefaultPlatform()),
                mode)) {
            args.addAll(group.getArguments());
        }
        return args;
    }

    private static String assertPath (
            FileObject fileObject,
            FileObject[] expectedRoots,
            Project project,
            SourceRoots roots,
            String pathType) {
        final StringBuilder sb = new StringBuilder ();
        sb.append ("File: ").append (fileObject);                                                                       //NOI18N
        sb.append ("\nPath Type: ").append (pathType);                                                                  //NOI18N
        final Project owner = FileOwnerQuery.getOwner(fileObject);
        sb.append ("\nOwner: ").append (owner == null ? "" : ProjectUtils.getInformation(owner).getDisplayName());      //NOI18N
        sb.append ("\nClassPathProviders: ");                                                                           //NOI18N
        for (ClassPathProvider impl  : Lookup.getDefault ().lookupResult (ClassPathProvider.class).allInstances ())
            sb.append ("\n  ").append (impl);                                                                           //NOI18N
        sb.append ("\nProject SourceGroups:");                                                                          //NOI18N
        final SourceGroup[] sgs =  ProjectUtils.getSources(project).getSourceGroups(JavaProjectConstants.SOURCES_TYPE_JAVA);
        for (SourceGroup sg : sgs) {
            sb.append("\n  ").append(FileUtil.getFileDisplayName(sg.getRootFolder()));                                  //NOI18N
        }
        sb.append ("\nProject Source Roots(");                                                                          //NOI18N
        sb.append(System.identityHashCode(roots));
        sb.append("):");                                                                                                //NOI18N
        for (FileObject expectedRoot : expectedRoots) {
            sb.append("\n  ").append(FileUtil.getFileDisplayName(expectedRoot));                                        //NOI18N
        }
        return sb.toString ();
    }

    private static void prepareWorkDir(
            @NonNull final UpdateHelper updateHelper,
            @NonNull final PropertyEvaluator evaluator,
            @NonNull final Map<String, Object> properties) {
        String val = evaluator.getProperty(ProjectProperties.RUN_WORK_DIR);
        if (val == null) {
            val = ".";
        }
        final File file = updateHelper.getAntProjectHelper().resolveFile(val);
        properties.put(JavaRunner.PROP_WORK_DIR, file);
    }


    static final class ModifiedFilesSupport {
        private final Project project;
        private final UpdateHelper updateHelper;
        private final PropertyEvaluator evaluator;
        private final FileChangeListener modificationListener;
        private final PropertyChangeListener propListner;
        private volatile Boolean allowsFileTracking;
        /** Set of Java source files (as relative path from source root) known to have been modified. See issue #104508. */
        //@GuardedBy("this")
        private Set<String> dirty;
        //@GuardedBy("this")
        private Sources src;
        //@GuardedBy("this")
        private List<FileObject> roots;

        private ModifiedFilesSupport(
                @NonNull final Project project,
                @NonNull final UpdateHelper updateHelper,
                @NonNull final PropertyEvaluator evaluator) {
            this.project = project;
            this.updateHelper = updateHelper;
            this.evaluator = evaluator;
            this.modificationListener = new FileChangeAdapter() {
                @Override
                public void fileChanged(final FileEvent fe) {
                    modification(fe.getFile());
                }
                @Override
                public void fileDataCreated(final FileEvent fe) {
                    modification(fe.getFile());
                }
            };
            this.propListner = (e) -> {
                final String propName = e.getPropertyName();
                if (propName == null || ProjectProperties.TRACK_FILE_CHANGES.equals(propName)) {
                    synchronized(this) {
                        this.allowsFileTracking = null;
                        this.dirty = null;
                    }
                }
            };
            this.evaluator.addPropertyChangeListener(WeakListeners.propertyChange(this.propListner, this.evaluator));
        }

        void start() {
            try {
                final FileSystem fs = project.getProjectDirectory().getFileSystem();
                // XXX would be more efficient to only listen while TRACK_FILE_CHANGES is set,
                // but it needs adding and removing of listeners depending on PropertyEvaluator events,
                // the file event handling is cheap when TRACK_FILE_CHANGES is disabled.
                fs.addFileChangeListener(FileUtil.weakFileChangeListener(modificationListener, fs));
            } catch (FileStateInvalidException x) {
                Exceptions.printStackTrace(x);
            }
        }

        synchronized void resetDirtyList() {
            dirty = null;
        }

        @CheckForNull
        String prepareDirtyList(final boolean isExplicitBuildTarget) {
            String doDepend = evaluator.getProperty(ProjectProperties.DO_DEPEND);
            String buildClassesDirValue = evaluator.getProperty(ProjectProperties.BUILD_CLASSES_DIR);
            if (buildClassesDirValue == null) {
                //Log
                StringBuilder logRecord = new StringBuilder();
                logRecord.append("EVALUATOR: ").append(evaluator.getProperties()).append(";"); // NOI18N
                logRecord.append("PROJECT_PROPS: ").append(updateHelper.getProperties(AntProjectHelper.PROJECT_PROPERTIES_PATH).entrySet()).append(";"); // NOI18N
                logRecord.append("PRIVATE_PROPS: ").append(updateHelper.getProperties(AntProjectHelper.PRIVATE_PROPERTIES_PATH).entrySet()).append(";"); // NOI18N
                LOG.log(Level.WARNING, "No build.classes.dir property: {0}", logRecord.toString());
                return null;
            }
            File buildClassesDir = updateHelper.getAntProjectHelper().resolveFile(buildClassesDirValue);
            synchronized (this) {
                if (dirty == null) {
                    if (allowsFileChangesTracking()) {
                        // #119777: the first time, build everything.
                        dirty = new TreeSet<>();
                    }
                    return null;
                }
                for (DataObject d : DataObject.getRegistry().getModified()) {
                    // Treat files modified in memory as dirty as well.
                    // (If you make an edit and press F11, the save event happens *after* Ant is launched.)
                    modification(d.getPrimaryFile());
                }
                String res = null;
                boolean wasBuiltAutomatically = new File(buildClassesDir,BaseActionProvider.AUTOMATIC_BUILD_TAG).canRead(); //NOI18N
                if (!"true".equalsIgnoreCase(doDepend) && !(isExplicitBuildTarget && dirty.isEmpty()) && !wasBuiltAutomatically) { // NOI18N
                    // #104508: if not using <depend>, try to compile just those files known to have been touched since the last build.
                    // (In case there are none such, yet the user invoked build anyway, probably they know what they are doing.)
                    if (dirty.isEmpty()) {
                        // includes="" apparently is ignored.
                        dirty.add("nothing whatsoever"); // NOI18N
                    }
                    StringBuilder dirtyList = new StringBuilder();
                    for (String f : dirty) {
                        if (dirtyList.length() > 0) {
                            dirtyList.append(',');
                        }
                        dirtyList.append(f);
                    }
                    res = dirtyList.toString();
                }
                dirty.clear();
                return res;
            }
        }

        private void modification(FileObject f) {
            if (!allowsFileChangesTracking()) {
                return;
            }
            final Iterable <? extends FileObject> _roots = getRoots();
            assert _roots != null;
            for (FileObject root : _roots) {
                String path = FileUtil.getRelativePath(root, f);
                if (path != null) {
                    synchronized (this) {
                        if (dirty != null) {
                            dirty.add(path);
                        }
                    }
                    break;
                }
            }
        }

        private Iterable <? extends FileObject> getRoots () {
            Sources _src;
            synchronized (this) {
                if (this.roots != null) {
                    return this.roots;
                }
                if (this.src == null) {
                    this.src = ProjectUtils.getSources(this.project);
                    this.src.addChangeListener ((e) -> {
                        resetDirtyList();
                    });
                }
                _src = this.src;
            }
            assert _src != null;
            final SourceGroup[] sgs = _src.getSourceGroups (JavaProjectConstants.SOURCES_TYPE_JAVA);
            final List<FileObject> _roots = new ArrayList<>(sgs.length);
            for (SourceGroup sg : sgs) {
                final FileObject root = sg.getRootFolder();
                if (UnitTestForSourceQuery.findSources(root).length == 0) {
                    _roots.add (root);
                }
            }
            synchronized (this) {
                if (this.roots == null) {
                    this.roots = _roots;
                }
                return this.roots;
            }
        }

        private boolean allowsFileChangesTracking () {
            //allowsFileTracking is volatile primitive, fine to do double checking
            synchronized (this) {
                if (allowsFileTracking != null) {
                    return allowsFileTracking.booleanValue();
                }
            }
            final String val = evaluator.getProperty(ProjectProperties.TRACK_FILE_CHANGES);
            synchronized (this) {
                if (allowsFileTracking == null) {
                    allowsFileTracking = "true".equals(val) ? Boolean.TRUE : Boolean.FALSE;  //NOI18N
                }
                return allowsFileTracking.booleanValue();
            }
        }

        @NonNull
        static ModifiedFilesSupport newInstance(
                @NonNull final Project project,
                @NonNull final UpdateHelper helper,
                @NonNull final PropertyEvaluator evaluator) {
            return new ModifiedFilesSupport(project, helper, evaluator);
        }
    }

    private static final class JavaModelWork implements Runnable {
        private final JavaActionProvider.ScriptAction scriptAction;
        private final Context context;
        private final String userPropertiesFile;
        private final AtomicReference<Thread> caller;
        private final AtomicBoolean called;
        private final ActionProgress listener;
        /**
         * True when the action always requires access to java model
         */
        boolean needsJavaModel = true;
        /**
         * When true getTargetNames accesses java model, when false
         * the default values (possibly incorrect) are used.
         */
        boolean doJavaChecks = true;

        JavaModelWork(
                @NonNull final JavaActionProvider.ScriptAction scriptAction,
                @NonNull final Context context,
                @NullAllowed final String userPropertiesFile) {
            Parameters.notNull("scriptAction", scriptAction);   //NOI18N
            this.scriptAction = scriptAction;
            this.context = context;
            this.userPropertiesFile = userPropertiesFile;
            this.caller = new AtomicReference<>(Thread.currentThread());
            this.called  = new AtomicBoolean(false);
            // XXX prefer to call just if and when actually starting target, but that is hard to calculate here
            this.listener = ActionProgress.start(this.context.getActiveLookup());
        }

        boolean isCalled() {
            return called.get();
        }

        @Override
        public void run() {
            if (!needsJavaModel && caller.get() != Thread.currentThread()) {
                return;
            }
            called.set(true);
            ExecutorTask task = null;
            try {
                task = execute();
            } finally {
                if (task != null) {
                    task.addTaskListener((t) -> {
                        listener.finished(((ExecutorTask)t).result() == 0);
                    });
                } else {
                    listener.finished(false);
                }
            }
        }

        private ExecutorTask execute() {
            context.setProperty("nb.internal.action.name", context.getCommand());                  //NOI18N
            if (userPropertiesFile != null) {
                context.setProperty("user.properties.file", userPropertiesFile);   //NOI18N
            }
            context.setJavaChecks(doJavaChecks);
            String[] targetNames = scriptAction.getTargetNames(context);
            if (targetNames == null) {
                return null;
            }
            if (context.getCompileOnSaveOperations().contains(CompileOnSaveOperation.EXECUTE)) {
                final Result r = scriptAction.performCompileOnSave(context, targetNames);
                if (r == Result.abort()) {
                    return null;
                } else if (r != Result.follow()) {
                    final ExecutorTask t = r.getTask();
                    assert t != null;
                    return t;
                }
            }
            collectStartupExtenderArgs(context, (k,v) -> {
                context.setProperty(k, v);
                return null;
            });
            if (targetNames.length == 0) {
                targetNames = null;
            }
            if (context.getCompileOnSaveOperations().contains(CompileOnSaveOperation.UPDATE) && !NO_SYNC_COMMANDS.contains(context.getCommand())) {
                context.setProperty("nb.wait.for.caches", "true");  //NOI18N
            }
            try {
                final FileObject buildFo = getBuildScript(context);
                if (buildFo == null || !buildFo.isValid()) {
                    //The build.xml was deleted after the isActionEnabled was called
                    NotifyDescriptor nd = new NotifyDescriptor.Message(LBL_No_Build_XML_Found(), NotifyDescriptor.WARNING_MESSAGE);
                    DialogDisplayer.getDefault().notify(nd);
                } else {
                    context.fireAntTargetInvocationListener(0, 0);
                    try {
                        final Properties props = context.getProperties();
                        LOG.log(
                                Level.FINE,
                                "runTargets: {0} with pros: {1}",   //NOI18N
                                new Object[] {
                                    targetNames,
                                    props
                                });
                        final ExecutorTask task = ActionUtils.runTarget(buildFo, targetNames, props, context.getConcealedProperties());
                        task.addTaskListener((Task t) -> {
                            context.fireAntTargetInvocationListener(1, task.result());
                        });
                        return task;
                    } catch (IOException | RuntimeException ex) {
                        context.fireAntTargetInvocationListener(2, 0);
                        throw ex;
                    }
                }
            } catch (IOException e) {
                    Exceptions.printStackTrace(e);
            }
            return null;
        }
    }
}
