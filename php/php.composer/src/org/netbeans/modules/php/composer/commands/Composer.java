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
package org.netbeans.modules.php.composer.commands;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.extexecution.base.input.InputProcessor;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.executable.PhpExecutable;
import org.netbeans.modules.php.api.executable.PhpExecutableValidator;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.netbeans.modules.php.api.validation.ValidationResult;
import org.netbeans.modules.php.composer.options.ComposerOptions;
import org.netbeans.modules.php.composer.options.ComposerOptionsValidator;
import org.netbeans.modules.php.composer.output.model.SearchResult;
import org.netbeans.modules.php.composer.output.parsers.Parsers;
import org.netbeans.modules.php.composer.ui.options.ComposerOptionsPanelController;
import org.netbeans.modules.php.composer.util.ComposerUtils;
import org.openide.DialogDescriptor;
import org.openide.DialogDisplayer;
import org.openide.NotifyDescriptor;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;
import org.openide.util.Utilities;

/**
 * Represents <a href="http://getcomposer.org/">Composer</a> command line tool.
 */
public final class Composer {

    static final Logger LOGGER = Logger.getLogger(Composer.class.getName());

    public static final String COMPOSER_FILENAME = "composer.json"; // NOI18N

    public static final List<String> COMPOSER_FILENAMES;

    private static final String COMPOSER = "composer"; // NOI18N
    private static final String COMPOSER_PHAR = COMPOSER + ".phar"; // NOI18N
    private static final String COMPOSER_BAT = COMPOSER + ".bat"; // NOI18N

    // commands
    private static final String INIT_COMMAND = "init"; // NOI18N
    private static final String INSTALL_COMMAND = "install"; // NOI18N
    private static final String UPDATE_COMMAND = "update"; // NOI18N
    private static final String UPDATE_AUTOLOADER_COMMAND = "dump-autoload"; // NOI18N
    private static final String REQUIRE_COMMAND = "require"; // NOI18N
    private static final String RUN_SCRIPT_COMMAND = "run-script"; // NOI18N
    private static final String VALIDATE_COMMAND = "validate"; // NOI18N
    private static final String SELF_UPDATE_COMMAND = "self-update"; // NOI18N
    private static final String SEARCH_COMMAND = "search"; // NOI18N
    private static final String SHOW_COMMAND = "show"; // NOI18N
    // params
    private static final String ANSI_PARAM = "--ansi"; // NOI18N
    private static final String NO_ANSI_PARAM = "--no-ansi"; // NOI18N
    private static final String NO_INTERACTION_PARAM = "--no-interaction"; // NOI18N
    private static final String NAME_PARAM = "--name=%s"; // NOI18N
    private static final String AUTHOR_PARAM = "--author=%s <%s>"; // NOI18N
    private static final String DESCRIPTION_PARAM = "--description=%s"; // NOI18N
    private static final String DEV_PARAM = "--dev"; // NOI18N
    private static final String NO_DEV_PARAM = "--no-dev"; // NOI18N
    private static final String ONLY_NAME_PARAM = "--only-name"; // NOI18N
    private static final String ALL_PARAM = "--all"; // NOI18N
    private static final List<String> DEFAULT_PARAMS = Arrays.asList(
        ANSI_PARAM,
        NO_INTERACTION_PARAM
    );

    private final String composerPath;

    private volatile File workDir;


    static {
        // #243767
        List<String> fileNames = new ArrayList<>(2);
        if (Utilities.isWindows()) {
            fileNames.add(Composer.COMPOSER_BAT);
            fileNames.add(Composer.COMPOSER_PHAR);
        } else {
            fileNames.add(Composer.COMPOSER);
            fileNames.add(Composer.COMPOSER_PHAR);
        }
        COMPOSER_FILENAMES = new CopyOnWriteArrayList<>(fileNames);
    }

    public Composer(String composerPath) {
        this.composerPath = composerPath;
    }

    /**
     * Get the default, <b>valid only</b> Composer.
     * @return the default, <b>valid only</b> Composer.
     * @throws InvalidPhpExecutableException if Composer is not valid.
     */
    public static Composer getDefault() throws InvalidPhpExecutableException {
        String composerPath = ComposerOptions.getInstance().getComposerPath();
        String error = validate(composerPath);
        if (error != null) {
            throw new InvalidPhpExecutableException(error);
        }
        return new Composer(composerPath);
    }

