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
package org.netbeans.modules.php.analysis.commands;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.netbeans.api.annotations.common.CheckForNull;
import org.netbeans.api.annotations.common.NullAllowed;
import org.netbeans.api.extexecution.ExecutionDescriptor;
import org.netbeans.api.project.FileOwnerQuery;
import org.netbeans.api.project.Project;
import org.netbeans.modules.php.analysis.CodingStandardsFixerParams;
import org.netbeans.modules.php.analysis.options.AnalysisOptions;
import org.netbeans.modules.php.analysis.parsers.CodingStandardsFixerReportParser;
import org.netbeans.modules.php.analysis.results.Result;
import org.netbeans.modules.php.analysis.ui.options.AnalysisOptionsPanelController;
import org.netbeans.modules.php.api.executable.InvalidPhpExecutableException;
import org.netbeans.modules.php.api.executable.PhpExecutable;
import org.netbeans.modules.php.api.executable.PhpExecutableValidator;
import org.netbeans.modules.php.api.phpmodule.PhpModule;
import org.netbeans.modules.php.api.util.StringUtils;
import org.netbeans.modules.php.api.util.UiUtils;
import org.openide.filesystems.FileObject;
import org.openide.filesystems.FileUtil;
import org.openide.util.NbBundle;

public final class CodingStandardsFixer {

    static final Logger LOGGER = Logger.getLogger(CodingStandardsFixer.class.getName());

    public static final String NAME = "php-cs-fixer"; // NOI18N
    public static final String LONG_NAME = NAME + ".phar"; // NOI18N

    static final File XML_LOG = new File(System.getProperty("java.io.tmpdir"), "nb-php-phpcsfixer-log.xml"); // NOI18N

    // commands
    private static final String FIX_COMMAND = "fix"; // NOI18N
    private static final String LIST_COMMAND = "list"; // NOI18N
    private static final String SELF_UPDATE_COMMAND = "self-update"; // NOI18N

    // params
    private static final String ANSI_PARAM = "--ansi"; // NOI18N
    private static final String HELP_PARAM = "--help"; // NOI18N
    private static final String NO_ANSI_PARAM = "--no-ansi"; // NOI18N
    private static final String NO_INTERACTION_PARAM = "--no-interaction"; // NOI18N
    private static final String VERBOSE_PARAM = "--verbose"; // NOI18N
    private static final String DIFF_PARAM = "--diff"; // NOI18N

    private static final String DRY_RUN_PARAM = "--dry-run"; // NOI18N
    private static final String CONFIG_PARAM = "--config=%s"; // NOI18N
    private static final String LEVEL_PARAM = "--level=%s"; // NOI18N
    private static final String FIXERS_PARAM = "--fixers=%s"; // NOI18N
    private static final String FORMAT_XML_PARAM = "--format=xml"; // NOI18N
    private static final List<String> ANALYZE_DEFAULT_PARAMS = Arrays.asList(
            NO_ANSI_PARAM,
            VERBOSE_PARAM,
            DRY_RUN_PARAM,
            DIFF_PARAM,
            FORMAT_XML_PARAM,
            NO_INTERACTION_PARAM);
    private static final List<String> DEFAULT_PARAMS = Arrays.asList(
            ANSI_PARAM,
            NO_INTERACTION_PARAM);

    // configuration files
    public static final String CONFIG_FILE_NAME_V2 = ".php_cs"; // NOI18N
    public static final String DIST_CONFIG_FILE_NAME_V2 = ".php_cs.dist"; // NOI18N
    public static final String CONFIG_FILE_NAME_V3 = ".php-cs-fixer.php"; // NOI18N
    public static final String DIST_CONFIG_FILE_NAME_V3 = ".php-cs-fixer.dist.php"; // NOI18N