    @NbBundle.Messages("Composer.script.label=Composer")
    public static String validate(String composerPath) {
        return PhpExecutableValidator.validateCommand(composerPath, Bundle.Composer_script_label());
    }

    public static boolean isValidOutput(String output) {
        if (output.startsWith("Warning:") // NOI18N
                || output.startsWith("No composer.json found")) { // NOI18N
            return false;
        }
        return true;
    }

    public Future<Integer> initIfNotPresent(PhpModule phpModule) {
        assert phpModule != null;
        FileObject composerJson = getComposerJson(phpModule);
        if (composerJson != null
                && composerJson.isValid()) {
            return null;
        }
        return init(phpModule);
    }

    @NbBundle.Messages({
        "Composer.file.exists=Composer.json already exists - overwrite it?",
        "# {0} - project name",
        "Composer.init.description=Description of project {0}.",
    })
    public Future<Integer> init(PhpModule phpModule) {
        assert phpModule != null;
        // composer.json
        FileObject composerJson = getComposerJson(phpModule);
        if (composerJson != null
                && composerJson.isValid()) {
            // existing config
            if (!userConfirmation(phpModule.getDisplayName(), Bundle.Composer_file_exists())) {
                return null;
            }
        }
        ComposerOptions options = ComposerOptions.getInstance();
        // validation
        ValidationResult result = new ComposerOptionsValidator()
                .validate(options)
                .getResult();
        if (!result.isFaultless()) {
            UiUtils.showOptions(ComposerOptionsPanelController.OPTIONS_SUBPATH);
            return null;
        }
        // command params
        List<String> params = Arrays.asList(
                String.format(NAME_PARAM, getInitName(options.getVendor(), phpModule.getName())),
                String.format(AUTHOR_PARAM, options.getAuthorName(), options.getAuthorEmail()),
                String.format(DESCRIPTION_PARAM, Bundle.Composer_init_description(phpModule.getDisplayName())));
        return runCommand(phpModule, true, INIT_COMMAND, params);
    }

    private String getInitName(String vendor, String projectName) {
        StringBuilder name = new StringBuilder(50);
        name.append(vendor);
        name.append('/'); // NOI18N
        name.append(StringUtils.webalize(projectName));
        return name.toString();
    }

    public Future<Integer> install(PhpModule phpModule) {
        assert phpModule != null;
        return runCommand(phpModule, INSTALL_COMMAND);
    }

    public Future<Integer> installDev(PhpModule phpModule) {
        return install(phpModule);
    }

    public Future<Integer> installNoDev(PhpModule phpModule) {
        assert phpModule != null;
        return runCommand(phpModule, INSTALL_COMMAND, Collections.singletonList(NO_DEV_PARAM));
    }

    public Future<Integer> update(PhpModule phpModule) {
        assert phpModule != null;
        return runCommand(phpModule, UPDATE_COMMAND);
    }

    public Future<Integer> updateDev(PhpModule phpModule) {
        return update(phpModule);
    }

    public Future<Integer> updateNoDev(PhpModule phpModule) {
        assert phpModule != null;
        return runCommand(phpModule, UPDATE_COMMAND, Collections.singletonList(NO_DEV_PARAM));
    }

    public Future<Integer> updateAutoloader(PhpModule phpModule) {
        assert phpModule != null;
        return runCommand(phpModule, UPDATE_AUTOLOADER_COMMAND);
    }

    public Future<Integer> updateAutoloaderDev(PhpModule phpModule) {
        return updateAutoloader(phpModule);
    }

    public Future<Integer> updateAutoloaderNoDev(PhpModule phpModule) {
        assert phpModule != null;
        return runCommand(phpModule, UPDATE_AUTOLOADER_COMMAND, Collections.singletonList(NO_DEV_PARAM));
    }

    public Future<Integer> require(PhpModule phpModule, String... packages) {
        assert phpModule != null;
        return runCommand(phpModule, REQUIRE_COMMAND, Arrays.asList(packages));
    }

    public Future<Integer> requireDev(PhpModule phpModule, String... packages) {
        assert phpModule != null;
        List<String> params = new ArrayList<>(packages.length + 1);
        params.add(DEV_PARAM);
        params.addAll(Arrays.asList(packages));
        return runCommand(phpModule, REQUIRE_COMMAND, params);
    }

    public Future<Integer> runScript(PhpModule phpModule, String scriptName) {
        assert phpModule != null;
        return runCommand(phpModule, RUN_SCRIPT_COMMAND, Collections.singletonList(scriptName));
    }

    public Future<Integer> validate(PhpModule phpModule) {
        assert phpModule != null;
        return runCommand(phpModule, VALIDATE_COMMAND);
    }

    public Future<Integer> selfUpdate() {
        return runCommand(null, SELF_UPDATE_COMMAND);
    }

    public Future<Integer> search(@NullAllowed PhpModule phpModule, String token, boolean onlyName, final OutputProcessor<SearchResult> outputProcessor) {
        PhpExecutable composer = getComposerExecutable(phpModule, false);
        if (composer == null) {
            return null;
        }
        // params
        List<String> defaultParams = new ArrayList<>(DEFAULT_PARAMS);
        defaultParams.remove(ANSI_PARAM);
        defaultParams.add(NO_ANSI_PARAM);
        List<String> params = new ArrayList<>(2);
        if (onlyName) {
            params.add(ONLY_NAME_PARAM);
        }
        params.add(token);
        composer = composer
                .additionalParameters(mergeParameters(SEARCH_COMMAND, defaultParams, params))
                // avoid parser confusion
                .redirectErrorStream(false);
        // descriptor
        ExecutionDescriptor descriptor = getDescriptor(phpModule)
                .frontWindow(false);
        // run
        return composer
                .run(descriptor, new ExecutionDescriptor.InputProcessorFactory2() {
                    @Override
                    public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                        return new OutputProcessorImpl(new OutputParser() {
                            @Override
                            public void parse(char[] chars) {
                                for (SearchResult result : Parsers.parseSearch(new String(chars))) {
                                    outputProcessor.process(result);
                                }
                            }
                        });
                    }
                });
    }

    public Future<Integer> show(@NullAllowed PhpModule phpModule, String name, final OutputProcessor<String> outputProcessor) {
        PhpExecutable composer = getComposerExecutable(phpModule, false);
        if (composer == null) {
            return null;
        }
        // params
        List<String> defaultParams = new ArrayList<>(DEFAULT_PARAMS);
        defaultParams.remove(ANSI_PARAM);
        defaultParams.add(NO_ANSI_PARAM);
        composer = composer
                .additionalParameters(mergeParameters(SHOW_COMMAND, defaultParams, Arrays.asList(ALL_PARAM, name)))
                // avoid parser confusion
                .redirectErrorStream(false);
        // descriptor
        ExecutionDescriptor descriptor = getDescriptor(phpModule)
                .frontWindow(false);
        // run
        return composer
                .run(descriptor, new ExecutionDescriptor.InputProcessorFactory2() {
                    @Override
                    public InputProcessor newInputProcessor(InputProcessor defaultProcessor) {
                        return new OutputProcessorImpl(new OutputParser() {
                            @Override
                            public void parse(char[] chars) {
                                String chunk = new String(chars);
                                if (!isValidOutput(chunk)) {
                                    return;
                                }
                                outputProcessor.process(chunk);
                            }
                        });
                    }
                });
    }

    private Future<Integer> runCommand(@NullAllowed PhpModule phpModule, String command) {
        return runCommand(phpModule, command, Collections.<String>emptyList());
    }

    private Future<Integer> runCommand(@NullAllowed PhpModule phpModule, String command, List<String> commandParams) {
        return runCommand(phpModule, false, command, commandParams);
    }

    private Future<Integer> runCommand(@NullAllowed PhpModule phpModule, boolean forceProjectDir, String command, List<String> commandParams) {
        PhpExecutable composer = getComposerExecutable(phpModule, forceProjectDir);
        if (composer == null) {
            return null;
        }
        return composer
                .additionalParameters(mergeParameters(command, DEFAULT_PARAMS, commandParams))
                .run(getDescriptor(phpModule));
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "Composer.run.title=Composer ({0})",
        "Composer.run.title.pure=Composer",
    })
    @CheckForNull
    private PhpExecutable getComposerExecutable(@NullAllowed PhpModule phpModule, boolean forceProjectDir) {
        File dir = resolveWorkDir(phpModule, forceProjectDir);
        if (dir == null
                && phpModule != null) {
            warnNoSources(phpModule.getDisplayName());
            return null;
        }
        PhpExecutable composer = new PhpExecutable(composerPath)
                .optionsSubcategory(ComposerOptionsPanelController.OPTIONS_SUBPATH)
                .displayName(phpModule != null ? Bundle.Composer_run_title(phpModule.getDisplayName()) : Bundle.Composer_run_title_pure());
        if (dir != null) {
            composer.workDir(dir);
        }
        return composer;
    }

    private List<String> mergeParameters(String command, List<String> defaultParams, List<String> commandParams) {
        List<String> allParams = new ArrayList<>(defaultParams.size() + commandParams.size() + 1);
        allParams.addAll(defaultParams);
        allParams.add(command);
        allParams.addAll(commandParams);
        return allParams;
    }

    private ExecutionDescriptor getDescriptor(@NullAllowed PhpModule phpModule) {
        ExecutionDescriptor descriptor = PhpExecutable.DEFAULT_EXECUTION_DESCRIPTOR
                .optionsPath(ComposerOptionsPanelController.getOptionsPath())
                .inputVisible(false);
        if (phpModule != null) {
            final FileObject sourceDirectory = phpModule.getSourceDirectory();
            if (sourceDirectory != null) {
                descriptor = descriptor
                        .postExecution(new Runnable() {
                            @Override
                            public void run() {
                                // refresh sources after running command
                                sourceDirectory.refresh();
                            }
                        });
            }
        }
        return descriptor;
    }

    @NbBundle.Messages({
        "# {0} - project name",
        "Composer.project.noSources=Project {0} has no Source Files."
    })
    private static void warnNoSources(String projectName) {
        DialogDisplayer.getDefault().notifyLater(
                new NotifyDescriptor.Message(Bundle.Composer_project_noSources(projectName), NotifyDescriptor.WARNING_MESSAGE));
    }

    @CheckForNull
    private FileObject getComposerJson(PhpModule phpModule) {
        assert phpModule != null;
        if (workDir != null) {
            FileObject fo = FileUtil.toFileObject(workDir);
            if (fo == null) {
                // invalid workdir?
                LOGGER.log(Level.INFO, "Valid workdir expected but invalid given: {0}", workDir);
                return null;
            }
            return fo.getFileObject(COMPOSER_FILENAME);
        }
        return ComposerUtils.getComposerWorkDir(phpModule)
                .getFileObject(COMPOSER_FILENAME);
    }

    private boolean userConfirmation(String title, String question) {
        NotifyDescriptor confirmation = new DialogDescriptor.Confirmation(question, title, DialogDescriptor.YES_NO_OPTION);
        return DialogDisplayer.getDefault().notify(confirmation) == DialogDescriptor.YES_OPTION;
    }

    @CheckForNull
    private File resolveWorkDir(PhpModule phpModule, boolean forceProjectDir) {
        if (workDir != null) {
            return workDir;
        }
        if (phpModule == null) {
            return null;
        }
        FileObject composerJson = getComposerJson(phpModule);
        if (composerJson != null) {
            return FileUtil.toFile(composerJson.getParent());
        }
        FileObject dir = forceProjectDir ? phpModule.getProjectDirectory() : phpModule.getSourceDirectory();
        if (dir == null) {
            // broken project
            return null;
        }
        return FileUtil.toFile(dir);
    }

    public File getWorkDir() {
        return workDir;
    }

    public void setWorkDir(File workDir) {
        assert workDir == null || workDir.isDirectory() : "Existing directory or null expected: " + workDir;
        this.workDir = workDir;
    }

    //~ Inner classes

    public interface OutputProcessor<T> {
        void process(T item);
    }

    private interface OutputParser {
        void parse(char[] chars);
    }

    private static final class OutputProcessorImpl implements InputProcessor {

        private final OutputParser outputParser;


        public OutputProcessorImpl(OutputParser outputParser) {
            this.outputParser = outputParser;
        }

        @Override
        public void processInput(char[] chars) throws IOException {
            outputParser.parse(chars);
        }

        @Override
        public void reset() throws IOException {
            // noop
        }

        @Override
        public void close() throws IOException {
            // noop
        }

    }

}