    @org.netbeans.api.annotations.common.SuppressWarnings(value = "MS_MUTABLE_COLLECTION", justification = "It is immutable") // NOI18N
    public static final List<String> VERSIONS = Arrays.asList(
            "1", // NOI18N
            "2" // NOI18N
    );
    @org.netbeans.api.annotations.common.SuppressWarnings(value = "MS_MUTABLE_COLLECTION", justification = "It is immutable") // NOI18N
    public static final List<String> ALL_LEVEL = Arrays.asList(
            "", // NOI18N
            "psr0", // NOI18N
            "psr1", // NOI18N
            "psr2", // NOI18N
            "symfony" // NOI18N
    );
    // XXX get from help?
    @org.netbeans.api.annotations.common.SuppressWarnings(value = "MS_MUTABLE_COLLECTION", justification = "It is immutable") // NOI18N
    public static final List<String> ALL_CONFIG = Arrays.asList(
            "", // NOI18N
            "default", // NOI18N
            "mangento", // NOI18N
            "sf23" // NOI18N
    );

    private final String codingStandardsFixerPath;

    private int analyzeGroupCounter = 1;

    private CodingStandardsFixer(String codingStandardsFixerPath) {
        this.codingStandardsFixerPath = codingStandardsFixerPath;
    }

    public static CodingStandardsFixer getDefault() throws InvalidPhpExecutableException {
        return getCustom(AnalysisOptions.getInstance().getCodingStandardsFixerPath());
    }

    public static CodingStandardsFixer getCustom(String codingStandardsFixerPath) throws InvalidPhpExecutableException {
        String error = validate(codingStandardsFixerPath);
        if (error != null) {
            throw new InvalidPhpExecutableException(error);
        }
        return new CodingStandardsFixer(codingStandardsFixerPath);
    }

    @NbBundle.Messages("CodingStandardsFixer.script.label=Coding Standards Fixer")
    public static String validate(String codingStandardsFixerPath) {
        return PhpExecutableValidator.validateCommand(codingStandardsFixerPath, Bundle.CodingStandardsFixer_script_label());
    }

    public void startAnalyzeGroup() {
        analyzeGroupCounter = 1;
    }

    @NbBundle.Messages({
        "# {0} - command",
        "CodingStandardsFixer.script.run=Coding Standards Fixer({0})",
    })
    public Future<Integer> selfUpdate(PhpModule phpModule) {
        return runCommand(phpModule, SELF_UPDATE_COMMAND, Bundle.CodingStandardsFixer_script_run(SELF_UPDATE_COMMAND));
    }

    private Future<Integer> runCommand(PhpModule phpModule, String command, String title) {
        return runCommand(phpModule, command, title, Collections.<String>emptyList());
    }

    private Future<Integer> runCommand(PhpModule phpModule, String command, String title, List<String> params) {
        PhpExecutable phpcsfixer = getExecutable(phpModule, title);
        if (phpcsfixer == null) {
            return null;
        }
        return phpcsfixer
                .additionalParameters(mergeParameters(command, DEFAULT_PARAMS, params))
                .run(getDescriptor(phpModule));
    }

    private List<String> mergeParameters(String command, List<String> defaultParams, List<String> params) {
        List<String> allParams = new ArrayList<>(defaultParams.size() + params.size() + 1);
        allParams.add(command);
        allParams.addAll(params);
        allParams.addAll(defaultParams);
        return allParams;
    }

    @NbBundle.Messages({
        "# {0} - counter",
        "CodingStandardsFixer.analyze=Coding Standards Fixer (analyze #{0})",
    })
    @CheckForNull
    public List<Result> analyze(CodingStandardsFixerParams params, FileObject file) {
        assert file.isValid() : "Invalid file given: " + file;
        try {
            File workDir = findWorkDir(file);
            Integer result = getExecutable(Bundle.CodingStandardsFixer_analyze(analyzeGroupCounter++), workDir)
                    .additionalParameters(getParameters(params, file))
                    .runAndWait(getDescriptor(), "Running coding standards fixer..."); // NOI18N
            if (result == null) {
                return null;
            }
            // if the project for the file is not found(i.e. if the workDir is not found),
            // the results are not shown in the inspector window
            FileObject root = workDir != null ? FileUtil.toFileObject(workDir) : file;
            return CodingStandardsFixerReportParser.parse(XML_LOG, root);
        } catch (CancellationException ex) {
            // cancelled
            return Collections.emptyList();
        } catch (ExecutionException ex) {
            LOGGER.log(Level.INFO, null, ex);
            UiUtils.processExecutionException(ex, AnalysisOptionsPanelController.OPTIONS_SUB_PATH);
        }
        return null;
    }

    /**
     * Finds project directory for the given file since it can contain
     * {@code .php_cs}, {@code .php_cs.dist}.
     *
     * @param file file to find project directory for
     * @return project directory or {@code null}
     */
    @CheckForNull
    private File findWorkDir(FileObject file) {
        assert file != null;
        Project project = FileOwnerQuery.getOwner(file);
        File workDir = null;
        if (project != null) {
            workDir = FileUtil.toFile(project.getProjectDirectory());
            if (LOGGER.isLoggable(Level.FINE)) {
                if (workDir != null) {
                    LOGGER.log(Level.FINE, "Project directory for {0} is found in {1}", new Object[]{FileUtil.toFile(file), workDir}); // NOI18N
                } else {
                    // the file/directory may not be in a PHP project
                    LOGGER.log(Level.FINE, "Project directory for {0} is not found", FileUtil.toFile(file)); // NOI18N
                }
            }
        }
        return workDir;
    }

    private PhpExecutable getExecutable(PhpModule phpModule, String title) {
        return new PhpExecutable(codingStandardsFixerPath)
                .optionsSubcategory(AnalysisOptionsPanelController.OPTIONS_SUB_PATH)
                .displayName(title);
    }

    private PhpExecutable getExecutable(String title, @NullAllowed File workDir) {
        PhpExecutable executable = new PhpExecutable(codingStandardsFixerPath)
                .optionsSubcategory(AnalysisOptionsPanelController.OPTIONS_SUB_PATH)
                .fileOutput(XML_LOG, "UTF-8", false) // NOI18N
                .redirectErrorStream(false)
                .displayName(title);
        if (workDir != null) {
            executable.workDir(workDir);
        }
        return executable;
    }

    private ExecutionDescriptor getDescriptor(PhpModule phpModule) {
        return PhpExecutable.DEFAULT_EXECUTION_DESCRIPTOR
                .optionsPath(AnalysisOptionsPanelController.OPTIONS_PATH);
    }

    private ExecutionDescriptor getDescriptor() {
        return PhpExecutable.DEFAULT_EXECUTION_DESCRIPTOR
                .optionsPath(AnalysisOptionsPanelController.OPTIONS_PATH)
                .frontWindowOnError(false)
                .inputVisible(false)
                .preExecution(new Runnable() {
                    @Override
                    public void run() {
                        if (XML_LOG.isFile()) {
                            if (!XML_LOG.delete()) {
                                LOGGER.log(Level.INFO, "Cannot delete log file {0}", XML_LOG.getAbsolutePath());
                            }
                        }
                    }
                });
    }

    private List<String> getParameters(CodingStandardsFixerParams parameters, FileObject file) {
        // fix /path/to/{dir|file}
        List<String> params = new ArrayList<>();
        params.add(FIX_COMMAND);
        params.addAll(ANALYZE_DEFAULT_PARAMS);
        String version = parameters.getVersion();
        if ("1".equals(version)) { // NOI18N
            String level = parameters.getLevel();
            if (!StringUtils.isEmpty(level)) {
                params.add(String.format(LEVEL_PARAM, level));
            }
            String config = parameters.getConfig();
            if (!StringUtils.isEmpty(config)) {
                params.add(String.format(CONFIG_PARAM, config));
            }
        }
        String options = parameters.getOptions();
        if (!StringUtils.isEmpty(options)) {
            params.addAll(StringUtils.explode(options, " ")); // NOI18N
        }
        params.add(FileUtil.toFile(file).getAbsolutePath());
        return params;
    }

}
